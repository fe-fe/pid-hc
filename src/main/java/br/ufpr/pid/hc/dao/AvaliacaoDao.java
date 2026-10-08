package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Avaliacao;
import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import br.ufpr.pid.hc.enumeration.ResultadoTecnico;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AvaliacaoDao extends AbstractDao<Avaliacao, UUID> {

    public AvaliacaoDao() { super(Avaliacao.class); }

    @Override
    public List<Avaliacao> buscar(int pagina, int tamanhoPagina, CampoOrdenacao campoOrdenacao, boolean incluirInativos) {
        return entityManager.createQuery(
                        "SELECT a FROM Avaliacao a JOIN a.material m JOIN a.fabricante f LEFT JOIN a.marca ma " +
                                filtroAtivo("a", incluirInativos) +
                                "ORDER BY " + campoOrdenacao.getCampoBanco(),
                        Avaliacao.class)
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    public List<Material> buscarMateriaisAvaliados(int pagina, int tamanhoPagina, String termo,
                                                   String fabricante, ResultadoTecnico resultado) {
        TypedQuery<Material> query = entityManager.createQuery(
                "SELECT DISTINCT m FROM Avaliacao a JOIN a.material m JOIN a.fabricante f LEFT JOIN a.marca ma " +
                        filtros(termo, fabricante, resultado) +
                        "ORDER BY m.codigo",
                Material.class);
        aplicarFiltros(query, termo, fabricante, resultado);
        return query
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    public long contarMateriaisAvaliados(String termo, String fabricante, ResultadoTecnico resultado) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT count(DISTINCT m) FROM Avaliacao a JOIN a.material m JOIN a.fabricante f LEFT JOIN a.marca ma " +
                        filtros(termo, fabricante, resultado),
                Long.class);
        aplicarFiltros(query, termo, fabricante, resultado);
        return query.getSingleResult();
    }

    public List<Avaliacao> buscarPorMateriais(List<Material> materiais, String termo,
                                              String fabricante, ResultadoTecnico resultado) {
        if (materiais.isEmpty()) {
            return List.of();
        }

        TypedQuery<Avaliacao> query = entityManager.createQuery(
                "SELECT a FROM Avaliacao a JOIN a.material m JOIN a.fabricante f LEFT JOIN a.marca ma " +
                        filtros(termo, fabricante, resultado) +
                        "AND m IN :materiais " +
                        "ORDER BY m.codigo, a.data DESC",
                Avaliacao.class);
        aplicarFiltros(query, termo, fabricante, resultado);
        return query.setParameter("materiais", materiais).getResultList();
    }

    public List<String> buscarFabricantesAvaliados() {
        return entityManager.createQuery(
                "SELECT DISTINCT f.nome FROM Avaliacao a JOIN a.fabricante f " +
                        "WHERE (a.ativo IS NULL OR a.ativo = true) " +
                        "ORDER BY f.nome",
                String.class
        ).getResultList();
    }

    private String filtros(String termo, String fabricante, ResultadoTecnico resultado) {
        StringBuilder where = new StringBuilder("WHERE (a.ativo IS NULL OR a.ativo = true) ");
        if (termo != null && !termo.isBlank()) {
            where.append("AND (lower(m.codigo) LIKE :termo OR lower(m.nome) LIKE :termo ")
                    .append("OR lower(f.nome) LIKE :termo OR lower(ma.nome) LIKE :termo) ");
        }
        if (fabricante != null && !fabricante.isBlank()) {
            where.append("AND f.nome = :fabricante ");
        }
        if (resultado != null) {
            where.append("AND a.resultado = :resultado ");
        }
        return where.toString();
    }

    private void aplicarFiltros(TypedQuery<?> query, String termo, String fabricante, ResultadoTecnico resultado) {
        if (termo != null && !termo.isBlank()) {
            query.setParameter("termo", "%" + termo.trim().toLowerCase() + "%");
        }
        if (fabricante != null && !fabricante.isBlank()) {
            query.setParameter("fabricante", fabricante);
        }
        if (resultado != null) {
            query.setParameter("resultado", resultado);
        }
    }
}
