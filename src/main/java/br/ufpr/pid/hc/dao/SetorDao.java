package br.ufpr.pid.hc.dao;


import br.ufpr.pid.hc.entity.Setor;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SetorDao extends AbstractDao<Setor, UUID> {

    public SetorDao() { super(Setor.class); }

    public List<Setor> buscar(int pagina, int tamanhoPagina) {
        return entityManager.createQuery(
                        "SELECT s FROM Setor s ORDER BY s.codigo",
                        Setor.class
                )
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    public long contarTotal() {
        return entityManager.createQuery(
                "SELECT count(s) FROM Setor s",
                Long.class
        ).getSingleResult();
    }
}