package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum EnvioAmostra {
    SIM("Sim"),
    NAO("Não"),
    FOTO("Foto");

    private final String rotulo;

    EnvioAmostra(String rotulo) {
        this.rotulo = rotulo;
    }
}
