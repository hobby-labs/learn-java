package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.service;

import jakarta.servlet.ServletContext;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.JwsInfo;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.listener.AppContextListener;

/**
 * Service class for JWS-related operations in web controllers.
 * This service provides a clean abstraction layer between controllers and ServletContext,
 * making the controllers more testable and maintainable.
 */
public class JwsService {
    private final ServletContext servletContext;
    
    public JwsService(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /**
     * Retrieves the current JWS information from ServletContext
     * @return JwsInfo object if available, null otherwise
     */
    public JwsInfo getCurrentJwsInfo() {
        return (JwsInfo) servletContext.getAttribute(AppContextListener.JWS_INFO_KEY);
    }
    
    /**
     * Checks if JWS information is currently available
     * @return true if JWS info is available, false otherwise
     */
    public boolean isJwsAvailable() {
        return getCurrentJwsInfo() != null;
    }
}
