package com.exchange.service;

import com.exchange.entity.Item;
import java.util.Map;

public interface ItemService {
    // 发布二手物品
    boolean publishItem(Item item);

    // 分页检索二手物品，返回包含总数和当前页数据的 Map 结构
    Map<String, Object> getItemPage(int page, int size, Integer categoryId, String keyword);
}