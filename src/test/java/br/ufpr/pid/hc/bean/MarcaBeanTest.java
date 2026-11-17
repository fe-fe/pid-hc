package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Marca;
import br.ufpr.pid.hc.exception.DuplicateRecordException;
import br.ufpr.pid.hc.service.MarcaService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarcaBeanTest {

    @Mock
    private MarcaService marcaService;

    @InjectMocks
    private MarcaBean bean;

    private MockedStatic<Bootstrap> bootstrap;

    @BeforeEach
    void abrirBootstrap() {
        bootstrap = mockStatic(Bootstrap.class);
    }

    @AfterEach
    void fecharBootstrap() {
        bootstrap.close();
    }

    @Test
    void novoAbreFormularioLimpoEAtivo() {
        bean.novo();

        assertTrue(bean.isNovo());
        assertEquals("Incluir marca", bean.getTituloModal());
        assertTrue(bean.getEmEdicao().getAtivo());
        assertEquals(null, bean.getEmEdicao().getNome());
        bootstrap.verify(() -> Bootstrap.abrirModal("modalCadastro"));
    }

    @Test
    void editarCarregaDoBanco() {
        UUID id = UUID.randomUUID();
        Marca doBanco = new Marca();
        doBanco.setNome("ALFA");
        when(marcaService.buscarPorId(id)).thenReturn(doBanco);

        bean.editar(id);

        assertFalse(bean.isNovo());
        assertEquals("Editar marca", bean.getTituloModal());
        assertSame(doBanco, bean.getEmEdicao());
        assertTrue(doBanco.getAtivo(), "registro antigo com ativo nulo aparece como ativo");
        bootstrap.verify(() -> Bootstrap.abrirModal("modalCadastro"));
    }

    @Test
    void novoDepoisDeEditarVoltaAoModoIncluir() {
        UUID id = UUID.randomUUID();
        when(marcaService.buscarPorId(id)).thenReturn(new Marca());
        bean.editar(id);

        bean.novo();

        assertTrue(bean.isNovo());
        assertEquals("Incluir marca", bean.getTituloModal());
    }

    @Test
    void salvarNovoVoltaParaPrimeiraPaginaEFechaModal() {
        when(marcaService.contarPorTermo(isNull(), anyBoolean())).thenReturn(1L);
        when(marcaService.buscarPorTermo(anyInt(), anyInt(), any(), isNull(), anyBoolean())).thenReturn(List.of(new Marca()));
        bean.setPaginaAtual(3);
        bean.novo();
        bean.getEmEdicao().setNome("Alfa");

        bean.salvar();

        verify(marcaService).salvar(bean.getEmEdicao());
        assertEquals(0, bean.getPaginaAtual());
        assertEquals(1, bean.getLista().size());
        bootstrap.verify(() -> Bootstrap.fecharModal("modalCadastro"));
    }

    @Test
    void salvarComErroMantemModalAbertoEMostraMensagem() {
        FacesContext contexto = mock(FacesContext.class);
        when(marcaService.salvar(any())).thenThrow(new DuplicateRecordException("Já existe um cadastro com o nome ALFA"));
        bean.novo();

        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(contexto);
            bean.salvar();
        }

        ArgumentCaptor<FacesMessage> mensagem = ArgumentCaptor.forClass(FacesMessage.class);
        verify(contexto).addMessage(isNull(), mensagem.capture());
        assertEquals("Já existe um cadastro com o nome ALFA", mensagem.getValue().getSummary());
        assertEquals(FacesMessage.SEVERITY_ERROR, mensagem.getValue().getSeverity());
        verify(marcaService, never()).buscarPorTermo(anyInt(), anyInt(), any(), any(), anyBoolean());
        bootstrap.verify(() -> Bootstrap.fecharModal(any()), never());
    }
}
