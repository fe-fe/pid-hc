package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Auditavel;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import br.ufpr.pid.hc.exception.DomainException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.util.UUID;

public abstract class AbstractCadastroBean<T extends Auditavel, E extends Enum<E> & CampoOrdenacao>
        extends AbstractCrudBean<T, UUID, E> {

    protected AbstractCadastroBean(E ordenacaoPadrao) {
        super(15, ordenacaoPadrao);
    }

    public abstract E[] getOpcoesOrdenacao();

    @Override
    public void cadastrar() {
        try {
            super.cadastrar();
        } catch (DomainException e) {
            adicionarErro(e.getMessage());
        }
    }

    @Override
    public void atualizar() {
        try {
            super.atualizar();
        } catch (DomainException e) {
            carregarPagina();
            adicionarErro(e.getMessage());
        }
    }

    protected void adicionarErro(String mensagem) {
        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, null)
        );
    }
}
