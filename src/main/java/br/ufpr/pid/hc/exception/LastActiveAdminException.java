package br.ufpr.pid.hc.exception;

public class LastActiveAdminException extends DomainException {
    public LastActiveAdminException() {
        super("Não é possível desativar ou alterar o perfil do último administrador ativo");
    }
}
