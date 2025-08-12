package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Hello!");
        // Optional: You can also log additional startup information
        System.out.println("Application started successfully at " + new java.util.Date());
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Good bye!");
        // Optional: You can also log additional shutdown information
        System.out.println("Application shutdown completed at " + new java.util.Date());
    }
}
