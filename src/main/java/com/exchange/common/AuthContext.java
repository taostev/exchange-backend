package com.exchange.common;

/**
 * 登录上下文：拦截器解析 JWT 后把当前用户 ID/角色放入 ThreadLocal，业务层可直接读取。
 */
public class AuthContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> ROLE = new ThreadLocal<>();

    public static void set(Long userId, Integer role) {
        USER_ID.set(userId);
        ROLE.set(role);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static Integer getRole() {
        return ROLE.get();
    }

    public static boolean isAdmin() {
        return ROLE.get() != null && ROLE.get() == 1;
    }

    public static void clear() {
        USER_ID.remove();
        ROLE.remove();
    }
}
