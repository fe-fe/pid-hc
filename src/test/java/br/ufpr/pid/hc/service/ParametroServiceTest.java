package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.ParametroDao;
import br.ufpr.pid.hc.entity.Parametro;
import br.ufpr.pid.hc.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParametroServiceTest {

    @Mock
    private ParametroDao parametroDao;

    @InjectMocks
    private ParametroService service;

    @Test
    void validadePadrao() {
        when(parametroDao.buscar(Parametro.VALIDADE)).thenReturn(Optional.empty());

        assertEquals(24, service.validade());
    }

    @Test
    void validadeConfigurada() {
        when(parametroDao.buscar(Parametro.VALIDADE)).thenReturn(Optional.of("36"));

        assertEquals(36, service.validade());
    }

    @Test
    void alteraValidade() {
        service.alterarValidade(12);

        verify(parametroDao).salvar(Parametro.VALIDADE, "12");
    }

    @Test
    void recusaValidadeInvalida() {
        assertThrows(DomainException.class, () -> service.alterarValidade(0));
        assertThrows(DomainException.class, () -> service.alterarValidade(121));
        assertThrows(DomainException.class, () -> service.alterarValidade(null));
        verify(parametroDao, never()).salvar(any(), any());
    }
}
