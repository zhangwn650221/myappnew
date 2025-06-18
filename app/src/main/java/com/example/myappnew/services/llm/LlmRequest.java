package com.example.myappnew.services.llm;

public class LlmRequest {
    String prompt;
    // Add other common parameters like temperature, maxTokens, etc.
    // Potentially a way to specify the target model/provider

    public LlmRequest(String prompt) {
        this.prompt = prompt;
    }

    // Getters and setters
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
}
