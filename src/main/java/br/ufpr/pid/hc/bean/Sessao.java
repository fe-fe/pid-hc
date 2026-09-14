package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.Perfil;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@Named
@SessionScoped
public class Sessao implements Serializable {

    private Usuario usuarioLogado;

    public boolean isAutenticado() {
        return usuarioLogado != null;
    }

    public boolean contemPerfil(Perfil ... perfis) {
        return isAutenticado() && Set.of(perfis).contains(usuarioLogado.getPerfil());
    }
}
