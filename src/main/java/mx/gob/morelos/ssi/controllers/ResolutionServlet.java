package mx.gob.morelos.ssi.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.models.magistrate.Magistrate;
import mx.gob.morelos.ssi.models.magistrate.MagistrateDao;
import mx.gob.morelos.ssi.models.resolution.Resolution;
import mx.gob.morelos.ssi.models.resolution.ResolutionDao;
import mx.gob.morelos.ssi.models.user.User;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "ResolutionServlet", urlPatterns = {"/resoluciones", "/resoluciones/*"})
public class ResolutionServlet extends HttpServlet {

    private ResolutionDao resolutionDao;
    private MagistrateDao magistrateDao;

    @Override
    public void init() {
        resolutionDao = new ResolutionDao();
        magistrateDao = new MagistrateDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        User user = getUser(req);
        if (user == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }

        Magistrate mag = magistrateDao.getByUserId(user.getId());
        if (mag == null) { res.sendError(403); return; }

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            List<Resolution> list = resolutionDao.getByMagistrate(mag.getIdMagistrate());
            req.setAttribute("resolutions", list);
            req.setAttribute("activeView", "resolutions");
            req.getRequestDispatcher("/views/resolution/resolutions.jsp").forward(req, res);
            return;
        }

        // /resoluciones/{id}/pdf
        String[] parts = pathInfo.substring(1).split("/");
        if (parts.length == 2 && "pdf".equals(parts[1])) {
            Long id = parseLong(parts[0]);
            if (id == null) { res.sendError(404); return; }
            Resolution r = resolutionDao.getById(id.intValue());
            if (r == null || !mag.getIdMagistrate().equals(r.getIdMagistrate())) {
                res.sendError(404);
                return;
            }
            // TODO: generar/entregar PDF real en Fase 5
            res.setContentType("application/pdf");
            res.setHeader("Content-Disposition",
                    "attachment; filename=\"" + r.getTocaNumber().replace("/", "-") + ".pdf\"");
            res.getOutputStream().write(new byte[0]);
            return;
        }

        res.sendError(404);
    }

    private Long parseLong(String v) {
        if (v == null) return null;
        try { long n = Long.parseLong(v.trim()); return n > 0 ? n : null; }
        catch (NumberFormatException e) { return null; }
    }

    private User getUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        Object v = s.getAttribute("usuario");
        return v instanceof User u ? u : null;
    }
}