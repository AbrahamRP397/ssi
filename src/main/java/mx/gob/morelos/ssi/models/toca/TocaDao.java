package mx.gob.morelos.ssi.models.toca;

import mx.gob.morelos.ssi.templates.Dao;
import mx.gob.morelos.ssi.utils.SQLConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TocaDao implements Dao<Toca, Integer> {

    private static final String BASE_SELECT = """
            SELECT t.id_toca, t.toca_number, t.subject, t.original_case_number,
                   t.appellant, t.id_room, t.id_magistrate, t.status,
                   t.reception_date, t.updated_at,
                   r.name AS room_name,
                   TRIM(COALESCE(u.name,'') || ' ' || COALESCE(u.surname,'') || ' ' || COALESCE(u.lastname,'')) AS magistrate_name
            FROM tocas t
            INNER JOIN rooms r ON t.id_room = r.id_room
            LEFT JOIN magistrates m ON t.id_magistrate = m.id_magistrate
            LEFT JOIN users u ON m.id_user = u.id_user
            """;

    @Override
    public List<Toca> getAll() {
        String sql = BASE_SELECT + " ORDER BY t.reception_date DESC, t.id_toca DESC";
        List<Toca> list = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar las tocas.", e);
        }
    }

    public List<Toca> getByMagistrate(long idMagistrate) {
        String sql = BASE_SELECT + " WHERE t.id_magistrate = ? ORDER BY t.reception_date DESC, t.id_toca DESC";
        List<Toca> list = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idMagistrate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar las tocas del magistrado.", e);
        }
    }

    @Override
    public Toca getById(Integer id) {
        if (id == null || id <= 0) return null;
        String sql = BASE_SELECT + " WHERE t.id_toca = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Toca t = map(rs);
                t.setDocuments(getDocuments(t.getIdToca()));
                t.setHistory(getHistory(t.getIdToca()));
                return t;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar la toca.", e);
        }
    }

    @Override
    public boolean create(Toca t) {
        String sql = """
                INSERT INTO tocas (toca_number, subject, original_case_number, appellant,
                                   id_room, id_magistrate, status, reception_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getTocaNumber());
            ps.setString(2, t.getSubject());
            ps.setString(3, t.getOriginalCaseNumber());
            ps.setString(4, t.getAppellant());
            ps.setLong(5, t.getIdRoom());
            if (t.getIdMagistrate() == null) ps.setNull(6, Types.BIGINT);
            else ps.setLong(6, t.getIdMagistrate());
            ps.setString(7, t.getStatus() == null ? "Nueva" : t.getStatus());
            ps.setDate(8, t.getReceptionDate() == null ? new Date(System.currentTimeMillis()) : t.getReceptionDate());

            if (ps.executeUpdate() != 1) return false;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) t.setIdToca(keys.getLong(1));
            }

            // Historial inicial
            if (t.getIdToca() != null) {
                addHistory(t.getIdToca(), "Recepción automática vía API");
            }
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar la toca.", e);
        }
    }

    @Override
    public boolean update(Toca t) {
        String sql = """
                UPDATE tocas
                SET toca_number = ?, subject = ?, original_case_number = ?,
                    appellant = ?, id_room = ?, id_magistrate = ?, status = ?,
                    reception_date = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id_toca = ?
                """;
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTocaNumber());
            ps.setString(2, t.getSubject());
            ps.setString(3, t.getOriginalCaseNumber());
            ps.setString(4, t.getAppellant());
            ps.setLong(5, t.getIdRoom());
            if (t.getIdMagistrate() == null) ps.setNull(6, Types.BIGINT);
            else ps.setLong(6, t.getIdMagistrate());
            ps.setString(7, t.getStatus());
            ps.setDate(8, t.getReceptionDate());
            ps.setLong(9, t.getIdToca());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la toca.", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null || id <= 0) return false;
        String sql = "DELETE FROM tocas WHERE id_toca = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la toca.", e);
        }
    }

    public boolean changeStatus(long idToca, String status) {
        if (idToca <= 0 || status == null) return false;
        String sql = "UPDATE tocas SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id_toca = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, idToca);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error al cambiar el estado de la toca.", e);
        }
    }

    // ---------- Documentos ----------

    public List<TocaDocument> getDocuments(long idToca) {
        String sql = """
                SELECT id_document, id_toca, title, type, pages, size_bytes, file_url, uploaded_at
                FROM documents WHERE id_toca = ? ORDER BY uploaded_at
                """;
        List<TocaDocument> list = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idToca);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TocaDocument d = new TocaDocument();
                    d.setIdDocument(rs.getLong("id_document"));
                    d.setIdToca(rs.getLong("id_toca"));
                    d.setTitle(rs.getString("title"));
                    d.setType(rs.getString("type"));
                    d.setPages(rs.getInt("pages"));
                    d.setSizeBytes(rs.getLong("size_bytes"));
                    d.setFileUrl(rs.getString("file_url"));
                    d.setUploadedAt(rs.getTimestamp("uploaded_at"));
                    list.add(d);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar los documentos.", e);
        }
    }

    // ---------- Historial ----------

    public List<TocaHistory> getHistory(long idToca) {
        String sql = """
                SELECT id_history, id_toca, description, occurred_at
                FROM toca_history WHERE id_toca = ? ORDER BY occurred_at
                """;
        List<TocaHistory> list = new ArrayList<>();
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idToca);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TocaHistory h = new TocaHistory();
                    h.setIdHistory(rs.getLong("id_history"));
                    h.setIdToca(rs.getLong("id_toca"));
                    h.setDescription(rs.getString("description"));
                    h.setOccurredAt(rs.getTimestamp("occurred_at"));
                    list.add(h);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el historial.", e);
        }
    }

    public void addHistory(long idToca, String description) {
        if (idToca <= 0 || description == null || description.isBlank()) return;
        String sql = "INSERT INTO toca_history (id_toca, description) VALUES (?, ?)";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idToca);
            ps.setString(2, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar historial.", e);
        }
    }

    // ---------- Dashboard ----------

    public int countByMagistrateAndStatus(long idMagistrate, String status) {
        String sql = "SELECT COUNT(*) FROM tocas WHERE id_magistrate = ? AND status = ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idMagistrate);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar tocas.", e);
        }
    }

    public int countByMagistrateAndStatusDifferentFrom(long idMagistrate, String status) {
        String sql = "SELECT COUNT(*) FROM tocas WHERE id_magistrate = ? AND status <> ?";
        try (Connection conn = SQLConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idMagistrate);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar tocas.", e);
        }
    }

    private Toca map(ResultSet rs) throws SQLException {
        Toca t = new Toca();
        t.setIdToca(rs.getLong("id_toca"));
        t.setTocaNumber(rs.getString("toca_number"));
        t.setSubject(rs.getString("subject"));
        t.setOriginalCaseNumber(rs.getString("original_case_number"));
        t.setAppellant(rs.getString("appellant"));
        t.setIdRoom(rs.getLong("id_room"));
        long idMag = rs.getLong("id_magistrate");
        t.setIdMagistrate(rs.wasNull() ? null : idMag);
        t.setStatus(rs.getString("status"));
        t.setReceptionDate(rs.getDate("reception_date"));
        t.setUpdatedAt(rs.getTimestamp("updated_at"));
        t.setRoomName(rs.getString("room_name"));
        String mag = rs.getString("magistrate_name");
        t.setMagistrateName(mag == null || mag.isBlank() ? "Sin asignar" : mag.trim());
        return t;
    }
}