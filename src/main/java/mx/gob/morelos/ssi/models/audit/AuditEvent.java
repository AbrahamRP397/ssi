package mx.gob.morelos.ssi.models.audit;

import java.sql.Timestamp;

public class AuditEvent {

    private Long idEvent;
    private Long idUser;
    private String actorName;
    private String role;       // "magistrado" | "administrador"
    private String action;
    private String detail;
    private String tone;       // green | gold | blue | crimson
    private Timestamp occurredAt;

    public Long getIdEvent() { return idEvent; }
    public void setIdEvent(Long idEvent) { this.idEvent = idEvent; }
    public Long getIdUser() { return idUser; }
    public void setIdUser(Long idUser) { this.idUser = idUser; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }
    public Timestamp getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Timestamp occurredAt) { this.occurredAt = occurredAt; }
    
    public String getOccurredLabel() {
        if (occurredAt == null) return "-";
        return new java.text.SimpleDateFormat("dd MMM yyyy HH:mm", new java.util.Locale("es", "MX"))
                .format(occurredAt);
    }
}