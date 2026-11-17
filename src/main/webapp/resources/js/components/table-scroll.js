
(() => {
    "use strict";

    const tabelas = new Map();

    const resizeObserver = new ResizeObserver(() => {
        tabelas.forEach(({ atualizar }) => atualizar());
    });

    function configurarTabela(container) {
        const viewport = container.querySelector("[data-table-scroll-viewport]");
        if (!viewport || tabelas.has(viewport)) return;

        const table = viewport.querySelector("table");
        const botoes = container.querySelectorAll("[data-table-scroll-direction]");

        function atualizar() {
            const maximo = Math.max(0, viewport.scrollWidth - viewport.clientWidth);

            viewport.classList.toggle("is-scrollable", maximo > 1);

            botoes.forEach(botao => {
                const direcao = Number(botao.dataset.tableScrollDirection);

                botao.disabled = direcao < 0
                    ? viewport.scrollLeft <= 1
                    : viewport.scrollLeft >= maximo - 1;
            });
        }

        // Navegação pelos botões
        botoes.forEach(botao => {
            botao.addEventListener("click", () => {
                const direcao = Number(botao.dataset.tableScrollDirection);
                const movimentoReduzido = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

                viewport.scrollBy({
                    left: direcao * viewport.clientWidth * 0.75,
                    behavior: movimentoReduzido ? "auto" : "smooth"
                });
            });
        });

        viewport.addEventListener("scroll", atualizar, { passive: true });

        tabelas.set(viewport, { atualizar, table });

        resizeObserver.observe(viewport);
        if (table) resizeObserver.observe(table);

        atualizar();
    }

    function inicializarTabelas() {
        // Remove referências de tabelas substituídas pelo AJAX
        tabelas.forEach(({ table }, viewport) => {
            if (viewport.isConnected) return;

            resizeObserver.unobserve(viewport);
            if (table) resizeObserver.unobserve(table);

            tabelas.delete(viewport);
        });

        document.querySelectorAll("[data-table-scroll]").forEach(configurarTabela);
    }

    function iniciar() {
        inicializarTabelas();

        // Identifica tabelas adicionadas ou substituídas pelo JSF
        new MutationObserver(inicializarTabelas).observe(document.body, {
            childList: true,
            subtree: true
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", iniciar);
    } else {
        iniciar();
    }
})();
