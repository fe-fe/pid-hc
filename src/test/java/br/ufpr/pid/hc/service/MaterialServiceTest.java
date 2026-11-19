package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.MaterialDao;
import br.ufpr.pid.hc.entity.Material;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialDao materialDao;

    @InjectMocks
    private MaterialService service;

    @Test
    void tiraEspacosDoCodigoEDoNome() {
        when(materialDao.buscarPorCodigo("123")).thenReturn(Optional.empty());
        when(materialDao.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Material salvo = service.salvar(material(null, " 123 ", " Gaze "));

        assertEquals("123", salvo.getCodigo());
        assertEquals("Gaze", salvo.getNome());
    }

    @Test
    void exigeCodigoENome() {
        assertThrows(MissingRequiredFieldsException.class, () -> service.salvar(material(null, "", "Gaze")));
        assertThrows(MissingRequiredFieldsException.class, () -> service.salvar(material(null, "123", null)));
    }

    @Test
    void recusaCodigoDeOutroMaterial() {
        when(materialDao.buscarPorCodigo("123")).thenReturn(Optional.of(material(UUID.randomUUID(), "123", "Gaze")));

        assertThrows(DuplicateRecordException.class, () -> service.salvar(material(null, "123", "Luva")));
        verify(materialDao, never()).salvar(any());
    }

    @Test
    void buscarPorCodigoEmBrancoNaoConsulta() {
        assertTrue(service.buscarPorCodigo(" ").isEmpty());
        verify(materialDao, never()).buscarPorCodigo(any());
    }

    @Test
    void obterOuCriarReaproveitaExistente() {
        Material existente = material(UUID.randomUUID(), "123", "Gaze");
        when(materialDao.buscarPorCodigo("123")).thenReturn(Optional.of(existente));

        assertSame(existente, service.obterOuCriar("123", "Outro nome"));
    }

    private static Material material(UUID id, String codigo, String nome) {
        Material material = new Material();
        material.setId(id);
        material.setCodigo(codigo);
        material.setNome(nome);
        return material;
    }
}
