package com.example.myappnew.modelprovider;

public interface ModelProvider {
    /**
     * 获取 WebSocket 地址，通常需要 API Key 或 Token。
     */
    String getWebSocketUrl(String apiKey);

    /**
     * 获取当前模型名称（如 OpenAI、Gemini、讯飞等）。
     */
    String getModelName();

    /**
     * 构造大模型请求消息体（如 JSON 格式），可根据实际需求扩展参数。
     */
    String buildRequestMessage(String userInput);

    /**
     * 设置当前大模型的具体版本（如 gemini-1.5-flash、gemini-1.5-pro 等）。
     */
    void setModelVersion(String version);

    /**
     * 获取当前大模型的具体版本。
     */
    String getModelVersion();
}
