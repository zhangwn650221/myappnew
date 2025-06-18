package com.example.myappnew.ui.record;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myappnew.R;
import com.example.myappnew.services.llm.LlmServiceProvider;
import com.example.myappnew.services.media.ImageAnalysisService;
import com.example.myappnew.services.media.VoiceAnalysisService;

/**
 * Fragment for recording emotions (simulated image/voice) and displaying AI-generated advice.
 *
 * ---
 * <h4>Testing Strategy (Integration/UI Tests - Espresso):</h4>
 * <ul>
 *     <li>Test button interactions: "Capture Image & Get Advice", "Record Voice & Get Advice".</li>
 *     <li>Mock <code>ImageAnalysisService</code> and <code>VoiceAnalysisService</code> (or their underlying <code>LlmService</code>) to provide controlled responses.</li>
 *     <li>Verify that after button clicks and simulated service responses:
 *         <ul>
 *             <li>The "AI Generated Suggestion:" title becomes visible.</li>
 *             <li>The results TextView displays the expected success or error message from the mocked services.</li>
 *         </ul>
 *     </li>
 *     <li>Test UI state changes (e.g., "Simulating analysis..." message).</li>
 * </ul>
 * ---
 */
public class RecordFragment extends Fragment {

    private Button buttonCaptureImage;
    private Button buttonRecordVoice;
    private TextView textAnalysisResult;
    private TextView textAiSuggestionTitle; // New TextView for the title

    private ImageAnalysisService imageAnalysisService;
    private VoiceAnalysisService voiceAnalysisService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LlmServiceProvider llmProvider = new LlmServiceProvider();
        imageAnalysisService = new ImageAnalysisService(requireContext(), llmProvider);
        voiceAnalysisService = new VoiceAnalysisService(requireContext(), llmProvider);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_record, container, false);

        buttonCaptureImage = root.findViewById(R.id.button_capture_image);
        buttonRecordVoice = root.findViewById(R.id.button_record_voice);
        textAnalysisResult = root.findViewById(R.id.text_analysis_result);
        textAiSuggestionTitle = root.findViewById(R.id.text_ai_suggestion_title); // Initialize title TextView

        buttonCaptureImage.setOnClickListener(v -> {
            textAiSuggestionTitle.setVisibility(View.GONE); // Hide title before new request
            textAnalysisResult.setText("Simulating image capture and analysis...");
            Uri dummyImageUri = Uri.parse("file:///simulated_image.jpg");
            // Toast.makeText(getContext(), "Simulated image captured: " + dummyImageUri.toString(), Toast.LENGTH_SHORT).show();
            imageAnalysisService.analyzeImage(dummyImageUri, new ImageAnalysisService.AnalysisCallback() {
                @Override
                public void onSuccess(String analysisResult) {
                    if (isAddedSafely()) {
                       requireActivity().runOnUiThread(() -> {
                           textAiSuggestionTitle.setVisibility(View.VISIBLE);
                           textAnalysisResult.setText(analysisResult);
                       });
                    }
                }

                @Override
                public void onError(String error) {
                     if (isAddedSafely()) {
                        requireActivity().runOnUiThread(() -> {
                            textAiSuggestionTitle.setVisibility(View.VISIBLE); // Show title even for error message
                            textAnalysisResult.setText("Image Analysis Error: " + error);
                        });
                     }
                }
            });
        });

        buttonRecordVoice.setOnClickListener(v -> {
            textAiSuggestionTitle.setVisibility(View.GONE); // Hide title before new request
            textAnalysisResult.setText("Simulating voice recording and analysis...");
            Uri dummyVoiceUri = Uri.parse("file:///simulated_audio.mp3");
            // Toast.makeText(getContext(), "Simulated voice recorded: " + dummyVoiceUri.toString(), Toast.LENGTH_SHORT).show();
            voiceAnalysisService.analyzeVoice(dummyVoiceUri, new VoiceAnalysisService.AnalysisCallback() {
                @Override
                public void onSuccess(String analysisResult) {
                    if (isAddedSafely()) {
                        requireActivity().runOnUiThread(() -> {
                            textAiSuggestionTitle.setVisibility(View.VISIBLE);
                            textAnalysisResult.setText(analysisResult);
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    if (isAddedSafely()) {
                        requireActivity().runOnUiThread(() -> {
                            textAiSuggestionTitle.setVisibility(View.VISIBLE); // Show title even for error message
                            textAnalysisResult.setText("Voice Analysis Error: " + error);
                        });
                    }
                }
            });
        });

        return root;
    }

    // Helper method to check if fragment is added and activity is available
    private boolean isAddedSafely() {
        return isAdded() && getActivity() != null;
    }
}
