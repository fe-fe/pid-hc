package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum GrauDano {
    AUSENTE("Ausente"),
    POTENCIAL("Dano potencial"),
    LEVE("Leve"),
    MODERADO("Moderado"),
    GRAVE("Grave"),
    OBITO("Óbito");

    private final String rotulo;

    GrauDano(String rotulo) {
        this.rotulo = rotulo;
    }
}
