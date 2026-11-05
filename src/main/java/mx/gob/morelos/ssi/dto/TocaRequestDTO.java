package mx.gob.morelos.ssi.dto;

public class TocaRequestDTO {
    private String expedienteOriginal;
    private String materia;
    private String documentoApelacionUrl;
    private Long idSala;

    // Getters y Setters
    public String getExpedienteOriginal() { return expedienteOriginal; }
    public void setExpedienteOriginal(String expedienteOriginal) { this.expedienteOriginal = expedienteOriginal; }
    public String getMateria() { return materia; }
    public void setMateria(String materia) { this.materia = materia; }
    public String getDocumentoApelacionUrl() { return documentoApelacionUrl; }
    public void setDocumentoApelacionUrl(String documentoApelacionUrl) { this.documentoApelacionUrl = documentoApelacionUrl; }
    public Long getIdSala() { return idSala; }
    public void setIdSala(Long idSala) { this.idSala = idSala; }
}