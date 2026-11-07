package br.ufpr.pid.hc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
public class ImportacaoAnvisa {

    public ImportacaoAnvisa() {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String sha256;

    @Column(name = "dt_atualizacao_dado", nullable = false)
    private LocalDateTime dtAtualizacaoDado;

    @Column(name = "importado_em", nullable = false)
    private LocalDateTime importadoEm;
}
