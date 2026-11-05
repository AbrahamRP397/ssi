package mx.gob.morelos.ssi.models.toca;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Toca {

    private Long idToca;
    private String tocaNumber;
    private String subject;
    private String originalCaseNumber;
    private String appellant;
    private Long idRoom;
    private Long idMagistrate;
    private String status;             // "Nueva" | "En ponencia" | "Resuelto"
    private Date receptionDate;
    private Timestamp updatedAt;

    // Derivados para la vista
    private String roomName;
    private String magistrateName;
    private List<TocaDocument> documents = new ArrayList<>();
    private List<TocaHistory> history = new ArrayList<>();

    public Long getIdToca() { return idToca; }
    public void setIdToca(Long idToca) { this.idToca = idToca; }
    public String getTocaNumber() { return tocaNumber; }
    public void setTocaNumber(String tocaNumber) { this.tocaNumber = tocaNumber; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getOriginalCaseNumber() { return originalCaseNumber; }
    public void setOriginalCaseNumber(String originalCaseNumber) { this.originalCaseNumber = originalCaseNumber; }
    public String getAppellant() { return appellant; }
    public void setAppellant(String appellant) { this.appellant = appellant; }
    public Long getIdRoom() { return idRoom; }
    public void setIdRoom(Long idRoom) { this.idRoom = idRoom; }
    public Long getIdMagistrate() { return idMagistrate; }
    public void setIdMagistrate(Long idMagistrate) { this.idMagistrate = idMagistrate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getReceptionDate() { return receptionDate; }
    public void setReceptionDate(Date receptionDate) { this.receptionDate = receptionDate; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getMagistrateName() { return magistrateName; }
    public void setMagistrateName(String magistrateName) { this.magistrateName = magistrateName; }
    public List<TocaDocument> getDocuments() { return documents; }
    public void setDocuments(List<TocaDocument> documents) { this.documents = documents == null ? new ArrayList<>() : documents; }
    public List<TocaHistory> getHistory() { return history; }
    public void setHistory(List<TocaHistory> history) { this.history = history == null ? new ArrayList<>() : history; }

    /** Tono del badge según el estado (gold / blue / green) */
    public String getStatusTone() {
        if ("Nueva".equals(status)) return "gold";
        if ("En ponencia".equals(status)) return "blue";
        if ("Resuelto".equals(status)) return "green";
        return "blue";
    }

    /** Fecha formateada para el listado y el detalle ("18 jun 2025") */
    public String getReceptionDateLabel() {
        if (receptionDate == null) return "-";
        return new java.text.SimpleDateFormat("dd MMM yyyy", new java.util.Locale("es", "MX"))
                .format(receptionDate);
    }

    /** "Hace 2 horas", "Hace 1 día", etc. Para el "Actualizado hace…" */
    public String getUpdatedLabel() {
        if (updatedAt == null) return "recién";
        long diffMs = System.currentTimeMillis() - updatedAt.getTime();
        long minutes = diffMs / (60 * 1000);
        long hours = diffMs / (60 * 60 * 1000);
        long days = diffMs / (24 * 60 * 60 * 1000);
        if (minutes < 1) return "hace unos segundos";
        if (minutes < 60) return "hace " + minutes + " min";
        if (hours < 24) return "hace " + hours + " h";
        if (days == 1) return "hace 1 día";
        return "hace " + days + " días";
    }

    /** Última actividad (para el panel de acciones) */
    public String getLastActivityLabel() {
        if (history.isEmpty()) {
            return "Expediente recibido el " + getReceptionDateLabel() + ".";
        }
        TocaHistory last = history.get(history.size() - 1);
        return last.getOccurredLabel() + " · " + last.getDescription();
    }

    /** Tamaño total de documentos en MB, con 1 decimal ("23.7") */
    public String getDocumentsTotalMb() {
        long total = 0;
        for (TocaDocument d : documents) {
            if (d.getSizeBytes() != null) total += d.getSizeBytes();
        }
        double mb = total / (1024.0 * 1024.0);
        return String.format(java.util.Locale.ROOT, "%.1f", mb);
    }
}