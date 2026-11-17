package br.ufpr.pid.hc.bean;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named
@RequestScoped
public class MenuBean {

    private static final String EXTENSAO = ".xhtml";
    private static final String PASTA_GESTAO = "/pages/controle/";
    private static final String PAGINA_USUARIOS = "/pages/admin/users";

    public String ariaCurrent(String pagina) {
        return getPaginaAtual().equals(pagina) ? "page" : null;
    }

    public boolean isGestaoAberta() {
        String atual = getPaginaAtual();
        return atual.startsWith(PASTA_GESTAO) || atual.equals(PAGINA_USUARIOS);
    }

    private String getPaginaAtual() {
        String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        return viewId.endsWith(EXTENSAO) ? viewId.substring(0, viewId.length() - EXTENSAO.length()) : viewId;
    }
}
