package br.ufpr.pid.hc.bean;

import java.util.ArrayList;
import java.util.List;

public interface Paginavel {

    int VIZINHAS = 1;

    int getPaginaAtual();

    int getTamanhoPagina();

    long getTotalRegistros();

    void irParaPagina(int pagina);

    default int getTotalPaginas() {
        return (int) Math.max(1, (getTotalRegistros() + getTamanhoPagina() - 1) / getTamanhoPagina());
    }

    default boolean isVariasPaginas() {
        return getTotalPaginas() > 1;
    }

    default boolean isUltimaPagina() {
        return getPaginaAtual() >= getTotalPaginas() - 1;
    }

    default void paginaAnterior() {
        if (getPaginaAtual() > 0) {
            irParaPagina(getPaginaAtual() - 1);
        }
    }

    default void proximaPagina() {
        if (!isUltimaPagina()) {
            irParaPagina(getPaginaAtual() + 1);
        }
    }

    default long getPrimeiroRegistro() {
        return getTotalRegistros() == 0 ? 0 : (long) getPaginaAtual() * getTamanhoPagina() + 1;
    }

    default long getUltimoRegistro() {
        return Math.min(getTotalRegistros(), (long) (getPaginaAtual() + 1) * getTamanhoPagina());
    }

    default List<Integer> getPaginas() {
        int total = getTotalPaginas();
        int atual = getPaginaAtual();
        int inicio = Math.max(1, atual - VIZINHAS);
        int fim = Math.min(total - 2, atual + VIZINHAS);
        if (inicio <= 2) {
            inicio = 1;
        }
        if (fim >= total - 3) {
            fim = total - 2;
        }

        List<Integer> paginas = new ArrayList<>();
        paginas.add(0);
        if (inicio > 1) {
            paginas.add(null);
        }
        for (int pagina = inicio; pagina <= fim; pagina++) {
            paginas.add(pagina);
        }
        if (fim < total - 2) {
            paginas.add(null);
        }
        if (total > 1) {
            paginas.add(total - 1);
        }
        return paginas;
    }
}
