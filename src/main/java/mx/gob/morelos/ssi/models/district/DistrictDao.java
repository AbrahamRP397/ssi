package mx.gob.morelos.ssi.models.district;

import mx.gob.morelos.ssi.templates.Dao;
import mx.gob.morelos.ssi.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DistrictDao implements Dao<District, Integer> {

    @Override
    public List<District> getAll() {
        String sql = "SELECT id_district, name, head_city, status FROM districts ORDER BY UPPER(name)";
        List<District> list = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar los distritos.", e);
        }
    }

    @Override
    public District getById(Integer id) {
        if (id == null || id <= 0) return null;
        String sql = "SELECT id_district, name, head_city, status FROM districts WHERE id_district = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el distrito.", e);
        }
    }

    // Los distritos vienen de la API (Primera Instancia). No se crean aquí.
    @Override
    public boolean create(District d) {
        throw new UnsupportedOperationException("Los distritos son un catálogo de solo lectura.");
    }

    // No se pueden modificar.
    @Override
    public boolean update(District d) {
        throw new UnsupportedOperationException("Los distritos son un catálogo de solo lectura.");
    }

    // No se pueden desactivar manualmente.
    @Override
    public boolean delete(Integer id) {
        throw new UnsupportedOperationException("Los distritos son un catálogo de solo lectura.");
    }

    private District map(ResultSet rs) throws SQLException {
        District d = new District();
        d.setIdDistrict(rs.getLong("id_district"));
        d.setName(rs.getString("name"));
        d.setHeadCity(rs.getString("head_city"));
        d.setStatus(rs.getInt("status"));
        return d;
    }
}