(function () {
    "use strict";

    /* ---------- Modales basados en .modal-backdrop ---------- */
    document.addEventListener("click", function (e) {
        const closer = e.target.closest("[data-modal-close]");
        if (closer) {
            const modal = closer.closest(".modal-backdrop");
            if (modal) modal.style.display = "none";
            return;
        }
        const trigger = e.target.closest("[data-modal-open]");
        if (trigger) {
            e.preventDefault();
            const id = trigger.dataset.modalOpen;
            const modal = document.getElementById(id);
            if (modal) modal.style.display = "grid";
            return;
        }
        // Click fuera del modal
        if (e.target.classList.contains("modal-backdrop")) {
            e.target.style.display = "none";
        }
    });

    /* ---------- Tabs (detalle de toca) ---------- */
    document.addEventListener("click", function (e) {
        const tab = e.target.closest("[data-tab]");
        if (!tab) return;
        const group = tab.closest("[data-tab-group]");
        if (!group) return;
        const name = tab.dataset.tab;
        group.querySelectorAll("[data-tab]").forEach(t => t.classList.remove("active"));
        tab.classList.add("active");
        group.parentElement.querySelectorAll("[data-tab-panel]").forEach(p => {
            p.style.display = p.dataset.tabPanel === name ? "" : "none";
        });
    });
})();