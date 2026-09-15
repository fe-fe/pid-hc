package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Categoria;
import br.ufpr.pid.hc.service.CategoriaService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import jakarta.security.enterprise.SecurityContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.omnifaces.cdi.ViewScoped;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
@Slf4j
@Getter
@Setter
public class CategoriaBean implements Serializable {

    @Inject
    private CategoriaService categoriaService;
    @Inject
    private SecurityContext securityContext;
    @Inject
    private Sessao session;

    private Categoria categoria = new Categoria();
    private Categoria categoriaSelecionada;
    private List<Categoria> categorias;

    private int paginaAtual = 0;
    private int tamanhoPagina = 15;
    private long totalRegistros;

    private void carregarPagina() {
        categorias = categoriaService.buscar(paginaAtual, tamanhoPagina);
    }

    @PostConstruct
    public void init() {
        totalRegistros = categoriaService.contarTotal();
        carregarPagina();
    }

    public void cadastrar() {
        Categoria novaCategoria = categoriaService.salvar(categoria);
        categoria = new Categoria();
        totalRegistros = categoriaService.contarTotal();
        paginaAtual = 0;
        carregarPagina();
    }

    public void atualizar() {
        categoriaService.salvar(categoriaSelecionada);
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

}
