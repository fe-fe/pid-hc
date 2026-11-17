package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Desvio;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@ApplicationScoped
public class DesvioDao extends AbstractDao<Desvio, UUID> {
    public DesvioDao() { super(Desvio.class); }

    public List<Desvio> buscarComFiltros(int pagina, int tamanhoPagina, CampoOrdenacao ordenacao,
            String termo, LocalDate dataInicial, LocalDate dataFinal, boolean incluirInativos) {
        TypedQuery<Desvio> query = entityManager.createQuery(
                "SELECT d FROM Desvio d " + filtros(termo, dataInicial, dataFinal, incluirInativos)
                        + "ORDER BY d." + ordenacao.getCampoBanco() + ", d.id", Desvio.class);
        aplicarFiltros(query, termo, dataInicial, dataFinal);
        return query.setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    public long contarComFiltros(String termo, LocalDate dataInicial, LocalDate dataFinal,
            boolean incluirInativos) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT count(d) FROM Desvio d " + filtros(termo, dataInicial, dataFinal, incluirInativos),
                Long.class);
        aplicarFiltros(query, termo, dataInicial, dataFinal);
        return query.getSingleResult();
    }

    private String filtros(String termo, LocalDate dataInicial, LocalDate dataFinal, boolean incluirInativos) {
        StringBuilder filtro = new StringBuilder(incluirInativos ? "WHERE 1 = 1 " : filtroAtivo("d", false));
        if (termo != null && !termo.isBlank()) {
            filtro.append("AND (lower(d.vigihosp) LIKE :termo ESCAPE '!' ")
                    .append("OR lower(d.material.codigo) LIKE :termo ESCAPE '!') ");
        }
        if (dataInicial != null) {
            filtro.append("AND d.data >= :dataInicial ");
        }
        if (dataFinal != null) {
            filtro.append("AND d.data <= :dataFinal ");
        }
        return filtro.toString();
    }

    private void aplicarFiltros(TypedQuery<?> query, String termo, LocalDate dataInicial, LocalDate dataFinal) {
        if (termo != null && !termo.isBlank()) {
            query.setParameter("termo", "%" + termo.trim().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%");
        }
        if (dataInicial != null) {
            query.setParameter("dataInicial", dataInicial);
        }
        if (dataFinal != null) {
            query.setParameter("dataFinal", dataFinal);
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
