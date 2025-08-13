package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model;

public class AppInfo {
    private String jws;
    private String dateString;
    private String expiresAt;

    public AppInfo() {}

    public AppInfo(String jws, String dateString) {
        this.jws = jws;
        this.dateString = dateString;
    }
    
    public AppInfo(String jws, String dateString, String expiresAt) {
        this.jws = jws;
        this.dateString = dateString;
        this.expiresAt = expiresAt;
    }

    public String getJws() {
        return jws;
    }

    public void setJws(String jws) {
        this.jws = jws;
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
}
