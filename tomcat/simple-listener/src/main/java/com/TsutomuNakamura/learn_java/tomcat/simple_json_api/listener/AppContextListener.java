package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.AppInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@WebListener
public class AppContextListener implements ServletContextListener {
    
    public static final String APP_INFO_CSV_KEY = "appInfoCsv";
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Hello!");
        
        // Generate UUID and date string
        String uuid = UUID.randomUUID().toString();
        String dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // Create AppInfo object
        AppInfo appInfo = new AppInfo(uuid, dateString);
        
        // Store as CSV in ServletContext
        String csvData = appInfo.toCsv();
        sce.getServletContext().setAttribute(APP_INFO_CSV_KEY, csvData);
        
        System.out.println("Application started successfully at " + new java.util.Date());
        System.out.println("Generated UUID: " + uuid);
        System.out.println("Generated Date: " + dateString);
        System.out.println("Stored CSV: " + csvData);
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Good bye!");
        // Optional: You can also log additional shutdown information
        System.out.println("Application shutdown completed at " + new java.util.Date());
    }
}
