package com.example.bookup.utils;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.os.VibrationEffect;

public class RingtonePlayer {
    private static RingtonePlayer instance;
    private Ringtone ringtone;
    private Vibrator vibrator;
    private final Context context;

    private RingtonePlayer(Context context) {
        this.context = context.getApplicationContext();
        this.vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public static synchronized RingtonePlayer getInstance(Context context) {
        if (instance == null) {
            instance = new RingtonePlayer(context);
        }
        return instance;
    }

    public void startRinging() {
        if (ringtone != null && ringtone.isPlaying()) return;

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(context, notification);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone.setLooping(true);
        }
        
        ringtone.play();

        if (vibrator != null) {
            long[] pattern = {0, 1000, 1000};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0);
            }
        }
    }

    public void stopRinging() {
        if (ringtone != null) {
            ringtone.stop();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}
