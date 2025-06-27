package com.example.myappnew.modelprovider;

public class OpenAIProvider implements ModelProvider {
    private String modelVersion = "gpt-3.5-turbo";

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
        // 示例：OpenAI WebSocket URL，实际需查阅官方文档
        return "wss://api.openai.com/v1/chat?api_key=" + apiKey;
    }

    @Override
    public String getModelName() {
        return "OpenAI";
    }

    @Override
    public String buildRequestMessage(String userInput) {
        return buildRequestMessage(userInput, null);
    }

    @Override
    public String buildRequestMessage(String userInput, String systemPrompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"model\":\"").append(modelVersion).append("\",");
        sb.append("\"messages\":[");
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            sb.append("{\"role\":\"system\",\"content\":\"")
              .append(systemPrompt.replace("\"", "\\\""))
              .append("\"},");
        }
        sb.append("{\"role\":\"user\",\"content\":\"")
          .append(userInput.replace("\"", "\\\""))
          .append("\"}]");
        sb.append("}");
        return sb.toString();
    }
}
