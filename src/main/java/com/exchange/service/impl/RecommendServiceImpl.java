package com.exchange.service.impl;

import com.exchange.entity.Item;
import com.exchange.mapper.FavoriteMapper;
import com.exchange.mapper.ItemMapper;
import com.exchange.service.ItemService;
import com.exchange.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RecommendServiceImpl implements RecommendService {

    private static final int RECOMMEND_SIZE = 3;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Override
    public Map<String, Object> recommend(Long userId) {
        if (userId == null) {
            return itemService.getItemPage(1, RECOMMEND_SIZE, null, null);
        }

        Set<Integer> preferredCategories = new LinkedHashSet<>();
        for (Item favoriteItem : favoriteMapper.selectFavoriteItems(userId)) {
            if (favoriteItem.getCategoryId() != null) {
                preferredCategories.add(favoriteItem.getCategoryId());
            }
        }
        for (Item myItem : itemMapper.selectByUserId(userId)) {
            if (myItem.getCategoryId() != null) {
                preferredCategories.add(myItem.getCategoryId());
            }
        }

        List<Item> records = new ArrayList<>();
        for (Integer categoryId : preferredCategories) {
            if (records.size() >= RECOMMEND_SIZE) {
                break;
            }
            List<Item> candidates = itemMapper.selectRecommendByCategory(categoryId, userId, RECOMMEND_SIZE - records.size());
            for (Item candidate : candidates) {
                if (records.stream().noneMatch(item -> item.getItemId().equals(candidate.getItemId()))) {
                    records.add(candidate);
                }
            }
        }

        if (records.size() < RECOMMEND_SIZE) {
            List<Item> fallback = itemMapper.selectRecommendFallback(userId, RECOMMEND_SIZE);
            for (Item candidate : fallback) {
                if (records.size() >= RECOMMEND_SIZE) {
                    break;
                }
                if (records.stream().noneMatch(item -> item.getItemId().equals(candidate.getItemId()))) {
                    records.add(candidate);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", records.size());
        result.put("current", 1);
        result.put("records", records);
        return result;
    }
}
