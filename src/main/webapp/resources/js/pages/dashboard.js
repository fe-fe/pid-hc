(function () {
    "use strict";

    // Base exclusivamente demonstrativa. Nenhum dado é consultado ou gravado no backend.
    // Os exemplos de avaliações seguem o histórico visual; os incidentes são fictícios.
    var materials = [
        {
            code: "MAT-001", name: "Cateter intravenoso periférico 20G", category: "Acesso vascular",
            description: "Cateter periférico calibre 20G, com dispositivo de segurança.",
            evaluations: [
                { brand: "Insyte Autoguard", date: "2026-09-26", result: "APROVADO", opinion: "Boa estabilidade durante a punção e dispositivo de segurança adequado." },
                { brand: "Safe Cath", date: "2026-08-11", result: "REPROVADO", opinion: "O dispositivo apresentou resistência acima do esperado na progressão." }
            ],
            incidents: [
                { brand: "Insyte Autoguard", date: "2026-10-02", reference: "DEMO-002", lot: "EX-2026-B", sector: "Unidade de internação", description: "Relato de dificuldade no acionamento do dispositivo de segurança em uma unidade do lote." },
                { brand: "Safe Cath", date: "2026-09-15", reference: "DEMO-001", lot: "EX-2026-A", sector: "Pronto Atendimento", description: "Relato de resistência durante a utilização do cateter. Amostra encaminhada para análise." }
            ]
        },
        {
            code: "MAT-003", name: "Equipo para infusão macrogotas", category: "Infusão",
            description: "Equipo macrogotas para administração de soluções.",
            evaluations: [
                { brand: "FluxCare", date: "2026-09-18", result: "APROVADO", opinion: "Amostra conforme os requisitos técnicos." },
                { brand: "MedFlow", date: "2026-07-22", result: "APROVADO", opinion: "Fluxo regular e conexões compatíveis com os equipamentos avaliados." }
            ],
            incidents: []
        },
        {
            code: "MAT-004", name: "Luva de procedimento sem pó", category: "Proteção individual",
            description: "Luva descartável sem pó para procedimentos não cirúrgicos.",
            evaluations: [
                { brand: "ProtecPlus", date: "2026-09-15", result: "REPROVADO", opinion: "Resistência abaixo do especificado no lote avaliado." },
                { brand: "Supermax Premium", date: "2026-08-29", result: "APROVADO", opinion: "Material íntegro, com bom ajuste e resistência durante o uso simulado." }
            ],
            incidents: [
                { brand: "ProtecPlus", date: "2026-09-20", reference: "DEMO-003", lot: "EX-2026-C", sector: "Pronto Atendimento", description: "Relato de rompimento da luva durante a colocação. Lote identificado para investigação." }
            ]
        }
    ];

    var results = {
        APROVADO: { label: "Aprovado", style: "approved" },
        REPROVADO: { label: "Reprovado", style: "rejected" },
        INCONCLUSIVO: { label: "Inconclusivo", style: "inconclusive" },
        PENDENTE: { label: "Pendente", style: "pending" }
    };

    function normalize(value) {
        return value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase().trim();
    }

    function formatDate(value) {
        return value ? value.split("-").reverse().join("/") : "Sem avaliação";
    }

    function setField(root, name, value) {
        root.querySelector('[data-field="' + name + '"]').textContent = value;
    }

    function newestFirst(items) {
        return items.slice().sort(function (a, b) { return b.date.localeCompare(a.date); });
    }

    function initialize() {
        var root = document.getElementById("materialLookup");
        if (!root) return;
        var form = document.getElementById("materialLookupForm");
        var search = document.getElementById("materialLookupSearch");
        var container = document.getElementById("materialLookupResults");
        var empty = document.getElementById("materialLookupEmpty");
        var status = document.getElementById("materialLookupStatus");

        function makeCard(material) {
            var card = document.getElementById("materialLookupCardTemplate").content.cloneNode(true);
            var evaluations = newestFirst(material.evaluations);
            var incidents = newestFirst(material.incidents);
            ["code", "name", "description", "category"].forEach(function (field) { setField(card, field, material[field]); });
            setField(card, "evaluationCount", evaluations.length);
            setField(card, "brandCount", new Set(evaluations.map(function (item) { return item.brand; })).size);
            setField(card, "incidentCount", incidents.length);
            setField(card, "lastEvaluation", formatDate(evaluations.length ? evaluations[0].date : null));

            // Compara cada incidente à última avaliação da mesma marca, sem alterar seu resultado.
            var latestByBrand = new Map();
            evaluations.forEach(function (item) { if (!latestByBrand.has(item.brand)) latestByBrand.set(item.brand, item); });
            var afterApproval = incidents.filter(function (incident) {
                var latest = latestByBrand.get(incident.brand);
                return latest && latest.result === "APROVADO" && incident.date > latest.date;
            }).length;
            setField(card, "notice", afterApproval
                ? "Atenção: " + afterApproval + (afterApproval === 1 ? " incidente foi relatado" : " incidentes foram relatados") + " após a última aprovação da respectiva marca. Consulte os relatos e o parecer antes de decidir."
                : "Consulte o parecer de cada marca e os relatos disponíveis. O resultado de uma avaliação não se aplica automaticamente às demais marcas ou lotes.");

            evaluations.forEach(function (evaluation) {
                var row = document.getElementById("materialLookupEvaluationTemplate").content.cloneNode(true);
                setField(row, "brand", evaluation.brand);
                setField(row, "date", formatDate(evaluation.date));
                row.querySelector("time").dateTime = evaluation.date;
                setField(row, "opinion", evaluation.opinion);
                var result = results[evaluation.result] || { label: "Sem resultado", style: "pending" };
                setField(row, "result", result.label);
                row.querySelector('[data-field="result"]').classList.add("material-lookup__badge--" + result.style);
                card.querySelector('[data-field="evaluations"]').appendChild(row);
            });
            card.querySelector('[data-field="noEvaluations"]').hidden = evaluations.length > 0;
            card.querySelector(".material-lookup__table-wrap").hidden = evaluations.length === 0;

            incidents.forEach(function (incident) {
                var item = document.getElementById("materialLookupIncidentTemplate").content.cloneNode(true);
                setField(item, "brand", incident.brand);
                setField(item, "date", formatDate(incident.date));
                item.querySelector("time").dateTime = incident.date;
                setField(item, "description", incident.description);
                setField(item, "reference", "Registro: " + incident.reference);
                setField(item, "lot", "Lote: " + (incident.lot || "Não informado"));
                setField(item, "sector", "Setor: " + (incident.sector || "Não informado"));
                card.querySelector('[data-field="incidents"]').appendChild(item);
            });
            card.querySelector('[data-field="noIncidents"]').hidden = incidents.length > 0;
            card.querySelector('[data-field="incidents"]').hidden = incidents.length === 0;
            return card;
        }

        function showEmpty(title, message) {
            empty.hidden = false;
            document.getElementById("materialLookupEmptyTitle").textContent = title;
            document.getElementById("materialLookupEmptyText").textContent = message;
        }

        function consult() {
            var query = normalize(search.value);
            container.replaceChildren();
            if (!query) {
                status.textContent = "Digite um código ou nome para consultar.";
                showEmpty("Qual material você quer consultar?", "Pesquise pelo código ou nome para reunir o histórico de avaliações e os incidentes em um só lugar.");
                search.focus();
                return;
            }
            var terms = query.split(/\s+/);
            var matches = materials.filter(function (material) {
                var text = normalize(material.code + " " + material.name);
                return terms.every(function (term) { return text.indexOf(term) !== -1; });
            });
            status.textContent = matches.length + (matches.length === 1 ? " material encontrado" : " materiais encontrados") + " na base demonstrativa para “" + search.value.trim() + "”.";
            empty.hidden = matches.length > 0;
            if (!matches.length) showEmpty("Nenhum material encontrado", "Confira o código ou tente parte do nome. Você também pode consultar um dos exemplos acima.");
            matches.forEach(function (material) { container.appendChild(makeCard(material)); });
        }

        form.addEventListener("submit", function (event) { event.preventDefault(); consult(); });
        root.querySelectorAll("[data-search]").forEach(function (button) {
            button.addEventListener("click", function () { search.value = button.dataset.search; consult(); });
        });
        document.getElementById("materialLookupClear").addEventListener("click", function () {
            search.value = "";
            consult();
            status.textContent = "Consulta limpa.";
        });
    }

    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", initialize);
    else initialize();
})();
