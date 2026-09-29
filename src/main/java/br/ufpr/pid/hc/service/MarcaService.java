package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.MarcaDao;
import br.ufpr.pid.hc.entity.Marca;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.UUID;

@Stateless
public class MarcaService extends AbstractService<Marca, UUID> {
    @Inject
    private MarcaDao marcaDao;

    @Override
    protected AbstractDao<Marca, UUID> getDao() { return marcaDao; }
}
