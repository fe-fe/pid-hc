package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.entity.Auditavel;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class AbstractService<T extends Auditavel, ID> {

    protected abstract AbstractDao<T, ID> getDao();

    @PermitAll
    public List<T> buscar(int pagina, int tamanhoPagina, CampoOrdenacao campoOrdenacao) {
        try {
            return getDao().buscar(pagina, tamanhoPagina, campoOrdenacao);
        } catch (RuntimeException e) {
            log.error(e.getClass().getName() + ": " + e.getMessage());
            return List.of();
        }
    }

    @PermitAll
    public long contarTotal() {
        return getDao().contarTotal();
    }

    @RolesAllowed({"ADMINISTRADOR", "ANALISTA"})
    public T salvar(T entidade) {
        return getDao().salvar(entidade, null);
    }

    @PermitAll
    public T buscarPorId(ID id) {
        return getDao().buscarPorId(id);
    }

}
