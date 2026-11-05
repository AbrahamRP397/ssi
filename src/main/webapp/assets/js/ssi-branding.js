(function () {
    "use strict";

    const primaryInput = document.getElementById("primaryInput");
    const accentInput = document.getElementById("accentInput");
    const primaryLabel = document.getElementById("primaryLabel");
    const accentLabel = document.getElementById("accentLabel");
    const preview = document.getElementById("brandPreview");
    const form = document.getElementById("formBranding");
    const savedMsg = document.getElementById("savedMsg");

    function syncPreview() {
        if (!preview) return;
        if (primaryInput) {
            preview.style.setProperty("--preview-primary", primaryInput.value);
            primaryLabel.textContent = primaryInput.value.toUpperCase();
        }
        if (accentInput) {
            preview.style.setProperty("--preview-accent", accentInput.value);
            accentLabel.textContent = accentInput.value.toUpperCase();
        }
    }

    primaryInput?.addEventListener("input", syncPreview);
    accentInput?.addEventListener("input", syncPreview);

    form?.addEventListener("submit", async function (e) {
        e.preventDefault();
        try {
            const formData = new FormData(form);
            const res = await Api.request(form.action, {
                method: "POST",
                body: formData
            });
            if (window.showToast) window.showToast(res.message || "Identidad guardada", "success");
            if (savedMsg) savedMsg.style.display = "inline";
            setTimeout(() => window.location.reload(), 1200);
        } catch (err) {
            if (window.showToast) window.showToast(err.message, "error");
        }
    });
})();