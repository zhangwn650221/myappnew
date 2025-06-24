package com.example.myappnew.services.llm;

// import com.example.myappnew.BuildConfig; // Not strictly needed for this snippet if API key is passed directly
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
            System.err.println("GEMINI_DEBUG: GeminiLlmServiceImpl - API Key is null or empty. Service will not function.");
            return;
        }
        System.out.println("GEMINI_DEBUG: GeminiLlmServiceImpl - Initializing with API Key.");

        GenerationConfig generationConfig = new GenerationConfig.Builder().build();
        SafetySetting harassmentSafety = new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE);

        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash-latest",
                apiKey,
                generationConfig,
                Collections.singletonList(harassmentSafety)
        );
        this.generativeModelFutures = GenerativeModelFutures.from(gm);
        System.out.println("GEMINI_DEBUG: GeminiLlmServiceImpl - GenerativeModelFutures initialized.");
    }

    @Override
    public Call<LlmResponse> generateText(LlmRequest llmRequest) {
        System.out.println("GEMINI_DEBUG: generateText called.");
        if (generativeModelFutures == null) {
            System.err.println("GEMINI_DEBUG: generateText - GenerativeModelFutures not initialized.");
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
        System.out.println("GEMINI_DEBUG: generateText - Content built for prompt: " + llmRequest.getPrompt());

        ListenableFuture<GenerateContentResponse> future = generativeModelFutures.generateContent(content);
        System.out.println("GEMINI_DEBUG: generateText - generateContent future created.");
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
            System.out.println("GEMINI_DEBUG: extractTextFromCandidate called. Candidate is " + (candidate == null ? "null" : "not null"));
            if (candidate != null) {
                Content content = candidate.getContent();
                System.out.println("GEMINI_DEBUG: Content object is " + (content == null ? "null" : "not null"));
                if (content != null) {
                    List<Part> parts = content.getParts(); 
                    System.out.println("GEMINI_DEBUG: Parts list is " + (parts == null ? "null" : (parts.isEmpty() ? "empty" : "not empty (" + parts.size() + " parts)")));
                    if (parts != null && !parts.isEmpty()) {
                        Part firstPart = parts.get(0);
                        System.out.println("GEMINI_DEBUG: firstPart object is " + (firstPart == null ? "null" : "not null"));
                        if (firstPart != null) {
                            System.out.println("GEMINI_DEBUG: firstPart actual class: " + firstPart.getClass().getName());
                            System.out.println("GEMINI_DEBUG: firstPart.toString(): " + firstPart.toString());
                            // Actual text extraction from Part to be determined based on its class and available methods
                            // For now, this method will return null to focus on logging.
                        }
                    }
                }
            }
            return null; 
        }

        @Override
        public Response<T> execute() throws java.io.IOException {
            System.out.println("GEMINI_DEBUG: execute called.");
            try {
                GenerateContentResponse geminiResponse = future.get();
                System.out.println("GEMINI_DEBUG: execute - geminiResponse is " + (geminiResponse == null ? "null" : "not null"));
                LlmResponse llmResp = new LlmResponse();
                String extractedText = null;
                FinishReason finishReason = null;
                String blockReasonText = null;

                if (geminiResponse != null && geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                    Candidate candidate = geminiResponse.getCandidates().get(0);
                    System.out.println("GEMINI_DEBUG: execute - Candidate is " + (candidate == null ? "null" : "not null"));
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
                System.out.println("GEMINI_DEBUG: execute - Extracted text: " + extractedText);
                if (extractedText != null) {
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
                    System.err.println("GEMINI_DEBUG: execute - Error case: " + errorMessage);
                }
                return Response.success((T) llmResp);
            } catch (Exception e) {
                System.err.println("GEMINI_DEBUG: execute - Exception: " + e.getMessage());
                e.printStackTrace();
                throw new java.io.IOException("Failed to execute Gemini request", e);
            }
        }

        @Override
        public void enqueue(Callback<T> callback) {
            System.out.println("GEMINI_DEBUG: enqueue called. Future is: " + (future == null ? "null" : "not null"));
            Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse geminiResponse) {
                    System.out.println("GEMINI_DEBUG: onSuccess triggered. Cancelled: " + cancelled);
                    if (cancelled) return;
                    System.out.println("GEMINI_DEBUG: onSuccess - geminiResponse is " + (geminiResponse == null ? "null" : "not null"));

                    LlmResponse llmResp = new LlmResponse();
                    String extractedText = null;
                    FinishReason finishReason = null;
                    String blockReasonText = null;

                    if (geminiResponse != null && geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                        Candidate candidate = geminiResponse.getCandidates().get(0);
                        System.out.println("GEMINI_DEBUG: onSuccess - Candidate is " + (candidate == null ? "null" : "not null"));
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
                    System.out.println("GEMINI_DEBUG: onSuccess - Extracted text: " + extractedText);
                    if (extractedText != null) {
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
                        System.err.println("GEMINI_DEBUG: onSuccess - Error case: " + errorMessage);
                        callbackExecutor.execute(() -> callback.onResponse(ListenableFutureCall.this, Response.success((T) llmResp)));
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    System.err.println("GEMINI_DEBUG: onFailure CALLED. Cancelled: " + cancelled);
                    if (cancelled) {
                        System.err.println("GEMINI_DEBUG: Call was cancelled, returning.");
                        return;
                    }

                    if (t == null) {
                        System.err.println("GEMINI_DEBUG: Throwable t IS NULL.");
                    } else {
                        System.err.println("GEMINI_DEBUG: Throwable t is NOT NULL. Class: " + t.getClass().getName() + ", Message: " + t.getMessage());
                        System.err.println("GEMINI_DEBUG: Attempting t.printStackTrace()...");
                        try {
                            t.printStackTrace(); // Directly invokes printStackTrace on the System.err stream
                            System.err.println("GEMINI_DEBUG: t.printStackTrace() CALLED SUCCESSFULLY (check for subsequent stack trace lines).");
                        } catch (Exception e_printStackTrace) {
                            System.err.println("GEMINI_DEBUG: EXCEPTION DURING t.printStackTrace(): " + e_printStackTrace.getMessage());
                        }
                    }

                    final Throwable finalT = t; 
                    callbackExecutor.execute(() -> callback.onFailure(ListenableFutureCall.this, finalT));
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
