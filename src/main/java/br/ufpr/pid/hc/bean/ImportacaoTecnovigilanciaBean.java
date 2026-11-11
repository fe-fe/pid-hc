package br.ufpr.pid.hc.bean;

import br.ufpr.pid.hc.exception.DomainException;
import br.ufpr.pid.hc.exception.PlanilhaInvalidaException;
import br.ufpr.pid.hc.service.ImportacaoTecnovigilanciaService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.omnifaces.util.Faces;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Named
@ViewScoped
@Getter
@Setter
public class ImportacaoTecnovigilanciaBean implements Serializable {

    private static final long TAMANHO_MAXIMO = 10L * 1024 * 1024;
    private static final int MAXIMO_ERROS_EXIBIDOS = 200;
    private static final int MAXIMO_CODIGOS_EXIBIDOS = 10;

    @Inject
    private ImportacaoTecnovigilanciaService importacaoTecnovigilanciaService;

    @Inject
    private Sessao sessao;

    private transient Part arquivo;
    private List<PlanilhaInvalidaException.Erro> erros = List.of();
    private Integer importados;

    public void baixarModelo() throws IOException {
        Faces.sendFile(importacaoTecnovigilanciaService.gerarModelo(),
                "modelo-tecnovigilancia-" + LocalDate.now() + ".xlsx", true);
    }

    public void importar() {
        erros = List.of();
        importados = null;
        if (arquivo == null || arquivo.getSize() == 0) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Selecione a planilha antes de importar.");
            return;
        }
        String nome = arquivo.getSubmittedFileName();
        if (nome == null || !nome.toLowerCase().endsWith(".xlsx")) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Envie um arquivo .xlsx gerado a partir do modelo.");
            return;
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "O arquivo passa de 10 MB.");
            return;
        }

        try (InputStream entrada = arquivo.getInputStream()) {
            ImportacaoTecnovigilanciaService.Resultado resultado = importacaoTecnovigilanciaService
                    .importar(nome, entrada.readAllBytes(), sessao.getUsuarioLogado());
            importados = resultado.importados();
            adicionarMensagem(FacesMessage.SEVERITY_INFO, switch (importados) {
                case 0 -> "Nenhum desvio novo para importar.";
                case 1 -> "1 desvio importado.";
                default -> importados + " desvios importados.";
            });
            List<String> ignorados = resultado.vigihospIgnorados();
            if (!ignorados.isEmpty()) {
                adicionarMensagem(FacesMessage.SEVERITY_WARN, (ignorados.size() == 1
                        ? "1 linha foi ignorada porque o VigiHosp já existe no PID: "
                        : ignorados.size() + " linhas foram ignoradas porque o VigiHosp já existe no PID: ")
                        + resumirCodigos(ignorados) + ".");
            }
            List<String> novos = resultado.materiaisNovos();
            if (!novos.isEmpty()) {
                adicionarMensagem(FacesMessage.SEVERITY_WARN, (novos.size() == 1
                        ? "1 material ainda não estava no PID e foi cadastrado"
                        : novos.size() + " materiais ainda não estavam no PID e foram cadastrados")
                        + " com o nome da planilha: " + resumirCodigos(novos) + ".");
            }
            List<String> divergentes = resultado.materiaisDivergentes();
            if (!divergentes.isEmpty()) {
                adicionarMensagem(FacesMessage.SEVERITY_WARN, (divergentes.size() == 1
                        ? "1 código de material tem"
                        : divergentes.size() + " códigos de material têm")
                        + " nome diferente do cadastro; foi mantido o nome do cadastro: "
                        + resumirCodigos(divergentes) + ".");
            }
        } catch (PlanilhaInvalidaException e) {
            erros = e.getErros();
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, e.getMessage());
        } catch (DomainException e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, e.getMessage());
        } catch (IOException | RuntimeException e) {
            log.error("Erro ao importar planilha de tecnovigilância", e);
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Não foi possível importar a planilha.");
        } finally {
            arquivo = null;
        }
    }

    public List<PlanilhaInvalidaException.Erro> getErrosExibidos() {
        return erros.size() > MAXIMO_ERROS_EXIBIDOS ? erros.subList(0, MAXIMO_ERROS_EXIBIDOS) : erros;
    }

    public int getErrosOcultos() {
        return Math.max(erros.size() - MAXIMO_ERROS_EXIBIDOS, 0);
    }

    private String resumirCodigos(List<String> codigos) {
        return codigos.size() > MAXIMO_CODIGOS_EXIBIDOS
                ? String.join(", ", codigos.subList(0, MAXIMO_CODIGOS_EXIBIDOS))
                        + " e mais " + (codigos.size() - MAXIMO_CODIGOS_EXIBIDOS)
                : String.join(", ", codigos);
    }

    private void adicionarMensagem(FacesMessage.Severity severidade, String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidade, mensagem, null));
    }
}
