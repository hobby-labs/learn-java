package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.service;

import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.User;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util.JwsTokenPersistenceUtil;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util.JwsUtil;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.JwsInfo;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.JwsTokenManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Enhanced service class for managing active and passive JWS tokens
 * with lifecycle management including automatic token rotation and cleanup.
 */
public class JwsActivePassiveManagementService {
    
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final int JWS_EXPIRATION_MINUTES = 3; // JWS expires after 3 minutes
    private static final int TOKEN_ROTATION_MINUTES = 1; // New token every 1 minute

    private final JwsTokenPersistenceUtil persistenceUtil;
    private final JwsUtil jwsUtil;
    private final UserService userService;
    private JwsTokenManager tokenManager;
    private LocalDateTime lastTokenCreationTime;
    
    public JwsActivePassiveManagementService() throws Exception {
        this.persistenceUtil = new JwsTokenPersistenceUtil();
        this.jwsUtil = new JwsUtil();
        this.userService = new UserService();
        
        // Verify JWS utility is working
        if (!jwsUtil.isWorking()) {
            throw new Exception("JWS utility initialization failed");
        }
        
        // Load existing token state or initialize new manager
        this.tokenManager = persistenceUtil.loadTokenManager();
        
        // Initialize last creation time based on active token or current time
        if (tokenManager.hasActiveToken()) {
            this.lastTokenCreationTime = tokenManager.getActiveToken().getCreatedTime();
        } else {
            this.lastTokenCreationTime = null;
        }
    }
    
    /**
     * Gets current active JWS info, ensuring a valid active token exists
     * @return Current active JwsInfo object
     */
    public JwsInfo getCurrentActiveJwsInfo() {
        // Clean up expired tokens first
        cleanupExpiredTokens();
        
        // Ensure we have an active token
        if (!tokenManager.hasActiveToken()) {
            createNewActiveToken();
        }
        
        JwsInfo activeToken = tokenManager.getActiveToken();
        System.out.println("[ActivePassive-Service] Returning active token (created: " + 
                         activeToken.getFormattedCreatedTime() + ")");
        
        return activeToken;
    }
    
    /**
     * Performs periodic maintenance: token rotation, expiration cleanup
     * This should be called every 10 seconds from the scheduled task
     * @return Current active JwsInfo object after maintenance
     */
    public JwsInfo performPeriodicMaintenance() {
        System.out.println("[ActivePassive-Maintenance] Starting periodic maintenance");
        
        // Clean up expired tokens
        int expiredCount = cleanupExpiredTokens();
        if (expiredCount > 0) {
            System.out.println("[ActivePassive-Maintenance] Cleaned up " + expiredCount + " expired tokens");
        }
        
        // Check if we need to rotate tokens (every 1 minute)
        boolean needsRotation = shouldRotateToken();
        if (needsRotation) {
            createNewActiveToken();
            System.out.println("[ActivePassive-Maintenance] Rotated to new active token");
        }
        
        // Ensure we have an active token
        if (!tokenManager.hasActiveToken()) {
            createNewActiveToken();
            System.out.println("[ActivePassive-Maintenance] Created initial active token");
        }
        
        // Save current state
        persistenceUtil.saveTokenManager(tokenManager);
        
        // Log current state
        System.out.println("[ActivePassive-Maintenance] " + tokenManager.getTokenSummary());
        
        return tokenManager.getActiveToken();
    }
    
    /**
     * Checks if token rotation is needed based on timing
     * @return true if a new token should be created
     */
    private boolean shouldRotateToken() {
        if (lastTokenCreationTime == null) {
            return true; // First token needed
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRotationTime = lastTokenCreationTime.plusMinutes(TOKEN_ROTATION_MINUTES);
        
        boolean shouldRotate = now.isAfter(nextRotationTime) || now.isEqual(nextRotationTime);
        
        if (shouldRotate) {
            System.out.println("[ActivePassive-Service] Token rotation needed - last created: " + 
                             lastTokenCreationTime.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)) + 
                             ", next rotation was: " + 
                             nextRotationTime.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
        }
        
        return shouldRotate;
    }
    
    /**
     * Creates a new active token and manages the rotation
     */
    private void createNewActiveToken() {
        try {
            // Get current user data to include in JWS payload
            List<User> users = userService.getAllUsers();
            String jws = generateJwsWithUserData(users);
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(JWS_EXPIRATION_MINUTES);
            
            JwsInfo newToken = new JwsInfo(jws, now, expiresAt);
            
            // Set as active token (automatically moves current active to passive)
            tokenManager.setActiveToken(newToken);
            this.lastTokenCreationTime = now;
            
            String dateString = now.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
            String expiresString = expiresAt.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
            
            System.out.println("[ActivePassive-Service] Created new active token at " + dateString);
            System.out.println("[ActivePassive-Service] Token will expire at: " + expiresString);
            
        } catch (Exception e) {
            System.err.println("[ActivePassive-Service] Failed to create new token: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Removes expired tokens from the manager
     * @return Number of tokens removed
     */
    private int cleanupExpiredTokens() {
        int removedCount = tokenManager.removeExpiredTokens();
        
        // Also check if active token is expired
        if (tokenManager.isActiveTokenExpired()) {
            System.out.println("[ActivePassive-Service] Active token expired, will be replaced");
        }
        
        return removedCount;
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
     * Gets the current token manager for inspection
     * @return Current JwsTokenManager
     */
    public JwsTokenManager getTokenManager() {
        return tokenManager;
    }
    
    /**
     * Gets configuration values
     */
    public int getExpirationMinutes() {
        return JWS_EXPIRATION_MINUTES;
    }
    
    public int getRotationMinutes() {
        return TOKEN_ROTATION_MINUTES;
    }
    
    /**
     * Forces immediate token rotation (for testing or manual refresh)
     */
    public JwsInfo forceTokenRotation() {
        System.out.println("[ActivePassive-Service] Forcing token rotation");
        createNewActiveToken();
        persistenceUtil.saveTokenManager(tokenManager);
        return tokenManager.getActiveToken();
    }
    
    /**
     * Clears all token data and resets the manager
     */
    public void clearAllTokenData() {
        persistenceUtil.clearAllData();
        tokenManager = new JwsTokenManager();
        lastTokenCreationTime = null;
        System.out.println("[ActivePassive-Service] Cleared all token data and reset manager");
    }
    
    /**
     * Gets a detailed status report of the current token state
     * @return String with detailed token information
     */
    public String getDetailedStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== JWS Active/Passive Token Status ===\n");
        sb.append("Configuration: Expiration=").append(JWS_EXPIRATION_MINUTES)
          .append("min, Rotation=").append(TOKEN_ROTATION_MINUTES).append("min\n");
        
        if (lastTokenCreationTime != null) {
            sb.append("Last Token Creation: ").append(lastTokenCreationTime.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN))).append("\n");
            sb.append("Next Rotation Due: ").append(lastTokenCreationTime.plusMinutes(TOKEN_ROTATION_MINUTES).format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN))).append("\n");
        }
        
        sb.append("Current Time: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN))).append("\n");
        sb.append(tokenManager.getTokenSummary()).append("\n");
        
        if (tokenManager.hasActiveToken()) {
            JwsInfo active = tokenManager.getActiveToken();
            sb.append("Active Token Details:\n");
            sb.append("  Created: ").append(active.getFormattedCreatedTime()).append("\n");
            sb.append("  Expires: ").append(active.getFormattedExpiresTime()).append("\n");
            sb.append("  Is Expired: ").append(active.isExpired()).append("\n");
        }
        
        List<JwsInfo> passiveTokens = tokenManager.getPassiveTokens();
        if (!passiveTokens.isEmpty()) {
            sb.append("Passive Tokens:\n");
            for (int i = 0; i < passiveTokens.size(); i++) {
                JwsInfo token = passiveTokens.get(i);
                sb.append("  ").append(i + 1).append(": Created=").append(token.getFormattedCreatedTime())
                  .append(", Expires=").append(token.getFormattedExpiresTime())
                  .append(", Expired=").append(token.isExpired()).append("\n");
            }
        }
        
        return sb.toString();
    }
}
