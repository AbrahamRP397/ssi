(function () {
    "use strict";

    // ----- Fecha de hoy (chip) -----
    const todayLabel = document.getElementById("todayLabel");
    if (todayLabel) {
        const now = new Date();
        todayLabel.textContent = new Intl.DateTimeFormat("es-MX", {
            weekday: "long", day: "numeric", month: "long"
        }).format(now);
    }

    // ----- Búsqueda en vivo -----
    const searchInput = document.getElementById("searchTocas");
    const rows = Array.from(document.querySelectorAll(".toca-row"));

    function applyFilter() {
        const q = (searchInput?.value || "").toLowerCase().trim();
        let visible = 0;
        rows.forEach(r => {
            const match = !q || (r.dataset.search || "").includes(q);
            r.style.display = match ? "" : "none";
            if (match) visible++;
        });
        const footer = document.getElementById("tocasFooterCount");
        if (footer) {
            footer.textContent = `Mostrando ${visible} de ${rows.length} tocas`;
        }
    }

    if (searchInput) searchInput.addEventListener("input", applyFilter);

    // ----- Filtro de estado (ciclo) -----
    const statusStates = [
        { label: "Todos los estados", value: null },
        { label: "Nueva", value: "Nueva" },
        { label: "En ponencia", value: "En ponencia" },
        { label: "Resuelto", value: "Resuelto" }
    ];
    let statusIndex = 0;
    const btn = document.getElementById("btnStatusFilter");
    const label = document.getElementById("statusFilterLabel");

    if (btn && label) {
        btn.addEventListener("click", () => {
            statusIndex = (statusIndex + 1) % statusStates.length;
            const state = statusStates[statusIndex];
            label.textContent = state.label;
            rows.forEach(r => {
                const match = !state.value || r.dataset.status === state.value;
                if (!match) r.style.display = "none";
            });
            applyFilter();
        });
    }
})();