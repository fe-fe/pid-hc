package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.ImportacaoAnvisa;
import br.ufpr.pid.hc.entity.ProdutoAnvisa;
import br.ufpr.pid.hc.enumeration.ResultadoImportacaoAnvisa;
import br.ufpr.pid.hc.service.ImportacaoAnvisaService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Named
@ViewScoped
@Getter
@Setter
public class ProdutoAnvisaBean implements Serializable {

    private static final int TAMANHO_PAGINA = 15;

    @Inject
    private ImportacaoAnvisaService importacaoAnvisaService;

    private String termo;
    private List<ProdutoAnvisa> lista;
    private int paginaAtual = 0;
    private long totalRegistros;
    private ImportacaoAnvisa ultimaImportacao;

    @PostConstruct
    public void init() {
        ultimaImportacao = importacaoAnvisaService.buscarUltima().orElse(null);
        carregarPagina();
    }

    private void carregarPagina() {
        totalRegistros = importacaoAnvisaService.contarProdutos(termo);
        lista = importacaoAnvisaService.buscarProdutos(termo, paginaAtual, TAMANHO_PAGINA);
    }

    public void buscar() {
        paginaAtual = 0;
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
        return (long) (paginaAtual + 1) * TAMANHO_PAGINA >= totalRegistros;
    }

    public boolean isAtualizacaoBloqueada() {
        return !importacaoAnvisaService.isAtualizacaoPermitida();
    }

    public LocalDateTime getProximaAtualizacao() {
        return importacaoAnvisaService.getProximaAtualizacaoPermitida();
    }

    public void atualizar() {
        try {
            ResultadoImportacaoAnvisa resultado = importacaoAnvisaService.importar();
            ultimaImportacao = importacaoAnvisaService.buscarUltima().orElse(null);
            buscar();
            switch (resultado) {
                case IMPORTADA -> adicionarMensagem(FacesMessage.SEVERITY_INFO, "Base ANVISA atualizada.");
                case SEM_ALTERACAO -> adicionarMensagem(FacesMessage.SEVERITY_INFO,
                        "A base ANVISA não mudou desde a última atualização.");
                case EM_ANDAMENTO -> adicionarMensagem(FacesMessage.SEVERITY_WARN,
                        "Já existe uma atualização em andamento. Tente novamente em alguns minutos.");
                case AGUARDANDO_INTERVALO -> adicionarMensagem(FacesMessage.SEVERITY_WARN,
                        "A base ANVISA só pode ser atualizada novamente a partir de "
                                + getProximaAtualizacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")) + ".");
            }
        } catch (RuntimeException e) {
            log.error("Erro ao atualizar a base ANVISA", e);
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Não foi possível atualizar a base ANVISA.");
        }
    }

    private void adicionarMensagem(FacesMessage.Severity severidade, String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidade, mensagem, null));
    }
}
