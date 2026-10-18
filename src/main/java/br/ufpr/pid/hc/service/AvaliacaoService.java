package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.AvaliacaoDao;
import br.ufpr.pid.hc.entity.Avaliacao;
import br.ufpr.pid.hc.entity.Fabricante;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Stateless
public class AvaliacaoService extends AbstractService<Avaliacao, UUID> {
    @Inject
    private AvaliacaoDao avaliacaoDao;

    @Inject
    private MaterialService materialService;

    @Inject
    private FabricanteService fabricanteService;

    @Inject
    private MarcaService marcaService;

    @Override
    protected AbstractDao<Avaliacao, UUID> getDao() {
        return avaliacaoDao;
    }

    @PermitAll
    public List<Fabricante> buscarFabricantesPorNome(String termo) {
        return avaliacaoDao.buscarFabricantesAvaliadosPorNome(termo);
    }

    @PermitAll
    public List<Avaliacao> buscarComFiltros(int pagina, int tamanhoPagina, CampoOrdenacao campoOrdenacao,
            String termoMaterial, List<UUID> fabricantes,
            LocalDate dataInicio, LocalDate dataFim) {
        return avaliacaoDao.buscarComFiltros(pagina, tamanhoPagina, campoOrdenacao, termoMaterial, fabricantes,
                dataInicio, dataFim);
    }

    @PermitAll
    public long contarComFiltros(String termoMaterial, List<UUID> fabricantes, LocalDate dataInicio,
            LocalDate dataFim) {
        return avaliacaoDao.contarComFiltros(termoMaterial, fabricantes, dataInicio, dataFim);
    }

    @RolesAllowed({ "ADMINISTRADOR", "ANALISTA" })
    public Avaliacao registrar(Avaliacao avaliacao, String codigoMaterial, String nomeMaterial,
            String fabricante, String marca) {
        avaliacao.setMaterial(materialService.obterOuCriar(codigoMaterial, nomeMaterial));
        avaliacao.setFabricante(fabricanteService.obterOuCriar(fabricante));
        avaliacao.setMarca(marca == null || marca.isBlank() ? null : marcaService.obterOuCriar(marca));
        return salvar(avaliacao);
    }

    @RolesAllowed({ "ADMINISTRADOR", "ANALISTA" })
    public List<Avaliacao> buscarAvaliacoesPorMaterial(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return List.of();
        }

        return avaliacaoDao.buscarAvaliacoesPorCodigoMaterial(codigo.trim());
    }
}
