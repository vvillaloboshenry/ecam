package com.example.ecam.util;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

import java.lang.ref.WeakReference;

public class VibrationUtils {
    private static VibrationUtils instance;
    private final WeakReference<Vibrator> vibratorRef;
    private static final long VIBRATION_DURATION_SHORT = 250L;
    private static final long[] VIBRATION_PATTERN_CLICKED = {0, 40, 40, 40, 0};

    private VibrationUtils(Vibrator vibrator) {
        this.vibratorRef = new WeakReference<>(vibrator);
    }

    public static synchronized VibrationUtils getInstance(Context context) {
        if (instance == null) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            instance = new VibrationUtils(vibrator);
        }
        return instance;
    }

    public void vibrate(long milliseconds) {
        Vibrator vibrator = vibratorRef.get();
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(milliseconds);
            }
        }
    }

    public void vibrate(long[] pattern) {
        Vibrator vibrator = vibratorRef.get();
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                vibrator.vibrate(pattern, -1);
            }
        }
    }

    public void vibrateShort() {
        Vibrator vibrator = vibratorRef.get();
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION_SHORT, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(VIBRATION_DURATION_SHORT);
            }
        }
    }

    public void vibrateClicked() {
        Vibrator vibrator = vibratorRef.get();
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN_CLICKED, -1));
            } else {
                vibrator.vibrate(VIBRATION_PATTERN_CLICKED, -1);
            }
        }
    }
}
