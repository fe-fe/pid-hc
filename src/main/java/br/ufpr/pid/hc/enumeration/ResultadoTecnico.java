package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum ResultadoTecnico {
    APROVADO("Aprovado"),
    REPROVADO("Reprovado");

    private final String rotulo;

    ResultadoTecnico(String rotulo) {
        this.rotulo = rotulo;
    }
}
