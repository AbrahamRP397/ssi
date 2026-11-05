package mx.gob.morelos.ssi.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.models.dashboard.DashboardDao;
import mx.gob.morelos.ssi.models.district.District;
import mx.gob.morelos.ssi.models.district.DistrictDao;
import mx.gob.morelos.ssi.models.room.Room;
import mx.gob.morelos.ssi.models.room.RoomDao;
import mx.gob.morelos.ssi.models.user.User;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "DistrictServlet", value = "/admin/distritos")
public class DistrictServlet extends HttpServlet {

    private DistrictDao districtDao;
    private RoomDao roomDao;
    private DashboardDao dashboardDao;

    @Override
    public void init() {
        districtDao = new DistrictDao();
        roomDao = new RoomDao();
        dashboardDao = new DashboardDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        User user = getUser(req);
        if (user == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }

        List<District> districts = districtDao.getAll();
        List<Room> rooms = roomDao.getAll();

        // Enriquecer cada distrito con su conteo de salas usando Streams
        java.util.Map<Long, Long> roomCountByDistrict = rooms.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Room::getIdDistrict, java.util.stream.Collectors.counting()));

        req.setAttribute("districts", districts);
        req.setAttribute("roomCountByDistrict", roomCountByDistrict);
        req.setAttribute("stats", dashboardDao.forAdmin());
        req.setAttribute("activeView", "districts");

        req.getRequestDispatcher("/views/district/districts.jsp").forward(req, res);
    }

    private User getUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        Object v = s.getAttribute("usuario");
        return v instanceof User u ? u : null;
    }
}