package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.dto.ConsultaMaterialFiltro;
import br.ufpr.pid.hc.dto.ResumoMaterial;
import br.ufpr.pid.hc.entity.Avaliacao;
import br.ufpr.pid.hc.entity.Desvio;
import br.ufpr.pid.hc.entity.Fabricante;
import br.ufpr.pid.hc.entity.Marca;
import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.enumeration.MaterialOrdenacao;
import br.ufpr.pid.hc.service.DashboardMaterialService;
import br.ufpr.pid.hc.service.MaterialService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Named
@ViewScoped
@Getter
@Setter
public class DashboardMaterialBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int TAMANHO_PAGINA = 10;
    private static final int MAX_SUGESTOES = 10;

    @Inject
    private MaterialService materialService;
    @Inject
    private DashboardMaterialService dashboardMaterialService;

    private String termo;
    private boolean buscaRealizada;
    private boolean maisSugestoes;
    private List<Material> sugestoes = List.of();
    private Material material;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String fabricanteId;
    private String marcaId;
    private List<Fabricante> fabricantes = List.of();
    private List<Marca> marcas = List.of();
    private ConsultaMaterialFiltro filtroAplicado;
    private ResumoMaterial resumo = ResumoMaterial.VAZIO;
    private List<Avaliacao> avaliacoes = List.of();
    private List<Desvio> notificacoes = List.of();
    private boolean abaAvaliacoes = true;
    private int pagina;

    public void buscarSugestoes() {
        buscaRealizada = termo != null && !termo.isBlank();
        List<Material> encontrados = buscaRealizada
                ? materialService.buscarPorTermo(0, MAX_SUGESTOES + 1, MaterialOrdenacao.CODIGO, termo, true)
                : List.of();
        maisSugestoes = encontrados.size() > MAX_SUGESTOES;
        sugestoes = encontrados.stream().limit(MAX_SUGESTOES).toList();
    }

    public void pesquisar() {
        if (termo == null || termo.isBlank()) {
            buscarSugestoes();
            return;
        }
        Material codigoExato = materialService.buscarPorCodigo(termo).orElse(null);
        if (codigoExato != null) {
            selecionar(codigoExato);
            return;
        }
        buscarSugestoes();
        if (sugestoes.size() == 1) selecionar(sugestoes.getFirst());
    }

    public void selecionar(Material selecionado) {
        material = materialService.buscarPorId(selecionado.getId());
        if (material == null) {
            buscarSugestoes();
            return;
        }
        termo = material.getCodigo();
        sugestoes = List.of();
        buscaRealizada = false;
        maisSugestoes = false;
        fabricantes = dashboardMaterialService.buscarFabricantes(material.getId());
        marcas = dashboardMaterialService.buscarMarcas(material.getId());
        abaAvaliacoes = true;
        limparFiltros();
    }

    public void aplicarFiltros() {
        if (material == null) return;
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("formFiltrosMaterial:dataFim", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "A data final deve ser igual ou posterior à data inicial.", null));
            context.validationFailed();
            return;
        }
        filtroAplicado = new ConsultaMaterialFiltro(material.getId(), dataInicio, dataFim,
                identificador(fabricanteId), identificador(marcaId));
        resumo = dashboardMaterialService.resumir(filtroAplicado);
        pagina = 0;
        carregarPagina();
    }

    public void limparFiltros() {
        dataInicio = null;
        dataFim = null;
        fabricanteId = null;
        marcaId = null;
        aplicarFiltros();
    }

    public void mostrarAvaliacoes() {
        abaAvaliacoes = true;
        pagina = 0;
        carregarPagina();
    }

    public void mostrarTecnovigilancia() {
        abaAvaliacoes = false;
        pagina = 0;
        carregarPagina();
    }

    public void paginaAnterior() {
        if (pagina > 0) {
            pagina--;
            carregarPagina();
        }
    }

    public void proximaPagina() {
        if (isTemProximaPagina()) {
            pagina++;
            carregarPagina();
        }
    }

    private void carregarPagina() {
        if (filtroAplicado == null) return;
        avaliacoes = abaAvaliacoes
                ? dashboardMaterialService.buscarAvaliacoes(filtroAplicado, pagina, TAMANHO_PAGINA) : List.of();
        notificacoes = abaAvaliacoes ? List.of()
                : dashboardMaterialService.buscarNotificacoes(filtroAplicado, pagina, TAMANHO_PAGINA);
    }

    private UUID identificador(String valor) {
        return valor == null || valor.isBlank() ? null : UUID.fromString(valor);
    }

    public long getTotalRegistros() {
        return abaAvaliacoes ? resumo.avaliacoes() : resumo.notificacoes();
    }

    public int getPaginaAtual() {
        return pagina;
    }

    public boolean isUltimaPagina() {
        return !isTemProximaPagina();
    }

    public long getEscalaGrafico() {
        return Math.max(resumo.avaliacoes(), resumo.notificacoes());
    }

    public String getLarguraAprovadas() {
        return larguraGrafico(resumo.aprovadas());
    }

    public String getLarguraReprovadas() {
        // A diferença mantém os segmentos dentro da largura total após o arredondamento.
        return new BigDecimal(larguraGrafico(resumo.avaliacoes()))
                .subtract(new BigDecimal(getLarguraAprovadas())).toPlainString();
    }

    public String getLarguraNotificacoes() {
        return larguraGrafico(resumo.notificacoes());
    }

    private String larguraGrafico(long quantidade) {
        long escala = getEscalaGrafico();
        return escala == 0 ? "0" : BigDecimal.valueOf(quantidade).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(escala), 2, RoundingMode.HALF_UP).toPlainString();
    }

    public boolean isTemProximaPagina() {
        return ((long) pagina + 1) * TAMANHO_PAGINA < getTotalRegistros();
    }

    public long getPrimeiroRegistro() {
        return getTotalRegistros() == 0 ? 0 : (long) pagina * TAMANHO_PAGINA + 1;
    }

    public long getUltimoRegistro() {
        int tamanho = abaAvaliacoes ? avaliacoes.size() : notificacoes.size();
        return tamanho == 0 ? 0 : (long) pagina * TAMANHO_PAGINA + tamanho;
    }

    public String getPeriodoAplicado() {
        if (filtroAplicado == null) return "Todo o histórico";
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate inicio = filtroAplicado.dataInicio();
        LocalDate fim = filtroAplicado.dataFim();
        if (inicio == null && fim == null) return "Todo o histórico";
        if (inicio == null) return "Até " + fim.format(formato);
        if (fim == null) return "Desde " + inicio.format(formato);
        return inicio.format(formato) + " a " + fim.format(formato);
    }

    public boolean isFiltroAplicado() {
        return filtroAplicado != null && (filtroAplicado.dataInicio() != null || filtroAplicado.dataFim() != null
                || filtroAplicado.fabricanteId() != null || filtroAplicado.marcaId() != null);
    }
}
