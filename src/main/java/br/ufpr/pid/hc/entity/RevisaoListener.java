package br.ufpr.pid.hc.entity;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.security.enterprise.SecurityContext;
import org.hibernate.envers.RevisionListener;

import java.security.Principal;

public class RevisaoListener implements RevisionListener {

    private static final String ANONIMO = "anonymous";

    @Override
    public void newRevision(Object revisao) {
        ((Revisao) revisao).setUsuario(usuarioLogado());
    }

    private static String usuarioLogado() {
        try {
            Principal principal = CDI.current().select(SecurityContext.class).get().getCallerPrincipal();
            return principal == null || ANONIMO.equals(principal.getName()) ? null : principal.getName();
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
