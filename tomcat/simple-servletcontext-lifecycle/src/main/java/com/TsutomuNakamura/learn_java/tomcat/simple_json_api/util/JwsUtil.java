package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * Utility class for generating JSON Web Signatures (JWS) using ES256 algorithm
 */
public class JwsUtil {
    
    private static final String PRIVATE_KEY_PEM = """
            -----BEGIN PRIVATE KEY-----
            MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgSXm+jWiG/BcUUEyy
            42A8GnIbSYYuW75sAwXOEBK0Ij+hRANCAATEa8CFI+wMQnEedjOVZUyMi/GOMUSs
            hfE3vebRtblWcU5Zus6XKjq8Gi4dvq0DHQKJ1/yjDjLMVNe73iP1nInM
            -----END PRIVATE KEY-----
            """;
    
    private final ECPrivateKey privateKey;
    
    public JwsUtil() throws Exception {
        this.privateKey = loadPrivateKey();
    }
    
    /**
     * Generates a JWS with the specified header and payload
     * 
     * Header: {"typ":"JWT","alg":"ES256"}
     * Payload: {"message": "hello"}
     * 
     * @return JWS token as string
     * @throws Exception if JWS generation fails
     */
    public String generateJws() throws Exception {
        // Create JWS header
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .type(JOSEObjectType.JWT)
                .build();
        
        // Create payload with claims
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .claim("message", "hello")
                .issueTime(new Date())
                .build();
        
        // Create signed JWT
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        
        // Create ECDSA signer
        ECDSASigner signer = new ECDSASigner(privateKey);
        
        // Sign the JWT
        signedJWT.sign(signer);
        
        // Return serialized JWS
        return signedJWT.serialize();
    }
    
    /**
     * Generates a JWS with custom payload data (e.g., user data)
     * 
     * Header: {"typ":"JWT","alg":"ES256"}
     * Payload: Custom data provided
     * 
     * @param payloadData Custom payload data to include in JWS
     * @return JWS token as string
     * @throws Exception if JWS generation fails
     */
    public String generateJwsWithPayload(Object payloadData) throws Exception {
        // Create JWS header
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .type(JOSEObjectType.JWT)
                .build();
        
        // Create payload with custom data
        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .issueTime(new Date());
        
        // Add custom payload data
        if (payloadData != null) {
            claimsBuilder.claim("data", payloadData);
        }
        
        JWTClaimsSet claimsSet = claimsBuilder.build();
        
        // Create signed JWT
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        
        // Create ECDSA signer
        ECDSASigner signer = new ECDSASigner(privateKey);
        
        // Sign the JWT
        signedJWT.sign(signer);
        
        // Return serialized JWS
        return signedJWT.serialize();
    }
    
    /**
     * Loads the private key from the hardcoded PEM string
     * 
     * @return ECPrivateKey for signing
     * @throws Exception if key loading fails
     */
    private ECPrivateKey loadPrivateKey() throws Exception {
        // Remove PEM headers and decode Base64
        String privateKeyPEM = PRIVATE_KEY_PEM
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
        
        // Create key specification
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        
        // Generate private key
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        
        if (!(privateKey instanceof ECPrivateKey)) {
            throw new IllegalArgumentException("Private key is not an EC private key");
        }
        
        return (ECPrivateKey) privateKey;
    }
    
    /**
     * Verifies that the JWS utility is working correctly
     * 
     * @return true if JWS can be generated successfully
     */
    public boolean isWorking() {
        try {
            String jws = generateJws();
            return jws != null && !jws.isEmpty() && jws.split("\\.").length == 3;
        } catch (Exception e) {
            System.err.println("JWS utility test failed: " + e.getMessage());
            return false;
        }
    }
}
