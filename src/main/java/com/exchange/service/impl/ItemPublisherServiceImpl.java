package com.exchange.service.impl;

import com.exchange.common.BusinessException;
import com.exchange.dto.PublisherVO;
import com.exchange.entity.Item;
import com.exchange.entity.User;
import com.exchange.mapper.ExchangeOrderMapper;
import com.exchange.mapper.ItemMapper;
import com.exchange.mapper.UserMapper;
import com.exchange.service.ItemPublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemPublisherServiceImpl implements ItemPublisherService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ExchangeOrderMapper orderMapper;

    @Override
    public PublisherVO getPublisher(Long itemId, Long viewerUserId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("物品不存在");
        }
        User publisher = userMapper.findById(item.getUserId());
        if (publisher == null) {
            throw new BusinessException("发布者不存在");
        }

        PublisherVO vo = new PublisherVO();
        vo.setUserId(publisher.getId());
        vo.setNickname(publisher.getNickname() == null ? publisher.getUsername() : publisher.getNickname());
        vo.setProfile(publisher.getProfile());

        boolean contactVisible = viewerUserId != null
                && orderMapper.countAgreedOrderBetween(viewerUserId, publisher.getId()) > 0;
        vo.setContactVisible(contactVisible);
        if (contactVisible) {
            vo.setContactInfo(publisher.getContactInfo());
        }
        return vo;
    }
}
