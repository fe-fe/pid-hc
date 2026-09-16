package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.MaterialDao;
import br.ufpr.pid.hc.entity.Material;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.UUID;

@Stateless
public class MaterialService extends AbstractService<Material, UUID> {
    @Inject
    private MaterialDao materialDao;

    @Override
    protected AbstractDao<Material, UUID> getDao() { return materialDao; }
}
