package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Categoria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CategoriaDao extends AbstractDao<Categoria, UUID> {

    public CategoriaDao() { super(Categoria.class); }

    public List<Categoria> buscar(int pagina, int tamanhoPagina) {
        return entityManager.createQuery(
                "SELECT c FROM Categoria c ORDER BY c.nome",
                Categoria.class
                )
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    public long contarTotal() {
        return entityManager.createQuery(
                "SELECT count(c) FROM Categoria c",
                Long.class
        ).getSingleResult();
    }
}
