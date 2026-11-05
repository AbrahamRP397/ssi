package mx.gob.morelos.ssi.models.branding;

import mx.gob.morelos.ssi.utils.SQLConnector;

import java.sql.*;

public class BrandingDao {

    public Branding get() {
        String sql = "SELECT id_branding, logo_url, primary_color, accent_color, updated_by FROM branding WHERE id_branding = 1";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                Branding defaults = Branding.defaults();
                insertDefaults(defaults);
                return defaults;
            }
            Branding b = new Branding();
            b.setIdBranding(rs.getLong("id_branding"));
            b.setLogoUrl(rs.getString("logo_url"));
            b.setPrimaryColor(rs.getString("primary_color"));
            b.setAccentColor(rs.getString("accent_color"));
            long ub = rs.getLong("updated_by");
            b.setUpdatedBy(rs.wasNull() ? null : ub);
            return b;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el branding.", e);
        }
    }

    private void insertDefaults(Branding b) {
        String sql = """
            INSERT INTO branding (id_branding, logo_url, primary_color, accent_color, updated_by)
            VALUES (1, NULL, ?, ?, NULL)
            ON DUPLICATE KEY UPDATE primary_color=VALUES(primary_color), accent_color=VALUES(accent_color)
            """;
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getPrimaryColor());
            ps.setString(2, b.getAccentColor());
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public boolean update(Branding b) {
        String sql = """
                UPDATE branding
                SET logo_url = ?, primary_color = ?, accent_color = ?,
                    updated_by = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id_branding = 1
                """;
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (b.getLogoUrl() == null || b.getLogoUrl().isBlank()) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, b.getLogoUrl());
            }
            ps.setString(2, b.getPrimaryColor());
            ps.setString(3, b.getAccentColor());
            if (b.getUpdatedBy() == null) ps.setNull(4, Types.BIGINT);
            else ps.setLong(4, b.getUpdatedBy());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el branding.", e);
        }
    }
}