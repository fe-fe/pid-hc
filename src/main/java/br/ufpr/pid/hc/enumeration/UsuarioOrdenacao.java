package br.ufpr.pid.hc.enumeration;

import lombok.Getter;

@Getter
public enum UsuarioOrdenacao implements CampoOrdenacao {
    NOME("nome", "Nome"),
    EMAIL("email", "E-mail"),
    PERFIL("perfil", "Perfil");

    private final String campoBanco;
    private final String rotulo;

    UsuarioOrdenacao(String campoBanco, String rotulo) {
        this.campoBanco = campoBanco;
        this.rotulo = rotulo;
    }
}
