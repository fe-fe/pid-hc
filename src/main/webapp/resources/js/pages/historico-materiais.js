(function () {
    "use strict";

    function initializeHistoryModals() {
        var modal = document.getElementById("historyMaterialModal");
        var deleteModal = document.getElementById("historyDeleteModal");
        if (!modal || !deleteModal) return;

        // Apenas prepara a apresentação das modais. Nenhuma ação grava ou altera os cards.
        modal.addEventListener("show.bs.modal", function (event) {
            var data = event.relatedTarget ? event.relatedTarget.dataset : {};
            var mode = data.mode || "material";
            var editing = mode === "edit";
            var existingMaterial = mode !== "material";
            var form = document.getElementById("historyMaterialForm");
            form.reset();

            document.getElementById("historyMaterialModalTitle").textContent =
                editing ? "Editar histórico" : existingMaterial ? "Novo histórico" : "Novo material";
            document.getElementById("historyMaterialModalDescription").textContent =
                editing ? "Edite os dados da análise desta marca" : existingMaterial
                    ? "Cadastre a análise de uma nova marca para este material"
                    : "Cadastre um material e sua primeira análise";
            document.getElementById("historyMaterialModalAction").textContent =
                editing ? "Salvar alterações" : existingMaterial ? "Adicionar histórico" : "Criar material";

            ["codigo", "nome", "local"].forEach(function (name) {
                var source = { codigo: "code", nome: "name", local: "location" }[name];
                form.elements[name].value = existingMaterial ? data[source] || "" : "";
                form.elements[name].readOnly = existingMaterial;
            });
            form.elements.fabricante.value = editing ? data.manufacturer || "" : "";
            form.elements.marca.value = editing ? data.brand || "" : "";
            form.elements.data.value = editing ? data.date || "" : "";
            form.elements.status.value = editing ? data.status || "aprovado" : "aprovado";
            form.elements.parecer.value = editing ? data.opinion || "" : "";
        });

        deleteModal.addEventListener("show.bs.modal", function (event) {
            var data = event.relatedTarget ? event.relatedTarget.dataset : {};
            document.getElementById("historyDeleteBrand").textContent = data.brand || "";
            document.getElementById("historyDeleteMaterial").textContent = data.name || "";
        });

        // Impede submissões acidentais com Enter nos formulários de demonstração.
        ["historyMaterialForm", "historyFiltersForm"].forEach(function (id) {
            document.getElementById(id).addEventListener("submit", function (event) {
                event.preventDefault();
            });
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initializeHistoryModals);
    } else {
        initializeHistoryModals();
    }
})();
