package mx.gob.morelos.ssi.models.branding;

public class Branding {

    private Long idBranding;
    private String logoUrl;
    private String primaryColor;
    private String accentColor;
    private Long updatedBy;

    public static Branding defaults() {
        Branding b = new Branding();
        b.setIdBranding(1L);
        b.setPrimaryColor("#2e3b2b");
        b.setAccentColor("#6f7857");
        return b;
    }

    public Long getIdBranding() { return idBranding; }
    public void setIdBranding(Long idBranding) { this.idBranding = idBranding; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }
    public String getAccentColor() { return accentColor; }
    public void setAccentColor(String accentColor) { this.accentColor = accentColor; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
}