package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.service.UsuarioService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Named
@RequestScoped
@Getter
@Setter
@Slf4j
public class LoginBean {

    private String email;
    private String senha;

    @Inject
    private Sessao session;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private Pbkdf2PasswordHash hashUtil;

    @Inject
    private UsuarioService usuarioService;

    public String login() {

        if (session.isAutenticado()) {
            return "greet?faces-redirect=true";
        }

        Credential credenciais = new UsernamePasswordCredential(email, senha);

        AuthenticationStatus status = securityContext.authenticate(
                (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest(),
                (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse(),
                AuthenticationParameters.withParams().credential(credenciais)
        );

        if (status == AuthenticationStatus.SUCCESS) {
            Usuario usuario = usuarioService.buscarPorEmail(email);
            session.setUsuarioLogado(usuario);
            return "greet?faces-redirect=true";
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email ou senha inválidos", null));
        return null;
    }

    public String logout() throws ServletException {
        HttpServletRequest request = (HttpServletRequest)
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getRequest();
        request.logout();
        session.setUsuarioLogado(null);
        return "/login?faces-redirect=true";
    }

}
