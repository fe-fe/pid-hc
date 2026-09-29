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

    // chamado via f:viewAction na tela de login (só em GET); num @PostConstruct o redirect disparava também no logout
    public String redirecionarSeAutenticado() {
        return session.isAutenticado() ? session.getPaginaInicial() + "?faces-redirect=true" : null;
    }

    public String login() {

        Credential credenciais = new UsernamePasswordCredential(email, senha);

        AuthenticationStatus status = securityContext.authenticate(
                (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest(),
                (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse(),
                AuthenticationParameters.withParams().credential(credenciais)
        );

        if (status == AuthenticationStatus.SEND_CONTINUE) {
            session.setUsuarioLogado(usuarioService.buscarPorEmail(email));
            FacesContext.getCurrentInstance().responseComplete();
            return null;
        }

        if (status != AuthenticationStatus.SUCCESS) {
            String mensagem = isUsuarioDesativado()
                    ? "Usuário desativado. Procure um administrador"
                    : "Email ou senha inválidos";
            FacesContext.getCurrentInstance().addMessage("authForm:senha",
            new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, null));
            return null;
        }

        Usuario usuario = usuarioService.buscarPorEmail(email);
        session.setUsuarioLogado(usuario);

        return session.getPaginaInicial() + "?faces-redirect=true";
    }

    // só informa que a conta está desativada se a senha estiver correta, para não expor quais emails existem
    private boolean isUsuarioDesativado() {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        return usuario != null && usuario.isInativo() && hashUtil.verify(senha.toCharArray(), usuario.getSenha());
    }

    public String logout() throws ServletException {
        HttpServletRequest request = (HttpServletRequest)
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getRequest();
        request.logout();
        session.setUsuarioLogado(null);
        return "/pages/auth/login?faces-redirect=true";
    }

}
