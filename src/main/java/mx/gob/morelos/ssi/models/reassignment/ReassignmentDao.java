package mx.gob.morelos.ssi.models.reassignment;

import mx.gob.morelos.ssi.templates.Dao;
import mx.gob.morelos.ssi.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReassignmentDao implements Dao<Reassignment, Integer> {

    private static final String BASE_SELECT = """
            SELECT re.id_reassignment, re.id_toca, re.id_room_from, re.id_room_to,
                   re.reason, re.status, re.requested_by, re.created_at,
                   t.toca_number,
                   r_from.name AS current_room_name,
                   r_to.name AS target_room_name
            FROM reassignments re
            INNER JOIN tocas t ON re.id_toca = t.id_toca
            INNER JOIN rooms r_from ON re.id_room_from = r_from.id_room
            LEFT JOIN rooms r_to ON re.id_room_to = r_to.id_room
            """;

    @Override
    public List<Reassignment> getAll() {
        String sql = BASE_SELECT + " ORDER BY re.created_at DESC";
        List<Reassignment> list = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar las reasignaciones.", e);
        }
    }

    @Override
    public Reassignment getById(Integer id) {
        if (id == null || id <= 0) return null;
        String sql = BASE_SELECT + " WHERE re.id_reassignment = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar la reasignación.", e);
        }
    }

    @Override
    public boolean create(Reassignment r) {
        String sql = """
                INSERT INTO reassignments (id_toca, id_room_from, id_room_to, reason, status, requested_by)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, r.getIdToca());
            ps.setLong(2, r.getIdRoomFrom());
            if (r.getIdRoomTo() == null) ps.setNull(3, Types.BIGINT);
            else ps.setLong(3, r.getIdRoomTo());
            ps.setString(4, r.getReason());
            ps.setString(5, r.getStatus() == null ? "Requiere validación" : r.getStatus());
            if (r.getRequestedBy() == null) ps.setNull(6, Types.BIGINT);
            else ps.setLong(6, r.getRequestedBy());
            if (ps.executeUpdate() != 1) return false;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setIdReassignment(keys.getLong(1));
            }
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar la reasignación.", e);
        }
    }

    @Override
    public boolean update(Reassignment r) {
        String sql = """
                UPDATE reassignments
                SET id_toca = ?, id_room_from = ?, id_room_to = ?,
                    reason = ?, status = ?, requested_by = ?
                WHERE id_reassignment = ?
                """;
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, r.getIdToca());
            ps.setLong(2, r.getIdRoomFrom());
            if (r.getIdRoomTo() == null) ps.setNull(3, Types.BIGINT);
            else ps.setLong(3, r.getIdRoomTo());
            ps.setString(4, r.getReason());
            ps.setString(5, r.getStatus());
            if (r.getRequestedBy() == null) ps.setNull(6, Types.BIGINT);
            else ps.setLong(6, r.getRequestedBy());
            ps.setLong(7, r.getIdReassignment());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la reasignación.", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null || id <= 0) return false;
        String sql = "DELETE FROM reassignments WHERE id_reassignment = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la reasignación.", e);
        }
    }

    private Reassignment map(ResultSet rs) throws SQLException {
        Reassignment r = new Reassignment();
        r.setIdReassignment(rs.getLong("id_reassignment"));
        r.setIdToca(rs.getLong("id_toca"));
        r.setIdRoomFrom(rs.getLong("id_room_from"));
        long to = rs.getLong("id_room_to");
        r.setIdRoomTo(rs.wasNull() ? null : to);
        r.setReason(rs.getString("reason"));
        r.setStatus(rs.getString("status"));
        long reqBy = rs.getLong("requested_by");
        r.setRequestedBy(rs.wasNull() ? null : reqBy);
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setTocaNumber(rs.getString("toca_number"));
        r.setCurrentRoomName(rs.getString("current_room_name"));
        r.setTargetRoomName(rs.getString("target_room_name"));
        return r;
    }
}