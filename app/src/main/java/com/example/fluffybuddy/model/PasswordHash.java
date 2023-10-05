package com.example.fluffybuddy.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHash {
    private static final String HASH_ALGORITHM = "SHA-256";

    public static String hashPassword(String password) {
        try {
            // Create an instance of MessageDigest with the specified algorithm
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);

            // Convert the password string to bytes
            byte[] passwordBytes = password.getBytes();

            // Generate the hashed password bytes
            byte[] hashedBytes = messageDigest.digest(passwordBytes);

            // Convert the hashed password bytes to a hexadecimal string
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : hashedBytes) {
                stringBuilder.append(String.format("%02x", b));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
