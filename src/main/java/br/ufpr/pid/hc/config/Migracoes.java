package br.ufpr.pid.hc.config;

import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public final class Migracoes {

    private Migracoes() {}

    public static Flyway flyway(DataSource dataSource) {
        return Flyway.configure(Migracoes.class.getClassLoader())
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load();
    }
}
