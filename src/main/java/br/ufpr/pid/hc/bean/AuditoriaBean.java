package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.dto.EventoAuditoria;
import br.ufpr.pid.hc.dto.FiltroAuditoria;
import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.TipoEvento;
import br.ufpr.pid.hc.service.AuditoriaService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Named
@ViewScoped
@Getter
@Setter
public class AuditoriaBean implements Serializable, Paginavel {

    private static final int TAMANHO_PAGINA = 15;

    @Inject
    private AuditoriaService auditoriaService;

    private TipoEvento tipo;
    private String usuario;
    private LocalDate inicio;
    private LocalDate fim;

    private FiltroAuditoria filtroAplicado = FiltroAuditoria.VAZIO;
    private List<EventoAuditoria> eventos = List.of();
    private List<Usuario> usuarios = List.of();
    private long totalRegistros;
    private int paginaAtual;

    @PostConstruct
    void iniciar() {
        usuarios = auditoriaService.listarUsuarios();
        carregar();
    }

    public void aplicarFiltros() {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            FacesContext contexto = FacesContext.getCurrentInstance();
            contexto.addMessage("auditoriaFiltersForm:fim", new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "A data final deve ser igual ou posterior à inicial", null));
            contexto.validationFailed();
            return;
        }
        filtroAplicado = new FiltroAuditoria(tipo, usuario == null || usuario.isBlank() ? null : usuario, inicio, fim);
        paginaAtual = 0;
        carregar();
        Bootstrap.fecharOffcanvas("auditoriaFilters");
    }

    public void limparFiltros() {
        tipo = null;
        usuario = null;
        inicio = null;
        fim = null;
        aplicarFiltros();
    }

    public boolean isFiltroAtivo() {
        return !FiltroAuditoria.VAZIO.equals(filtroAplicado);
    }

    public TipoEvento[] getTipos() {
        return TipoEvento.values();
    }

    @Override
    public int getTamanhoPagina() {
        return TAMANHO_PAGINA;
    }

    @Override
    public void irParaPagina(int pagina) {
        paginaAtual = Math.max(0, Math.min(pagina, getTotalPaginas() - 1));
        carregar();
    }

    private void carregar() {
        totalRegistros = auditoriaService.contar(filtroAplicado);
        eventos = auditoriaService.buscar(filtroAplicado, paginaAtual, TAMANHO_PAGINA);
    }
}
