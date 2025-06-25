package com.example.myappnew.ui.record;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView; // Added for image preview
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log; // Added for debug logging

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
    private Button buttonTextRecord;
    private Button buttonSelectPhoto;
    private Button buttonRecordVideo;
    private TextView textAnalysisResult;
    private TextView textAiSuggestionTitle; // New TextView for the title
    private ImageView imagePreview; // Added for image preview
    private Uri currentImageUri; // To store URI of the image to be captured

    private static final String TAG_DEBUG = "DEBUG-RecordFragment"; // Tag for debug logs

    private ImageAnalysisService imageAnalysisService;
    private VoiceAnalysisService voiceAnalysisService;

    // ActivityResultLaunchers for permissions and camera
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    // private ActivityResultLauncher<Intent> takePictureLauncher; // Kept for reference, but not actively used for primary path
    private ActivityResultLauncher<Uri> takePictureWithUriOutputLauncher;

    // ActivityResultLaunchers for permissions and audio recording
    private ActivityResultLauncher<String> requestMicPermissionLauncher;
    private ActivityResultLauncher<Intent> recordAudioLauncher; // Using generic StartActivityForResult for MediaStore.Audio.Media.RECORD_SOUND_ACTION
    private Uri currentAudioUri; // To store URI of the recorded audio


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LlmServiceProvider llmProvider = new LlmServiceProvider();
        imageAnalysisService = new ImageAnalysisService(requireContext(), llmProvider);
        voiceAnalysisService = new VoiceAnalysisService(requireContext(), llmProvider);

        // Initialize permission launcher
        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                launchCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission is required to capture images.", Toast.LENGTH_SHORT).show();
                textAnalysisResult.setText("Camera permission denied. Cannot capture image.");
                textAiSuggestionTitle.setVisibility(View.VISIBLE);
            }
        });

        // Initialize camera launcher (for thumbnail) - Kept for reference, but prefer Uri output
        // takePictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> { ... });

        // Initialize camera launcher for full-size image URI output
        takePictureWithUriOutputLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success) {
                if (currentImageUri != null) {
                    showToast("Image captured successfully: " + currentImageUri.toString());
                    startImageAnalysis(currentImageUri);
                } else {
                    showToast("Image captured, but URI is null.");
                    textAnalysisResult.setText("Image captured, but URI is null.");
                    textAiSuggestionTitle.setVisibility(View.VISIBLE);
                }
            } else {
                showToast("Image capture failed or was cancelled.");
                // Clean up the empty file if it was created and capture failed
                if (currentImageUri != null) {
                    getContext().getContentResolver().delete(currentImageUri, null, null);
                    currentImageUri = null;
                }
                textAnalysisResult.setText("Image capture failed or was cancelled.");
                textAiSuggestionTitle.setVisibility(View.VISIBLE);
            }
        });

        // Initialize microphone permission launcher
        requestMicPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                launchAudioRecorder();
            } else {
                Toast.makeText(getContext(), "Microphone permission is required to record audio.", Toast.LENGTH_SHORT).show();
                textAnalysisResult.setText("Microphone permission denied. Cannot record audio.");
                textAiSuggestionTitle.setVisibility(View.VISIBLE);
            }
        });

        // Initialize audio recorder launcher
        // Using StartActivityForResult because ActivityResultContracts.RecordAudio() is very basic
        // and MediaStore.Audio.Media.RECORD_SOUND_ACTION gives more control to system apps.
        recordAudioLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                currentAudioUri = result.getData().getData(); // The system audio recorder returns the Uri of the saved file.
                if (currentAudioUri != null) {
                    showToast("Audio recorded successfully: " + currentAudioUri.toString());
                    startVoiceAnalysis(currentAudioUri);
                } else {
                    showToast("Audio recorded, but URI is null.");
                    textAnalysisResult.setText("Audio recorded, but URI is null.");
                    textAiSuggestionTitle.setVisibility(View.VISIBLE);
                }
            } else {
                showToast("Audio recording failed or was cancelled.");
                textAnalysisResult.setText("Audio recording failed or was cancelled.");
                textAiSuggestionTitle.setVisibility(View.VISIBLE);
                currentAudioUri = null; // Reset if recording failed
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_record, container, false);

        buttonCaptureImage = root.findViewById(R.id.button_capture_image);
        buttonRecordVoice = root.findViewById(R.id.button_record_voice);
        buttonTextRecord = root.findViewById(R.id.button_text_record);
        buttonSelectPhoto = root.findViewById(R.id.button_select_photo);
        buttonRecordVideo = root.findViewById(R.id.button_record_video);
        textAnalysisResult = root.findViewById(R.id.text_analysis_result);
        textAiSuggestionTitle = root.findViewById(R.id.text_ai_suggestion_title);
        imagePreview = root.findViewById(R.id.image_preview); // Initialize ImageView

        buttonCaptureImage.setOnClickListener(v -> {
            Log.d(TAG_DEBUG, "Capture Image button clicked.");
            imagePreview.setImageURI(null); // Clear previous preview
            imagePreview.setVisibility(View.GONE);
            textAiSuggestionTitle.setVisibility(View.GONE);
            textAnalysisResult.setText("Checking camera permission...");
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Log.d(TAG_DEBUG, "Requesting camera permission.");
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        buttonRecordVoice.setOnClickListener(v -> {
            Log.d(TAG_DEBUG, "Record Voice button clicked.");
            imagePreview.setImageURI(null);
            imagePreview.setVisibility(View.GONE);
            textAiSuggestionTitle.setVisibility(View.GONE);
            textAnalysisResult.setText("Checking microphone permission...");
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                launchAudioRecorder();
            } else {
                Log.d(TAG_DEBUG, "Requesting microphone permission.");
                requestMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        });

        buttonTextRecord.setOnClickListener(v -> {
            Toast.makeText(getContext(), "文本记录功能开发中...", Toast.LENGTH_SHORT).show();
        });
        buttonSelectPhoto.setOnClickListener(v -> {
            Toast.makeText(getContext(), "选择照片功能开发中...", Toast.LENGTH_SHORT).show();
        });
        buttonRecordVideo.setOnClickListener(v -> {
            Toast.makeText(getContext(), "录制视频功能开发中...", Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    private void launchCamera() {
        Log.d(TAG_DEBUG, "launchCamera called.");
        textAnalysisResult.setText("Launching camera...");
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture " + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DESCRIPTION, "From MyAppnew Camera");
        currentImageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Log.d(TAG_DEBUG, "launchCamera - currentImageUri created: " + (currentImageUri != null ? currentImageUri.toString() : "null"));

        if (currentImageUri != null) {
            takePictureWithUriOutputLauncher.launch(currentImageUri);
        } else {
            Log.e(TAG_DEBUG, "launchCamera - Failed to create image file URI.");
            Toast.makeText(getContext(), "Failed to create image file.", Toast.LENGTH_SHORT).show();
            textAnalysisResult.setText("Failed to prepare for image capture.");
            textAiSuggestionTitle.setVisibility(View.VISIBLE);
        }
        // Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
        //    // If you also want to save to URI with this older method, you'd set EXTRA_OUTPUT here
        //    // takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
        //    takePictureLauncher.launch(takePictureIntent); // This launcher expects a Bitmap in return if EXTRA_OUTPUT not set
        // } else {
        //    Toast.makeText(getContext(), "No camera app found.", Toast.LENGTH_SHORT).show();
        // }
        // --- End Alternative ---
    }

    private void launchAudioRecorder() {
        Log.d(TAG_DEBUG, "launchAudioRecorder called.");
        textAnalysisResult.setText("Launching audio recorder...");
        Intent recordAudioIntent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        if (recordAudioIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            recordAudioLauncher.launch(recordAudioIntent);
        } else {
            Log.e(TAG_DEBUG, "launchAudioRecorder - No app found to record audio.");
            Toast.makeText(getContext(), "No app found to record audio.", Toast.LENGTH_LONG).show();
            textAnalysisResult.setText("No app found to record audio.");
            textAiSuggestionTitle.setVisibility(View.VISIBLE);
        }
    }

    private void startImageAnalysis(Uri imageUri) {
        Log.d(TAG_DEBUG, "startImageAnalysis - received URI: " + (imageUri != null ? imageUri.toString() : "null"));
        if (imageUri == null) {
            Log.e(TAG_DEBUG, "startImageAnalysis - imageUri is null.");
            textAnalysisResult.setText("Error: No image URI provided for analysis.");
            textAiSuggestionTitle.setVisibility(View.VISIBLE);
            return;
        }
        imagePreview.setImageURI(imageUri); // Show preview
        imagePreview.setVisibility(View.VISIBLE);
        textAnalysisResult.setText("Image captured. Starting analysis...");
        imageAnalysisService.analyzeImage(imageUri, new ImageAnalysisService.AnalysisCallback() {
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
                        textAiSuggestionTitle.setVisibility(View.VISIBLE);
                        textAnalysisResult.setText("Image Analysis Error: " + error);
                    });
                }
            }
        });
    }

    private void startVoiceAnalysis(Uri audioUri) {
        Log.d(TAG_DEBUG, "startVoiceAnalysis - received URI: " + (audioUri != null ? audioUri.toString() : "null"));
        if (audioUri == null) {
            Log.e(TAG_DEBUG, "startVoiceAnalysis - audioUri is null.");
            textAnalysisResult.setText("Error: No audio URI provided for analysis.");
            textAiSuggestionTitle.setVisibility(View.VISIBLE);
            return;
        }
        // No preview for audio, just indicate analysis.
        imagePreview.setVisibility(View.GONE); // Hide image preview if it was visible
        textAnalysisResult.setText("Audio recorded. Starting analysis...");
        voiceAnalysisService.analyzeVoice(audioUri, new VoiceAnalysisService.AnalysisCallback() {
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
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    // Helper method to check if fragment is added and activity is available
    private boolean isAddedSafely() {
        return isAdded() && getActivity() != null;
    }
}
