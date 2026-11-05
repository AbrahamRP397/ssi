package mx.gob.morelos.ssi.controllers;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.models.audit.AuditDao;
import mx.gob.morelos.ssi.models.reassignment.Reassignment;
import mx.gob.morelos.ssi.models.reassignment.ReassignmentDao;
import mx.gob.morelos.ssi.models.room.Room;
import mx.gob.morelos.ssi.models.room.RoomDao;
import mx.gob.morelos.ssi.models.toca.Toca;
import mx.gob.morelos.ssi.models.toca.TocaDao;
import mx.gob.morelos.ssi.models.user.User;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(name = "ReassignmentServlet",
        urlPatterns = {"/admin/reasignacion", "/admin/reasignacion/asignar", "/admin/reasignacion/crear"})
public class ReassignmentServlet extends HttpServlet {

    private ReassignmentDao reassignmentDao;
    private RoomDao roomDao;
    private TocaDao tocaDao;
    private AuditDao auditDao;
    private Gson gson;

    @Override
    public void init() {
        reassignmentDao = new ReassignmentDao();
        roomDao = new RoomDao();
        tocaDao = new TocaDao();
        auditDao = new AuditDao();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        List<Toca> tocasActivas = tocaDao.getAll().stream()
                .filter(t -> !"Resuelto".equals(t.getStatus()))
                .collect(Collectors.toList());
        req.setAttribute("tocasActivas", tocasActivas);

        User user = getUser(req);
        if (user == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }

        List<Reassignment> list = reassignmentDao.getAll();
        List<Room> rooms = roomDao.getActiveRooms();

        req.setAttribute("reassignments", list);
        req.setAttribute("rooms", rooms);
        req.setAttribute("activeView", "reassignment");
        req.getRequestDispatcher("/views/reassignment/reassignments.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

        User user = getUser(req);
        if (user == null) { sendJson(res, 401, false, "error", "Sesión no válida.", null); return; }

        if (!"/admin/reasignacion/asignar".equals(req.getServletPath())) {
            sendJson(res, 404, false, "error", "Ruta no encontrada.", null);
            return;
        }

        if ("/admin/reasignacion/crear".equals(req.getServletPath())) {
            createReassignment(req, res, user);
            return;
        }

        try {
            Long idReassignment = parseLong(req.getParameter("idReassignment"));
            Long idRoomTo = parseLong(req.getParameter("idRoomTo"));
            String reason = trim(req.getParameter("reason"));

            if (idReassignment == null || idRoomTo == null || reason.isBlank()) {
                sendJson(res, 400, false, "warning",
                        "Completa la sala destino y el motivo.", null);
                return;
            }

            Reassignment re = reassignmentDao.getById(idReassignment.intValue());
            if (re == null) {
                sendJson(res, 404, false, "error", "La solicitud no existe.", null);
                return;
            }

            re.setIdRoomTo(idRoomTo);
            re.setReason(reason);
            re.setStatus("Aprobada");

            if (!reassignmentDao.update(re)) {
                sendJson(res, 500, false, "error", "No fue posible actualizar la solicitud.", null);
                return;
            }

            // Actualizar la toca a la nueva sala
            Toca toca = tocaDao.getById(re.getIdToca().intValue());
            if (toca != null) {
                toca.setIdRoom(idRoomTo);
                tocaDao.update(toca);
                tocaDao.addHistory(toca.getIdToca(),
                        "Reasignada a sala " + idRoomTo + " · " + reason);
            }

            try {
                auditDao.log(user.getId(), user.getFullName(), "administrador",
                        "Reasignó una toca",
                        "Toca " + re.getTocaNumber() + " · " + reason,
                        "gold");
            } catch (RuntimeException ignored) {}

            sendJson(res, 200, true, "success", "Toca reasignada correctamente.", null);

        } catch (RuntimeException ex) {
            getServletContext().log("Error al reasignar.", ex);
            sendJson(res, 500, false, "error", "No fue posible reasignar la toca.", null);
        }
    }

    private void createReassignment(HttpServletRequest req, HttpServletResponse res, User user) throws IOException {
        try {
            Long idToca = parseLong(req.getParameter("idToca"));
            Long idRoomTo = parseLong(req.getParameter("idRoomTo"));
            String reason = trim(req.getParameter("reason"));

            if (idToca == null || idRoomTo == null || reason.isBlank()) {
                sendJson(res, 400, false, "warning", "Completa todos los campos.", null);
                return;
            }

            Toca toca = tocaDao.getById(idToca.intValue());
            if (toca == null || "Resuelto".equals(toca.getStatus())) {
                sendJson(res, 400, false, "warning", "La toca no existe o ya está resuelta.", null);
                return;
            }

            Reassignment re = new Reassignment();
            re.setIdToca(idToca);
            re.setIdRoomFrom(toca.getIdRoom());
            re.setIdRoomTo(idRoomTo);
            re.setReason(reason);
            re.setStatus("Requiere validación");
            re.setRequestedBy(user.getId());

            if (!reassignmentDao.create(re)) {
                sendJson(res, 500, false, "error", "No fue posible crear la solicitud.", null);
                return;
            }

            sendJson(res, 201, true, "success", "Solicitud de reasignación creada.", null);
        } catch (RuntimeException ex) {
            getServletContext().log("Error al crear reasignación", ex);
            sendJson(res, 500, false, "error", "Error interno.", null);
        }
    }

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