package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util;

import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;

public class JsonResponseUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static void sendJsonResponse(HttpServletResponse response, Object data, int statusCode) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        
        // Enable CORS for browser testing
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        PrintWriter out = response.getWriter();
        String jsonString = objectMapper.writeValueAsString(data);
        out.print(jsonString);
        out.flush();
    }
}
