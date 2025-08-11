package com.TsutomuNakamura.learn_java.tomcat.simple_json_api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/api/users/*")
public class HelloApi extends HttpServlet {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<User> users = new ArrayList<>();
    
    @Override
    public void init() throws ServletException {
        // Initialize with some sample data
        users.add(new User(1L, "John Doe", "john@example.com", 30));
        users.add(new User(2L, "Jane Smith", "jane@example.com", 25));
        users.add(new User(3L, "Bob Johnson", "bob@example.com", 35));
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all users
                ApiResponse<List<User>> apiResponse = ApiResponse.success(users);
                sendJsonResponse(response, apiResponse, HttpServletResponse.SC_OK);
            } else {
                // Get specific user by ID
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    Long userId = Long.parseLong(pathParts[1]);
                    User user = users.stream()
                            .filter(u -> u.getId().equals(userId))
                            .findFirst()
                            .orElse(null);
                    
                    if (user != null) {
                        ApiResponse<User> apiResponse = ApiResponse.success(user);
                        sendJsonResponse(response, apiResponse, HttpServletResponse.SC_OK);
                    } else {
                        ApiResponse<Object> apiResponse = ApiResponse.error("User not found");
                        sendJsonResponse(response, apiResponse, HttpServletResponse.SC_NOT_FOUND);
                    }
                } else {
                    ApiResponse<Object> apiResponse = ApiResponse.error("Invalid request format");
                    sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        } catch (NumberFormatException e) {
            ApiResponse<Object> apiResponse = ApiResponse.error("Invalid user ID format");
            sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            ApiResponse<Object> apiResponse = ApiResponse.error("Internal server error");
            sendJsonResponse(response, apiResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String jsonString = request.getReader().lines().collect(Collectors.joining());
            User newUser = objectMapper.readValue(jsonString, User.class);
            
            // Generate new ID
            Long newId = users.stream().mapToLong(User::getId).max().orElse(0L) + 1;
            newUser.setId(newId);
            
            users.add(newUser);
            
            ApiResponse<User> apiResponse = ApiResponse.success("User created successfully", newUser);
            sendJsonResponse(response, apiResponse, HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            ApiResponse<Object> apiResponse = ApiResponse.error("Invalid JSON format or missing required fields");
            sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            ApiResponse<Object> apiResponse = ApiResponse.error("User ID is required for update");
            sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length == 2) {
                Long userId = Long.parseLong(pathParts[1]);
                
                String jsonString = request.getReader().lines().collect(Collectors.joining());
                User updatedUser = objectMapper.readValue(jsonString, User.class);
                
                User existingUser = users.stream()
                        .filter(u -> u.getId().equals(userId))
                        .findFirst()
                        .orElse(null);
                
                if (existingUser != null) {
                    existingUser.setName(updatedUser.getName());
                    existingUser.setEmail(updatedUser.getEmail());
                    existingUser.setAge(updatedUser.getAge());
                    
                    ApiResponse<User> apiResponse = ApiResponse.success("User updated successfully", existingUser);
                    sendJsonResponse(response, apiResponse, HttpServletResponse.SC_OK);
                } else {
                    ApiResponse<Object> apiResponse = ApiResponse.error("User not found");
                    sendJsonResponse(response, apiResponse, HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                ApiResponse<Object> apiResponse = ApiResponse.error("Invalid request format");
                sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (NumberFormatException e) {
            ApiResponse<Object> apiResponse = ApiResponse.error("Invalid user ID format");
            sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            ApiResponse<Object> apiResponse = ApiResponse.error("Invalid JSON format or missing required fields");
            sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            ApiResponse<Object> apiResponse = ApiResponse.error("User ID is required for deletion");
            sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length == 2) {
                Long userId = Long.parseLong(pathParts[1]);
                
                boolean removed = users.removeIf(u -> u.getId().equals(userId));
                
                if (removed) {
                    ApiResponse<Object> apiResponse = ApiResponse.success("User deleted successfully", null);
                    sendJsonResponse(response, apiResponse, HttpServletResponse.SC_OK);
                } else {
                    ApiResponse<Object> apiResponse = ApiResponse.error("User not found");
                    sendJsonResponse(response, apiResponse, HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                ApiResponse<Object> apiResponse = ApiResponse.error("Invalid request format");
                sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (NumberFormatException e) {
            ApiResponse<Object> apiResponse = ApiResponse.error("Invalid user ID format");
            sendJsonResponse(response, apiResponse, HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            ApiResponse<Object> apiResponse = ApiResponse.error("Internal server error");
            sendJsonResponse(response, apiResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private void sendJsonResponse(HttpServletResponse response, Object data, int statusCode) 
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
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Handle CORS preflight requests
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
