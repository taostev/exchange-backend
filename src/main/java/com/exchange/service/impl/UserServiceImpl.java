package com.exchange.service.impl;

import com.exchange.common.BusinessException;
import com.exchange.common.PasswordUtils;
import com.exchange.entity.User;
import com.exchange.mapper.UserMapper;
import com.exchange.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service // 必须加，声明这是 Service 组件
public class UserServiceImpl implements UserService {

    @Autowired // 自动注入仓库管理员 UserMapper
    private UserMapper userMapper;

    @Override
    public boolean register(User user) {
        // 1. 先检查用户名是否已经存在
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            return false; // 用户名已存在，注册失败
        }

        // 2. 初始化新用户默认属性（根据你的详细设计书：0-普通会员，1-正常状态）
        user.setRole(0);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setPassword(PasswordUtils.encode(user.getPassword()));

        // 3. 写入数据库
        int rows = userMapper.insertUser(user);
        return rows > 0;
    }

    @Override
    public User login(String username, String password) {
        // 1. 根据用户名查出用户
        User user = userMapper.findByUsername(username);

        // 2. 校验用户是否存在，以及密码是否正确
        if (user != null && PasswordUtils.matches(password, user.getPassword())) {
            // 3. 检查账号是否被封禁（status 为 0 代表限制登录）
            if (user.getStatus() == 0) {
                return null;
            }
            return user; // 登录成功，返回整个用户对象（不含敏感处理逻辑先走通）
        }
        return null; // 登录失败
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return userMapper.findByUsername(username.trim()) == null;
    }

    @Override
    public User getById(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public boolean updateProfile(Long userId, User user) {
        if (user.getContactInfo() == null || user.getContactInfo().isBlank()) {
            throw new BusinessException("联系方式不能为空");
        }
        user.setId(userId);
        return userMapper.updateProfile(user) > 0;
    }

    @Override
    public List<User> listUsers() {
        List<User> users = userMapper.selectAll();
        for (User user : users) {
            user.setPassword(null);
        }
        return users;
    }

    @Override
    public boolean updateStatus(Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("账号状态只能是 0 或 1");
        }
        return userMapper.updateStatus(userId, status) > 0;
    }
}