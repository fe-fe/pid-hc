package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.DesvioDao;
import br.ufpr.pid.hc.dao.ImportacaoTecnovigilanciaDao;
import br.ufpr.pid.hc.dao.MaterialDao;
import br.ufpr.pid.hc.entity.Desvio;
import br.ufpr.pid.hc.entity.Fabricante;
import br.ufpr.pid.hc.entity.ImportacaoTecnovigilancia;
import br.ufpr.pid.hc.entity.Marca;
import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.ColunaTecnovigilancia;
import br.ufpr.pid.hc.enumeration.Desfecho;
import br.ufpr.pid.hc.enumeration.EnvioAmostra;
import br.ufpr.pid.hc.enumeration.GrauDano;
import br.ufpr.pid.hc.enumeration.MaterialOrdenacao;
import br.ufpr.pid.hc.exception.DomainException;
import br.ufpr.pid.hc.exception.PlanilhaInvalidaException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static br.ufpr.pid.hc.enumeration.ColunaTecnovigilancia.*;

@Stateless
public class ImportacaoTecnovigilanciaService extends AbstractService<ImportacaoTecnovigilancia, UUID> {

    private static final String ABA_DESVIOS = "Desvios";
    private static final String ABA_INSTRUCOES = "Instruções";
    private static final String ABA_LISTAS = "Listas";
    private static final int LINHAS_COM_VALIDACAO = 5000;
    private static final int TAMANHO_MAXIMO_TEXTO = 255;
    private static final int TAMANHO_MAXIMO_VALOR_EXIBIDO = 80;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final Pattern REGISTRO_ANVISA_VALIDO = Pattern.compile("\\d{11}");
    private static final Pattern PROCESSO_SEI_VALIDO = Pattern.compile("\\d{5}\\.\\d{6}/\\d{4}-\\d{2}");

    @Inject
    private ImportacaoTecnovigilanciaDao importacaoTecnovigilanciaDao;

    @Inject
    private DesvioDao desvioDao;

    @Inject
    private MaterialDao materialDao;

    @Inject
    private FabricanteService fabricanteService;

    @Inject
    private MarcaService marcaService;

    @Inject
    private MaterialService materialService;

    @Override
    protected AbstractDao<ImportacaoTecnovigilancia, UUID> getDao() { return importacaoTecnovigilanciaDao; }

    @RolesAllowed({"ADMINISTRADOR", "ANALISTA"})
    public byte[] gerarModelo() {
        List<Material> materiais = materialDao.buscar(0, Integer.MAX_VALUE, MaterialOrdenacao.CODIGO, false);
        Map<ColunaTecnovigilancia, List<String>> opcoes = new LinkedHashMap<>();
        opcoes.put(CODIGO_MATERIAL, materiais.stream().map(Material::getCodigo).toList());
        opcoes.put(FABRICANTE, fabricanteService.listarNomes());
        opcoes.put(AMOSTRA, Arrays.stream(EnvioAmostra.values()).map(EnvioAmostra::getRotulo).toList());
        opcoes.put(DESFECHO, Arrays.stream(Desfecho.values()).map(Desfecho::getRotulo).toList());
        opcoes.put(GRAU_DANO, Arrays.stream(GrauDano.values()).map(GrauDano::getRotulo).toList());

        try (XSSFWorkbook planilha = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            XSSFSheet desvios = planilha.createSheet(ABA_DESVIOS);
            XSSFSheet instrucoes = planilha.createSheet(ABA_INSTRUCOES);
            XSSFSheet listas = planilha.createSheet(ABA_LISTAS);
            Map<ColunaTecnovigilancia, String> faixasDasListas = escreverListas(planilha, listas, materiais, opcoes);
            escreverDesvios(planilha, desvios, faixasDasListas);
            escreverInstrucoes(planilha, instrucoes);
            planilha.setActiveSheet(0);
            planilha.write(saida);
            return saida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gerar o modelo de tecnovigilância", e);
        }
    }

    @RolesAllowed({"ADMINISTRADOR", "ANALISTA"})
    public Resultado importar(String nomeArquivo, byte[] conteudo, Usuario usuario) {
        String sha256 = calcularSha256(conteudo);
        importacaoTecnovigilanciaDao.buscarPorSha256(sha256).ifPresent(anterior -> {
            throw new DomainException("Esta planilha já foi importada em "
                    + anterior.getCriadoEm().format(FORMATO_DATA_HORA) + ".");
        });

        Leitura leitura = ler(conteudo);
        List<LinhaLida> linhas = leitura.linhas();
        if (linhas.isEmpty()) {
            return new Resultado(0, List.of(), List.of(), leitura.vigihospIgnorados());
        }

        ImportacaoTecnovigilancia importacao = new ImportacaoTecnovigilancia();
        importacao.setNomeArquivo(nomeArquivo);
        importacao.setSha256(sha256);
        importacao.setTotalRegistros(linhas.size());
        importacaoTecnovigilanciaDao.salvar(importacao, usuario);

        Map<String, Material> materiais = materialDao
                .buscar(0, Integer.MAX_VALUE, MaterialOrdenacao.CODIGO, true).stream()
                .collect(Collectors.toMap(Material::getCodigo, material -> material));
        Map<String, Fabricante> fabricantes = new HashMap<>();
        Map<String, Marca> marcas = new HashMap<>();
        Set<String> materiaisNovos = new LinkedHashSet<>();
        Set<String> materiaisDivergentes = new LinkedHashSet<>();
        for (LinhaLida linha : linhas) {
            Desvio desvio = linha.desvio();
            Material material = materiais.get(linha.codigoMaterial());
            if (material == null) {
                material = materialService.obterOuCriar(linha.codigoMaterial(), linha.nomeMaterial());
                materiais.put(linha.codigoMaterial(), material);
                materiaisNovos.add(linha.codigoMaterial());
            } else if (!padronizarNome(material.getNome()).equals(padronizarNome(linha.nomeMaterial()))) {
                materiaisDivergentes.add(linha.codigoMaterial());
            }
            desvio.setMaterial(material);
            desvio.setFabricante(fabricantes.computeIfAbsent(padronizarNome(linha.fabricante()),
                    fabricanteService::obterOuCriar));
            if (linha.marca() != null) {
                desvio.setMarca(marcas.computeIfAbsent(padronizarNome(linha.marca()), marcaService::obterOuCriar));
            }
            desvio.setImportacao(importacao);
            desvioDao.salvar(desvio, usuario);
        }
        return new Resultado(linhas.size(), List.copyOf(materiaisNovos), List.copyOf(materiaisDivergentes),
                leitura.vigihospIgnorados());
    }

    private Leitura ler(byte[] conteudo) {
        try (XSSFWorkbook planilha = abrir(conteudo)) {
            XSSFSheet aba = planilha.getSheet(ABA_DESVIOS);
            if (aba == null) {
                List<String> abas = new ArrayList<>();
                planilha.forEach(existente -> abas.add("\"" + existente.getSheetName() + "\""));
                throw new DomainException("A planilha não tem a aba \"" + ABA_DESVIOS + "\" (abas encontradas: "
                        + String.join(", ", abas) + "). Use o modelo baixado nesta página.");
            }
            return lerLinhas(aba, localizarColunas(aba.getRow(0)));
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao fechar a planilha de tecnovigilância", e);
        }
    }

    private XSSFWorkbook abrir(byte[] conteudo) {
        try {
            return new XSSFWorkbook(new ByteArrayInputStream(conteudo));
        } catch (IOException | RuntimeException e) {
            throw new DomainException("Não foi possível ler o arquivo. Envie a planilha .xlsx do modelo baixado nesta página.");
        }
    }

    private Map<ColunaTecnovigilancia, Integer> localizarColunas(Row cabecalho) {
        Map<String, ColunaTecnovigilancia> porTitulo = Arrays.stream(ColunaTecnovigilancia.values())
                .collect(Collectors.toMap(ColunaTecnovigilancia::getTitulo, coluna -> coluna));
        Map<ColunaTecnovigilancia, Integer> posicoes = new EnumMap<>(ColunaTecnovigilancia.class);
        List<PlanilhaInvalidaException.Erro> erros = new ArrayList<>();
        DataFormatter formatador = new DataFormatter();

        int totalColunas = cabecalho == null ? 0 : cabecalho.getLastCellNum();
        for (int i = 0; i < totalColunas; i++) {
            Cell celula = cabecalho.getCell(i);
            String lido = celula == null ? "" : normalizar(formatador.formatCellValue(celula));
            if (lido.isEmpty()) {
                continue;
            }
            String letra = CellReference.convertNumToColString(i);
            ColunaTecnovigilancia coluna = porTitulo.get(lido);
            if (coluna == null) {
                erros.add(new PlanilhaInvalidaException.Erro(1, letra, encurtar(lido),
                        "Cabeçalho fora do modelo; não renomeie nem acrescente colunas na aba " + ABA_DESVIOS));
                continue;
            }
            Integer primeira = posicoes.putIfAbsent(coluna, i);
            if (primeira != null) {
                erros.add(new PlanilhaInvalidaException.Erro(1, letra, lido,
                        "Coluna repetida; já aparece na coluna " + CellReference.convertNumToColString(primeira)));
            }
        }

        for (ColunaTecnovigilancia coluna : ColunaTecnovigilancia.values()) {
            if (!posicoes.containsKey(coluna)) {
                erros.add(new PlanilhaInvalidaException.Erro(1, coluna.getTitulo(), "",
                        "Coluna ausente no cabeçalho; não apague nem renomeie colunas do modelo"));
            }
        }

        if (!erros.isEmpty()) {
            throw new PlanilhaInvalidaException(erros);
        }
        return posicoes;
    }

    private Leitura lerLinhas(XSSFSheet aba, Map<ColunaTecnovigilancia, Integer> posicoes) {
        Referencias referencias = new Referencias();
        List<PlanilhaInvalidaException.Erro> erros = new ArrayList<>();
        Map<String, Integer> linhaPorVigihosp = new HashMap<>();
        List<LinhaLida> linhas = new ArrayList<>();

        for (int i = 1; i <= aba.getLastRowNum(); i++) {
            XSSFRow linha = aba.getRow(i);
            if (isVazia(linha)) {
                continue;
            }
            LeitorLinha leitor = new LeitorLinha(linha, posicoes, erros);
            LinhaLida lida = leitor.ler(referencias);
            Desvio desvio = lida.desvio();

            if (desvio.getVigihosp() != null) {
                Integer repetida = linhaPorVigihosp.putIfAbsent(desvio.getVigihosp().toUpperCase(), leitor.numero);
                if (repetida != null) {
                    leitor.erro(VIGIHOSP, desvio.getVigihosp(), "VigiHosp repetido na planilha (também na linha " + repetida + ")");
                }
            }
            linhas.add(lida);
        }

        if (linhas.isEmpty()) {
            throw new DomainException("A aba " + ABA_DESVIOS + " não tem nenhuma linha preenchida.");
        }

        Set<String> existentes = desvioDao.listarVigihospExistentes(linhaPorVigihosp.keySet()).stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        Set<Integer> numerosIgnorados = new HashSet<>();
        Set<String> vigihospIgnorados = new LinkedHashSet<>();
        List<LinhaLida> novas = new ArrayList<>();
        for (LinhaLida lida : linhas) {
            String vigihosp = lida.desvio().getVigihosp();
            if (vigihosp != null && existentes.contains(vigihosp.toUpperCase())) {
                numerosIgnorados.add(lida.numero());
                vigihospIgnorados.add(vigihosp);
            } else {
                novas.add(lida);
            }
        }
        erros.removeIf(erro -> numerosIgnorados.contains(erro.getLinha()));

        if (!erros.isEmpty()) {
            erros.sort(Comparator.comparingInt(PlanilhaInvalidaException.Erro::getLinha));
            throw new PlanilhaInvalidaException(erros);
        }
        return new Leitura(novas, List.copyOf(vigihospIgnorados));
    }

    private boolean isVazia(Row linha) {
        if (linha == null) {
            return true;
        }
        for (Cell celula : linha) {
            if (!isCelulaVazia(celula)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isCelulaVazia(Cell celula) {
        return celula == null
                || celula.getCellType() == CellType.BLANK
                || celula.getCellType() == CellType.STRING && normalizar(celula.getStringCellValue()).isEmpty();
    }

    private static String normalizar(String valor) {
        return valor.replace(' ', ' ').trim().replaceAll("\\s+", " ");
    }

    private static String normalizarTextoLongo(String valor) {
        return valor.replace(' ', ' ').replace("\r\n", "\n").strip();
    }

    private static String encurtar(String valor) {
        return valor.length() > TAMANHO_MAXIMO_VALOR_EXIBIDO
                ? valor.substring(0, TAMANHO_MAXIMO_VALOR_EXIBIDO) + "…"
                : valor;
    }

    private static <E extends Enum<E>> Map<String, E> porRotulo(E[] valores, Function<E, String> rotulo) {
        return Arrays.stream(valores).collect(Collectors.toMap(rotulo, valor -> valor));
    }

    public record Resultado(int importados, List<String> materiaisNovos, List<String> materiaisDivergentes,
            List<String> vigihospIgnorados) {}

    private record Leitura(List<LinhaLida> linhas, List<String> vigihospIgnorados) {}

    private record LinhaLida(int numero, Desvio desvio, String codigoMaterial, String nomeMaterial, String fabricante, String marca) {}

    private class Referencias {
        private final Map<String, EnvioAmostra> amostras = porRotulo(EnvioAmostra.values(), EnvioAmostra::getRotulo);
        private final Map<String, Desfecho> desfechos = porRotulo(Desfecho.values(), Desfecho::getRotulo);
        private final Map<String, GrauDano> grausDano = porRotulo(GrauDano.values(), GrauDano::getRotulo);
    }

    private static class LeitorLinha {
        private final Row linha;
        private final int numero;
        private final Map<ColunaTecnovigilancia, Integer> posicoes;
        private final List<PlanilhaInvalidaException.Erro> erros;

        LeitorLinha(Row linha, Map<ColunaTecnovigilancia, Integer> posicoes, List<PlanilhaInvalidaException.Erro> erros) {
            this.linha = linha;
            this.numero = linha.getRowNum() + 1;
            this.posicoes = posicoes;
            this.erros = erros;
        }

        LinhaLida ler(Referencias referencias) {
            Desvio desvio = new Desvio();
            desvio.setVigihosp(texto(VIGIHOSP));
            desvio.setData(data(DATA));
            String codigoMaterial = texto(CODIGO_MATERIAL);
            String nomeMaterial = texto(MATERIAL);
            String fabricante = texto(FABRICANTE);
            String marca = texto(MARCA);
            desvio.setRegistroAnvisa(formato(REGISTRO_ANVISA, REGISTRO_ANVISA_VALIDO,
                    "Registro ANVISA deve ter 11 dígitos, só números"));
            desvio.setLote(texto(LOTE));
            desvio.setValidade(data(VALIDADE));
            desvio.setFormaEntrada(texto(FORMA_ENTRADA));
            desvio.setAmostra(opcao(AMOSTRA, referencias.amostras));
            desvio.setServico(texto(SERVICO));
            desvio.setUnidade(texto(UNIDADE));
            desvio.setMotivo(texto(MOTIVO));
            desvio.setProvidencias(texto(PROVIDENCIAS));
            desvio.setTecnicoUsep(texto(TECNICO_USEP));
            desvio.setDesfecho(opcao(DESFECHO, referencias.desfechos));
            desvio.setGrauDano(opcao(GRAU_DANO, referencias.grausDano));
            desvio.setNotificacaoAnvisa(texto(NOTIFICACAO_ANVISA));
            desvio.setProcessoSei(formato(PROCESSO_SEI, PROCESSO_SEI_VALIDO,
                    "Processo SEI deve estar no formato 00000.000000/0000-00"));

            if (desvio.getData() != null && desvio.getData().isAfter(LocalDate.now())) {
                erro(DATA, desvio.getData().format(FORMATO_DATA), "A data não pode ser futura");
            }
            validarCelulasForaDoModelo();
            return new LinhaLida(numero, desvio, codigoMaterial, nomeMaterial, fabricante, marca);
        }

        void erro(ColunaTecnovigilancia coluna, String valor, String mensagem) {
            erros.add(new PlanilhaInvalidaException.Erro(numero, coluna.getTitulo(), encurtar(valor), mensagem));
        }

        private void validarCelulasForaDoModelo() {
            for (Cell celula : linha) {
                if (!posicoes.containsValue(celula.getColumnIndex()) && !isCelulaVazia(celula)) {
                    erros.add(new PlanilhaInvalidaException.Erro(numero,
                            CellReference.convertNumToColString(celula.getColumnIndex()),
                            encurtar(new DataFormatter().formatCellValue(celula)),
                            "Dado fora das colunas do modelo"));
                }
            }
        }

        private Cell celula(ColunaTecnovigilancia coluna) {
            Cell celula = linha.getCell(posicoes.get(coluna), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            return isCelulaVazia(celula) ? null : celula;
        }

        private String obrigatorio(ColunaTecnovigilancia coluna) {
            if (coluna.isObrigatoria()) {
                erro(coluna, "", "Campo obrigatório");
            }
            return null;
        }

        private String texto(ColunaTecnovigilancia coluna) {
            Cell celula = celula(coluna);
            if (celula == null) {
                return obrigatorio(coluna);
            }
            String valor;
            switch (celula.getCellType()) {
                case STRING -> valor = coluna.getTipo() == ColunaTecnovigilancia.Tipo.TEXTO_LONGO
                        ? normalizarTextoLongo(celula.getStringCellValue())
                        : normalizar(celula.getStringCellValue());
                case NUMERIC -> {
                    if (DateUtil.isCellDateFormatted(celula)) {
                        erro(coluna, new DataFormatter().formatCellValue(celula),
                                "Esperava texto, mas a célula está formatada como data");
                        return null;
                    }
                    valor = BigDecimal.valueOf(celula.getNumericCellValue()).stripTrailingZeros().toPlainString();
                }
                case FORMULA -> {
                    erro(coluna, "=" + celula.getCellFormula(), "Fórmulas não são aceitas; cole como valores");
                    return null;
                }
                default -> {
                    erro(coluna, new DataFormatter().formatCellValue(celula), "Tipo de célula não aceito");
                    return null;
                }
            }
            if (coluna.getTipo() != ColunaTecnovigilancia.Tipo.TEXTO_LONGO && valor.length() > TAMANHO_MAXIMO_TEXTO) {
                erro(coluna, valor, "Mais de " + TAMANHO_MAXIMO_TEXTO + " caracteres");
                return null;
            }
            return valor;
        }

        private LocalDate data(ColunaTecnovigilancia coluna) {
            Cell celula = celula(coluna);
            if (celula == null) {
                obrigatorio(coluna);
                return null;
            }
            switch (celula.getCellType()) {
                case NUMERIC -> {
                    if (DateUtil.isCellDateFormatted(celula)) {
                        return celula.getLocalDateTimeCellValue().toLocalDate();
                    }
                    erro(coluna, new DataFormatter().formatCellValue(celula),
                            "Número não formatado como data; use dd/mm/aaaa");
                }
                case STRING -> {
                    String valor = normalizar(celula.getStringCellValue());
                    try {
                        return LocalDate.parse(valor, FORMATO_DATA);
                    } catch (DateTimeParseException e) {
                        erro(coluna, valor, "Data inválida; use dd/mm/aaaa");
                    }
                }
                case FORMULA -> erro(coluna, "=" + celula.getCellFormula(), "Fórmulas não são aceitas; cole como valores");
                default -> erro(coluna, new DataFormatter().formatCellValue(celula), "Tipo de célula não aceito");
            }
            return null;
        }

        private String formato(ColunaTecnovigilancia coluna, Pattern padrao, String mensagem) {
            String valor = texto(coluna);
            if (valor != null && !padrao.matcher(valor).matches()) {
                erro(coluna, valor, mensagem);
                return null;
            }
            return valor;
        }

        private <E extends Enum<E>> E opcao(ColunaTecnovigilancia coluna, Map<String, E> opcoes) {
            String valor = texto(coluna);
            if (valor == null) {
                return null;
            }
            E opcao = opcoes.get(valor);
            if (opcao == null) {
                erro(coluna, valor, "Opção fora da lista do modelo");
            }
            return opcao;
        }

    }

    private Map<ColunaTecnovigilancia, String> escreverListas(XSSFWorkbook planilha, XSSFSheet aba,
            List<Material> materiais, Map<ColunaTecnovigilancia, List<String>> opcoes) {
        XSSFCellStyle estiloCabecalho = estiloCabecalho(planilha, new byte[]{(byte) 0xE7, (byte) 0xE6, (byte) 0xE6});
        XSSFCellStyle estiloTexto = estiloTexto(planilha);
        Map<ColunaTecnovigilancia, String> faixas = new EnumMap<>(ColunaTecnovigilancia.class);
        XSSFRow cabecalho = aba.createRow(0);

        int indice = 0;
        for (Map.Entry<ColunaTecnovigilancia, List<String>> lista : opcoes.entrySet()) {
            escreverColunaDeLista(aba, cabecalho, indice, lista.getKey().getTitulo(), lista.getValue(),
                    estiloCabecalho, estiloTexto);
            faixas.put(lista.getKey(), faixa(indice, lista.getValue().size()));
            indice++;
            if (lista.getKey() == CODIGO_MATERIAL) {
                escreverColunaDeLista(aba, cabecalho, indice, MATERIAL.getTitulo(),
                        materiais.stream().map(Material::getNome).toList(), estiloCabecalho, estiloTexto);
                faixas.put(MATERIAL, faixa(indice, materiais.size()));
                indice++;
            }
        }
        aba.createFreezePane(0, 1);
        return faixas;
    }

    private String faixa(int indice, int total) {
        String letra = CellReference.convertNumToColString(indice);
        return ABA_LISTAS + "!$" + letra + "$2:$" + letra + "$" + Math.max(total + 1, 2);
    }

    private void escreverColunaDeLista(XSSFSheet aba, XSSFRow cabecalho, int indice, String titulo, List<String> valores,
            XSSFCellStyle estiloCabecalho, XSSFCellStyle estiloTexto) {
        Cell celulaCabecalho = cabecalho.createCell(indice);
        celulaCabecalho.setCellValue(titulo);
        celulaCabecalho.setCellStyle(estiloCabecalho);
        for (int i = 0; i < valores.size(); i++) {
            XSSFRow linha = aba.getRow(i + 1) == null ? aba.createRow(i + 1) : aba.getRow(i + 1);
            Cell celula = linha.createCell(indice);
            celula.setCellValue(valores.get(i));
            celula.setCellStyle(estiloTexto);
        }
        int maior = valores.stream().mapToInt(String::length).max().orElse(0);
        aba.setColumnWidth(indice, Math.min(Math.max(maior, titulo.length()) + 2, 60) * 256);
    }

    private void escreverDesvios(XSSFWorkbook planilha, XSSFSheet aba, Map<ColunaTecnovigilancia, String> faixasDasListas) {
        XSSFCellStyle estiloObrigatoria = estiloCabecalho(planilha, new byte[]{(byte) 0xF8, (byte) 0xCB, (byte) 0xAD});
        XSSFCellStyle estiloOpcional = estiloCabecalho(planilha, new byte[]{(byte) 0xDD, (byte) 0xEB, (byte) 0xF7});
        XSSFCellStyle estiloTexto = estiloTexto(planilha);
        XSSFCellStyle estiloTextoLongo = estiloTexto(planilha);
        estiloTextoLongo.setWrapText(true);
        estiloTextoLongo.setVerticalAlignment(VerticalAlignment.TOP);
        XSSFCellStyle estiloData = planilha.createCellStyle();
        estiloData.setDataFormat(planilha.createDataFormat().getFormat("dd/mm/yyyy"));

        XSSFDataValidationHelper ajudante = new XSSFDataValidationHelper(aba);
        XSSFRow cabecalho = aba.createRow(0);
        for (ColunaTecnovigilancia coluna : ColunaTecnovigilancia.values()) {
            int indice = coluna.ordinal();
            Cell celula = cabecalho.createCell(indice);
            celula.setCellValue(coluna.getTitulo());
            celula.setCellStyle(coluna.isObrigatoria() ? estiloObrigatoria : estiloOpcional);
            aba.setColumnWidth(indice, coluna.getLargura() * 256);
            aba.setDefaultColumnStyle(indice, switch (coluna.getTipo()) {
                case DATA -> estiloData;
                case TEXTO_LONGO -> estiloTextoLongo;
                default -> estiloTexto;
            });

            DataValidationConstraint restricao = restricao(ajudante, coluna, faixasDasListas.get(coluna));
            if (restricao == null) {
                continue;
            }
            DataValidation validacao = ajudante.createValidation(restricao,
                    new CellRangeAddressList(1, LINHAS_COM_VALIDACAO, indice, indice));
            validacao.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validacao.setShowErrorBox(coluna != CODIGO_MATERIAL && coluna != MATERIAL && coluna != FABRICANTE);
            validacao.createErrorBox(coluna.getTitulo(), coluna.getRegra());
            validacao.setShowPromptBox(true);
            validacao.createPromptBox(coluna.getTitulo(), coluna.getRegra());
            validacao.setEmptyCellAllowed(true);
            if (coluna.getTipo() == ColunaTecnovigilancia.Tipo.LISTA) {
                validacao.setSuppressDropDownArrow(true);
            }
            aba.addValidationData(validacao);
        }
        aba.createFreezePane(1, 1);
    }

    private DataValidationConstraint restricao(XSSFDataValidationHelper ajudante, ColunaTecnovigilancia coluna,
            String faixaDaLista) {
        return switch (coluna) {
            case DATA -> ajudante.createDateConstraint(OperatorType.LESS_OR_EQUAL, "TODAY()", null, null);
            case VALIDADE -> ajudante.createDateConstraint(OperatorType.GREATER_OR_EQUAL, "DATE(1900,1,1)", null, null);
            case REGISTRO_ANVISA -> ajudante.createTextLengthConstraint(OperatorType.EQUAL, "11", null);
            case PROCESSO_SEI -> ajudante.createTextLengthConstraint(OperatorType.EQUAL, "20", null);
            default -> switch (coluna.getTipo()) {
                case LISTA -> ajudante.createFormulaListConstraint(faixaDaLista);
                case TEXTO -> ajudante.createTextLengthConstraint(OperatorType.LESS_OR_EQUAL,
                        String.valueOf(TAMANHO_MAXIMO_TEXTO), null);
                default -> null;
            };
        };
    }

    private void escreverInstrucoes(XSSFWorkbook planilha, XSSFSheet aba) {
        XSSFCellStyle estiloTitulo = planilha.createCellStyle();
        XSSFFont fonteTitulo = planilha.createFont();
        fonteTitulo.setBold(true);
        fonteTitulo.setFontHeightInPoints((short) 14);
        estiloTitulo.setFont(fonteTitulo);
        XSSFCellStyle estiloCabecalho = estiloCabecalho(planilha, new byte[]{(byte) 0xE7, (byte) 0xE6, (byte) 0xE6});
        XSSFCellStyle estiloQuebra = planilha.createCellStyle();
        estiloQuebra.setWrapText(true);
        estiloQuebra.setVerticalAlignment(VerticalAlignment.TOP);

        List<String> regras = List.of(
                "Preencha somente a aba " + ABA_DESVIOS + ", uma linha por notificação, a partir da linha 2.",
                "Não renomeie, apague nem acrescente colunas; a ordem das colunas pode mudar. Abas extras são ignoradas.",
                "Colunas com cabeçalho laranja são obrigatórias; as azuis podem ficar em branco.",
                "Código e nome do material são obrigatórios. Código que ainda não existe no PID é cadastrado com o nome da planilha; código já cadastrado com outro nome usa o material do cadastro. As duas colunas têm a lista dos materiais já cadastrados, mas aceitam outros valores.",
                "Fabricante tem a lista dos fabricantes já cadastrados, mas aceita outros nomes; um fabricante novo é cadastrado na importação.",
                "Envio de amostra, Desfecho e Grau de dano aceitam só os valores da aba " + ABA_LISTAS + ", exatamente como estão. As demais colunas são texto livre ou data.",
                "Datas no formato dd/mm/aaaa. Não use fórmulas: ao colar de outra planilha, use \"Colar especial > Somente valores\".",
                "A importação é tudo ou nada: se alguma linha nova tiver erro, nenhuma linha é gravada e o PID mostra a lista de erros por linha e coluna.",
                "O mesmo arquivo não pode ser importado duas vezes. Linhas com Nº VigiHosp que já existe no PID são ignoradas, e o PID avisa quais foram.");

        int indice = 0;
        Cell titulo = aba.createRow(indice++).createCell(0);
        titulo.setCellValue("Modelo de importação de tecnovigilância");
        titulo.setCellStyle(estiloTitulo);
        indice++;
        for (String regra : regras) {
            Cell celula = aba.createRow(indice++).createCell(0);
            celula.setCellValue("• " + regra);
        }
        indice++;

        XSSFRow cabecalho = aba.createRow(indice++);
        String[] titulos = {"Coluna", "Campo", "Obrigatória", "Regra"};
        for (int i = 0; i < titulos.length; i++) {
            Cell celula = cabecalho.createCell(i);
            celula.setCellValue(titulos[i]);
            celula.setCellStyle(estiloCabecalho);
        }
        for (ColunaTecnovigilancia coluna : ColunaTecnovigilancia.values()) {
            XSSFRow linha = aba.createRow(indice++);
            linha.createCell(0).setCellValue(CellReference.convertNumToColString(coluna.ordinal()));
            linha.createCell(1).setCellValue(coluna.getTitulo());
            linha.createCell(2).setCellValue(coluna.isObrigatoria() ? "Sim" : "Não");
            Cell regra = linha.createCell(3);
            regra.setCellValue(coluna.getRegra());
            regra.setCellStyle(estiloQuebra);
        }
        aba.setColumnWidth(0, 10 * 256);
        aba.setColumnWidth(1, 22 * 256);
        aba.setColumnWidth(2, 13 * 256);
        aba.setColumnWidth(3, 90 * 256);
    }

    private XSSFCellStyle estiloCabecalho(XSSFWorkbook planilha, byte[] cor) {
        XSSFCellStyle estilo = planilha.createCellStyle();
        XSSFFont fonte = planilha.createFont();
        fonte.setBold(true);
        estilo.setFont(fonte);
        estilo.setFillForegroundColor(new XSSFColor(cor, null));
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        return estilo;
    }

    private XSSFCellStyle estiloTexto(XSSFWorkbook planilha) {
        XSSFCellStyle estilo = planilha.createCellStyle();
        estilo.setDataFormat(planilha.createDataFormat().getFormat("@"));
        return estilo;
    }

    private String calcularSha256(byte[] conteudo) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(conteudo));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
