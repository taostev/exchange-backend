package com.exchange.service.llm;

import com.exchange.entity.Category;
import com.exchange.entity.Item;
import com.exchange.entity.User;
import com.exchange.mapper.CategoryMapper;
import com.exchange.mapper.FavoriteMapper;
import com.exchange.mapper.ItemMapper;
import com.exchange.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 组装用户画像与候选物品，供 LLM Prompt 使用 */
@Component
public class RecommendContextBuilder {

    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;
    private final ItemMapper itemMapper;
    private final CategoryMapper categoryMapper;
    private final ObjectMapper objectMapper;

    public RecommendContextBuilder(UserMapper userMapper,
                                   FavoriteMapper favoriteMapper,
                                   ItemMapper itemMapper,
                                   CategoryMapper categoryMapper,
                                   ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.favoriteMapper = favoriteMapper;
        this.itemMapper = itemMapper;
        this.categoryMapper = categoryMapper;
        this.objectMapper = objectMapper;
    }

    public String buildUserProfileJson(Long userId) throws JsonProcessingException {
        User user = userMapper.findById(userId);
        Map<Long, String> categoryNames = categoryMapper.selectAll().stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName, (a, b) -> a));

        List<String> favoriteTitles = new ArrayList<>();
        List<String> favoriteCategories = new ArrayList<>();
        for (Item item : favoriteMapper.selectFavoriteItems(userId)) {
            favoriteTitles.add(item.getTitle());
            if (item.getCategoryId() != null) {
                favoriteCategories.add(categoryNames.getOrDefault(item.getCategoryId().longValue(), "未知分类"));
            }
        }

        List<String> publishedTitles = new ArrayList<>();
        List<String> publishedWishes = new ArrayList<>();
        for (Item item : itemMapper.selectByUserId(userId)) {
            publishedTitles.add(item.getTitle());
            if (item.getExchangeWish() != null && !item.getExchangeWish().isBlank()) {
                publishedWishes.add(item.getExchangeWish());
            }
        }

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("nickname", user == null ? "会员" : user.getNickname());
        profile.put("profile", user == null ? null : user.getProfile());
        profile.put("favoriteItemTitles", favoriteTitles);
        profile.put("favoriteCategories", favoriteCategories.stream().distinct().toList());
        profile.put("publishedItemTitles", publishedTitles);
        profile.put("exchangeWishes", publishedWishes.stream().distinct().limit(5).toList());
        return objectMapper.writeValueAsString(profile);
    }

    public String buildCandidatesJson(List<Item> candidates) throws JsonProcessingException {
        Map<Long, String> categoryNames = categoryMapper.selectAll().stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getName, (a, b) -> a));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Item item : candidates) {
            Map<String, Object> row = new HashMap<>();
            row.put("itemId", item.getItemId());
            row.put("title", item.getTitle());
            row.put("category", item.getCategoryId() == null
                    ? "未分类"
                    : categoryNames.getOrDefault(item.getCategoryId().longValue(), "未分类"));
            row.put("exchangeWish", item.getExchangeWish());
            row.put("description", truncate(item.getDescription(), 80));
            rows.add(row);
        }
        return objectMapper.writeValueAsString(rows);
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
