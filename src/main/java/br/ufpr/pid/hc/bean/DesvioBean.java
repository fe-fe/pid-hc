package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Desvio;
import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.enumeration.Desfecho;
import br.ufpr.pid.hc.enumeration.DesvioOrdenacao;
import br.ufpr.pid.hc.enumeration.EnvioAmostra;
import br.ufpr.pid.hc.enumeration.GrauDano;
import br.ufpr.pid.hc.enumeration.MaterialOrdenacao;
import br.ufpr.pid.hc.exception.DomainException;
import br.ufpr.pid.hc.service.AbstractService;
import br.ufpr.pid.hc.service.DesvioService;
import br.ufpr.pid.hc.service.FabricanteService;
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
public class DesvioBean extends AbstractCrudBean<Desvio, UUID, DesvioOrdenacao> {

    @Inject
    private DesvioService desvioService;

    @Inject
    private MaterialService materialService;

    @Inject
    private FabricanteService fabricanteService;

    @Inject
    private MarcaService marcaService;

    private Desvio emEdicao;
    private String codigoMaterial;
    private String nomeMaterial;
    private boolean materialExistente;
    private String fabricante;
    private String marca;
    private List<Material> materiais = List.of();
    private List<String> fabricantes = List.of();
    private List<String> marcas = List.of();
    private String termoPesquisa;
    private String termoFiltrado;

    public DesvioBean() {
        super(15, DesvioOrdenacao.DATA);
    }

    @Override
    protected AbstractService<Desvio, UUID> getService() { return desvioService; }

    public List<Desvio> getDesvios() { return getLista(); }

    public void pesquisar() {
        termoFiltrado = termoPesquisa == null || termoPesquisa.isBlank() ? null : termoPesquisa.trim();
        paginaAtual = 0;
        carregarPagina();
    }

    @Override
    protected long contarRegistros() {
        return desvioService.contarPorTermo(termoFiltrado, mostrarInativos);
    }

    @Override
    protected List<Desvio> buscarRegistros() {
        return desvioService.buscarPorTermo(paginaAtual, tamanhoPagina, ordenacaoAtual,
                termoFiltrado, mostrarInativos);
    }

    public Desfecho[] getDesfechos() { return Desfecho.values(); }

    public GrauDano[] getGrausDano() { return GrauDano.values(); }

    public EnvioAmostra[] getOpcoesAmostra() { return EnvioAmostra.values(); }

    public LocalDate getDataMaxima() { return LocalDate.now(); }

    public void editar(Desvio selecionado) {
        // Edite uma cópia para que cancelar não altere os dados da tabela.
        Desvio copia = new Desvio();
        copia.setId(selecionado.getId());
        copia.setMaterial(selecionado.getMaterial());
        copia.setFabricante(selecionado.getFabricante());
        copia.setMarca(selecionado.getMarca());
        copia.setData(selecionado.getData());
        copia.setVigihosp(selecionado.getVigihosp());
        copia.setRegistroAnvisa(selecionado.getRegistroAnvisa());
        copia.setLote(selecionado.getLote());
        copia.setValidade(selecionado.getValidade());
        copia.setFormaEntrada(selecionado.getFormaEntrada());
        copia.setAmostra(selecionado.getAmostra());
        copia.setServico(selecionado.getServico());
        copia.setUnidade(selecionado.getUnidade());
        copia.setMotivo(selecionado.getMotivo());
        copia.setProvidencias(selecionado.getProvidencias());
        copia.setTecnicoUsep(selecionado.getTecnicoUsep());
        copia.setDesfecho(selecionado.getDesfecho());
        copia.setGrauDano(selecionado.getGrauDano());
        copia.setNotificacaoAnvisa(selecionado.getNotificacaoAnvisa());
        copia.setProcessoSei(selecionado.getProcessoSei());
        emEdicao = copia;
        codigoMaterial = selecionado.getMaterial().getCodigo();
        nomeMaterial = selecionado.getMaterial().getNome();
        materialExistente = true;
        fabricante = selecionado.getFabricante().getNome();
        marca = selecionado.getMarca() == null ? null : selecionado.getMarca().getNome();
        materiais = materialService.buscar(0, Integer.MAX_VALUE, MaterialOrdenacao.CODIGO, false);
        fabricantes = fabricanteService.listarNomes();
        marcas = marcaService.listarNomes();
        Bootstrap.abrirModal("modalTecnovigilancia");
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

    public void salvar() {
        try {
            desvioService.atualizar(emEdicao, codigoMaterial, nomeMaterial, fabricante, marca);
        } catch (DomainException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
            return;
        }
        carregarPagina();
        Bootstrap.fecharModal("modalTecnovigilancia");
    }
}
