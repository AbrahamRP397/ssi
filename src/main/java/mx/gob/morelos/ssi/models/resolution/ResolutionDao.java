package mx.gob.morelos.ssi.models.resolution;

import mx.gob.morelos.ssi.templates.Dao;
import mx.gob.morelos.ssi.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResolutionDao implements Dao<Resolution, Integer> {

    private static final String BASE_SELECT = """
            SELECT r.id_resolution, r.id_toca, r.id_magistrate, r.outcome,
                   r.body, r.observations, r.pdf_url, r.emitted_at,
                   t.toca_number
            FROM resolutions r
            INNER JOIN tocas t ON r.id_toca = t.id_toca
            """;

    @Override
    public List<Resolution> getAll() {
        String sql = BASE_SELECT + " ORDER BY r.emitted_at DESC";
        List<Resolution> list = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar las resoluciones.", e);
        }
    }

    public List<Resolution> getByMagistrate(long idMagistrate) {
        String sql = BASE_SELECT + " WHERE r.id_magistrate = ? ORDER BY r.emitted_at DESC";
        List<Resolution> list = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idMagistrate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar las resoluciones del magistrado.", e);
        }
    }

    public int countByMagistrate(long idMagistrate) {
        String sql = "SELECT COUNT(*) FROM resolutions WHERE id_magistrate = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idMagistrate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar resoluciones.", e);
        }
    }

    @Override
    public Resolution getById(Integer id) {
        if (id == null || id <= 0) return null;
        String sql = BASE_SELECT + " WHERE r.id_resolution = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar la resolución.", e);
        }
    }

    @Override
    public boolean create(Resolution r) {
        String sql = """
                INSERT INTO resolutions (id_toca, id_magistrate, outcome, body, observations, pdf_url)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, r.getIdToca());
            ps.setLong(2, r.getIdMagistrate());
            ps.setString(3, r.getOutcome());
            ps.setString(4, r.getBody());
            ps.setString(5, r.getObservations());
            ps.setString(6, r.getPdfUrl());
            if (ps.executeUpdate() != 1) return false;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setIdResolution(keys.getLong(1));
            }
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar la resolución.", e);
        }
    }

    @Override
    public boolean update(Resolution r) {
        String sql = """
                UPDATE resolutions
                SET id_toca = ?, id_magistrate = ?, outcome = ?, body = ?,
                    observations = ?, pdf_url = ?
                WHERE id_resolution = ?
                """;
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, r.getIdToca());
            ps.setLong(2, r.getIdMagistrate());
            ps.setString(3, r.getOutcome());
            ps.setString(4, r.getBody());
            ps.setString(5, r.getObservations());
            ps.setString(6, r.getPdfUrl());
            ps.setLong(7, r.getIdResolution());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la resolución.", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null || id <= 0) return false;
        String sql = "DELETE FROM resolutions WHERE id_resolution = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la resolución.", e);
        }
    }

    private Resolution map(ResultSet rs) throws SQLException {
        Resolution r = new Resolution();
        r.setIdResolution(rs.getLong("id_resolution"));
        r.setIdToca(rs.getLong("id_toca"));
        r.setIdMagistrate(rs.getLong("id_magistrate"));
        r.setOutcome(rs.getString("outcome"));
        r.setBody(rs.getString("body"));
        r.setObservations(rs.getString("observations"));
        r.setPdfUrl(rs.getString("pdf_url"));
        r.setEmittedAt(rs.getTimestamp("emitted_at"));
        r.setTocaNumber(rs.getString("toca_number"));
        return r;
    }
}