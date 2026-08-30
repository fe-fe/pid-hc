package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.dao.UsuarioDao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UsuarioService {

    @Inject
    private UsuarioDao usuarioDao;

    public Usuario buscarPorEmail(String email) {
        return usuarioDao.buscarPorEmail(email);
    }
}
