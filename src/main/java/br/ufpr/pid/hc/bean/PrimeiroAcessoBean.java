package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.exception.DomainException;
import br.ufpr.pid.hc.exception.DuplicateEmailException;
import br.ufpr.pid.hc.exception.WeakPasswordException;
import br.ufpr.pid.hc.service.PrimeiroAcessoService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@RequestScoped
@Getter
@Setter
public class PrimeiroAcessoBean {

    private static final String LOGIN = "/pages/auth/login?faces-redirect=true";

    @Inject
    private PrimeiroAcessoService primeiroAcessoService;

    private Usuario usuario = new Usuario();
    private String codigo;

    public boolean isPendente() {
        return primeiroAcessoService.isPendente();
    }

    public String redirecionarSeConcluido() {
        return isPendente() ? null : LOGIN;
    }

    public String criar() {
        try {
            primeiroAcessoService.criarAdministrador(usuario, codigo);
        } catch (DomainException e) {
            String componente = switch (e) {
                case DuplicateEmailException ignorada -> "authForm:email";
                case WeakPasswordException ignorada -> "authForm:senha";
                default -> null;
            };
            FacesContext.getCurrentInstance().addMessage(componente,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
            return null;
        }
        FacesContext contexto = FacesContext.getCurrentInstance();
        contexto.getExternalContext().getFlash().setKeepMessages(true);
        contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Administrador criado. Entre com o email e a senha cadastrados.", null));
        return LOGIN;
    }
}
