package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.UsuarioDao;
import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.Perfil;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import br.ufpr.pid.hc.exception.DomainException;
import br.ufpr.pid.hc.exception.DuplicateEmailException;
import br.ufpr.pid.hc.exception.DuplicateRecordException;
import br.ufpr.pid.hc.exception.LastActiveAdminException;
import br.ufpr.pid.hc.exception.MissingRequiredFieldsException;
import br.ufpr.pid.hc.exception.WeakPasswordException;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

import java.util.List;
import java.util.Locale;
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
        return email == null ? null : usuarioDao.buscarPorEmail(padronizarEmail(email));
    }

    @RolesAllowed("ADMINISTRADOR")
    public List<Usuario> buscarPorNome(int pagina, int tamanhoPagina, CampoOrdenacao ordenacao,
            String nome, boolean incluirInativos) {
        return usuarioDao.buscarPorNome(pagina, tamanhoPagina, ordenacao, nome, incluirInativos);
    }

    @RolesAllowed("ADMINISTRADOR")
    public long contarPorNome(String nome, boolean incluirInativos) {
        return usuarioDao.contarPorNome(nome, incluirInativos);
    }

    @PermitAll
    public boolean existeAdministradorAtivo() {
        return usuarioDao.contarAtivosPorPerfil(Perfil.ADMINISTRADOR) > 0;
    }

    @PermitAll
    public Usuario cadastrarPrimeiroAdministrador(Usuario usuario) {
        if (existeAdministradorAtivo()) {
            throw new DomainException("O primeiro acesso já foi concluído");
        }
        usuario.setPerfil(Perfil.ADMINISTRADOR);
        usuario.setAtivo(true);
        return cadastrar(usuario);
    }

    @Override
    @RolesAllowed("ADMINISTRADOR")
    public Usuario salvar(Usuario usuario) {
        if (usuario.getId() == null) {
            return cadastrar(usuario);
        }

        return atualizar(usuario);
    }

    private Usuario cadastrar(Usuario usuario) {
        validarCamposObrigatorios(usuario, true);
        usuario.setEmail(padronizarEmail(usuario.getEmail()));
        validarEmailDuplicado(usuario);
        usuario.setCpf(usuario.getCpf() == null || usuario.getCpf().isBlank() ? null : usuario.getCpf().trim());
        validarCpfDuplicado(usuario);

        if (usuario.getSenha().length() < 6) {
            throw new WeakPasswordException();
        }

        String senhaHash = hashUtil.generate(usuario.getSenha().toCharArray());
        usuario.setSenha(senhaHash);

        if (usuario.getAtivo() == null) {
            usuario.setAtivo(true);
        }

        return usuarioDao.salvar(usuario);
    }

    private Usuario atualizar(Usuario dadosAtualizados) {
        validarCamposObrigatorios(dadosAtualizados, false);
        dadosAtualizados.setEmail(padronizarEmail(dadosAtualizados.getEmail()));
        validarEmailDuplicado(dadosAtualizados);

        Usuario usuario = usuarioDao.buscarPorId(dadosAtualizados.getId());
        if (usuario == null) {
            throw new DomainException("Usuário não encontrado");
        }

        validarUltimoAdministradorAtivo(usuario, dadosAtualizados);

        usuario.setNome(dadosAtualizados.getNome());
        usuario.setEmail(dadosAtualizados.getEmail());
        usuario.setPerfil(dadosAtualizados.getPerfil());
        usuario.setAtivo(dadosAtualizados.getAtivo());

        return usuarioDao.salvar(usuario);
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

    private void validarUltimoAdministradorAtivo(Usuario atual, Usuario dadosAtualizados) {
        boolean eraAdminAtivo = atual.getPerfil() == Perfil.ADMINISTRADOR && !atual.isInativo();
        boolean continuaAdminAtivo = dadosAtualizados.getPerfil() == Perfil.ADMINISTRADOR && !dadosAtualizados.isInativo();

        if (eraAdminAtivo && !continuaAdminAtivo
                && usuarioDao.contarAtivosPorPerfil(Perfil.ADMINISTRADOR) <= 1) {
            throw new LastActiveAdminException();
        }
    }

    private void validarEmailDuplicado(Usuario usuario) {
        Usuario usuarioComMesmoEmail = buscarPorEmail(usuario.getEmail());

        if (usuarioComMesmoEmail != null &&
                !usuarioComMesmoEmail.getId().equals(usuario.getId())) {
            throw new DuplicateEmailException();
        }
    }

    private static String padronizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validarCpfDuplicado(Usuario usuario) {
        if (usuario.getCpf() != null && usuarioDao.buscarPorCpf(usuario.getCpf()) != null) {
            throw new DuplicateRecordException("CPF já foi cadastrado");
        }
    }
}
