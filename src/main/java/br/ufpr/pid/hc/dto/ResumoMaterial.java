package br.ufpr.pid.hc.dto;

import java.io.Serializable;

public record ResumoMaterial(long avaliacoes, long aprovadas, long reprovadas,
        long notificacoes) implements Serializable {
    public static final ResumoMaterial VAZIO = new ResumoMaterial(0, 0, 0, 0);
}
