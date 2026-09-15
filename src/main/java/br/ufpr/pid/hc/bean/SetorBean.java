package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Setor;
import br.ufpr.pid.hc.service.SetorService;
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
public class SetorBean implements Serializable {

    @Inject
    private SetorService setorService;
    @Inject
    private SecurityContext securityContext;
    @Inject
    private Sessao session;

    private Setor setor = new Setor();
    private Setor setorSelecionado;
    private List<Setor> setores;

    private int paginaAtual = 0;
    private int tamanhoPagina = 15;
    private long totalRegistros;

    private void carregarPagina() {
        setores = setorService.buscar(paginaAtual, tamanhoPagina);
    }

    @PostConstruct
    public void init() {
        totalRegistros = setorService.contarTotal();
        carregarPagina();
    }

    public void cadastrar() {
        setorService.salvar(setor);
        setor = new Setor();
        totalRegistros = setorService.contarTotal();
        paginaAtual = 0;
        carregarPagina();
    }

    public void atualizar() {
        setorService.salvar(setorSelecionado);
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
        return (long) (paginaAtual + 1) * tamanhoPagina >= totalRegistros;
    }

}
