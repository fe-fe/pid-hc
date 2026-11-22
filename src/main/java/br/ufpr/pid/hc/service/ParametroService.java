package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.ParametroDao;
import br.ufpr.pid.hc.entity.Parametro;
import br.ufpr.pid.hc.exception.DomainException;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class ParametroService {

    public static final int VALIDADE_PADRAO = 24;
    public static final int VALIDADE_MAXIMA = 120;

    @Inject
    private ParametroDao parametroDao;

    @PermitAll
    public int validade() {
        return parametroDao.buscar(Parametro.VALIDADE).map(Integer::parseInt).orElse(VALIDADE_PADRAO);
    }

    @RolesAllowed("ADMINISTRADOR")
    public void alterarValidade(Integer meses) {
        if (meses == null || meses < 1 || meses > VALIDADE_MAXIMA) {
            throw new DomainException("O prazo de validade deve ficar entre 1 e " + VALIDADE_MAXIMA + " meses.");
        }
        parametroDao.salvar(Parametro.VALIDADE, String.valueOf(meses));
    }
}
