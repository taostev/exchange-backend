package com.exchange.mapper;

import com.exchange.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper // 必须加这个注解，Spring Boot 才能扫描到它
public interface UserMapper {

    // 1. 根据用户名查询用户（详细设计说明书 5.1.1 登录逻辑需要）
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(String username);

    // 2. 插入新用户（详细设计说明书 5.1.2 注册逻辑需要）
    @Insert("INSERT INTO sys_user(username, password, nickname, contact_info, role, status, create_time) " +
            "VALUES(#{username}, #{password}, #{nickname}, #{contactInfo}, #{role}, #{status}, #{createTime})")
    int insertUser(User user);
}