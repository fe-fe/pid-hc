package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.DesvioDao;
import br.ufpr.pid.hc.entity.Desvio;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.UUID;

@Stateless
public class DesvioService extends AbstractService<Desvio, UUID> {
    @Inject
    private DesvioDao desvioDao;

    @Override
    protected AbstractDao<Desvio, UUID> getDao() { return desvioDao; }
}
