package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.ImportacaoAnvisa;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Optional;

@ApplicationScoped
public class ImportacaoAnvisaDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<ImportacaoAnvisa> buscarUltima() {
        return entityManager.createQuery(
                        "SELECT i FROM ImportacaoAnvisa i ORDER BY i.importadoEm DESC", ImportacaoAnvisa.class)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public void salvar(ImportacaoAnvisa importacao) {
        entityManager.persist(importacao);
    }
}
