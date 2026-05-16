package com.example.bookup.helpers;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class VoiceRecorder {
    private static final String TAG = "VoiceRecorder";
    private MediaRecorder recorder;
    private String fileName;
    private final Context context;
    private boolean isPaused = false;

    public VoiceRecorder(Context context) {
        this.context = context;
    }

    public void startRecording() {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) cacheDir = context.getCacheDir();
        fileName = cacheDir.getAbsolutePath() + "/audiorecord.3gp";
        
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
            recorder.start();
            isPaused = false;
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed", e);
        }
    }

    public void pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && recorder != null && !isPaused) {
            try {
                recorder.pause();
                isPaused = true;
            } catch (RuntimeException e) {
                Log.e(TAG, "pause() failed", e);
            }
        }
    }

    public void resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && recorder != null && isPaused) {
            try {
                recorder.resume();
                isPaused = false;
            } catch (RuntimeException e) {
                Log.e(TAG, "resume() failed", e);
            }
        }
    }

    public File stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException e) {
                // handle cleanup
            } finally {
                recorder.release();
                recorder = null;
            }
            return new File(fileName);
        }
        return null;
    }

    public boolean isPaused() {
        return isPaused;
    }
}
