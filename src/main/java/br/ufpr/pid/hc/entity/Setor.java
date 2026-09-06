package br.ufpr.pid.hc.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
public class Setor extends Auditavel {

    public Setor() {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String codigo;

    @Column
    private String unidade;

    @Column
    private String unidadeGestora;

    @Column
    private String centroAtividades;

    @Column
    private String predio;

    @Column
    private String andar;

    @Column
    private Float areaMetrosQuadrados;

    @Column
    private String chefia;
}
