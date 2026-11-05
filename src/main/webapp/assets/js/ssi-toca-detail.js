(function () {
    "use strict";

    const formPonencia = document.getElementById("ponenciaForm");
    const formResolucion = document.getElementById("resolucionForm");
    const editor = document.getElementById("resolucionEditor");
    const bodyHidden = document.getElementById("resolucionBody");
    const resolucionError = document.getElementById("resolucionError");

    /* ---------- Editor rich-text básico ---------- */
    /* ---------- Editor rich-text básico ---------- */
    document.querySelectorAll(".editor-toolbar [data-cmd]").forEach(btn => {
        // IMPORTANTE: Usar 'mousedown' y 'e.preventDefault()' para no perder el foco del texto
        btn.addEventListener("mousedown", function(e) {
            e.preventDefault();

            const cmd = this.dataset.cmd;
            document.execCommand(cmd, false, null);

            // Volvemos a poner el cursor en el editor
            if(editor) editor.focus();
        });
    });

    /* ---------- Submit Ponencia ---------- */
    if (formPonencia) {
        formPonencia.addEventListener("submit", async function (e) {
            e.preventDefault();
            if (!formPonencia.checkValidity()) return;
            try {
                await Api.submitForm(formPonencia);
                document.getElementById("ponenciaFormWrap").style.display = "none";
                document.getElementById("ponenciaSuccess").style.display = "flex";
            } catch (err) {
                showToast(err.message || "No fue posible guardar la ponencia.", "error");
            }
        });
    }

    /* ---------- Submit Resolución ---------- */
    if (formResolucion && editor) {
        formResolucion.addEventListener("submit", async function (e) {
            e.preventDefault();

            const text = editor.innerText.trim();
            if (!text) {
                resolucionError.style.display = "block";
                editor.focus();
                return;
            }
            resolucionError.style.display = "none";
            bodyHidden.value = text;

            try {
                await Api.submitForm(formResolucion);
                document.getElementById("resolucionFormWrap").style.display = "none";
                document.getElementById("resolucionSuccess").style.display = "flex";
            } catch (err) {
                showToast(err.message || "No fue posible guardar la resolución.", "error");
            }
        });
    }

    /* ---------- Toast ---------- */
    window.showToast = function (message, type = "success") {
        const toast = document.getElementById("toast");
        if (!toast) return;
        toast.textContent = message;
        toast.dataset.type = type;
        toast.classList.add("show");
        setTimeout(() => toast.classList.remove("show"), 4000);
    };
})();