package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.DesvioDao;
import br.ufpr.pid.hc.dao.ImportacaoTecnovigilanciaDao;
import br.ufpr.pid.hc.dao.MaterialDao;
import br.ufpr.pid.hc.entity.Desvio;
import br.ufpr.pid.hc.entity.Fabricante;
import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.ColunaTecnovigilancia;
import br.ufpr.pid.hc.exception.PlanilhaInvalidaException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static br.ufpr.pid.hc.enumeration.ColunaTecnovigilancia.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportacaoTecnovigilanciaServiceTest {

    @Mock
    private ImportacaoTecnovigilanciaDao importacaoTecnovigilanciaDao;

    @Mock
    private DesvioDao desvioDao;

    @Mock
    private MaterialDao materialDao;

    @Mock
    private FabricanteService fabricanteService;

    @Mock
    private MarcaService marcaService;

    @Mock
    private MaterialService materialService;

    @InjectMocks
    private ImportacaoTecnovigilanciaService service;

    @BeforeEach
    void prepararCadastros() {
        when(materialDao.buscar(anyInt(), anyInt(), any(), anyBoolean())).thenReturn(List.of());
        when(fabricanteService.listarNomes()).thenReturn(List.of("FAB X"));
        when(importacaoTecnovigilanciaDao.buscarPorSha256(anyString())).thenReturn(Optional.empty());
        when(desvioDao.listarVigihospExistentes(any())).thenReturn(List.of());
        when(materialService.obterOuCriar(anyString(), anyString())).thenAnswer(chamada -> {
            Material material = new Material();
            material.setCodigo(chamada.getArgument(0));
            material.setNome(chamada.getArgument(1));
            return material;
        });
        when(fabricanteService.obterOuCriar(anyString())).thenReturn(new Fabricante());
    }

    @Test
    void modeloDeixaAsColunasDeDataComoTexto() throws IOException {
        try (XSSFWorkbook modelo = abrir(service.gerarModelo())) {
            XSSFSheet desvios = modelo.getSheet("Desvios");
            for (ColunaTecnovigilancia coluna : List.of(DATA, VALIDADE)) {
                CellStyle estilo = desvios.getColumnStyle(coluna.ordinal());
                assertEquals("@", estilo.getDataFormatString(), coluna.getTitulo());
            }
        }
    }

    @Test
    void modeloNaoValidaDatasPeloExcel() throws IOException {
        try (XSSFWorkbook modelo = abrir(service.gerarModelo())) {
            for (DataValidation validacao : modelo.getSheet("Desvios").getDataValidations()) {
                assertTrue(validacao.getValidationConstraint().getValidationType() != ValidationType.DATE,
                        "validação de data em " + validacao.getRegions().getCellRangeAddress(0).formatAsString());
            }
        }
    }

    @Test
    void importaDatasDigitadasComUmOuDoisDigitos() throws IOException {
        List<Desvio> desvios = importar(
                linha("V1", Map.of(DATA, "1/2/2025", VALIDADE, "5/3/2027")),
                linha("V2", Map.of(DATA, "01/02/2025", VALIDADE, " 05/03/2027 ")));

        for (Desvio desvio : desvios) {
            assertEquals(LocalDate.of(2025, 2, 1), desvio.getData(), desvio.getVigihosp());
            assertEquals(LocalDate.of(2027, 3, 5), desvio.getValidade(), desvio.getVigihosp());
        }
    }

    @Test
    void importaDataColadaComoDataDoExcel() throws IOException {
        List<Desvio> desvios = importar(planilha -> {
            XSSFRow linha = preencher(planilha, 1, "V1", Map.of());
            CellStyle data = planilha.getWorkbook().createCellStyle();
            data.setDataFormat(planilha.getWorkbook().createDataFormat().getFormat("dd/mm/yyyy"));
            linha.createCell(DATA.ordinal()).setCellValue(LocalDate.of(2025, 2, 1));
            linha.getCell(DATA.ordinal()).setCellStyle(data);
        });

        assertEquals(LocalDate.of(2025, 2, 1), desvios.get(0).getData());
    }

    @Test
    void recusaDataQueNaoExiste() {
        PlanilhaInvalidaException erro = assertThrows(PlanilhaInvalidaException.class,
                () -> importar(linha("V1", Map.of(DATA, "31/02/2025"))));

        assertEquals("Data", erro.getErros().get(0).getColuna());
        assertEquals("31/02/2025", erro.getErros().get(0).getValor());
    }

    @Test
    void recusaDataNoFormatoAmericano() {
        PlanilhaInvalidaException erro = assertThrows(PlanilhaInvalidaException.class,
                () -> importar(linha("V1", Map.of(DATA, "2025-02-01"))));

        assertEquals("Data", erro.getErros().get(0).getColuna());
    }

    @Test
    void recusaDataFutura() {
        String amanha = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/uuuu"));

        PlanilhaInvalidaException erro = assertThrows(PlanilhaInvalidaException.class,
                () -> importar(linha("V1", Map.of(DATA, amanha))));

        assertEquals("A data não pode ser futura", erro.getErros().get(0).getMensagem());
    }

    @SafeVarargs
    private List<Desvio> importar(Consumer<XSSFSheet>... linhas) throws IOException {
        byte[] conteudo;
        try (XSSFWorkbook planilha = abrir(service.gerarModelo()); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            for (Consumer<XSSFSheet> linha : linhas) {
                linha.accept(planilha.getSheet("Desvios"));
            }
            planilha.write(saida);
            conteudo = saida.toByteArray();
        }

        service.importar("desvios.xlsx", conteudo, new Usuario());

        ArgumentCaptor<Desvio> salvos = ArgumentCaptor.forClass(Desvio.class);
        verify(desvioDao, times(linhas.length)).salvar(salvos.capture(), any());
        return salvos.getAllValues();
    }

    private static Consumer<XSSFSheet> linha(String vigihosp, Map<ColunaTecnovigilancia, String> valores) {
        return aba -> preencher(aba, aba.getLastRowNum() + 1, vigihosp, valores);
    }

    private static XSSFRow preencher(XSSFSheet aba, int numero, String vigihosp, Map<ColunaTecnovigilancia, String> valores) {
        XSSFRow linha = aba.createRow(numero);
        linha.createCell(VIGIHOSP.ordinal()).setCellValue(vigihosp);
        linha.createCell(CODIGO_MATERIAL.ordinal()).setCellValue("123");
        linha.createCell(MATERIAL.ordinal()).setCellValue("Gaze");
        linha.createCell(FABRICANTE.ordinal()).setCellValue("Fab X");
        linha.createCell(MOTIVO.ordinal()).setCellValue("Embalagem violada");
        valores.forEach((coluna, valor) -> linha.createCell(coluna.ordinal()).setCellValue(valor));
        return linha;
    }

    private static XSSFWorkbook abrir(byte[] conteudo) throws IOException {
        return new XSSFWorkbook(new ByteArrayInputStream(conteudo));
    }
}
