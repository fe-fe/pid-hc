package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Setor;
import br.ufpr.pid.hc.enumeration.SetorOrdenacao;
import br.ufpr.pid.hc.service.AbstractService;
import br.ufpr.pid.hc.service.SetorService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;
import java.util.UUID;

@Named
@ViewScoped
public class SetorBean extends AbstractCrudBean<Setor, UUID, SetorOrdenacao> {

    @Inject
    private SetorService setorService;

    public SetorBean() {
        super(15, SetorOrdenacao.CODIGO);
    }

    @Override
    protected AbstractService<Setor, UUID> getService() { return setorService; }

    @Override
    protected Setor criarNovaEntidade() { return new Setor(); }

    public SetorOrdenacao[] getOpcoesOrdenacao() { return SetorOrdenacao.values(); }

    public List<Setor> getSetores() { return getLista(); }
    public Setor getSetor() { return getEntidade(); }
    public Setor getSetorSelecionado() { return getEntidadeSelecionada(); }
    public void setSetorSelecionado(Setor setor) { setEntidadeSelecionada(setor); }
}