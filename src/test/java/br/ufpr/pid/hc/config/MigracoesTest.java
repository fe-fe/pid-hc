package br.ufpr.pid.hc.config;

import br.ufpr.pid.hc.entity.Marca;
import org.flywaydb.core.api.output.MigrateResult;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Testcontainers
class MigracoesTest {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16");

    private PGSimpleDataSource dataSource;

    @BeforeEach
    void limparBanco() throws SQLException {
        dataSource = BancoDeTeste.dataSource(POSTGRES);
        executar("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
    }

    @Test
    void migrationsBatemComAsEntidades() throws Exception {
        Migracoes.flyway(dataSource).migrate();

        validarEntidades();
    }

    @Test
    void segundaExecucaoNaoAplicaNada() {
        Migracoes.flyway(dataSource).migrate();

        MigrateResult resultado = Migracoes.flyway(dataSource).migrate();

        assertEquals(0, resultado.migrationsExecuted);
    }

    @Test
    void bancoAnteriorAoFlywayEntraComoBaseline() throws Exception {
        executar(new String(getClass().getResourceAsStream("/db/migration/V1__esquema_inicial.sql").readAllBytes()));

        MigrateResult resultado = Migracoes.flyway(dataSource).migrate();

        assertFalse(resultado.migrations.stream().anyMatch(m -> m.version.equals("1")),
                "o esquema inicial não deve rodar sobre um banco que já existe");
        validarEntidades();
    }

    @Test
    void historicoDosRegistrosAntigos() throws Exception {
        executar(new String(getClass().getResourceAsStream("/db/migration/V1__esquema_inicial.sql").readAllBytes()));
        executar("""
                INSERT INTO usuario (id, nome, email, senha, perfil, ativo)
                VALUES ('00000000-0000-0000-0000-000000000001', 'Ana', 'ana@hc.br', 'x', 'ADMINISTRADOR', true);
                INSERT INTO marca (id, nome, ativo, criado_em, atualizado_em, criado_por, atualizado_por)
                VALUES ('00000000-0000-0000-0000-000000000002', 'ALFA', true,
                        '2026-10-01 09:00', '2026-10-05 14:00', '00000000-0000-0000-0000-000000000001', NULL);
                """);

        Migracoes.flyway(dataSource).migrate();

        assertEquals("2026-10-01 09:00:00|ana@hc.br|0, 2026-10-05 14:00:00|null|1", consultar("""
                SELECT string_agg(to_char(r.data, 'YYYY-MM-DD HH24:MI:SS') || '|' || COALESCE(r.usuario, 'null')
                                  || '|' || a.revtype, ', ' ORDER BY r.id)
                FROM marca_aud a JOIN revisao r ON r.id = a.rev"""));
        assertEquals("0", consultar("""
                SELECT count(*) FROM information_schema.columns
                WHERE table_schema = 'public' AND column_name IN ('criado_em', 'atualizado_em', 'criado_por', 'atualizado_por')"""));

        try (SessionFactory hibernate = BancoDeTeste.hibernate(POSTGRES, Map.of("hibernate.hbm2ddl.auto", "validate"))) {
            hibernate.inTransaction(sessao -> {
                Marca marca = new Marca();
                marca.setNome("BETA");
                sessao.persist(marca);
            });
        }
        assertEquals("3", consultar("SELECT count(*) FROM marca_aud"));
    }

    @Test
    void cpfVazio() throws Exception {
        executar(new String(getClass().getResourceAsStream("/db/migration/V1__esquema_inicial.sql").readAllBytes()));
        executar("""
                INSERT INTO usuario (id, nome, email, senha, perfil, ativo, cpf)
                VALUES ('00000000-0000-0000-0000-000000000001', 'Ana', 'ana@hc.br', 'x', 'ADMINISTRADOR', true, '');
                """);

        Migracoes.flyway(dataSource).migrate();

        assertEquals("1", consultar("SELECT count(*) FROM usuario WHERE cpf IS NULL"));
    }

    private String consultar(String sql) throws SQLException {
        try (Connection conexao = dataSource.getConnection(); Statement comando = conexao.createStatement();
             ResultSet resultado = comando.executeQuery(sql)) {
            resultado.next();
            return resultado.getString(1);
        }
    }

    private void validarEntidades() throws Exception {
        BancoDeTeste.hibernate(POSTGRES, Map.of("hibernate.hbm2ddl.auto", "validate")).close();
    }

    private void executar(String sql) throws SQLException {
        try (Connection conexao = dataSource.getConnection(); Statement comando = conexao.createStatement()) {
            comando.execute(sql);
        }
    }
}
