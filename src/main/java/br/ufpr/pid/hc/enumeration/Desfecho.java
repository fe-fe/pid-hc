package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum Desfecho {
    QUEIXA_PONTUAL("Queixa pontual"),
    ESCLARECIMENTO("Esclarecimento usuários e/ou serviços envolvidos"),
    INCONCLUSIVO("Inconclusivo ou descartada queixa técnica"),
    DESVIO_FINALIDADE("Desvio de finalidade"),
    SUGESTAO_TROCA_LOTE("Sugestão de troca de lote"),
    SUGESTAO_REPROVACAO_MARCA("Sugestão de reprovação marca"),
    SUGESTAO_REVISAO_DESCRITIVO("Sugestão de revisão descritivo insumo"),
    SUGESTAO_REPOSICAO("Sugestão de reposição do item pelo fornecedor"),
    SUGESTAO_RECOLHIMENTO("Sugestão de recolhimento do produto pelo SAFS"),
    SUGESTAO_CAPACITACAO("Sugestão de capacitação usuários");

    private final String rotulo;

    Desfecho(String rotulo) {
        this.rotulo = rotulo;
    }
}
