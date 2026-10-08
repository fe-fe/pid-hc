package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum FormaEntrada {
    COMPRA_LOCAL("Compra local"),
    COMPRA_CENTRALIZADA("Compra centralizada"),
    DOACAO("Doação"),
    EMPRESTIMO("Empréstimo"),
    SEM_REGISTRO("Sem registro de entrada");

    private final String rotulo;

    FormaEntrada(String rotulo) {
        this.rotulo = rotulo;
    }
}
