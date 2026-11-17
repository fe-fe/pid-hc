package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.Perfil;
import br.ufpr.pid.hc.enumeration.UsuarioOrdenacao;
import br.ufpr.pid.hc.service.AbstractService;
import br.ufpr.pid.hc.service.UsuarioService;
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
public class UsuarioBean extends AbstractCadastroBean<Usuario, UsuarioOrdenacao> {

    @Inject
    private UsuarioService usuarioService;

    private String nomePesquisa;
    private String nomeFiltrado;

    public UsuarioBean() {
        super(UsuarioOrdenacao.NOME, "usuário");
    }

    public void limparFiltros() {
        nomePesquisa = null;
        nomeFiltrado = null;
        ordenacaoAtual = UsuarioOrdenacao.NOME;
        mostrarInativos = false;
        reordenar();
    }

    public void aplicarFiltros() {
        reordenar();
        Bootstrap.fecharOffcanvas("usuariosFilters");
    }

    public void pesquisarPorNome() {
        nomeFiltrado = nomePesquisa == null || nomePesquisa.isBlank() ? null : nomePesquisa.trim();
        reordenar();
    }

    @Override
    protected long contarRegistros() {
        return nomeFiltrado == null
                ? super.contarRegistros()
                : usuarioService.contarPorNome(nomeFiltrado, mostrarInativos);
    }

    @Override
    protected List<Usuario> buscarRegistros() {
        return nomeFiltrado == null
                ? super.buscarRegistros()
                : usuarioService.buscarPorNome(paginaAtual, tamanhoPagina, ordenacaoAtual,
                        nomeFiltrado, mostrarInativos);
    }

    @Override
    protected AbstractService<Usuario, UUID> getService() {
        return usuarioService;
    }

    @Override
    protected Usuario criarNovaEntidade() {
        return new Usuario();
    }

    public List<Usuario> getUsuarios() {
        return getLista();
    }

    public Perfil[] getPerfis() {
        return Perfil.values();
    }

    @Override
    public UsuarioOrdenacao[] getOpcoesOrdenacao() {
        return UsuarioOrdenacao.values();
    }
}
