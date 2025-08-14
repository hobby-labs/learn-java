package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.JwsInfo;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.model.JwsTokenManager;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.service.JwsActivePassiveManagementService;
import com.TsutomuNakamura.learn_java.tomcat.simple_json_api.service.JwsService;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/api/jws/status")
public class JwsStatusController extends HttpServlet {
    
    private JwsService jwsService;
    
    @Override
    public void init() throws ServletException {
        this.jwsService = new JwsService(getServletContext());
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>JWS Active/Passive Token Status</title>");
            out.println("<style>");
            out.println("body { font-family: monospace; margin: 20px; }");
            out.println(".active { color: #006400; font-weight: bold; }");
            out.println(".passive { color: #4169E1; }");
            out.println(".expired { color: #DC143C; text-decoration: line-through; }");
            out.println(".container { max-width: 800px; }");
            out.println(".token-info { margin: 10px 0; padding: 10px; border: 1px solid #ccc; border-radius: 5px; }");
            out.println(".refresh-btn { padding: 10px 20px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; margin: 10px 0; }");
            out.println("</style>");
            out.println("<script>");
            out.println("function autoRefresh() { setTimeout(function(){ location.reload(); }, 5000); }");
            out.println("</script>");
            out.println("</head>");
            out.println("<body onload='autoRefresh()'>");
            out.println("<div class='container'>");
            
            out.println("<h1>JWS Active/Passive Token Management Status</h1>");
            out.println("<p><strong>Current Time:</strong> " + 
                       LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>");
            out.println("<p><em>This page auto-refreshes every 5 seconds</em></p>");
            
            // Get current active token from ServletContext (what the API returns)
            if (jwsService.isJwsAvailable()) {
                JwsInfo currentActiveToken = jwsService.getCurrentJwsInfo();
                
                out.println("<h2>Current API Response (Active Token)</h2>");
                out.println("<div class='token-info active'>");
                out.println("<strong>JWS:</strong> " + truncateJws(currentActiveToken.getJws()) + "<br>");
                out.println("<strong>Created:</strong> " + currentActiveToken.getFormattedCreatedTime() + "<br>");
                out.println("<strong>Expires:</strong> " + currentActiveToken.getFormattedExpiresTime() + "<br>");
                out.println("<strong>Status:</strong> " + (currentActiveToken.isExpired() ? "EXPIRED" : "ACTIVE"));
                out.println("</div>");
                
                // Create a temporary service instance to get detailed status
                try {
                    JwsActivePassiveManagementService tempService = new JwsActivePassiveManagementService();
                    JwsTokenManager tokenManager = tempService.getTokenManager();
                    
                    out.println("<h2>Token Management Details</h2>");
                    out.println("<pre>");
                    out.println(tempService.getDetailedStatus());
                    out.println("</pre>");
                    
                    out.println("<h3>Active Token</h3>");
                    if (tokenManager.hasActiveToken()) {
                        JwsInfo activeToken = tokenManager.getActiveToken();
                        out.println("<div class='token-info " + 
                                   (activeToken.isExpired() ? "expired" : "active") + "'>");
                        out.println("<strong>JWS:</strong> " + truncateJws(activeToken.getJws()) + "<br>");
                        out.println("<strong>Created:</strong> " + activeToken.getFormattedCreatedTime() + "<br>");
                        out.println("<strong>Expires:</strong> " + activeToken.getFormattedExpiresTime() + "<br>");
                        out.println("<strong>Status:</strong> " + (activeToken.isExpired() ? "EXPIRED" : "ACTIVE"));
                        out.println("</div>");
                    } else {
                        out.println("<p>No active token found</p>");
                    }
                    
                    out.println("<h3>Passive Tokens (" + tokenManager.getPassiveTokenCount() + ")</h3>");
                    List<JwsInfo> passiveTokens = tokenManager.getPassiveTokens();
                    if (passiveTokens.isEmpty()) {
                        out.println("<p>No passive tokens</p>");
                    } else {
                        for (int i = 0; i < passiveTokens.size(); i++) {
                            JwsInfo token = passiveTokens.get(i);
                            out.println("<div class='token-info " + 
                                       (token.isExpired() ? "expired" : "passive") + "'>");
                            out.println("<strong>Token " + (i + 1) + ":</strong><br>");
                            out.println("<strong>JWS:</strong> " + truncateJws(token.getJws()) + "<br>");
                            out.println("<strong>Created:</strong> " + token.getFormattedCreatedTime() + "<br>");
                            out.println("<strong>Expires:</strong> " + token.getFormattedExpiresTime() + "<br>");
                            out.println("<strong>Status:</strong> " + (token.isExpired() ? "EXPIRED" : "PASSIVE"));
                            out.println("</div>");
                        }
                    }
                    
                    out.println("<h3>Configuration</h3>");
                    out.println("<ul>");
                    out.println("<li><strong>Check Interval:</strong> 10 seconds</li>");
                    out.println("<li><strong>Token Rotation:</strong> " + tempService.getRotationMinutes() + " minute(s)</li>");
                    out.println("<li><strong>Token Expiration:</strong> " + tempService.getExpirationMinutes() + " minute(s)</li>");
                    out.println("</ul>");
                    
                } catch (Exception e) {
                    out.println("<h2>Error accessing token management details</h2>");
                    out.println("<pre>" + e.getMessage() + "</pre>");
                }
                
            } else {
                out.println("<h2>No JWS Available</h2>");
                out.println("<p>The JWS system is not currently available.</p>");
            }
            
            out.println("<h3>Expected Behavior Timeline</h3>");
            out.println("<ol>");
            out.println("<li><strong>T+0:</strong> 1st JWS token created when application starts</li>");
            out.println("<li><strong>T+1 min:</strong> 2nd JWS token created, 1st token moves to passive</li>");
            out.println("<li><strong>T+2 min:</strong> 3rd JWS token created, 2nd token moves to passive</li>");
            out.println("<li><strong>T+3 min:</strong> 4th JWS token created, 3rd token moves to passive, 1st token expires and is removed</li>");
            out.println("<li><strong>Every 10 sec:</strong> Maintenance check runs (token rotation + cleanup)</li>");
            out.println("</ol>");
            
            out.println("<button class='refresh-btn' onclick='location.reload()'>Manual Refresh</button>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.println("Error: " + e.getMessage());
            }
        }
    }
    
    private String truncateJws(String jws) {
        if (jws == null) return "null";
        if (jws.length() <= 50) return jws;
        return jws.substring(0, 20) + "..." + jws.substring(jws.length() - 20);
    }
}
