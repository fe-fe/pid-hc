package br.ufpr.pid.hc.bean;

import org.omnifaces.util.Ajax;

final class Bootstrap {

    private Bootstrap() {
    }

    static void abrirModal(String id) {
        executar("Modal", id, "show");
    }

    static void fecharModal(String id) {
        executar("Modal", id, "hide");
    }

    static void fecharOffcanvas(String id) {
        executar("Offcanvas", id, "hide");
    }

    private static void executar(String componente, String id, String metodo) {
        Ajax.oncomplete("bootstrap." + componente + ".getOrCreateInstance(document.getElementById('" + id + "'))."
                + metodo + "()");
    }
}
