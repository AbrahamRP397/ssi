package mx.gob.morelos.ssi.listeners;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import mx.gob.morelos.ssi.utils.DatabaseSchemaRunner;
import mx.gob.morelos.ssi.utils.DatabaseSeeder;
import mx.gob.morelos.ssi.utils.SQLConnector;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("===== [AppInitializer] Iniciando =====");
        try {
            System.out.println("[AppInitializer] Ejecutando schema...");
            DatabaseSchemaRunner.runScript("db/01-schema.sql");
            System.out.println("[AppInitializer] Schema OK.");

            System.out.println("[AppInitializer] Ejecutando seed...");
            DatabaseSeeder.seed();
            System.out.println("[AppInitializer] Seed OK.");

        } catch (Throwable e) {
            System.err.println("===== [AppInitializer] ERROR FATAL =====");
            e.printStackTrace();
            System.err.println("=========================================");
        }
        System.out.println("===== [AppInitializer] Terminado =====");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try { SQLConnector.closeConnection(); } catch (Exception ignored) {}
    }
}