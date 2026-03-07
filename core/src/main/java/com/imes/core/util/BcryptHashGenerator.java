package com.imes.core.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=== BCRYPT HASHES FOR TEST ACCOUNTS ===\n");
        
        String[] passwords = {"admin123", "hr123", "mentor123", "intern123"};
        String[] emails = {"admin@imes.com", "hr@imes.com", "mentor@imes.com", "intern@imes.com"};
        
        for (int i = 0; i < passwords.length; i++) {
            String hash = encoder.encode(passwords[i]);
            System.out.println("Email: " + emails[i]);
            System.out.println("Password: " + passwords[i]);
            System.out.println("Hash: " + hash);
            System.out.println();
        }
    }
}
