package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.config.BancoDeTeste;
import br.ufpr.pid.hc.config.Migracoes;
import br.ufpr.pid.hc.dto.Alteracao;
import br.ufpr.pid.hc.dto.EventoAuditoria;
import br.ufpr.pid.hc.dto.FiltroAuditoria;
import br.ufpr.pid.hc.entity.Acesso;
import br.ufpr.pid.hc.entity.Avaliacao;
import br.ufpr.pid.hc.entity.Desvio;
import br.ufpr.pid.hc.entity.Fabricante;
import br.ufpr.pid.hc.entity.ImportacaoAnvisa;
import br.ufpr.pid.hc.entity.ImportacaoTecnovigilancia;
import br.ufpr.pid.hc.entity.Marca;
import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.entity.Parametro;
import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.Perfil;
import br.ufpr.pid.hc.enumeration.ResultadoTecnico;
import br.ufpr.pid.hc.enumeration.TipoAcesso;
import br.ufpr.pid.hc.enumeration.TipoEvento;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class AuditoriaDaoTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16");

    private static SessionFactory hibernate;
    private static AuditoriaDao dao;
    private static UUID avaliacao;
    private static UUID material;
    private static UUID fabricante;
    private static UUID desvio;
    private static UUID usuario;

    @BeforeAll
    static void registrarEventos() throws Exception {
        Migracoes.flyway(BancoDeTeste.dataSource(POSTGRES)).migrate();
        hibernate = BancoDeTeste.hibernate(POSTGRES, Map.of("hibernate.hbm2ddl.auto", "validate"));

        hibernate.inTransaction(sessao -> {
            Parametro validade = new Parametro();
            validade.setChave(Parametro.VALIDADE);
            validade.setValor("36");
            sessao.persist(validade);
        });

        hibernate.inTransaction(sessao -> {
            Material material = new Material();
            material.setCodigo("777");
            material.setNome("Gaze");
            Fabricante fabricante = new Fabricante();
            fabricante.setNome("FAB");
            Avaliacao nova = new Avaliacao();
            nova.setMaterial(material);
            nova.setFabricante(fabricante);
            nova.setData(LocalDate.of(2026, 11, 10));
            nova.setResultado(ResultadoTecnico.APROVADO);
            sessao.persist(material);
            sessao.persist(fabricante);
            sessao.persist(nova);
            avaliacao = nova.getId();
            AuditoriaDaoTest.material = material.getId();
            AuditoriaDaoTest.fabricante = fabricante.getId();
        });
        hibernate.inTransaction(sessao -> {
            Avaliacao editada = sessao.find(Avaliacao.class, avaliacao);
            editada.setResultado(ResultadoTecnico.REPROVADO);
            editada.setParecer("Embalagem violada");
        });
        hibernate.inTransaction(sessao -> {
            ImportacaoTecnovigilancia importacao = new ImportacaoTecnovigilancia();
            importacao.setNomeArquivo("desvios.xlsx");
            importacao.setSha256("abc");
            importacao.setTotalRegistros(2);
            sessao.persist(importacao);
            Marca marca = new Marca();
            marca.setNome("MARCA DA PLANILHA");
            sessao.persist(marca);
            Desvio novo = new Desvio();
            novo.setMaterial(sessao.find(Material.class, material));
            novo.setFabricante(sessao.find(Fabricante.class, fabricante));
            novo.setVigihosp("123");
            sessao.persist(novo);
            desvio = novo.getId();
        });
        hibernate.inTransaction(sessao -> {
            ImportacaoAnvisa anvisa = new ImportacaoAnvisa();
            anvisa.setSha256("def");
            anvisa.setDtAtualizacaoDado(LocalDateTime.of(2026, 11, 3, 8, 0));
            anvisa.setImportadoEm(LocalDateTime.now());
            sessao.persist(anvisa);
        });
        hibernate.inTransaction(sessao -> {
            Usuario ana = new Usuario();
            ana.setNome("Ana");
            ana.setEmail("ana@hc.br");
            ana.setSenha("senha");
            ana.setPerfil(Perfil.ANALISTA);
            sessao.persist(ana);
            usuario = ana.getId();
        });
        hibernate.inTransaction(sessao -> sessao.find(Usuario.class, usuario).setAtivo(false));
        hibernate.inTransaction(sessao -> sessao.find(Desvio.class, desvio).setLote("L1"));
        hibernate.inTransaction(sessao -> {
            Acesso login = new Acesso();
            login.setData(LocalDateTime.now().plusSeconds(1));
            login.setUsuario("ana@hc.br");
            login.setTipo(TipoAcesso.LOGIN);
            sessao.persist(login);
        });
        hibernate.inTransaction(sessao -> sessao.createNativeMutationQuery(
                "UPDATE revisao SET usuario = 'ana@hc.br' WHERE id = (SELECT min(rev) FROM importacao_tecnovigilancia_aud)")
                .executeUpdate());

        dao = new AuditoriaDao();
        Field campo = AuditoriaDao.class.getDeclaredField("entityManager");
        campo.setAccessible(true);
        campo.set(dao, hibernate.createEntityManager());
    }

    @AfterAll
    static void fechar() {
        hibernate.close();
    }

    @Test
    void todos() {
        List<EventoAuditoria> eventos = dao.buscar(FiltroAuditoria.VAZIO, 0, 15);

        assertEquals(List.of(TipoEvento.LOGIN, TipoEvento.DESVIO_EDITADO, TipoEvento.USUARIO_DESATIVADO,
                        TipoEvento.USUARIO_CRIADO, TipoEvento.ANVISA, TipoEvento.PLANILHA, TipoEvento.AVALIACAO_EDITADA),
                eventos.stream().limit(7).map(EventoAuditoria::getTipo).toList());
        assertEquals(11, dao.contar(FiltroAuditoria.VAZIO));
        assertEquals("Ana", eventos.get(0).getAutor());
        assertEquals("VigiHosp 123 — material 777", eventos.get(1).getDescricao());
        assertEquals("Ana — ana@hc.br", eventos.get(2).getDescricao());
        assertEquals("Base de produtos de 03/11/2026", eventos.get(4).getDescricao());
        assertEquals("Sistema", eventos.get(4).getAutor());
        assertEquals("desvios.xlsx · 2 desvios", eventos.get(5).getDescricao());
        assertEquals("Ana", eventos.get(5).getAutor());
    }

    @Test
    void cadastros() {
        List<String> descricoes = dao.buscar(new FiltroAuditoria(TipoEvento.CADASTRO_CRIADO, null, null, null), 0, 15)
                .stream().map(EventoAuditoria::getDescricao).sorted().toList();

        assertEquals(List.of("Fabricante: FAB", "Material: 777 — Gaze"), descricoes);
    }

    @Test
    void configuracao() {
        List<EventoAuditoria> eventos = dao.buscar(new FiltroAuditoria(TipoEvento.CONFIGURACAO, null, null, null), 0, 15);

        assertEquals(1, eventos.size());
        assertEquals("Prazo de validade das avaliações: 36 meses", eventos.get(0).getDescricao());
    }

    @Test
    void filtros() {
        assertEquals(1, dao.contar(new FiltroAuditoria(TipoEvento.AVALIACAO_EDITADA, null, null, null)));
        assertEquals(2, dao.contar(new FiltroAuditoria(null, "ana@hc.br", null, null)));
        assertEquals(11, dao.contar(new FiltroAuditoria(null, null, LocalDate.now(), LocalDate.now())));
        assertEquals(0, dao.contar(new FiltroAuditoria(null, null, null, LocalDate.now().minusDays(1))));
    }

    @Test
    void alteracoes() {
        EventoAuditoria edicao = dao.buscar(new FiltroAuditoria(TipoEvento.AVALIACAO_EDITADA, null, null, null), 0, 15).get(0);

        assertEquals(List.of(
                        new Alteracao("Resultado", "Aprovado", "Reprovado"),
                        new Alteracao("Parecer", "—", "Embalagem violada")),
                dao.alteracoesAvaliacao(edicao.getRegistro(), edicao.getRevisao()));
    }

    @Test
    void criacaoNaoTemAlteracoes() {
        EventoAuditoria criacao = dao.buscar(new FiltroAuditoria(TipoEvento.AVALIACAO_CRIADA, null, null, null), 0, 15).get(0);

        assertEquals(List.of(), dao.alteracoesAvaliacao(criacao.getRegistro(), criacao.getRevisao()));
    }
}
