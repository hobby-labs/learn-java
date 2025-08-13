package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * Utility class for loading application configuration from YAML files
 */
public class ConfigUtil {
    
    private static final String CONFIG_FILE = "application.yml";
    private static Map<String, Object> config;
    
    static {
        loadConfig();
    }
    
    /**
     * Loads the configuration from application.yml
     */
    private static void loadConfig() {
        try (InputStream inputStream = ConfigUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new RuntimeException("Could not find " + CONFIG_FILE + " in classpath");
            }
            
            Yaml yaml = new Yaml();
            config = yaml.load(inputStream);
            
            System.out.println("Successfully loaded configuration from " + CONFIG_FILE);
        } catch (Exception e) {
            System.err.println("Failed to load configuration from " + CONFIG_FILE + ": " + e.getMessage());
            throw new RuntimeException("Configuration loading failed", e);
        }
    }
    
    /**
     * Gets a configuration value using dot notation (e.g., "jws.private-key")
     * 
     * @param key Configuration key in dot notation
     * @return Configuration value as string, or null if not found
     */
    public static String getString(String key) {
        Object value = getValue(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Gets a configuration value using dot notation
     * 
     * @param key Configuration key in dot notation
     * @return Configuration value as object, or null if not found
     */
    @SuppressWarnings("unchecked")
    public static Object getValue(String key) {
        if (config == null) {
            return null;
        }
        
        String[] keys = key.split("\\.");
        Map<String, Object> current = config;
        
        for (int i = 0; i < keys.length - 1; i++) {
            Object next = current.get(keys[i]);
            if (next instanceof Map) {
                current = (Map<String, Object>) next;
            } else {
                return null;
            }
        }
        
        return current.get(keys[keys.length - 1]);
    }
    
    /**
     * Gets the JWS private key from configuration
     * 
     * @return Private key as PEM string
     * @throws RuntimeException if private key is not configured
     */
    public static String getJwsPrivateKey() {
        String privateKey = getString("jws.private-key");
        if (privateKey == null || privateKey.trim().isEmpty()) {
            throw new RuntimeException("JWS private key not configured in application.yml");
        }
        return privateKey.trim();
    }
}
