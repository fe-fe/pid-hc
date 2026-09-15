package br.ufpr.pid.hc.exception;

public class WeakPasswordException extends DomainException {
    public WeakPasswordException() {
        super("Senha fraca");
    }
}
