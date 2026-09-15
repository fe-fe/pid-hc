package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.CategoriaDao;
import br.ufpr.pid.hc.entity.Categoria;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Stateless
public class CategoriaService {

    @Inject
    private CategoriaDao categoriaDao;

    @PermitAll
    public List<Categoria> buscar() {
        try {
            return categoriaDao.buscar();
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            return List.of();
        }
    }

    @RolesAllowed({"ADMINISTRADOR", "ANALISTA"})
    public Categoria salvar(Categoria cat) {
        categoriaDao.salvar(cat, null);
        return cat;
    }
}
