package br.ufpr.pid.hc.exception;

public class DuplicateRecordException extends DomainException {
    public DuplicateRecordException(String message) {
        super(message);
    }
}
