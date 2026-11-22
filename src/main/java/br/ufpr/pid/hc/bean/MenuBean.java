package br.ufpr.pid.hc.bean;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.util.List;

@Named
@RequestScoped
public class MenuBean {

    private static final String EXTENSAO = ".xhtml";
    private static final String PASTA_GESTAO = "/pages/controle/";
    private static final List<String> PAGINAS_GESTAO_ADMIN = List.of("/pages/admin/users", "/pages/admin/auditoria",
            "/pages/admin/configuracoes");

    public String ariaCurrent(String pagina) {
        return getPaginaAtual().equals(pagina) ? "page" : null;
    }

    public boolean isGestaoAberta() {
        String atual = getPaginaAtual();
        return atual.startsWith(PASTA_GESTAO) || PAGINAS_GESTAO_ADMIN.contains(atual);
    }

    private String getPaginaAtual() {
        String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        return viewId.endsWith(EXTENSAO) ? viewId.substring(0, viewId.length() - EXTENSAO.length()) : viewId;
    }
}
