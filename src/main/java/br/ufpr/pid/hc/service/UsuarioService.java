package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.dao.UsuarioDao;
import br.ufpr.pid.hc.enumeration.Perfil;
import br.ufpr.pid.hc.exception.CamposVaziosException;
import br.ufpr.pid.hc.exception.EmailJaCadastradoException;
import br.ufpr.pid.hc.exception.SenhaCurtaException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

@ApplicationScoped
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
            throw new CamposVaziosException();
        }

        if (usuario.getSenha().length() < 6) {
            throw new SenhaCurtaException();
        }

        if (buscarPorEmail(usuario.getEmail()) != null) {
            throw new EmailJaCadastradoException();
        }

        String senhaHash = hashUtil.generate(usuario.getSenha().toCharArray());
        usuario.setSenha(senhaHash);
        usuario.setPerfil(Perfil.ADMINISTRADOR);

        usuarioDao.salvar(usuario, null);
        return usuario;
    }
}
