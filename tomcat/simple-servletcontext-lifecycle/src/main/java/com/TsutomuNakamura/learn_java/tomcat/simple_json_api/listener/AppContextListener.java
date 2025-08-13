package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.AppInfo;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.service.JwsManagementService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class AppContextListener implements ServletContextListener {
    
    public static final String APP_INFO_CSV_KEY = "appInfoCsv";
    private static final int UPDATE_INTERVAL_SECONDS = 10;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;
    private static final String THREAD_NAME = "JWS-Updater-Thread";
    
    private ScheduledExecutorService scheduler;
    private ServletContextEvent servletContextEvent;
    private JwsManagementService jwsService;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.servletContextEvent = sce; // Store reference for use in scheduled task
        
        try {
            this.jwsService = new JwsManagementService(); // Initialize JWS management service
            
            // Get current valid JWS info from service (handles loading/generation logic)
            AppInfo currentJwsInfo = jwsService.getCurrentValidJwsInfo();
            
            // Update ServletContext with current JWS info
            updateServletContext(currentJwsInfo);
            
            System.out.println("Application started successfully at " + new java.util.Date());
            
            // Start background thread to check JWS expiration periodically
            startJwsExpirationChecker();
            
            System.out.println("JWS expiration checker started - will check every " + UPDATE_INTERVAL_SECONDS + " seconds");
            
        } catch (Exception e) {
            System.err.println("Failed to initialize JWS management service: " + e.getMessage());
            e.printStackTrace();
            
            // Create a fallback AppInfo to prevent the application from failing
            AppInfo fallbackInfo = new AppInfo("JWS_INIT_FAILED", 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), "N/A");
            updateServletContext(fallbackInfo);
        }
    }
    
    /**
     * Updates ServletContext with JWS information
     * @param appInfo AppInfo object to store
     */
    private void updateServletContext(AppInfo appInfo) {
        servletContextEvent.getServletContext().setAttribute(APP_INFO_CSV_KEY, appInfo);
    }
    
    /**
     * Starts the background thread for periodic JWS expiration checking
     */
    private void startJwsExpirationChecker() {
        if (jwsService == null) {
            System.err.println("Cannot start JWS expiration checker - service not initialized");
            return;
        }
        
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, THREAD_NAME);
            t.setDaemon(true); // Make it a daemon thread so it doesn't prevent JVM shutdown
            return t;
        });
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Delegate expiration checking to the service
                AppInfo refreshedJwsInfo = jwsService.checkAndRefreshIfExpired();
                updateServletContext(refreshedJwsInfo);
            } catch (Exception e) {
                System.err.println("[JWS-Expiration-Checker] Error checking JWS expiration: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Good bye!");
        
        shutdownJwsExpirationChecker();
        
        System.out.println("Application shutdown completed at " + new java.util.Date());
    }
    
    /**
     * Gracefully shuts down the JWS expiration checker thread
     */
    private void shutdownJwsExpirationChecker() {
        if (scheduler != null && !scheduler.isShutdown()) {
            System.out.println("Shutting down JWS expiration checker thread...");
            scheduler.shutdown();
            try {
                // Wait up to configured timeout for existing tasks to terminate
                if (!scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    System.out.println("JWS expiration checker did not terminate gracefully, forcing shutdown");
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for JWS expiration checker to terminate");
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("JWS expiration checker shut down completed");
        }
    }
}
