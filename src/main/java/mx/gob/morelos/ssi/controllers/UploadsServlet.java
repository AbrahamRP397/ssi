package mx.gob.morelos.ssi.controllers;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet(name = "UploadsServlet", value = "/uploads/*")
public class UploadsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String pathInfo = req.getPathInfo();   // "/logos/xxx.png"
        if (pathInfo == null || pathInfo.equals("/")) {
            res.sendError(404);
            return;
        }

        // Sanitizar: no permitir ../ ni rutas absolutas
        String relative = pathInfo.substring(1).replace("..", "").replace("\\", "/");
        if (relative.contains("..") || relative.startsWith("/")) {
            res.sendError(400);
            return;
        }

        String base = System.getProperty("catalina.base");
        if (base == null || base.isBlank()) base = System.getProperty("java.io.tmpdir");

        Path file = Paths.get(base, "ssi-uploads").resolve(relative).normalize();
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            res.sendError(404);
            return;
        }

        String contentType = getServletContext().getMimeType(file.getFileName().toString());
        if (contentType == null) contentType = "application/octet-stream";

        res.setContentType(contentType);
        res.setContentLengthLong(Files.size(file));
        res.setHeader("Cache-Control", "public, max-age=86400");

        try (OutputStream out = res.getOutputStream()) {
            Files.copy(file, out);
        }
    }
}