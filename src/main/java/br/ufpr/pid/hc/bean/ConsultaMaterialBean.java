package br.ufpr.pid.hc.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import br.ufpr.pid.hc.entity.Avaliacao;
import br.ufpr.pid.hc.entity.Fabricante;
import br.ufpr.pid.hc.entity.Marca;
import br.ufpr.pid.hc.entity.Material;
import br.ufpr.pid.hc.service.AvaliacaoService;
import br.ufpr.pid.hc.service.MaterialService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@ViewScoped
@Getter
@Setter
public class ConsultaMaterialBean implements Serializable {

    @Inject
    private AvaliacaoService avaliacaoService;

    @Inject
    private MaterialService materialService;

    private static final long serialVersionUID = 1L;

    private String codigo;
    private Material material;

    private List<Avaliacao> avaliacoes = List.of();

    private String termoFabricante;
    private List<Fabricante> fabricantesEncontrados = List.of();
    private List<Fabricante> fabricantesSelecionados = new ArrayList<>();
    private List<UUID> fabricantesFiltrados = List.of();
    private String termoMarca;
    private List<Marca> marcasEncontradas = List.of();
    private List<Marca> marcasSelecionadas = new ArrayList<>();
    private List<UUID> marcasFiltradas = List.of();

    public void consultar() {
        material = materialService.buscarPorCodigo(codigo).orElse(null);

        avaliacoes = material == null ? List.of() : avaliacaoService.buscarAvaliacoesPorMaterial(codigo);
    }

    public Map<String, List<Avaliacao>> getAvaliacoesPorMarca() {
        if (avaliacoes == null || avaliacoes.isEmpty()) {
            return Collections.emptyMap();
        }

        return avaliacoes.stream()
            .filter(avaliacao -> fabricantesFiltrados.isEmpty()
                || (avaliacao.getFabricante() != null
                    && fabricantesFiltrados.contains(avaliacao.getFabricante().getId())))
            .filter(avaliacao -> marcasFiltradas.isEmpty()
                || (avaliacao.getMarca() != null && marcasFiltradas.contains(avaliacao.getMarca().getId())))
            .collect(
            Collectors.groupingBy(
            avaliacao -> avaliacao.getMarca() != null ? avaliacao.getMarca().getNome() : "Sem marca",
            TreeMap::new,
            Collectors.toList())
        );
    }

    public void buscarFabricantes() {
        fabricantesEncontrados = isBuscaFabricanteValida()
                ? avaliacaoService.buscarFabricantesPorNome(termoFabricante.trim())
                : List.of();
    }

    public boolean isBuscaFabricanteValida() {
        return termoFabricante != null && termoFabricante.trim().length() >= 3;
    }

    public List<Fabricante> getSugestoesFabricantesFiltro() {
        return fabricantesEncontrados.stream()
                .filter(opcao -> fabricantesSelecionados.stream()
                        .noneMatch(selecionado -> selecionado.getId().equals(opcao.getId())))
                .toList();
    }

    public void adicionarFabricante(Fabricante fabricanteSelecionado) {
        if (fabricanteSelecionado != null && fabricantesSelecionados.stream()
                .noneMatch(selecionado -> selecionado.getId().equals(fabricanteSelecionado.getId()))) {
            fabricantesSelecionados.add(fabricanteSelecionado);
        }
    }

    public void removerFabricante(Fabricante fabricanteSelecionado) {
        fabricantesSelecionados.removeIf(selecionado -> selecionado.getId().equals(fabricanteSelecionado.getId()));
    }

    public void buscarMarcas() {
        marcasEncontradas = isBuscaMarcaValida()
                ? avaliacaoService.buscarMarcasPorNome(termoMarca.trim())
                : List.of();
    }

    public boolean isBuscaMarcaValida() {
        return termoMarca != null && termoMarca.trim().length() >= 3;
    }

    public List<Marca> getSugestoesMarcasFiltro() {
        return marcasEncontradas.stream()
                .filter(opcao -> marcasSelecionadas.stream()
                        .noneMatch(selecionada -> selecionada.getId().equals(opcao.getId())))
                .toList();
    }

    public void adicionarMarca(Marca marcaSelecionada) {
        if (marcaSelecionada != null && marcasSelecionadas.stream()
                .noneMatch(selecionada -> selecionada.getId().equals(marcaSelecionada.getId()))) {
            marcasSelecionadas.add(marcaSelecionada);
        }
    }

    public void removerMarca(Marca marcaSelecionada) {
        marcasSelecionadas.removeIf(selecionada -> selecionada.getId().equals(marcaSelecionada.getId()));
    }

    public void aplicarFiltros() {
        fabricantesFiltrados = fabricantesSelecionados.stream().map(Fabricante::getId).toList();
        marcasFiltradas = marcasSelecionadas.stream().map(Marca::getId).toList();
    }

    public void limparFiltros() {
        termoFabricante = null;
        fabricantesEncontrados = List.of();
        fabricantesSelecionados = new ArrayList<>();
        fabricantesFiltrados = List.of();
        termoMarca = null;
        marcasEncontradas = List.of();
        marcasSelecionadas = new ArrayList<>();
        marcasFiltradas = List.of();
    }

}
