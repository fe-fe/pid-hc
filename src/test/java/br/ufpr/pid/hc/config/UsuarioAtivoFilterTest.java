package br.ufpr.pid.hc.config;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.Perfil;
import br.ufpr.pid.hc.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioAtivoFilterTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private UsuarioAtivoFilter filtro;

    private Usuario usuario;

    @BeforeEach
    void logar() {
        usuario = new Usuario();
        usuario.setEmail("analista@hc.br");
        usuario.setPerfil(Perfil.ANALISTA);
        usuario.setAtivo(true);
        when(request.getUserPrincipal()).thenReturn(() -> "analista@hc.br");
        when(request.getContextPath()).thenReturn("");
        when(request.isUserInRole("ANALISTA")).thenReturn(true);
        when(usuarioService.buscarPorEmail("analista@hc.br")).thenReturn(usuario);
    }

    @Test
    void usuarioAtivo() throws Exception {
        filtro.doFilter(request, response, session, chain);

        verify(chain).doFilter(request, response);
        verify(request, never()).logout();
    }

    @Test
    void usuarioDesativado() throws Exception {
        usuario.setAtivo(false);

        filtro.doFilter(request, response, session, chain);

        verify(request).logout();
        verify(session).invalidate();
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void perfilAlterado() throws Exception {
        usuario.setPerfil(Perfil.ADMINISTRADOR);

        filtro.doFilter(request, response, session, chain);

        verify(request).logout();
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void usuarioRemovido() throws Exception {
        when(usuarioService.buscarPorEmail("analista@hc.br")).thenReturn(null);

        filtro.doFilter(request, response, session, chain);

        verify(request).logout();
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void semLogin() throws Exception {
        when(request.getUserPrincipal()).thenReturn(null);

        filtro.doFilter(request, response, session, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(usuarioService);
    }
}
