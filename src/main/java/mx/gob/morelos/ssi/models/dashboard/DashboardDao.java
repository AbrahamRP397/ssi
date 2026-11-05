package mx.gob.morelos.ssi.models.dashboard;

import mx.gob.morelos.ssi.models.ponencia.PonenciaDao;
import mx.gob.morelos.ssi.models.resolution.ResolutionDao;
import mx.gob.morelos.ssi.models.toca.TocaDao;
import mx.gob.morelos.ssi.utils.SQLConnector;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class DashboardDao {

    private final TocaDao tocaDao = new TocaDao();
    private final PonenciaDao ponenciaDao = new PonenciaDao();
    private final ResolutionDao resolutionDao = new ResolutionDao();

    /** Summary para el dashboard del magistrado. */
    public DashboardSummary forMagistrate(long idMagistrate) {
        DashboardSummary s = new DashboardSummary();

        int pending = tocaDao.countByMagistrateAndStatusDifferentFrom(idMagistrate, "Resuelto");
        int urgent = tocaDao.countByMagistrateAndStatus(idMagistrate, "Nueva");
        s.setPending(pending);
        s.setPendingHelper(urgent > 0 ? urgent + " con atención prioritaria" : "Sin pendientes urgentes");

        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        int week = ponenciaDao.countWeekByMagistrate(idMagistrate, monday, sunday);
        s.setWeekPonencias(week);
        s.setWeekPonenciasHelper(week == 0 ? "Sin sesiones esta semana" : "Ponencias programadas");

        int res = resolutionDao.countByMagistrate(idMagistrate);
        s.setResolutions(res);
        s.setResolutionsHelper(res == 0 ? "Aún sin resoluciones" : "Emitidas en total");

        return s;
    }

    /** Summary para el dashboard del admin. */
    public DashboardSummary forAdmin() {
        DashboardSummary s = new DashboardSummary();
        String sql = """
                SELECT
                  (SELECT COUNT(*) FROM rooms) AS total_rooms,
                  (SELECT COUNT(*) FROM rooms WHERE status = 1) AS active_rooms,
                  (SELECT COUNT(*) FROM magistrates WHERE status = 1) AS active_mags,
                  (SELECT COUNT(*) FROM districts) AS total_districts
                """;
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int total = rs.getInt("total_rooms");
                int active = rs.getInt("active_rooms");
                int mags = rs.getInt("active_mags");
                int districts = rs.getInt("total_districts");

                s.setTotalRooms(total);
                s.setRoomsHelper(active + " activas · " + (total - active) + " en configuración");
                s.setActiveMagistrates(mags);
                s.setMagistratesHelper("Cobertura institucional");
                s.setTotalDistricts(districts);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al calcular el resumen del admin.", e);
        }
        return s;
    }
}