package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.MaterialDao;
import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.exception.DuplicateRecordException;
import br.ufpr.pid.hc.exception.MissingRequiredFieldsException;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Stateless
public class MaterialService extends AbstractService<Material, UUID> {
    @Inject
    private MaterialDao materialDao;

    @Override
    protected AbstractDao<Material, UUID> getDao() { return materialDao; }

    @PermitAll
    public Optional<Material> buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return materialDao.buscarPorCodigo(codigo.trim());
    }

    @RolesAllowed({"ADMINISTRADOR", "CONSULTOR", "AVALIADOR", "ANALISTA"})
    public Material obterOuCriar(String codigo, String nome) {
        return buscarPorCodigo(codigo).orElseGet(() -> {
            Material novo = new Material();
            novo.setCodigo(codigo);
            novo.setNome(nome);
            return salvar(novo);
        });
    }

    @Override
    @RolesAllowed({"ADMINISTRADOR", "CONSULTOR", "AVALIADOR", "ANALISTA"})
    public Material salvar(Material material) {
        if (material.getCodigo() == null || material.getCodigo().isBlank()
                || material.getNome() == null || material.getNome().isBlank()) {
            throw new MissingRequiredFieldsException();
        }
        material.setCodigo(material.getCodigo().trim());
        material.setNome(material.getNome().trim());
        materialDao.buscarPorCodigo(material.getCodigo())
                .filter(existente -> !Objects.equals(existente.getId(), material.getId()))
                .ifPresent(existente -> {
                    throw new DuplicateRecordException("Já existe um material com o código " + existente.getCodigo());
                });
        return materialDao.salvar(material, null);
    }
}
