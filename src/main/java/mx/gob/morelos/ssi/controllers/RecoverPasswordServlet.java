package mx.gob.morelos.ssi.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.models.passwordreset.PasswordResetToken;
import mx.gob.morelos.ssi.models.passwordreset.PasswordResetTokenDao;
import mx.gob.morelos.ssi.models.user.User;
import mx.gob.morelos.ssi.models.user.UserDao;
import mx.gob.morelos.ssi.utils.EmailSender;
import mx.gob.morelos.ssi.utils.PasswordPolicy;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;

@WebServlet(name = "RecoverPasswordServlet", urlPatterns = {
        "/verify-email", "/reset-password", "/password-reset-success"
})
public class RecoverPasswordServlet extends HttpServlet {

    private UserDao userDao;
    private PasswordResetTokenDao tokenDao;

    private static final int RESET_TOKEN_MINUTES = 15;
    private static final int RESET_TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String GENERIC_RECOVERY_MESSAGE =
            "Si el correo está registrado y la cuenta se encuentra activa, recibirás un enlace para restablecer tu contraseña.";

    @Override
    public void init() {
        userDao = new UserDao();
        tokenDao = new PasswordResetTokenDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        disableCache(res);

        switch (req.getServletPath()) {
            case "/verify-email" -> req.getRequestDispatcher("/verify-email.jsp").forward(req, res);
            case "/reset-password" -> showResetPassword(req, res);
            case "/password-reset-success" -> req.getRequestDispatcher("/password-reset-success.jsp").forward(req, res);
            default -> res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        disableCache(res);
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());

        switch (req.getServletPath()) {
            case "/verify-email" -> processRecoveryRequest(req, res);
            case "/reset-password" -> processPasswordReset(req, res);
            default -> res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showResetPassword(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String token = normalizeToken(req.getParameter("token"));
        if (token.isBlank()) {
            showRecoveryError(req, res, "El enlace de recuperación no es válido.");
            return;
        }

        PasswordResetToken resetToken;
        try {
            resetToken = tokenDao.findValidToken(token);
        } catch (RuntimeException ex) {
            getServletContext().log("Error al validar el token.", ex);
            showRecoveryError(req, res, "No fue posible validar el enlace de recuperación.");
            return;
        }

        if (resetToken == null) {
            showRecoveryError(req, res,
                    "El enlace de recuperación ya fue utilizado o ha expirado.");
            return;
        }

        req.setAttribute("token", token);
        req.getRequestDispatcher("/reset-password.jsp").forward(req, res);
    }

    private void processRecoveryRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String email = normalizeEmail(req.getParameter("email"));

        if (email.isBlank() || !isReasonableEmail(email)) {
            showGenericRecoveryMessage(req, res);
            return;
        }

        User user;
        try {
            user = userDao.findByEmail(email);
        } catch (RuntimeException ex) {
            getServletContext().log("Error al consultar usuario para recuperación.", ex);
            showRecoveryProcessingError(req, res);
            return;
        }

        // Nunca revelar si el correo existe
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            showGenericRecoveryMessage(req, res);
            return;
        }

        try {
            // Invalida tokens anteriores e inserta uno nuevo
            tokenDao.invalidateUserTokens(user.getId());

            String token = generateSecureToken();
            LocalDateTime expiration = LocalDateTime.now().plusMinutes(RESET_TOKEN_MINUTES);

            if (!tokenDao.create(user.getId(), token, expiration)) {
                showGenericRecoveryMessage(req, res);
                return;
            }

            String resetLink = buildResetLink(req, token);
            String body = buildRecoveryEmail(user, resetLink);

            try {
                EmailSender.sendMail(user.getEmail(),
                        "Restablecimiento de contraseña - SSI", body);
            } catch (RuntimeException ex) {
                getServletContext().log("SMTP no configurado o falló. Token guardado, enlace: " + resetLink, ex);
                // No invalidamos el token: en dev sin SMTP al menos se puede probar el enlace
            }

            showGenericRecoveryMessage(req, res);

        } catch (RuntimeException ex) {
            getServletContext().log("Error inesperado en recuperación.", ex);
            showGenericRecoveryMessage(req, res);
        }
    }

    private void processPasswordReset(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String token = normalizeToken(req.getParameter("token"));
        String pwd = req.getParameter("password");
        String confirm = req.getParameter("confirmPassword");

        if (token.isBlank()) {
            showRecoveryError(req, res, "El enlace de recuperación no es válido.");
            return;
        }
        if (pwd == null || pwd.isBlank()) {
            showResetError(req, res, token, "Ingresa una contraseña nueva.");
            return;
        }
        if (!PasswordPolicy.isValid(pwd)) {
            showResetError(req, res, token, PasswordPolicy.getValidationMessage());
            return;
        }
        if (confirm == null || !pwd.equals(confirm)) {
            showResetError(req, res, token, "Las contraseñas no coinciden.");
            return;
        }

        try {
            PasswordResetToken resetToken = tokenDao.findValidToken(token);
            if (resetToken == null) {
                showRecoveryError(req, res,
                        "El enlace de recuperación ya fue utilizado o ha expirado.");
                return;
            }

            if (!userDao.updatePasswordById(resetToken.getIdUser(), pwd)) {
                showResetError(req, res, token, "No fue posible actualizar la contraseña.");
                return;
            }

            tokenDao.invalidateUserTokens(resetToken.getIdUser());
            res.sendRedirect(req.getContextPath() + "/password-reset-success");

        } catch (RuntimeException ex) {
            getServletContext().log("Error al restablecer contraseña.", ex);
            showResetError(req, res, token, "No fue posible cambiar la contraseña. Intenta nuevamente.");
        }
    }

    // ---------- utilidades ----------

    private String generateSecureToken() {
        byte[] bytes = new byte[RESET_TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildResetLink(HttpServletRequest req, String token) {
        String base = System.getenv("APP_BASE_URL");
        if (base == null || base.isBlank()) {
            // fallback: usar el host de la request
            base = req.getScheme() + "://" + req.getServerName()
                    + (req.getServerPort() == 80 || req.getServerPort() == 443
                    ? "" : ":" + req.getServerPort());
        }
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base + req.getContextPath() + "/reset-password?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String buildRecoveryEmail(User user, String resetLink) {
        String name = user.getFullName();
        return """
                <!DOCTYPE html>
                <html lang="es"><head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,Helvetica,sans-serif;color:#2d3748;">
                  <div style="max-width:560px;margin:0 auto;padding:24px;">
                    <div style="background:#2e3b2b;color:#fff;padding:20px;border-radius:12px 12px 0 0;">
                      <h1 style="margin:0;font-size:20px;">Sistema de Segunda Instancia</h1>
                      <p style="margin:6px 0 0;opacity:.85;">Restablecimiento de contraseña</p>
                    </div>
                    <div style="background:#fff;padding:24px;border:1px solid #eee9e6;border-radius:0 0 12px 12px;">
                      <p>Hola <strong>%s</strong>,</p>
                      <p>Recibimos una solicitud para restablecer la contraseña de tu cuenta.</p>
                      <p style="text-align:center;margin:28px 0;">
                        <a href="%s" style="display:inline-block;background:#6f7857;color:#fff;padding:12px 22px;border-radius:8px;text-decoration:none;font-weight:bold;">Restablecer contraseña</a>
                      </p>
                      <p style="background:#fff8e5;color:#805b10;padding:12px 14px;border-radius:8px;">
                        Este enlace es de un solo uso y vencerá en %d minutos.
                      </p>
                      <p style="color:#718096;">Si no solicitaste este cambio, ignora este mensaje.</p>
                    </div>
                  </div>
                </body></html>
                """.formatted(escapeHtml(name), escapeHtml(resetLink), RESET_TOKEN_MINUTES);
    }

    private void showGenericRecoveryMessage(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setAttribute("mensaje", GENERIC_RECOVERY_MESSAGE);
        req.getRequestDispatcher("/verify-email.jsp").forward(req, res);
    }

    private void showRecoveryProcessingError(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setAttribute("error", "No fue posible procesar la solicitud en este momento.");
        req.getRequestDispatcher("/verify-email.jsp").forward(req, res);
    }

    private void showRecoveryError(HttpServletRequest req, HttpServletResponse res, String msg)
            throws ServletException, IOException {
        req.setAttribute("error", msg);
        req.getRequestDispatcher("/verify-email.jsp").forward(req, res);
    }

    private void showResetError(HttpServletRequest req, HttpServletResponse res,
                                String token, String msg) throws ServletException, IOException {
        req.setAttribute("error", msg);
        req.setAttribute("token", token);
        req.getRequestDispatcher("/reset-password.jsp").forward(req, res);
    }

    private boolean isReasonableEmail(String email) {
        if (email == null || email.isBlank() || email.length() > 100) return false;
        int at = email.indexOf('@');
        return at > 0 && at == email.lastIndexOf('@') && at < email.length() - 3
                && email.indexOf('.', at) > at + 1;
    }

    private String normalizeEmail(String v) {
        return v == null ? "" : v.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeToken(String v) {
        return v == null ? "" : v.trim();
    }

    private String escapeHtml(String v) {
        if (v == null) return "";
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private void disableCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}