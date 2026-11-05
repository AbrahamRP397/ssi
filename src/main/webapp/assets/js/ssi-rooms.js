(function () {
    "use strict";

    const form = document.getElementById("formSala");
    if (!form) return;

    form.addEventListener("submit", async function (e) {
        e.preventDefault();
        if (!form.checkValidity()) return;
        try {
            const res = await Api.submitForm(form);
            if (window.showToast) window.showToast(res.message || "Sala registrada", "success");
            const modal = document.getElementById("modalSala");
            if (modal) modal.style.display = "none";
            setTimeout(() => window.location.reload(), 900);
        } catch (err) {
            if (window.showToast) window.showToast(err.message, "error");
        }
    });

    // Lógica para desactivar sala
    document.querySelectorAll(".btn-desactivar-sala").forEach(btn => {
        btn.addEventListener("click", async function () {
            const id = this.dataset.id;
            const name = this.dataset.name;

            if (!confirm(`¿Estás seguro de desactivar la ${name}?`)) return;

            try {
                // Usamos el Api.post que ya tienes configurado
                const res = await Api.post("/admin/salas/desactivar", { idRoom: id });
                if (window.showToast) window.showToast(res.message || "Sala desactivada", "success");
                setTimeout(() => window.location.reload(), 900);
            } catch (err) {
                // Aquí se mostrará el error si tiene tocas pendientes (Error 409 del servlet)
                if (window.showToast) window.showToast(err.message, "error");
                else alert(err.message);
            }
        });
    });

    // Búsqueda
    const search = document.getElementById("searchRooms");
    const rows = Array.from(document.querySelectorAll(".room-row"));
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