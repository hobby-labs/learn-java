package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.JwsInfo;

/**
 * Utility class for persisting JWS data with expiration across application restarts
 */
public class JwsPersistenceUtil {
    
    private static final String PERSISTENCE_DIR = "jws-data";
    private static final String PERSISTENCE_FILE = "jws-persistence.properties";
    private static final String JWS_KEY = "jws";
    private static final String CREATED_TIME_KEY = "created.time";
    private static final String EXPIRES_TIME_KEY = "expires.time";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final Path persistenceFilePath;
    
    public JwsPersistenceUtil() {
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
     * Save JWS info to persistent storage
     * @param jwsInfo JWS information to save
     */
    public void saveJwsInfo(JwsInfo jwsInfo) {
        Properties props = new Properties();
        props.setProperty(JWS_KEY, jwsInfo.getJws());
        props.setProperty(CREATED_TIME_KEY, jwsInfo.getFormattedCreatedTime());
        props.setProperty(EXPIRES_TIME_KEY, jwsInfo.getFormattedExpiresTime());
        
        try (FileWriter writer = new FileWriter(persistenceFilePath.toFile())) {
            props.store(writer, "JWS Persistence Data - Created: " + LocalDateTime.now());
            System.out.println("JWS info saved to: " + persistenceFilePath);
        } catch (IOException e) {
            System.err.println("Failed to save JWS info: " + e.getMessage());
        }
    }
    
    /**
     * Load JWS info from persistent storage
     * @return JwsInfo if exists and valid, null otherwise
     */
    public JwsInfo loadJwsInfo() {
        if (!Files.exists(persistenceFilePath)) {
            System.out.println("No persistence file found: " + persistenceFilePath);
            return null;
        }
        
        Properties props = new Properties();
        try (FileReader reader = new FileReader(persistenceFilePath.toFile())) {
            props.load(reader);
            
            String jws = props.getProperty(JWS_KEY);
            String createdTimeStr = props.getProperty(CREATED_TIME_KEY);
            String expiresTimeStr = props.getProperty(EXPIRES_TIME_KEY);
            
            if (jws == null || createdTimeStr == null || expiresTimeStr == null) {
                System.out.println("Incomplete JWS info in persistence file");
                return null;
            }
            
            LocalDateTime createdTime = LocalDateTime.parse(createdTimeStr, DATETIME_FORMATTER);
            LocalDateTime expiresTime = LocalDateTime.parse(expiresTimeStr, DATETIME_FORMATTER);
            
            JwsInfo jwsInfo = new JwsInfo(jws, createdTime, expiresTime);
            System.out.println("Loaded JWS info from persistence (expires: " + expiresTimeStr + ")");
            
            return jwsInfo;
            
        } catch (IOException | java.time.format.DateTimeParseException e) {
            System.err.println("Failed to load JWS info: " + e.getMessage());
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
