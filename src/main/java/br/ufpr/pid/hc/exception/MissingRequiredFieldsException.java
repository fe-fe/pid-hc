package br.ufpr.pid.hc.exception;

public class MissingRequiredFieldsException extends DomainException {
    public MissingRequiredFieldsException() {
        super("Todos os campos obrigatórios devem ser preenchidos");
    }
}
