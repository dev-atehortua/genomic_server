package com.genomic.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumUtil {
    
    public static String calculateMD5(String input) {
        return calculateHash(input, "MD5");
    }
    
    public static String calculateSHA256(String input) {
        return calculateHash(input, "SHA-256");
    }
    
    private static String calculateHash(String input, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(input.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available: " + algorithm, e);
        }
    }
    
    public static boolean verifyChecksum(String input, String expectedChecksum) {
        String md5 = calculateMD5(input);
        String sha256 = calculateSHA256(input);
        
        return md5.equals(expectedChecksum) || sha256.equals(expectedChecksum);
    }
}
