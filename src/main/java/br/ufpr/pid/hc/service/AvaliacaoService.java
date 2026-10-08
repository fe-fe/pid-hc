package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.AvaliacaoDao;
import br.ufpr.pid.hc.entity.Avaliacao;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.UUID;

@Stateless
public class AvaliacaoService extends AbstractService<Avaliacao, UUID> {
    @Inject
    private AvaliacaoDao avaliacaoDao;

    @Inject
    private FabricanteService fabricanteService;

    @Inject
    private MarcaService marcaService;

    @Override
    protected AbstractDao<Avaliacao, UUID> getDao() { return avaliacaoDao; }

    @RolesAllowed({"ADMINISTRADOR", "CONSULTOR", "AVALIADOR", "ANALISTA"})
    public Avaliacao registrar(Avaliacao avaliacao, String fabricante, String marca) {
        avaliacao.setFabricante(fabricanteService.obterOuCriar(fabricante));
        avaliacao.setMarca(marca == null || marca.isBlank() ? null : marcaService.obterOuCriar(marca));
        return salvar(avaliacao);
    }
}
