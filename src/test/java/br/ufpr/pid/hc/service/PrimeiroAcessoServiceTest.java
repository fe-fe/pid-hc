package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrimeiroAcessoServiceTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private PrimeiroAcessoService service;

    @Test
    void geraCodigoLegivelEnquantoNaoHaAdministrador() throws Exception {
        when(usuarioService.existeAdministradorAtivo()).thenReturn(false);

        assertTrue(service.isPendente());
        String codigo = codigo();
        assertTrue(codigo.matches("[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}"), codigo);

        service.isPendente();
        assertEquals(codigo, codigo(), "o código não muda enquanto o processo está de pé");
    }

    @Test
    void naoFicaPendenteComAdministrador() {
        when(usuarioService.existeAdministradorAtivo()).thenReturn(true);

        assertFalse(service.isPendente());
    }

    @Test
    void criaAdministradorComCodigoDigitadoEmMinusculasEComEspacos() throws Exception {
        when(usuarioService.existeAdministradorAtivo()).thenReturn(false);
        service.isPendente();
        Usuario usuario = new Usuario();

        service.criarAdministrador(usuario, "  " + codigo().toLowerCase() + " ");

        verify(usuarioService).cadastrarPrimeiroAdministrador(usuario);
    }

    @Test
    void recusaCodigoErrado() throws Exception {
        when(usuarioService.existeAdministradorAtivo()).thenReturn(false);
        service.isPendente();
        String errado = codigo().equals("AAAA-AAAA-AAAA") ? "BBBB-BBBB-BBBB" : "AAAA-AAAA-AAAA";
        assertNotEquals(codigo(), errado);

        assertThrows(DomainException.class, () -> service.criarAdministrador(new Usuario(), errado));
        assertThrows(DomainException.class, () -> service.criarAdministrador(new Usuario(), null));
        verify(usuarioService, never()).cadastrarPrimeiroAdministrador(any());
    }

    @Test
    void recusaDepoisDeConcluido() {
        when(usuarioService.existeAdministradorAtivo()).thenReturn(true);

        assertThrows(DomainException.class, () -> service.criarAdministrador(new Usuario(), "AAAA-AAAA-AAAA"));
        verify(usuarioService, never()).cadastrarPrimeiroAdministrador(any());
    }

    private String codigo() throws Exception {
        Field campo = PrimeiroAcessoService.class.getDeclaredField("codigo");
        campo.setAccessible(true);
        return (String) campo.get(service);
    }
}
