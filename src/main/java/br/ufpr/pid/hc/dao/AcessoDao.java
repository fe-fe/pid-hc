package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Acesso;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class AcessoDao {

    @PersistenceContext
    private EntityManager entityManager;

    public void salvar(Acesso acesso) {
        entityManager.persist(acesso);
    }
}
