package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.User;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.ApiResponse;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.service.UserService;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.exception.UserNotFoundException;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.exception.InvalidDataException;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util.JsonResponseUtil;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/users/*")
public class UserController extends HttpServlet {
    
    private final UserService userService = new UserService();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all users
                List<User> users = userService.getAllUsers();
                ApiResponse<List<User>> apiResponse = ApiResponse.success(users);
                JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_OK);
            } else {
                // Get specific user by ID
                Long userId = parseUserIdFromPath(pathInfo);
                User user = userService.getUserById(userId);
                ApiResponse<User> apiResponse = ApiResponse.success(user);
                JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_OK);
            }
        } catch (UserNotFoundException e) {
            ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_NOT_FOUND);
        } catch (InvalidDataException e) {
            ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            ApiResponse<Object> apiResponse = ApiResponse.error("Internal server error");
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
 
    // Helper methods
    private Long parseUserIdFromPath(String pathInfo) throws InvalidDataException {
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length != 2) {
            throw new InvalidDataException("Invalid request format");
        }
        
        try {
            return Long.parseLong(pathParts[1]);
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Invalid user ID format");
        }
    }
}
