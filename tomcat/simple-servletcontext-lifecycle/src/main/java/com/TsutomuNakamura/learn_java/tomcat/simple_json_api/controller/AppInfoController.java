package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.AppInfo;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util.JsonResponseUtil;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.listener.AppContextListener;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

@WebServlet("/api/appinfo")
public class AppInfoController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get AppInfo object from ServletContext (single source of truth)
            AppInfo appInfo = (AppInfo) getServletContext().getAttribute(AppContextListener.APP_INFO_CSV_KEY);
            
            if (appInfo != null) {
                // Return response in the new format: {"jws": "<JWS_VALUE>"}
                Map<String, String> jwsResponse = new HashMap<>();
                jwsResponse.put("jws", appInfo.getJws());
                
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
