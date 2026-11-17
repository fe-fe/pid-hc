package br.ufpr.pid.hc.bean;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MenuBeanTest {

    private final MenuBean bean = new MenuBean();

    @Test
    void marcaSomenteAPaginaAtual() {
        naPagina("/pages/avaliacoes.xhtml", () -> {
            assertEquals("page", bean.ariaCurrent("/pages/avaliacoes"));
            assertNull(bean.ariaCurrent("/pages/tecnovigilancia"));
        });
    }

    @Test
    void gestaoAbertaNosCadastrosENosUsuarios() {
        naPagina("/pages/controle/marcas.xhtml", () -> assertTrue(bean.isGestaoAberta()));
        naPagina("/pages/admin/users.xhtml", () -> assertTrue(bean.isGestaoAberta()));
        naPagina("/pages/avaliacoes.xhtml", () -> assertFalse(bean.isGestaoAberta()));
    }

    private static void naPagina(String viewId, Runnable verificacao) {
        FacesContext contexto = mock(FacesContext.class);
        UIViewRoot raiz = mock(UIViewRoot.class);
        when(contexto.getViewRoot()).thenReturn(raiz);
        when(raiz.getViewId()).thenReturn(viewId);
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(contexto);
            verificacao.run();
        }
    }
}
