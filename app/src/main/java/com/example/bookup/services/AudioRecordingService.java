package com.example.bookup.services;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import java.io.File;
import java.io.IOException;

/**
 * Service for recording audio with MediaRecorder.
 * Handles recording lifecycle, file management, and error handling.
 * Records in AAC codec at 44.1kHz in MPEG-4 container (.m4a).
 */
public class AudioRecordingService {

    private static final String TAG = "AudioRecordingService";

    // Audio recording parameters
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int OUTPUT_FORMAT = MediaRecorder.OutputFormat.MPEG_4;
    private static final int AUDIO_ENCODER = MediaRecorder.AudioEncoder.AAC;
    private static final int SAMPLE_RATE = 44100; // 44.1 kHz
    private static final int BIT_RATE = 128000; // 128 kbps
    private static final int CHANNELS = 1; // Mono

    private static final long MINIMUM_RECORDING_DURATION = 1000; // 1 second minimum
    private static final long MAXIMUM_RECORDING_DURATION = 300000; // 5 minutes maximum

    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private long recordingStartTime;
    private boolean isRecording = false;

    // Callbacks
    public interface OnRecordingListener {
        void onRecordingStarted();
        void onRecordingProgress(long durationMs);
        void onRecordingStopped(long durationMs, String filePath);
        void onRecordingError(String errorMessage);
    }

    private OnRecordingListener recordingListener;

    public AudioRecordingService(Context context) {
        // Audio files will be stored in app's cache directory
        File cacheDir = context.getCacheDir();
        File audioDir = new File(cacheDir, "audio_recordings");
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }
        this.audioFilePath = new File(audioDir, "recording_" + System.currentTimeMillis() + ".m4a").getAbsolutePath();
    }

    public void setOnRecordingListener(OnRecordingListener listener) {
        this.recordingListener = listener;
    }

    /**
     * Start audio recording.
     * Initializes MediaRecorder and begins capturing audio.
     */
    public void startRecording() {
        try {
            if (isRecording) {
                Log.w(TAG, "Recording is already in progress");
                return;
            }

            // Create new MediaRecorder instance
            mediaRecorder = new MediaRecorder();
            Log.d(TAG, "✅ MediaRecorder created");

            // Set audio source (microphone)
            mediaRecorder.setAudioSource(AUDIO_SOURCE);
            Log.d(TAG, "✅ Audio source set to MIC");

            // Set output format
            mediaRecorder.setOutputFormat(OUTPUT_FORMAT);
            Log.d(TAG, "✅ Output format set to MPEG-4");

            // Set audio encoder
            mediaRecorder.setAudioEncoder(AUDIO_ENCODER);
            Log.d(TAG, "✅ Audio encoder set to AAC");

            // Set sample rate
            mediaRecorder.setAudioSamplingRate(SAMPLE_RATE);
            Log.d(TAG, "✅ Sample rate set to " + SAMPLE_RATE + " Hz");

            // Set bit rate
            mediaRecorder.setAudioEncodingBitRate(BIT_RATE);
            Log.d(TAG, "✅ Bit rate set to " + BIT_RATE + " bps");

            // Set number of channels (mono)
            mediaRecorder.setAudioChannels(CHANNELS);
            Log.d(TAG, "✅ Channels set to " + CHANNELS + " (mono)");

            // Set output file path
            mediaRecorder.setOutputFile(audioFilePath);
            Log.d(TAG, "✅ Output file set to " + audioFilePath);

            // Set maximum duration (safety limit)
            mediaRecorder.setMaxDuration((int) MAXIMUM_RECORDING_DURATION);
            Log.d(TAG, "✅ Max duration set to " + MAXIMUM_RECORDING_DURATION + "ms");

            // Prepare recorder
            mediaRecorder.prepare();
            Log.d(TAG, "✅ MediaRecorder prepared");

            // Start recording
            mediaRecorder.start();
            Log.d(TAG, "✅ Recording started");

            recordingStartTime = System.currentTimeMillis();
            isRecording = true;

            if (recordingListener != null) {
                recordingListener.onRecordingStarted();
            }

            Log.d(TAG, "✅ Recording initiated successfully: " + audioFilePath);

        } catch (IOException e) {
            Log.e(TAG, "❌ IOException when starting recording", e);
            cleanup();
            if (recordingListener != null) {
                recordingListener.onRecordingError("Failed to start recording: " + e.getMessage());
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "❌ IllegalStateException when starting recording", e);
            cleanup();
            if (recordingListener != null) {
                recordingListener.onRecordingError("Recording state error: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Unexpected error when starting recording", e);
            cleanup();
            if (recordingListener != null) {
                recordingListener.onRecordingError("Failed to start recording: " + e.getMessage());
            }
        }
    }

    /**
     * Stop audio recording and save file.
     * Validates recording duration before returning success.
     */
    public void stopRecording() {
        if (!isRecording || mediaRecorder == null) {
            Log.w(TAG, "Recording is not active");
            return;
        }

        try {
            // Try to stop the recorder
            mediaRecorder.stop();
            long recordingDurationMs = System.currentTimeMillis() - recordingStartTime;

            // Check minimum recording duration
            if (recordingDurationMs < MINIMUM_RECORDING_DURATION) {
                Log.w(TAG, "Recording too short: " + recordingDurationMs + "ms");
                // Delete the short recording
                deleteAudioFile();
                if (recordingListener != null) {
                    recordingListener.onRecordingError(
                            "Recording too short (minimum 1 second)");
                }
                cleanup();
                return;
            }

            isRecording = false;

            if (recordingListener != null) {
                recordingListener.onRecordingStopped(recordingDurationMs, audioFilePath);
            }

            Log.d(TAG, "Recording stopped. Duration: " + recordingDurationMs + "ms");

        } catch (IllegalStateException e) {
            // MediaRecorder not in correct state
            Log.e(TAG, "❌ MediaRecorder in invalid state when stopping", e);
            if (recordingListener != null) {
                recordingListener.onRecordingError("Recording state error: " + e.getMessage());
            }
        } catch (RuntimeException e) {
            // Catch other runtime exceptions (IO errors, etc)
            Log.e(TAG, "❌ Failed to stop recording", e);
            if (recordingListener != null) {
                recordingListener.onRecordingError("Failed to stop recording: " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }

    /**
     * Cancel recording without saving.
     */
    public void cancelRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                Log.e(TAG, "Error stopping recorder during cancel", e);
            }
        }
        deleteAudioFile();
        cleanup();
    }

    /**
     * Get current recording duration in milliseconds.
     */
    public long getCurrentRecordingDuration() {
        if (isRecording) {
            return System.currentTimeMillis() - recordingStartTime;
        }
        return 0;
    }

    /**
     * Check if currently recording.
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * Get the path to the recorded audio file.
     */
    public String getAudioFilePath() {
        return audioFilePath;
    }

    /**
     * Get the duration of the last recorded audio in milliseconds.
     */
    public long getRecordedDuration() {
        if (recordingStartTime > 0) {
            return System.currentTimeMillis() - recordingStartTime;
        }
        return 0;
    }

    /**
     * Get file size in bytes.
     */
    public long getAudioFileSize() {
        File file = new File(audioFilePath);
        if (file.exists()) {
            return file.length();
        }
        return 0;
    }

    /**
     * Delete the audio file.
     */
    public void deleteAudioFile() {
        File file = new File(audioFilePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Log.d(TAG, "Audio file deleted: " + audioFilePath);
            }
        }
    }

    /**
     * Release MediaRecorder resources.
     */
    private void cleanup() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.release();
                mediaRecorder = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error releasing MediaRecorder", e);
        }
        isRecording = false;
    }

    /**
     * Release all resources.
     */
    public void release() {
        cleanup();
    }
}
