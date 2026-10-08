package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.FabricanteDao;
import br.ufpr.pid.hc.entity.Fabricante;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@Stateless
public class FabricanteService extends AbstractService<Fabricante, UUID> {
    @Inject
    private FabricanteDao fabricanteDao;

    @Override
    protected AbstractDao<Fabricante, UUID> getDao() { return fabricanteDao; }

    @PermitAll
    public List<String> listarNomes() {
        return fabricanteDao.listarNomes();
    }

    @RolesAllowed({"ADMINISTRADOR", "CONSULTOR", "AVALIADOR", "ANALISTA"})
    public Fabricante obterOuCriar(String nome) {
        String nomePadronizado = nome.trim().replaceAll("\\s+", " ").toUpperCase();
        return fabricanteDao.buscarPorNome(nomePadronizado).orElseGet(() -> {
            Fabricante novo = new Fabricante();
            novo.setNome(nomePadronizado);
            return fabricanteDao.salvar(novo, null);
        });
    }
}
