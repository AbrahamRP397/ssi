package mx.gob.morelos.ssi.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import mx.gob.morelos.ssi.dto.TocaRequestDTO;
import mx.gob.morelos.ssi.models.audit.AuditDao;
import mx.gob.morelos.ssi.models.magistrate.Magistrate;
import mx.gob.morelos.ssi.models.magistrate.MagistrateDao;
import mx.gob.morelos.ssi.models.room.Room;
import mx.gob.morelos.ssi.models.room.RoomDao;
import mx.gob.morelos.ssi.models.toca.Toca;
import mx.gob.morelos.ssi.models.toca.TocaDao;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(name = "TocaApiServlet", value = "/api/tocas/recibir")
public class TocaApiServlet extends HttpServlet {

    private TocaDao tocaDao;
    private RoomDao roomDao;
    private MagistrateDao magistrateDao;
    private AuditDao auditDao;
    private Gson gson;

    private static final String API_TOKEN = "SSI_API_PRIMERA_INSTANCIA_2026";

    @Override
    public void init() {
        tocaDao = new TocaDao();
        roomDao = new RoomDao();
        magistrateDao = new MagistrateDao();
        auditDao = new AuditDao();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        // 1. Validar Token
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.equals("Bearer " + API_TOKEN)) {
            sendError(res, HttpServletResponse.SC_UNAUTHORIZED, "Token de API inválido o ausente.");
            return;
        }

        // 2. Leer JSON
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) sb.append(line);
        }

        TocaRequestDTO dto;
        try {
            dto = gson.fromJson(sb.toString(), TocaRequestDTO.class);
            if (dto == null) throw new JsonSyntaxException("Body vacío");
        } catch (JsonSyntaxException e) {
            sendError(res, HttpServletResponse.SC_BAD_REQUEST, "Formato JSON inválido o vacío.");
            return;
        }

        // 3. Validar campos
        if (dto.getExpedienteOriginal() == null || dto.getExpedienteOriginal().isBlank() ||
                dto.getMateria() == null || dto.getMateria().isBlank() ||
                dto.getIdSala() == null) {
            sendError(res, HttpServletResponse.SC_BAD_REQUEST, "Faltan campos obligatorios.");
            return;
        }

        // 4. Validar Sala
        Room sala = roomDao.getById(dto.getIdSala().intValue());
        if (sala == null) {
            sendError(res, HttpServletResponse.SC_NOT_FOUND, "La sala especificada no existe.");
            return;
        }
        if (sala.getStatus() != 1) {
            sendError(res, HttpServletResponse.SC_CONFLICT, "La sala está inactiva.");
            return;
        }

        // 5. Validar Magistrado
        Magistrate magistrado = magistrateDao.getByRoomId(dto.getIdSala());
        if (magistrado == null) {
            sendError(res, HttpServletResponse.SC_CONFLICT, "La sala no tiene un magistrado asignado.");
            return;
        }

        // 6. Crear Toca
        try {
            Toca nuevaToca = new Toca();
            String folio = "TOCA-" + java.time.Year.now().getValue() + "-" + (1000 + (int)(Math.random() * 9000));

            nuevaToca.setTocaNumber(folio);
            nuevaToca.setSubject(dto.getMateria());
            nuevaToca.setOriginalCaseNumber(dto.getExpedienteOriginal());
            nuevaToca.setAppellant("Por definir");
            nuevaToca.setIdRoom(dto.getIdSala());
            nuevaToca.setIdMagistrate(magistrado.getIdMagistrate());
            nuevaToca.setStatus("Nueva");
            nuevaToca.setReceptionDate(new java.sql.Date(System.currentTimeMillis()));

            if (!tocaDao.create(nuevaToca)) {
                sendError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al guardar la toca.");
                return;
            }

            // 7. Auditar
            try {
                auditDao.log(null, "Sistema Primera Instancia", "sistema",
                        "Recepción automática de Toca",
                        folio + " · " + dto.getExpedienteOriginal() + " · Sala: " + sala.getName(), "blue");
            } catch (RuntimeException ignored) {}

            // 8. Responder
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Toca recibida y asignada correctamente.");
            response.put("idToca", nuevaToca.getIdToca());
            response.put("folio", folio);
            response.put("idMagistrate", magistrado.getIdMagistrate());

            res.setStatus(HttpServletResponse.SC_CREATED);
            res.getWriter().write(gson.toJson(response));

        } catch (RuntimeException e) {
            getServletContext().log("Error al recibir toca vía API.", e);
            sendError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno al procesar la toca.");
        }
    }

    private void sendError(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("success", false);
        error.put("message", message);
        res.getWriter().write(gson.toJson(error));
    }
}