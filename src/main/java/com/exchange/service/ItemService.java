package com.exchange.service;

import com.exchange.entity.Item;
import java.util.Map;
import java.util.List;

public interface ItemService {
    // 发布二手物品
    boolean publishItem(Long userId, Item item);

    // 分页检索二手物品，返回包含总数和当前页数据的 Map 结构
    Map<String, Object> getItemPage(int page, int size, Integer categoryId, String keyword);

    // 查询物品详情
    Item getDetail(Long itemId);

    // 查询当前用户发布的物品
    List<Item> listMyItems(Long userId);

    // 发布者或管理员修改物品状态
    boolean updateStatus(Long currentUserId, boolean admin, Long itemId, Integer status);
}