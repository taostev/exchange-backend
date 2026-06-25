package com.exchange.controller;

import com.exchange.common.AuthContext;
import com.exchange.common.BusinessException;
import com.exchange.common.JwtUtils;
import com.exchange.common.Result;
import com.exchange.dto.LoginResponse;
import com.exchange.entity.User;
import com.exchange.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户核心模块", description = "负责处理用户的注册、登录以及鉴权相关的接口") // 标记分类说明
@RestController
@RequestMapping("/api/user")
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

    @Operation(summary = "用户名占用校验", description = "注册表单异步校验用户名是否可用")
    @GetMapping("/check-username")
    public Result<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        return Result.success(Map.of("available", userService.isUsernameAvailable(username)));
    }

    @Operation(summary = "用户登录接口", description = "根据账号密码进行比对，若账号被管理员封禁（status=0）则拒绝登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody User loginUser) {
        User user = userService.login(loginUser.getUsername(), loginUser.getPassword());
        if (user != null) {
            String token = JwtUtils.generateToken(user.getId(), user.getRole());
            user.setPassword(null); // 安全处理
            return Result.success(new LoginResponse(token, user));
        } else {
            return Result.error("登录失败，账号或密码错误或已被封禁");
        }
    }

    @Operation(summary = "获取当前用户资料", description = "根据 JWT 中的用户 ID 查询个人资料")
    @GetMapping("/profile")
    public Result<User> profile() {
        return Result.success(userService.getById(AuthContext.getUserId()));
    }

    @Operation(summary = "修改当前用户资料", description = "维护昵称、联系方式、个人简介等交换所需信息")
    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody User user) {
        boolean success = userService.updateProfile(AuthContext.getUserId(), user);
        if (!success) {
            throw new BusinessException("资料更新失败");
        }
        return Result.success("资料更新成功");
    }
}