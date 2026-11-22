package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum TipoEvento {
    LOGIN("Login", "text-bg-secondary"),
    LOGIN_RECUSADO("Login recusado", "text-bg-danger"),
    LOGOUT("Logout", "text-bg-secondary"),
    USUARIO_CRIADO("Usuário criado", "text-bg-success"),
    USUARIO_EDITADO("Usuário editado", "text-bg-warning"),
    USUARIO_DESATIVADO("Usuário desativado", "text-bg-danger"),
    USUARIO_REATIVADO("Usuário reativado", "text-bg-success"),
    CADASTRO_CRIADO("Cadastro criado", "text-bg-success"),
    CADASTRO_EDITADO("Cadastro editado", "text-bg-warning"),
    CADASTRO_DESATIVADO("Cadastro desativado", "text-bg-danger"),
    CADASTRO_REATIVADO("Cadastro reativado", "text-bg-success"),
    PLANILHA("Importação de planilha", "text-bg-primary"),
    DESVIO_EDITADO("Desvio editado", "text-bg-warning"),
    ANVISA("Atualização da ANVISA", "text-bg-info"),
    AVALIACAO_CRIADA("Avaliação criada", "text-bg-success"),
    AVALIACAO_EDITADA("Avaliação editada", "text-bg-warning");

    private final String rotulo;
    private final String classe;

    TipoEvento(String rotulo, String classe) {
        this.rotulo = rotulo;
        this.classe = classe;
    }
}
