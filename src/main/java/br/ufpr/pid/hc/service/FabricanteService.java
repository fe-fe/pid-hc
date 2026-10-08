package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.FabricanteDao;
import br.ufpr.pid.hc.entity.Fabricante;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import br.ufpr.pid.hc.exception.DuplicateRecordException;
import br.ufpr.pid.hc.exception.MissingRequiredFieldsException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Objects;
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
        String nomePadronizado = padronizarNome(nome);
        return fabricanteDao.buscarPorNome(nomePadronizado).orElseGet(() -> {
            Fabricante novo = new Fabricante();
            novo.setNome(nomePadronizado);
            return fabricanteDao.salvar(novo, null);
        });
    }

    @Override
    @RolesAllowed({"ADMINISTRADOR", "CONSULTOR", "AVALIADOR", "ANALISTA"})
    public Fabricante salvar(Fabricante fabricante) {
        if (fabricante.getNome() == null || fabricante.getNome().isBlank()) {
            throw new MissingRequiredFieldsException();
        }
        fabricante.setNome(padronizarNome(fabricante.getNome()));
        fabricanteDao.buscarPorNome(fabricante.getNome())
                .filter(existente -> !Objects.equals(existente.getId(), fabricante.getId()))
                .ifPresent(existente -> {
                    throw new DuplicateRecordException("Já existe um cadastro com o nome " + existente.getNome());
                });
        return fabricanteDao.salvar(fabricante, null);
    }
}
