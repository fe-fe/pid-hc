package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.MarcaDao;
import br.ufpr.pid.hc.entity.Marca;
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
public class MarcaService extends AbstractService<Marca, UUID> {
    @Inject
    private MarcaDao marcaDao;

    @Override
    protected AbstractDao<Marca, UUID> getDao() { return marcaDao; }

    @PermitAll
    public List<String> listarNomes() {
        return marcaDao.listarNomes();
    }

    @RolesAllowed({"ADMINISTRADOR", "CONSULTOR", "AVALIADOR", "ANALISTA"})
    public Marca obterOuCriar(String nome) {
        String nomePadronizado = padronizarNome(nome);
        return marcaDao.buscarPorNome(nomePadronizado).orElseGet(() -> {
            Marca novo = new Marca();
            novo.setNome(nomePadronizado);
            return marcaDao.salvar(novo, null);
        });
    }

    @Override
    @RolesAllowed({"ADMINISTRADOR", "CONSULTOR", "AVALIADOR", "ANALISTA"})
    public Marca salvar(Marca marca) {
        if (marca.getNome() == null || marca.getNome().isBlank()) {
            throw new MissingRequiredFieldsException();
        }
        marca.setNome(padronizarNome(marca.getNome()));
        marcaDao.buscarPorNome(marca.getNome())
                .filter(existente -> !Objects.equals(existente.getId(), marca.getId()))
                .ifPresent(existente -> {
                    throw new DuplicateRecordException("Já existe um cadastro com o nome " + existente.getNome());
                });
        return marcaDao.salvar(marca, null);
    }
}
