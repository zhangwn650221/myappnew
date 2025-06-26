package com.example.myappnew.modelprovider;

public class OpenAIProvider implements ModelProvider {
    @Override
    public String getWebSocketUrl(String apiKey) {
        // 示例：OpenAI WebSocket URL，实际需查阅官方文档
        return "wss://api.openai.com/v1/chat?api_key=" + apiKey;
    }

    @Override
    public String getModelName() {
        return "OpenAI";
    }

    @Override
    public String buildRequestMessage(String userInput) {
        // 示例：实际应按 OpenAI 协议构造 JSON
        return "{\"role\":\"user\",\"content\":\"" + userInput + "\"}";
    }
}
