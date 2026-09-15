package br.ufpr.pid.hc.exception;

public class DuplicateEmailException extends DomainException {
    public DuplicateEmailException() {
        super("Email já foi cadastrado");
    }
}
