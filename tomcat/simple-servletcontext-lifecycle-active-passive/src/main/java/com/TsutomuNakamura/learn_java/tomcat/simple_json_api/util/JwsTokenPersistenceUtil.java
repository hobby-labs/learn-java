package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.JwsInfo;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.JwsTokenManager;

/**
 * Enhanced persistence utility for managing active and passive JWS tokens
 */
public class JwsTokenPersistenceUtil {
    
    private static final String PERSISTENCE_DIR = "jws-data";
    private static final String ACTIVE_TOKEN_FILE = "active-token.properties";
    private static final String PASSIVE_TOKENS_FILE = "passive-tokens.properties";
    private static final String JWS_KEY = "jws";
    private static final String CREATED_TIME_KEY = "created.time";
    private static final String EXPIRES_TIME_KEY = "expires.time";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final Path persistenceDir;
    private final Path activeTokenPath;
    private final Path passiveTokensPath;
    
    public JwsTokenPersistenceUtil() {
        // Create persistence directory if it doesn't exist
        this.persistenceDir = Paths.get(PERSISTENCE_DIR);
        try {
            Files.createDirectories(persistenceDir);
        } catch (IOException e) {
            System.err.println("Failed to create persistence directory: " + e.getMessage());
        }
        
        this.activeTokenPath = persistenceDir.resolve(ACTIVE_TOKEN_FILE);
        this.passiveTokensPath = persistenceDir.resolve(PASSIVE_TOKENS_FILE);
    }
    
    /**
     * Save token manager state to persistent storage
     * @param tokenManager JWS token manager to save
     */
    public void saveTokenManager(JwsTokenManager tokenManager) {
        // Save active token
        saveActiveToken(tokenManager.getActiveToken());
        
        // Save passive tokens
        savePassiveTokens(tokenManager.getPassiveTokens());
        
        System.out.println("[TokenPersistence] Saved token manager state - " + tokenManager.getTokenSummary());
    }
    
    /**
     * Load token manager state from persistent storage
     * @return JwsTokenManager with loaded state, or empty manager if no data
     */
    public JwsTokenManager loadTokenManager() {
        JwsInfo activeToken = loadActiveToken();
        List<JwsInfo> passiveTokens = loadPassiveTokens();
        
        JwsTokenManager manager = new JwsTokenManager(activeToken, passiveTokens);
        
        if (activeToken != null || !passiveTokens.isEmpty()) {
            System.out.println("[TokenPersistence] Loaded token manager state - " + manager.getTokenSummary());
        } else {
            System.out.println("[TokenPersistence] No persisted token data found");
        }
        
        return manager;
    }
    
    /**
     * Save active token to file
     * @param activeToken Token to save, null to clear
     */
    private void saveActiveToken(JwsInfo activeToken) {
        if (activeToken == null) {
            try {
                Files.deleteIfExists(activeTokenPath);
                System.out.println("[TokenPersistence] Cleared active token");
            } catch (IOException e) {
                System.err.println("Failed to clear active token file: " + e.getMessage());
            }
            return;
        }
        
        Properties props = new Properties();
        props.setProperty(JWS_KEY, activeToken.getJws());
        props.setProperty(CREATED_TIME_KEY, activeToken.getFormattedCreatedTime());
        props.setProperty(EXPIRES_TIME_KEY, activeToken.getFormattedExpiresTime());
        
        try (FileWriter writer = new FileWriter(activeTokenPath.toFile())) {
            props.store(writer, "Active JWS Token - Saved: " + LocalDateTime.now());
        } catch (IOException e) {
            System.err.println("Failed to save active token: " + e.getMessage());
        }
    }
    
    /**
     * Load active token from file
     * @return Active token or null if not found/invalid
     */
    private JwsInfo loadActiveToken() {
        if (!Files.exists(activeTokenPath)) {
            return null;
        }
        
        Properties props = new Properties();
        try (FileReader reader = new FileReader(activeTokenPath.toFile())) {
            props.load(reader);
            return parseJwsInfoFromProperties(props);
        } catch (IOException | java.time.format.DateTimeParseException e) {
            System.err.println("Failed to load active token: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Save passive tokens to file
     * @param passiveTokens List of tokens to save
     */
    private void savePassiveTokens(List<JwsInfo> passiveTokens) {
        if (passiveTokens.isEmpty()) {
            try {
                Files.deleteIfExists(passiveTokensPath);
                System.out.println("[TokenPersistence] Cleared passive tokens");
            } catch (IOException e) {
                System.err.println("Failed to clear passive tokens file: " + e.getMessage());
            }
            return;
        }
        
        Properties props = new Properties();
        props.setProperty("count", String.valueOf(passiveTokens.size()));
        
        for (int i = 0; i < passiveTokens.size(); i++) {
            JwsInfo token = passiveTokens.get(i);
            String prefix = "token." + i + ".";
            props.setProperty(prefix + JWS_KEY, token.getJws());
            props.setProperty(prefix + CREATED_TIME_KEY, token.getFormattedCreatedTime());
            props.setProperty(prefix + EXPIRES_TIME_KEY, token.getFormattedExpiresTime());
        }
        
        try (FileWriter writer = new FileWriter(passiveTokensPath.toFile())) {
            props.store(writer, "Passive JWS Tokens - Saved: " + LocalDateTime.now());
        } catch (IOException e) {
            System.err.println("Failed to save passive tokens: " + e.getMessage());
        }
    }
    
    /**
     * Load passive tokens from file
     * @return List of passive tokens (empty if none found)
     */
    private List<JwsInfo> loadPassiveTokens() {
        List<JwsInfo> tokens = new ArrayList<>();
        
        if (!Files.exists(passiveTokensPath)) {
            return tokens;
        }
        
        Properties props = new Properties();
        try (FileReader reader = new FileReader(passiveTokensPath.toFile())) {
            props.load(reader);
            
            String countStr = props.getProperty("count");
            if (countStr == null) {
                return tokens;
            }
            
            int count = Integer.parseInt(countStr);
            for (int i = 0; i < count; i++) {
                String prefix = "token." + i + ".";
                Properties tokenProps = new Properties();
                tokenProps.setProperty(JWS_KEY, props.getProperty(prefix + JWS_KEY));
                tokenProps.setProperty(CREATED_TIME_KEY, props.getProperty(prefix + CREATED_TIME_KEY));
                tokenProps.setProperty(EXPIRES_TIME_KEY, props.getProperty(prefix + EXPIRES_TIME_KEY));
                
                JwsInfo token = parseJwsInfoFromProperties(tokenProps);
                if (token != null) {
                    tokens.add(token);
                }
            }
            
        } catch (IOException | NumberFormatException | java.time.format.DateTimeParseException e) {
            System.err.println("Failed to load passive tokens: " + e.getMessage());
        }
        
        return tokens;
    }
    
    /**
     * Parse JwsInfo from properties
     * @param props Properties containing JWS data
     * @return JwsInfo object or null if invalid
     */
    private JwsInfo parseJwsInfoFromProperties(Properties props) {
        String jws = props.getProperty(JWS_KEY);
        String createdTimeStr = props.getProperty(CREATED_TIME_KEY);
        String expiresTimeStr = props.getProperty(EXPIRES_TIME_KEY);
        
        if (jws == null || createdTimeStr == null || expiresTimeStr == null) {
            return null;
        }
        
        LocalDateTime createdTime = LocalDateTime.parse(createdTimeStr, DATETIME_FORMATTER);
        LocalDateTime expiresTime = LocalDateTime.parse(expiresTimeStr, DATETIME_FORMATTER);
        
        return new JwsInfo(jws, createdTime, expiresTime);
    }
    
    /**
     * Clear all persisted token data
     */
    public void clearAllData() {
        try {
            Files.deleteIfExists(activeTokenPath);
            Files.deleteIfExists(passiveTokensPath);
            System.out.println("[TokenPersistence] Cleared all token data");
        } catch (IOException e) {
            System.err.println("Failed to clear token data: " + e.getMessage());
        }
    }
    
    /**
     * Check if any persisted data exists
     * @return true if any token files exist
     */
    public boolean hasPersistedData() {
        return Files.exists(activeTokenPath) || Files.exists(passiveTokensPath);
    }
}
