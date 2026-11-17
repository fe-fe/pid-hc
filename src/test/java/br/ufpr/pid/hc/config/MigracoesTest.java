package br.ufpr.pid.hc.config;

import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
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

    private void validarEntidades() throws Exception {
        BancoDeTeste.hibernate(POSTGRES, Map.of("hibernate.hbm2ddl.auto", "validate")).close();
    }

    private void executar(String sql) throws SQLException {
        try (Connection conexao = dataSource.getConnection(); Statement comando = conexao.createStatement()) {
            comando.execute(sql);
        }
    }
}
