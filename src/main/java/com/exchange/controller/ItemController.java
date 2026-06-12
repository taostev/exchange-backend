package com.exchange.controller;

import com.exchange.common.AuthContext;
import com.exchange.common.BusinessException;
import com.exchange.common.Result;
import com.exchange.entity.Item;
import com.exchange.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;
import java.util.UUID;

@Tag(name = "物品流转模块", description = "负责物品发布、检索、详情、上下架和图片上传")
@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Operation(summary = "发布二手物品", description = "会员填写标题、描述、分类和交换意向后发布物品")
    @PostMapping
    public Result<String> publish(@RequestBody Item item) {
        boolean success = itemService.publishItem(AuthContext.getUserId(), item);
        return success ? Result.success("发布成功") : Result.error("发布失败");
    }

    @Operation(summary = "分页检索物品", description = "首页和分类页使用，支持分类、关键词和分页")
    @GetMapping("/page")
    public Result<Map<String, Object>> page(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) Integer categoryId,
                                            @RequestParam(required = false) String keyword) {
        return Result.success(itemService.getItemPage(page, size, categoryId, keyword));
    }

    @Operation(summary = "物品详情", description = "根据物品 ID 查询详情页数据")
    @GetMapping("/{itemId}")
    public Result<Item> detail(@PathVariable Long itemId) {
        return Result.success(itemService.getDetail(itemId));
    }

    @Operation(summary = "我的物品列表", description = "查询当前登录会员发布的所有物品")
    @GetMapping("/mine")
    public Result<?> mine() {
        return Result.success(itemService.listMyItems(AuthContext.getUserId()));
    }

    @Operation(summary = "修改物品状态", description = "发布者可上下架自己的物品，管理员可强制下架")
    @PutMapping("/{itemId}/status")
    public Result<String> updateStatus(@PathVariable Long itemId, @RequestBody Map<String, Integer> body) {
        boolean success = itemService.updateStatus(AuthContext.getUserId(), AuthContext.isAdmin(), itemId, body.get("status"));
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }

    @Operation(summary = "上传物品图片", description = "保存 JPG/PNG 图片并返回可访问路径")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException("单张图片不能超过 2MB");
        }
        String originalName = file.getOriginalFilename();
        String suffix = originalName == null ? "" : originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        if (!suffix.equals("jpg") && !suffix.equals("jpeg") && !suffix.equals("png")) {
            throw new BusinessException("仅支持 JPG/PNG 图片");
        }
        try {
            File uploadDir = new File("uploads/item");
            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                throw new BusinessException("上传目录创建失败");
            }
            String filename = UUID.randomUUID() + "." + suffix;
            file.transferTo(new File(uploadDir, filename));
            return Result.success("/uploads/item/" + filename);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("图片上传失败");
        }
    }
}
