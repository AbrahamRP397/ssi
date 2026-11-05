package mx.gob.morelos.ssi.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.models.audit.AuditDao;
import mx.gob.morelos.ssi.models.user.User;
import mx.gob.morelos.ssi.models.user.UserDao;

import java.io.IOException;
import java.util.Locale;

@WebServlet(name = "LoginServlet", value = "/login")
public class LoginServlet extends HttpServlet {

    private UserDao userDao;
    private AuditDao auditDao;

    private static final int SESSION_TIMEOUT_SECONDS = 8 * 30 * 60;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOGIN_BLOCK_MILLIS = 15 * 60 * 1000L;
    private static final String ATTEMPTS_ATTRIBUTE = "loginAttempts";
    private static final String BLOCK_UNTIL_ATTRIBUTE = "loginBlockedUntil";

    @Override
    public void init() {
        userDao = new UserDao();
        auditDao = new AuditDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        disableCache(res);

        HttpSession session = req.getSession(false);
        if (session != null) {
            Object v = session.getAttribute("usuario");
            if (v instanceof User user) {
                if (user.requiresPasswordChange()) {
                    res.sendRedirect(req.getContextPath() + "/force-password-change");
                } else {
                    res.sendRedirect(req.getContextPath() + "/mis-tocas");
                }
                return;
            }
        }

        req.getRequestDispatcher("/login.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        disableCache(res);
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(true);

        if (isLoginBlocked(session)) {
            long remaining = getRemainingBlockSeconds(session);
            req.setAttribute("error",
                    "Has realizado demasiados intentos. Intenta nuevamente en "
                            + remaining + " segundos.");
            req.getRequestDispatcher("/login.jsp").forward(req, res);
            return;
        }

        String email = normalizeEmail(req.getParameter("email"));
        String password = req.getParameter("password");

        if (email.isBlank() || password == null || password.isBlank()) {
            req.setAttribute("error", "Completa el correo electrónico y la contraseña.");
            req.setAttribute("emailIngresado", email);
            req.getRequestDispatcher("/login.jsp").forward(req, res);
            return;
        }

        User user;
        try {
            user = userDao.login(email, password);
        } catch (RuntimeException ex) {
            getServletContext().log("Error al intentar iniciar sesión.", ex);
            req.setAttribute("error", "No fue posible iniciar sesión. Intenta nuevamente.");
            req.setAttribute("emailIngresado", email);
            req.getRequestDispatcher("/login.jsp").forward(req, res);
            return;
        }

        if (user == null) {
            registerFailedAttempt(session);
            req.setAttribute("error",
                    isLoginBlocked(session)
                            ? "Has realizado demasiados intentos. Intenta nuevamente en unos minutos."
                            : "Los datos ingresados son incorrectos.");
            req.setAttribute("emailIngresado", email);
            req.getRequestDispatcher("/login.jsp").forward(req, res);
            return;
        }

        clearLoginAttempts(session);

        if (user.requiresPasswordChange() && userDao.isTemporaryPasswordExpired(user)) {
            req.setAttribute("error",
                    "La contraseña temporal ha expirado. Solicita un nuevo acceso.");
            req.setAttribute("emailIngresado", email);
            req.getRequestDispatcher("/login.jsp").forward(req, res);
            return;
        }

        try { req.changeSessionId(); } catch (IllegalStateException ignored) {}

        session.setAttribute("usuario", user);
        session.setAttribute("rol", user.getRole());
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

        // Auditar el acceso
        try {
            auditDao.log(user.getId(), user.getFullName(), user.getRole(),
                    "Inició sesión", "Acceso al sistema", "blue");
        } catch (RuntimeException ex) {
            getServletContext().log("No se pudo registrar el acceso en auditoría.", ex);
        }

        if (user.requiresPasswordChange()) {
            res.sendRedirect(req.getContextPath() + "/force-password-change");
            return;
        }

        // Redirect según rol
        String target = "Administrador".equalsIgnoreCase(user.getRole())
                ? "/admin/distritos"
                : "/mis-tocas";
        res.sendRedirect(req.getContextPath() + target);
    }

    // --- utilidades (idénticas al SGDA) ---

    private boolean isLoginBlocked(HttpSession session) {
        if (session == null) return false;
        Object v = session.getAttribute(BLOCK_UNTIL_ATTRIBUTE);
        if (!(v instanceof Long blockedUntil)) return false;
        long now = System.currentTimeMillis();
        if (blockedUntil <= now) { clearLoginAttempts(session); return false; }
        return true;
    }

    private long getRemainingBlockSeconds(HttpSession session) {
        Object v = session.getAttribute(BLOCK_UNTIL_ATTRIBUTE);
        if (!(v instanceof Long b)) return 0;
        long r = b - System.currentTimeMillis();
        return r <= 0 ? 0 : Math.max(1, (long) Math.ceil(r / 1000.0));
    }

    private void registerFailedAttempt(HttpSession session) {
        int attempts = getLoginAttempts(session) + 1;
        session.setAttribute(ATTEMPTS_ATTRIBUTE, attempts);
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            session.setAttribute(BLOCK_UNTIL_ATTRIBUTE,
                    System.currentTimeMillis() + LOGIN_BLOCK_MILLIS);
        }
    }

    private int getLoginAttempts(HttpSession session) {
        Object v = session.getAttribute(ATTEMPTS_ATTRIBUTE);
        return v instanceof Integer i ? i : 0;
    }

    private void clearLoginAttempts(HttpSession session) {
        session.removeAttribute(ATTEMPTS_ATTRIBUTE);
        session.removeAttribute(BLOCK_UNTIL_ATTRIBUTE);
    }

    private String normalizeEmail(String v) {
        return v == null ? "" : v.trim().toLowerCase(Locale.ROOT);
    }

    private void disableCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}