package com.exchange.service;

import com.exchange.dto.CreateOrderRequest;
import com.exchange.entity.ExchangeOrder;

import java.util.List;

public interface ExchangeOrderService {
    // 发起交换意向
    Long create(Long userId, CreateOrderRequest request);

    // 处理订单状态流转，返回给前端展示的提示语
    String updateStatus(Long userId, Long orderId, String action);

    // 当前用户相关订单
    List<ExchangeOrder> listMine(Long userId);

    // 管理员全局订单列表
    List<ExchangeOrder> listAll();
}
