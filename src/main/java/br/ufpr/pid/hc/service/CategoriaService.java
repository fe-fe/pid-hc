package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.CategoriaDao;
import br.ufpr.pid.hc.entity.Categoria;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.UUID;

@Stateless
public class CategoriaService extends AbstractService<Categoria, UUID> {
    @Inject
    private CategoriaDao categoriaDao;

    @Override
    protected AbstractDao<Categoria, UUID> getDao() { return categoriaDao; }
}
