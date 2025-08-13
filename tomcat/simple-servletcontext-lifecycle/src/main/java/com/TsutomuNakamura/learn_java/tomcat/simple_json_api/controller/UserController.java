package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.JwsInfo;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util.JsonResponseUtil;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.listener.AppContextListener;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

@WebServlet("/api/users/*")
public class UserController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get JWS info from ServletContext (single source of truth)
            JwsInfo jwsInfo = (JwsInfo) getServletContext().getAttribute(AppContextListener.JWS_INFO_KEY);
            
            if (jwsInfo != null) {
                // Return response in the new format: {"jws": "<JWS_VALUE>"}
                // The JWS already contains user data in its payload
                Map<String, String> jwsResponse = new HashMap<>();
                jwsResponse.put("jws", jwsInfo.getJws());
                
                JsonResponseUtil.sendJsonResponse(response, jwsResponse, HttpServletResponse.SC_OK);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "App info not found");
                JsonResponseUtil.sendJsonResponse(response, errorResponse, HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error: " + e.getMessage());
            JsonResponseUtil.sendJsonResponse(response, errorResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
