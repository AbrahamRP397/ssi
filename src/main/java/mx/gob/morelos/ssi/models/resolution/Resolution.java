package mx.gob.morelos.ssi.models.resolution;

import java.sql.Timestamp;

public class Resolution {

    private Long idResolution;
    private Long idToca;
    private Long idMagistrate;
    private String outcome;    // "Confirma" | "Revoca" | "Modifica"
    private String body;
    private String observations;
    private String pdfUrl;
    private Timestamp emittedAt;

    // Derivados
    private String tocaNumber;

    public Long getIdResolution() { return idResolution; }
    public void setIdResolution(Long idResolution) { this.idResolution = idResolution; }
    public Long getIdToca() { return idToca; }
    public void setIdToca(Long idToca) { this.idToca = idToca; }
    public Long getIdMagistrate() { return idMagistrate; }
    public void setIdMagistrate(Long idMagistrate) { this.idMagistrate = idMagistrate; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    public Timestamp getEmittedAt() { return emittedAt; }
    public void setEmittedAt(Timestamp emittedAt) { this.emittedAt = emittedAt; }
    public String getTocaNumber() { return tocaNumber; }
    public void setTocaNumber(String tocaNumber) { this.tocaNumber = tocaNumber; }

    public String getEmittedLabel() {
        if (emittedAt == null) return "-";
        return new java.text.SimpleDateFormat("dd MMM yyyy", new java.util.Locale("es", "MX"))
                .format(emittedAt);
    }
}