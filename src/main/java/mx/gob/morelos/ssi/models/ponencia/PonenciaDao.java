package mx.gob.morelos.ssi.models.ponencia;

import mx.gob.morelos.ssi.templates.Dao;
import mx.gob.morelos.ssi.utils.SQLConnector;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PonenciaDao implements Dao<Ponencia, Integer> {

    private static final String BASE_SELECT = """
            SELECT p.id_ponencia, p.id_toca, p.id_magistrate, p.session_date,
                   p.start_time, p.end_time, p.kind, p.created_at,
                   t.toca_number, t.original_case_number
            FROM ponencias p
            INNER JOIN tocas t ON p.id_toca = t.id_toca
            """;

    @Override
    public List<Ponencia> getAll() {
        String sql = BASE_SELECT + " ORDER BY p.session_date DESC, p.start_time";
        List<Ponencia> list = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar las ponencias.", e);
        }
    }

    public List<Ponencia> getWeekByMagistrate(long idMagistrate, LocalDate weekStart, LocalDate weekEnd) {
        String sql = BASE_SELECT + """
                 WHERE p.id_magistrate = ?
                   AND p.session_date BETWEEN ? AND ?
                 ORDER BY p.session_date, p.start_time
                """;
        List<Ponencia> list = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idMagistrate);
            ps.setDate(2, Date.valueOf(weekStart));
            ps.setDate(3, Date.valueOf(weekEnd));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar la agenda.", e);
        }
    }

    public int countWeekByMagistrate(long idMagistrate, LocalDate weekStart, LocalDate weekEnd) {
        String sql = """
                SELECT COUNT(*) FROM ponencias
                WHERE id_magistrate = ?
                  AND session_date BETWEEN ? AND ?
                """;
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idMagistrate);
            ps.setDate(2, Date.valueOf(weekStart));
            ps.setDate(3, Date.valueOf(weekEnd));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar ponencias.", e);
        }
    }

    @Override
    public Ponencia getById(Integer id) {
        if (id == null || id <= 0) return null;
        String sql = BASE_SELECT + " WHERE p.id_ponencia = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar la ponencia.", e);
        }
    }

    @Override
    public boolean create(Ponencia p) {
        String sql = """
                INSERT INTO ponencias (id_toca, id_magistrate, session_date, start_time, end_time, kind)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, p.getIdToca());
            ps.setLong(2, p.getIdMagistrate());
            ps.setDate(3, p.getSessionDate());
            ps.setTime(4, p.getStartTime());
            ps.setTime(5, p.getEndTime());
            ps.setString(6, p.getKind());
            if (ps.executeUpdate() != 1) return false;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setIdPonencia(keys.getLong(1));
            }
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar la ponencia.", e);
        }
    }

    @Override
    public boolean update(Ponencia p) {
        String sql = """
                UPDATE ponencias
                SET id_toca = ?, id_magistrate = ?, session_date = ?,
                    start_time = ?, end_time = ?, kind = ?
                WHERE id_ponencia = ?
                """;
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, p.getIdToca());
            ps.setLong(2, p.getIdMagistrate());
            ps.setDate(3, p.getSessionDate());
            ps.setTime(4, p.getStartTime());
            ps.setTime(5, p.getEndTime());
            ps.setString(6, p.getKind());
            ps.setLong(7, p.getIdPonencia());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la ponencia.", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null || id <= 0) return false;
        String sql = "DELETE FROM ponencias WHERE id_ponencia = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la ponencia.", e);
        }
    }

    // NUEVO MÉTODO: Verificar si hay cruce de horarios con 1 hora de holgura
    public boolean hasScheduleConflict(long idMagistrate, java.sql.Date sessionDate, java.sql.Time startTime, java.sql.Time endTime) {
        // Sumamos 1 hora a la hora de inicio y restamos 1 hora a la hora de fin para la holgura
        String sql = """
                SELECT COUNT(*) FROM ponencias 
                WHERE id_magistrate = ? 
                  AND session_date = ?
                  AND (
                      (start_time <= ? AND end_time >= ?) OR
                      (start_time >= ? AND start_time < ?) OR
                      (end_time > ? AND end_time <= ?)
                  )
                """;
        try (java.sql.Connection conn = mx.gob.morelos.ssi.utils.SQLConnector.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            // Lógica: La nueva ponencia no puede empezar antes de que termine la anterior + 1 hora
            // ni terminar después de que empiece la siguiente - 1 hora.
            // Para simplificar, revisamos si el rango [start - 1h, end + 1h] choca con algo.

            long startMs = startTime.getTime();
            long endMs = endTime.getTime();
            long oneHourMs = 60 * 60 * 1000;

            java.sql.Time startWithBuffer = new java.sql.Time(startMs - oneHourMs);
            java.sql.Time endWithBuffer = new java.sql.Time(endMs + oneHourMs);

            ps.setLong(1, idMagistrate);
            ps.setDate(2, sessionDate);

            // Revisamos si alguna ponencia existente choca con nuestro bloque de tiempo extendido
            ps.setTime(3, endWithBuffer); // start_time <= (nuestro end + 1h)
            ps.setTime(4, startWithBuffer); // end_time >= (nuestro start - 1h)

            ps.setTime(5, startWithBuffer); // start_time >= (nuestro start - 1h)
            ps.setTime(6, endWithBuffer); // start_time < (nuestro end + 1h)

            ps.setTime(7, startWithBuffer); // end_time > (nuestro start - 1h)
            ps.setTime(8, endWithBuffer); // end_time <= (nuestro end + 1h)

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error al verificar conflictos de horario.", e);
        }
        return false;
    }

    private Ponencia map(ResultSet rs) throws SQLException {
        Ponencia p = new Ponencia();
        p.setIdPonencia(rs.getLong("id_ponencia"));
        p.setIdToca(rs.getLong("id_toca"));
        p.setIdMagistrate(rs.getLong("id_magistrate"));
        p.setSessionDate(rs.getDate("session_date"));
        p.setStartTime(rs.getTime("start_time"));
        p.setEndTime(rs.getTime("end_time"));
        p.setKind(rs.getString("kind"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setTocaNumber(rs.getString("toca_number"));
        p.setOriginalCaseNumber(rs.getString("original_case_number"));
        return p;
    }
}