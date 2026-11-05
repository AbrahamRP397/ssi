package mx.gob.morelos.ssi.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class SQLConnector {

    private static HikariDataSource dataSource;

    static {
        try {
            ClassLoader cl = SQLConnector.class.getClassLoader();

            String dbUser = System.getenv("DB_USER");
            String dbPass = System.getenv("DB_PASS");
            String dbUrl  = System.getenv("DB_NAME");

            if (dbUser == null || dbPass == null || dbUrl == null) {
                Properties creds = new Properties();
                try (InputStream is = cl.getResourceAsStream("credentials.properties")) {
                    if (is == null) {
                        throw new RuntimeException("No se encontró credentials.properties.");
                    }
                    creds.load(is);
                }
                if (dbUser == null) dbUser = creds.getProperty("db.user");
                if (dbPass == null) dbPass = creds.getProperty("db.pass");
                if (dbUrl  == null) dbUrl  = creds.getProperty("db.name");
            }

            dbUser = require(dbUser, "db.user / DB_USER");
            dbPass = dbPass == null ? "" : dbPass.trim(); // Permite contraseña vacía
            dbUrl  = require(dbUrl,  "db.name / DB_NAME");

            // ---- MySQL ----
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.cj.jdbc.Driver"); // <-- DRIVER MYSQL
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPass);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(20000);

            dataSource = new HikariDataSource(config);
            System.out.println("Pool MySQL inicializado correctamente. URL: " + dbUrl);

        } catch (Exception e) {
            System.err.println("=== ERROR REAL DE MYSQL ===");
            e.printStackTrace();
            System.err.println("===========================");
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private static String require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Falta configurar: " + name);
        }
        return value.trim();
    }
}