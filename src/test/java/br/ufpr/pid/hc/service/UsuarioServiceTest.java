package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.UsuarioDao;
import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.Perfil;
import br.ufpr.pid.hc.exception.DomainException;
import br.ufpr.pid.hc.exception.DuplicateEmailException;
import br.ufpr.pid.hc.exception.DuplicateRecordException;
import br.ufpr.pid.hc.exception.LastActiveAdminException;
import br.ufpr.pid.hc.exception.MissingRequiredFieldsException;
import br.ufpr.pid.hc.exception.WeakPasswordException;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioDao usuarioDao;

    @Mock
    private Pbkdf2PasswordHash hashUtil;

    @InjectMocks
    private UsuarioService service;

    @Test
    void cadastraComSenhaCriptografadaEAtivo() {
        Usuario novo = usuario(null, "ana@hc.br", Perfil.ANALISTA, null);
        novo.setSenha("segredo123");
        when(hashUtil.generate(any())).thenReturn("hash");
        when(usuarioDao.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Usuario salvo = service.salvar(novo);

        assertEquals("hash", salvo.getSenha());
        assertTrue(salvo.getAtivo());
    }

    @Test
    void recusaSenhaCurta() {
        Usuario novo = usuario(null, "ana@hc.br", Perfil.ANALISTA, null);
        novo.setSenha("12345");

        assertThrows(WeakPasswordException.class, () -> service.salvar(novo));
        verify(usuarioDao, never()).salvar(any());
    }

    @Test
    void recusaCadastroSemSenha() {
        Usuario novo = usuario(null, "ana@hc.br", Perfil.ANALISTA, null);

        assertThrows(MissingRequiredFieldsException.class, () -> service.salvar(novo));
    }

    @Test
    void recusaCamposEmBranco() {
        Usuario novo = usuario(null, " ", null, null);
        novo.setSenha("segredo123");

        assertThrows(MissingRequiredFieldsException.class, () -> service.salvar(novo));
    }

    @Test
    void recusaEmailDeOutroUsuario() {
        Usuario novo = usuario(null, "ana@hc.br", Perfil.ANALISTA, null);
        novo.setSenha("segredo123");
        when(usuarioDao.buscarPorEmail("ana@hc.br")).thenReturn(usuario(UUID.randomUUID(), "ana@hc.br", Perfil.ANALISTA, true));

        assertThrows(DuplicateEmailException.class, () -> service.salvar(novo));
    }

    @Test
    void cpfEmBranco() {
        Usuario novo = usuario(null, "ana@hc.br", Perfil.ANALISTA, null);
        novo.setSenha("segredo123");
        novo.setCpf("  ");
        when(usuarioDao.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Usuario salvo = service.salvar(novo);

        assertNull(salvo.getCpf());
        verify(usuarioDao, never()).buscarPorCpf(any());
    }

    @Test
    void cpfRepetido() {
        Usuario novo = usuario(null, "ana@hc.br", Perfil.ANALISTA, null);
        novo.setSenha("segredo123");
        novo.setCpf("12345678901");
        when(usuarioDao.buscarPorCpf("12345678901")).thenReturn(usuario(UUID.randomUUID(), "bia@hc.br", Perfil.ANALISTA, true));

        assertThrows(DuplicateRecordException.class, () -> service.salvar(novo));
        verify(usuarioDao, never()).salvar(any());
    }

    @Test
    void edicaoAceitaOProprioEmailENaoMexeNaSenha() {
        UUID id = UUID.randomUUID();
        Usuario atual = usuario(id, "ana@hc.br", Perfil.ANALISTA, true);
        atual.setSenha("hash");
        Usuario editado = usuario(id, "ana@hc.br", Perfil.ANALISTA, true);
        editado.setNome("Ana Maria");
        when(usuarioDao.buscarPorEmail("ana@hc.br")).thenReturn(atual);
        when(usuarioDao.buscarPorId(id)).thenReturn(atual);
        when(usuarioDao.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Usuario salvo = service.salvar(editado);

        assertEquals("Ana Maria", salvo.getNome());
        assertEquals("hash", salvo.getSenha());
    }

    @Test
    void naoRebaixaOUltimoAdministradorAtivo() {
        UUID id = UUID.randomUUID();
        when(usuarioDao.buscarPorId(id)).thenReturn(usuario(id, "admin@hc.br", Perfil.ADMINISTRADOR, true));
        when(usuarioDao.contarAtivosPorPerfil(Perfil.ADMINISTRADOR)).thenReturn(1L);

        assertThrows(LastActiveAdminException.class,
                () -> service.salvar(usuario(id, "admin@hc.br", Perfil.ANALISTA, true)));
    }

    @Test
    void naoDesativaOUltimoAdministradorAtivo() {
        UUID id = UUID.randomUUID();
        when(usuarioDao.buscarPorId(id)).thenReturn(usuario(id, "admin@hc.br", Perfil.ADMINISTRADOR, true));
        when(usuarioDao.contarAtivosPorPerfil(Perfil.ADMINISTRADOR)).thenReturn(1L);

        assertThrows(LastActiveAdminException.class,
                () -> service.salvar(usuario(id, "admin@hc.br", Perfil.ADMINISTRADOR, false)));
    }

    @Test
    void rebaixaAdministradorQuandoHaOutro() {
        UUID id = UUID.randomUUID();
        when(usuarioDao.buscarPorId(id)).thenReturn(usuario(id, "admin@hc.br", Perfil.ADMINISTRADOR, true));
        when(usuarioDao.contarAtivosPorPerfil(Perfil.ADMINISTRADOR)).thenReturn(2L);
        when(usuarioDao.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Usuario salvo = service.salvar(usuario(id, "admin@hc.br", Perfil.ANALISTA, true));

        assertEquals(Perfil.ANALISTA, salvo.getPerfil());
    }

    @Test
    void primeiroAdministradorViraAdministradorAtivo() {
        Usuario novo = usuario(null, "admin@hc.br", Perfil.ANALISTA, false);
        novo.setSenha("segredo123");
        when(usuarioDao.contarAtivosPorPerfil(Perfil.ADMINISTRADOR)).thenReturn(0L);
        when(hashUtil.generate(any())).thenReturn("hash");
        when(usuarioDao.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Usuario salvo = service.cadastrarPrimeiroAdministrador(novo);

        assertEquals(Perfil.ADMINISTRADOR, salvo.getPerfil());
        assertTrue(salvo.getAtivo());
    }

    @Test
    void primeiroAdministradorSoUmaVez() {
        when(usuarioDao.contarAtivosPorPerfil(Perfil.ADMINISTRADOR)).thenReturn(1L);

        assertThrows(DomainException.class,
                () -> service.cadastrarPrimeiroAdministrador(usuario(null, "x@hc.br", Perfil.ANALISTA, true)));
        verify(usuarioDao, never()).salvar(any());
    }

    private static Usuario usuario(UUID id, String email, Perfil perfil, Boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Fulano");
        usuario.setEmail(email);
        usuario.setPerfil(perfil);
        usuario.setAtivo(ativo);
        return usuario;
    }
}
