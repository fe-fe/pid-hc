package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Avaliacao;
import br.ufpr.pid.hc.entity.Fabricante;
import br.ufpr.pid.hc.entity.Marca;
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
import java.util.ArrayList;
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

    private String termoMaterial;
    private String codigoPesquisa;
    private String codigoFiltrado;
    private String termoFabricante;
    private List<Fabricante> fabricantesEncontrados = List.of();
    private List<Fabricante> fabricantesSelecionados = new ArrayList<>();
    private List<UUID> fabricantesFiltrados;
    private String termoMarca;
    private List<Marca> marcasEncontradas = List.of();
    private List<Marca> marcasSelecionadas = new ArrayList<>();
    private List<UUID> marcasFiltradas;
    private String materialFiltrado;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalDate dataInicioFiltrada;
    private LocalDate dataFimFiltrada;

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

    public boolean isFiltroAtivo() {
        return codigoFiltrado != null || materialFiltrado != null || fabricantesFiltrados != null || marcasFiltradas != null
                || dataInicioFiltrada != null || dataFimFiltrada != null;
    }

    public void pesquisarPorCodigo() {
        codigoFiltrado = codigoPesquisa == null || codigoPesquisa.isBlank() ? null : codigoPesquisa.trim();
        reordenar();
    }

    public void buscarFabricantes() {
        fabricantesEncontrados = isBuscaFabricanteValida()
                ? avaliacaoService.buscarFabricantesPorNome(termoFabricante.trim())
                : List.of();
    }

    public boolean isBuscaFabricanteValida() {
        return termoFabricante != null && termoFabricante.trim().length() >= 3;
    }

    public List<Fabricante> getSugestoesFabricantesFiltro() {
        return fabricantesEncontrados.stream()
                .filter(opcao -> fabricantesSelecionados.stream()
                        .noneMatch(selecionado -> selecionado.getId().equals(opcao.getId())))
                .toList();
    }

    public void adicionarFabricante(Fabricante fabricanteSelecionado) {
        if (fabricanteSelecionado != null && fabricantesSelecionados.stream()
                .noneMatch(selecionado -> selecionado.getId().equals(fabricanteSelecionado.getId()))) {
            fabricantesSelecionados.add(fabricanteSelecionado);
        }
    }

    public void removerFabricante(Fabricante fabricanteSelecionado) {
        fabricantesSelecionados.removeIf(selecionado -> selecionado.getId().equals(fabricanteSelecionado.getId()));
    }

    public void buscarMarcas() {
        marcasEncontradas = isBuscaMarcaValida()
                ? avaliacaoService.buscarMarcasPorNome(termoMarca.trim())
                : List.of();
    }

    public boolean isBuscaMarcaValida() {
        return termoMarca != null && termoMarca.trim().length() >= 3;
    }

    public List<Marca> getSugestoesMarcasFiltro() {
        return marcasEncontradas.stream()
                .filter(opcao -> marcasSelecionadas.stream()
                        .noneMatch(selecionada -> selecionada.getId().equals(opcao.getId())))
                .toList();
    }

    public void adicionarMarca(Marca marcaSelecionada) {
        if (marcaSelecionada != null && marcasSelecionadas.stream()
                .noneMatch(selecionada -> selecionada.getId().equals(marcaSelecionada.getId()))) {
            marcasSelecionadas.add(marcaSelecionada);
        }
    }

    public void removerMarca(Marca marcaSelecionada) {
        marcasSelecionadas.removeIf(selecionada -> selecionada.getId().equals(marcaSelecionada.getId()));
    }

    public void aplicarFiltros() {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            FacesContext.getCurrentInstance().addMessage("formFiltrosAvaliacao:dataFim",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "A data final deve ser igual ou posterior à inicial", null));
            return;
        }
        dataInicioFiltrada = dataInicio;
        dataFimFiltrada = dataFim;
        materialFiltrado = termoMaterial == null || termoMaterial.isBlank() ? null : termoMaterial;
        fabricantesFiltrados = fabricantesSelecionados.isEmpty()
                ? null
                : fabricantesSelecionados.stream().map(Fabricante::getId).toList();
        marcasFiltradas = marcasSelecionadas.isEmpty()
                ? null
                : marcasSelecionadas.stream().map(Marca::getId).toList();
        reordenar();
    }

    public void limparFiltros() {
        codigoPesquisa = null;
        codigoFiltrado = null;
        dataInicio = null;
        dataFim = null;
        dataInicioFiltrada = null;
        dataFimFiltrada = null;
        termoMaterial = null;
        materialFiltrado = null;
        termoFabricante = null;
        fabricantesEncontrados = List.of();
        fabricantesSelecionados = new ArrayList<>();
        fabricantesFiltrados = null;
        termoMarca = null;
        marcasEncontradas = List.of();
        marcasSelecionadas = new ArrayList<>();
        marcasFiltradas = null;
        reordenar();
    }

    @Override
    protected long contarRegistros() {
        return isFiltroAtivo()
                ? avaliacaoService.contarComFiltros(materialFiltrado, fabricantesFiltrados,
                        dataInicioFiltrada, dataFimFiltrada, codigoFiltrado, marcasFiltradas)
                : super.contarRegistros();
    }

    @Override
    protected List<Avaliacao> buscarRegistros() {
        return isFiltroAtivo()
                ? avaliacaoService.buscarComFiltros(paginaAtual, tamanhoPagina, ordenacaoAtual, materialFiltrado,
                        fabricantesFiltrados, dataInicioFiltrada, dataFimFiltrada, codigoFiltrado, marcasFiltradas)
                : super.buscarRegistros();
    }

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
