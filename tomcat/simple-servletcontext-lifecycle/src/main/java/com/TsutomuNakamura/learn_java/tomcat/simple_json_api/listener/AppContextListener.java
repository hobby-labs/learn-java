package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.AppInfo;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.service.UuidManagementService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class AppContextListener implements ServletContextListener {
    
    public static final String APP_INFO_CSV_KEY = "appInfoCsv";
    private static final int UPDATE_INTERVAL_SECONDS = 120;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;
    private static final String THREAD_NAME = "UUID-Updater-Thread";
    
    private ScheduledExecutorService scheduler;
    private ServletContextEvent servletContextEvent;
    private UuidManagementService uuidService;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Hello!");
        this.servletContextEvent = sce; // Store reference for use in scheduled task
        this.uuidService = new UuidManagementService(); // Initialize UUID management service
        
        // Get current valid UUID info from service (handles loading/generation logic)
        AppInfo currentUuidInfo = uuidService.getCurrentValidUuidInfo();
        
        // Update ServletContext with current UUID info
        updateServletContext(currentUuidInfo);
        
        System.out.println("Application started successfully at " + new java.util.Date());
        
        // Start background thread to check UUID expiration periodically
        startUuidExpirationChecker();
        
        System.out.println("UUID expiration checker started - will check every " + UPDATE_INTERVAL_SECONDS + " seconds");
    }
    
    /**
     * Updates ServletContext with UUID information
     * @param appInfo AppInfo object to store
     */
    private void updateServletContext(AppInfo appInfo) {
        servletContextEvent.getServletContext().setAttribute(APP_INFO_CSV_KEY, appInfo);
    }
    
    /**
     * Starts the background thread for periodic UUID expiration checking
     */
    private void startUuidExpirationChecker() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, THREAD_NAME);
            t.setDaemon(true); // Make it a daemon thread so it doesn't prevent JVM shutdown
            return t;
        });
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Delegate expiration checking to the service
                AppInfo refreshedUuidInfo = uuidService.checkAndRefreshIfExpired();
                updateServletContext(refreshedUuidInfo);
            } catch (Exception e) {
                System.err.println("[UUID-Expiration-Checker] Error checking UUID expiration: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Good bye!");
        
        shutdownUuidExpirationChecker();
        
        System.out.println("Application shutdown completed at " + new java.util.Date());
    }
    
    /**
     * Gracefully shuts down the UUID expiration checker thread
     */
    private void shutdownUuidExpirationChecker() {
        if (scheduler != null && !scheduler.isShutdown()) {
            System.out.println("Shutting down UUID expiration checker thread...");
            scheduler.shutdown();
            try {
                // Wait up to configured timeout for existing tasks to terminate
                if (!scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    System.out.println("UUID expiration checker did not terminate gracefully, forcing shutdown");
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for UUID expiration checker to terminate");
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("UUID expiration checker shut down completed");
        }
    }
}
