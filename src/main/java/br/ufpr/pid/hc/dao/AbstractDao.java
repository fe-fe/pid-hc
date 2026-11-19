package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Auditavel;
import br.ufpr.pid.hc.entity.Revisao;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    public T salvar(T entidade) {
        Object id = entityManager.getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .getIdentifier(entidade);
        if (id == null) {
            entityManager.persist(entidade);
            return entidade;
        }
        return entityManager.merge(entidade);
    }

    public Optional<LocalDateTime> dataCriacao(ID id) {
        return AuditReaderFactory.get(entityManager).createQuery()
                .forRevisionsOfEntity(tipoEntidade, false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().asc())
                .setMaxResults(1)
                .getResultList().stream()
                .map(linha -> ((Revisao) ((Object[]) linha)[1]).getData())
                .findFirst();
    }

    public List<T> buscar(int pagina, int tamanhoPagina, CampoOrdenacao campoOrdenacao) {
        return buscar(pagina, tamanhoPagina, campoOrdenacao, true);
    }

    public List<T> buscar(int pagina, int tamanhoPagina, CampoOrdenacao campoOrdenacao, boolean incluirInativos) {
        return entityManager.createQuery(
                        "SELECT e FROM " + tipoEntidade.getSimpleName() + " e " +
                                filtroAtivo("e", incluirInativos) +
                                "ORDER BY e." + campoOrdenacao.getCampoBanco(),
                        tipoEntidade)
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    public long contarTotal() {
        return contarTotal(true);
    }

    public long contarTotal(boolean incluirInativos) {
        return entityManager.createQuery(
                "SELECT count(e) FROM " + tipoEntidade.getSimpleName() + " e " + filtroAtivo("e", incluirInativos),
                Long.class
        ).getSingleResult();
    }

    protected String filtroAtivo(String alias, boolean incluirInativos) {
        if (incluirInativos) {
            return "";
        }

        return "WHERE (" + alias + ".ativo IS NULL OR " + alias + ".ativo = true) ";
    }
}