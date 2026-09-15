package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Auditavel;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import br.ufpr.pid.hc.service.AbstractService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public abstract class AbstractCrudBean<T extends Auditavel, ID> implements Serializable {

    protected T entidade;
    protected T entidadeSelecionada;
    protected List<T> lista;

    protected int paginaAtual = 0;
    protected final int tamanhoPagina;
    protected long totalRegistros;
    protected CampoOrdenacao ordenacaoAtual;

    protected AbstractCrudBean(int tamanhoPagina, CampoOrdenacao ordenacaoPadrao) {
        this.tamanhoPagina = tamanhoPagina;
        this.ordenacaoAtual = ordenacaoPadrao;
    }

    protected abstract AbstractService<T, ID> getService();
    protected abstract T criarNovaEntidade();

    protected void carregarPagina() {
        lista = getService().buscar(paginaAtual, tamanhoPagina, ordenacaoAtual);
    }

    @PostConstruct
    public void init() {
        entidade = criarNovaEntidade();
        totalRegistros = getService().contarTotal();
        carregarPagina();
    }

    public void cadastrar() {
        getService().salvar(entidade);
        entidade = criarNovaEntidade();
        totalRegistros = getService().contarTotal();
        paginaAtual = 0;
        carregarPagina();
    }

    public void atualizar() {
        getService().salvar(entidadeSelecionada);
        carregarPagina();
    }

    public void proximaPagina() {
        if (!isUltimaPagina()) {
            paginaAtual++;
            carregarPagina();
        }
    }

    public void paginaAnterior() {
        if (paginaAtual > 0) {
            paginaAtual--;
            carregarPagina();
        }
    }

    public boolean isUltimaPagina() {
        return (long) (paginaAtual + 1) * tamanhoPagina >= totalRegistros;
    }

    public void reordenar() {
        paginaAtual = 0;
        carregarPagina();
    }
}