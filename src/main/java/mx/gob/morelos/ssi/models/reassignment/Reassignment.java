package mx.gob.morelos.ssi.models.reassignment;

import java.sql.Timestamp;

public class Reassignment {

    private Long idReassignment;
    private Long idToca;
    private Long idRoomFrom;
    private Long idRoomTo;
    private String reason;
    private String status;      // "Requiere validación" | "Aprobada" | "Rechazada"
    private Long requestedBy;
    private Timestamp createdAt;

    // Derivados
    private String tocaNumber;
    private String currentRoomName;
    private String targetRoomName;

    public Long getIdReassignment() { return idReassignment; }
    public void setIdReassignment(Long idReassignment) { this.idReassignment = idReassignment; }
    public Long getIdToca() { return idToca; }
    public void setIdToca(Long idToca) { this.idToca = idToca; }
    public Long getIdRoomFrom() { return idRoomFrom; }
    public void setIdRoomFrom(Long idRoomFrom) { this.idRoomFrom = idRoomFrom; }
    public Long getIdRoomTo() { return idRoomTo; }
    public void setIdRoomTo(Long idRoomTo) { this.idRoomTo = idRoomTo; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Long requestedBy) { this.requestedBy = requestedBy; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getTocaNumber() { return tocaNumber; }
    public void setTocaNumber(String tocaNumber) { this.tocaNumber = tocaNumber; }
    public String getCurrentRoomName() { return currentRoomName; }
    public void setCurrentRoomName(String currentRoomName) { this.currentRoomName = currentRoomName; }
    public String getTargetRoomName() { return targetRoomName; }
    public void setTargetRoomName(String targetRoomName) { this.targetRoomName = targetRoomName; }
}