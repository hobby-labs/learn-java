package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class KeyUtil {
    /**
     * Loads the private key from the configuration file
     * 
     * @return ECPrivateKey for signing
     * @throws Exception if key loading fails
     */
    public static ECPrivateKey loadEcPrivateKey() throws Exception {
        // Load private key from configuration
        String privateKeyPEM = ConfigUtil.getJwsPrivateKey();
        
        // Remove PEM headers and decode Base64
        String cleanedKey = privateKeyPEM
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
        
        // Create key specification
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        
        // Generate private key
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        
        if (!(privateKey instanceof ECPrivateKey)) {
            throw new IllegalArgumentException("Private key is not an EC private key");
        }
        
        System.out.println("Successfully loaded JWS private key from configuration");
        return (ECPrivateKey) privateKey;
    }
}
