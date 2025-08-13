package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.service;

import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.User;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util.JwsPersistenceUtil;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util.JwsUtil;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.JwsInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service class responsible for JWS lifecycle management including generation, 
 * expiration checking, and persistence operations.
 * 
 * This class encapsulates all JWS-related business logic, keeping it separate 
 * from application lifecycle concerns.
 */
public class JwsManagementService {
    
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    //private static final int JWS_EXPIRATION_MINUTES = 10; // JWS expires after 10 minutes
    private static final int JWS_EXPIRATION_MINUTES = 1;

    private final JwsPersistenceUtil persistenceUtil;
    private final JwsUtil jwsUtil;
    private final UserService userService;
    
    public JwsManagementService() throws Exception {
        this.persistenceUtil = new JwsPersistenceUtil();
        this.jwsUtil = new JwsUtil();
        this.userService = new UserService();
        
        // Verify JWS utility is working
        if (!jwsUtil.isWorking()) {
            throw new Exception("JWS utility initialization failed");
        }
    }
    
    /**
     * Gets current valid JWS info, generating new one if expired or missing
     * @return Current valid JwsInfo object
     */
    public JwsInfo getCurrentValidJwsInfo() {
        JwsInfo persistedJws = persistenceUtil.loadJwsInfo();
        
        if (persistedJws != null && !persistedJws.isExpired()) {
            // Return existing valid JWS
            System.out.println("[JWS-Service] Using existing valid JWS (expires: " + 
                             persistedJws.getFormattedExpiresTime() + ")");
            
            return persistedJws;
        } else {
            // Generate new JWS if none exists or existing is expired
            if (persistedJws != null) {
                System.out.println("[JWS-Service] JWS expired, generating new one");
                persistenceUtil.clearPersistedData();
            } else {
                System.out.println("[JWS-Service] No persisted JWS found, generating new one");
            }
            
            return generateNewJwsInfo();
        }
    }
    
    /**
     * Checks if current JWS is expired and returns updated info if needed
     * @return Current valid JwsInfo object (new if previous was expired)
     */
    public JwsInfo checkAndRefreshIfExpired() {
        JwsInfo persistedJws = persistenceUtil.loadJwsInfo();
        
        if (persistedJws == null || persistedJws.isExpired()) {
            if (persistedJws != null) {
                System.out.println("[JWS-Expiration-Check] JWS expired");
                persistenceUtil.clearPersistedData();
            }
            
            JwsInfo newJwsInfo = generateNewJwsInfo();
            System.out.println("[JWS-Auto-Renewal] Generated new JWS due to expiration");
            return newJwsInfo;
        } else {
            System.out.println("[JWS-Expiration-Check] JWS still valid (expires: " + 
                             persistedJws.getFormattedExpiresTime() + ")");
            return persistedJws;
        }
    }
    
    /**
     * Generates a new JWS with expiration and persists it
     * @return New JwsInfo object with JWS and expiration info
     */
    public JwsInfo generateNewJwsInfo() {
        try {
            // Get current user data to include in JWS payload
            List<User> users = userService.getAllUsers();
            String jws = generateJwsWithUserData(users);
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(JWS_EXPIRATION_MINUTES);
            
            String dateString = now.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
            String expiresString = expiresAt.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
            
            // Store in persistence
            JwsInfo jwsInfo = new JwsInfo(jws, now, expiresAt);
            persistenceUtil.saveJwsInfo(jwsInfo);
            
            System.out.println("[JWS-Service] Generated new JWS with user data at " + dateString);
            System.out.println("[JWS-Service] JWS will expire at: " + expiresString);
            
            return jwsInfo;
        } catch (Exception e) {
            System.err.println("[JWS-Service] Failed to generate JWS: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: create an error info
            LocalDateTime now = LocalDateTime.now();
            return new JwsInfo("JWS_GENERATION_FAILED", now, now);
        }
    }
    
    /**
     * Generates a new JWS token with user data in payload
     * @param users List of users to include in payload
     * @return JWS as string
     * @throws Exception if JWS generation fails
     */
    private String generateJwsWithUserData(List<User> users) throws Exception {
        return jwsUtil.generateJwsWithPayload(users);
    }
    
    /**
     * Gets expiration minutes configuration
     * @return Expiration time in minutes
     */
    public int getExpirationMinutes() {
        return JWS_EXPIRATION_MINUTES;
    }
    
    /**
     * Clears all persisted JWS data
     */
    public void clearPersistedData() {
        persistenceUtil.clearPersistedData();
        System.out.println("[JWS-Service] Cleared all persisted JWS data");
    }
}
