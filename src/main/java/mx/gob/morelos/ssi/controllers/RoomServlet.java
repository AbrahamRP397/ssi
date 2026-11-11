package mx.gob.morelos.ssi.controllers;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.models.audit.AuditDao;
import mx.gob.morelos.ssi.models.dashboard.DashboardDao;
import mx.gob.morelos.ssi.models.district.District;
import mx.gob.morelos.ssi.models.district.DistrictDao;
import mx.gob.morelos.ssi.models.room.Room;
import mx.gob.morelos.ssi.models.room.RoomDao;
import mx.gob.morelos.ssi.models.user.User;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "RoomServlet", urlPatterns = {
        "/admin/salas",
        "/admin/salas/save",
        "/admin/salas/editar",
        "/admin/salas/desactivar"
})
public class RoomServlet extends HttpServlet {

    private RoomDao roomDao;
    private DistrictDao districtDao;
    private DashboardDao dashboardDao;
    private AuditDao auditDao;
    private Gson gson;

    @Override
    public void init() {
        roomDao = new RoomDao();
        districtDao = new DistrictDao();
        dashboardDao = new DashboardDao();
        auditDao = new AuditDao();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User user = getUser(req);
        if (user == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }

        List<Room> rooms = roomDao.getAll();
        List<District> districts = districtDao.getAll();

        req.setAttribute("rooms", rooms);
        req.setAttribute("districts", districts);
        req.setAttribute("stats", dashboardDao.forAdmin());
        req.setAttribute("activeView", "rooms");
        req.getRequestDispatcher("/views/room/rooms.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        User user = getUser(req);
        if (user == null) { sendJson(res, 401, false, "error", "Sesión no válida.", null); return; }

        if ("/admin/salas/save".equals(req.getServletPath())) {
            saveRoom(req, res, user);
        } else if ("/admin/salas/editar".equals(req.getServletPath())) {
            updateRoom(req, res, user);
        } else if ("/admin/salas/desactivar".equals(req.getServletPath())) {
            deactivateRoom(req, res, user);
        } else {
            sendJson(res, 404, false, "error", "Ruta no encontrada.", null);
        }
    }

    private void saveRoom(HttpServletRequest req, HttpServletResponse res, User user) throws IOException {
        try {
            String name = trim(req.getParameter("name"));
            String address = trim(req.getParameter("address"));
            Long idDistrict = parseLong(req.getParameter("idDistrict"));

            if (name.isBlank() || address.isBlank() || idDistrict == null) {
                sendJson(res, 400, false, "warning", "Completa el nombre, la dirección y el distrito.", null);
                return;
            }

            District d = districtDao.getById(idDistrict.intValue());
            if (d == null) {
                sendJson(res, 400, false, "warning", "Distrito no válido.", null);
                return;
            }

            Room r = new Room();
            r.setIdDistrict(idDistrict);
            r.setName(name);
            r.setAddress(address);
            r.setStatus(1);

            if (!roomDao.create(r)) {
                sendJson(res, 500, false, "error", "No fue posible crear la sala.", null);
                return;
            }

            try {
                auditDao.log(user.getId(), user.getFullName(), "administrador",
                        "Registró una sala", name + " · " + d.getName(), "gold");
            } catch (RuntimeException ignored) {}

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("idRoom", r.getIdRoom());

            sendJson(res, 201, true, "success", "Sala registrada correctamente.", data);
        } catch (RuntimeException ex) {
            getServletContext().log("Error al crear sala.", ex);
            sendJson(res, 500, false, "error", "No fue posible crear la sala.", null);
        }
    }

    private void updateRoom(HttpServletRequest req, HttpServletResponse res, User user) throws IOException {
        try {
            Long idRoom = parseLong(req.getParameter("idRoom"));
            String name = trim(req.getParameter("name"));
            String address = trim(req.getParameter("address"));
            Long idDistrict = parseLong(req.getParameter("idDistrict"));

            if (idRoom == null || name.isBlank() || address.isBlank() || idDistrict == null) {
                sendJson(res, 400, false, "warning", "Completa todos los campos.", null);
                return;
            }

            Room r = roomDao.getById(idRoom.intValue());
            if (r == null) {
                sendJson(res, 404, false, "error", "La sala no existe.", null);
                return;
            }

            District d = districtDao.getById(idDistrict.intValue());
            if (d == null) {
                sendJson(res, 400, false, "warning", "Distrito no válido.", null);
                return;
            }

            r.setName(name);
            r.setAddress(address);
            r.setIdDistrict(idDistrict);

            if (!roomDao.update(r)) {
                sendJson(res, 500, false, "error", "No fue posible actualizar la sala.", null);
                return;
            }

            try {
                auditDao.log(user.getId(), user.getFullName(), "administrador",
                        "Editó una sala", name + " · " + d.getName(), "gold");
            } catch (RuntimeException ignored) {}

            sendJson(res, 200, true, "success", "Sala actualizada correctamente.", null);
        } catch (RuntimeException ex) {
            getServletContext().log("Error al editar sala.", ex);
            sendJson(res, 500, false, "error", ex.getMessage() != null && ex.getMessage().contains("tocas")
                    ? ex.getMessage()
                    : "No fue posible actualizar la sala.", null);
        }
    }

    private void deactivateRoom(HttpServletRequest req, HttpServletResponse res, User user) throws IOException {
        try {
            Long idRoom = parseLong(req.getParameter("idRoom"));
            if (idRoom == null) {
                sendJson(res, 400, false, "warning", "Sala no válida.", null);
                return;
            }

            Room r = roomDao.getById(idRoom.intValue());
            if (r == null) {
                sendJson(res, 404, false, "error", "La sala no existe.", null);
                return;
            }

            if (roomDao.hasPendingTocas(idRoom.intValue())) {
                sendJson(res, 409, false, "warning",
                        "No se puede desactivar la sala porque tiene tocas pendientes de resolución.", null);
                return;
            }

            r.setStatus(r.getStatus() == 1 ? 0 : 1);
            if (!roomDao.update(r)) {
                sendJson(res, 500, false, "error", "No fue posible desactivar la sala.", null);
                return;
            }

            try {
                auditDao.log(user.getId(), user.getFullName(), "administrador",
                        "Desactivó una sala", r.getName(), "crimson");
            } catch (RuntimeException ignored) {}

            sendJson(res, 200, true, "success", "Sala desactivada correctamente.", null);
        } catch (RuntimeException ex) {
            getServletContext().log("Error al desactivar sala.", ex);
            sendJson(res, 500, false, "error", "No fue posible desactivar la sala.", null);
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