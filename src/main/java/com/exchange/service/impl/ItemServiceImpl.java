package com.exchange.service.impl;

import com.exchange.common.BusinessException;
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
    public boolean publishItem(Long userId, Item item) {
        // 发布物品必须绑定当前登录用户，防止前端伪造 userId。
        item.setUserId(userId);
        if (item.getTitle() == null || item.getTitle().isBlank()) {
            throw new BusinessException("物品标题不能为空");
        }
        if (item.getExchangeWish() == null || item.getExchangeWish().isBlank()) {
            throw new BusinessException("交换意向说明不能为空");
        }
        validateImages(item.getImages());
        item.setStatus(1);
        return itemMapper.insertItem(item) > 0;
    }

    private void validateImages(String images) {
        if (images == null || images.isBlank()) {
            return;
        }
        String[] parts = images.split(",");
        int count = 0;
        for (String part : parts) {
            if (!part.isBlank()) {
                count++;
            }
        }
        if (count > 5) {
            throw new BusinessException("物品图片最多上传 5 张");
        }
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

    @Override
    public Item getDetail(Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("物品不存在");
        }
        return item;
    }

    @Override
    public List<Item> listMyItems(Long userId, Integer status) {
        if (status == null) {
            return itemMapper.selectByUserId(userId);
        }
        return itemMapper.selectByUserIdAndStatus(userId, status);
    }

    @Override
    public boolean updateStatus(Long currentUserId, boolean admin, Long itemId, Integer status) {
        Item item = getDetail(itemId);
        // 普通会员只能操作自己发布的物品，管理员可以强制下架。
        if (!admin && !item.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "只能管理自己发布的物品");
        }
        if (status == null || status < 1 || status > 4) {
            throw new BusinessException("物品状态不合法");
        }
        return itemMapper.updateStatus(itemId, status) > 0;
    }
}