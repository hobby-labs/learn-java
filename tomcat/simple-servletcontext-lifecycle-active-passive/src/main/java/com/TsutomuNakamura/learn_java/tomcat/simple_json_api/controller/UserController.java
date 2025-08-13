package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.JwsInfo;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.service.JwsService;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util.ApiResponseUtil;

import java.io.IOException;

@WebServlet("/api/users/*")
public class UserController extends HttpServlet {
    
    private JwsService jwsService;
    
    @Override
    public void init() throws ServletException {
        this.jwsService = new JwsService(getServletContext());
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            if (jwsService.isJwsAvailable()) {
                JwsInfo jwsInfo = jwsService.getCurrentJwsInfo();
                ApiResponseUtil.sendJwsResponse(response, jwsInfo.getJws());
            } else {
                ApiResponseUtil.sendJwsNotFoundError(response);
            }
        } catch (Exception e) {
            ApiResponseUtil.sendInternalServerError(response, e);
        }
    }
}
