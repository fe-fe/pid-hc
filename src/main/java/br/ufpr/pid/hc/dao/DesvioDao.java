package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Desvio;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@ApplicationScoped
public class DesvioDao extends AbstractDao<Desvio, UUID> {
    public DesvioDao() { super(Desvio.class); }

    public List<Desvio> buscarPorTermo(int pagina, int tamanhoPagina, CampoOrdenacao ordenacao,
            String termo, boolean incluirInativos) {
        TypedQuery<Desvio> query = entityManager.createQuery(
                "SELECT d FROM Desvio d " + filtroTermo(termo, incluirInativos)
                        + "ORDER BY d." + ordenacao.getCampoBanco() + ", d.id", Desvio.class);
        aplicarTermo(query, termo);
        return query.setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    public long contarPorTermo(String termo, boolean incluirInativos) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT count(d) FROM Desvio d " + filtroTermo(termo, incluirInativos), Long.class);
        aplicarTermo(query, termo);
        return query.getSingleResult();
    }

    private String filtroTermo(String termo, boolean incluirInativos) {
        String filtro = filtroAtivo("d", incluirInativos);
        if (termo == null || termo.isBlank()) {
            return filtro;
        }
        return filtro + (incluirInativos ? "WHERE " : "AND ")
                + "(lower(d.vigihosp) LIKE :termo ESCAPE '!' "
                + "OR lower(d.material.codigo) LIKE :termo ESCAPE '!') ";
    }

    private void aplicarTermo(TypedQuery<?> query, String termo) {
        if (termo != null && !termo.isBlank()) {
            query.setParameter("termo", "%" + termo.trim().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%");
        }
    }

    public boolean existeOutroVigihosp(String vigihosp, UUID id) {
        return entityManager.createQuery(
                        "SELECT count(d) FROM Desvio d WHERE upper(d.vigihosp) = upper(:vigihosp) AND d.id <> :id",
                        Long.class)
                .setParameter("vigihosp", vigihosp)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }

    public List<String> listarVigihospExistentes(Collection<String> vigihosps) {
        if (vigihosps.isEmpty()) {
            return List.of();
        }
        return entityManager.createQuery(
                        "SELECT d.vigihosp FROM Desvio d WHERE upper(d.vigihosp) IN :vigihosps", String.class)
                .setParameter("vigihosps", vigihosps.stream().map(String::toUpperCase).toList())
                .getResultList();
    }
}
