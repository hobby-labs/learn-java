package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for standardized API responses.
 * This class provides consistent response formatting across all controllers.
 */
public class ApiResponseUtil {
    
    // Response keys
    private static final String JWS_KEY = "jws";
    private static final String ERROR_KEY = "error";
    
    // Error messages
    private static final String JWS_NOT_FOUND_MESSAGE = "JWS token not available";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error";
    
    /**
     * Sends a successful JWS response
     * @param response HTTP response object
     * @param jws JWS token to include in response
     * @throws IOException if response writing fails
     */
    public static void sendJwsResponse(HttpServletResponse response, String jws) throws IOException {
        Map<String, String> jwsResponse = new HashMap<>();
        jwsResponse.put(JWS_KEY, jws);
        JsonResponseUtil.sendJsonResponse(response, jwsResponse, HttpServletResponse.SC_OK);
    }
    
    /**
     * Sends a JWS not found error response
     * @param response HTTP response object
     * @throws IOException if response writing fails
     */
    public static void sendJwsNotFoundError(HttpServletResponse response) throws IOException {
        sendErrorResponse(response, JWS_NOT_FOUND_MESSAGE, HttpServletResponse.SC_NOT_FOUND);
    }
    
    /**
     * Sends an internal server error response
     * @param response HTTP response object
     * @param exception Exception that caused the error
     * @throws IOException if response writing fails
     */
    public static void sendInternalServerError(HttpServletResponse response, Exception exception) throws IOException {
        String errorMessage = INTERNAL_SERVER_ERROR_MESSAGE + ": " + exception.getMessage();
        sendErrorResponse(response, errorMessage, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Sends a generic error response
     * @param response HTTP response object
     * @param message Error message
     * @param statusCode HTTP status code
     * @throws IOException if response writing fails
     */
    private static void sendErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put(ERROR_KEY, message);
        JsonResponseUtil.sendJsonResponse(response, errorResponse, statusCode);
    }
}
