package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Avaliacao;
import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.entity.ProdutoAnvisa;
import br.ufpr.pid.hc.enumeration.AvaliacaoOrdenacao;
import br.ufpr.pid.hc.enumeration.MaterialOrdenacao;
import br.ufpr.pid.hc.enumeration.ResultadoTecnico;
import br.ufpr.pid.hc.exception.DomainException;
import br.ufpr.pid.hc.service.AbstractService;
import br.ufpr.pid.hc.service.AvaliacaoService;
import br.ufpr.pid.hc.service.FabricanteService;
import br.ufpr.pid.hc.service.ImportacaoAnvisaService;
import br.ufpr.pid.hc.service.MarcaService;
import br.ufpr.pid.hc.service.MaterialService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
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
public class AvaliacaoBean extends AbstractCrudBean<Avaliacao, UUID, AvaliacaoOrdenacao> {

    @Inject
    private AvaliacaoService avaliacaoService;

    @Inject
    private MaterialService materialService;

    @Inject
    private FabricanteService fabricanteService;

    @Inject
    private MarcaService marcaService;

    @Inject
    private ImportacaoAnvisaService importacaoAnvisaService;

    private Avaliacao avaliacao;
    private String codigoMaterial;
    private String nomeMaterial;
    private boolean materialExistente;
    private String fabricante;
    private String marca;

    private List<Material> materiais;
    private List<String> fabricantes;
    private List<String> marcas;

    private Boolean registroAnvisaEncontrado;
    private List<String> fabricantesAnvisa = List.of();
    private List<String> marcasAnvisa = List.of();

    public AvaliacaoBean() {
        super(15, AvaliacaoOrdenacao.MATERIAL);
    }

    @Override
    protected AbstractService<Avaliacao, UUID> getService() { return avaliacaoService; }

    @Override
    protected Avaliacao criarNovaEntidade() { return new Avaliacao(); }

    @Override
    protected void posInit() {
        carregarSugestoes();
        novaAvaliacao();
    }

    public AvaliacaoOrdenacao[] getOpcoesOrdenacao() { return AvaliacaoOrdenacao.values(); }

    public List<Avaliacao> getAvaliacoes() { return getLista(); }

    private void carregarSugestoes() {
        materiais = materialService.buscar(0, Integer.MAX_VALUE, MaterialOrdenacao.CODIGO, false);
        fabricantes = fabricanteService.listarNomes();
        marcas = marcaService.listarNomes();
    }

    public void novaAvaliacao() {
        avaliacao = new Avaliacao();
        avaliacao.setData(LocalDate.now());
        avaliacao.setResultado(ResultadoTecnico.APROVADO);
        codigoMaterial = null;
        nomeMaterial = null;
        materialExistente = false;
        fabricante = null;
        marca = null;
        registroAnvisaEncontrado = null;
        fabricantesAnvisa = List.of();
        marcasAnvisa = List.of();
    }

    public void buscarRegistroAnvisa() {
        String registro = avaliacao.getRegistroAnvisa() == null ? "" : avaliacao.getRegistroAnvisa().replaceAll("\\D", "");
        List<ProdutoAnvisa> produtos = registro.isEmpty() ? List.of() : importacaoAnvisaService.buscarPorRegistro(registro);
        registroAnvisaEncontrado = registro.isEmpty() ? null : !produtos.isEmpty();
        fabricantesAnvisa = produtos.stream()
                .map(ProdutoAnvisa::getNomeFabricante)
                .filter(nome -> nome != null && !nome.isBlank())
                .distinct()
                .toList();
        marcasAnvisa = produtos.stream()
                .map(ProdutoAnvisa::getNomeComercial)
                .filter(nome -> nome != null && !nome.isBlank())
                .distinct()
                .toList();
    }

    public List<String> getSugestoesFabricante() {
        return fabricantesAnvisa.isEmpty() ? fabricantes : fabricantesAnvisa;
    }

    public List<String> getSugestoesMarca() {
        return marcasAnvisa.isEmpty() ? marcas : marcasAnvisa;
    }

    public void buscarMaterial() {
        materialService.buscarPorCodigo(codigoMaterial).ifPresentOrElse(material -> {
            nomeMaterial = material.getNome();
            materialExistente = true;
        }, () -> {
            if (materialExistente) {
                nomeMaterial = null;
            }
            materialExistente = false;
        });
    }

    public ResultadoTecnico[] getResultados() { return ResultadoTecnico.values(); }

    public void salvar() {
        try {
            avaliacaoService.registrar(avaliacao, codigoMaterial, nomeMaterial, fabricante, marca);
            novaAvaliacao();
            carregarSugestoes();
            reordenar();
        } catch (DomainException e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null)
            );
        }
    }
}
