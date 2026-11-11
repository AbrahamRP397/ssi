package mx.gob.morelos.ssi.controllers;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.models.audit.AuditDao;
import mx.gob.morelos.ssi.models.magistrate.Magistrate;
import mx.gob.morelos.ssi.models.magistrate.MagistrateDao;
import mx.gob.morelos.ssi.models.room.Room;
import mx.gob.morelos.ssi.models.room.RoomDao;
import mx.gob.morelos.ssi.models.user.User;
import mx.gob.morelos.ssi.models.user.UserDao;
import mx.gob.morelos.ssi.utils.EmailSender; // <-- IMPORTANTE

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "MagistrateServlet", urlPatterns = {
        "/admin/magistrados",
        "/admin/magistrados/save",
        "/admin/magistrados/desactivar" // <-- Nueva ruta
})
public class MagistrateServlet extends HttpServlet {

    private MagistrateDao magistrateDao;
    private UserDao userDao;
    private RoomDao roomDao;
    private AuditDao auditDao;
    private Gson gson;

    @Override
    public void init() {
        magistrateDao = new MagistrateDao();
        userDao = new UserDao();
        roomDao = new RoomDao();
        auditDao = new AuditDao();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        User user = getUser(req);
        if (user == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }

        List<Magistrate> magistrates = magistrateDao.getAll();
        List<Room> rooms = roomDao.getActiveRooms();

        req.setAttribute("magistrates", magistrates);
        req.setAttribute("rooms", rooms);
        req.setAttribute("activeView", "magistrates");
        req.getRequestDispatcher("/views/magistrate/magistrates.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        User user = getUser(req);
        if (user == null) { sendJson(res, 401, false, "error", "Sesión no válida.", null); return; }

        if ("/admin/magistrados/save".equals(req.getServletPath())) {
            saveMagistrate(req, res, user);
        } else if ("/admin/magistrados/desactivar".equals(req.getServletPath())) {
            deactivateMagistrate(req, res, user);
        } else {
            sendJson(res, 404, false, "error", "Ruta no encontrada.", null);
        }
    }

    private void saveMagistrate(HttpServletRequest req, HttpServletResponse res, User user) throws IOException {
        try {
            String fullName = trim(req.getParameter("fullName"));
            String email = trim(req.getParameter("email")).toLowerCase();
            Long idRoom = parseLong(req.getParameter("idRoom"));

            if (fullName.isBlank() || email.isBlank() || idRoom == null) {
                sendJson(res, 400, false, "warning", "Completa nombre, correo y sala.", null);
                return;
            }
            if (!isReasonableEmail(email)) {
                sendJson(res, 400, false, "warning", "Correo no válido.", null);
                return;
            }
            if (userDao.findAnyByEmail(email) != null) {
                sendJson(res, 409, false, "warning", "Ya existe un usuario con ese correo.", null);
                return;
            }

            String[] parts = fullName.split("\\s+");
            String name = parts.length > 0 ? parts[0] : fullName;
            String surname = parts.length > 1 ? parts[1] : "";
            String lastname = parts.length > 2 ? parts[parts.length - 1] : "";

            // Generar contraseña temporal
            String tempPassword = "SSI" + (100000 + (int)(Math.random() * 900000)) + "!";

            User newUser = new User();
            newUser.setName(name);
            newUser.setSurname(surname);
            newUser.setLastname(lastname);
            newUser.setEmail(email);
            newUser.setPassword(tempPassword);
            newUser.setRole("Magistrado");
            newUser.setStatus(1);
            newUser.setMustChangePassword(1);
            newUser.setTemporaryPasswordExpiration(new java.sql.Timestamp(
                    System.currentTimeMillis() + 24L * 60 * 60 * 1000)); // 24 horas

            if (!userDao.create(newUser)) {
                sendJson(res, 500, false, "error", "No fue posible crear el usuario.", null);
                return;
            }

            Magistrate m = new Magistrate();
            m.setIdUser(newUser.getId());
            m.setIdRoom(idRoom);
            m.setStatus(1);

            if (!magistrateDao.create(m)) {
                sendJson(res, 500, false, "error", "No fue posible asociar el magistrado.", null);
                return;
            }

            // ENVIAR CORREO ELECTRÓNICO CON CREDENCIALES
            try {
                String asunto = "Bienvenido al Sistema de Segunda Instancia (SSI)";
                String cuerpo = buildWelcomeEmail(fullName, email, tempPassword);
                EmailSender.sendMail(email, asunto, cuerpo);
            } catch (Exception e) {
                getServletContext().log("No se pudo enviar el correo de bienvenida a: " + email, e);
                // Nota: No detenemos el registro si el correo falla, pero lo dejamos en el log.
            }

            try {
                auditDao.log(user.getId(), user.getFullName(), "administrador",
                        "Registró un magistrado", fullName + " · " + email, "gold");
            } catch (RuntimeException ignored) {}

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("idMagistrate", m.getIdMagistrate());
            data.put("tempPassword", tempPassword);

            sendJson(res, 201, true, "success", "Magistrado registrado y correo enviado.", data);

        } catch (RuntimeException ex) {
            getServletContext().log("Error al registrar magistrado.", ex);
            sendJson(res, 500, false, "error", "No fue posible registrar el magistrado.", null);
        }
    }

    private void deactivateMagistrate(HttpServletRequest req, HttpServletResponse res, User user) throws IOException {
        try {
            Long idMagistrate = parseLong(req.getParameter("idMagistrate"));
            if (idMagistrate == null) {
                sendJson(res, 400, false, "warning", "Magistrado no válido.", null);
                return;
            }

            Magistrate m = magistrateDao.getById(idMagistrate.intValue());
            if (m == null) {
                sendJson(res, 404, false, "error", "El magistrado no existe.", null);
                return;
            }

            // REGLA DE NEGOCIO: Validar que no tenga tocas pendientes
            // (Reutilizamos el DAO de tocas para verificar, o hacemos una consulta rápida)
            if (magistrateDao.hasPendingTocas(idMagistrate)) {
                sendJson(res, 409, false, "warning",
                        "No se puede desactivar al magistrado porque tiene tocas pendientes de resolución. Reasígnalas primero.", null);
                return;
            }

            m.setStatus(m.getStatus() == 1 ? 0 : 1);
            if (!magistrateDao.update(m)) {
                sendJson(res, 500, false, "error", "No fue posible desactivar al magistrado.", null);
                return;
            }

            // También desactivamos su usuario de acceso
            userDao.changeStatus(m.getIdUser().intValue(), 0);

            try {
                auditDao.log(user.getId(), user.getFullName(), "administrador",
                        "Desactivó un magistrado", m.getFullName(), "crimson");
            } catch (RuntimeException ignored) {}

            sendJson(res, 200, true, "success", "Magistrado desactivado correctamente.", null);
        } catch (RuntimeException ex) {
            getServletContext().log("Error al desactivar magistrado.", ex);
            sendJson(res, 500, false, "error", "No fue posible desactivar al magistrado.", null);
        }
    }

    private String buildWelcomeEmail(String name, String email, String tempPassword) {
        return """
                <!DOCTYPE html>
                <html lang="es"><head><meta charset="UTF-8"></head>
                <body style="font-family:Arial,Helvetica,sans-serif;color:#2d3748;">
                  <div style="max-width:560px;margin:0 auto;padding:24px;">
                    <div style="background:#2e3b2b;color:#fff;padding:20px;border-radius:12px 12px 0 0;">
                      <h1 style="margin:0;font-size:20px;">Sistema de Segunda Instancia</h1>
                      <p style="margin:6px 0 0;opacity:.85;">Bienvenido</p>
                    </div>
                    <div style="background:#fff;padding:24px;border:1px solid #eee9e6;border-radius:0 0 12px 12px;">
                      <p>Hola <strong>%s</strong>,</p>
                      <p>Has sido dado de alta como Magistrado en el sistema.</p>
                      <p>Tus credenciales de acceso son:</p>
                      <ul style="background:#f8f7f4;padding:16px;border-radius:8px;list-style:none;">
                        <li><strong>Correo:</strong> %s</li>
                        <li><strong>Contraseña temporal:</strong> %s</li>
                      </ul>
                      <p style="background:#fff8e5;color:#805b10;padding:12px 14px;border-radius:8px;">
                        Por seguridad, el sistema te pedirá cambiar esta contraseña al iniciar sesión por primera vez. Tienes 24 horas para hacerlo.
                      </p>
                    </div>
                  </div>
                </body></html>
                """.formatted(name, email, tempPassword);
    }

    // utilidades
    private User getUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        Object v = s.getAttribute("usuario");
        return v instanceof User u ? u : null;
    }
    private Long parseLong(String v) {
        if (v == null) return null;
        try { long n = Long.parseLong(v.trim()); return n > 0 ? n : null; }
        catch (NumberFormatException e) { return null; }
    }
    private String trim(String v) { return v == null ? "" : v.trim(); }
    private boolean isReasonableEmail(String e) {
        if (e == null || e.isBlank() || e.length() > 100) return false;
        int at = e.indexOf('@');
        return at > 0 && at == e.lastIndexOf('@') && at < e.length() - 3
                && e.indexOf('.', at) > at + 1;
    }
    private void sendJson(HttpServletResponse res, int status, boolean ok, String type, String message, Object data) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("success", ok);
        m.put("type", type);
        m.put("message", message == null ? "" : message.trim());
        if (data != null) m.put("data", data);
        res.getWriter().write(gson.toJson(m));
    }
}