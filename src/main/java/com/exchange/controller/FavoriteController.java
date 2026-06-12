package com.exchange.controller;

import com.exchange.common.AuthContext;
import com.exchange.common.Result;
import com.exchange.entity.Item;
import com.exchange.mapper.FavoriteMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "关注收藏模块", description = "负责物品关注、取消关注和我的关注列表")
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Operation(summary = "关注物品", description = "会员将感兴趣的物品加入关注清单")
    @PostMapping("/{itemId}")
    public Result<String> add(@PathVariable Long itemId) {
        favoriteMapper.insert(AuthContext.getUserId(), itemId);
        return Result.success("关注成功");
    }

    @Operation(summary = "取消关注", description = "从当前用户关注清单中移除物品")
    @DeleteMapping("/{itemId}")
    public Result<String> remove(@PathVariable Long itemId) {
        favoriteMapper.delete(AuthContext.getUserId(), itemId);
        return Result.success("取消关注成功");
    }

    @Operation(summary = "我的关注列表", description = "查询当前用户关注的物品及其实时状态")
    @GetMapping
    public Result<List<Item>> list() {
        return Result.success(favoriteMapper.selectFavoriteItems(AuthContext.getUserId()));
    }
}
