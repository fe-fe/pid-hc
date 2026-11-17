package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.dto.ConsultaMaterialFiltro;
import br.ufpr.pid.hc.dto.ResumoMaterial;
import br.ufpr.pid.hc.entity.Avaliacao;
import br.ufpr.pid.hc.entity.Desvio;
import br.ufpr.pid.hc.entity.Fabricante;
import br.ufpr.pid.hc.entity.Marca;
import br.ufpr.pid.hc.enumeration.ResultadoTecnico;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DashboardMaterialDao {
    @PersistenceContext
    private EntityManager entityManager;

    public ResumoMaterial resumir(ConsultaMaterialFiltro filtro) {
        List<Object[]> resultados = filtrar(entityManager.createQuery(
                "SELECT e.resultado, count(e) FROM Avaliacao e " + where(filtro)
                        + "GROUP BY e.resultado", Object[].class), filtro).getResultList();
        long total = 0;
        long aprovadas = 0;
        long reprovadas = 0;
        for (Object[] resultado : resultados) {
            long quantidade = ((Number) resultado[1]).longValue();
            total += quantidade;
            if (resultado[0] == ResultadoTecnico.APROVADO) aprovadas += quantidade;
            if (resultado[0] == ResultadoTecnico.REPROVADO) reprovadas += quantidade;
        }
        long notificacoes = filtrar(entityManager.createQuery(
                "SELECT count(e) FROM Desvio e " + where(filtro), Long.class), filtro).getSingleResult();
        return new ResumoMaterial(total, aprovadas, reprovadas, notificacoes);
    }

    public List<Avaliacao> buscarAvaliacoes(ConsultaMaterialFiltro filtro, int pagina, int tamanho) {
        return buscarRegistros(Avaliacao.class, filtro, pagina, tamanho);
    }

    public List<Desvio> buscarNotificacoes(ConsultaMaterialFiltro filtro, int pagina, int tamanho) {
        return buscarRegistros(Desvio.class, filtro, pagina, tamanho);
    }

    private <T> List<T> buscarRegistros(Class<T> tipo, ConsultaMaterialFiltro filtro, int pagina, int tamanho) {
        return filtrar(entityManager.createQuery("SELECT e FROM " + tipo.getSimpleName()
                + " e JOIN FETCH e.fabricante LEFT JOIN FETCH e.marca " + where(filtro)
                + "ORDER BY CASE WHEN e.data IS NULL THEN 1 ELSE 0 END, e.data DESC, e.id DESC", tipo), filtro)
                .setFirstResult(pagina * tamanho).setMaxResults(tamanho).getResultList();
    }

    public List<Fabricante> buscarFabricantes(UUID materialId) {
        return entityManager.createQuery("""
                SELECT f FROM Fabricante f WHERE
                EXISTS (SELECT a.id FROM Avaliacao a WHERE a.fabricante = f
                    AND a.material.id = :materialId AND (a.ativo IS NULL OR a.ativo = true))
                OR EXISTS (SELECT d.id FROM Desvio d WHERE d.fabricante = f
                    AND d.material.id = :materialId AND (d.ativo IS NULL OR d.ativo = true))
                ORDER BY f.nome, f.id
                """, Fabricante.class).setParameter("materialId", materialId).getResultList();
    }

    public List<Marca> buscarMarcas(UUID materialId) {
        return entityManager.createQuery("""
                SELECT m FROM Marca m WHERE
                EXISTS (SELECT a.id FROM Avaliacao a WHERE a.marca = m
                    AND a.material.id = :materialId AND (a.ativo IS NULL OR a.ativo = true))
                OR EXISTS (SELECT d.id FROM Desvio d WHERE d.marca = m
                    AND d.material.id = :materialId AND (d.ativo IS NULL OR d.ativo = true))
                ORDER BY m.nome, m.id
                """, Marca.class).setParameter("materialId", materialId).getResultList();
    }

    private String where(ConsultaMaterialFiltro filtro) {
        StringBuilder where = new StringBuilder(
                "WHERE e.material.id = :materialId AND (e.ativo IS NULL OR e.ativo = true) ");
        if (filtro.dataInicio() != null) where.append("AND e.data >= :dataInicio ");
        if (filtro.dataFim() != null) where.append("AND e.data <= :dataFim ");
        if (filtro.fabricanteId() != null) where.append("AND e.fabricante.id = :fabricanteId ");
        if (filtro.marcaId() != null) where.append("AND e.marca.id = :marcaId ");
        return where.toString();
    }

    private <T> TypedQuery<T> filtrar(TypedQuery<T> query, ConsultaMaterialFiltro filtro) {
        query.setParameter("materialId", filtro.materialId());
        if (filtro.dataInicio() != null) query.setParameter("dataInicio", filtro.dataInicio());
        if (filtro.dataFim() != null) query.setParameter("dataFim", filtro.dataFim());
        if (filtro.fabricanteId() != null) query.setParameter("fabricanteId", filtro.fabricanteId());
        if (filtro.marcaId() != null) query.setParameter("marcaId", filtro.marcaId());
        return query;
    }
}
