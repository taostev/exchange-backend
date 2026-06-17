package com.exchange.controller;

import com.exchange.common.AuthContext;
import com.exchange.common.AuthTokenSupport;
import com.exchange.common.Result;
import com.exchange.dto.CreateOrderRequest;
import com.exchange.dto.OrderStatusRequest;
import com.exchange.dto.OrderVO;
import com.exchange.service.ExchangeOrderService;
import com.exchange.service.impl.OrderViewAssembler;
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

    @Autowired
    private OrderViewAssembler orderViewAssembler;

    @Operation(summary = "发起交换意向", description = "选择自己的物品去交换对方的目标物品")
    @PostMapping("/create")
    public Result<Map<String, Long>> create(@RequestBody CreateOrderRequest request) {
        Long orderId = orderService.create(AuthContext.getUserId(), request);
        return Result.success(Map.of("orderId", orderId));
    }

    @Operation(summary = "订单状态流转", description = "支持 ACCEPT、REJECT、CANCEL、FINISH")
    @PutMapping("/{orderId}/status")
    public Result<String> updateStatus(@PathVariable Long orderId, @RequestBody OrderStatusRequest request) {
        String message = orderService.updateStatus(AuthContext.getUserId(), orderId, request.getAction());
        return Result.success(message);
    }

    @Operation(summary = "我的交换订单", description = "查询当前用户发起或收到的交换订单")
    @GetMapping("/mine")
    public Result<List<OrderVO>> mine() {
        return Result.success(orderViewAssembler.toViewList(orderService.listMine(AuthContext.getUserId())));
    }
}
