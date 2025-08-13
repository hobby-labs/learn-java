package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model;

public class AppInfo {
    private String uuid;
    private String dateString;

    public AppInfo() {}

    public AppInfo(String uuid, String dateString) {
        this.uuid = uuid;
        this.dateString = dateString;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    // Method to convert to CSV format
    public String toCsv() {
        return uuid + "," + dateString;
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
