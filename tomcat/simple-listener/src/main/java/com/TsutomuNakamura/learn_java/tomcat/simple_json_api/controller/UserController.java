package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.User;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.ApiResponse;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.service.UserService;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util.JsonResponseUtil;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/users/*")
public class UserController extends HttpServlet {
    
    private final UserService userService = new UserService();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Get all users
            List<User> users = userService.getAllUsers();
            ApiResponse<List<User>> apiResponse = ApiResponse.success(users);
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_OK);
        } catch (Exception e) {
            ApiResponse<Object> apiResponse = ApiResponse.error("Internal server error");
            JsonResponseUtil.sendJsonResponse(response, apiResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
