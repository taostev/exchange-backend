package com.exchange.controller;

import com.exchange.common.Result;
import com.exchange.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "推荐模块", description = "AI 推荐未接入时，降级返回最新在架物品")
@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Autowired
    private ItemService itemService;

    @Operation(summary = "推荐物品", description = "当前版本先返回最新在架物品，后续可接入大模型推荐")
    @GetMapping
    public Result<Map<String, Object>> recommend() {
        return Result.success(itemService.getItemPage(1, 3, null, null));
    }
}
