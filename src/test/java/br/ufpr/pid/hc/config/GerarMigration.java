package br.ufpr.pid.hc.config;

import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class GerarMigration {

    private static final Path PASTA = Path.of("src/main/resources/db/migration");
    private static final Pattern VERSAO = Pattern.compile("V(\\d+)__.*\\.sql");

    @Test
    void gerar() throws Exception {
        String descricao = System.getProperty("migration", "");
        if (!descricao.matches("[a-z0-9_]+")) {
            throw new IllegalArgumentException("Informe a descrição em minúsculas: -Dmigration=marca_site");
        }

        List<String> comandos;
        try (PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")) {
            postgres.start();
            Migracoes.flyway(BancoDeTeste.dataSource(postgres)).migrate();
            Path script = Files.createTempFile("migration", ".sql");
            BancoDeTeste.hibernate(postgres, Map.of(
                    "jakarta.persistence.schema-generation.scripts.action", "update",
                    "jakarta.persistence.schema-generation.scripts.create-target", script.toString(),
                    "hibernate.hbm2ddl.delimiter", ";",
                    "hibernate.format_sql", "true")).close();
            comandos = Files.readAllLines(script);
            Files.delete(script);
        }

        if (comandos.stream().allMatch(String::isBlank)) {
            System.out.println("\nNenhuma diferença entre as entidades e as migrations; nada foi gerado.\n");
            return;
        }
        Path arquivo = PASTA.resolve("V" + proximaVersao() + "__" + descricao + ".sql");
        Files.write(arquivo, comandos);
        System.out.println("\nGerado " + arquivo + ". Revise antes de commitar: renomear coluna vira apagar e criar,"
                + " e coluna obrigatória em tabela com dados precisa ser preenchida antes do NOT NULL.\n");
    }

    private static int proximaVersao() throws Exception {
        try (Stream<Path> arquivos = Files.list(PASTA)) {
            return arquivos.map(arquivo -> VERSAO.matcher(arquivo.getFileName().toString()))
                    .filter(Matcher::matches)
                    .mapToInt(versao -> Integer.parseInt(versao.group(1)))
                    .max().orElse(0) + 1;
        }
    }
}
