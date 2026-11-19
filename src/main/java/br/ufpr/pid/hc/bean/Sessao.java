package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.Perfil;
import br.ufpr.pid.hc.service.UsuarioService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;

@Named
@SessionScoped
public class Sessao implements Serializable {

    @Inject
    private UsuarioService usuarioService;

    private Usuario usuarioLogado;

    public Usuario getUsuarioLogado() {
        Principal principal = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
        if (principal == null) {
            usuarioLogado = null;
        } else if (usuarioLogado == null || !principal.getName().equals(usuarioLogado.getEmail())) {
            usuarioLogado = usuarioService.buscarPorEmail(principal.getName());
        }
        return usuarioLogado;
    }

    public boolean isAutenticado() {
        return getUsuarioLogado() != null;
    }

    public boolean contemPerfil(Perfil ... perfis) {
        return isAutenticado() && Set.of(perfis).contains(usuarioLogado.getPerfil());
    }

    public String getPaginaInicial() {
        if (!isAutenticado()) {
            return "/pages/auth/login";
        }

        return switch (usuarioLogado.getPerfil()) {
            case ADMINISTRADOR -> "/pages/admin/dashboard";
            case ANALISTA -> "/pages/analista/dashboard";
        };
    }
}
