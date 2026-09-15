package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.CategoriaDao;
import br.ufpr.pid.hc.dao.SetorDao;
import br.ufpr.pid.hc.entity.Setor;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Stateless
public class SetorService {

    @Inject
    private SetorDao setorDao;

    @PermitAll
    public List<Setor> buscar(int pagina, int tamanhoPagina) {
        try {
            return setorDao.buscar(pagina, tamanhoPagina);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            return List.of();
        }
    }

    @RolesAllowed({"ADMINISTRADOR", "ANALISTA"})
    public Setor salvar(Setor setor) {
        return setorDao.salvar(setor, null);
    }

    @PermitAll
    public long contarTotal() {
        return setorDao.contarTotal();
    }

}
