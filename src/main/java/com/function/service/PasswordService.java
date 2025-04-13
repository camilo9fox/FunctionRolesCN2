package com.function.service;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordService {
    private static final int ROUNDS = 12;

    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(ROUNDS));
    }

    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}
