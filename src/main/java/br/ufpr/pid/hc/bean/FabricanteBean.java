package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Fabricante;
import br.ufpr.pid.hc.enumeration.NomeOrdenacao;
import br.ufpr.pid.hc.service.AbstractService;
import br.ufpr.pid.hc.service.FabricanteService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Named
@ViewScoped
@Getter
@Setter
public class FabricanteBean extends AbstractCadastroBean<Fabricante, NomeOrdenacao> {

    @Inject
    private FabricanteService fabricanteService;

    public FabricanteBean() {
        super(NomeOrdenacao.NOME);
    }

    @Override
    protected AbstractService<Fabricante, UUID> getService() { return fabricanteService; }

    @Override
    protected Fabricante criarNovaEntidade() { return new Fabricante(); }

    @Override
    public NomeOrdenacao[] getOpcoesOrdenacao() { return NomeOrdenacao.values(); }
}
