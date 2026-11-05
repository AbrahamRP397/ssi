package mx.gob.morelos.ssi.models.room;

import mx.gob.morelos.ssi.templates.Dao;
import mx.gob.morelos.ssi.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDao implements Dao<Room, Integer> {

    // ¡OJO! Aquí ya NO pedimos r.address en el SELECT
    private static final String BASE_SELECT = """
            SELECT r.id_room, r.id_district, r.name, r.status,
                   d.name AS district_name,
                   TRIM(COALESCE(u.name,'') || ' ' || COALESCE(u.surname,'') || ' ' || COALESCE(u.lastname,'')) AS magistrate_name,
                   (SELECT COUNT(*) FROM tocas t
                    WHERE t.id_room = r.id_room
                      AND t.status <> 'Resuelto') AS active_tocas
            FROM rooms r
            INNER JOIN districts d ON r.id_district = d.id_district
            LEFT JOIN magistrates m ON m.id_room = r.id_room AND m.status = 1
            LEFT JOIN users u ON m.id_user = u.id_user
            """;

    @Override
    public List<Room> getAll() {
        String sql = BASE_SELECT + " ORDER BY UPPER(d.name), UPPER(r.name)";
        List<Room> list = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar las salas.", e);
        }
    }

    public List<Room> getActiveRooms() {
        String sql = BASE_SELECT + " WHERE r.status = 1 ORDER BY UPPER(r.name)";
        List<Room> list = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar las salas activas.", e);
        }
    }

    @Override
    public Room getById(Integer id) {
        if (id == null || id <= 0) return null;
        String sql = BASE_SELECT + " WHERE r.id_room = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar la sala.", e);
        }
    }

    @Override
    public boolean create(Room r) {
        // Insertamos, pero si falla por address, no importa
        String sql = "INSERT INTO rooms (id_district, name, status) VALUES (?, ?, ?)";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, r.getIdDistrict());
            ps.setString(2, r.getName());
            ps.setInt(3, r.getStatus() == null ? 1 : r.getStatus());
            if (ps.executeUpdate() != 1) return false;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setIdRoom(keys.getLong(1));
            }
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear la sala.", e);
        }
    }

    @Override
    public boolean update(Room r) {
        // 1. Validar si cambió el distrito
        Room original = getById(r.getIdRoom().intValue());
        if (original != null && !original.getIdDistrict().equals(r.getIdDistrict())) {
            // Si cambió el distrito, verificar si tiene tocas
            String checkSql = "SELECT COUNT(*) FROM tocas WHERE id_room = ?";
            try (Connection conn = SQLConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setLong(1, r.getIdRoom());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new RuntimeException("No se puede cambiar el distrito de una sala que ya tiene tocas asignadas.");
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al validar cambio de distrito.", e);
            }
        }

        // 2. Proceder con la actualización normal
        String sql = "UPDATE rooms SET id_district = ?, name = ?, address = ?, status = ? WHERE id_room = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, r.getIdDistrict());
            ps.setString(2, r.getName());
            ps.setString(3, r.getAddress()); // Ojo: asegúrate de que Room tenga el getter/setter de address
            ps.setInt(4, r.getStatus() == null ? 1 : r.getStatus());
            ps.setLong(5, r.getIdRoom());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la sala.", e);
        }
    }

    public boolean hasPendingTocas(int idRoom) {
        String sql = "SELECT COUNT(*) FROM tocas WHERE id_room = ? AND status <> 'Resuelto'";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRoom);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar tocas pendientes.", e);
        }
        return false;
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null || id <= 0) return false;
        String sql = "UPDATE rooms SET status = 0 WHERE id_room = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al desactivar la sala.", e);
        }
    }

    private Room map(ResultSet rs) {
        Room r = new Room();
        try {
            r.setIdRoom(rs.getLong("id_room"));
            r.setIdDistrict(rs.getLong("id_district"));
            r.setName(rs.getString("name"));
            // Como no pedimos address en el SELECT, aquí lo ponemos directo
            r.setAddress("Sin dirección");
            r.setStatus(rs.getInt("status"));
            r.setDistrictName(rs.getString("district_name"));
            String mag = rs.getString("magistrate_name");
            r.setMagistrateName(mag == null || mag.isBlank() ? "Sin asignar" : mag.trim());
            r.setActiveTocas(rs.getInt("active_tocas"));
        } catch (SQLException e) {
            throw new RuntimeException("Error al mapear la sala.", e);
        }
        return r;
    }
}