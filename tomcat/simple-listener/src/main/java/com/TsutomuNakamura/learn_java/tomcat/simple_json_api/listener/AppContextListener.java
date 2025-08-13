package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.AppInfo;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util.UuidPersistenceUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class AppContextListener implements ServletContextListener {
    
    public static final String APP_INFO_CSV_KEY = "appInfoCsv";
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final int UPDATE_INTERVAL_SECONDS = 120;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;
    private static final String THREAD_NAME = "UUID-Updater-Thread";
    private static final int UUID_EXPIRATION_MINUTES = 10; // UUID expires after 10 minutes
    
    private ScheduledExecutorService scheduler;
    private ServletContextEvent servletContextEvent;
    private UuidPersistenceUtil persistenceUtil;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Hello!");
        this.servletContextEvent = sce; // Store reference for use in scheduled task
        this.persistenceUtil = new UuidPersistenceUtil(); // Initialize persistence utility
        
        // Try to load existing UUID from persistence
        UuidPersistenceUtil.UuidInfo persistedUuid = persistenceUtil.loadUuidInfo();
        
        if (persistedUuid != null && !persistedUuid.isExpired()) {
            // Use existing UUID if it's not expired
            System.out.println("Found valid persisted UUID: " + persistedUuid.getUuid());
            System.out.println("UUID created: " + persistedUuid.getFormattedCreatedTime());
            System.out.println("UUID expires: " + persistedUuid.getFormattedExpiresTime());
            
            updateServletContextWithUuid(persistedUuid.getUuid(), persistedUuid.getFormattedCreatedTime(), 
                                       persistedUuid.getFormattedExpiresTime(), false);
        } else {
            // Generate new UUID if none exists or existing is expired
            if (persistedUuid != null) {
                System.out.println("Persisted UUID has expired, generating new one");
            }
            generateAndStoreNewUuid("Generated initial UUID");
        }
        
        System.out.println("Application started successfully at " + new java.util.Date());
        
        // Start background thread to check UUID expiration and update if needed
        startUuidUpdaterThread();
        
        System.out.println("UUID updater thread started - will check every " + UPDATE_INTERVAL_SECONDS + " seconds");
    }
    
    /**
     * Generates new UUID with expiration and stores it persistently
     * @param logPrefix Prefix for console log message
     */
    private void generateAndStoreNewUuid(String logPrefix) {
        String uuid = generateUuid();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(UUID_EXPIRATION_MINUTES);
        
        String dateString = now.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
        String expiresString = expiresAt.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
        
        // Store in persistence
        UuidPersistenceUtil.UuidInfo uuidInfo = new UuidPersistenceUtil.UuidInfo(uuid, now, expiresAt);
        persistenceUtil.saveUuidInfo(uuidInfo);
        
        // Update ServletContext
        updateServletContextWithUuid(uuid, dateString, expiresString, false);
        
        System.out.println("[" + logPrefix + "] UUID: " + uuid + " at " + dateString);
        System.out.println("UUID will expire at: " + expiresString);
    }
    
    /**
     * Updates ServletContext with UUID information
     */
    private void updateServletContextWithUuid(String uuid, String dateString, String expiresAt, boolean isExpired) {
        AppInfo appInfo = new AppInfo(uuid, dateString, expiresAt, isExpired);
        String csvData = appInfo.toCsv();
        servletContextEvent.getServletContext().setAttribute(APP_INFO_CSV_KEY, csvData);
        
        // Also store extended info for JSON responses
        servletContextEvent.getServletContext().setAttribute(APP_INFO_CSV_KEY + "_EXTENDED", appInfo);
    }
    
    /**
     * Checks if current UUID is expired and generates new one if needed
     */
    private void checkAndUpdateExpiredUuid() {
        UuidPersistenceUtil.UuidInfo persistedUuid = persistenceUtil.loadUuidInfo();
        
        if (persistedUuid == null || persistedUuid.isExpired()) {
            if (persistedUuid != null) {
                System.out.println("[UUID-Expiration-Check] UUID expired: " + persistedUuid.getUuid());
                persistenceUtil.clearPersistedData();
            }
            generateAndStoreNewUuid("UUID-Auto-Renewal");
        } else {
            System.out.println("[UUID-Expiration-Check] UUID still valid: " + persistedUuid.getUuid() + 
                             " (expires: " + persistedUuid.getFormattedExpiresTime() + ")");
        }
    }
    
    /**
     * Generates a new UUID string
     * @return UUID as string
     */
    private String generateUuid() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Starts the background thread for periodic UUID updates
     */
    private void startUuidUpdaterThread() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, THREAD_NAME);
            t.setDaemon(true); // Make it a daemon thread so it doesn't prevent JVM shutdown
            return t;
        });
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndUpdateExpiredUuid();
            } catch (Exception e) {
                System.err.println("[UUID-Updater] Error checking UUID expiration: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Good bye!");
        
        shutdownUuidUpdaterThread();
        
        System.out.println("Application shutdown completed at " + new java.util.Date());
    }
    
    /**
     * Gracefully shuts down the UUID updater thread
     */
    private void shutdownUuidUpdaterThread() {
        if (scheduler != null && !scheduler.isShutdown()) {
            System.out.println("Shutting down UUID updater thread...");
            scheduler.shutdown();
            try {
                // Wait up to configured timeout for existing tasks to terminate
                if (!scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    System.out.println("UUID updater thread did not terminate gracefully, forcing shutdown");
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for UUID updater thread to terminate");
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("UUID updater thread shut down completed");
        }
    }
}
