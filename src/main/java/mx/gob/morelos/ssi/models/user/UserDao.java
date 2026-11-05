package mx.gob.morelos.ssi.models.user;

import mx.gob.morelos.ssi.templates.Dao;
import mx.gob.morelos.ssi.utils.SQLConnector;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao implements Dao<User, Integer> {

    @Override
    public boolean create(User user) {
        if (user == null || user.getPassword() == null || user.getPassword().isBlank()) return false;

        String sql = """
                INSERT INTO users (
                    name, surname, lastname, phone, email, password,
                    role, status, must_change_password, temporary_password_expiration
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String hash = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getSurname());
            ps.setString(3, user.getLastname());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getEmail());
            ps.setString(6, hash);
            ps.setString(7, user.getRole());
            ps.setInt(8, user.getStatus() == null ? 1 : user.getStatus());
            ps.setInt(9, user.getMustChangePassword() == null ? 0 : user.getMustChangePassword());
            ps.setTimestamp(10, user.getTemporaryPasswordExpiration());

            if (ps.executeUpdate() != 1) return false;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getLong(1));
            }
            return true;

        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar el usuario.", e);
        }
    }

    @Override
    public List<User> getAll() {
        String sql = """
                SELECT id_user, name, surname, lastname, phone, email,
                       role, status, must_change_password,
                       temporary_password_expiration, last_access_at, created_at
                FROM users
                ORDER BY status DESC, UPPER(name), UPPER(surname), UPPER(lastname)
                """;

        List<User> users = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(mapUser(rs, false));
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar los usuarios.", e);
        }
    }

    @Override
    public User getById(Integer id) {
        if (id == null || id <= 0) return null;

        String sql = """
                SELECT id_user, name, surname, lastname, phone, email,
                       role, status, must_change_password,
                       temporary_password_expiration, last_access_at, created_at
                FROM users WHERE id_user = ?
                """;

        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs, false) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el usuario.", e);
        }
    }

    @Override
    public boolean update(User user) {
        if (user == null || user.getId() == null || user.getId() <= 0) return false;

        String sql = """
                UPDATE users
                SET name = ?, surname = ?, lastname = ?, phone = ?, email = ?, role = ?
                WHERE id_user = ?
                """;

        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getSurname());
            ps.setString(3, user.getLastname());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getRole());
            ps.setLong(7, user.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el usuario.", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null || id <= 0) return false;
        return changeStatus(id, 0);
    }

    public boolean changeStatus(int id, int status) {
        if (id <= 0 || (status != 0 && status != 1)) return false;

        String sql = "UPDATE users SET status = ? WHERE id_user = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al cambiar el estado del usuario.", e);
        }
    }

    public User login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) return null;

        String sql = """
                SELECT id_user, name, surname, lastname, phone, email, password,
                       role, status, must_change_password,
                       temporary_password_expiration, last_access_at, created_at
                FROM users
                WHERE LOWER(email) = LOWER(?) AND status = 1
                """;

        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                User user = mapUser(rs, true);
                String stored = user.getPassword();
                if (stored == null || stored.isBlank()) return null;
                if (!BCrypt.checkpw(password, stored)) return null;
                touchLastAccess(user.getId());
                return user;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al iniciar sesión.", e);
        }
    }

    private void touchLastAccess(Long id) {
        if (id == null) return;
        String sql = "UPDATE users SET last_access_at = CURRENT_TIMESTAMP WHERE id_user = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public User findByEmail(String email) {
        if (email == null || email.isBlank()) return null;

        String sql = """
                SELECT id_user, name, surname, lastname, phone, email,
                       role, status, must_change_password,
                       temporary_password_expiration, last_access_at, created_at
                FROM users WHERE LOWER(email) = LOWER(?) AND status = 1
                """;

        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs, false) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar usuario por correo.", e);
        }
    }

    public User findAnyByEmail(String email) {
        if (email == null || email.isBlank()) return null;

        String sql = """
                SELECT id_user, name, surname, lastname, phone, email,
                       role, status, must_change_password,
                       temporary_password_expiration, last_access_at, created_at
                FROM users WHERE LOWER(email) = LOWER(?)
                """;

        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs, false) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar usuario por correo.", e);
        }
    }

    public boolean updatePasswordById(long idUser, String newPassword) {
        if (idUser <= 0 || newPassword == null || newPassword.isBlank()) return false;

        String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String sql = """
                UPDATE users
                SET password = ?, must_change_password = 0,
                    temporary_password_expiration = NULL
                WHERE id_user = ? AND status = 1
                """;

        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setLong(2, idUser);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la contraseña.", e);
        }
    }

    public boolean isTemporaryPasswordExpired(User user) {
        if (user == null || !user.requiresPasswordChange()) return false;
        Timestamp exp = user.getTemporaryPasswordExpiration();
        return exp == null || exp.before(new Timestamp(System.currentTimeMillis()));
    }

    private User mapUser(ResultSet rs, boolean includePassword) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id_user"));
        u.setName(rs.getString("name"));
        u.setSurname(rs.getString("surname"));
        u.setLastname(rs.getString("lastname"));
        u.setPhone(rs.getString("phone"));
        u.setEmail(rs.getString("email"));
        if (includePassword) u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getInt("status"));
        u.setMustChangePassword(rs.getInt("must_change_password"));
        u.setTemporaryPasswordExpiration(rs.getTimestamp("temporary_password_expiration"));
        u.setLastAccessAt(rs.getTimestamp("last_access_at"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}