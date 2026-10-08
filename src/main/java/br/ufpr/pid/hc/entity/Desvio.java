package br.ufpr.pid.hc.entity;

import br.ufpr.pid.hc.enumeration.Desfecho;
import br.ufpr.pid.hc.enumeration.EnvioAmostra;
import br.ufpr.pid.hc.enumeration.FormaEntrada;
import br.ufpr.pid.hc.enumeration.GrauDano;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Desvio extends Auditavel {

    public Desvio() {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fabricante_id", nullable = false)
    private Fabricante fabricante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "marca_id")
    private Marca marca;

    private LocalDate data;
    private String vigihosp;
    private String registroAnvisa;
    private String lote;
    private LocalDate validade;

    @Enumerated(EnumType.STRING)
    private FormaEntrada formaEntrada;

    @Enumerated(EnumType.STRING)
    private EnvioAmostra amostra;

    private String servico;
    private String unidade;

    @Column(columnDefinition = "text")
    private String motivo;

    @Column(columnDefinition = "text")
    private String providencias;

    private String tecnicoUsep;

    @Enumerated(EnumType.STRING)
    private Desfecho desfecho;

    @Enumerated(EnumType.STRING)
    private GrauDano grauDano;

    private String notificacaoAnvisa;
    private String processoSei;
}
