package com.exchange.common;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 可选鉴权：公开接口若携带有效 Token，也能读取当前用户上下文。
 */
public final class AuthTokenSupport {
    private AuthTokenSupport() {
    }

    public static void bindOptionalUser(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return;
        }
        try {
            JwtUtils.JwtPayload payload = JwtUtils.parseToken(authorization.substring(7));
            AuthContext.set(payload.getUserId(), payload.getRole());
        } catch (BusinessException ignored) {
            // 公开接口忽略无效 Token，按游客处理。
        }
    }
}
