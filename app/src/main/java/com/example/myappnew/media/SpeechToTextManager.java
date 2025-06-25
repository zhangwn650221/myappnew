package com.example.myappnew.media;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log; // Using Android's Log

import java.util.ArrayList;
import java.util.Locale;

public class SpeechToTextManager {

    private static final String TAG = "SpeechToTextManager";

    private SpeechRecognizer speechRecognizer;
    private Context context;
    private SpeechToTextListener listener;
    private boolean isListening = false;

    public interface SpeechToTextListener {
        void onSpeechResult(String text);
        void onSpeechError(String error);
        void onReadyForSpeech();
        void onEndOfSpeech();
    }

    public SpeechToTextManager(Context context, SpeechToTextListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        initializeSpeechRecognizer();
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d(TAG, "onReadyForSpeech");
                    if (listener != null) listener.onReadyForSpeech();
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.d(TAG, "onBeginningOfSpeech");
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // Can be used for UI feedback (e.g., voice meter)
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    Log.d(TAG, "onBufferReceived");
                }

                @Override
                public void onEndOfSpeech() {
                    Log.d(TAG, "onEndOfSpeech");
                    isListening = false;
                    if (listener != null) listener.onEndOfSpeech();
                }

                @Override
                public void onError(int error) {
                    Log.e(TAG, "onError: " + getErrorText(error));
                    isListening = false;
                    if (listener != null) listener.onSpeechError(getErrorText(error));
                }

                @Override
                public void onResults(Bundle results) {
                    Log.d(TAG, "onResults");
                    isListening = false;
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        if (listener != null) listener.onSpeechResult(matches.get(0));
                    } else {
                        if (listener != null) listener.onSpeechError("No speech results found.");
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    // Can provide live transcription updates if needed
                    // ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    // if (matches != null && !matches.isEmpty()) {
                    //     Log.d(TAG, "onPartialResults: " + matches.get(0));
                    // }
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    Log.d(TAG, "onEvent: " + eventType);
                }
            });
        } else {
            Log.e(TAG, "Speech recognition not available on this device.");
            if (listener != null) listener.onSpeechError("Speech recognition not available.");
        }
    }

    public void startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
             if (listener != null) listener.onSpeechError("Speech recognition not available.");
            return;
        }
        if (speechRecognizer == null) {
            initializeSpeechRecognizer(); // Safety net
            if (speechRecognizer == null) { // Still null after re-init
                if (listener != null) listener.onSpeechError("Speech recognizer could not be initialized.");
                return;
            }
        }
        if (isListening) {
            Log.w(TAG, "Already listening.");
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Optionally set language, e.g., for Chinese:
        // intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        // intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()); // Use device default
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true); // Example: if you want partial results

        try {
            speechRecognizer.startListening(intent);
            isListening = true;
            Log.i(TAG, "Speech recognition listening started.");
        } catch (Exception e) {
            Log.e(TAG, "Exception starting speech recognition", e);
            isListening = false;
            if (listener != null) listener.onSpeechError("Could not start speech recognition: " + e.getMessage());
        }
    }

    public void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            Log.i(TAG, "Speech recognition listening stopped by user.");
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
            isListening = false;
            Log.i(TAG, "SpeechRecognizer destroyed.");
        }
    }

    public boolean isListening() {
        return isListening;
    }

    private String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions (RECORD_AUDIO permission missing?)";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No speech match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}
