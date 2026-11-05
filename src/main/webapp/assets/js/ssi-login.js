(function () {
    "use strict";

    const form = document.getElementById("loginForm");
    const pwdInput = document.getElementById("password");
    const toggle = document.getElementById("togglePassword");
    const btnLogin = document.getElementById("btnLogin");
    const btnLabel = document.getElementById("btnLoginLabel");
    const submittedMsg = document.getElementById("submittedMsg");
    const btnRecovery = document.getElementById("btnRecovery");
    const recoveryMsg = document.getElementById("recoveryMsg");

    /* ---------- Mostrar/ocultar contraseña ---------- */
    if (toggle && pwdInput) {
        toggle.addEventListener("click", function () {
            const hidden = pwdInput.type === "password";
            pwdInput.type = hidden ? "text" : "password";
            toggle.innerHTML = hidden
                ? `<svg viewBox="0 0 24 24" aria-hidden="true">
                       <path d="m3 3 18 18M10.6 10.7a2 2 0 0 0 2.7 2.7M9.9 4.3A10.8 10.8 0 0 1 12 4c5.3 0 9 5.1 9 5.1a12.7 12.7 0 0 1-2.1 2.6M6.6 6.7C4.4 8.1 3 10 3 10s3.7 5 9 5c1.1 0 2.1-.2 3-.5"/>
                   </svg>`
                : `<svg viewBox="0 0 24 24" aria-hidden="true">
                       <path d="M3 12s3.7-5 9-5 9 5 9 5-3.7 5-9 5-9-5-9-5Z"/>
                       <circle cx="12" cy="12" r="2.6"/>
                   </svg>`;
            toggle.setAttribute("aria-label",
                hidden ? "Ocultar contraseña" : "Mostrar contraseña");
        });
    }

    /* ---------- Recuperar contraseña ---------- */
    if (btnRecovery && recoveryMsg) {
        btnRecovery.addEventListener("click", function () {
            recoveryMsg.style.display = "flex";
        });
    }

    /* ---------- CSRF sync ---------- */
    function getCookie(name) {
        const prefix = name + "=";
        const cookies = document.cookie ? document.cookie.split(";") : [];
        for (const c of cookies) {
            const v = c.trim();
            if (v.startsWith(prefix)) return decodeURIComponent(v.substring(prefix.length));
        }
        return "";
    }
    const csrfInput = document.getElementById("loginCsrfToken");
    function syncCsrf() {
        const t = getCookie("XSRF-TOKEN");
        if (t && csrfInput) csrfInput.value = t;
    }
    syncCsrf();
    window.addEventListener("pageshow", syncCsrf);
    window.addEventListener("focus", syncCsrf);

    /* ---------- Envío con estado "Validando acceso…" 450 ms ---------- */
    let submitting = false;
    if (form) {
        form.addEventListener("submit", function (e) {
            if (submitting) { e.preventDefault(); return; }

            const email = document.getElementById("email");
            const pwd = document.getElementById("password");
            if (!email.value.trim() || !pwd.value) {
                e.preventDefault();
                return;
            }

            e.preventDefault();
            submitting = true;
            syncCsrf();

            if (submittedMsg) submittedMsg.style.display = "flex";
            if (btnLogin) btnLogin.disabled = true;
            if (btnLabel) btnLabel.textContent = "Validando…";

            // 450 ms después, el navegador hace el POST real (fiel al setTimeout(onLogin, 450) de React)
            window.setTimeout(function () {
                submitting = false;
                form.submit();
            }, 450);
        });
    }
})();