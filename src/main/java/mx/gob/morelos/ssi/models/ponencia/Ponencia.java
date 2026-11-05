package mx.gob.morelos.ssi.models.ponencia;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Ponencia {

    private Long idPonencia;
    private Long idToca;
    private Long idMagistrate;
    private Date sessionDate;
    private Time startTime;
    private Time endTime;
    private String kind;
    private Timestamp createdAt;

    // Derivados
    private String tocaNumber;
    private String originalCaseNumber;

    public Long getIdPonencia() { return idPonencia; }
    public void setIdPonencia(Long idPonencia) { this.idPonencia = idPonencia; }
    public Long getIdToca() { return idToca; }
    public void setIdToca(Long idToca) { this.idToca = idToca; }
    public Long getIdMagistrate() { return idMagistrate; }
    public void setIdMagistrate(Long idMagistrate) { this.idMagistrate = idMagistrate; }
    public Date getSessionDate() { return sessionDate; }
    public void setSessionDate(Date sessionDate) { this.sessionDate = sessionDate; }
    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }
    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getTocaNumber() { return tocaNumber; }
    public void setTocaNumber(String tocaNumber) { this.tocaNumber = tocaNumber; }
    public String getOriginalCaseNumber() { return originalCaseNumber; }
    public void setOriginalCaseNumber(String originalCaseNumber) { this.originalCaseNumber = originalCaseNumber; }

    /** "10:00–12:00" */
    public String getTimeLabel() {
        if (startTime == null || endTime == null) return "";
        return String.format("%s–%s",
                startTime.toLocalTime().toString().substring(0, 5),
                endTime.toLocalTime().toString().substring(0, 5));
    }

    /** Para la grilla: cuántas filas de hora ocupa (asumiendo filas de 1h) */
    public int getDurationHours() {
        if (startTime == null || endTime == null) return 1;
        long diff = endTime.getTime() - startTime.getTime();
        int h = (int) Math.max(1, Math.round(diff / (60.0 * 60 * 1000)));
        return h;
    }
}