(function () {
    "use strict";
    const form = document.getElementById("formReasignar");
    if (!form) return;

    form.addEventListener("submit", async function (e) {
        e.preventDefault();
        if (!form.checkValidity()) return;
        try {
            const res = await Api.submitForm(form);
            if (window.showToast) window.showToast(res.message || "Toca reasignada", "success");
            const m = document.getElementById("modalReasignar");
            if (m) m.style.display = "none";
            setTimeout(() => window.location.reload(), 900);
        } catch (err) {
            if (window.showToast) window.showToast(err.message, "error");
        }
    });
})();