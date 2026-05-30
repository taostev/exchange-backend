package com.exchange.controller;

import com.exchange.common.Result;
import com.exchange.entity.User;
import com.exchange.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户核心模块", description = "负责处理用户的注册、登录以及鉴权相关的接口") // 标记分类说明
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户注册接口", description = "接收前端传来的用户名、密码及联系方式，校验通过后写入数据库 sys_user 表") // 标记方法说明
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return Result.error("账号或密码不能为空");
        }

        boolean success = userService.register(user);
        if (success) {
            return Result.success("注册成功！");
        } else {
            return Result.error("注册失败，用户名已存在");
        }
    }

    @Operation(summary = "用户登录接口", description = "根据账号密码进行比对，若账号被管理员封禁（status=0）则拒绝登录")
    @PostMapping("/login")
    public Result<User> login(@RequestBody User loginUser) {
        User user = userService.login(loginUser.getUsername(), loginUser.getPassword());
        if (user != null) {
            user.setPassword(null); // 安全处理
            return Result.success(user);
        } else {
            return Result.error("登录失败，账号或密码错误或已被封禁");
        }
    }
}