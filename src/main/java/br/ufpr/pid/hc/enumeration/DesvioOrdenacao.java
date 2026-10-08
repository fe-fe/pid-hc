package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum DesvioOrdenacao implements CampoOrdenacao {
    DATA("data DESC", "Data"),
    VIGIHOSP("vigihosp", "VigiHosp");

    private final String campoBanco;
    private final String rotulo;

    DesvioOrdenacao(String campoBanco, String rotulo) {
        this.campoBanco = campoBanco;
        this.rotulo = rotulo;
    }
}
