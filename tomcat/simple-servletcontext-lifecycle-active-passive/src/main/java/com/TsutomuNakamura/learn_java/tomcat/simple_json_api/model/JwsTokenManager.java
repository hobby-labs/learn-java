package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages active and passive JWS tokens with lifecycle management
 */
public class JwsTokenManager {
    
    private JwsInfo activeToken;
    private List<JwsInfo> passiveTokens;
    
    public JwsTokenManager() {
        this.passiveTokens = new ArrayList<>();
    }
    
    public JwsTokenManager(JwsInfo activeToken, List<JwsInfo> passiveTokens) {
        this.activeToken = activeToken;
        this.passiveTokens = passiveTokens != null ? new ArrayList<>(passiveTokens) : new ArrayList<>();
    }
    
    /**
     * Gets the currently active token
     * @return Active JWS token, null if none
     */
    public JwsInfo getActiveToken() {
        return activeToken;
    }
    
    /**
     * Gets all passive tokens
     * @return List of passive tokens
     */
    public List<JwsInfo> getPassiveTokens() {
        return new ArrayList<>(passiveTokens);
    }
    
    /**
     * Sets a new active token, moving the current active to passive if it exists
     * @param newActiveToken The new active token
     */
    public void setActiveToken(JwsInfo newActiveToken) {
        if (activeToken != null) {
            // Move current active to passive
            passiveTokens.add(activeToken);
            System.out.println("[TokenManager] Moved active token to passive (created: " + 
                             activeToken.getFormattedCreatedTime() + ")");
        }
        
        this.activeToken = newActiveToken;
        System.out.println("[TokenManager] Set new active token (created: " + 
                         newActiveToken.getFormattedCreatedTime() + ")");
    }
    
    /**
     * Removes expired tokens from passive list
     * @return Number of tokens removed
     */
    public int removeExpiredTokens() {
        int removedCount = 0;
        Iterator<JwsInfo> iterator = passiveTokens.iterator();
        
        while (iterator.hasNext()) {
            JwsInfo token = iterator.next();
            if (token.isExpired()) {
                iterator.remove();
                removedCount++;
                System.out.println("[TokenManager] Removed expired passive token (created: " + 
                                 token.getFormattedCreatedTime() + ", expired: " + 
                                 token.getFormattedExpiresTime() + ")");
            }
        }
        
        return removedCount;
    }
    
    /**
     * Gets total number of tokens (active + passive)
     * @return Total token count
     */
    public int getTotalTokenCount() {
        return (activeToken != null ? 1 : 0) + passiveTokens.size();
    }
    
    /**
     * Gets the count of passive tokens
     * @return Passive token count
     */
    public int getPassiveTokenCount() {
        return passiveTokens.size();
    }
    
    /**
     * Checks if there's an active token
     * @return true if active token exists
     */
    public boolean hasActiveToken() {
        return activeToken != null;
    }
    
    /**
     * Checks if the active token is expired
     * @return true if active token exists and is expired
     */
    public boolean isActiveTokenExpired() {
        return activeToken != null && activeToken.isExpired();
    }
    
    /**
     * Gets a summary of current token state for logging
     * @return String summary
     */
    public String getTokenSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Token Summary - ");
        
        if (activeToken != null) {
            sb.append("Active: ").append(activeToken.getFormattedCreatedTime()).append(" (expires: ")
              .append(activeToken.getFormattedExpiresTime()).append("), ");
        } else {
            sb.append("Active: none, ");
        }
        
        sb.append("Passive: ").append(passiveTokens.size()).append(" tokens");
        
        if (!passiveTokens.isEmpty()) {
            sb.append(" [");
            for (int i = 0; i < passiveTokens.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(passiveTokens.get(i).getFormattedCreatedTime());
            }
            sb.append("]");
        }
        
        return sb.toString();
    }
    
    /**
     * Validates all tokens and removes any that are null or malformed
     */
    public void validateAndCleanup() {
        if (activeToken != null && activeToken.getJws() == null) {
            System.out.println("[TokenManager] Removing malformed active token");
            activeToken = null;
        }
        
        passiveTokens.removeIf(token -> token == null || token.getJws() == null);
    }
}
