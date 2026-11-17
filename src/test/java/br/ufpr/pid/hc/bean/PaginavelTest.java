package br.ufpr.pid.hc.bean;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaginavelTest {

    @Test
    void semRegistros() {
        Tabela tabela = new Tabela(0, 0);

        assertEquals(1, tabela.getTotalPaginas());
        assertEquals(List.of(0), tabela.getPaginas());
        assertEquals(0, tabela.getPrimeiroRegistro());
        assertEquals(0, tabela.getUltimoRegistro());
        assertTrue(tabela.isUltimaPagina());
        assertFalse(tabela.isVariasPaginas());
    }

    @Test
    void variasPaginas() {
        assertFalse(new Tabela(15, 0).isVariasPaginas());
        assertTrue(new Tabela(16, 0).isVariasPaginas());
    }

    @Test
    void poucasPaginas() {
        assertEquals(List.of(0, 1, 2, 3, 4), new Tabela(75, 2).getPaginas());
    }

    @Test
    void reticenciasNoMeio() {
        assertEquals(Arrays.asList(0, null, 4, 5, 6, null, 11), new Tabela(180, 5).getPaginas());
    }

    @Test
    void reticenciasNasPontas() {
        assertEquals(Arrays.asList(0, 1, null, 11), new Tabela(180, 0).getPaginas());
        assertEquals(Arrays.asList(0, null, 10, 11), new Tabela(180, 11).getPaginas());
        assertEquals(Arrays.asList(0, 1, 2, 3, null, 11), new Tabela(180, 2).getPaginas());
    }

    @Test
    void intervalo() {
        Tabela tabela = new Tabela(42, 2);

        assertEquals(31, tabela.getPrimeiroRegistro());
        assertEquals(42, tabela.getUltimoRegistro());
        assertTrue(tabela.isUltimaPagina());
    }

    @Test
    void limites() {
        Tabela tabela = new Tabela(42, 0);

        tabela.paginaAnterior();
        assertEquals(0, tabela.getPaginaAtual());

        tabela.proximaPagina();
        tabela.proximaPagina();
        tabela.proximaPagina();
        assertEquals(2, tabela.getPaginaAtual());
        assertFalse(new Tabela(42, 1).isUltimaPagina());
    }

    private static final class Tabela implements Paginavel {

        private final long total;
        private int pagina;

        Tabela(long total, int pagina) {
            this.total = total;
            this.pagina = pagina;
        }

        @Override
        public int getPaginaAtual() {
            return pagina;
        }

        @Override
        public int getTamanhoPagina() {
            return 15;
        }

        @Override
        public long getTotalRegistros() {
            return total;
        }

        @Override
        public void irParaPagina(int pagina) {
            this.pagina = pagina;
        }
    }
}
