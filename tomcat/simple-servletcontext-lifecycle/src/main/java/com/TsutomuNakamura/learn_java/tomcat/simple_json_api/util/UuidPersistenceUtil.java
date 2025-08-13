package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Utility class for persisting UUID data with expiration across application restarts
 */
public class UuidPersistenceUtil {
    
    private static final String PERSISTENCE_DIR = "uuid-data";
    private static final String PERSISTENCE_FILE = "uuid-persistence.properties";
    private static final String UUID_KEY = "uuid";
    private static final String CREATED_TIME_KEY = "created.time";
    private static final String EXPIRES_TIME_KEY = "expires.time";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final Path persistenceFilePath;
    
    public UuidPersistenceUtil() {
        // Create persistence directory if it doesn't exist
        Path persistenceDir = Paths.get(PERSISTENCE_DIR);
        try {
            Files.createDirectories(persistenceDir);
        } catch (IOException e) {
            System.err.println("Failed to create persistence directory: " + e.getMessage());
        }
        
        this.persistenceFilePath = persistenceDir.resolve(PERSISTENCE_FILE);
    }
    
    /**
     * Data class to hold UUID information with expiration
     */
    public static class UuidInfo {
        private final String uuid;
        private final LocalDateTime createdTime;
        private final LocalDateTime expiresTime;
        
        public UuidInfo(String uuid, LocalDateTime createdTime, LocalDateTime expiresTime) {
            this.uuid = uuid;
            this.createdTime = createdTime;
            this.expiresTime = expiresTime;
        }
        
        public String getUuid() { return uuid; }
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
    
    /**
     * Save UUID info to persistent storage
     * @param uuidInfo UUID information to save
     */
    public void saveUuidInfo(UuidInfo uuidInfo) {
        Properties props = new Properties();
        props.setProperty(UUID_KEY, uuidInfo.getUuid());
        props.setProperty(CREATED_TIME_KEY, uuidInfo.getFormattedCreatedTime());
        props.setProperty(EXPIRES_TIME_KEY, uuidInfo.getFormattedExpiresTime());
        
        try (FileWriter writer = new FileWriter(persistenceFilePath.toFile())) {
            props.store(writer, "UUID Persistence Data - Created: " + LocalDateTime.now());
            System.out.println("UUID info saved to: " + persistenceFilePath);
        } catch (IOException e) {
            System.err.println("Failed to save UUID info: " + e.getMessage());
        }
    }
    
    /**
     * Load UUID info from persistent storage
     * @return UuidInfo if exists and valid, null otherwise
     */
    public UuidInfo loadUuidInfo() {
        if (!Files.exists(persistenceFilePath)) {
            System.out.println("No persistence file found: " + persistenceFilePath);
            return null;
        }
        
        Properties props = new Properties();
        try (FileReader reader = new FileReader(persistenceFilePath.toFile())) {
            props.load(reader);
            
            String uuid = props.getProperty(UUID_KEY);
            String createdTimeStr = props.getProperty(CREATED_TIME_KEY);
            String expiresTimeStr = props.getProperty(EXPIRES_TIME_KEY);
            
            if (uuid == null || createdTimeStr == null || expiresTimeStr == null) {
                System.out.println("Incomplete UUID info in persistence file");
                return null;
            }
            
            LocalDateTime createdTime = LocalDateTime.parse(createdTimeStr, DATETIME_FORMATTER);
            LocalDateTime expiresTime = LocalDateTime.parse(expiresTimeStr, DATETIME_FORMATTER);
            
            UuidInfo uuidInfo = new UuidInfo(uuid, createdTime, expiresTime);
            System.out.println("Loaded UUID info from persistence: " + uuid + " (expires: " + expiresTimeStr + ")");
            
            return uuidInfo;
            
        } catch (IOException | java.time.format.DateTimeParseException e) {
            System.err.println("Failed to load UUID info: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Delete the persistence file
     */
    public void clearPersistedData() {
        try {
            Files.deleteIfExists(persistenceFilePath);
            System.out.println("Persistence file deleted: " + persistenceFilePath);
        } catch (IOException e) {
            System.err.println("Failed to delete persistence file: " + e.getMessage());
        }
    }
}
