package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.DashboardMaterialDao;
import br.ufpr.pid.hc.dto.ConsultaMaterialFiltro;
import br.ufpr.pid.hc.dto.ResumoMaterial;
import br.ufpr.pid.hc.entity.Avaliacao;
import br.ufpr.pid.hc.entity.Desvio;
import br.ufpr.pid.hc.entity.Fabricante;
import br.ufpr.pid.hc.entity.Marca;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@Stateless
@RolesAllowed({"ADMINISTRADOR", "ANALISTA"})
public class DashboardMaterialService {
    @Inject
    private DashboardMaterialDao dashboardMaterialDao;

    public ResumoMaterial resumir(ConsultaMaterialFiltro filtro) {
        return dashboardMaterialDao.resumir(filtro);
    }

    public List<Avaliacao> buscarAvaliacoes(ConsultaMaterialFiltro filtro, int pagina, int tamanho) {
        return dashboardMaterialDao.buscarAvaliacoes(filtro, pagina, tamanho);
    }

    public List<Desvio> buscarNotificacoes(ConsultaMaterialFiltro filtro, int pagina, int tamanho) {
        return dashboardMaterialDao.buscarNotificacoes(filtro, pagina, tamanho);
    }

    public List<Fabricante> buscarFabricantes(UUID materialId) {
        return dashboardMaterialDao.buscarFabricantes(materialId);
    }

    public List<Marca> buscarMarcas(UUID materialId) {
        return dashboardMaterialDao.buscarMarcas(materialId);
    }
}
