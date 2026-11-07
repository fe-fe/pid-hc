package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class MaterialDao extends AbstractDao<Material, UUID> {
    public MaterialDao() { super(Material.class); }

    public List<Material> buscarPorTermo(int pagina, int tamanhoPagina, CampoOrdenacao ordenacao,
            String termo, boolean incluirInativos) {
        TypedQuery<Material> query = entityManager.createQuery(
                "SELECT m FROM Material m " + filtroTermo(termo, incluirInativos)
                        + "ORDER BY m." + ordenacao.getCampoBanco() + ", m.id", Material.class);
        aplicarTermo(query, termo);
        return query.setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    public long contarPorTermo(String termo, boolean incluirInativos) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT count(m) FROM Material m " + filtroTermo(termo, incluirInativos), Long.class);
        aplicarTermo(query, termo);
        return query.getSingleResult();
    }

    private String filtroTermo(String termo, boolean incluirInativos) {
        String filtro = filtroAtivo("m", incluirInativos);
        if (termo == null || termo.isBlank()) {
            return filtro;
        }
        return filtro + (incluirInativos ? "WHERE " : "AND ")
                + "(lower(m.codigo) LIKE :termo ESCAPE '!' "
                + "OR lower(m.nome) LIKE :termo ESCAPE '!') ";
    }

    private void aplicarTermo(TypedQuery<?> query, String termo) {
        if (termo != null && !termo.isBlank()) {
            query.setParameter("termo", termoBusca(termo));
        }
    }

    private String termoBusca(String termo) {
        return "%" + termo.trim().toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    public Optional<Material> buscarPorCodigo(String codigo) {
        return entityManager.createQuery(
                        "SELECT m FROM Material m WHERE m.codigo = :codigo", Material.class)
                .setParameter("codigo", codigo)
                .getResultStream()
                .findFirst();
    }
}
