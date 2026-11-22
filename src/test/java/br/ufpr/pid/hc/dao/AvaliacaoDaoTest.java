package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.config.BancoDeTeste;
import br.ufpr.pid.hc.config.Migracoes;
import br.ufpr.pid.hc.entity.Avaliacao;
import br.ufpr.pid.hc.entity.Fabricante;
import br.ufpr.pid.hc.entity.Marca;
import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.enumeration.AvaliacaoOrdenacao;
import br.ufpr.pid.hc.enumeration.ResultadoTecnico;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class AvaliacaoDaoTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16");

    private static final LocalDate HOJE = LocalDate.now();
    private static final LocalDate LIMITE = HOJE.minusMonths(24);

    private static SessionFactory hibernate;
    private static AvaliacaoDao dao;
    private static UUID sozinha;
    private static UUID substituida;
    private static UUID reteste;
    private static UUID semMarca;
    private static UUID recente;

    @BeforeAll
    static void registrar() throws Exception {
        Migracoes.flyway(BancoDeTeste.dataSource(POSTGRES)).migrate();
        hibernate = BancoDeTeste.hibernate(POSTGRES, Map.of("hibernate.hbm2ddl.auto", "validate"));

        hibernate.inTransaction(sessao -> {
            Material gaze = material(sessao, "100", "Gaze");
            Material luva = material(sessao, "200", "Luva");
            Fabricante fabricante = new Fabricante();
            fabricante.setNome("FAB");
            sessao.persist(fabricante);
            Marca marca = new Marca();
            marca.setNome("MARCA");
            sessao.persist(marca);

            sozinha = avaliacao(sessao, gaze, fabricante, marca, HOJE.minusMonths(30));
            substituida = avaliacao(sessao, luva, fabricante, marca, HOJE.minusMonths(40));
            reteste = avaliacao(sessao, luva, fabricante, marca, HOJE.minusMonths(30));
            semMarca = avaliacao(sessao, luva, fabricante, null, HOJE.minusMonths(26));
            recente = avaliacao(sessao, gaze, fabricante, null, HOJE.minusMonths(3));
        });

        dao = new AvaliacaoDao();
        Field campo = AbstractDao.class.getDeclaredField("entityManager");
        campo.setAccessible(true);
        campo.set(dao, hibernate.createEntityManager());
    }

    @AfterAll
    static void fechar() {
        hibernate.close();
    }

    @Test
    void vencidas() {
        Set<UUID> vencidas = Set.copyOf(dao.vencidas(List.of(sozinha, substituida, reteste, semMarca, recente), LIMITE));

        assertEquals(Set.of(sozinha, reteste, semMarca), vencidas);
    }

    @Test
    void filtro() {
        Set<UUID> filtradas = dao.buscarComFiltros(0, 15, AvaliacaoOrdenacao.DATA, null, null, null, null, null, null,
                LIMITE).stream().map(Avaliacao::getId).collect(Collectors.toSet());

        assertEquals(Set.of(sozinha, reteste, semMarca), filtradas);
        assertEquals(3, dao.contarComFiltros(null, null, null, null, null, null, LIMITE));
        assertEquals(5, dao.contarComFiltros(null, null, null, null, null, null, null));
    }

    private static Material material(Session sessao, String codigo, String nome) {
        Material material = new Material();
        material.setCodigo(codigo);
        material.setNome(nome);
        sessao.persist(material);
        return material;
    }

    private static UUID avaliacao(Session sessao, Material material, Fabricante fabricante, Marca marca,
            LocalDate data) {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setMaterial(material);
        avaliacao.setFabricante(fabricante);
        avaliacao.setMarca(marca);
        avaliacao.setData(data);
        avaliacao.setResultado(ResultadoTecnico.APROVADO);
        sessao.persist(avaliacao);
        return avaliacao.getId();
    }
}
