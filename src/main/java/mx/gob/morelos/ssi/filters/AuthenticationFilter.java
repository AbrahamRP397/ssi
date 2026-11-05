package mx.gob.morelos.ssi.filters;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.models.user.User;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    private Gson gson;

    @Override
    public void init(FilterConfig filterConfig) {
        gson = new Gson();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri != null && contextPath != null && uri.startsWith(contextPath)
                ? uri.substring(contextPath.length()) : uri;

        // ¡ESCUDO PARA LA API!
        if (path != null && path.startsWith("/api/")) {
            chain.doFilter(req, res);
            return;
        }

        if (isPublicResource(path)) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("usuario");

        if (!(value instanceof User user) || user.getId() == null || user.getId() <= 0) {
            handleUnauthenticated(request, response, contextPath);
            return;
        }

        // Si el usuario debe cambiar contraseña, solo puede ir a /force-password-change, /logout y /assets
        if (user.requiresPasswordChange() && !isAllowedDuringPasswordChange(path)) {
            if (isAjaxRequest(request)) {
                sendJson(response, HttpServletResponse.SC_FORBIDDEN, false, "warning",
                        "Debes cambiar tu contraseña antes de continuar.");
            } else {
                response.sendRedirect(contextPath + "/force-password-change");
            }
            return;
        }

        // Si ya cambió la contraseña, no puede volver a /force-password-change
        if (!user.requiresPasswordChange() && path.equals("/force-password-change")) {
            response.sendRedirect(contextPath + "/mis-tocas");
            return;
        }

        // Rutas /admin/* solo para administradores
        if (path.startsWith("/admin/") && !"Administrador".equalsIgnoreCase(user.getRole())) {
            handleForbidden(request, response);
            return;
        }

        // Rutas del magistrado solo para magistrados
        if ((path.equals("/mis-tocas") || path.startsWith("/mis-tocas/")
                || path.equals("/agenda")
                || path.equals("/resoluciones") || path.startsWith("/resoluciones/"))
                && !"Magistrado".equalsIgnoreCase(user.getRole())) {
            handleForbidden(request, response);
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean isPublicResource(String path) {
        if (path.startsWith("/api/")) {
            return true; // SALTA TODOS LOS FILTROS SI ES API
        }
        return path.equals("/")
                || path.equals("/index.jsp")
                || path.equals("/login")
                || path.equals("/login.jsp")
                || path.equals("/verify-email")
                || path.equals("/verify-email.jsp")
                || path.equals("/reset-password")
                || path.equals("/reset-password.jsp")
                || path.equals("/password-reset-success")
                || path.equals("/password-reset-success.jsp")
                || path.startsWith("/assets/");
    }

    private boolean isAllowedDuringPasswordChange(String path) {
        return path.equals("/force-password-change")
                || path.equals("/logout")
                || path.startsWith("/assets/");
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String xrw = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(xrw)) return true;
        String accept = request.getHeader("Accept");
        return accept != null && accept.toLowerCase(Locale.ROOT).contains("application/json");
    }

    private void handleUnauthenticated(HttpServletRequest req, HttpServletResponse res, String ctx)
            throws IOException {
        disableCache(res);
        if (isAjaxRequest(req)) {
            sendJson(res, HttpServletResponse.SC_UNAUTHORIZED, false, "error",
                    "Tu sesión terminó o tu cuenta ya no está disponible.");
        } else {
            res.sendRedirect(ctx + "/login");
        }
    }

    private void handleForbidden(HttpServletRequest req, HttpServletResponse res) throws IOException {
        disableCache(res);
        if (isAjaxRequest(req)) {
            sendJson(res, HttpServletResponse.SC_FORBIDDEN, false, "warning",
                    "No tienes permisos para acceder a este recurso.");
        } else {
            res.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "No tienes permisos para acceder a este recurso.");
        }
    }

    private void sendJson(HttpServletResponse response, int status, boolean ok,
                          String type, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", ok);
        m.put("type", type);
        m.put("message", message);
        response.getWriter().write(gson.toJson(m));
    }

    private void disableCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    @Override
    public void destroy() { gson = null; }
}