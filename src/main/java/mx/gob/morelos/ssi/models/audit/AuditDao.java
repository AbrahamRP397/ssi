package mx.gob.morelos.ssi.models.audit;

import mx.gob.morelos.ssi.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditDao {

    public List<AuditEvent> getAll() {
        String sql = """
                SELECT id_event, id_user, actor_name, role, action, detail, tone, occurred_at
                FROM audit_events
                ORDER BY occurred_at DESC, id_event DESC
                """;
        List<AuditEvent> list = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar la auditoría.", e);
        }
    }

    public void log(Long idUser, String actorName, String role,
                    String action, String detail, String tone) {
        String sql = """
                INSERT INTO audit_events (id_user, actor_name, role, action, detail, tone)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idUser == null) ps.setNull(1, Types.BIGINT);
            else ps.setLong(1, idUser);
            ps.setString(2, actorName);
            ps.setString(3, role);
            ps.setString(4, action);
            ps.setString(5, detail);
            ps.setString(6, tone == null ? "blue" : tone);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar evento de auditoría.", e);
        }
    }

    private AuditEvent map(ResultSet rs) throws SQLException {
        AuditEvent e = new AuditEvent();
        e.setIdEvent(rs.getLong("id_event"));
        long idUser = rs.getLong("id_user");
        e.setIdUser(rs.wasNull() ? null : idUser);
        e.setActorName(rs.getString("actor_name"));
        e.setRole(rs.getString("role"));
        e.setAction(rs.getString("action"));
        e.setDetail(rs.getString("detail"));
        e.setTone(rs.getString("tone"));
        e.setOccurredAt(rs.getTimestamp("occurred_at"));
        return e;
    }
}