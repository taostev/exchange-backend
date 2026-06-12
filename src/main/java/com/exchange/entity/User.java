package com.exchange.entity;

import java.time.LocalDateTime;

public class User {
    private Long id;              // 用户唯一主键
    private String username;      // 登录账号
    private String password;      // 登录密码
    private String nickname;      // 前台展示昵称
    private String contactInfo;   // 线下联系方式
    private String profile;       // 个人简介，便于线下交换前了解用户
    private Integer role;         // 角色权限：0-普通会员，1-系统管理员
    private Integer status;       // 账号状态：1-正常，0-限制登录
    private LocalDateTime createTime; // 记录创建时间

    // 无参构造方法
    public User() {}

    // 标准的 Getters 和 Setters（如果你安装了 Lombok，可以直接在类上加 @Data 注解代替下面所有代码）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }

    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}