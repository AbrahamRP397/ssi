package mx.gob.morelos.ssi.models.user;

import java.sql.Timestamp;

public class User {

    private Long id;
    private String name;
    private String surname;
    private String lastname;
    private String phone;
    private String email;
    private String password;
    private String role;                 // "Magistrado" | "Administrador"
    private Integer status;
    private Integer mustChangePassword;
    private Timestamp temporaryPasswordExpiration;
    private Timestamp lastAccessAt;
    private Timestamp createdAt;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(Integer mustChangePassword) { this.mustChangePassword = mustChangePassword; }
    public Timestamp getTemporaryPasswordExpiration() { return temporaryPasswordExpiration; }
    public void setTemporaryPasswordExpiration(Timestamp temporaryPasswordExpiration) { this.temporaryPasswordExpiration = temporaryPasswordExpiration; }
    public Timestamp getLastAccessAt() { return lastAccessAt; }
    public void setLastAccessAt(Timestamp lastAccessAt) { this.lastAccessAt = lastAccessAt; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean requiresPasswordChange() {
        return Integer.valueOf(1).equals(mustChangePassword);
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (name != null) sb.append(name.trim());
        if (surname != null && !surname.isBlank()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(surname.trim());
        }
        if (lastname != null && !lastname.isBlank()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(lastname.trim());
        }
        return sb.toString();
    }

    public String getInitials() {
        String n = name == null ? "" : name.trim();
        String s = surname == null ? "" : surname.trim();
        String i = (n.isEmpty() ? "" : n.substring(0, 1))
                + (s.isEmpty() ? "" : s.substring(0, 1));
        return i.toUpperCase();
    }
}