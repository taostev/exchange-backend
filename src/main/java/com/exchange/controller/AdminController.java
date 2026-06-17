package com.exchange.controller;

import com.exchange.common.AuthContext;
import com.exchange.common.BusinessException;
import com.exchange.common.Result;
import com.exchange.entity.Category;
import com.exchange.entity.ExchangeOrder;
import com.exchange.entity.User;
import com.exchange.mapper.CategoryMapper;
import com.exchange.mapper.ExchangeOrderMapper;
import com.exchange.mapper.ItemMapper;
import com.exchange.mapper.UserMapper;
import com.exchange.service.ExchangeOrderService;
import com.exchange.service.ItemService;
import com.exchange.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "后台管理模块", description = "管理员维护分类、用户状态、物品状态和平台统计")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ExchangeOrderService orderService;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ExchangeOrderMapper orderMapper;

    private void requireAdmin() {
        if (!AuthContext.isAdmin()) {
            throw new BusinessException(403, "需要管理员权限");
        }
    }

    @Operation(summary = "管理员用户列表", description = "查看所有注册用户并用于风控管理")
    @GetMapping("/users")
    public Result<List<User>> users() {
        requireAdmin();
        return Result.success(userService.listUsers());
    }

    @Operation(summary = "冻结或恢复用户", description = "status=0 冻结，status=1 恢复")
    @PutMapping("/users/{userId}/status")
    public Result<String> updateUserStatus(@PathVariable Long userId, @RequestBody Map<String, Integer> body) {
        requireAdmin();
        userService.updateStatus(userId, body.get("status"));
        return Result.success("用户状态更新成功");
    }

    @Operation(summary = "管理员强制修改物品状态", description = "可用于违规物品强制下架")
    @PutMapping("/items/{itemId}/status")
    public Result<String> updateItemStatus(@PathVariable Long itemId, @RequestBody Map<String, Integer> body) {
        requireAdmin();
        itemService.updateStatus(AuthContext.getUserId(), true, itemId, body.get("status"));
        return Result.success("物品状态更新成功");
    }

    @Operation(summary = "分类列表", description = "查询物品分类字典")
    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        requireAdmin();
        return Result.success(categoryMapper.selectAll());
    }

    @Operation(summary = "新增分类", description = "管理员维护物品分类树")
    @PostMapping("/categories")
    public Result<Category> addCategory(@RequestBody Category category) {
        requireAdmin();
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        categoryMapper.insert(category);
        return Result.success(category);
    }

    @Operation(summary = "修改分类", description = "修改分类名称、父级和排序")
    @PutMapping("/categories/{categoryId}")
    public Result<String> updateCategory(@PathVariable Long categoryId, @RequestBody Category category) {
        requireAdmin();
        category.setCategoryId(categoryId);
        categoryMapper.update(category);
        return Result.success("分类更新成功");
    }

    @Operation(summary = "删除分类", description = "分类下存在物品时自动转移到「其他」分类")
    @DeleteMapping("/categories/{categoryId}")
    public Result<String> deleteCategory(@PathVariable Long categoryId) {
        requireAdmin();
        Category category = categoryMapper.selectAll().stream()
                .filter(item -> item.getCategoryId().equals(categoryId))
                .findFirst()
                .orElse(null);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if ("其他".equals(category.getName())) {
            throw new BusinessException("「其他」分类不能删除");
        }

        Category otherCategory = categoryMapper.findByName("其他");
        if (otherCategory == null) {
            throw new BusinessException("请先创建「其他」分类");
        }
        if (categoryMapper.countItems(categoryId) > 0) {
            categoryMapper.transferItems(categoryId, otherCategory.getCategoryId());
        }
        categoryMapper.delete(categoryId);
        return Result.success("分类删除成功");
    }

    @Operation(summary = "全局订单流水", description = "管理员查看所有交换订单")
    @GetMapping("/orders")
    public Result<List<ExchangeOrder>> orders() {
        requireAdmin();
        return Result.success(orderService.listAll());
    }

    @Operation(summary = "后台统计大盘", description = "统计注册人数、在架物品数和进行中订单数")
    @GetMapping("/stats")
    public Result<Map<String, Integer>> stats() {
        requireAdmin();
        return Result.success(Map.of(
                "userCount", userMapper.countUsers(),
                "availableItemCount", itemMapper.countAvailableItems(),
                "processingOrderCount", orderMapper.countProcessingOrders()
        ));
    }
}
