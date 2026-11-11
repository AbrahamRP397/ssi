(function () {
    "use strict";

    const form = document.getElementById("formMagistrado");
    if (form) {
        form.addEventListener("submit", async function (e) {
            e.preventDefault();
            if (!form.checkValidity()) return;
            try {
                const res = await Api.submitForm(form);
                if (window.showToast) window.showToast(res.message || "Magistrado registrado", "success");
                const m = document.getElementById("modalMagistrado");
                if (m) m.style.display = "none";
                setTimeout(() => window.location.reload(), 900);
            } catch (err) {
                if (window.showToast) window.showToast(err.message, "error");
            }
        });
    }

    // Lógica para activar/desactivar magistrado
    document.querySelectorAll(".btn-toggle-mag").forEach(btn => {
        btn.addEventListener("click", async function () {
            const id = this.dataset.id;
            const name = this.dataset.name;
            const isActive = this.dataset.active === "true";

            try {
                const res = await Api.post("/admin/magistrados/desactivar", { idMagistrate: id });
                if (window.showToast) window.showToast(res.message || (isActive ? "Magistrado desactivado" : "Magistrado activado"), "success");
                setTimeout(() => window.location.reload(), 900);
            } catch (err) {
                if (window.showToast) window.showToast(err.message, "error");
            }
        });
    });

    // Búsqueda
    const search = document.getElementById("searchMagistrates");
    const rows = Array.from(document.querySelectorAll(".mag-row"));
    if (search) {
        search.addEventListener("input", function () {
            const q = search.value.toLowerCase().trim();
            rows.forEach(r => {
                const match = !q || (r.dataset.search || "").includes(q);
                r.style.display = match ? "" : "none";
            });
        });
    }
})();