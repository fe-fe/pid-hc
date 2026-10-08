package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum AvaliacaoOrdenacao implements CampoOrdenacao {
    MATERIAL("m.codigo, f.nome, ma.nome, a.data DESC", "Material"),
    FABRICANTE("f.nome, ma.nome, m.codigo, a.data DESC", "Fabricante"),
    DATA("a.data DESC, m.codigo", "Data (mais recente)");

    private final String campoBanco;
    private final String rotulo;

    AvaliacaoOrdenacao(String campoBanco, String rotulo) {
        this.campoBanco = campoBanco;
        this.rotulo = rotulo;
    }
}
