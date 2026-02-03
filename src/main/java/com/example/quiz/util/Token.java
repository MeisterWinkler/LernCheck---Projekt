package com.example.quiz.util;

import java.security.SecureRandom;

public class Token {

    private static final String ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RND = new SecureRandom();

    public static String generate(int length) {
        if (length < 4) length = 4;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUM.charAt(RND.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }
}