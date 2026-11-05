package mx.gob.morelos.ssi.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.models.audit.AuditDao;
import mx.gob.morelos.ssi.models.audit.AuditEvent;
import mx.gob.morelos.ssi.models.user.User;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AuditServlet", value = "/admin/auditoria")
public class AuditServlet extends HttpServlet {

    private AuditDao auditDao;

    @Override
    public void init() { auditDao = new AuditDao(); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        User user = getUser(req);
        if (user == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }

        List<AuditEvent> events = auditDao.getAll();
        req.setAttribute("events", events);
        req.setAttribute("activeView", "audit");
        req.getRequestDispatcher("/views/audit/audit.jsp").forward(req, res);
    }

    private User getUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        Object v = s.getAttribute("usuario");
        return v instanceof User u ? u : null;
    }
}