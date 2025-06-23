package com.example.myappnew.services.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;

import com.example.myappnew.services.llm.LlmRequest;
import com.example.myappnew.services.llm.LlmResponse;
import com.example.myappnew.services.llm.LlmService;
import com.example.myappnew.services.llm.LlmServiceProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Service for handling image analysis.
 * It can convert an image URI to Base64 and send it as part of a prompt to an LLM.
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
        String base64Image = convertImageUriToBase64(imageUri);
        if (base64Image == null) {
            callback.onError("Failed to convert image to Base64.");
            return;
        }

        // For OpenAI, a common way to send an image is to describe its content
        // or use a model that supports image inputs directly (e.g., GPT-4 with vision).
        // Here, we'll create a generic prompt.
        // A more sophisticated approach would involve a multi-modal LLM or a two-step process:
        // 1. Use a vision model to get a description or tags for the image.
        // 2. Use that description as input to a text-based LLM for advice.
        // For simplicity, we just indicate an image was processed.
        // The LlmRequest could be extended to include an image_url or image_data field if the LLM API supports it directly.

        String prompt = "The user has provided an image. Based on general positive psychology principles, provide a brief, uplifting piece of advice or a thoughtful question related to finding joy in everyday moments. (Image content is not available to you in this simplified version).";
        // To send the image data (if the LLM and its client library supports it through text prompt):
        // String prompt = "Image data (Base64): " + base64Image.substring(0, Math.min(base64Image.length(), 100)) + "... Describe this image and give advice.";
        // Note: Sending full Base64 strings in prompts can be very long and may exceed token limits for many models.
        // GPT-4 Vision API is the proper way for OpenAI: send image URL or base64 data as a separate parameter.
        // Since our LlmService is generic now, we stick to a text prompt.

        LlmRequest request = new LlmRequest(prompt);
        System.out.println("ImageAnalysisService: Sending request for image (URI: " + imageUri.toString() + ")");

        Call<LlmResponse> call = llmService.generateText(request);
        if (call != null) {
            call.enqueue(new Callback<LlmResponse>() {
                @Override
                public void onResponse(Call<LlmResponse> call, Response<LlmResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getGeneratedText() != null) {
                        System.out.println("ImageAnalysisService: LLM analysis successful.");
                        callback.onSuccess(response.body().getGeneratedText());
                    } else {
                        String errorMsg = "LLM analysis failed: " + (response.errorBody() != null ? response.errorBody().toString() : (response.body() != null && response.body().getError() != null ? response.body().getError() : "Unknown error"));
                        System.out.println("ImageAnalysisService: " + errorMsg);
                        callback.onError(errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<LlmResponse> call, Throwable t) {
                    System.err.println("ImageAnalysisService: LLM call failed: " + t.getMessage());
                    t.printStackTrace();
                    callback.onError("LLM call failed: " + t.getMessage());
                }
            });
        } else {
            String errorMsg = "ImageAnalysisService: LlmService returned a null Call object. Cannot proceed.";
            System.err.println(errorMsg);
            callback.onError(errorMsg);
        }
    }

    private String convertImageUriToBase64(Uri imageUri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.getContentResolver(), imageUri));
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            }

            // Optional: Resize bitmap to reduce Base64 string length if needed
            // Bitmap resizedBitmap = resizeBitmap(bitmap, 600); // Example resize

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream); // Adjust quality as needed
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            System.err.println("ImageAnalysisService: IOException during Base64 conversion: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

     private Bitmap resizeBitmap(Bitmap source, int maxDimension) {
        int originalWidth = source.getWidth();
        int originalHeight = source.getHeight();
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (originalWidth > maxDimension || originalHeight > maxDimension) {
            if (originalWidth > originalHeight) {
                newWidth = maxDimension;
                newHeight = (newWidth * originalHeight) / originalWidth;
            } else {
                newHeight = maxDimension;
                newWidth = (newHeight * originalWidth) / originalHeight;
            }
            return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
        }
        return source;
    }


    public interface AnalysisCallback {
        void onSuccess(String analysisResult);
        void onError(String error);
    }
}
