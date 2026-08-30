package br.ufpr.pid.hc.config;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.service.UsuarioService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

import java.util.Set;

@ApplicationScoped
public class UserIdentityStore implements IdentityStore {

    @Inject
    private UsuarioService usuarioService;

    @Inject
    private Pbkdf2PasswordHash hashUtil;

    public CredentialValidationResult validate(UsernamePasswordCredential credencial) {
        Usuario usuario = usuarioService.buscarPorEmail(credencial.getCaller());

        if (usuario == null) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        boolean senhaCorreta = hashUtil.verify(
                credencial.getPassword().getValue(), usuario.getSenha());

        if (!senhaCorreta) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        return new CredentialValidationResult(
                usuario.getEmail(),
                Set.of(usuario.getPerfil().name())
        );
    }
}