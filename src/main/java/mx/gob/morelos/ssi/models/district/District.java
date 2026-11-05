package mx.gob.morelos.ssi.models.district;

public class District {

    private Long idDistrict;
    private String name;
    private String headCity;
    private Integer status;

    public Long getIdDistrict() { return idDistrict; }
    public void setIdDistrict(Long idDistrict) { this.idDistrict = idDistrict; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getHeadCity() { return headCity; }
    public void setHeadCity(String headCity) { this.headCity = headCity; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}