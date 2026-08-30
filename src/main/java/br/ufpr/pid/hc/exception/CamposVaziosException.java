package br.ufpr.pid.hc.exception;

public class CamposVaziosException extends RuntimeException {
    public CamposVaziosException() {
        super("Ambos os campos devem ser preenchidos");
    }
}
