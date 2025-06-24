package com.example.myappnew.services.llm;

import com.example.myappnew.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors; // Using a simple executor for the demo

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeminiLlmServiceImpl implements LlmService {

    private GenerativeModelFutures generativeModelFutures;
    private final Executor mainExecutor = Executors.newSingleThreadExecutor(); // Or use Android's MainThreadExecutor

    public GeminiLlmServiceImpl(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("GeminiLlmServiceImpl: API Key is null or empty. Service will not function.");
            // Optionally throw an IllegalArgumentException or handle this state appropriately
            return;
        }

        // Configure safety settings (adjust as needed)
        GenerationConfig generationConfig = new GenerationConfig.Builder()
            // .temperature(0.9f) // Example configuration
            // .topK(1)
            // .topP(1f)
            // .maxOutputTokens(2048)
            .build();

        SafetySetting harassmentSafety = new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE);
        SafetySetting hateSpeechSafety = new SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE);
        // Add other safety settings as needed

        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash-latest", // Or another suitable model like "gemini-pro"
                apiKey,
                generationConfig,
                Collections.singletonList(harassmentSafety) // Pass list of safety settings
        );
        this.generativeModelFutures = GenerativeModelFutures.from(gm);
    }

    @Override
    public Call<LlmResponse> generateText(LlmRequest llmRequest) {
        if (generativeModelFutures == null) {
            System.err.println("GeminiLlmServiceImpl: GenerativeModelFutures not initialized (likely due to missing API key).");
            // Return a Call that immediately fails
            return new Call<LlmResponse>() {
                @Override
                public Response<LlmResponse> execute() throws java.io.IOException {
                    throw new java.io.IOException("Gemini client not initialized.");
                }
                @Override
                public void enqueue(Callback<LlmResponse> callback) {
                    callback.onFailure(this, new IllegalStateException("Gemini client not initialized (API Key likely missing)."));
                }
                @Override public boolean isExecuted() { return false; }
                @Override public void cancel() {}
                @Override public boolean isCanceled() { return false; }
                @Override public Call<LlmResponse> clone() { return this; }
                @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("http://localhost/mock").build(); }
                @Override public okio.Timeout timeout() { return okio.Timeout.NONE;}
            };
        }

        Content content = new Content.Builder()
                .addText(llmRequest.getPrompt())
                .build();

        ListenableFuture<GenerateContentResponse> future = generativeModelFutures.generateContent(content);

        // Adapt ListenableFuture to Retrofit Call
        return new ListenableFutureCall<>(future, mainExecutor);
    }

    // Adapter class to bridge ListenableFuture with Retrofit's Call
    private static class ListenableFutureCall<T> implements Call<T> {
        private final ListenableFuture<GenerateContentResponse> future;
        private final Executor callbackExecutor;
        private volatile boolean cancelled = false;
        private T adaptedResponse; // To store the adapted response for synchronous execute (if ever needed)

        ListenableFutureCall(ListenableFuture<GenerateContentResponse> future, Executor callbackExecutor) {
            this.future = future;
            this.callbackExecutor = callbackExecutor;
        }

        @Override
        public Response<T> execute() throws java.io.IOException {
            // Blocking execute: Generally, avoid on Android's main thread.
            // This is a simplified implementation for the Call interface.
            try {
                GenerateContentResponse geminiResponse = future.get(); // This blocks
                LlmResponse llmResp = new LlmResponse();
                if (geminiResponse.getText() != null) {
                    llmResp.setGeneratedText(geminiResponse.getText());
                } else {
                    llmResp.setError("Gemini response was null or empty.");
                    // Consider checking geminiResponse.getCandidatesList() and FinishReason
                }
                adaptedResponse = (T) llmResp; // Unchecked cast, ensure T is LlmResponse
                return Response.success(adaptedResponse);
            } catch (Exception e) {
                throw new java.io.IOException("Failed to execute Gemini request", e);
            }
        }

        @Override
        public void enqueue(Callback<T> callback) {
            Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse geminiResponse) {
                    if (cancelled) return;
                    LlmResponse llmResp = new LlmResponse();
                    // Ensuring we use getCandidates() as per the attempted fix.
                    if (geminiResponse != null && geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty() &&
                        geminiResponse.getCandidates().get(0).getContent() != null &&
                        geminiResponse.getCandidates().get(0).getContent().getPartsList() != null &&
                        !geminiResponse.getCandidates().get(0).getContent().getPartsList().isEmpty() &&
                        geminiResponse.getCandidates().get(0).getContent().getPartsList().get(0).getText() != null) {
                        llmResp.setGeneratedText(geminiResponse.getCandidates().get(0).getContent().getPartsList().get(0).getText());
                        callbackExecutor.execute(() -> callback.onResponse(ListenableFutureCall.this, Response.success((T) llmResp)));
                    } else {
                        String errorMessage = "Gemini response content is null or empty.";
                        // Check candidate for FinishReason
                        if (geminiResponse != null && geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty() &&
                            geminiResponse.getCandidates().get(0).getFinishReason() != null) {
                            errorMessage += " Finish Reason: " + geminiResponse.getCandidates().get(0).getFinishReason().toString();
                        }
                        // Check PromptFeedback for BlockReason as a fallback or additional info
                        else if (geminiResponse != null && geminiResponse.getPromptFeedback() != null &&
                                 geminiResponse.getPromptFeedback().getBlockReason() != null) {
                            errorMessage += " Prompt Feedback Block Reason: " + geminiResponse.getPromptFeedback().getBlockReason().toString();
                        }
                        llmResp.setError(errorMessage);
                        System.err.println("GeminiLlmServiceImpl: " + errorMessage);
                        callbackExecutor.execute(() -> callback.onResponse(ListenableFutureCall.this, Response.success((T) llmResp)));
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    if (cancelled) return;
                    System.err.println("GeminiLlmServiceImpl: Failure from Gemini SDK: " + t.getMessage());
                    t.printStackTrace();
                    callbackExecutor.execute(() -> callback.onFailure(ListenableFutureCall.this, t));
                }
            }, callbackExecutor); // Ensure Futures uses the same executor for its own internal needs if not specified
        }

        @Override
        public boolean isExecuted() {
            return future.isDone(); // Or a more sophisticated tracking if needed
        }

        @Override
        public void cancel() {
            cancelled = true;
            future.cancel(true); // Propagate cancellation
        }

        @Override
        public boolean isCanceled() {
            return cancelled || future.isCancelled();
        }

        @Override
        public Call<T> clone() {
            // Create a new ListenableFutureCall with the same (or new) future.
            // For simplicity, if the original future can be retried, this might work.
            // However, ListenableFuture itself might not be "cloneable" in a way Retrofit expects.
            // This simplified version might not be robust for all Retrofit retry scenarios.
            return new ListenableFutureCall<>(future, callbackExecutor); // This is a shallow clone regarding the future.
        }

        @Override
        public okhttp3.Request request() {
            // Gemini SDK handles its own requests. We can't easily expose an OkHttp Request.
            // Return a dummy or throw UnsupportedOperationException.
            return new okhttp3.Request.Builder().url("http://localhost/gemini-sdk-internal").build();
        }

        @Override
        public okio.Timeout timeout() {
            return okio.Timeout.NONE; // Gemini SDK manages its own timeouts.
        }
    }
}
