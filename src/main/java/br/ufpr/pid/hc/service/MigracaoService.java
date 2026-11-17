package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.config.Migracoes;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.output.MigrateResult;

import javax.sql.DataSource;

@Singleton
@Startup
@PermitAll
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Slf4j
public class MigracaoService {

    @Resource(lookup = "java:/pidhcDS")
    private DataSource dataSource;

    @PostConstruct
    void migrar() {
        MigrateResult resultado = Migracoes.flyway(dataSource).migrate();
        log.info("Banco na versão {} ({} migrations aplicadas)",
                resultado.targetSchemaVersion == null ? resultado.initialSchemaVersion : resultado.targetSchemaVersion,
                resultado.migrationsExecuted);
    }
}
