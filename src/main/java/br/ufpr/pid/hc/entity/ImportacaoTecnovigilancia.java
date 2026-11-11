package br.ufpr.pid.hc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "importacao_tecnovigilancia")
public class ImportacaoTecnovigilancia extends Auditavel {

    public ImportacaoTecnovigilancia() {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(nullable = false, unique = true, length = 64)
    private String sha256;

    @Column(name = "total_registros", nullable = false)
    private Integer totalRegistros;
}
