package com.exchange.dto;

import com.exchange.entity.User;

/**
 * 登录响应 DTO：把 JWT Token 和脱敏后的用户信息一起返回给前端。
 */
public class LoginResponse {
    private String token;
    private User user;

    public LoginResponse() {}

    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
