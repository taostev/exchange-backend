package com.exchange.common;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * JWT 工具：生成和校验轻量级 HS256 Token，用于前后端分离的登录状态维持。
 */
public class JwtUtils {
    private static final String SECRET = "exchange-backend-secret-key";
    private static final long EXPIRE_MS = 7L * 24 * 60 * 60 * 1000;

    public static String generateToken(Long userId, Integer role) {
        long expireAt = System.currentTimeMillis() + EXPIRE_MS;
        String payload = userId + ":" + role + ":" + expireAt;
        String payloadText = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return payloadText + "." + sign(payloadText);
    }

    public static JwtPayload parseToken(String token) {
        try {
            if (token == null || !token.contains(".")) {
                throw new BusinessException(401, "Token 无效");
            }
            String[] parts = token.split("\\.", 2);
            if (!sign(parts[0]).equals(parts[1])) {
                throw new BusinessException(401, "Token 签名无效");
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] fields = payload.split(":");
            long expireAt = Long.parseLong(fields[2]);
            if (expireAt < System.currentTimeMillis()) {
                throw new BusinessException(401, "Token 已过期");
            }
            return new JwtPayload(Long.parseLong(fields[0]), Integer.parseInt(fields[1]));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(401, "Token 解析失败");
        }
    }

    private static String sign(String text) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BusinessException("Token 签名失败");
        }
    }

    public static class JwtPayload {
        private final Long userId;
        private final Integer role;

        public JwtPayload(Long userId, Integer role) {
            this.userId = userId;
            this.role = role;
        }

        public Long getUserId() {
            return userId;
        }

        public Integer getRole() {
            return role;
        }
    }
}
