package com.exchange.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "exchange.recommend.llm")
public class RecommendProperties {

    /** 是否启用 LLM 推荐；未配置 apiKey 时自动降级 */
    private boolean enabled = true;

    private String apiKey = "";

    /** 模型提供商：gemini / openai（OpenAI 兼容） */
    private String provider = "gemini";

    /** API 基础地址；Gemini 默认 generativelanguage.googleapis.com */
    private String baseUrl = "https://generativelanguage.googleapis.com";

    private String model = "gemini-2.0-flash";

    /** 计划书要求：超时 3 秒内返回，否则降级 */
    private long timeoutMs = 3000;

    /** 交给大模型挑选的候选物品池大小 */
    private int candidatePoolSize = 30;

    /** 最终推荐条数 */
    private int resultSize = 3;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public long getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }
    public int getCandidatePoolSize() { return candidatePoolSize; }
    public void setCandidatePoolSize(int candidatePoolSize) { this.candidatePoolSize = candidatePoolSize; }
    public int getResultSize() { return resultSize; }
    public void setResultSize(int resultSize) { this.resultSize = resultSize; }

    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    public boolean isGemini() {
        return provider == null || provider.isBlank() || "gemini".equalsIgnoreCase(provider);
    }
}
