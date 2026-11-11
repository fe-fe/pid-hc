(function () {
    "use strict";

    var resultado = document.getElementById("modalResultadoImportacao");
    if (resultado) {
        document.addEventListener("DOMContentLoaded", function () {
            bootstrap.Modal.getOrCreateInstance(resultado).show();
        });
    }

    var zone = document.querySelector("[data-drop-zone]");
    if (!zone) return;

    var input = zone.querySelector("input[type=file]");
    var label = zone.querySelector("[data-drop-file]");
    var defaultText = label.textContent;

    function showFile() {
        var file = input.files && input.files[0];
        label.textContent = file ? file.name : defaultText;
        zone.classList.toggle("dragDrop--selected", !!file);
    }

    ["dragenter", "dragover"].forEach(function (type) {
        zone.addEventListener(type, function (event) {
            event.preventDefault();
            zone.classList.add("dragDrop--over");
        });
    });

    ["dragleave", "drop"].forEach(function (type) {
        zone.addEventListener(type, function (event) {
            event.preventDefault();
            zone.classList.remove("dragDrop--over");
        });
    });

    zone.addEventListener("drop", function (event) {
        if (!event.dataTransfer.files.length) return;
        input.files = event.dataTransfer.files;
        showFile();
    });

    input.addEventListener("change", showFile);
})();
