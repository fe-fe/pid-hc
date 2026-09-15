package br.ufpr.pid.hc.exception;

import jakarta.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
