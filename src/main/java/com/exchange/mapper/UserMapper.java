package com.exchange.mapper;

import com.exchange.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper // 必须加这个注解，Spring Boot 才能扫描到它
public interface UserMapper {

    // 1. 根据用户名查询用户（详细设计说明书 5.1.1 登录逻辑需要）
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(String username);

    // 根据用户 ID 查询资料，供鉴权后获取当前用户信息使用。
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User findById(Long id);

    // 2. 插入新用户（详细设计说明书 5.1.2 注册逻辑需要）
    @Insert("INSERT INTO sys_user(username, password, nickname, contact_info, profile, role, status, create_time) " +
            "VALUES(#{username}, #{password}, #{nickname}, #{contactInfo}, #{profile}, #{role}, #{status}, #{createTime})")
    int insertUser(User user);

    // 会员维护自己的昵称、联系方式和简介。
    @Update("UPDATE sys_user SET nickname = #{nickname}, contact_info = #{contactInfo}, profile = #{profile} WHERE id = #{id}")
    int updateProfile(User user);

    // 管理员冻结/解冻账号，登录时会根据 status 判断是否允许进入。
    @Update("UPDATE sys_user SET status = #{status} WHERE id = #{userId}")
    int updateStatus(@Param("userId") Long userId, @Param("status") Integer status);

    // 管理员后台用户列表。
    @Select("SELECT * FROM sys_user ORDER BY create_time DESC")
    List<User> selectAll();

    // 后台统计总用户数。
    @Select("SELECT COUNT(*) FROM sys_user")
    int countUsers();
}