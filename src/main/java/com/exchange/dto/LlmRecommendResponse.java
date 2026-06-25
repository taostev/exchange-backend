package com.exchange.dto;

import java.util.ArrayList;
import java.util.List;

/** 解析大模型返回的推荐结果 */
public class LlmRecommendResponse {
    private List<Long> itemIds = new ArrayList<>();
    private String reason;

    public List<Long> getItemIds() { return itemIds; }
    public void setItemIds(List<Long> itemIds) { this.itemIds = itemIds; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
