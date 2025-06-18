package com.example.myappnew.services.llm;

public class LlmResponse {
    String generatedText;
    String error;
    // Add other relevant fields from a typical LLM response

    // Constructors, getters, and setters
    public String getGeneratedText() { return generatedText; }
    public void setGeneratedText(String generatedText) { this.generatedText = generatedText; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
