package com.exchange.service.impl;

import com.exchange.common.BusinessException;
import com.exchange.dto.OrderVO;
import com.exchange.entity.ExchangeOrder;
import com.exchange.entity.Item;
import com.exchange.entity.User;
import com.exchange.mapper.ExchangeOrderMapper;
import com.exchange.mapper.ItemMapper;
import com.exchange.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderViewAssembler {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ItemMapper itemMapper;

    public List<OrderVO> toViewList(List<ExchangeOrder> orders) {
        List<OrderVO> views = new ArrayList<>();
        for (ExchangeOrder order : orders) {
            views.add(toView(order));
        }
        return views;
    }

    public OrderVO toView(ExchangeOrder order) {
        OrderVO vo = new OrderVO();
        vo.setOrderId(order.getOrderId());
        vo.setInitiatorId(order.getInitiatorId());
        vo.setTargetId(order.getTargetId());
        vo.setOfferItemId(order.getOfferItemId());
        vo.setTargetItemId(order.getTargetItemId());
        vo.setRemark(order.getRemark());
        vo.setStatus(order.getStatus());
        vo.setInitiatorConfirmed(order.getInitiatorConfirmed());
        vo.setTargetConfirmed(order.getTargetConfirmed());
        vo.setCreateTime(order.getCreateTime());
        vo.setFinishTime(order.getFinishTime());

        User initiator = userMapper.findById(order.getInitiatorId());
        User target = userMapper.findById(order.getTargetId());
        if (initiator != null) {
            vo.setInitiatorNickname(initiator.getNickname() == null ? initiator.getUsername() : initiator.getNickname());
        }
        if (target != null) {
            vo.setTargetNickname(target.getNickname() == null ? target.getUsername() : target.getNickname());
        }

        Item offerItem = itemMapper.selectById(order.getOfferItemId());
        Item targetItem = itemMapper.selectById(order.getTargetItemId());
        if (offerItem != null) {
            vo.setOfferItemTitle(offerItem.getTitle());
            vo.setOfferItemImage(firstImage(offerItem.getImages()));
        }
        if (targetItem != null) {
            vo.setTargetItemTitle(targetItem.getTitle());
            vo.setTargetItemImage(firstImage(targetItem.getImages()));
        }
        return vo;
    }

    private String firstImage(String images) {
        if (images == null || images.isBlank()) {
            return null;
        }
        return images.split(",")[0].trim();
    }
}
