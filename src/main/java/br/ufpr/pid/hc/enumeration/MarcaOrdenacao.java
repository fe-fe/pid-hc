package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum MarcaOrdenacao implements CampoOrdenacao {
    NOME("nome", "Nome"),
    CODIGO("codigo", "Código");

    private final String campoBanco;
    private final String rotulo;

    MarcaOrdenacao(String campoBanco, String rotulo) {
        this.campoBanco = campoBanco;
        this.rotulo = rotulo;
    }
}
