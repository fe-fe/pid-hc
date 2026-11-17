package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.entity.Desvio;
import br.ufpr.pid.hc.entity.Fabricante;
import br.ufpr.pid.hc.entity.Marca;
import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.enumeration.Desfecho;
import br.ufpr.pid.hc.enumeration.DesvioOrdenacao;
import br.ufpr.pid.hc.enumeration.EnvioAmostra;
import br.ufpr.pid.hc.enumeration.GrauDano;
import br.ufpr.pid.hc.exception.DuplicateRecordException;
import br.ufpr.pid.hc.service.DesvioService;
import br.ufpr.pid.hc.service.FabricanteService;
import br.ufpr.pid.hc.service.MarcaService;
import br.ufpr.pid.hc.service.MaterialService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DesvioBeanTest {

    @Mock
    private DesvioService desvioService;

    @Mock
    private MaterialService materialService;

    @Mock
    private FabricanteService fabricanteService;

    @Mock
    private MarcaService marcaService;

    @InjectMocks
    private DesvioBean bean;

    private MockedStatic<Bootstrap> bootstrap;

    private final Material material = new Material();
    private final Desvio original = new Desvio();

    @BeforeEach
    void preparar() {
        bootstrap = mockStatic(Bootstrap.class);
        material.setCodigo("123");
        material.setNome("Material");
        Fabricante fabricante = new Fabricante();
        fabricante.setNome("FABRICANTE");
        Marca marca = new Marca();
        marca.setNome("MARCA");
        original.setId(UUID.randomUUID());
        original.setMaterial(material);
        original.setFabricante(fabricante);
        original.setMarca(marca);
        original.setVigihosp("VIG-1");
        original.setData(LocalDate.of(2026, 1, 1));
        original.setRegistroAnvisa("12345678901");
        original.setLote("LOTE-1");
        original.setValidade(LocalDate.of(2027, 1, 1));
        original.setFormaEntrada("VigiHosp");
        original.setAmostra(EnvioAmostra.FOTO);
        original.setServico("Serviço");
        original.setUnidade("Unidade");
        original.setMotivo("Motivo original");
        original.setProvidencias("Providências");
        original.setTecnicoUsep("Técnico");
        original.setDesfecho(Desfecho.QUEIXA_PONTUAL);
        original.setGrauDano(GrauDano.LEVE);
        original.setNotificacaoAnvisa("NOT-1");
        original.setProcessoSei("12345.123456/2026-01");
        when(materialService.buscar(any(Integer.class), any(Integer.class), any(), any(Boolean.class))).thenReturn(List.of(material));
        when(materialService.buscarPorCodigo(anyString())).thenReturn(Optional.empty());
        when(materialService.buscarPorCodigo("123")).thenReturn(Optional.of(material));
    }

    @AfterEach
    void fechar() {
        bootstrap.close();
    }

    @Test
    void pesquisarReiniciaPaginacaoEMantemFiltroNaProximaPagina() {
        when(desvioService.contarComFiltros("VIG-1", null, null, false)).thenReturn(16L);
        when(desvioService.buscarComFiltros(0, 15, DesvioOrdenacao.DATA, "VIG-1", null, null, false))
                .thenReturn(List.of(original));
        when(desvioService.buscarComFiltros(1, 15, DesvioOrdenacao.DATA, "VIG-1", null, null, false))
                .thenReturn(List.of(original));
        bean.setPaginaAtual(3);
        bean.setTermoPesquisa("  VIG-1  ");

        bean.pesquisar();

        assertEquals(0, bean.getPaginaAtual());
        assertEquals(16, bean.getTotalRegistros());
        assertEquals(List.of(original), bean.getDesvios());
        assertFalse(bean.isUltimaPagina());

        bean.proximaPagina();

        assertEquals(1, bean.getPaginaAtual());
        assertTrue(bean.isUltimaPagina());
        verify(desvioService).buscarComFiltros(1, 15, DesvioOrdenacao.DATA, "VIG-1", null, null, false);
    }

    @Test
    void limparPesquisaRemoveFiltroEReiniciaPaginacao() {
        bean.setTermoPesquisa("123");
        bean.pesquisar();
        bean.setPaginaAtual(2);
        when(desvioService.contarComFiltros(null, null, null, false)).thenReturn(1L);
        when(desvioService.buscarComFiltros(0, 15, DesvioOrdenacao.DATA, null, null, null, false))
                .thenReturn(List.of(original));
        bean.setTermoPesquisa(" ");

        bean.pesquisar();

        assertEquals(0, bean.getPaginaAtual());
        assertEquals(1, bean.getTotalRegistros());
        assertEquals(List.of(original), bean.getDesvios());
        verify(desvioService).contarComFiltros(null, null, null, false);
    }

    @Test
    void datasCombinamComPesquisaEPersistemAoPaginarETrocarCodigo() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        LocalDate fim = LocalDate.of(2026, 1, 31);
        bean.setTermoPesquisa("VIG-1");
        bean.pesquisar();
        bean.setDataInicial(inicio);
        bean.setDataFinal(fim);
        bean.setPaginaAtual(3);
        when(desvioService.contarComFiltros("VIG-1", inicio, fim, false)).thenReturn(16L);
        when(desvioService.buscarComFiltros(0, 15, DesvioOrdenacao.DATA, "VIG-1", inicio, fim, false))
                .thenReturn(List.of(original));
        when(desvioService.buscarComFiltros(1, 15, DesvioOrdenacao.DATA, "VIG-1", inicio, fim, false))
                .thenReturn(List.of(original));

        bean.aplicarFiltros();

        assertEquals(0, bean.getPaginaAtual());
        assertEquals(16, bean.getTotalRegistros());
        assertEquals(List.of(original), bean.getDesvios());
        bootstrap.verify(() -> Bootstrap.fecharOffcanvas("tecnovigilanciaFilters"));
        bean.proximaPagina();
        assertEquals(1, bean.getPaginaAtual());
        verify(desvioService).buscarComFiltros(1, 15, DesvioOrdenacao.DATA, "VIG-1", inicio, fim, false);

        bean.setTermoPesquisa("123");
        bean.pesquisar();
        verify(desvioService).buscarComFiltros(0, 15, DesvioOrdenacao.DATA, "123", inicio, fim, false);

        bean.setTermoPesquisa("");
        bean.pesquisar();
        verify(desvioService).buscarComFiltros(0, 15, DesvioOrdenacao.DATA, null, inicio, fim, false);
    }

    @Test
    void limparDatasPreservaPesquisaRapida() {
        bean.setTermoPesquisa("123");
        bean.pesquisar();
        bean.setDataInicial(LocalDate.of(2026, 1, 1));
        bean.setDataFinal(LocalDate.of(2026, 1, 31));
        bean.aplicarFiltros();
        bean.setPaginaAtual(2);

        bean.limparFiltros();

        assertNull(bean.getDataInicial());
        assertNull(bean.getDataFinal());
        assertNull(bean.getDataInicialFiltrada());
        assertNull(bean.getDataFinalFiltrada());
        assertEquals("123", bean.getTermoPesquisa());
        assertEquals("123", bean.getTermoFiltrado());
        assertEquals(0, bean.getPaginaAtual());
    }

    @Test
    void periodoInvalidoMantemFiltroAplicadoEModalAberta() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        bean.setDataInicial(inicio);
        bean.aplicarFiltros();
        bootstrap.clearInvocations();
        bean.setDataFinal(inicio.minusDays(1));
        bean.setPaginaAtual(2);
        FacesContext contexto = mock(FacesContext.class);

        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(contexto);
            bean.aplicarFiltros();
        }

        assertEquals(inicio, bean.getDataInicialFiltrada());
        assertNull(bean.getDataFinalFiltrada());
        assertEquals(2, bean.getPaginaAtual());
        ArgumentCaptor<FacesMessage> mensagem = ArgumentCaptor.forClass(FacesMessage.class);
        verify(contexto).addMessage(eq("formFiltrosTecnovigilancia:dataFinal"), mensagem.capture());
        assertEquals(FacesMessage.SEVERITY_ERROR, mensagem.getValue().getSeverity());
        verify(contexto).validationFailed();
        bootstrap.verify(() -> Bootstrap.fecharOffcanvas(any()), never());

        bean.setTermoPesquisa("123");
        bean.pesquisar();
        verify(desvioService).buscarComFiltros(0, 15, DesvioOrdenacao.DATA, "123", inicio, null, false);
    }

    @Test
    void datasEmEdicaoSoEntramNaPesquisaDepoisDeAplicar() {
        bean.setDataInicial(LocalDate.of(2026, 1, 1));
        bean.setDataFinal(LocalDate.of(2026, 1, 31));
        bean.setTermoPesquisa("123");

        bean.pesquisar();

        verify(desvioService).buscarComFiltros(0, 15, DesvioOrdenacao.DATA, "123", null, null, false);
    }

    @Test
    void editarAbreUmaCopiaECancelarNaoAlteraATabela() throws Exception {
        bean.editar(original);
        Desvio copia = bean.getEmEdicao();

        assertNotSame(original, copia);
        for (String nome : List.of("id", "material", "fabricante", "marca", "data", "vigihosp",
                "registroAnvisa", "lote", "validade", "formaEntrada", "amostra", "servico", "unidade",
                "motivo", "providencias", "tecnicoUsep", "desfecho", "grauDano", "notificacaoAnvisa", "processoSei")) {
            Field campo = Desvio.class.getDeclaredField(nome);
            campo.setAccessible(true);
            assertEquals(campo.get(original), campo.get(copia), nome);
        }
        assertEquals("123", bean.getCodigoMaterial());
        assertEquals("FABRICANTE", bean.getFabricante());
        bootstrap.verify(() -> Bootstrap.abrirModal("modalTecnovigilancia"));

        copia.setMotivo("Motivo alterado");
        bean.setFabricante("OUTRO");
        assertEquals("Motivo original", original.getMotivo());
        assertEquals("FABRICANTE", original.getFabricante().getNome());

        bean.editar(original);
        assertEquals("Motivo original", bean.getEmEdicao().getMotivo());
    }

    @Test
    void buscarMaterialAtualizaNomeSemMudarCadastro() {
        bean.editar(original);

        bean.setCodigoMaterial("novo");
        bean.buscarMaterial();
        assertFalse(bean.isMaterialExistente());
        assertNull(bean.getNomeMaterial());

        bean.setCodigoMaterial("123");
        bean.buscarMaterial();
        assertTrue(bean.isMaterialExistente());
        assertEquals("Material", bean.getNomeMaterial());
        assertEquals("123", original.getMaterial().getCodigo());
    }

    @Test
    void salvarFechaOModal() {
        bean.editar(original);

        bean.salvar();

        bootstrap.verify(() -> Bootstrap.fecharModal("modalTecnovigilancia"));
    }

    @Test
    void salvarComErroMantemOModalAberto() {
        bean.editar(original);
        when(desvioService.atualizar(eq(bean.getEmEdicao()), any(), any(), any(), any()))
                .thenThrow(new DuplicateRecordException("Já existe um registro com o VigiHosp VIG-1"));

        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(mock(FacesContext.class));
            bean.salvar();
        }

        bootstrap.verify(() -> Bootstrap.fecharModal(any()), never());
    }
}
