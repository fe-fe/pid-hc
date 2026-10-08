package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Avaliacao;
import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.enumeration.MaterialOrdenacao;
import br.ufpr.pid.hc.enumeration.ResultadoTecnico;
import br.ufpr.pid.hc.service.AbstractService;
import br.ufpr.pid.hc.service.AvaliacaoService;
import br.ufpr.pid.hc.service.FabricanteService;
import br.ufpr.pid.hc.service.MarcaService;
import br.ufpr.pid.hc.service.MaterialService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Named
@ViewScoped
@Getter
@Setter
public class MaterialBean extends AbstractCrudBean<Material, UUID, MaterialOrdenacao> {

    @Inject
    private MaterialService materialService;

    @Inject
    private FabricanteService fabricanteService;

    @Inject
    private MarcaService marcaService;

    @Inject
    private AvaliacaoService avaliacaoService;

    private List<String> fabricantes;
    private List<String> marcas;
    private List<Material> materiaisAvaliacao;
    private Avaliacao avaliacao;
    private String materialAvaliacaoId;
    private String fabricanteAvaliacao;
    private String marcaAvaliacao;


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

    @Override
    protected void posInit() {
        carregarMateriaisAvaliacao();
        carregarNomesAvaliacao();
        novaAvaliacao();
    }

    private void carregarNomesAvaliacao() {
        fabricantes = fabricanteService.listarNomes();
        marcas = marcaService.listarNomes();
    }

    private void carregarMateriaisAvaliacao() {
        materiaisAvaliacao = materialService.buscar(0, Integer.MAX_VALUE, MaterialOrdenacao.CODIGO, false);
    }

    @Override
    public void cadastrar() {
        super.cadastrar();
        carregarMateriaisAvaliacao();
    }

    public ResultadoTecnico[] getResultados() { return ResultadoTecnico.values(); }

    public void novaAvaliacao() {
        avaliacao = new Avaliacao();
        avaliacao.setData(LocalDate.now());
        avaliacao.setResultado(ResultadoTecnico.APROVADO);
        materialAvaliacaoId = null;
        fabricanteAvaliacao = null;
        marcaAvaliacao = null;
    }

    public void avaliar() {
        avaliacao.setMaterial(materialService.buscarPorId(UUID.fromString(materialAvaliacaoId)));
        avaliacaoService.registrar(avaliacao, fabricanteAvaliacao, marcaAvaliacao);
        novaAvaliacao();
        carregarNomesAvaliacao();
        carregarPagina();
    }
}
