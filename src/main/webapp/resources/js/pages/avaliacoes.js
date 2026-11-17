function abrirModalAvaliacao(data, modalId) {
    if (data.status === "success") {
        bootstrap.Modal.getOrCreateInstance(document.getElementById(modalId)).show();
    }
}

function abrirModalCriarAvaliacao(data) {
    abrirModalAvaliacao(data, "modalAvaliacao");
}

function abrirModalEditarAvaliacao(data) {
    abrirModalAvaliacao(data, "modalEditarAvaliacao");
}

function fecharModalAvaliacaoSeValido(data, modalId, formId) {
    if (data.status !== "success") return;
    var temErros = Array.from(document.querySelectorAll("#" + formId + " .text-danger"))
        .some(function (el) { return el.textContent.trim() !== ""; });
    if (!temErros) {
        bootstrap.Modal.getOrCreateInstance(document.getElementById(modalId)).hide();
    }
}

function fecharModalAvaliacaoSeSucesso(data) {
    fecharModalAvaliacaoSeValido(data, "modalAvaliacao", "formAvaliacao");
}

function fecharModalEditarAvaliacaoSeSucesso(data) {
    fecharModalAvaliacaoSeValido(data, "modalEditarAvaliacao", "formEditarAvaliacao");
}
