package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.UsuarioDao;
import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.exception.DuplicateEmailException;
import br.ufpr.pid.hc.exception.MissingRequiredFieldsException;
import br.ufpr.pid.hc.exception.WeakPasswordException;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

import java.util.UUID;

@Stateless
public class UsuarioService extends AbstractService<Usuario, UUID> {

    @Inject
    private UsuarioDao usuarioDao;

    @Inject
    private Pbkdf2PasswordHash hashUtil;

    @Override
    protected AbstractDao<Usuario, UUID> getDao() {
        return usuarioDao;
    }

    @PermitAll
    public Usuario buscarPorEmail(String email) {
        return usuarioDao.buscarPorEmail(email);
    }

    @Override
    @PermitAll
    public Usuario salvar(Usuario usuario) {
        if (usuario.getId() == null) {
            return cadastrar(usuario);
        }

        return atualizar(usuario);
    }

    private Usuario cadastrar(Usuario usuario) {
        validarCamposObrigatorios(usuario, true);
        validarEmailDuplicado(usuario);

        if (usuario.getSenha().length() < 6) {
            throw new WeakPasswordException();
        }

        String senhaHash = hashUtil.generate(usuario.getSenha().toCharArray());
        usuario.setSenha(senhaHash);

        if (usuario.getAtivo() == null) {
            usuario.setAtivo(true);
        }

        return usuarioDao.salvar(usuario, null);
    }

    private Usuario atualizar(Usuario dadosAtualizados) {
        validarCamposObrigatorios(dadosAtualizados, false);
        validarEmailDuplicado(dadosAtualizados);

        Usuario usuario = usuarioDao.buscarPorId(dadosAtualizados.getId());
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        usuario.setNome(dadosAtualizados.getNome());
        usuario.setEmail(dadosAtualizados.getEmail());
        usuario.setPerfil(dadosAtualizados.getPerfil());
        usuario.setSetor(dadosAtualizados.getSetor());

        return usuarioDao.salvar(usuario, null);
    }

    private void validarCamposObrigatorios(Usuario usuario, boolean validarSenha) {
        boolean nomeVazio = usuario.getNome() == null || usuario.getNome().isBlank();
        boolean emailVazio = usuario.getEmail() == null || usuario.getEmail().isBlank();
        boolean perfilVazio = usuario.getPerfil() == null;
        boolean senhaVazia = validarSenha && (usuario.getSenha() == null || usuario.getSenha().isBlank());

        if (nomeVazio || emailVazio || perfilVazio || senhaVazia) {
            throw new MissingRequiredFieldsException();
        }
    }

    private void validarEmailDuplicado(Usuario usuario) {
        Usuario usuarioComMesmoEmail = buscarPorEmail(usuario.getEmail());

        if (usuarioComMesmoEmail != null &&
                !usuarioComMesmoEmail.getId().equals(usuario.getId())) {
            throw new DuplicateEmailException();
        }
    }
}
