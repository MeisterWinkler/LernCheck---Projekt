package service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PinTokenService {
    private final SecureRandom random = new SecureRandom();
    private static final String DIGITS = "0123456789";
    private static final String ALPHANUM = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public String newPin(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        return sb.toString();
    }

    public String newAnonToken(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(ALPHANUM.charAt(random.nextInt(ALPHANUM.length())));
        return sb.toString();
    }
}