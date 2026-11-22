package br.ufpr.pid.hc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Getter
@Setter
@Entity
@Audited
public class Parametro {

    public static final String VALIDADE = "validade_avaliacao_meses";

    @Id
    private String chave;

    @Column(nullable = false)
    private String valor;
}
