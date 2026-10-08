package br.ufpr.pid.hc.entity;

import br.ufpr.pid.hc.enumeration.Perfil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Usuario extends Auditavel {

    public Usuario() {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "cpf", updatable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Column(name = "perfil", nullable = false)
    private Perfil perfil;

}
