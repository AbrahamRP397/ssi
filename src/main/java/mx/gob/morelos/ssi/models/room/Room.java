package mx.gob.morelos.ssi.models.room;

public class Room {

    private Long idRoom;
    private Long idDistrict;
    private String name;
    private String address;
    private Integer status;

    // Derivados para la vista
    private String districtName;
    private String magistrateName;
    private Integer activeTocas;
    private Integer roomsInDistrict;


    public Long getIdRoom() { return idRoom; }
    public void setIdRoom(Long idRoom) { this.idRoom = idRoom; }
    public Long getIdDistrict() { return idDistrict; }
    public void setIdDistrict(Long idDistrict) { this.idDistrict = idDistrict; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }
    public String getMagistrateName() { return magistrateName; }
    public void setMagistrateName(String magistrateName) { this.magistrateName = magistrateName; }
    public Integer getActiveTocas() { return activeTocas; }
    public void setActiveTocas(Integer activeTocas) { this.activeTocas = activeTocas; }
    public Integer getRoomsInDistrict() { return roomsInDistrict; }
    public void setRoomsInDistrict(Integer roomsInDistrict) { this.roomsInDistrict = roomsInDistrict; }

    public boolean isActive() { return Integer.valueOf(1).equals(status); }

    public String getDistrictLabel() {
        return districtName == null ? "" : districtName;
    }

    public String getMagistrateInitials() {
        if (magistrateName == null || magistrateName.isBlank()) return "—";
        String clean = magistrateName
                .replaceAll("(?i)\\b(lic|mtra|mtro|dr|dra|magistrado|magistrada)\\b\\.?", "")
                .trim();
        String[] parts = clean.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, 2); i++) {
            if (!parts[i].isEmpty()) sb.append(parts[i].charAt(0));
        }
        return sb.length() == 0 ? "—" : sb.toString().toUpperCase();
    }

    public String getDisplayMagistrate() {
        return magistrateName == null ? "Sin asignar" : magistrateName;
    }
}