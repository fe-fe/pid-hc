package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.Perfil;
import br.ufpr.pid.hc.exception.DomainException;
import br.ufpr.pid.hc.exception.DuplicateEmailException;
import br.ufpr.pid.hc.exception.WeakPasswordException;
import br.ufpr.pid.hc.service.UsuarioService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Named
@RequestScoped
@Slf4j
@Getter
@Setter
public class CadastroBean {

    @Inject
    private UsuarioService usuarioService;

    private Usuario usuario = new Usuario();

    public String cadastrar() {
        try {
            usuarioService.salvar(usuario);
            log.info("Cadastro realizado");
            return "/pages/auth/login?faces-redirect=true";
        } catch (DomainException e) {
            log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage());

            String componentId = null;
            if (e instanceof DuplicateEmailException) {
                componentId = "authForm:email";
            } else if (e instanceof WeakPasswordException) {
                componentId = "authForm:senha";
            }

            FacesContext.getCurrentInstance().addMessage(componentId,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
            return null;
        } catch (RuntimeException e) {
            log.error("Erro inesperado ao cadastrar usuário", e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Não foi possível realizar o cadastro", null));
            return null;
        }
    }

    public List<Perfil> getPerfis() {
        return List.of(Perfil.values());
    }

}
