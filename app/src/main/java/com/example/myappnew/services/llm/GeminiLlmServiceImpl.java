package com.example.myappnew.services.llm;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Candidate;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.FinishReason;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.PromptFeedback;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.ai.client.generativeai.type.TextPart;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeminiLlmServiceImpl implements LlmService {

    private GenerativeModelFutures generativeModelFutures;
    private final Executor mainExecutor = Executors.newSingleThreadExecutor();

    public GeminiLlmServiceImpl(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            // Using System.err for critical initialization errors, though a proper logger is better in production.
            System.err.println("GeminiLlmServiceImpl: API Key is null or empty. Service will not function.");
            return;
        }

        GenerationConfig generationConfig = new GenerationConfig.Builder().build();
        SafetySetting harassmentSafety = new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE);
        // Consider adding other safety settings like HATE_SPEECH, SEXUALLY_EXPLICIT, DANGEROUS_CONTENT
        // Example:
        // SafetySetting hateSpeechSafety = new SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE);
        // List<SafetySetting> safetySettings = Arrays.asList(harassmentSafety, hateSpeechSafety);

        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash-latest", // Or "gemini-pro" or other applicable models
                apiKey,
                generationConfig,
                Collections.singletonList(harassmentSafety) // Replace with 'safetySettings' if using multiple
        );
        this.generativeModelFutures = GenerativeModelFutures.from(gm);
    }

    @Override
    public Call<LlmResponse> generateText(LlmRequest llmRequest) {
        if (generativeModelFutures == null) {
            System.err.println("GeminiLlmServiceImpl: GenerativeModelFutures not initialized (likely due to missing API key).");
            return new Call<LlmResponse>() {
                @Override
                public Response<LlmResponse> execute() throws java.io.IOException {
                    throw new java.io.IOException("Gemini client not initialized.");
                }
                @Override
                public void enqueue(Callback<LlmResponse> callback) {
                    callback.onFailure(this, new IllegalStateException("Gemini client not initialized."));
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
        return new ListenableFutureCall<>(future, mainExecutor);
    }

    private static class ListenableFutureCall<T> implements Call<T> {
        private final ListenableFuture<GenerateContentResponse> future;
        private final Executor callbackExecutor;
        private volatile boolean cancelled = false;

        ListenableFutureCall(ListenableFuture<GenerateContentResponse> future, Executor callbackExecutor) {
            this.future = future;
            this.callbackExecutor = callbackExecutor;
        }

        private String extractTextFromCandidate(Candidate candidate) {
            if (candidate != null) {
                Content content = candidate.getContent();
                if (content != null) {
                    List<Part> parts = content.getParts();
                    if (parts != null && !parts.isEmpty()) {
                        Part firstPart = parts.get(0);
                        if (firstPart instanceof TextPart) {
                            TextPart textPart = (TextPart) firstPart;
                            return textPart.getText();
                        } else {
                             // Log if the part is not a TextPart, for debugging if needed in future.
                            System.err.println("GeminiLlmServiceImpl: First part is not an instance of TextPart. Actual type: " + firstPart.getClass().getName());
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public Response<T> execute() throws java.io.IOException {
            try {
                GenerateContentResponse geminiResponse = future.get();
                LlmResponse llmResp = new LlmResponse();
                String extractedText = null;
                FinishReason finishReason = null;
                String blockReasonText = null;

                if (geminiResponse != null && geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                    Candidate candidate = geminiResponse.getCandidates().get(0);
                    if (candidate != null) {
                        finishReason = candidate.getFinishReason();
                        extractedText = extractTextFromCandidate(candidate);
                    }
                }

                if (geminiResponse != null && geminiResponse.getPromptFeedback() != null) {
                    PromptFeedback promptFeedback = geminiResponse.getPromptFeedback();
                    if (promptFeedback.getBlockReason() != null) {
                         blockReasonText = "Prompt Feedback Block Reason: " + promptFeedback.getBlockReason().toString();
                    }
                }

                if (extractedText != null && !extractedText.isEmpty()) {
                    llmResp.setGeneratedText(extractedText);
                } else {
                    String errorMessage = "Gemini response content is null or empty.";
                    if (finishReason != null) {
                        errorMessage += " Finish Reason: " + finishReason.toString();
                    }
                    if (blockReasonText != null) {
                        errorMessage += (finishReason != null ? " | " : " ") + blockReasonText;
                    }
                    llmResp.setError(errorMessage);
                     System.err.println("GeminiLlmServiceImpl: execute - Error case: " + errorMessage);
                }
                return Response.success((T) llmResp);
            } catch (Exception e) {
                System.err.println("GeminiLlmServiceImpl: execute - Exception: " + e.getMessage());
                e.printStackTrace();
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
                    String extractedText = null;
                    FinishReason finishReason = null;
                    String blockReasonText = null;

                    if (geminiResponse != null && geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                        Candidate candidate = geminiResponse.getCandidates().get(0);
                        if (candidate != null) {
                            finishReason = candidate.getFinishReason();
                            extractedText = extractTextFromCandidate(candidate);
                        }
                    }

                    if (geminiResponse != null && geminiResponse.getPromptFeedback() != null) {
                        PromptFeedback promptFeedback = geminiResponse.getPromptFeedback();
                         if (promptFeedback.getBlockReason() != null) {
                            blockReasonText = "Prompt Feedback Block Reason: " + promptFeedback.getBlockReason().toString();
                        }
                    }

                    if (extractedText != null && !extractedText.isEmpty()) {
                        llmResp.setGeneratedText(extractedText);
                        callbackExecutor.execute(() -> callback.onResponse(ListenableFutureCall.this, Response.success((T) llmResp)));
                    } else {
                        String errorMessage = "Gemini response content is null or empty.";
                        if (finishReason != null) {
                            errorMessage += " Finish Reason: " + finishReason.toString();
                        }
                         if (blockReasonText != null) {
                            errorMessage += (finishReason != null ? " | " : " ") + blockReasonText;
                        }
                        llmResp.setError(errorMessage);
                        System.err.println("GeminiLlmServiceImpl: onSuccess - Error case: " + errorMessage);
                        callbackExecutor.execute(() -> callback.onResponse(ListenableFutureCall.this, Response.success((T) llmResp)));
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    if (cancelled) return;

                    // Log detailed error information
                    if (t != null) {
                        System.err.println("GeminiLlmServiceImpl: Failure from Gemini SDK. Class: " + t.getClass().getName() + ", Message: " + t.getMessage());
                        t.printStackTrace(); // This should print the full stack trace to System.err
                        Throwable cause = t.getCause();
                        if (cause != null) {
                             System.err.println("GeminiLlmServiceImpl: Caused by: " + cause.getClass().getName() + " - " + cause.getMessage());
                             cause.printStackTrace();
                        }
                    } else {
                        System.err.println("GeminiLlmServiceImpl: Failure from Gemini SDK: Throwable t is null.");
                    }
                    callbackExecutor.execute(() -> callback.onFailure(ListenableFutureCall.this, t));
                }
            }, callbackExecutor);
        }

        @Override public boolean isExecuted() { return future.isDone(); }
        @Override public void cancel() { cancelled = true; future.cancel(true); }
        @Override public boolean isCanceled() { return cancelled || future.isCancelled(); }
        @Override public Call<T> clone() { return new ListenableFutureCall<>(future, callbackExecutor); }
        @Override public okhttp3.Request request() { return new okhttp3.Request.Builder().url("http://localhost/gemini-sdk-internal").build(); }
        @Override public okio.Timeout timeout() { return okio.Timeout.NONE; }
    }
}
