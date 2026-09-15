package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.dao.UsuarioDao;
import br.ufpr.pid.hc.exception.MissingRequiredFieldsException;
import br.ufpr.pid.hc.exception.DuplicateEmailException;
import br.ufpr.pid.hc.exception.WeakPasswordException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

@Stateless
public class UsuarioService {

    @Inject
    private UsuarioDao usuarioDao;

    @Inject
    private Pbkdf2PasswordHash hashUtil;

    public Usuario buscarPorEmail(String email) {
        return usuarioDao.buscarPorEmail(email);
    }

    public Usuario salvar(Usuario usuario) {
        boolean emailVazio = usuario.getEmail() == null || usuario.getEmail().isBlank();
        boolean senhaVazio = usuario.getSenha() == null || usuario.getSenha().isBlank();

        if (emailVazio || senhaVazio)  {
            throw new MissingRequiredFieldsException();
        }

        if (usuario.getSenha().length() < 6) {
            throw new WeakPasswordException();
        }

        if (buscarPorEmail(usuario.getEmail()) != null) {
            throw new DuplicateEmailException();
        }

        String senhaHash = hashUtil.generate(usuario.getSenha().toCharArray());
        usuario.setSenha(senhaHash);

        usuarioDao.salvar(usuario, null);
        return usuario;
    }
}
