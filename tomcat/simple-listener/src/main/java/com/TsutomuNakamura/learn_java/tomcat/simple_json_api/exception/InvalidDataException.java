package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.exception;

public class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
    
    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
