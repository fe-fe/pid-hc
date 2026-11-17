function abrirModalEditarTecnovigilancia(data) {
    if (data.status === "success") {
        bootstrap.Modal.getOrCreateInstance(document.getElementById("modalEditarTecnovigilancia")).show();
    }
}

function fecharModalEditarTecnovigilanciaSeSucesso(data) {
    if (data.status !== "success") return;
    var temErros = Array.from(document.querySelectorAll("#formEditarTecnovigilancia .text-danger"))
        .some(function (el) { return el.textContent.trim() !== ""; });
    if (!temErros) {
        bootstrap.Modal.getOrCreateInstance(document.getElementById("modalEditarTecnovigilancia")).hide();
    }
}
