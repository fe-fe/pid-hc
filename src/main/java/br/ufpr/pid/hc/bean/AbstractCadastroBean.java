package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Auditavel;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import br.ufpr.pid.hc.exception.DomainException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public abstract class AbstractCadastroBean<T extends Auditavel, E extends Enum<E> & CampoOrdenacao>
        extends AbstractCrudBean<T, UUID, E> {

    private static final String MODAL = "modalCadastro";

    private final String rotulo;
    private T emEdicao;
    private UUID idEmEdicao;

    protected AbstractCadastroBean(E ordenacaoPadrao, String rotulo) {
        super(15, ordenacaoPadrao);
        this.rotulo = rotulo;
    }

    public abstract E[] getOpcoesOrdenacao();

    protected abstract T criarNovaEntidade();

    public void novo() {
        idEmEdicao = null;
        emEdicao = criarNovaEntidade();
        emEdicao.setAtivo(true);
        Bootstrap.abrirModal(MODAL);
    }

    public void editar(UUID id) {
        idEmEdicao = id;
        emEdicao = getService().buscarPorId(id);
        if (emEdicao.getAtivo() == null) {
            emEdicao.setAtivo(true);
        }
        Bootstrap.abrirModal(MODAL);
    }

    public boolean isNovo() {
        return idEmEdicao == null;
    }

    public String getTituloModal() {
        return (isNovo() ? "Incluir " : "Editar ") + rotulo;
    }

    public void salvar() {
        try {
            getService().salvar(emEdicao);
        } catch (DomainException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
            return;
        }
        if (isNovo()) {
            paginaAtual = 0;
        }
        carregarPagina();
        Bootstrap.fecharModal(MODAL);
    }
}
