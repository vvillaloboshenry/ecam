package pe.com.gescom.example.view;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

final class Tag {
    public static final int LECTURA = 1;
    public static final int MEDIDOR = 2;
}

final class ZoomLevel {
    public static final int ZOOM_0 = 5; // 0 -10
    public static final int ZOOM_1 = 15; // 10 -20
    public static final int ZOOM_2 = 25; // 20 - 30
    public static final int ZOOM_4 = 45; // 40 - 50
    public static final int ZOOM_10 = 95; // 90 - 99
}

final class OverlayView {
    public static final int BACKGROUND_GONE = 0;
    public static final int BACKGROUND_TRANSPARENT = 1;
    public static final int BACKGROUND_OPACO = 2;
    public static final int BACKGROUND_FUNNY = 3;

    public static float DEFAULT_LIMIT_OVERLAY_POSITION_IN_Y_TOP = 160f;
    public static float DEFAULT_LIMIT_OVERLAY_POSITION_IN_Y_BOT = 210f;
    public static float DEFAULT_LIMIT_OVERLAY_POSITION_IN_X = 30f;
    public static float DEFAULT_OVERLAY_SIZE_HEIGHT_BASED_PERCENTAGE = 0.18f;

    @IntDef({BACKGROUND_GONE, BACKGROUND_TRANSPARENT, BACKGROUND_OPACO, BACKGROUND_FUNNY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OverlayType {
    }
}

