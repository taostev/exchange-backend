package com.exchange.service.impl;

import com.exchange.config.RecommendProperties;
import com.exchange.dto.LlmRecommendResponse;
import com.exchange.entity.Item;
import com.exchange.mapper.FavoriteMapper;
import com.exchange.mapper.ItemMapper;
import com.exchange.service.ItemService;
import com.exchange.service.RecommendService;
import com.exchange.service.llm.LlmRecommendClient;
import com.exchange.service.llm.RecommendContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {

    private static final Logger log = LoggerFactory.getLogger(RecommendServiceImpl.class);

    @Autowired
    private RecommendProperties recommendProperties;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private LlmRecommendClient llmRecommendClient;

    @Autowired
    private RecommendContextBuilder recommendContextBuilder;

    @Override
    public Map<String, Object> recommend(Long userId) {
        if (userId == null) {
            Map<String, Object> hot = itemService.getItemPage(1, recommendProperties.getResultSize(), null, null);
            hot.put("source", "hot");
            hot.put("reason", "游客模式：展示最新在架热门物品");
            return hot;
        }

        if (recommendProperties.isConfigured()) {
            Map<String, Object> llmResult = tryLlmRecommend(userId);
            if (llmResult != null) {
                return llmResult;
            }
        }

        Map<String, Object> fallback = buildRuleRecommend(userId);
        fallback.put("source", "fallback");
        fallback.put("reason", recommendProperties.isConfigured()
                ? "大模型超时或不可用，已降级为规则推荐"
                : "未配置 LLM API Key，使用规则推荐");
        return fallback;
    }

    private Map<String, Object> tryLlmRecommend(Long userId) {
        try {
            List<Item> candidates = itemMapper.selectRecommendPool(userId, recommendProperties.getCandidatePoolSize());
            if (candidates.isEmpty()) {
                return null;
            }

            String profileJson = recommendContextBuilder.buildUserProfileJson(userId);
            String candidatesJson = recommendContextBuilder.buildCandidatesJson(candidates);
            LlmRecommendResponse llmResponse = llmRecommendClient.recommend(
                    profileJson,
                    candidatesJson,
                    recommendProperties.getResultSize()
            );
            if (llmResponse == null || llmResponse.getItemIds() == null || llmResponse.getItemIds().isEmpty()) {
                return null;
            }

            Set<Long> allowedIds = candidates.stream()
                    .map(Item::getItemId)
                    .collect(Collectors.toSet());
            List<Long> pickedIds = llmResponse.getItemIds().stream()
                    .filter(allowedIds::contains)
                    .limit(recommendProperties.getResultSize())
                    .toList();
            if (pickedIds.isEmpty()) {
                return null;
            }

            List<Item> records = itemMapper.selectAvailableByIds(pickedIds);
            records = sortByIdOrder(records, pickedIds);
            if (records.size() < recommendProperties.getResultSize()) {
                records = fillToSize(records, candidates, userId);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", records.size());
            result.put("current", 1);
            result.put("records", records);
            result.put("source", "llm");
            result.put("reason", llmResponse.getReason() == null || llmResponse.getReason().isBlank()
                    ? "根据您的收藏与发布偏好，由大模型智能匹配"
                    : llmResponse.getReason());
            return result;
        } catch (RestClientException e) {
            log.warn("LLM 推荐调用失败，将降级: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("LLM 推荐处理异常，将降级: {}", e.getMessage());
            return null;
        }
    }

    private List<Item> sortByIdOrder(List<Item> records, List<Long> idOrder) {
        Map<Long, Item> map = records.stream().collect(Collectors.toMap(Item::getItemId, item -> item, (a, b) -> a));
        List<Item> sorted = new ArrayList<>();
        for (Long id : idOrder) {
            if (map.containsKey(id)) {
                sorted.add(map.get(id));
            }
        }
        return sorted;
    }

    private List<Item> fillToSize(List<Item> records, List<Item> candidates, Long userId) {
        Set<Long> exists = records.stream().map(Item::getItemId).collect(Collectors.toCollection(HashSet::new));
        List<Item> filled = new ArrayList<>(records);
        for (Item candidate : candidates) {
            if (filled.size() >= recommendProperties.getResultSize()) {
                break;
            }
            if (!exists.contains(candidate.getItemId())) {
                filled.add(candidate);
                exists.add(candidate.getItemId());
            }
        }
        if (filled.size() < recommendProperties.getResultSize()) {
            for (Item candidate : itemMapper.selectRecommendFallback(userId, recommendProperties.getResultSize())) {
                if (filled.size() >= recommendProperties.getResultSize()) {
                    break;
                }
                if (!exists.contains(candidate.getItemId())) {
                    filled.add(candidate);
                    exists.add(candidate.getItemId());
                }
            }
        }
        return filled;
    }

    private Map<String, Object> buildRuleRecommend(Long userId) {
        int size = recommendProperties.getResultSize();
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
            if (records.size() >= size) {
                break;
            }
            List<Item> candidates = itemMapper.selectRecommendByCategory(categoryId, userId, size - records.size());
            for (Item candidate : candidates) {
                if (records.stream().noneMatch(item -> item.getItemId().equals(candidate.getItemId()))) {
                    records.add(candidate);
                }
            }
        }

        if (records.size() < size) {
            for (Item candidate : itemMapper.selectRecommendFallback(userId, size)) {
                if (records.size() >= size) {
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
