package br.ufpr.pid.hc.bean;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletResponse;

@Named
@RequestScoped
public class ErroBean {

    private int status;

    @PostConstruct
    void iniciar() {
        ExternalContext contexto = FacesContext.getCurrentInstance().getExternalContext();
        if (contexto.getRequestMap().get(RequestDispatcher.ERROR_STATUS_CODE) instanceof Integer codigo) {
            status = codigo;
        } else {
            status = HttpServletResponse.SC_NOT_FOUND;
            contexto.setResponseStatus(status);
        }
    }

    public boolean isAcessoNegado() {
        return status == HttpServletResponse.SC_FORBIDDEN;
    }

    public boolean isNaoEncontrada() {
        return status == HttpServletResponse.SC_NOT_FOUND;
    }

    public String getTitulo() {
        if (isAcessoNegado()) {
            return "Acesso negado";
        }
        return isNaoEncontrada() ? "Página não encontrada" : "Algo deu errado";
    }

    public String getMensagem() {
        if (isAcessoNegado()) {
            return "Seu perfil não tem permissão para abrir esta página.";
        }
        return isNaoEncontrada()
                ? "O endereço está errado ou a página não existe mais."
                : "Não foi possível concluir a operação. Tente de novo e, se o erro continuar, avise um administrador.";
    }
}
