package br.ufpr.pid.hc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class Auditavel {

    @Column(name = "ativo")
    private Boolean ativo;

    @PrePersist
    protected void onCreate() {
        if (this.ativo == null) {
            this.ativo = true;
        }
    }

    // registros antigos podem ter ativo = null; só conta como inativo se foi desativado explicitamente
    public boolean isInativo() {
        return Boolean.FALSE.equals(ativo);
    }
}
