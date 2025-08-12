package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.AppInfo;

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
    private static final int UPDATE_INTERVAL_SECONDS = 10;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;
    private static final String THREAD_NAME = "UUID-Updater-Thread";
    
    private ScheduledExecutorService scheduler;
    private ServletContextEvent servletContextEvent;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Hello!");
        this.servletContextEvent = sce; // Store reference for use in scheduled task
        
        // Generate and store initial UUID and date
        updateAppInfo("Generated initial UUID");
        
        System.out.println("Application started successfully at " + new java.util.Date());
        
        // Start background thread to update UUID every 10 seconds
        startUuidUpdaterThread();
        
        System.out.println("UUID updater thread started - will update every " + UPDATE_INTERVAL_SECONDS + " seconds");
    }
    
    /**
     * Generates new UUID and date, then updates ServletContext
     * @param logPrefix Prefix for console log message
     */
    private void updateAppInfo(String logPrefix) {
        String uuid = generateUuid();
        String dateString = generateDateString();
        
        AppInfo appInfo = new AppInfo(uuid, dateString);
        String csvData = appInfo.toCsv();
        
        servletContextEvent.getServletContext().setAttribute(APP_INFO_CSV_KEY, csvData);
        
        System.out.println("[" + logPrefix + "] UUID: " + uuid + " at " + dateString);
        if (logPrefix.contains("initial")) {
            System.out.println("Stored CSV: " + csvData);
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
     * Generates current date and time as formatted string
     * @return Formatted date string
     */
    private String generateDateString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
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
                updateAppInfo("UUID-Updater");
            } catch (Exception e) {
                System.err.println("[UUID-Updater] Error updating UUID: " + e.getMessage());
                e.printStackTrace();
            }
        }, UPDATE_INTERVAL_SECONDS, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);
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
