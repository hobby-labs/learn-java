package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.service;

import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.AppInfo;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util.UuidPersistenceUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service class responsible for UUID lifecycle management including generation, 
 * expiration checking, and persistence operations.
 * 
 * This class encapsulates all UUID-related business logic, keeping it separate 
 * from application lifecycle concerns.
 */
public class UuidManagementService {
    
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final int UUID_EXPIRATION_MINUTES = 10; // UUID expires after 10 minutes
    
    private final UuidPersistenceUtil persistenceUtil;
    
    public UuidManagementService() {
        this.persistenceUtil = new UuidPersistenceUtil();
    }
    
    /**
     * Gets current valid UUID info, generating new one if expired or missing
     * @return Current valid AppInfo object
     */
    public AppInfo getCurrentValidUuidInfo() {
        UuidPersistenceUtil.UuidInfo persistedUuid = persistenceUtil.loadUuidInfo();
        
        if (persistedUuid != null && !persistedUuid.isExpired()) {
            // Return existing valid UUID
            System.out.println("[UUID-Service] Using existing valid UUID: " + persistedUuid.getUuid() + 
                             " (expires: " + persistedUuid.getFormattedExpiresTime() + ")");
            
            return createAppInfoFromUuidInfo(persistedUuid);
        } else {
            // Generate new UUID if none exists or existing is expired
            if (persistedUuid != null) {
                System.out.println("[UUID-Service] UUID expired: " + persistedUuid.getUuid() + ", generating new one");
                persistenceUtil.clearPersistedData();
            } else {
                System.out.println("[UUID-Service] No persisted UUID found, generating new one");
            }
            
            return generateNewUuidInfo();
        }
    }
    
    /**
     * Checks if current UUID is expired and returns updated info if needed
     * @return Current valid AppInfo object (new if previous was expired)
     */
    public AppInfo checkAndRefreshIfExpired() {
        UuidPersistenceUtil.UuidInfo persistedUuid = persistenceUtil.loadUuidInfo();
        
        if (persistedUuid == null || persistedUuid.isExpired()) {
            if (persistedUuid != null) {
                System.out.println("[UUID-Expiration-Check] UUID expired: " + persistedUuid.getUuid());
                persistenceUtil.clearPersistedData();
            }
            
            AppInfo newUuidInfo = generateNewUuidInfo();
            System.out.println("[UUID-Auto-Renewal] Generated new UUID due to expiration");
            return newUuidInfo;
        } else {
            System.out.println("[UUID-Expiration-Check] UUID still valid: " + persistedUuid.getUuid() + 
                             " (expires: " + persistedUuid.getFormattedExpiresTime() + ")");
            return createAppInfoFromUuidInfo(persistedUuid);
        }
    }
    
    /**
     * Generates a new UUID with expiration and persists it
     * @return New AppInfo object with UUID and expiration info
     */
    public AppInfo generateNewUuidInfo() {
        String uuid = generateUuid();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(UUID_EXPIRATION_MINUTES);
        
        String dateString = now.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
        String expiresString = expiresAt.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
        
        // Store in persistence
        UuidPersistenceUtil.UuidInfo uuidInfo = new UuidPersistenceUtil.UuidInfo(uuid, now, expiresAt);
        persistenceUtil.saveUuidInfo(uuidInfo);
        
        System.out.println("[UUID-Service] Generated new UUID: " + uuid + " at " + dateString);
        System.out.println("[UUID-Service] UUID will expire at: " + expiresString);
        
        return new AppInfo(uuid, dateString, expiresString, false);
    }
    
    /**
     * Creates AppInfo object from UuidInfo
     * @param uuidInfo Persisted UUID information
     * @return AppInfo object for API responses
     */
    private AppInfo createAppInfoFromUuidInfo(UuidPersistenceUtil.UuidInfo uuidInfo) {
        return new AppInfo(
            uuidInfo.getUuid(),
            uuidInfo.getFormattedCreatedTime(),
            uuidInfo.getFormattedExpiresTime(),
            uuidInfo.isExpired()
        );
    }
    
    /**
     * Generates a new UUID string
     * @return UUID as string
     */
    private String generateUuid() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Gets expiration minutes configuration
     * @return Expiration time in minutes
     */
    public int getExpirationMinutes() {
        return UUID_EXPIRATION_MINUTES;
    }
    
    /**
     * Clears all persisted UUID data
     */
    public void clearPersistedData() {
        persistenceUtil.clearPersistedData();
        System.out.println("[UUID-Service] Cleared all persisted UUID data");
    }
}
