package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum NomeOrdenacao implements CampoOrdenacao {
    NOME("nome", "Nome");

    private final String campoBanco;
    private final String rotulo;

    NomeOrdenacao(String campoBanco, String rotulo) {
        this.campoBanco = campoBanco;
        this.rotulo = rotulo;
    }
}
