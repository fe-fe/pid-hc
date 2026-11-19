package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.MarcaDao;
import br.ufpr.pid.hc.entity.Marca;
import br.ufpr.pid.hc.exception.DuplicateRecordException;
import br.ufpr.pid.hc.exception.MissingRequiredFieldsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarcaServiceTest {

    @Mock
    private MarcaDao marcaDao;

    @InjectMocks
    private MarcaService service;

    @Test
    void padronizaONomeAoSalvar() {
        when(marcaDao.buscarPorNome("SERINGA BD")).thenReturn(Optional.empty());
        when(marcaDao.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Marca salva = service.salvar(marca(null, "  seringa   bd "));

        assertEquals("SERINGA BD", salva.getNome());
    }

    @Test
    void recusaNomeEmBranco() {
        assertThrows(MissingRequiredFieldsException.class, () -> service.salvar(marca(null, " ")));
    }

    @Test
    void recusaNomeDeOutraMarca() {
        when(marcaDao.buscarPorNome("ALFA")).thenReturn(Optional.of(marca(UUID.randomUUID(), "ALFA")));

        assertThrows(DuplicateRecordException.class, () -> service.salvar(marca(null, "alfa")));
        verify(marcaDao, never()).salvar(any());
    }

    @Test
    void edicaoAceitaOProprioNome() {
        UUID id = UUID.randomUUID();
        when(marcaDao.buscarPorNome("ALFA")).thenReturn(Optional.of(marca(id, "ALFA")));
        when(marcaDao.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        assertEquals("ALFA", service.salvar(marca(id, "Alfa")).getNome());
    }

    @Test
    void obterOuCriarReaproveitaExistente() {
        Marca existente = marca(UUID.randomUUID(), "ALFA");
        when(marcaDao.buscarPorNome("ALFA")).thenReturn(Optional.of(existente));

        assertSame(existente, service.obterOuCriar(" alfa "));
        verify(marcaDao, never()).salvar(any());
    }

    @Test
    void obterOuCriarCadastraNova() {
        when(marcaDao.buscarPorNome("BETA")).thenReturn(Optional.empty());
        when(marcaDao.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        assertEquals("BETA", service.obterOuCriar("beta").getNome());
    }

    @Test
    void obterEmBranco() {
        assertThrows(MissingRequiredFieldsException.class, () -> service.obterOuCriar("   "));
        verify(marcaDao, never()).salvar(any());
    }

    private static Marca marca(UUID id, String nome) {
        Marca marca = new Marca();
        marca.setId(id);
        marca.setNome(nome);
        return marca;
    }
}
