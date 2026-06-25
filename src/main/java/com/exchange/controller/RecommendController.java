package com.exchange.controller;

import com.exchange.common.AuthContext;
import com.exchange.common.AuthTokenSupport;
import com.exchange.common.Result;
import com.exchange.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "推荐模块", description = "LLM 智能推荐，超时或未配置时降级为规则/热门推荐")
@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @Operation(summary = "推荐物品", description = "登录用户优先走 LLM 智能推荐；游客返回热门；失败时降级")
    @GetMapping
    public Result<Map<String, Object>> recommend(HttpServletRequest request) {
        try {
            AuthTokenSupport.bindOptionalUser(request);
            return Result.success(recommendService.recommend(AuthContext.getUserId()));
        } finally {
            AuthContext.clear();
        }
    }
}
