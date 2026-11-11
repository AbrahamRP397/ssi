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

    // ----- Búsqueda en vivo + filtro de estado -----
    const searchInput = document.getElementById("searchTocas");
    const statusSelect = document.getElementById("statusFilterSelect");
    const rows = Array.from(document.querySelectorAll(".toca-row"));

    function applyFilters() {
        const q = (searchInput?.value || "").toLowerCase().trim();
        const status = statusSelect?.value || "";
        let visible = 0;

        rows.forEach(r => {
            const matchesText = !q || (r.dataset.search || "").includes(q);
            const matchesStatus = !status || r.dataset.status === status;
            const match = matchesText && matchesStatus;
            r.style.display = match ? "" : "none";
            if (match) visible++;
        });

        const footer = document.getElementById("tocasFooterCount");
        if (footer) {
            footer.textContent = `Mostrando ${visible} de ${rows.length} tocas`;
        }
    }

    if (searchInput) searchInput.addEventListener("input", applyFilters);
    if (statusSelect) statusSelect.addEventListener("change", applyFilters);
})();