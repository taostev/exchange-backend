package com.exchange.config;

import com.exchange.common.PasswordUtils;
import com.exchange.entity.Category;
import com.exchange.entity.User;
import com.exchange.mapper.CategoryMapper;
import com.exchange.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 首次启动时写入默认分类和管理员账号，保证本地一键初始化后可联调。
 */
@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) {
        seedCategories();
        seedAdminUser();
    }

    private void seedCategories() {
        if (!categoryMapper.selectAll().isEmpty()) {
            return;
        }
        insertCategory(0L, "数码", 1);
        insertCategory(0L, "图书教材", 2);
        insertCategory(0L, "生活用品", 3);
        insertCategory(0L, "其他", 99);
    }

    private void insertCategory(Long parentId, String name, int sort) {
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(name);
        category.setSort(sort);
        categoryMapper.insert(category);
    }

    private void seedAdminUser() {
        if (userMapper.findByUsername("admin") != null) {
            return;
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(PasswordUtils.encode("admin123"));
        admin.setNickname("系统管理员");
        admin.setContactInfo("admin@exchange.local");
        admin.setRole(1);
        admin.setStatus(1);
        admin.setCreateTime(LocalDateTime.now());
        userMapper.insertUser(admin);
    }
}
