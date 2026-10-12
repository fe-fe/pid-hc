package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.ImportacaoAnvisaDao;
import br.ufpr.pid.hc.dao.ProdutoAnvisaDao;
import br.ufpr.pid.hc.entity.ImportacaoAnvisa;
import br.ufpr.pid.hc.entity.ProdutoAnvisa;
import br.ufpr.pid.hc.enumeration.ResultadoImportacaoAnvisa;
import jakarta.annotation.Resource;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Schedule;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ImportacaoAnvisaService {

    private static final String URL = "https://dados.anvisa.gov.br/dados/TA_PRODUTO_SAUDE_SITE.csv";
    private static final Charset CODIFICACAO = Charset.forName("windows-1252");
    private static final DateTimeFormatter FORMATO_DATA_ATUALIZACAO = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
    private static final Duration INTERVALO_ENTRE_ATUALIZACOES = Duration.ofHours(2);
    private static final List<String> CABECALHO = List.of(
            "NUMERO_REGISTRO_CADASTRO", "NUMERO_PROCESSO", "NOME_TECNICO", "CLASSE_RISCO",
            "NOME_COMERCIAL", "CNPJ_DETENTOR_REGISTRO_CADASTRO", "DETENTOR_REGISTRO_CADASTRO",
            "NOME_FABRICANTE", "NOME_PAIS_FABRIC", "DT_PUB_REGISTRO_CADASTRO",
            "VALIDADE_REGISTRO_CADASTRO", "DT_ATUALIZACAO_DADO");

    @Inject
    private ImportacaoAnvisaDao importacaoAnvisaDao;

    @Inject
    private ProdutoAnvisaDao produtoAnvisaDao;

    @Resource
    private SessionContext contexto;

    private final AtomicBoolean emAndamento = new AtomicBoolean(false);
    private volatile LocalDateTime ultimaTentativa;

    @Schedule(dayOfWeek = "Mon", hour = "3", persistent = false)
    public void importarAgendado() {
        log.info("Importação agendada da base ANVISA: {}", importar());
    }

    public Optional<ImportacaoAnvisa> buscarUltima() {
        return importacaoAnvisaDao.buscarUltima();
    }

    public List<ProdutoAnvisa> buscarProdutos(String termo, int pagina, int tamanhoPagina) {
        return produtoAnvisaDao.buscar(termo, pagina, tamanhoPagina);
    }

    public List<ProdutoAnvisa> buscarPorRegistro(String registro) {
        return produtoAnvisaDao.buscarPorRegistro(registro);
    }

    public long contarProdutos(String termo) {
        return produtoAnvisaDao.contar(termo);
    }

    public LocalDateTime getProximaAtualizacaoPermitida() {
        LocalDateTime tentativa = ultimaTentativa;
        return tentativa == null ? null : tentativa.plus(INTERVALO_ENTRE_ATUALIZACOES);
    }

    public boolean isAtualizacaoPermitida() {
        LocalDateTime proxima = getProximaAtualizacaoPermitida();
        return proxima == null || !LocalDateTime.now().isBefore(proxima);
    }

    public ResultadoImportacaoAnvisa importar() {
        if (!isAtualizacaoPermitida()) {
            return ResultadoImportacaoAnvisa.AGUARDANDO_INTERVALO;
        }
        if (!emAndamento.compareAndSet(false, true)) {
            return ResultadoImportacaoAnvisa.EM_ANDAMENTO;
        }

        Path arquivo = null;
        try {
            arquivo = Files.createTempFile("anvisa", ".csv");
            baixar(arquivo);
            String sha256 = calcularSha256(arquivo);

            boolean semAlteracao = importacaoAnvisaDao.buscarUltima()
                    .map(ultima -> ultima.getSha256().equals(sha256))
                    .orElse(false);
            if (semAlteracao) {
                log.info("Base ANVISA sem alteração (sha256 {})", sha256);
                ultimaTentativa = LocalDateTime.now();
                return ResultadoImportacaoAnvisa.SEM_ALTERACAO;
            }

            contexto.getBusinessObject(ImportacaoAnvisaService.class).gravar(arquivo, sha256);
            ultimaTentativa = LocalDateTime.now();
            return ResultadoImportacaoAnvisa.IMPORTADA;
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Falha ao baixar a base ANVISA", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Importação da base ANVISA interrompida", e);
        } finally {
            emAndamento.set(false);
            apagar(arquivo);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void gravar(Path arquivo, String sha256) {
        try (BufferedReader leitor = abrir(arquivo)) {
            if (!String.join(";", CABECALHO).equals(leitor.readLine())) {
                throw new IllegalStateException("Cabeçalho do CSV da ANVISA mudou");
            }
            long total = produtoAnvisaDao.substituirTodos(
                    CABECALHO.stream().map(coluna -> coluna.toLowerCase(Locale.ROOT)).toList(),
                    leitor.lines().filter(registro -> !registro.isEmpty()).map(this::separar));
            if (total == 0) {
                throw new IllegalStateException("CSV da ANVISA sem linhas");
            }

            ImportacaoAnvisa importacao = new ImportacaoAnvisa();
            importacao.setSha256(sha256);
            importacao.setDtAtualizacaoDado(lerDataAtualizacao(arquivo));
            importacao.setImportadoEm(LocalDateTime.now());
            importacaoAnvisaDao.salvar(importacao);

            log.info("Base ANVISA importada: {} linhas, sha256 {}", total, sha256);
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Falha ao gravar a base ANVISA", e);
        }
    }

    private void baixar(Path destino) throws IOException, InterruptedException {
        HttpResponse<Path> resposta = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
                .send(HttpRequest.newBuilder(URI.create(URL)).GET().build(), HttpResponse.BodyHandlers.ofFile(destino));
        if (resposta.statusCode() != 200) {
            throw new IOException("ANVISA respondeu HTTP " + resposta.statusCode());
        }
    }

    private String calcularSha256(Path arquivo) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream entrada = new DigestInputStream(Files.newInputStream(arquivo), digest)) {
            entrada.transferTo(OutputStream.nullOutputStream());
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private BufferedReader abrir(Path arquivo) throws IOException {
        return new BufferedReader(new InputStreamReader(Files.newInputStream(arquivo), CODIFICACAO));
    }

    private LocalDateTime lerDataAtualizacao(Path arquivo) throws IOException {
        try (BufferedReader leitor = abrir(arquivo)) {
            leitor.readLine();
            String[] primeiraLinha = separar(leitor.readLine());
            return LocalDateTime.parse(primeiraLinha[CABECALHO.indexOf("DT_ATUALIZACAO_DADO")], FORMATO_DATA_ATUALIZACAO);
        }
    }

    private String[] separar(String registro) {
        String[] campos = registro.split(";", -1);
        if (campos.length != CABECALHO.size()) {
            throw new IllegalStateException("Linha com " + campos.length + " colunas: " + registro);
        }
        return campos;
    }

    private void apagar(Path arquivo) {
        if (arquivo == null) {
            return;
        }
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            log.warn("Não foi possível apagar o arquivo temporário {}", arquivo, e);
        }
    }
}
