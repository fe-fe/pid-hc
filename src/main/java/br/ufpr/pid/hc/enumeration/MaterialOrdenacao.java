package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum MaterialOrdenacao implements CampoOrdenacao {
    CODIGO("codigo","Código"),
    NOME("nome", "Nome"),
    CATEGORIA("categoria_id", "Categoria");

    private final String campoBanco;
    private final String rotulo;

    MaterialOrdenacao(String campoBanco, String rotulo) {
        this.campoBanco = campoBanco;
        this.rotulo = rotulo;
    }
}
