package br.ufpr.pid.hc.exception;

public class SenhaCurtaException extends RuntimeException {
    public SenhaCurtaException() {
        super("A senha deve ter mais que 5 caracteres");
    }
}
