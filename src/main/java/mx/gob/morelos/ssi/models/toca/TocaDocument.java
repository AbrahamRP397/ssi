package mx.gob.morelos.ssi.models.toca;

import java.sql.Timestamp;

public class TocaDocument {

    private Long idDocument;
    private Long idToca;
    private String title;
    private String type;
    private Integer pages;
    private Long sizeBytes;
    private String fileUrl;
    private Timestamp uploadedAt;

    public Long getIdDocument() { return idDocument; }
    public void setIdDocument(Long idDocument) { this.idDocument = idDocument; }
    public Long getIdToca() { return idToca; }
    public void setIdToca(Long idToca) { this.idToca = idToca; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getPages() { return pages; }
    public void setPages(Integer pages) { this.pages = pages; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Timestamp getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Timestamp uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getSizeLabel() {
        if (sizeBytes == null) return "-";
        double mb = sizeBytes / (1024.0 * 1024.0);
        return String.format(java.util.Locale.ROOT, "%.1f MB", mb);
    }

    public String getUploadedLabel() {
        if (uploadedAt == null) return "-";
        return new java.text.SimpleDateFormat("dd MMM yyyy", new java.util.Locale("es", "MX"))
                .format(uploadedAt);
    }
}