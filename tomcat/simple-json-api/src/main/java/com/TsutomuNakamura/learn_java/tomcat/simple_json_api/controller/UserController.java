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
import java.util.stream.Collectors;

@WebServlet("/api/users/*")
public class UserController extends HttpServlet {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
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
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            User newUser = parseUserFromRequest(request);
            User createdUser = userService.createUser(newUser);
            
            ApiResponse<User> apiResponse = ApiResponse.success("User created successfully", createdUser);
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_CREATED);
        } catch (InvalidDataException e) {
            ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            ApiResponse<Object> apiResponse = ApiResponse.error("Invalid JSON format or server error");
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            ApiResponse<Object> apiResponse = ApiResponse.error("User ID is required for update");
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            Long userId = parseUserIdFromPath(pathInfo);
            User updatedUser = parseUserFromRequest(request);
            
            User user = userService.updateUser(userId, updatedUser);
            ApiResponse<User> apiResponse = ApiResponse.success("User updated successfully", user);
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_OK);
        } catch (UserNotFoundException e) {
            ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_NOT_FOUND);
        } catch (InvalidDataException e) {
            ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            ApiResponse<Object> apiResponse = ApiResponse.error("Invalid JSON format or server error");
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            ApiResponse<Object> apiResponse = ApiResponse.error("User ID is required for deletion");
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            Long userId = parseUserIdFromPath(pathInfo);
            userService.deleteUser(userId);
            
            ApiResponse<Object> apiResponse = ApiResponse.success("User deleted successfully", null);
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_OK);
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
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Handle CORS preflight requests
        JsonResponseUtil.setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
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
    
    private User parseUserFromRequest(HttpServletRequest request) throws IOException, InvalidDataException {
        String jsonString = request.getReader().lines().collect(Collectors.joining());
        if (jsonString == null || jsonString.trim().isEmpty()) {
            throw new InvalidDataException("Request body cannot be empty");
        }
        
        try {
            return objectMapper.readValue(jsonString, User.class);
        } catch (Exception e) {
            throw new InvalidDataException("Invalid JSON format");
        }
    }
}
