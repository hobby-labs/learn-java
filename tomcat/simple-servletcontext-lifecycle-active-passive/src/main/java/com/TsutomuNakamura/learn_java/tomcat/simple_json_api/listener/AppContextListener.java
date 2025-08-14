package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.JwsInfo;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.service.JwsActivePassiveManagementService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class AppContextListener implements ServletContextListener {
    
    public static final String JWS_INFO_KEY = "jwsInfo";
    private static final int UPDATE_INTERVAL_SECONDS = 10;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;
    private static final String THREAD_NAME = "JWS-Maintenance-Thread";
    
    private ScheduledExecutorService scheduler;
    private ServletContextEvent servletContextEvent;
    private JwsActivePassiveManagementService jwsService;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.servletContextEvent = sce; // Store reference for use in scheduled task
        
        try {
            this.jwsService = new JwsActivePassiveManagementService(); // Initialize JWS management service
            
            // Get current active JWS info from service (handles loading/generation logic)
            JwsInfo currentJwsInfo = jwsService.getCurrentActiveJwsInfo();
            
            // Update ServletContext with current JWS info
            updateServletContext(currentJwsInfo);
            
            System.out.println("Application started successfully at " + new java.util.Date());
            System.out.println("Active/Passive JWS Management initialized - " + jwsService.getTokenManager().getTokenSummary());
            
            // Start background thread to check JWS expiration periodically
            startJwsExpirationChecker();
            
            System.out.println("JWS maintenance checker started - will check every " + UPDATE_INTERVAL_SECONDS + " seconds");
            
        } catch (Exception e) {
            System.err.println("Failed to initialize JWS management service: " + e.getMessage());
            e.printStackTrace();
            
            // Create a fallback JwsInfo to prevent the application from failing
            JwsInfo fallbackInfo = new JwsInfo("JWS_INIT_FAILED", 
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
            updateServletContext(fallbackInfo);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Good bye!");
        
        shutdownJwsExpirationChecker();
        
        System.out.println("Application shutdown completed at " + new java.util.Date());
    }

    /**
     * Starts the background thread for periodic JWS maintenance (rotation and cleanup)
     */
    private void startJwsExpirationChecker() {
        if (jwsService == null) {
            System.err.println("Cannot start JWS maintenance checker - service not initialized");
            return;
        }
        
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, THREAD_NAME);
            t.setDaemon(true); // Make it a daemon thread so it doesn't prevent JVM shutdown
            return t;
        });
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Perform periodic maintenance: token rotation, expiration cleanup
                JwsInfo refreshedJwsInfo = jwsService.performPeriodicMaintenance();
                updateServletContext(refreshedJwsInfo);
            } catch (Exception e) {
                System.err.println("[JWS-Maintenance-Checker] Error during periodic maintenance: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
        
    /**
     * Gracefully shuts down the JWS maintenance checker thread
     */
    private void shutdownJwsExpirationChecker() {
        if (scheduler != null && !scheduler.isShutdown()) {
            System.out.println("Shutting down JWS maintenance checker thread...");
            scheduler.shutdown();
            try {
                // Wait up to configured timeout for existing tasks to terminate
                if (!scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    System.out.println("JWS maintenance checker did not terminate gracefully, forcing shutdown");
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for JWS maintenance checker to terminate");
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("JWS maintenance checker shut down completed");
        }
    }

    /**
     * Updates ServletContext with JWS information
     * @param jwsInfo JwsInfo object to store
     */
    private void updateServletContext(JwsInfo jwsInfo) {
        servletContextEvent.getServletContext().setAttribute(JWS_INFO_KEY, jwsInfo);
    }

}
