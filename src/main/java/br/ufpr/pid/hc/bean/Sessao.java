package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Usuario;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Named
@SessionScoped
public class Sessao implements Serializable {

    private Usuario usuarioLogado;

    public boolean isAutenticado() {
        return usuarioLogado != null;
    }

}
