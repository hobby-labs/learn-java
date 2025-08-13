package com.TsutomuNakamura.learn_java.tomcat.simple_json_api.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.interfaces.ECPrivateKey;
import java.util.Date;

/**
 * Utility class for generating JSON Web Signatures (JWS) using ES256 algorithm
 */
public class JwsUtil {
    
    private final ECPrivateKey privateKey;
    
    public JwsUtil() throws Exception {
        this.privateKey = KeyUtil.loadEcPrivateKey();
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
