package br.ufpr.pid.hc.config;

import jakarta.persistence.Entity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class BancoDeTeste {

    private static final String PACOTE_ENTIDADES = "br.ufpr.pid.hc.entity";

    private BancoDeTeste() {}

    static PGSimpleDataSource dataSource(PostgreSQLContainer postgres) {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUser(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());
        return dataSource;
    }

    static SessionFactory hibernate(PostgreSQLContainer postgres, Map<String, String> configuracoes) throws URISyntaxException {
        StandardServiceRegistryBuilder registro = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", postgres.getJdbcUrl())
                .applySetting("hibernate.connection.username", postgres.getUsername())
                .applySetting("hibernate.connection.password", postgres.getPassword());
        configuracoes.forEach(registro::applySetting);
        MetadataSources fontes = new MetadataSources(registro.build());
        entidades().forEach(fontes::addAnnotatedClass);
        return fontes.buildMetadata().buildSessionFactory();
    }

    private static List<Class<?>> entidades() throws URISyntaxException {
        File pasta = new File(Objects.requireNonNull(BancoDeTeste.class.getClassLoader()
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
}
