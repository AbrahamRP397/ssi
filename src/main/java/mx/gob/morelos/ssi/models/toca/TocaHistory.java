package mx.gob.morelos.ssi.models.toca;

import java.sql.Timestamp;

public class TocaHistory {

    private Long idHistory;
    private Long idToca;
    private String description;
    private Timestamp occurredAt;

    public Long getIdHistory() { return idHistory; }
    public void setIdHistory(Long idHistory) { this.idHistory = idHistory; }
    public Long getIdToca() { return idToca; }
    public void setIdToca(Long idToca) { this.idToca = idToca; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Timestamp getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Timestamp occurredAt) { this.occurredAt = occurredAt; }

    public String getOccurredLabel() {
        if (occurredAt == null) return "-";
        return new java.text.SimpleDateFormat("dd MMM yyyy", new java.util.Locale("es", "MX"))
                .format(occurredAt);
    }
}