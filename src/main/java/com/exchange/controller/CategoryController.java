package com.exchange.controller;

import com.exchange.common.Result;
import com.exchange.entity.Category;
import com.exchange.mapper.CategoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "分类模块", description = "前台分类导航，游客和会员均可访问")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryMapper categoryMapper;

    @Operation(summary = "分类列表", description = "返回全站物品分类树，供首页和筛选页使用")
    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryMapper.selectAll());
    }
}
