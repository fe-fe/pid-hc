package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.DesvioDao;
import br.ufpr.pid.hc.entity.*;
import br.ufpr.pid.hc.enumeration.*;
import br.ufpr.pid.hc.exception.DomainException;
import br.ufpr.pid.hc.exception.DuplicateRecordException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DesvioServiceTest {
    private final Material material = new Material();
    private final Fabricante fabricante = new Fabricante();
    private final Marca marca = new Marca();
    private final DaoRegistro dao = new DaoRegistro();
    private final DesvioService service = new DesvioService();
    private final MaterialService materiais = new MaterialService() {
        @Override
        public List<Material> buscar(int pagina, int tamanho, CampoOrdenacao ordem, boolean inativos) {
            return List.of(material);
        }

        @Override
        public Optional<Material> buscarPorCodigo(String codigo) {
            return material.getCodigo().equals(codigo) ? Optional.of(material) : Optional.empty();
        }

        @Override
        public Material obterOuCriar(String codigo, String nome) { return material; }
    };
    private final FabricanteService fabricantes = new FabricanteService() {
        @Override
        public List<String> listarNomes() { return List.of(fabricante.getNome()); }

        @Override
        public Fabricante obterOuCriar(String nome) { return fabricante; }
    };
    private final MarcaService marcas = new MarcaService() {
        @Override
        public List<String> listarNomes() { return List.of(marca.getNome()); }

        @Override
        public Marca obterOuCriar(String nome) { return marca; }
    };

    @BeforeEach
    void setUp() throws Exception {
        material.setCodigo("123");
        material.setNome("Material");
        fabricante.setNome("FABRICANTE");
        marca.setNome("MARCA");
        injetar("desvioDao", dao);
        injetar("materialService", materiais);
        injetar("fabricanteService", fabricantes);
        injetar("marcaService", marcas);
        dao.existente = registro();
        dao.existente.setImportacao(new ImportacaoTecnovigilancia());
        dao.existente.setAtivo(true);
    }

    private void injetar(String nome, Object valor) throws Exception {
        Field campo = DesvioService.class.getDeclaredField(nome);
        campo.setAccessible(true);
        campo.set(service, valor);
    }

    private Desvio registro() {
        Desvio desvio = new Desvio();
        desvio.setId(UUID.randomUUID());
        desvio.setMaterial(material);
        desvio.setFabricante(fabricante);
        desvio.setMarca(marca);
        desvio.setVigihosp("VIG-1");
        desvio.setData(LocalDate.of(2026, 1, 1));
        desvio.setRegistroAnvisa("12345678901");
        desvio.setLote("LOTE-1");
        desvio.setValidade(LocalDate.of(2027, 1, 1));
        desvio.setFormaEntrada("VigiHosp");
        desvio.setAmostra(EnvioAmostra.FOTO);
        desvio.setServico("Serviço");
        desvio.setUnidade("Unidade");
        desvio.setMotivo("Motivo original");
        desvio.setProvidencias("Providências");
        desvio.setTecnicoUsep("Técnico");
        desvio.setDesfecho(Desfecho.QUEIXA_PONTUAL);
        desvio.setGrauDano(GrauDano.LEVE);
        desvio.setNotificacaoAnvisa("NOT-1");
        desvio.setProcessoSei("12345.123456/2026-01");
        return desvio;
    }

    @Test
    void salvarMantemIdEOrigem() {
        Desvio edicao = registro();
        edicao.setId(dao.existente.getId());
        edicao.setMotivo("Motivo atualizado");
        edicao.setVigihosp(" VIG-1 ");
        Desvio salva = service.atualizar(edicao, "123", "Material", "FABRICANTE", "MARCA");
        assertSame(edicao, salva);
        assertEquals(dao.existente.getId(), salva.getId());
        assertEquals("VIG-1", salva.getVigihosp());
        assertEquals("Motivo atualizado", dao.salvo.getMotivo());
        assertEquals("Motivo original", dao.existente.getMotivo());
        assertSame(dao.existente.getImportacao(), salva.getImportacao());
        assertEquals(dao.existente.getAtivo(), salva.getAtivo());
        assertEquals(dao.existente.getId(), dao.idExcluido);
    }

    @Test
    void camposOpcionaisPodemFicarVazios() {
        Desvio edicao = registro();
        edicao.setRegistroAnvisa(null);
        edicao.setProcessoSei("");
        edicao.setAmostra(null);
        edicao.setDesfecho(null);
        edicao.setGrauDano(null);
        service.atualizar(edicao, "123", "Material", "FABRICANTE", "");
        assertNull(dao.salvo.getMarca());
        assertNull(dao.salvo.getDesfecho());
        assertNull(dao.salvo.getAmostra());
        assertNull(dao.salvo.getGrauDano());
    }

    @Test
    void rejeitaVigihospDuplicado() {
        dao.duplicado = true;
        try {
            service.atualizar(registro(), "123", "Material", "FABRICANTE", null);
            fail("Deveria rejeitar VigiHosp de outro registro");
        } catch (DuplicateRecordException esperado) {
            assertNull(dao.salvo);
        }
    }

    @Test
    void rejeitaRegistroSemIdOuRemovido() {
        Desvio edicao = registro();
        edicao.setId(null);
        rejeitado(edicao);
        dao.existente = null;
        rejeitado(registro());
    }

    @Test
    void rejeitaCamposObrigatoriosVazios() {
        Desvio edicao = registro();
        edicao.setMotivo(" ");
        rejeitado(edicao);
        edicao = registro();
        edicao.setVigihosp(" ");
        rejeitado(edicao);
        edicao = registro();
        edicao.setData(null);
        rejeitado(edicao);
    }

    @Test
    void rejeitaDataFuturaEFormatosInvalidos() {
        Desvio edicao = registro();
        edicao.setData(LocalDate.now().plusDays(1));
        rejeitado(edicao);
        edicao = registro();
        edicao.setRegistroAnvisa("123");
        rejeitado(edicao);
        edicao = registro();
        edicao.setProcessoSei("123");
        rejeitado(edicao);
    }

    private void rejeitado(Desvio desvio) {
        try {
            service.atualizar(desvio, "123", "Material", "FABRICANTE", null);
            fail("Deveria rejeitar dados inválidos");
        } catch (DomainException esperado) {
            assertNull(dao.salvo);
        }
    }

    private static class DaoRegistro extends DesvioDao {
        Desvio existente;
        Desvio salvo;
        UUID idExcluido;
        boolean duplicado;

        @Override
        public Desvio buscarPorId(UUID id) { return existente; }

        @Override
        public boolean existeOutroVigihosp(String vigihosp, UUID id) {
            idExcluido = id;
            return duplicado;
        }

        @Override
        public Desvio salvar(Desvio desvio) {
            salvo = desvio;
            return desvio;
        }
    }
}
