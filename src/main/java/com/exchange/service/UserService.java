package com.exchange.service;

import com.exchange.entity.User;

public interface UserService {
    // 处理用户注册业务
    boolean register(User user);

    // 处理用户登录业务
    User login(String username, String password);
}