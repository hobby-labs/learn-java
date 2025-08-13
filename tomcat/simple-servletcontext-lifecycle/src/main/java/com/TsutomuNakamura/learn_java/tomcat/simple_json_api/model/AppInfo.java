package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model;

public class AppInfo {
    private String jws;
    private String dateString;
    private String expiresAt;
    private boolean isExpired;

    public AppInfo() {}

    public AppInfo(String jws, String dateString) {
        this.jws = jws;
        this.dateString = dateString;
    }
    
    public AppInfo(String jws, String dateString, String expiresAt, boolean isExpired) {
        this.jws = jws;
        this.dateString = dateString;
        this.expiresAt = expiresAt;
        this.isExpired = isExpired;
    }

    public String getJws() {
        return jws;
    }

    public void setJws(String jws) {
        this.jws = jws;
    }
    
    // Legacy getter for backward compatibility
    public String getUuid() {
        return jws;
    }

    public void setUuid(String uuid) {
        this.jws = uuid;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }
    
    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    // Method to convert to CSV format
    public String toCsv() {
        return jws + "," + dateString;
    }

    // Static method to create from CSV format
    public static AppInfo fromCsv(String csvLine) {
        String[] parts = csvLine.split(",", 2);
        if (parts.length == 2) {
            return new AppInfo(parts[0], parts[1]);
        }
        return null;
    }
}
