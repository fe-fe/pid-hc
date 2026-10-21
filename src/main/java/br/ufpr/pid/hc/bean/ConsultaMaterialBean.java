package br.ufpr.pid.hc.bean;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import br.ufpr.pid.hc.entity.Avaliacao;
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

    public void consultar() {
        material = materialService.buscarPorCodigo(codigo).orElse(null);

        avaliacoes = material == null ? List.of() : avaliacaoService.buscarAvaliacoesPorMaterial(codigo);
    }

    public Map<String, List<Avaliacao>> getAvaliacoesPorMarca() {
        if (avaliacoes == null || avaliacoes.isEmpty()) {
            return Collections.emptyMap();
        }

        return avaliacoes.stream().collect(
            Collectors.groupingBy(
            avaliacao -> avaliacao.getMarca() != null ? avaliacao.getMarca().getNome() : "Sem marca",
            TreeMap::new,
            Collectors.toList())
        );
    }

}
