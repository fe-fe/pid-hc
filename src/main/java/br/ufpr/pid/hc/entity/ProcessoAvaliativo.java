package br.ufpr.pid.hc.entity;

import br.ufpr.pid.hc.enumeration.EstadoProcesso;
import br.ufpr.pid.hc.enumeration.ResultadoTecnico;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
public class ProcessoAvaliativo extends Auditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime dataAbertura;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "marca_id", updatable = false, nullable = false)
    private Marca marca;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "material_id", updatable = false, nullable = false)
    private Material material;

    @ManyToMany
    @JoinTable(
            name = "processo_setor",
            joinColumns = @JoinColumn(name = "processo_id"),
            inverseJoinColumns = @JoinColumn(name = "setor_id")
    )
    private Set<Setor> setores;

    private ResultadoTecnico resultadoTecnico;
    private EstadoProcesso estadoProcesso;
    private String parecer;

}
