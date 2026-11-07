package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.enumeration.MaterialOrdenacao;
import br.ufpr.pid.hc.service.AbstractService;
import br.ufpr.pid.hc.service.MaterialService;
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
public class MaterialBean extends AbstractCadastroBean<Material, MaterialOrdenacao> {

    @Inject
    private MaterialService materialService;

    private String termoPesquisa;
    private String termoFiltrado;

    public MaterialBean() {
        super(MaterialOrdenacao.CODIGO);
    }

    public void pesquisar() {
        termoFiltrado = termoPesquisa == null || termoPesquisa.isBlank() ? null : termoPesquisa.trim();
        reordenar();
    }

    @Override
    protected long contarRegistros() {
        return materialService.contarPorTermo(termoFiltrado, true);
    }

    @Override
    protected List<Material> buscarRegistros() {
        return materialService.buscarPorTermo(paginaAtual, tamanhoPagina, ordenacaoAtual,
                termoFiltrado, true);
    }

    @Override
    protected AbstractService<Material, UUID> getService() { return materialService; }

    @Override
    protected Material criarNovaEntidade() { return new Material(); }

    @Override
    public MaterialOrdenacao[] getOpcoesOrdenacao() { return MaterialOrdenacao.values(); }
}
