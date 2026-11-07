package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Marca;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class MarcaDao extends AbstractDao<Marca, UUID> {
    public MarcaDao() { super(Marca.class); }

    public List<Marca> buscarPorTermo(int pagina, int tamanhoPagina, CampoOrdenacao ordenacao,
            String termo, boolean incluirInativos) {
        TypedQuery<Marca> query = entityManager.createQuery(
                "SELECT m FROM Marca m " + filtroTermo(termo, incluirInativos)
                        + "ORDER BY m." + ordenacao.getCampoBanco() + ", m.id", Marca.class);
        aplicarTermo(query, termo);
        return query.setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    public long contarPorTermo(String termo, boolean incluirInativos) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT count(m) FROM Marca m " + filtroTermo(termo, incluirInativos), Long.class);
        aplicarTermo(query, termo);
        return query.getSingleResult();
    }

    private String filtroTermo(String termo, boolean incluirInativos) {
        String filtro = filtroAtivo("m", incluirInativos);
        if (termo == null || termo.isBlank()) {
            return filtro;
        }
        return filtro + (incluirInativos ? "WHERE " : "AND ")
                + "lower(m.nome) LIKE :termo ESCAPE '!' ";
    }

    private void aplicarTermo(TypedQuery<?> query, String termo) {
        if (termo != null && !termo.isBlank()) {
            query.setParameter("termo", "%" + termo.trim().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%");
        }
    }

    public Optional<Marca> buscarPorNome(String nome) {
        return entityManager.createQuery(
                        "SELECT m FROM Marca m WHERE upper(m.nome) = upper(:nome)", Marca.class)
                .setParameter("nome", nome)
                .getResultStream()
                .findFirst();
    }

    public List<String> listarNomes() {
        return entityManager.createQuery(
                "SELECT m.nome FROM Marca m " + filtroAtivo("m", false) + "ORDER BY m.nome",
                String.class
        ).getResultList();
    }
}
