package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Auditavel;
import br.ufpr.pid.hc.entity.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

public abstract class AbstractDao<T extends Auditavel, ID> {

    @PersistenceContext
    protected EntityManager entityManager;

    private final Class<T> tipoEntidade;

    protected AbstractDao(Class<T> tipoEntidade) {
        this.tipoEntidade = tipoEntidade;
    }

    public T buscarPorId(ID id) {
        return entityManager.find(tipoEntidade, id);
    }

    @Transactional
    public void salvar(T entidade, Usuario usuario) {

        Object id = entityManager.getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .getIdentifier(entidade);

        entidade.setAtualizadoPor(usuario);
        if (id == null) {
            entidade.setCriadoPor(usuario);
            entityManager.persist(entidade);
        } else {
            entidade.setAtualizadoPor(usuario);
            entityManager.merge(entidade);
        }
    }

    public void desativar(T entidade, Usuario usuario) {
        entidade.setAtivo(false);
        salvar(entidade, usuario);
    }

    public void ativar(T entidade, Usuario usuario) {
        entidade.setAtivo(true);
        salvar(entidade, usuario);
    }
}
