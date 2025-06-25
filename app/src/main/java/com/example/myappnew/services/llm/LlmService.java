package com.example.myappnew.services.llm;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LlmService {
    // Placeholder for a generic text generation endpoint
    // In a real Retrofit setup, this would be more specific, e.g., @POST("v1/completions")
    @POST("generate") // This is a generic placeholder path
    Call<LlmResponse> generateText(@Body LlmRequest request);

    // Add other methods as needed, e.g., for specific models or tasks
    // LlmResponse analyzeSentiment(LlmRequest request);
    // LlmResponse translateText(LlmRequest request);

    // Non-Retrofit style example (if Retrofit isn't fully working due to environment):
    // void generateTextAsync(LlmRequest request, LlmCallback<LlmResponse> callback);
}

// Example callback interface (if not using Retrofit's Call)
// interface LlmCallback<T> {
//    void onSuccess(T response);
//    void onError(Exception e);
// }
