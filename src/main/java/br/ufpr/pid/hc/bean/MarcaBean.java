package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Marca;
import br.ufpr.pid.hc.enumeration.NomeOrdenacao;
import br.ufpr.pid.hc.service.AbstractService;
import br.ufpr.pid.hc.service.MarcaService;
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
public class MarcaBean extends AbstractCadastroBean<Marca, NomeOrdenacao> {

    @Inject
    private MarcaService marcaService;

    public MarcaBean() {
        super(NomeOrdenacao.NOME);
    }

    @Override
    protected AbstractService<Marca, UUID> getService() { return marcaService; }

    @Override
    protected Marca criarNovaEntidade() { return new Marca(); }

    @Override
    public NomeOrdenacao[] getOpcoesOrdenacao() { return NomeOrdenacao.values(); }
}
