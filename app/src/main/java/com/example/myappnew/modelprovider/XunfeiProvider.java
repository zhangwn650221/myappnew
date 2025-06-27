package com.example.myappnew.modelprovider;

public class XunfeiProvider implements ModelProvider {
    private String modelVersion = "default";

    @Override
    public void setModelVersion(String version) {
        this.modelVersion = version;
    }

    @Override
    public String getModelVersion() {
        return this.modelVersion;
    }

    @Override
    public String getWebSocketUrl(String apiKey) {
        // 示例：讯飞 WebSocket URL，实际需查阅官方文档
        return "wss://spark-api.xf-yun.com/v1/chat?api_key=" + apiKey;
    }

    @Override
    public String getModelName() {
        return "讯飞";
    }

    @Override
    public String buildRequestMessage(String userInput) {
        return buildRequestMessage(userInput, null);
    }

    @Override
    public String buildRequestMessage(String userInput, String systemPrompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            sb.append("\"system\":\"")
              .append(systemPrompt.replace("\"", "\\\""))
              .append("\",");
        }
        sb.append("\"text\":\"")
          .append(userInput.replace("\"", "\\\""))
          .append("\"");
        sb.append("}");
        return sb.toString();
    }
}
