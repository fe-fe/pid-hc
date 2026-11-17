package br.ufpr.pid.hc.config;

import jakarta.persistence.Entity;
import org.flywaydb.core.api.output.MigrateResult;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Testcontainers
class MigracoesTest {

    private static final String PACOTE_ENTIDADES = "br.ufpr.pid.hc.entity";

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16");

    private PGSimpleDataSource dataSource;

    @BeforeEach
    void limparBanco() throws SQLException {
        dataSource = new PGSimpleDataSource();
        dataSource.setUrl(POSTGRES.getJdbcUrl());
        dataSource.setUser(POSTGRES.getUsername());
        dataSource.setPassword(POSTGRES.getPassword());
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
    void bancoCriadoPeloHibernateEntraComoBaseline() throws Exception {
        criarEsquemaPeloHibernate();

        MigrateResult resultado = Migracoes.flyway(dataSource).migrate();

        assertFalse(resultado.migrations.stream().anyMatch(m -> m.version.equals("1")),
                "o esquema inicial não deve rodar sobre um banco que já existe");
        validarEntidades();
    }

    private void validarEntidades() throws Exception {
        sessionFactory("validate").close();
    }

    private void criarEsquemaPeloHibernate() throws Exception {
        sessionFactory("create").close();
    }

    private SessionFactory sessionFactory(String acao) throws Exception {
        StandardServiceRegistry registro = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", POSTGRES.getJdbcUrl())
                .applySetting("hibernate.connection.username", POSTGRES.getUsername())
                .applySetting("hibernate.connection.password", POSTGRES.getPassword())
                .applySetting("hibernate.hbm2ddl.auto", acao)
                .build();
        MetadataSources fontes = new MetadataSources(registro);
        entidades().forEach(fontes::addAnnotatedClass);
        return fontes.buildMetadata().buildSessionFactory();
    }

    private static List<Class<?>> entidades() throws URISyntaxException {
        File pasta = new File(Objects.requireNonNull(MigracoesTest.class.getClassLoader()
                .getResource(PACOTE_ENTIDADES.replace('.', '/'))).toURI());
        return Arrays.stream(Objects.requireNonNull(pasta.list((dir, nome) -> nome.endsWith(".class") && !nome.contains("$"))))
                .<Class<?>>map(nome -> carregar(PACOTE_ENTIDADES + "." + nome.replace(".class", "")))
                .filter(classe -> classe.isAnnotationPresent(Entity.class))
                .toList();
    }

    private static Class<?> carregar(String nome) {
        try {
            return Class.forName(nome);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private void executar(String sql) throws SQLException {
        try (Connection conexao = dataSource.getConnection(); Statement comando = conexao.createStatement()) {
            comando.execute(sql);
        }
    }
}
