package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Categoria;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import br.ufpr.pid.hc.enumeration.CategoriaOrdenacao;
import br.ufpr.pid.hc.service.AbstractService;
import br.ufpr.pid.hc.service.CategoriaService;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.omnifaces.cdi.ViewScoped;

import java.util.List;
import java.util.UUID;


@Named
@ViewScoped
public class CategoriaBean extends AbstractCrudBean<Categoria, UUID, CategoriaOrdenacao> {
    @Inject
    private CategoriaService categoriaService;

    public CategoriaBean() {
        super(15, CategoriaOrdenacao.NOME);
    }

    @Override
    protected AbstractService<Categoria, UUID> getService() { return categoriaService; }

    @Override
    protected Categoria criarNovaEntidade() { return new Categoria(); }

    public List<Categoria> getCategorias() { return lista; }
    public Categoria getCategoria() { return entidade; }
    public Categoria getCategoriaSelecionada() { return entidadeSelecionada; }
    public void setCategoriaSelecionada(Categoria c) { this.entidadeSelecionada = c; }

    public CategoriaOrdenacao[] getOpcoesOrdenacao() { return CategoriaOrdenacao.values(); }
}
