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
    private ScheduledExecutorService scheduler;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Hello!");
        
        // Generate initial UUID and date string
        String uuid = UUID.randomUUID().toString();
        String dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // Create AppInfo object
        AppInfo appInfo = new AppInfo(uuid, dateString);
        
        // Store as CSV in ServletContext
        String csvData = appInfo.toCsv();
        sce.getServletContext().setAttribute(APP_INFO_CSV_KEY, csvData);
        
        System.out.println("Application started successfully at " + new java.util.Date());
        System.out.println("Generated initial UUID: " + uuid);
        System.out.println("Generated Date: " + dateString);
        System.out.println("Stored CSV: " + csvData);
        
        // Start background thread to update UUID every 10 seconds
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "UUID-Updater-Thread");
            t.setDaemon(true); // Make it a daemon thread so it doesn't prevent JVM shutdown
            return t;
        });
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Generate new UUID and date
                String newUuid = UUID.randomUUID().toString();
                String newDateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
                // Create new AppInfo object
                AppInfo newAppInfo = new AppInfo(newUuid, newDateString);
                
                // Update CSV in ServletContext
                String newCsvData = newAppInfo.toCsv();
                sce.getServletContext().setAttribute(APP_INFO_CSV_KEY, newCsvData);
                
                System.out.println("[UUID-Updater] Updated UUID: " + newUuid + " at " + newDateString);
            } catch (Exception e) {
                System.err.println("[UUID-Updater] Error updating UUID: " + e.getMessage());
                e.printStackTrace();
            }
        }, 10, 10, TimeUnit.SECONDS); // Start after 10 seconds, then repeat every 10 seconds
        
        System.out.println("UUID updater thread started - will update every 10 seconds");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Good bye!");
        
        // Shutdown the UUID updater thread
        if (scheduler != null && !scheduler.isShutdown()) {
            System.out.println("Shutting down UUID updater thread...");
            scheduler.shutdown();
            try {
                // Wait up to 5 seconds for existing tasks to terminate
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
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
        
        System.out.println("Application shutdown completed at " + new java.util.Date());
    }
}
