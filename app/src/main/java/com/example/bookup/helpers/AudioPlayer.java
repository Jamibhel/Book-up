package com.example.bookup.helpers;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

public class AudioPlayer {
    private static final String TAG = "AudioPlayer";
    private MediaPlayer mediaPlayer;
    private final Handler handler = new Handler();
    private Runnable progressRunnable;
    private OnProgressListener progressListener;
    private String currentUrl;

    public interface OnProgressListener {
        void onProgress(int current, int total);
        void onStateChanged(boolean isPlaying);
        void onFinished();
        void onError(String error);
    }

    public void playAudio(String url, OnProgressListener listener) {
        if (url == null || url.isEmpty()) return;

        if (url.equals(currentUrl) && mediaPlayer != null) {
            togglePausePlay();
            return;
        }

        stopAudio();
        this.currentUrl = url;
        this.progressListener = listener;
        mediaPlayer = new MediaPlayer();
        
        mediaPlayer.setAudioAttributes(
            new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        );

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                if (progressListener != null) progressListener.onStateChanged(true);
                startProgressUpdate();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                String error = "MediaPlayer Error: " + what;
                if (progressListener != null) progressListener.onError(error);
                return false;
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                if (progressListener != null) {
                    progressListener.onStateChanged(false);
                    progressListener.onFinished();
                }
                stopAudio();
            });
        } catch (IOException e) {
            if (progressListener != null) progressListener.onError(e.getMessage());
        }
    }

    private void startProgressUpdate() {
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    if (progressListener != null) {
                        progressListener.onProgress(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
                    }
                    handler.postDelayed(this, 200);
                }
            }
        };
        handler.post(progressRunnable);
    }

    public void togglePausePlay() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                handler.removeCallbacks(progressRunnable);
                if (progressListener != null) progressListener.onStateChanged(false);
            } else {
                mediaPlayer.start();
                startProgressUpdate();
                if (progressListener != null) progressListener.onStateChanged(true);
            }
        }
    }

    public void stopAudio() {
        handler.removeCallbacks(progressRunnable);
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (Exception e) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
        currentUrl = null;
    }

    public boolean isPlaying(String url) {
        return mediaPlayer != null && mediaPlayer.isPlaying() && url.equals(currentUrl);
    }
}
