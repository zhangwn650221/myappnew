package com.example.myappnew.services.media;

import android.content.Context;
import android.net.Uri;
import com.example.myappnew.services.llm.LlmRequest;
import com.example.myappnew.services.llm.LlmResponse;
import com.example.myappnew.services.llm.LlmService;
import com.example.myappnew.services.llm.LlmServiceProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Service for handling voice analysis, currently using a dummy LLM service.
 *
 * ---
 * <h4>Testing Strategy (Unit Tests):</h4>
 * <ul>
 *     <li>Similar to <code>ImageAnalysisService</code>: Mock <code>LlmServiceProvider</code> and <code>LlmService</code>.</li>
 *     <li>Verify that <code>analyzeVoice</code> constructs the correct <code>LlmRequest</code>.</li>
 *     <li>Test the <code>AnalysisCallback</code> for both success and error scenarios from the mocked <code>LlmService</code>.</li>
 * </ul>
 * ---
 */
public class VoiceAnalysisService {
    private LlmService llmService;
    private Context context;

    public VoiceAnalysisService(Context context, LlmServiceProvider llmServiceProvider) {
        this.context = context.getApplicationContext();
        this.llmService = llmServiceProvider.getService();
    }

    public void analyzeVoice(Uri audioUri, AnalysisCallback callback) {
        // In a real app: process audio (e.g., speech-to-text), then send to LLM
        // For now, simulate sending a generic request
        String prompt = "Analyze this voice recording: " + audioUri.toString();
        LlmRequest request = new LlmRequest(prompt);

        System.out.println("VoiceAnalysisService: Sending request for audio URI: " + audioUri.toString());

        Call<LlmResponse> call = llmService.generateText(request);
         if (call != null) {
            call.enqueue(new Callback<LlmResponse>() {
                @Override
                public void onResponse(Call<LlmResponse> call, Response<LlmResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        System.out.println("VoiceAnalysisService: LLM analysis successful.");
                        callback.onSuccess(response.body().getGeneratedText());
                    } else {
                        String errorMsg = "LLM analysis failed: " + (response.errorBody() != null ? response.errorBody().toString() : "Unknown error");
                        System.out.println("VoiceAnalysisService: " + errorMsg);
                        callback.onError(errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<LlmResponse> call, Throwable t) {
                     System.out.println("VoiceAnalysisService: LLM call failed: " + t.getMessage());
                    callback.onError("LLM call failed: " + t.getMessage());
                }
            });
        } else {
            String errorMsg = "VoiceAnalysisService: LlmService returned a null Call object. Cannot proceed with analysis.";
            System.out.println(errorMsg);
            callback.onError(errorMsg);
        }
    }

    public interface AnalysisCallback {
        void onSuccess(String analysisResult);
        void onError(String error);
    }
}
