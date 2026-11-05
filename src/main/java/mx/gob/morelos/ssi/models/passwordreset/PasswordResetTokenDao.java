package mx.gob.morelos.ssi.models.passwordreset;

import mx.gob.morelos.ssi.utils.SQLConnector;

import java.sql.*;
import java.time.LocalDateTime;

public class PasswordResetTokenDao {

    public boolean create(long idUser, String token, LocalDateTime expiration) {
        if (idUser <= 0 || token == null || token.isBlank() || expiration == null) return false;

        String sql = """
                INSERT INTO password_reset_tokens (id_user, token, expiration, used)
                VALUES (?, ?, ?, 0)
                """;

        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUser);
            ps.setString(2, token.trim());
            ps.setTimestamp(3, Timestamp.valueOf(expiration));
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear el token de recuperación.", e);
        }
    }

    public PasswordResetToken findValidToken(String token) {
        if (token == null || token.isBlank()) return null;

        String sql = """
                SELECT prt.id_token, prt.id_user, prt.token, prt.expiration, prt.used, prt.created_at
                FROM password_reset_tokens prt
                INNER JOIN users u ON prt.id_user = u.id_user
                WHERE prt.token = ?
                  AND prt.used = 0
                  AND prt.expiration > CURRENT_TIMESTAMP
                  AND u.status = 1
                """;

        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapToken(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el token de recuperación.", e);
        }
    }

    public boolean markAsUsed(long idToken) {
        if (idToken <= 0) return false;

        String sql = "UPDATE password_reset_tokens SET used = 1 WHERE id_token = ? AND used = 0";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idToken);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al marcar el token como utilizado.", e);
        }
    }

    public boolean invalidateToken(String token) {
        if (token == null || token.isBlank()) return false;

        String sql = "UPDATE password_reset_tokens SET used = 1 WHERE token = ? AND used = 0";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token.trim());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Error al invalidar el token de recuperación.", e);
        }
    }

    public boolean invalidateUserTokens(long idUser) {
        if (idUser <= 0) return false;

        String sql = "UPDATE password_reset_tokens SET used = 1 WHERE id_user = ? AND used = 0";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUser);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Error al invalidar los tokens del usuario.", e);
        }
    }

    private PasswordResetToken mapToken(ResultSet rs) throws SQLException {
        PasswordResetToken t = new PasswordResetToken();
        t.setIdToken(rs.getLong("id_token"));
        t.setIdUser(rs.getLong("id_user"));
        t.setToken(rs.getString("token"));
        Timestamp exp = rs.getTimestamp("expiration");
        if (exp != null) t.setExpiration(exp.toLocalDateTime());
        t.setUsed(rs.getInt("used") == 1);
        Timestamp c = rs.getTimestamp("created_at");
        if (c != null) t.setCreatedAt(c.toLocalDateTime());
        return t;
    }
}