package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Auditavel;
import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

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
    public T salvar(T entidade, Usuario usuario) {

        Object id = entityManager.getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .getIdentifier(entidade);

        entidade.setAtualizadoPor(usuario);
        if (id == null) {
            entidade.setCriadoPor(usuario);
            entityManager.persist(entidade);
            return entidade;
        } else {
            entidade.setAtualizadoPor(usuario);
            return entityManager.merge(entidade);
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

    public List<T> buscar(int pagina, int tamanhoPagina, CampoOrdenacao campoOrdenacao) {
        return entityManager.createQuery(
                        "SELECT e FROM " + tipoEntidade.getSimpleName() + " e ORDER BY e." + campoOrdenacao.getCampoBanco(),
                        tipoEntidade)
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    public long contarTotal() {
        return entityManager.createQuery(
                "SELECT count(e) FROM " + tipoEntidade.getSimpleName() + " e",
                Long.class
        ).getSingleResult();
    }
}