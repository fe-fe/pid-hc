package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.ImportacaoTecnovigilancia;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ImportacaoTecnovigilanciaDao extends AbstractDao<ImportacaoTecnovigilancia, UUID> {
    public ImportacaoTecnovigilanciaDao() { super(ImportacaoTecnovigilancia.class); }

    public Optional<ImportacaoTecnovigilancia> buscarPorSha256(String sha256) {
        return entityManager.createQuery(
                        "SELECT i FROM ImportacaoTecnovigilancia i WHERE i.sha256 = :sha256",
                        ImportacaoTecnovigilancia.class)
                .setParameter("sha256", sha256)
                .getResultStream()
                .findFirst();
    }
}
