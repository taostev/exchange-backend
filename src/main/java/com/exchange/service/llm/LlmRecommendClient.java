package com.exchange.service.llm;

import com.exchange.config.RecommendProperties;
import com.exchange.dto.LlmRecommendResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 调用 Gemini 或 OpenAI 兼容接口，根据用户画像从候选池中挑选物品 ID。
 */
@Component
public class LlmRecommendClient {

    private static final Logger log = LoggerFactory.getLogger(LlmRecommendClient.class);
    private static final Pattern JSON_BLOCK = Pattern.compile("\\{[\\s\\S]*}");

    private final RecommendProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public LlmRecommendClient(RecommendProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.getTimeoutMs());
        factory.setReadTimeout((int) properties.getTimeoutMs());
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    public LlmRecommendResponse recommend(String userProfileJson, String candidatesJson, int resultSize) {
        String prompt = buildPrompt(userProfileJson, candidatesJson, resultSize);
        String responseBody = properties.isGemini()
                ? callGemini(prompt)
                : callOpenAiCompatible(prompt);
        return parseResponse(responseBody);
    }

    private String buildPrompt(String userProfileJson, String candidatesJson, int resultSize) {
        return """
                你是校园二手物品交换平台的智能推荐助手。
                请根据用户兴趣画像，从候选物品中挑选最匹配的物品。
                只能返回候选列表中存在的 itemId，数量恰好为 %d 个，按推荐优先级排序。
                只输出 JSON，不要 markdown，格式：{"itemIds":[1,2,3],"reason":"一句话说明推荐理由"}

                【用户兴趣画像】
                %s

                【候选物品列表】
                %s
                """.formatted(resultSize, userProfileJson, candidatesJson);
    }

    private String callGemini(String prompt) {
        String baseUrl = normalizeBaseUrl(properties.getBaseUrl(), "https://generativelanguage.googleapis.com");
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/v1beta/models/" + properties.getModel() + ":generateContent")
                .queryParam("key", properties.getApiKey())
                .toUriString();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        body.put("generationConfig", Map.of(
                "temperature", 0.2,
                "maxOutputTokens", 256
        ));

        return restClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(String.class);
    }

    private String callOpenAiCompatible(String prompt) {
        String baseUrl = normalizeBaseUrl(properties.getBaseUrl(), "https://api.deepseek.com");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getModel());
        body.put("temperature", 0.2);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        return restClient.post()
                .uri(baseUrl + "/v1/chat/completions")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(String.class);
    }

    LlmRecommendResponse parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = extractAssistantContent(root);
            String jsonText = extractJson(content);
            LlmRecommendResponse parsed = objectMapper.readValue(jsonText, LlmRecommendResponse.class);
            if (parsed.getItemIds() == null) {
                parsed.setItemIds(new ArrayList<>());
            }
            return parsed;
        } catch (Exception e) {
            log.warn("LLM 响应解析失败: {}", e.getMessage());
            return null;
        }
    }

    private String extractAssistantContent(JsonNode root) {
        if (properties.isGemini()) {
            return root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
        }
        return root.path("choices").path(0).path("message").path("content").asText("");
    }

    private String extractJson(String content) {
        Matcher matcher = JSON_BLOCK.matcher(content.trim());
        if (matcher.find()) {
            return matcher.group();
        }
        return content.trim();
    }

    private String normalizeBaseUrl(String baseUrl, String defaultUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return defaultUrl;
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
