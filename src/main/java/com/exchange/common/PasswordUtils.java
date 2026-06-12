package com.exchange.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码工具：注册时生成 salt+hash，登录时重新计算并比对，避免明文密码落库。
 */
public class PasswordUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String encode(String rawPassword) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltText = Base64.getEncoder().encodeToString(salt);
        return saltText + ":" + sha256(saltText + rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || !encodedPassword.contains(":")) {
            return false;
        }
        String[] parts = encodedPassword.split(":", 2);
        return encodedPassword.equals(parts[0] + ":" + sha256(parts[0] + rawPassword));
    }

    private static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new BusinessException("密码加密失败");
        }
    }
}
