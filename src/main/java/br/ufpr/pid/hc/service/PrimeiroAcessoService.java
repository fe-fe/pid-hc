package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.exception.DomainException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;

@Singleton
@Startup
@PermitAll
@Slf4j
public class PrimeiroAcessoService {

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO_BLOCO = 4;
    private static final int BLOCOS = 3;

    private final SecureRandom aleatorio = new SecureRandom();

    @Inject
    private UsuarioService usuarioService;

    private String codigo;

    @PostConstruct
    void iniciar() {
        isPendente();
    }

    public boolean isPendente() {
        if (usuarioService.existeAdministradorAtivo()) {
            codigo = null;
            return false;
        }
        if (codigo == null) {
            codigo = gerarCodigo();
            log.warn("""

                    ==================================================================
                     PRIMEIRO ACESSO: nenhum administrador ativo cadastrado.
                     Acesse /pages/auth/init e informe o código:

                         {}

                     O código muda a cada reinício até o administrador ser criado.
                    ==================================================================""", codigo);
        }
        return true;
    }

    public void criarAdministrador(Usuario usuario, String codigoInformado) {
        if (!isPendente()) {
            throw new DomainException("O primeiro acesso já foi concluído");
        }
        if (!confere(codigoInformado)) {
            throw new DomainException("Código de primeiro acesso inválido");
        }
        usuarioService.cadastrarPrimeiroAdministrador(usuario);
        codigo = null;
        log.info("Primeiro administrador cadastrado");
    }

    private boolean confere(String codigoInformado) {
        if (codigoInformado == null) {
            return false;
        }
        String normalizado = codigoInformado.strip().toUpperCase(Locale.ROOT);
        return MessageDigest.isEqual(codigo.getBytes(StandardCharsets.UTF_8),
                normalizado.getBytes(StandardCharsets.UTF_8));
    }

    private String gerarCodigo() {
        StringBuilder gerado = new StringBuilder();
        for (int i = 0; i < BLOCOS * TAMANHO_BLOCO; i++) {
            if (i > 0 && i % TAMANHO_BLOCO == 0) {
                gerado.append('-');
            }
            gerado.append(ALFABETO.charAt(aleatorio.nextInt(ALFABETO.length())));
        }
        return gerado.toString();
    }
}
