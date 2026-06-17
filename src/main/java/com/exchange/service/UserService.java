package com.exchange.service;

import com.exchange.entity.User;

import java.util.List;

public interface UserService {
    // 处理用户注册业务
    boolean register(User user);

    // 处理用户登录业务
    User login(String username, String password);

    // 注册前校验用户名是否可用
    boolean isUsernameAvailable(String username);

    // 查询当前登录用户资料
    User getById(Long userId);

    // 修改当前登录用户资料
    boolean updateProfile(Long userId, User user);

    // 管理员查询用户列表
    List<User> listUsers();

    // 管理员冻结或恢复账号
    boolean updateStatus(Long userId, Integer status);
}