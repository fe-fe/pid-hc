package br.ufpr.pid.hc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public class Auditavel {

    @Column(name = "ativo")
    private Boolean ativo;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", updatable = true)
    private LocalDateTime atualizadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por", updatable = true) // true placeholder
    private Usuario criadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atualizado_por", updatable = true)
    private Usuario atualizadoPor;

    @PrePersist
    protected void onCreate() {
        LocalDateTime tempo = LocalDateTime.now();
        this.criadoEm = tempo;
        this.atualizadoEm = tempo;
        if (this.ativo == null) {
            this.ativo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    // registros antigos podem ter ativo = null; só conta como inativo se foi desativado explicitamente
    public boolean isInativo() {
        return Boolean.FALSE.equals(ativo);
    }
}
