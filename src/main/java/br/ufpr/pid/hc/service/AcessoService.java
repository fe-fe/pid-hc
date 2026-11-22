package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AcessoDao;
import br.ufpr.pid.hc.entity.Acesso;
import br.ufpr.pid.hc.enumeration.TipoAcesso;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDateTime;

@Stateless
@PermitAll
public class AcessoService {

    @Inject
    private AcessoDao acessoDao;

    public void registrar(String usuario, TipoAcesso tipo) {
        if (usuario == null || usuario.isBlank()) {
            return;
        }
        Acesso acesso = new Acesso();
        acesso.setData(LocalDateTime.now());
        acesso.setUsuario(usuario.trim());
        acesso.setTipo(tipo);
        acessoDao.salvar(acesso);
    }
}
