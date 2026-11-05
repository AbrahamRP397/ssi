package mx.gob.morelos.ssi.controllers;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.models.audit.AuditDao;
import mx.gob.morelos.ssi.models.dashboard.DashboardDao;
import mx.gob.morelos.ssi.models.magistrate.Magistrate;
import mx.gob.morelos.ssi.models.magistrate.MagistrateDao;
import mx.gob.morelos.ssi.models.ponencia.Ponencia;
import mx.gob.morelos.ssi.models.ponencia.PonenciaDao;
import mx.gob.morelos.ssi.models.resolution.Resolution;
import mx.gob.morelos.ssi.models.resolution.ResolutionDao;
import mx.gob.morelos.ssi.models.toca.Toca;
import mx.gob.morelos.ssi.models.toca.TocaDao;
import mx.gob.morelos.ssi.models.user.User;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet(name = "TocaServlet", urlPatterns = {"/mis-tocas", "/mis-tocas/*"})
public class TocaServlet extends HttpServlet {

    private TocaDao tocaDao;
    private PonenciaDao ponenciaDao;
    private ResolutionDao resolutionDao;
    private MagistrateDao magistrateDao;
    private DashboardDao dashboardDao;
    private AuditDao auditDao;
    private Gson gson;

    @Override
    public void init() {
        tocaDao = new TocaDao();
        ponenciaDao = new PonenciaDao();
        resolutionDao = new ResolutionDao();
        magistrateDao = new MagistrateDao();
        dashboardDao = new DashboardDao();
        auditDao = new AuditDao();
        gson = new Gson();
    }

    /* ==========================================================
       GET
       ========================================================== */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        disableCache(res);

        User user = getUser(req);
        if (user == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }

        Magistrate mag = magistrateDao.getByUserId(user.getId());
        if (mag == null) { res.sendError(403, "El usuario no es magistrado."); return; }

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            showList(req, res, mag);
        } else {
            String[] parts = pathInfo.substring(1).split("/");
            if (parts.length == 1) {
                showDetail(req, res, mag, parts[0]);
            } else {
                res.sendError(404);
            }
        }
    }

    /* ==========================================================
       POST: /mis-tocas/{id}/ponencia y /mis-tocas/{id}/resolucion
       ========================================================== */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        disableCache(res);
        req.setCharacterEncoding("UTF-8");

        User user = getUser(req);
        if (user == null) { sendJson(res, 401, false, "error", "Sesión no válida.", null); return; }

        Magistrate mag = magistrateDao.getByUserId(user.getId());
        if (mag == null) { sendJson(res, 403, false, "error", "No autorizado.", null); return; }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendJson(res, 404, false, "error", "Ruta no encontrada.", null);
            return;
        }

        String[] parts = pathInfo.substring(1).split("/");
        if (parts.length != 2) {
            sendJson(res, 404, false, "error", "Ruta no encontrada.", null);
            return;
        }

        Long idToca = parseLong(parts[0]);
        if (idToca == null) {
            sendJson(res, 400, false, "warning", "Identificador de toca inválido.", null);
            return;
        }

        Toca toca = tocaDao.getById(idToca.intValue());
        if (toca == null || !mag.getIdMagistrate().equals(toca.getIdMagistrate())) {
            sendJson(res, 404, false, "error", "La toca no existe o no te pertenece.", null);
            return;
        }

        switch (parts[1]) {
            case "ponencia" -> savePonencia(req, res, user, mag, toca);
            case "resolucion" -> saveResolucion(req, res, user, mag, toca);
            default -> sendJson(res, 404, false, "error", "Ruta no encontrada.", null);
        }
    }

    /* ==========================================================
       MOSTRAR LISTA
       ========================================================== */
    private void showList(HttpServletRequest req, HttpServletResponse res, Magistrate mag)
            throws ServletException, IOException {

        List<Toca> tocas = tocaDao.getByMagistrate(mag.getIdMagistrate());
        req.setAttribute("tocas", tocas);
        req.setAttribute("summary", dashboardDao.forMagistrate(mag.getIdMagistrate()));
        req.setAttribute("activeView", "cases");
        req.getRequestDispatcher("/views/toca/tocas.jsp").forward(req, res);
    }

    /* ==========================================================
       MOSTRAR DETALLE
       ========================================================== */
    private void showDetail(HttpServletRequest req, HttpServletResponse res,
                            Magistrate mag, String idStr) throws ServletException, IOException {

        Long id = parseLong(idStr);
        if (id == null) { res.sendError(404); return; }

        Toca toca = tocaDao.getById(id.intValue());
        if (toca == null || !mag.getIdMagistrate().equals(toca.getIdMagistrate())) {
            res.sendError(404);
            return;
        }

        req.setAttribute("toca", toca);
        req.setAttribute("activeView", "case-detail");
        req.getRequestDispatcher("/views/toca/toca-detail.jsp").forward(req, res);
    }

    /* ==========================================================
       GUARDAR PONENCIA
       ========================================================== */
    private void savePonencia(HttpServletRequest req, HttpServletResponse res,
                              User user, Magistrate mag, Toca toca) throws IOException {

        try {
            String dateStr = req.getParameter("sessionDate");
            String startStr = req.getParameter("startTime");
            String endStr = req.getParameter("endTime");

            if (dateStr == null || startStr == null || endStr == null) {
                sendJson(res, 400, false, "warning", "Completa fecha y horario.", null);
                return;
            }

            LocalDate date = LocalDate.parse(dateStr);
            Time start = Time.valueOf(startStr.length() == 5 ? startStr + ":00" : startStr);
            Time end = Time.valueOf(endStr.length() == 5 ? endStr + ":00" : endStr);

            if (!end.after(start)) {
                sendJson(res, 400, false, "warning", "La hora final debe ser mayor a la inicial.", null);
                return;
            }

            if (ponenciaDao.hasScheduleConflict(mag.getIdMagistrate(), Date.valueOf(date), start, end)) {
                sendJson(res, 409, false, "warning",
                        "No puedes agendar esta ponencia. Debes dejar al menos 1 hora de diferencia con tus otras sesiones.", null);
                return;
            }

            Ponencia p = new Ponencia();
            p.setIdToca(toca.getIdToca());
            p.setIdMagistrate(mag.getIdMagistrate());
            p.setSessionDate(Date.valueOf(date));
            p.setStartTime(start);
            p.setEndTime(end);
            p.setKind("Ponencia");

            if (!ponenciaDao.create(p)) {
                sendJson(res, 500, false, "error", "No fue posible registrar la ponencia.", null);
                return;
            }

            tocaDao.addHistory(toca.getIdToca(),
                    "Ponencia registrada para el " + dateStr + " de " + startStr + " a " + endStr);
            if ("Nueva".equals(toca.getStatus())) {
                tocaDao.changeStatus(toca.getIdToca(), "En ponencia");
            }

            try {
                auditDao.log(user.getId(), user.getFullName(), "magistrado",
                        "Registró una ponencia",
                        "Toca " + toca.getTocaNumber() + " · " + dateStr,
                        "blue");
            } catch (RuntimeException ignored) {}

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("idPonencia", p.getIdPonencia());

            sendJson(res, 201, true, "success",
                    "Ponencia registrada correctamente.", data);

        } catch (RuntimeException ex) {
            getServletContext().log("Error al registrar ponencia.", ex);
            sendJson(res, 500, false, "error", "No fue posible registrar la ponencia.", null);
        }
    }

    /* ==========================================================
       GUARDAR RESOLUCIÓN
       ========================================================== */
    private void saveResolucion(HttpServletRequest req, HttpServletResponse res,
                                User user, Magistrate mag, Toca toca) throws IOException {

        try {
            String outcome = req.getParameter("outcome");
            String body = req.getParameter("body");
            String observations = req.getParameter("observations");

            if (outcome == null || outcome.isBlank()
                    || body == null || body.isBlank()) {
                sendJson(res, 400, false, "warning",
                        "Selecciona el sentido y redacta el texto de la resolución.", null);
                return;
            }

            if (!outcome.equals("Confirma") && !outcome.equals("Revoca") && !outcome.equals("Modifica")) {
                sendJson(res, 400, false, "warning", "Sentido no válido.", null);
                return;
            }

            Resolution r = new Resolution();
            r.setIdToca(toca.getIdToca());
            r.setIdMagistrate(mag.getIdMagistrate());
            r.setOutcome(outcome);
            r.setBody(body);
            r.setObservations(observations);
            String fileName = toca.getTocaNumber().replace("/", "-") + ".pdf";
            String base = System.getProperty("catalina.base");
            if (base == null || base.isBlank()) base = System.getProperty("java.io.tmpdir");
            Path uploadDir = Paths.get(base, "ssi-uploads", "resolutions");
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(fileName);

// Generar el PDF
            try (FileOutputStream fos = new FileOutputStream(target.toFile())) {
                Document document = new Document();
                PdfWriter.getInstance(document, fos);
                document.open();
                document.add(new Paragraph("PODER JUDICIAL DEL ESTADO DE MORELOS"));
                document.add(new Paragraph("SISTEMA DE SEGUNDA INSTANCIA"));
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Toca: " + toca.getTocaNumber()));
                document.add(new Paragraph("Sentido: " + outcome));
                document.add(new Paragraph(" "));
                document.add(new Paragraph("RESOLUCIÓN:"));
                document.add(new Paragraph(body)); // Aquí va el texto del editor
                if (observations != null && !observations.isBlank()) {
                    document.add(new Paragraph(" "));
                    document.add(new Paragraph("OBSERVACIONES: " + observations));
                }
                document.close();
            } catch (Exception e) {
                getServletContext().log("Error al generar PDF", e);
                sendJson(res, 500, false, "error", "No fue posible generar el PDF de la resolución.", null);
                return;
            }

            r.setPdfUrl("/uploads/resolutions/" + fileName);


            if (!resolutionDao.create(r)) {
                sendJson(res, 500, false, "error", "No fue posible registrar la resolución.", null);
                return;
            }

            tocaDao.changeStatus(toca.getIdToca(), "Resuelto");
            tocaDao.addHistory(toca.getIdToca(),
                    "Resolución emitida · sentido: " + outcome);

            try {
                auditDao.log(user.getId(), user.getFullName(), "magistrado",
                        "Emitió una resolución",
                        "Toca " + toca.getTocaNumber() + " · sentido: " + outcome,
                        "green");
            } catch (RuntimeException ignored) {}

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("idResolution", r.getIdResolution());
            data.put("pdfUrl", r.getPdfUrl());

            sendJson(res, 201, true, "success",
                    "Resolución guardada y PDF generado.", data);

        } catch (RuntimeException ex) {
            getServletContext().log("Error al registrar resolución.", ex);
            sendJson(res, 500, false, "error", "No fue posible registrar la resolución.", null);
        }
    }

    /* ==========================================================
       UTILIDADES
       ========================================================== */
    private User getUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        Object v = s.getAttribute("usuario");
        return v instanceof User u ? u : null;
    }

    private Long parseLong(String v) {
        if (v == null) return null;
        try {
            long n = Long.parseLong(v.trim());
            return n > 0 ? n : null;
        } catch (NumberFormatException e) { return null; }
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

    private void disableCache(HttpServletResponse res) {
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);
    }
}