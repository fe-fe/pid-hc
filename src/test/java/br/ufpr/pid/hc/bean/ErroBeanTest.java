package br.ufpr.pid.hc.bean;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErroBeanTest {

    @Test
    void acessoNegado() {
        ErroBean bean = iniciar(403, mock(ExternalContext.class));

        assertTrue(bean.isAcessoNegado());
        assertEquals("Acesso negado", bean.getTitulo());
    }

    @Test
    void paginaNaoEncontrada() {
        ErroBean bean = iniciar(404, mock(ExternalContext.class));

        assertTrue(bean.isNaoEncontrada());
        assertEquals("Página não encontrada", bean.getTitulo());
    }

    @Test
    void erroInterno() {
        ExternalContext externo = mock(ExternalContext.class);
        ErroBean bean = iniciar(500, externo);

        assertFalse(bean.isAcessoNegado());
        assertFalse(bean.isNaoEncontrada());
        assertEquals("Algo deu errado", bean.getTitulo());
        verify(externo, never()).setResponseStatus(500);
    }

    @Test
    void acessoDiretoAPaginaDeErroResponde404() {
        ExternalContext externo = mock(ExternalContext.class);
        ErroBean bean = iniciar(null, externo);

        assertTrue(bean.isNaoEncontrada());
        verify(externo).setResponseStatus(404);
    }

    private static ErroBean iniciar(Integer status, ExternalContext externo) {
        Map<String, Object> requisicao = new HashMap<>();
        if (status != null) {
            requisicao.put(RequestDispatcher.ERROR_STATUS_CODE, status);
        }
        FacesContext contexto = mock(FacesContext.class);
        when(contexto.getExternalContext()).thenReturn(externo);
        when(externo.getRequestMap()).thenReturn(requisicao);

        ErroBean bean = new ErroBean();
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(contexto);
            bean.iniciar();
        }
        return bean;
    }
}
