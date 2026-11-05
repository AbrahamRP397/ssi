package mx.gob.morelos.ssi.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;

public final class DatabaseSeeder {

    private DatabaseSeeder() {}

    public static void seed() {
        System.out.println("[DatabaseSeeder] Ejecutando 02-seed.sql...");
        DatabaseSchemaRunner.runScript("db/02-seed.sql");
        System.out.println("[DatabaseSeeder] 02-seed.sql OK.");

        System.out.println("[DatabaseSeeder] Forzando contraseñas con BCrypt...");
        try (Connection conn = SQLConnector.getConnection()) {
            updatePassword(conn, "admin@tsjmorelos.gob.mx", "Admin2025!");
            updatePassword(conn, "alejandro.ramirez@tsjmorelos.gob.mx", "Mag2025!");
            updatePassword(conn, "elena.salgado@tsjmorelos.gob.mx", "Mag2025!");
            updatePassword(conn, "roberto.velez@tsjmorelos.gob.mx", "Mag2025!");
            updatePassword(conn, "sofia.cortes@tsjmorelos.gob.mx", "Mag2025!");
            System.out.println("[DatabaseSeeder] Contraseñas BCrypt OK.");
        } catch (Exception e) {
            System.err.println("[DatabaseSeeder] Error hasheando contraseñas:");
            e.printStackTrace();
        }
    }

    private static void updatePassword(Connection conn, String email, String plain) throws Exception {
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(plain, org.mindrot.jbcrypt.BCrypt.gensalt());
        String sql = "UPDATE users SET password = ? WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }
}