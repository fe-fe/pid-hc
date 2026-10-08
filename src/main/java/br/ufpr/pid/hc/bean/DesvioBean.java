package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Desvio;
import br.ufpr.pid.hc.enumeration.DesvioOrdenacao;
import br.ufpr.pid.hc.service.AbstractService;
import br.ufpr.pid.hc.service.DesvioService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Named
@ViewScoped
@Getter
@Setter
public class DesvioBean extends AbstractCrudBean<Desvio, UUID, DesvioOrdenacao> {

    @Inject
    private DesvioService desvioService;

    public DesvioBean() {
        super(15, DesvioOrdenacao.DATA);
    }

    @Override
    protected AbstractService<Desvio, UUID> getService() { return desvioService; }

    @Override
    protected Desvio criarNovaEntidade() { return new Desvio(); }

    public List<Desvio> getDesvios() { return getLista(); }
}
