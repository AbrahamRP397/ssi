package mx.gob.morelos.ssi.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.models.magistrate.Magistrate;
import mx.gob.morelos.ssi.models.magistrate.MagistrateDao;
import mx.gob.morelos.ssi.models.user.User;
import mx.gob.morelos.ssi.models.user.UserDao;

import java.io.IOException;

@WebServlet(name = "ProfileServlet", value = "/perfil")
public class ProfileServlet extends HttpServlet {

    private UserDao userDao;
    private MagistrateDao magistrateDao;

    @Override
    public void init() {
        userDao = new UserDao();
        magistrateDao = new MagistrateDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        User sessionUser = getUser(req);
        if (sessionUser == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }

        User user = userDao.getById(sessionUser.getId().intValue());
        if (user == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }

        // Distrito / último acceso como datos derivados
        String districtLabel = "—";
        String lastAccessLabel = "—";

        if ("Magistrado".equalsIgnoreCase(user.getRole())) {
            Magistrate m = magistrateDao.getByUserId(user.getId());
            if (m != null && m.getRoomName() != null && !"Sin sala".equals(m.getRoomName())) {
                districtLabel = "Sala " + m.getRoomName();
            }
            lastAccessLabel = "Reciente";
        } else {
            districtLabel = "Administración del sistema";
            lastAccessLabel = "Reciente";
        }

        // ✅ SOLUCIÓN: Pasamos los datos como atributos separados, no dentro del User
        req.setAttribute("profileUser", user);
        req.setAttribute("districtLabel", districtLabel);
        req.setAttribute("lastAccessLabel", lastAccessLabel);
        req.setAttribute("activeView", "profile");

        req.getRequestDispatcher("/views/user/profile.jsp").forward(req, res);
    }

    private User getUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        Object v = s.getAttribute("usuario");
        return v instanceof User u ? u : null;
    }
}