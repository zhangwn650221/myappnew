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
 * Service for handling image analysis, currently using a dummy LLM service.
 *
 * ---
 * <h4>Testing Strategy (Unit Tests):</h4>
 * <ul>
 *     <li>Mock <code>LlmServiceProvider</code> and <code>LlmService</code> dependencies (e.g., using Mockito).</li>
 *     <li>Verify that <code>analyzeImage</code> constructs the correct <code>LlmRequest</code> based on input.</li>
 *     <li>Test the <code>AnalysisCallback</code>:
 *         <ul>
 *             <li>Ensure <code>onSuccess</code> is called with expected data when the mocked <code>LlmService</code> simulates a successful response.</li>
 *             <li>Ensure <code>onError</code> is called when the mocked <code>LlmService</code> simulates a failure or returns null Call object.</li>
 *         </ul>
 *     </li>
 *     <li>Consider testing with different URI inputs if applicable.</li>
 * </ul>
 * ---
 */
public class ImageAnalysisService {
    private LlmService llmService;
    private Context context;

    public ImageAnalysisService(Context context, LlmServiceProvider llmServiceProvider) {
        this.context = context.getApplicationContext();
        this.llmService = llmServiceProvider.getService();
    }

    public void analyzeImage(Uri imageUri, AnalysisCallback callback) {
        // In a real app: process the image, extract features, or prepare for LLM
        // For now, simulate sending a generic request based on the image URI
        String prompt = "Analyze this image: " + imageUri.toString();
        LlmRequest request = new LlmRequest(prompt);

        System.out.println("ImageAnalysisService: Sending request for image URI: " + imageUri.toString());

        // Using the dummy LlmService which should provide a mock Call
        Call<LlmResponse> call = llmService.generateText(request);
        if (call != null) {
            call.enqueue(new Callback<LlmResponse>() {
                @Override
                public void onResponse(Call<LlmResponse> call, Response<LlmResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        System.out.println("ImageAnalysisService: LLM analysis successful.");
                        callback.onSuccess(response.body().getGeneratedText());
                    } else {
                        String errorMsg = "LLM analysis failed: " + (response.errorBody() != null ? response.errorBody().toString() : "Unknown error");
                        System.out.println("ImageAnalysisService: " + errorMsg);
                        callback.onError(errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<LlmResponse> call, Throwable t) {
                    System.out.println("ImageAnalysisService: LLM call failed: " + t.getMessage());
                    callback.onError("LLM call failed: " + t.getMessage());
                }
            });
        } else {
            // This case might occur if the dummy service can't produce a Call object
            // (e.g. if retrofit-mock is not available and it returns null)
            String errorMsg = "ImageAnalysisService: LlmService returned a null Call object. Cannot proceed with analysis.";
            System.out.println(errorMsg);
            callback.onError(errorMsg);
        }
    }

    public interface AnalysisCallback {
        void onSuccess(String analysisResult);
        void onError(String error);
    }
}
