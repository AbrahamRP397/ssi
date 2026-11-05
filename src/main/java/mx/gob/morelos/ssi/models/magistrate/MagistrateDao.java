package mx.gob.morelos.ssi.models.magistrate;

import mx.gob.morelos.ssi.templates.Dao;
import mx.gob.morelos.ssi.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MagistrateDao implements Dao<Magistrate, Integer> {

    private static final String BASE_SELECT = """
            SELECT m.id_magistrate, m.id_user, m.id_room, m.status,
                   TRIM(COALESCE(u.name,'') || ' ' || COALESCE(u.surname,'') || ' ' || COALESCE(u.lastname,'')) AS full_name,
                   u.email,
                   r.name AS room_name
            FROM magistrates m
            INNER JOIN users u ON m.id_user = u.id_user
            LEFT JOIN rooms r ON m.id_room = r.id_room
            """;

    @Override
    public List<Magistrate> getAll() {
        String sql = BASE_SELECT + " WHERE m.status = 1 ORDER BY UPPER(u.name), UPPER(u.surname)";
        List<Magistrate> list = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar los magistrados.", e);
        }
    }

    public Magistrate getByUserId(long idUser) {
        String sql = BASE_SELECT + " WHERE m.id_user = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el magistrado.", e);
        }
    }

    @Override
    public Magistrate getById(Integer id) {
        if (id == null || id <= 0) return null;
        String sql = BASE_SELECT + " WHERE m.id_magistrate = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el magistrado.", e);
        }
    }

    @Override
    public boolean create(Magistrate m) {
        String sql = "INSERT INTO magistrates (id_user, id_room, status) VALUES (?, ?, ?)";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, m.getIdUser());
            if (m.getIdRoom() == null) ps.setNull(2, Types.BIGINT);
            else ps.setLong(2, m.getIdRoom());
            ps.setInt(3, m.getStatus() == null ? 1 : m.getStatus());
            if (ps.executeUpdate() != 1) return false;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) m.setIdMagistrate(keys.getLong(1));
            }
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear el magistrado.", e);
        }
    }

    @Override
    public boolean update(Magistrate m) {
        String sql = "UPDATE magistrates SET id_room = ?, status = ? WHERE id_magistrate = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (m.getIdRoom() == null) ps.setNull(1, Types.BIGINT);
            else ps.setLong(1, m.getIdRoom());
            ps.setInt(2, m.getStatus() == null ? 1 : m.getStatus());
            ps.setLong(3, m.getIdMagistrate());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el magistrado.", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null || id <= 0) return false;
        String sql = "UPDATE magistrates SET status = 0 WHERE id_magistrate = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al desactivar el magistrado.", e);
        }
    }

    // NUEVO MÉTODO: Verificar si el magistrado tiene tocas pendientes
    public boolean hasPendingTocas(long idMagistrate) {
        String sql = "SELECT COUNT(*) FROM tocas WHERE id_magistrate = ? AND status <> 'Resuelto'";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idMagistrate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar tocas pendientes del magistrado.", e);
        }
        return false;
    }

    // NUEVO MÉTODO: Buscar magistrado activo por ID de sala
    public Magistrate getByRoomId(long idRoom) {
        String sql = BASE_SELECT + " WHERE m.id_room = ? AND m.status = 1";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idRoom);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el magistrado por sala.", e);
        }
    }

    private Magistrate map(ResultSet rs) throws SQLException {
        Magistrate m = new Magistrate();
        m.setIdMagistrate(rs.getLong("id_magistrate"));
        m.setIdUser(rs.getLong("id_user"));
        long idRoom = rs.getLong("id_room");
        m.setIdRoom(rs.wasNull() ? null : idRoom);
        m.setStatus(rs.getInt("status"));
        m.setFullName(rs.getString("full_name"));
        m.setEmail(rs.getString("email"));
        String rn = rs.getString("room_name");
        m.setRoomName(rn == null ? "Sin sala" : rn);
        return m;
    }
}