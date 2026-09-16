package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Categoria;
import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.enumeration.CategoriaOrdenacao;
import br.ufpr.pid.hc.enumeration.MaterialOrdenacao;
import br.ufpr.pid.hc.service.AbstractService;
import br.ufpr.pid.hc.service.CategoriaService;
import br.ufpr.pid.hc.service.MaterialService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Named
@ViewScoped
@Getter
@Setter
public class MaterialBean extends AbstractCrudBean<Material, UUID, MaterialOrdenacao> {

    @Inject
    private MaterialService materialService;

    @Inject
    private CategoriaService categoriaService;


    public MaterialBean() {
        super(15, MaterialOrdenacao.CODIGO);
    }

    @Override
    protected AbstractService<Material, UUID> getService() { return materialService; }

    @Override
    protected Material criarNovaEntidade() { return new Material(); }

    public MaterialOrdenacao[] getOpcoesOrdenacao() { return MaterialOrdenacao.values(); }

    public List<Material> getMateriais() { return getLista(); }
    public Material getMaterial() { return getEntidade(); }
    public Material getMaterialSelecionado() { return getEntidadeSelecionada(); }
    public void setMaterialSelecionado(Material material) { setEntidadeSelecionada(material); }

    private List<Categoria> categorias;
    private String categoriaSelecionadaAtualizarId;
    private String categoriaSelecionadaCadastrarId;

    @Override
    protected void posInit() {
        categorias = categoriaService.buscar(0, Integer.MAX_VALUE, CategoriaOrdenacao.NOME);
    }

    @Override
    public void cadastrar() {
        Categoria categoria;
        if (categoriaSelecionadaCadastrarId == null) {
            categoria = null;
        } else {
            categoria = categoriaService.buscarPorId(UUID.fromString(categoriaSelecionadaCadastrarId));
        }
        Material material = getMaterial();
        material.setCategoria(categoria);
        super.cadastrar();
    }

    @Override
    public void atualizar() {
        Material material = getMaterialSelecionado();
        UUID categoriaAtualId = material.getCategoria() != null
                ? material.getCategoria().getId()
                : null;

        UUID novaCategoriaId = categoriaSelecionadaAtualizarId != null
                ? UUID.fromString(categoriaSelecionadaAtualizarId)
                : null;

        boolean categoriaMudou = !Objects.equals(categoriaAtualId, novaCategoriaId);
        if (categoriaMudou) {
            Categoria novaCategoria = categoriaSelecionadaAtualizarId != null
                    ? categoriaService.buscarPorId(novaCategoriaId)
                    : null;
            material.setCategoria(novaCategoria);
        }
        super.atualizar();
    }
}