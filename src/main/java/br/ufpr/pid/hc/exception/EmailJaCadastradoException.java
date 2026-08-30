package br.ufpr.pid.hc.exception;

public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException() {
        super("Email já foi cadastrado");
    }
}
