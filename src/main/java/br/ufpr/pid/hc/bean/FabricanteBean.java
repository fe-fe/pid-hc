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

import java.util.List;
import java.util.UUID;

@Named
@ViewScoped
@Getter
@Setter
public class FabricanteBean extends AbstractCadastroBean<Fabricante, NomeOrdenacao> {

    @Inject
    private FabricanteService fabricanteService;

    private String termoPesquisa;
    private String termoFiltrado;

    public FabricanteBean() {
        super(NomeOrdenacao.NOME);
    }

    public void pesquisar() {
        termoFiltrado = termoPesquisa == null || termoPesquisa.isBlank() ? null : termoPesquisa.trim();
        reordenar();
    }

    @Override
    protected long contarRegistros() {
        return fabricanteService.contarPorTermo(termoFiltrado, mostrarInativos);
    }

    @Override
    protected List<Fabricante> buscarRegistros() {
        return fabricanteService.buscarPorTermo(paginaAtual, tamanhoPagina, ordenacaoAtual,
                termoFiltrado, mostrarInativos);
    }

    @Override
    protected AbstractService<Fabricante, UUID> getService() { return fabricanteService; }

    @Override
    protected Fabricante criarNovaEntidade() { return new Fabricante(); }

    @Override
    public NomeOrdenacao[] getOpcoesOrdenacao() { return NomeOrdenacao.values(); }
}
