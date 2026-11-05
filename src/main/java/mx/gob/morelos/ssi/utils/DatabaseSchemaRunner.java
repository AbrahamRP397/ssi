package mx.gob.morelos.ssi.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

public final class DatabaseSchemaRunner {

    private DatabaseSchemaRunner() {}

    public static void runScript(String classpath) {
        System.out.println("[SchemaRunner] Buscando recurso: " + classpath);

        InputStream is = DatabaseSchemaRunner.class.getClassLoader().getResourceAsStream(classpath);
        if (is == null) {
            is = DatabaseSchemaRunner.class.getClassLoader().getResourceAsStream("/" + classpath);
        }
        if (is == null) {
            throw new RuntimeException("Script no encontrado en classpath: " + classpath);
        }

        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().startsWith("--")) continue;
                    sb.append(line).append('\n');
                }
            }

            String[] statements = sb.toString().split(";\\s*(?:\\r?\\n|$)");
            try (Connection conn = SQLConnector.getConnection();
                 Statement st = conn.createStatement()) {
                int count = 0;
                for (String raw : statements) {
                    String s = raw.trim();
                    if (s.isEmpty()) continue;
                    st.execute(s);
                    count++;
                }
                System.out.println("[SchemaRunner] " + classpath + " → " + count + " sentencias ejecutadas.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error ejecutando script " + classpath, e);
        }
    }
}