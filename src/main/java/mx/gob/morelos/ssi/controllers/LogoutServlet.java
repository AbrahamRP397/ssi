package mx.gob.morelos.ssi.controllers;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.models.audit.AuditDao;
import mx.gob.morelos.ssi.models.user.User;

import java.io.IOException;

@WebServlet(name = "LogoutServlet", value = "/logout")
public class LogoutServlet extends HttpServlet {

    private AuditDao auditDao;

    @Override
    public void init() { auditDao = new AuditDao(); }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object v = session.getAttribute("usuario");
            if (v instanceof User user) {
                try {
                    auditDao.log(user.getId(), user.getFullName(), user.getRole(),
                            "Cerró sesión", "Fin de sesión", "blue");
                } catch (RuntimeException ignored) {}
            }
            try { session.invalidate(); } catch (IllegalStateException ignored) {}
        }
        res.sendRedirect(req.getContextPath() + "/login");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "Utiliza POST para cerrar sesión.");
    }
}