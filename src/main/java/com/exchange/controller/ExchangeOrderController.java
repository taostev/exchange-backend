package com.exchange.controller;

import com.exchange.common.AuthContext;
import com.exchange.common.Result;
import com.exchange.dto.CreateOrderRequest;
import com.exchange.dto.OrderStatusRequest;
import com.exchange.entity.ExchangeOrder;
import com.exchange.service.ExchangeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "交换撮合模块", description = "负责发起交换意向和订单状态流转")
@RestController
@RequestMapping("/api/orders")
public class ExchangeOrderController {

    @Autowired
    private ExchangeOrderService orderService;

    @Operation(summary = "发起交换意向", description = "选择自己的物品去交换对方的目标物品")
    @PostMapping("/create")
    public Result<Map<String, Long>> create(@RequestBody CreateOrderRequest request) {
        Long orderId = orderService.create(AuthContext.getUserId(), request);
        return Result.success(Map.of("orderId", orderId));
    }

    @Operation(summary = "订单状态流转", description = "支持 ACCEPT、REJECT、CANCEL、FINISH")
    @PutMapping("/{orderId}/status")
    public Result<String> updateStatus(@PathVariable Long orderId, @RequestBody OrderStatusRequest request) {
        boolean success = orderService.updateStatus(AuthContext.getUserId(), orderId, request.getAction());
        return success ? Result.success("订单状态更新成功") : Result.error("订单状态更新失败");
    }

    @Operation(summary = "我的交换订单", description = "查询当前用户发起或收到的交换订单")
    @GetMapping("/mine")
    public Result<List<ExchangeOrder>> mine() {
        return Result.success(orderService.listMine(AuthContext.getUserId()));
    }
}
