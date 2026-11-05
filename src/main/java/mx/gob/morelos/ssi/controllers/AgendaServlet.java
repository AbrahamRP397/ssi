package mx.gob.morelos.ssi.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.models.magistrate.Magistrate;
import mx.gob.morelos.ssi.models.magistrate.MagistrateDao;
import mx.gob.morelos.ssi.models.ponencia.Ponencia;
import mx.gob.morelos.ssi.models.ponencia.PonenciaDao;
import mx.gob.morelos.ssi.models.user.User;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@WebServlet(name = "AgendaServlet", value = "/agenda")
public class AgendaServlet extends HttpServlet {

    private PonenciaDao ponenciaDao;
    private MagistrateDao magistrateDao;

    private static final String[] HOURS = {
            "08:00","09:00","10:00","11:00","12:00","13:00",
            "14:00","15:00","16:00","17:00","18:00"
    };

    @Override
    public void init() {
        ponenciaDao = new PonenciaDao();
        magistrateDao = new MagistrateDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        User user = getUser(req);
        if (user == null) { res.sendRedirect(req.getContextPath() + "/login"); return; }

        Magistrate mag = magistrateDao.getByUserId(user.getId());
        if (mag == null) { res.sendError(403); return; }

        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate friday = monday.plusDays(4);
        LocalDate sunday = monday.plusDays(6);

        List<Ponencia> ponencias = ponenciaDao.getWeekByMagistrate(
                mag.getIdMagistrate(), monday, sunday);

        // Days para el grid
        List<Map<String, Object>> days = new ArrayList<>();
        String[] dayNames = {"Lun","Mar","Mié","Jue","Vie"};
        for (int i = 0; i < 5; i++) {
            LocalDate d = monday.plusDays(i);
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("short", dayNames[i]);
            day.put("dayNumber", d.getDayOfMonth());
            days.add(day);
        }

        // Items para el calendario
        List<Map<String, Object>> items = new ArrayList<>();
        for (Ponencia p : ponencias) {
            int dayIdx = (int) (p.getSessionDate().toLocalDate().toEpochDay() - monday.toEpochDay());
            if (dayIdx < 0 || dayIdx > 4) continue;
            int startHour = p.getStartTime().toLocalTime().getHour() - 8;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("day", dayIdx);
            item.put("start", Math.max(0, startHour));
            item.put("duration", p.getDurationHours());
            item.put("time", p.getTimeLabel());
            item.put("tocaNumber", p.getTocaNumber());
            item.put("originalCaseNumber", p.getOriginalCaseNumber());
            item.put("kind", p.getKind() == null ? "Ponencia" : p.getKind());
            items.add(item);
        }

        req.setAttribute("weekLabel", formatWeek(monday, friday));
        req.setAttribute("days", days);
        req.setAttribute("hours", Arrays.asList(HOURS));
        req.setAttribute("agendaItems", items);
        req.setAttribute("activeView", "agenda");

        req.getRequestDispatcher("/views/agenda/agenda.jsp").forward(req, res);
    }

    private String formatWeek(LocalDate monday, LocalDate friday) {
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("d", new Locale("es","MX"));
        java.text.SimpleDateFormat mf = new java.text.SimpleDateFormat("MMMM", new Locale("es","MX"));
        return df.format(java.sql.Date.valueOf(monday))
                + " al " + df.format(java.sql.Date.valueOf(friday))
                + " de " + mf.format(java.sql.Date.valueOf(friday));
    }

    private User getUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        Object v = s.getAttribute("usuario");
        return v instanceof User u ? u : null;
    }
}