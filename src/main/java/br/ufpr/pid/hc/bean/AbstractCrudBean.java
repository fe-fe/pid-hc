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
public abstract class AbstractCrudBean<T extends Auditavel, ID, E extends Enum<E> & CampoOrdenacao> implements Serializable, Paginavel {

    protected List<T> lista;

    protected int paginaAtual = 0;
    protected final int tamanhoPagina;
    protected long totalRegistros;
    protected E ordenacaoAtual;
    protected boolean mostrarInativos = false;

    private final Class<E> tipoOrdenacao;

    protected AbstractCrudBean(int tamanhoPagina, E ordenacaoPadrao) {
        this.tamanhoPagina = tamanhoPagina;
        this.ordenacaoAtual = ordenacaoPadrao;
        this.tipoOrdenacao = ordenacaoPadrao.getDeclaringClass();
    }

    protected abstract AbstractService<T, ID> getService();

    protected long contarRegistros() {
        return getService().contarTotal(mostrarInativos);
    }

    protected List<T> buscarRegistros() {
        return getService().buscar(paginaAtual, tamanhoPagina, ordenacaoAtual, mostrarInativos);
    }

    protected void carregarPagina() {
        totalRegistros = contarRegistros();
        lista = buscarRegistros();

        if (lista.isEmpty() && paginaAtual > 0) {
            paginaAtual--;
            carregarPagina();
        }
    }

    @Override
    public void irParaPagina(int pagina) {
        paginaAtual = Math.max(0, Math.min(pagina, getTotalPaginas() - 1));
        carregarPagina();
    }

    public void reordenar() {
        paginaAtual = 0;
        carregarPagina();
    }

    public void alternarInativos() {
        paginaAtual = 0;
        carregarPagina();
    }

    @PostConstruct
    public void init() {
        carregarPagina();
        posInit();
    }

    protected void posInit() {}
}