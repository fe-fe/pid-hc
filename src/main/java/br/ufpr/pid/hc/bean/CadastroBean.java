package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.service.UsuarioService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
            return "login?faces-redirect=true";
        } catch (RuntimeException e) {
            log.warn(e.getMessage());
            return null;
        }
    }


}
