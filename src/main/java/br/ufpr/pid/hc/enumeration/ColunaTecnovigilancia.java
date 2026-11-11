package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum ColunaTecnovigilancia {
    VIGIHOSP("Nº VigiHosp", true, Tipo.TEXTO, 16,
            "Número da notificação no VigiHosp. Não pode se repetir na planilha; se já existir no PID, a linha é ignorada."),
    DATA("Data", true, Tipo.DATA, 13,
            "Data da notificação, no formato dd/mm/aaaa. Não pode ser futura."),
    CODIGO_MATERIAL("Código do material", true, Tipo.LISTA, 20,
            "Escolha um código da lista ou digite outro. Se ainda não existir no PID, é cadastrado com o nome da coluna Material."),
    MATERIAL("Material", true, Tipo.LISTA, 40,
            "Escolha um nome da lista ou digite outro. Se o código já existe no PID com outro nome, vale o nome do cadastro."),
    FABRICANTE("Fabricante", true, Tipo.LISTA, 32,
            "Escolha um nome da lista ou digite outro, de preferência como consta no registro ANVISA."),
    MARCA("Marca", false, Tipo.TEXTO, 28,
            "Nome comercial do produto, de preferência como consta no registro ANVISA. Pode ficar em branco."),
    REGISTRO_ANVISA("Registro ANVISA", false, Tipo.TEXTO, 17,
            "11 dígitos, só números, sem pontos ou traços."),
    LOTE("Lote", false, Tipo.TEXTO, 14, "Lote do produto."),
    VALIDADE("Validade", false, Tipo.DATA, 13, "Validade do lote, no formato dd/mm/aaaa."),
    FORMA_ENTRADA("Forma de entrada", false, Tipo.TEXTO, 24, "Forma de entrada do produto, em texto livre."),
    AMOSTRA("Envio de amostra", false, Tipo.LISTA, 16, "Uma das opções da lista."),
    SERVICO("Serviço", false, Tipo.TEXTO, 24, "Serviço que notificou."),
    UNIDADE("Unidade", false, Tipo.TEXTO, 24, "Unidade que notificou."),
    MOTIVO("Motivo", true, Tipo.TEXTO_LONGO, 48, "Descrição do desvio de qualidade."),
    PROVIDENCIAS("Providências", false, Tipo.TEXTO_LONGO, 48, "Providências tomadas."),
    TECNICO_USEP("Técnico USEP", false, Tipo.TEXTO, 22, "Técnico responsável pela análise."),
    DESFECHO("Desfecho", false, Tipo.LISTA, 44, "Uma das opções da lista."),
    GRAU_DANO("Grau de dano", false, Tipo.LISTA, 16, "Uma das opções da lista."),
    NOTIFICACAO_ANVISA("Notificação ANVISA", false, Tipo.TEXTO, 20, "Número da notificação no Notivisa."),
    PROCESSO_SEI("Processo SEI", false, Tipo.TEXTO, 24, "No formato 00000.000000/0000-00.");

    public enum Tipo { TEXTO, TEXTO_LONGO, DATA, LISTA }

    private final String titulo;
    private final boolean obrigatoria;
    private final Tipo tipo;
    private final int largura;
    private final String regra;

    ColunaTecnovigilancia(String titulo, boolean obrigatoria, Tipo tipo, int largura, String regra) {
        this.titulo = titulo;
        this.obrigatoria = obrigatoria;
        this.tipo = tipo;
        this.largura = largura;
        this.regra = regra;
    }
}
