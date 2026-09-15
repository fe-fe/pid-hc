package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Categoria;
import br.ufpr.pid.hc.service.CategoriaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import jakarta.security.enterprise.SecurityContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Named
@RequestScoped
@Slf4j
@Getter
@Setter
public class CategoriaBean {

    @Inject
    private CategoriaService categoriaService;

    @Inject
    private SecurityContext securityContext;
    @Inject
    private Sessao session;

    private Categoria categoria = new Categoria();

    public List<Categoria> getCategorias() {
        return categoriaService.buscar();
    }

    public String cadastrar() {
        Categoria novaCategoria = categoriaService.salvar(categoria);
        return "/pages/categoria?faces-redirect=true";
    }

}
