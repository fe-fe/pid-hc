package br.ufpr.pid.hc.entity;

import br.ufpr.pid.hc.enumeration.ResultadoTecnico;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Avaliacao extends Auditavel {

    public Avaliacao() {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "processo_avaliativo_id", updatable = false, nullable = false)
    private ProcessoAvaliativo processoAvaliativo;

    @ManyToOne
    @JoinColumn(name = "setor_id", updatable = false, nullable = false)
    private Setor setor;

    private ResultadoTecnico resultadoTecnico;
    private LocalDate data;
    private String observacoes;
}
