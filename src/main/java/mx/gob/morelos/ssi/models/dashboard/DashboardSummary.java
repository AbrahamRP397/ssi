package mx.gob.morelos.ssi.models.dashboard;

public class DashboardSummary {

    // Para el magistrado
    private int pending;
    private String pendingHelper;
    private int weekPonencias;
    private String weekPonenciasHelper;
    private int resolutions;
    private String resolutionsHelper;

    // Para el admin
    private int totalRooms;
    private String roomsHelper;
    private int activeMagistrates;
    private String magistratesHelper;
    private int totalDistricts;

    public int getPending() { return pending; }
    public void setPending(int pending) { this.pending = pending; }
    public String getPendingHelper() { return pendingHelper; }
    public void setPendingHelper(String pendingHelper) { this.pendingHelper = pendingHelper; }
    public int getWeekPonencias() { return weekPonencias; }
    public void setWeekPonencias(int weekPonencias) { this.weekPonencias = weekPonencias; }
    public String getWeekPonenciasHelper() { return weekPonenciasHelper; }
    public void setWeekPonenciasHelper(String weekPonenciasHelper) { this.weekPonenciasHelper = weekPonenciasHelper; }
    public int getResolutions() { return resolutions; }
    public void setResolutions(int resolutions) { this.resolutions = resolutions; }
    public String getResolutionsHelper() { return resolutionsHelper; }
    public void setResolutionsHelper(String resolutionsHelper) { this.resolutionsHelper = resolutionsHelper; }
    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }
    public String getRoomsHelper() { return roomsHelper; }
    public void setRoomsHelper(String roomsHelper) { this.roomsHelper = roomsHelper; }
    public int getActiveMagistrates() { return activeMagistrates; }
    public void setActiveMagistrates(int activeMagistrates) { this.activeMagistrates = activeMagistrates; }
    public String getMagistratesHelper() { return magistratesHelper; }
    public void setMagistratesHelper(String magistratesHelper) { this.magistratesHelper = magistratesHelper; }
    public int getTotalDistricts() { return totalDistricts; }
    public void setTotalDistricts(int totalDistricts) { this.totalDistricts = totalDistricts; }
}