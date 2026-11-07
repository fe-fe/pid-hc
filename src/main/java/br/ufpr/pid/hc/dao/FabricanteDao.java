package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Fabricante;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class FabricanteDao extends AbstractDao<Fabricante, UUID> {
    public FabricanteDao() { super(Fabricante.class); }

    public List<Fabricante> buscarPorTermo(int pagina, int tamanhoPagina, CampoOrdenacao ordenacao,
            String termo, boolean incluirInativos) {
        TypedQuery<Fabricante> query = entityManager.createQuery(
                "SELECT f FROM Fabricante f " + filtroTermo(termo, incluirInativos)
                        + "ORDER BY f." + ordenacao.getCampoBanco() + ", f.id", Fabricante.class);
        aplicarTermo(query, termo);
        return query.setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    public long contarPorTermo(String termo, boolean incluirInativos) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT count(f) FROM Fabricante f " + filtroTermo(termo, incluirInativos), Long.class);
        aplicarTermo(query, termo);
        return query.getSingleResult();
    }

    private String filtroTermo(String termo, boolean incluirInativos) {
        String filtro = filtroAtivo("f", incluirInativos);
        if (termo == null || termo.isBlank()) {
            return filtro;
        }
        return filtro + (incluirInativos ? "WHERE " : "AND ")
                + "lower(f.nome) LIKE :termo ESCAPE '!' ";
    }

    private void aplicarTermo(TypedQuery<?> query, String termo) {
        if (termo != null && !termo.isBlank()) {
            query.setParameter("termo", "%" + termo.trim().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%");
        }
    }

    public Optional<Fabricante> buscarPorNome(String nome) {
        return entityManager.createQuery(
                        "SELECT f FROM Fabricante f WHERE upper(f.nome) = upper(:nome)", Fabricante.class)
                .setParameter("nome", nome)
                .getResultStream()
                .findFirst();
    }

    public List<String> listarNomes() {
        return entityManager.createQuery(
                "SELECT f.nome FROM Fabricante f " + filtroAtivo("f", false) + "ORDER BY f.nome",
                String.class
        ).getResultList();
    }
}
