package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JwsInfo {
    private final String jws;
    private final LocalDateTime createdTime;
    private final LocalDateTime expiresTime;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public JwsInfo(String jws, LocalDateTime createdTime, LocalDateTime expiresTime) {
        this.jws = jws;
        this.createdTime = createdTime;
        this.expiresTime = expiresTime;
    }
    
    public String getJws() { return jws; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public LocalDateTime getExpiresTime() { return expiresTime; }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresTime);
    }
    
    public String getFormattedCreatedTime() {
        return createdTime.format(DATETIME_FORMATTER);
    }
    
    public String getFormattedExpiresTime() {
        return expiresTime.format(DATETIME_FORMATTER);
    }
}
