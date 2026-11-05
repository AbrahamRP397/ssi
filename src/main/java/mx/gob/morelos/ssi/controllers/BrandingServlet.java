package mx.gob.morelos.ssi.controllers;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.filters.BrandingFilter;
import mx.gob.morelos.ssi.models.audit.AuditDao;
import mx.gob.morelos.ssi.models.branding.Branding;
import mx.gob.morelos.ssi.models.branding.BrandingDao;
import mx.gob.morelos.ssi.models.user.User;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@WebServlet(name = "BrandingServlet", value = "/admin/identidad")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
public class BrandingServlet extends HttpServlet {

    private BrandingDao brandingDao;
    private AuditDao auditDao;
    private Gson gson;

    @Override
    public void init() {
        brandingDao = new BrandingDao();
        auditDao = new AuditDao();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        User user = getUser(req);
        if (user == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }

        req.setAttribute("branding", brandingDao.get());
        req.setAttribute("activeView", "appearance");
        req.getRequestDispatcher("/views/branding/branding.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        User user = getUser(req);
        if (user == null) { sendJson(res, 401, false, "error", "Sesión no válida.", null); return; }

        try {
            Branding current = brandingDao.get();

            String primaryColor = trim(req.getParameter("primaryColor"));
            String accentColor = trim(req.getParameter("accentColor"));

            if (primaryColor.isBlank()) primaryColor = current.getPrimaryColor();
            if (accentColor.isBlank()) accentColor = current.getAccentColor();

            if (!isHexColor(primaryColor) || !isHexColor(accentColor)) {
                sendJson(res, 400, false, "warning", "Colores inválidos.", null);
                return;
            }

            String logoUrl = current.getLogoUrl();

            Part logoPart = null;
            try { logoPart = req.getPart("logo"); } catch (Exception ignored) {}

            if (logoPart != null && logoPart.getSize() > 0) {
                String original = logoPart.getSubmittedFileName();
                String ext = extractExtension(original);
                if (ext == null) {
                    sendJson(res, 400, false, "warning",
                            "Formato no válido. Usa PNG, JPG o SVG.", null);
                    return;
                }

                Path uploadDir = resolveUploadDir("logos");
                Files.createDirectories(uploadDir);

                String fileName = UUID.randomUUID() + "." + ext;
                Path target = uploadDir.resolve(fileName);

                try (InputStream in = logoPart.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }

                logoUrl = req.getContextPath() + "/uploads/logos/" + fileName;
            }

            current.setLogoUrl(logoUrl);
            current.setPrimaryColor(primaryColor);
            current.setAccentColor(accentColor);
            current.setUpdatedBy(user.getId());

            if (!brandingDao.update(current)) {
                sendJson(res, 500, false, "error",
                        "No fue posible guardar la identidad visual.", null);
                return;
            }

            BrandingFilter.invalidate();

            try {
                auditDao.log(user.getId(), user.getFullName(), "administrador",
                        "Actualizó la identidad visual",
                        "Colores y logo institucional", "gold");
            } catch (RuntimeException ignored) {}

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("logoUrl", logoUrl);
            data.put("primaryColor", primaryColor);
            data.put("accentColor", accentColor);

            sendJson(res, 200, true, "success",
                    "Identidad visual actualizada.", data);

        } catch (RuntimeException ex) {
            getServletContext().log("Error al guardar branding.", ex);
            sendJson(res, 500, false, "error",
                    "No fue posible guardar la identidad visual.", null);
        }
    }

    /* ---------- helpers ---------- */

    private Path resolveUploadDir(String subdir) {
        String base = System.getProperty("catalina.base");
        if (base == null || base.isBlank()) base = System.getProperty("java.io.tmpdir");
        return Paths.get(base, "ssi-uploads", subdir);
    }

    private String extractExtension(String fileName) {
        if (fileName == null) return null;
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) return null;
        String ext = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return switch (ext) {
            case "png", "jpg", "jpeg", "svg" -> ext;
            default -> null;
        };
    }

    private boolean isHexColor(String c) {
        return c != null && c.matches("^#[0-9a-fA-F]{6}$");
    }

    private String trim(String v) { return v == null ? "" : v.trim(); }

    private User getUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        Object v = s.getAttribute("usuario");
        return v instanceof User u ? u : null;
    }

    private void sendJson(HttpServletResponse res, int status, boolean ok,
                          String type, String message, Object data) throws IOException {
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