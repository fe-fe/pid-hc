
(() => {
    "use strict";

    const tabelas = new Map();
    const seletorInterativo = "a, button, input, select, textarea, label, [contenteditable], [role='button']";

    const resizeObserver = new ResizeObserver(() => {
        tabelas.forEach(({ atualizar }) => atualizar());
    });

    function configurarTabela(container) {
        const viewport = container.querySelector("[data-table-scroll-viewport]");
        if (!viewport || tabelas.has(viewport)) return;

        const table = viewport.querySelector("table");
        const botoes = container.querySelectorAll("[data-table-scroll-direction]");

        let arraste = null;
        let arrastou = false;

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

        // Início do arraste
        viewport.addEventListener("pointerdown", event => {
            arrastou = false;

            if (event.pointerType !== "mouse" || event.button !== 0) return;
            if (event.target.closest(seletorInterativo)) return;
            if (viewport.scrollWidth <= viewport.clientWidth) return;

            event.preventDefault();

            arraste = {
                id: event.pointerId,
                x: event.clientX,
                scroll: viewport.scrollLeft
            };

            viewport.focus({ preventScroll: true });
            viewport.setPointerCapture(event.pointerId);
        });

        // Movimento do arraste
        viewport.addEventListener("pointermove", event => {
            if (!arraste || event.pointerId !== arraste.id) return;

            const distancia = event.clientX - arraste.x;
            if (!arrastou && Math.abs(distancia) < 5) return;

            arrastou = true;
            viewport.classList.add("is-dragging");
            viewport.scrollLeft = arraste.scroll - distancia;
        });

        function finalizarArraste(event) {
            if (!arraste || event.pointerId !== arraste.id) return;

            const id = arraste.id;
            arraste = null;

            viewport.classList.remove("is-dragging");

            if (viewport.hasPointerCapture(id)) {
                viewport.releasePointerCapture(id);
            }
        }

        ["pointerup", "pointercancel", "lostpointercapture"].forEach(evento => {
            viewport.addEventListener(evento, finalizarArraste);
        });

        viewport.addEventListener("click", event => {
            if (!arrastou) return;

            event.preventDefault();
            event.stopPropagation();
            arrastou = false;
        }, true);

        viewport.addEventListener("dragstart", event => {
            if (arraste) event.preventDefault();
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
