(function () {
    "use strict";

    /* ---------- Crear sala ---------- */
    const form = document.getElementById("formSala");
    if (form) {
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
    }

    /* ---------- Editar sala ---------- */
    document.querySelectorAll(".btn-editar-sala").forEach(btn => {
        btn.addEventListener("click", function () {
            document.getElementById("editIdRoom").value = this.dataset.id;
            document.getElementById("editName").value = this.dataset.name;
            document.getElementById("editAddress").value = this.dataset.address;
            document.getElementById("editDistrict").value = this.dataset.district;
            document.getElementById("modalEditarSala").style.display = "grid";
        });
    });

    const formEditar = document.getElementById("formEditarSala");
    if (formEditar) {
        formEditar.addEventListener("submit", async function (e) {
            e.preventDefault();
            if (!formEditar.checkValidity()) return;
            try {
                const res = await Api.submitForm(formEditar);
                if (window.showToast) window.showToast(res.message || "Sala actualizada", "success");
                document.getElementById("modalEditarSala").style.display = "none";
                setTimeout(() => window.location.reload(), 900);
            } catch (err) {
                if (window.showToast) window.showToast(err.message, "error");
            }
        });
    }

    /* ---------- Activar / Desactivar sala ---------- */
    document.querySelectorAll(".btn-toggle-sala").forEach(btn => {
        btn.addEventListener("click", async function () {
            const id = this.dataset.id;
            const name = this.dataset.name;
            const isActive = this.dataset.active === "true";

            // Si quieres confirmación, aquí podrías abrir un modal genérico,
            // pero para agilizar, solo ejecutamos la acción.
            try {
                const res = await Api.post("/admin/salas/desactivar", { idRoom: id }); // El servlet ya hace toggle o desactiva
                if (window.showToast) window.showToast(res.message || (isActive ? "Sala desactivada" : "Sala activada"), "success");
                setTimeout(() => window.location.reload(), 900);
            } catch (err) {
                if (window.showToast) window.showToast(err.message, "error");
            }
        });
    });

    /* ---------- Búsqueda ---------- */
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