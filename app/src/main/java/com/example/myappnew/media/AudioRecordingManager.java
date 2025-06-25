package com.example.myappnew.media;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log; // Using Android's Log for this example

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioRecordingManager {

    private static final String TAG = "AudioRecordingManager";
    private MediaRecorder mediaRecorder;
    private String currentFilePath;
    private Context context;
    private boolean isRecording = false;

    public AudioRecordingManager(Context context) {
        this.context = context.getApplicationContext();
    }

    private File createAudioFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String audioFileName = "AUDIO_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(null); // Or internal storage: context.getFilesDir()
        if (storageDir == null) {
            Log.e(TAG, "External storage directory not found.");
            throw new IOException("External storage directory not found.");
        }
        File audioFile = File.createTempFile(
                audioFileName,  /* prefix */
                ".3gp",         /* suffix */
                storageDir      /* directory */
        );
        currentFilePath = audioFile.getAbsolutePath();
        return audioFile;
    }

    public boolean startRecording() {
        if (isRecording) {
            Log.w(TAG, "Recording is already in progress.");
            return false;
        }

        File audioFile;
        try {
            audioFile = createAudioFile();
        } catch (IOException e) {
            Log.e(TAG, "Failed to create audio file", e);
            currentFilePath = null;
            return false;
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // .3gp format
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(currentFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Log.i(TAG, "Recording started: " + currentFilePath);
            return true;
        } catch (IOException | IllegalStateException e) {
            Log.e(TAG, "MediaRecorder prepare() or start() failed", e);
            releaseMediaRecorder(); // Clean up
            return false;
        }
    }

    public void stopRecording() {
        if (!isRecording) {
            Log.w(TAG, "Recording was not in progress.");
            return;
        }
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                Log.i(TAG, "Recording stopped: " + currentFilePath);
            } catch (RuntimeException stopRuntimeException) {
                // Handle "stop failed" case, often occurs if stop is called too soon after start or if recorder is in a bad state.
                Log.e(TAG, "MediaRecorder stop() failed", stopRuntimeException);
                deleteCurrentFile(); // Clean up potentially corrupted file
            } finally {
                releaseMediaRecorder();
            }
        }
        isRecording = false;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear configuration (optional here)
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
        }
        // currentFilePath remains for access until next recording starts or file is deleted
    }

    private void deleteCurrentFile() {
        if (currentFilePath != null) {
            File file = new File(currentFilePath);
            if (file.exists()) {
                if (file.delete()) {
                    Log.i(TAG, "Deleted incomplete/corrupted recording: " + currentFilePath);
                } else {
                    Log.e(TAG, "Failed to delete incomplete/corrupted recording: " + currentFilePath);
                }
            }
            currentFilePath = null;
        }
    }

    public String getCurrentFilePath() {
        return currentFilePath;
    }

    public boolean isRecording() {
        return isRecording;
    }

    // Call this when the recording is no longer needed or the UI is destroyed
    public void cleanup() {
        if (isRecording) {
            stopRecording();
        }
        // Potentially delete currentFilePath if it's considered temporary and not explicitly saved by user
    }
}
