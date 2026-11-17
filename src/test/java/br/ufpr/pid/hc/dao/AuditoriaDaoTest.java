package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.config.BancoDeTeste;
import br.ufpr.pid.hc.config.Migracoes;
import br.ufpr.pid.hc.dto.Alteracao;
import br.ufpr.pid.hc.dto.EventoAuditoria;
import br.ufpr.pid.hc.dto.FiltroAuditoria;
import br.ufpr.pid.hc.entity.Avaliacao;
import br.ufpr.pid.hc.entity.Fabricante;
import br.ufpr.pid.hc.entity.ImportacaoAnvisa;
import br.ufpr.pid.hc.entity.ImportacaoTecnovigilancia;
import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.enumeration.ResultadoTecnico;
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

    @BeforeAll
    static void registrarEventos() throws Exception {
        Migracoes.flyway(BancoDeTeste.dataSource(POSTGRES)).migrate();
        hibernate = BancoDeTeste.hibernate(POSTGRES, Map.of("hibernate.hbm2ddl.auto", "validate"));

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
        });
        hibernate.inTransaction(sessao -> {
            ImportacaoAnvisa anvisa = new ImportacaoAnvisa();
            anvisa.setSha256("def");
            anvisa.setDtAtualizacaoDado(LocalDateTime.of(2026, 11, 3, 8, 0));
            anvisa.setImportadoEm(LocalDateTime.now());
            sessao.persist(anvisa);
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

        assertEquals(List.of(TipoEvento.ANVISA, TipoEvento.PLANILHA, TipoEvento.AVALIACAO_EDITADA, TipoEvento.AVALIACAO_CRIADA),
                eventos.stream().map(EventoAuditoria::getTipo).toList());
        assertEquals(4, dao.contar(FiltroAuditoria.VAZIO));
        assertEquals("Base de produtos de 03/11/2026", eventos.get(0).getDescricao());
        assertEquals("Sistema", eventos.get(0).getAutor());
        assertEquals("desvios.xlsx · 2 desvios", eventos.get(1).getDescricao());
        assertEquals("ana@hc.br", eventos.get(1).getAutor());
        assertEquals("777 — Gaze", eventos.get(3).getDescricao());
    }

    @Test
    void filtros() {
        assertEquals(1, dao.contar(new FiltroAuditoria(TipoEvento.AVALIACAO_EDITADA, null, null, null)));
        assertEquals(1, dao.contar(new FiltroAuditoria(null, "ana@hc.br", null, null)));
        assertEquals(4, dao.contar(new FiltroAuditoria(null, null, LocalDate.now(), LocalDate.now())));
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
