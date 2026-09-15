package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum SetorOrdenacao implements CampoOrdenacao {
    CODIGO("codigo","Código"),
    UNIDADE("unidade", "Unidade");

    private final String campoBanco;
    private final String rotulo;

    SetorOrdenacao(String campoBanco, String rotulo) {
        this.campoBanco = campoBanco;
        this.rotulo = rotulo;
    }
}