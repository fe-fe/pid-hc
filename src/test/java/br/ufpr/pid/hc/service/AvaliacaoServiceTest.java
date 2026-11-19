package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AvaliacaoDao;
import br.ufpr.pid.hc.entity.Avaliacao;
import br.ufpr.pid.hc.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoDao avaliacaoDao;

    @Mock
    private MaterialService materialService;

    @Mock
    private FabricanteService fabricanteService;

    @Mock
    private MarcaService marcaService;

    @InjectMocks
    private AvaliacaoService service;

    @Test
    void registroInvalido() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setRegistroAnvisa("80.102.510.123");

        DomainException erro = assertThrows(DomainException.class,
                () -> service.registrar(avaliacao, "M1", "MATERIAL", "FABRICANTE", null));

        assertEquals("Registro ANVISA deve ter 11 dígitos, só números.", erro.getMessage());
        verifyNoInteractions(materialService, fabricanteService);
        verify(avaliacaoDao, never()).salvar(any());
    }

    @Test
    void registroValido() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setRegistroAnvisa("80102510123");
        when(avaliacaoDao.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        assertEquals("80102510123", service.registrar(avaliacao, "M1", "MATERIAL", "FABRICANTE", null).getRegistroAnvisa());
    }

    @Test
    void registroVazio() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setRegistroAnvisa("");
        when(avaliacaoDao.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        assertEquals("", service.registrar(avaliacao, "M1", "MATERIAL", "FABRICANTE", null).getRegistroAnvisa());
    }
}
