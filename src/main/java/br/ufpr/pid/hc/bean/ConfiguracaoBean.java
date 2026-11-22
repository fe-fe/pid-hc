package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.exception.DomainException;
import br.ufpr.pid.hc.service.ParametroService;
import jakarta.annotation.PostConstruct;
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
public class ConfiguracaoBean {

    @Inject
    private ParametroService parametroService;

    private Integer validade;

    @PostConstruct
    public void init() {
        validade = parametroService.validade();
    }

    public int getValidadePadrao() {
        return ParametroService.VALIDADE_PADRAO;
    }

    public int getValidadeMaxima() {
        return ParametroService.VALIDADE_MAXIMA;
    }

    public void salvar() {
        try {
            parametroService.alterarValidade(validade);
            mensagem(FacesMessage.SEVERITY_INFO, "Configurações salvas.");
        } catch (DomainException e) {
            mensagem(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    private void mensagem(FacesMessage.Severity severidade, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidade, texto, null));
    }
}
