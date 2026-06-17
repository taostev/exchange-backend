package com.exchange.service.impl;

import com.exchange.common.BusinessException;
import com.exchange.dto.CreateOrderRequest;
import com.exchange.entity.ExchangeOrder;
import com.exchange.entity.Item;
import com.exchange.entity.User;
import com.exchange.mapper.ExchangeOrderMapper;
import com.exchange.mapper.ItemMapper;
import com.exchange.mapper.UserMapper;
import com.exchange.service.ExchangeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExchangeOrderServiceImpl implements ExchangeOrderService {

    @Autowired
    private ExchangeOrderMapper orderMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Long create(Long userId, CreateOrderRequest request) {
        User user = userMapper.findById(userId);
        if (user == null || user.getContactInfo() == null || user.getContactInfo().isBlank()) {
            throw new BusinessException("请先完善联系方式后再发起交换");
        }

        Item offerItem = itemMapper.selectById(request.getOfferItemId());
        Item targetItem = itemMapper.selectById(request.getTargetItemId());
        if (offerItem == null || targetItem == null) {
            throw new BusinessException("交换物品不存在");
        }
        if (!offerItem.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能使用自己发布的物品发起交换");
        }
        if (targetItem.getUserId().equals(userId)) {
            throw new BusinessException("不能和自己的物品发起交换");
        }
        if (offerItem.getStatus() != 1 || targetItem.getStatus() != 1) {
            throw new BusinessException("双方物品必须均为在架状态");
        }

        ExchangeOrder order = new ExchangeOrder();
        order.setInitiatorId(userId);
        order.setTargetId(targetItem.getUserId());
        order.setOfferItemId(offerItem.getItemId());
        order.setTargetItemId(targetItem.getItemId());
        order.setRemark(request.getRemark());
        order.setStatus(0);
        order.setInitiatorConfirmed(false);
        order.setTargetConfirmed(false);
        orderMapper.insert(order);
        return order.getOrderId();
    }

    @Override
    @Transactional
    public String updateStatus(Long userId, Long orderId, String action) {
        ExchangeOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (action == null) {
            throw new BusinessException("订单操作不能为空");
        }

        String normalized = action.toUpperCase();
        switch (normalized) {
            case "ACCEPT":
                accept(userId, order);
                return "已同意交换";
            case "REJECT":
                reject(userId, order);
                return "已拒绝交换";
            case "CANCEL":
                cancel(userId, order);
                return "订单已取消";
            case "FINISH":
                return finish(userId, order);
            default:
                throw new BusinessException("不支持的订单操作");
        }
    }

    private void accept(Long userId, ExchangeOrder order) {
        if (!order.getTargetId().equals(userId)) {
            throw new BusinessException(403, "只有接收方可以同意交换");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只有待确认订单可以同意");
        }
        if (itemMapper.lockAvailableItem(order.getOfferItemId()) == 0 ||
                itemMapper.lockAvailableItem(order.getTargetItemId()) == 0) {
            throw new BusinessException("物品已被他人预订");
        }
        order.setStatus(1);
        order.setInitiatorConfirmed(false);
        order.setTargetConfirmed(false);
        order.setFinishTime(null);
        orderMapper.updateStatus(order);
    }

    private void reject(Long userId, ExchangeOrder order) {
        if (!order.getTargetId().equals(userId)) {
            throw new BusinessException(403, "只有接收方可以拒绝交换");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只有待确认订单可以拒绝");
        }
        order.setStatus(2);
        order.setFinishTime(LocalDateTime.now());
        orderMapper.updateStatus(order);
    }

    private void cancel(Long userId, ExchangeOrder order) {
        if (!order.getInitiatorId().equals(userId) && !order.getTargetId().equals(userId)) {
            throw new BusinessException(403, "只能取消与自己相关的订单");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("只有交换中订单可以取消");
        }
        itemMapper.updateTwoItemsStatus(order.getOfferItemId(), order.getTargetItemId(), 1);
        order.setStatus(4);
        order.setInitiatorConfirmed(false);
        order.setTargetConfirmed(false);
        order.setFinishTime(LocalDateTime.now());
        orderMapper.updateStatus(order);
    }

    private String finish(Long userId, ExchangeOrder order) {
        if (!order.getInitiatorId().equals(userId) && !order.getTargetId().equals(userId)) {
            throw new BusinessException(403, "只能完成与自己相关的订单");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("只有交换中订单可以完成");
        }

        if (order.getInitiatorId().equals(userId)) {
            if (Boolean.TRUE.equals(order.getInitiatorConfirmed())) {
                return "您已确认完成，等待对方确认";
            }
            order.setInitiatorConfirmed(true);
        } else {
            if (Boolean.TRUE.equals(order.getTargetConfirmed())) {
                return "您已确认完成，等待对方确认";
            }
            order.setTargetConfirmed(true);
        }

        if (Boolean.TRUE.equals(order.getInitiatorConfirmed()) && Boolean.TRUE.equals(order.getTargetConfirmed())) {
            itemMapper.updateTwoItemsStatus(order.getOfferItemId(), order.getTargetItemId(), 3);
            order.setStatus(3);
            order.setFinishTime(LocalDateTime.now());
            orderMapper.updateStatus(order);
            return "交换已完成";
        }

        orderMapper.updateConfirmFlags(order);
        return "已确认完成，等待对方确认";
    }

    @Override
    public List<ExchangeOrder> listMine(Long userId) {
        return orderMapper.selectByUserId(userId);
    }

    @Override
    public List<ExchangeOrder> listAll() {
        return orderMapper.selectAll();
    }
}
