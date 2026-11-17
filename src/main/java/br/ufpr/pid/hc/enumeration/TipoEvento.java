package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum TipoEvento {
    PLANILHA("Importação de planilha", "text-bg-primary"),
    ANVISA("Atualização da ANVISA", "text-bg-info"),
    AVALIACAO_CRIADA("Avaliação criada", "text-bg-success"),
    AVALIACAO_EDITADA("Avaliação editada", "text-bg-warning");

    private final String rotulo;
    private final String classe;

    TipoEvento(String rotulo, String classe) {
        this.rotulo = rotulo;
        this.classe = classe;
    }
}
