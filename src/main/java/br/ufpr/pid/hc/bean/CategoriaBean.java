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

    @PostConstruct
    public void init() {
        categorias = categoriaService.buscar();
    }

    public void cadastrar() {
        Categoria novaCategoria = categoriaService.salvar(categoria);
        categorias = categoriaService.buscar();
    }

    public void atualizar() {
        categoriaService.salvar(categoriaSelecionada);
        categorias = categoriaService.buscar();
    }

}
