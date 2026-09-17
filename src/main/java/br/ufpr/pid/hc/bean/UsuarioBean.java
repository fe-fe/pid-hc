package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Setor;
import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.Perfil;
import br.ufpr.pid.hc.enumeration.SetorOrdenacao;
import br.ufpr.pid.hc.enumeration.UsuarioOrdenacao;
import br.ufpr.pid.hc.exception.DomainException;
import br.ufpr.pid.hc.service.AbstractService;
import br.ufpr.pid.hc.service.SetorService;
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

    @Inject
    private SetorService setorService;

    private List<Setor> setores;
    private String setorSelecionadoCadastrarId;
    private String setorSelecionadoAtualizarId;

    public UsuarioBean() {
        super(15, UsuarioOrdenacao.NOME);
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
    protected void posInit() {
        setores = setorService.buscar(0, Integer.MAX_VALUE, SetorOrdenacao.CODIGO);
    }

    @Override
    public void cadastrar() {
        try {
            getUsuario().setSetor(buscarSetor(setorSelecionadoCadastrarId));
            super.cadastrar();
            setorSelecionadoCadastrarId = null;
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Usuário cadastrado com sucesso");
        } catch (DomainException | IllegalArgumentException e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    @Override
    public void atualizar() {
        try {
            getUsuarioSelecionado().setSetor(buscarSetor(setorSelecionadoAtualizarId));
            super.atualizar();
            adicionarMensagem(FacesMessage.SEVERITY_INFO, "Usuário atualizado com sucesso");
        } catch (DomainException | IllegalArgumentException e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    private Setor buscarSetor(String setorId) {
        if (setorId == null || setorId.isBlank()) {
            return null;
        }

        return setorService.buscarPorId(UUID.fromString(setorId));
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
        setorSelecionadoAtualizarId = usuario != null && usuario.getSetor() != null
                ? usuario.getSetor().getId().toString()
                : null;
    }

    public Perfil[] getPerfis() {
        return Perfil.values();
    }

    public UsuarioOrdenacao[] getOpcoesOrdenacao() {
        return UsuarioOrdenacao.values();
    }
}
