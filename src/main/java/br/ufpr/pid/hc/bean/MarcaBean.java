package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Marca;
import br.ufpr.pid.hc.enumeration.MarcaOrdenacao;
import br.ufpr.pid.hc.service.AbstractService;
import br.ufpr.pid.hc.service.MarcaService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.omnifaces.cdi.ViewScoped;

import java.util.List;
import java.util.UUID;

@Named
@ViewScoped
public class MarcaBean extends AbstractCrudBean<Marca, UUID, MarcaOrdenacao> {
    @Inject
    private MarcaService marcaService;

    public MarcaBean() {
        super(15, MarcaOrdenacao.NOME);
    }

    @Override
    protected AbstractService<Marca, UUID> getService() { return marcaService; }

    @Override
    protected Marca criarNovaEntidade() { return new Marca(); }

    public List<Marca> getMarcas() { return lista; }
    public Marca getMarca() { return entidade; }
    public Marca getMarcaSelecionada() { return entidadeSelecionada; }
    public void setMarcaSelecionada(Marca m) { setEntidadeSelecionada(m); }

    public MarcaOrdenacao[] getOpcoesOrdenacao() { return MarcaOrdenacao.values(); }
}
