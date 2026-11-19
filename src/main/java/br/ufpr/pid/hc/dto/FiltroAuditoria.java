package br.ufpr.pid.hc.dto;

import br.ufpr.pid.hc.enumeration.TipoEvento;

import java.io.Serializable;
import java.time.LocalDate;

public record FiltroAuditoria(TipoEvento tipo, String usuario, LocalDate inicio, LocalDate fim) implements Serializable {
    public static final FiltroAuditoria VAZIO = new FiltroAuditoria(null, null, null, null);
}
