package mx.gob.morelos.ssi.models.magistrate;

public class Magistrate {

    private Long idMagistrate;
    private Long idUser;
    private Long idRoom;
    private Integer status;

    // Derivados
    private String fullName;
    private String email;
    private String roomName;

    public Long getIdMagistrate() { return idMagistrate; }
    public void setIdMagistrate(Long idMagistrate) { this.idMagistrate = idMagistrate; }
    public Long getIdUser() { return idUser; }
    public void setIdUser(Long idUser) { this.idUser = idUser; }
    public Long getIdRoom() { return idRoom; }
    public void setIdRoom(Long idRoom) { this.idRoom = idRoom; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
}