package com.example.myappnew.modelprovider;

public class GeminiProvider implements ModelProvider {
    @Override
    public String getWebSocketUrl(String apiKey) {
        // Gemini 官方目前仅提供 REST API，WebSocket 需企业合作或特殊通道。
        // 这里假设有 WebSocket 代理服务，实际部署时请替换为真实可用的 Gemini WebSocket 地址。
        return "wss://your-gemini-websocket-proxy.example.com/v1/chat?api_key=" + apiKey;
    }

    @Override
    public String getModelName() {
        return "Gemini";
    }

    @Override
    public String buildRequestMessage(String userInput) {
        // Gemini 推荐的消息格式，role:user, parts:[text]
        return "{" +
                "\"model\":\"gemini-1.5-flash\"," +
                "\"contents\":[{" +
                "\"role\":\"user\"," +
                "\"parts\":[{\"text\":\"" + userInput + "\"}]" +
                "}]" +
                "}";
    }
}
