package br.ufpr.pid.hc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "anvisa_produto_saude", indexes = @Index(columnList = "numero_registro_cadastro"))
public class ProdutoAnvisa {

    public ProdutoAnvisa() {}

    @Id
    private UUID id;

    @Column(name = "numero_registro_cadastro", nullable = false)
    private String numeroRegistroCadastro;

    @Column(name = "numero_processo")
    private String numeroProcesso;

    @Column(name = "nome_tecnico", columnDefinition = "text")
    private String nomeTecnico;

    @Column(name = "classe_risco")
    private String classeRisco;

    @Column(name = "nome_comercial", columnDefinition = "text")
    private String nomeComercial;

    @Column(name = "cnpj_detentor_registro_cadastro")
    private String cnpjDetentorRegistroCadastro;

    @Column(name = "detentor_registro_cadastro", columnDefinition = "text")
    private String detentorRegistroCadastro;

    @Column(name = "nome_fabricante", columnDefinition = "text")
    private String nomeFabricante;

    @Column(name = "nome_pais_fabric")
    private String nomePaisFabric;

    @Column(name = "dt_pub_registro_cadastro")
    private String dtPubRegistroCadastro;

    @Column(name = "validade_registro_cadastro")
    private String validadeRegistroCadastro;

    @Column(name = "dt_atualizacao_dado")
    private String dtAtualizacaoDado;
}
