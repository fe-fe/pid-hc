package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.SetorDao;
import br.ufpr.pid.hc.entity.Setor;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.UUID;

@Stateless
public class SetorService extends AbstractService<Setor, UUID> {
    @Inject
    private SetorDao setorDao;

    @Override
    protected AbstractDao<Setor, UUID> getDao() { return setorDao; }
}
