(() => {
    const indicador = document.getElementById("indicadorCarregamento");
    if (!indicador) return;

    let pendentes = 0;
    let timer;

    function carregar(iniciar) {
        if (iniciar) {
            if (pendentes++ === 0) {
                timer = setTimeout(() => indicador.hidden = false, 200);
            }
        } else if (pendentes && --pendentes === 0) {
            clearTimeout(timer);
            indicador.hidden = true;
        }
    }

    window.faces?.ajax?.addOnEvent(({ status }) => {
        if (status === "begin") carregar(true);
        if (status === "complete") carregar(false);
    });

    window.navigation?.addEventListener("navigate", ({ destination, downloadRequest, signal }) => {
        if (destination.sameDocument || downloadRequest != null) return;
        if (new URL(destination.url).origin !== location.origin) return;

        carregar(true);
        signal.addEventListener("abort", () => carregar(false), { once: true });
    });

    window.addEventListener("pagehide", () => {
        pendentes = 0;
        clearTimeout(timer);
        indicador.hidden = true;
    });
})();