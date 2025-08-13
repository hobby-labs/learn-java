package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.User;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.AppInfo;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.ApiResponse;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.service.UserService;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util.JsonResponseUtil;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.listener.AppContextListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@WebServlet("/api/users/*")
public class UserController extends HttpServlet {
    
    private final UserService userService = new UserService();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get all users
            List<User> users = userService.getAllUsers();
            
            // Get app info from ServletContext (try extended first, fallback to CSV)
            AppInfo appInfo = (AppInfo) getServletContext().getAttribute(AppContextListener.APP_INFO_CSV_KEY + "_EXTENDED");
            
            if (appInfo == null) {
                // Fallback to CSV parsing
                String csvData = (String) getServletContext().getAttribute(AppContextListener.APP_INFO_CSV_KEY);
                if (csvData != null) {
                    appInfo = AppInfo.fromCsv(csvData);
                }
            }
            
            // Create response data with both users and app info
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("users", users);
            responseData.put("appInfo", appInfo);
            
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(responseData);
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_OK);
        } catch (Exception e) {
            ApiResponse<Object> apiResponse = ApiResponse.error("Internal server error");
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
