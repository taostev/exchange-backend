package com.exchange.service.impl;

import com.exchange.entity.Item;
import com.exchange.mapper.ItemMapper;
import com.exchange.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Override
    public boolean publishItem(Item item) {
        item.setStatus(1); // 详细设计书规定：发布后默认状态为 1-在架
        return itemMapper.insertItem(item) > 0;
    }

    @Override
    public Map<String, Object> getItemPage(int page, int size, Integer categoryId, String keyword) {
        // 计算 MySQL 的 LIMIT 偏移量
        int offset = (page - 1) * size;

        // 1. 查询当前页的记录列表
        List<Item> records = itemMapper.selectItemPage(offset, size, categoryId, keyword);
        // 2. 查询符合条件的总条数
        int total = itemMapper.selectItemCount(categoryId, keyword);

        // 3. 严格按照你详细设计书 3.1 节要求的格式进行数据打包
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", total);
        resultMap.put("current", page);
        resultMap.put("records", records);

        return resultMap;
    }
}