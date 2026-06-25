package com.exchange.service.llm;

import com.exchange.config.RecommendProperties;
import com.exchange.dto.LlmRecommendResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LlmRecommendClientTest {

    @Test
    void parseResponse_shouldExtractJsonFromChatCompletion() {
        RecommendProperties properties = new RecommendProperties();
        properties.setProvider("openai");
        LlmRecommendClient client = new LlmRecommendClient(properties, new com.fasterxml.jackson.databind.ObjectMapper());

        String body = """
                {
                  "choices": [{
                    "message": {
                      "content": "{\\"itemIds\\":[12,8,5],\\"reason\\":\\"与您关注的图书类物品匹配\\"}"
                    }
                  }]
                }
                """;

        LlmRecommendResponse response = client.parseResponse(body);
        assertNotNull(response);
        assertEquals(3, response.getItemIds().size());
        assertEquals(12L, response.getItemIds().get(0));
    }

    @Test
    void parseResponse_shouldExtractJsonFromGemini() {
        RecommendProperties properties = new RecommendProperties();
        properties.setProvider("gemini");
        LlmRecommendClient client = new LlmRecommendClient(properties, new com.fasterxml.jackson.databind.ObjectMapper());

        String body = """
                {
                  "candidates": [{
                    "content": {
                      "parts": [{
                        "text": "{\\"itemIds\\":[3,7],\\"reason\\":\\"匹配数码类兴趣\\"}"
                      }]
                    }
                  }]
                }
                """;

        LlmRecommendResponse response = client.parseResponse(body);
        assertNotNull(response);
        assertEquals(2, response.getItemIds().size());
        assertEquals(3L, response.getItemIds().get(0));
    }
}
