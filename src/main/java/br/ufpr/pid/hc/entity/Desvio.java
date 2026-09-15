package br.ufpr.pid.hc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Desvio extends Auditavel{

    public Desvio() {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "marca_id", updatable = false, nullable = false)
    private Marca marca;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "material_id", updatable = false, nullable = false)
    private Marca material;

    private Integer codigoVigihosp;
    private LocalDate dataOcorrido;
    private String lote;
    private String amostra;
    private String servicoNotificador;
    private Integer severidadeDano;
    private String parecer;
    private String tecnicoUsep;
    private String notificacaoAnvisa;
    private String processoSei;
}
