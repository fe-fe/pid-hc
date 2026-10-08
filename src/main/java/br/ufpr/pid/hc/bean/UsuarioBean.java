package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.Perfil;
import br.ufpr.pid.hc.enumeration.UsuarioOrdenacao;
import br.ufpr.pid.hc.exception.DomainException;
import br.ufpr.pid.hc.service.AbstractService;
import br.ufpr.pid.hc.service.UsuarioService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Named
@ViewScoped
@Getter
@Setter
public class UsuarioBean extends AbstractCrudBean<Usuario, UUID, UsuarioOrdenacao> {

    @Inject
    private UsuarioService usuarioService;

    public UsuarioBean() {
        super(15, UsuarioOrdenacao.NOME);
    }

    public void limparFiltros() {
        ordenacaoAtual = UsuarioOrdenacao.NOME;
        mostrarInativos = false;
        reordenar();
    }

    @Override
    protected AbstractService<Usuario, UUID> getService() {
        return usuarioService;
    }

    @Override
    protected Usuario criarNovaEntidade() {
        return new Usuario();
    }

    @Override
    public void cadastrar() {
        try {
            super.cadastrar();
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Usuário cadastrado com sucesso");
        } catch (DomainException | IllegalArgumentException e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    @Override
    public void atualizar() {
        try {
            super.atualizar();
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Usuário atualizado com sucesso");
        } catch (DomainException | IllegalArgumentException e) {
            // descarta as alterações feitas em memória na linha editada
            carregarPagina();
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    private void adicionarMensagem(FacesMessage.Severity severidade, String mensagem) {
        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(severidade, mensagem, null)
        );
    }

    public List<Usuario> getUsuarios() {
        return getLista();
    }

    public Usuario getUsuario() {
        return getEntidade();
    }

    public Usuario getUsuarioSelecionado() {
        return getEntidadeSelecionada();
    }

    public void setUsuarioSelecionado(Usuario usuario) {
        setEntidadeSelecionada(usuario);
    }

    public Perfil[] getPerfis() {
        return Perfil.values();
    }

    public UsuarioOrdenacao[] getOpcoesOrdenacao() {
        return UsuarioOrdenacao.values();
    }
}
