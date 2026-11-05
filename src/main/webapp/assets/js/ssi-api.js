(function () {
    "use strict";
    if (window.Api) return;

    function getContextPath() {
        return document.body?.dataset?.contextPath ?? "";
    }

    function buildUrl(url) {
        const u = String(url).trim();
        if (u.startsWith("http://") || u.startsWith("https://")) return u;
        return u.startsWith("/") ? getContextPath() + u : getContextPath() + "/" + u;
    }

    function getCookie(name) {
        const prefix = name + "=";
        for (const c of (document.cookie ? document.cookie.split(";") : [])) {
            const v = c.trim();
            if (v.startsWith(prefix)) return decodeURIComponent(v.substring(prefix.length));
        }
        return "";
    }

    function toParams(data) {
        const params = new URLSearchParams();
        if (!data) return params;
        Object.entries(data).forEach(([k, v]) => {
            if (v === undefined || v === null) return;
            if (Array.isArray(v)) v.forEach(item => params.append(k, String(item)));
            else params.append(k, String(v));
        });
        return params;
    }

    async function request(url, options = {}) {
        const method = (options.method || "GET").toUpperCase();
        const headers = {
            "Accept": "application/json",
            "X-Requested-With": "XMLHttpRequest",
            ...(options.headers || {})
        };

        if (["POST", "PUT", "PATCH", "DELETE"].includes(method)) {
            headers["X-CSRF-Token"] = getCookie("XSRF-TOKEN");
        }

        const response = await fetch(buildUrl(url), {
            method,
            credentials: "same-origin",
            cache: "no-store",
            headers,
            body: options.body
        });

        const ct = response.headers.get("content-type") || "";
        if (!ct.toLowerCase().includes("application/json")) {
            if (response.redirected && response.url) {
                window.location.href = response.url;
            }
            throw new Error("Respuesta no válida del servidor.");
        }

        const result = await response.json();
        if (!response.ok) {
            if (response.status === 401) {
                window.setTimeout(() => window.location.href = getContextPath() + "/login", 300);
            }
            const err = new Error(result.message || "Operación no completada.");
            err.status = response.status;
            err.data = result;
            throw err;
        }
        return result;
    }

    async function get(url, query = null) {
        let finalUrl = String(url);
        if (query) {
            const qs = toParams(query).toString();
            if (qs) finalUrl += (finalUrl.includes("?") ? "&" : "?") + qs;
        }
        return request(finalUrl, { method: "GET" });
    }

    async function post(url, data) {
        return request(url, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
            body: toParams(data)
        });
    }

    async function submitForm(form) {
        const method = (form.method || "POST").toUpperCase();
        const body = new URLSearchParams();
        for (const field of form.elements) {
            if (!field.name || field.disabled) continue;
            if ((field.type === "checkbox" || field.type === "radio") && !field.checked) continue;
            body.append(field.name, field.value);
        }
        return request(form.action, {
            method,
            headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
            body
        });
    }

    window.Api = { request, get, post, submitForm, getContextPath };
})();