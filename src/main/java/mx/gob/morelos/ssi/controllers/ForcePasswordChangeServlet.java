package mx.gob.morelos.ssi.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.models.user.User;
import mx.gob.morelos.ssi.models.user.UserDao;
import mx.gob.morelos.ssi.utils.PasswordPolicy;

import java.io.IOException;

@WebServlet(name = "ForcePasswordChangeServlet", value = "/force-password-change")
public class ForcePasswordChangeServlet extends HttpServlet {

    private UserDao userDao;

    @Override
    public void init() { userDao = new UserDao(); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        User user = getUser(req);
        if (user == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }
        if (!user.requiresPasswordChange()) {
            res.sendRedirect(req.getContextPath() + targetFor(user)); return;
        }
        req.setAttribute("user", user);
        req.getRequestDispatcher("/force-password-change.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        User user = getUser(req);
        if (user == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }
        if (!user.requiresPasswordChange()) {
            res.sendRedirect(req.getContextPath() + targetFor(user)); return;
        }

        String pwd = req.getParameter("password");
        String confirm = req.getParameter("confirmation");

        if (pwd == null || pwd.isBlank() || confirm == null || confirm.isBlank()) {
            showError(req, res, user, "Completa la contraseña y su confirmación.");
            return;
        }
        if (!PasswordPolicy.isValid(pwd)) {
            showError(req, res, user, PasswordPolicy.getValidationMessage());
            return;
        }
        if (!pwd.equals(confirm)) {
            showError(req, res, user, "Las contraseñas no coinciden.");
            return;
        }

        if (!userDao.updatePasswordById(user.getId(), pwd)) {
            showError(req, res, user, "No fue posible actualizar la contraseña.");
            return;
        }

        user.setPassword(null);
        user.setMustChangePassword(0);
        user.setTemporaryPasswordExpiration(null);

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.setAttribute("usuario", user);
            session.removeAttribute("passwordChangeRequired");
        }

        res.sendRedirect(req.getContextPath() + targetFor(user));
    }

    private String targetFor(User user) {
        return "Administrador".equalsIgnoreCase(user.getRole())
                ? "/admin/distritos"
                : "/mis-tocas";
    }

    private User getUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        Object v = s.getAttribute("usuario");
        return v instanceof User u ? u : null;
    }

    private void showError(HttpServletRequest req, HttpServletResponse res, User user, String msg)
            throws ServletException, IOException {
        req.setAttribute("error", msg);
        req.setAttribute("user", user);
        req.getRequestDispatcher("/force-password-change.jsp").forward(req, res);
    }
}