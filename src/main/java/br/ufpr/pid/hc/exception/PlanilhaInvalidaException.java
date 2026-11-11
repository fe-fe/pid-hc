package br.ufpr.pid.hc.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class PlanilhaInvalidaException extends DomainException {

    @Getter
    @AllArgsConstructor
    public static class Erro implements Serializable {
        private final int linha;
        private final String coluna;
        private final String valor;
        private final String mensagem;
    }

    private final List<Erro> erros;

    public PlanilhaInvalidaException(List<Erro> erros) {
        super(erros.size() == 1
                ? "A planilha tem 1 erro. Nada foi importado."
                : "A planilha tem " + erros.size() + " erros. Nada foi importado.");
        this.erros = List.copyOf(erros);
    }
}
