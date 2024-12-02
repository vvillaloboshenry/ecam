package com.example.ecam.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.example.ecam.R;
import com.example.ecam.util.VibrationUtils;

public class MovableOverlayView extends FrameLayout {
    private float dX, dY;
    private boolean isMovable = false;
    private GestureDetector gestureDetector;
    private Context mContext;
    private TransparentAreaView transparentAreaView;
    private OnTouchListener externalTouchListener;
    private static float height_overlay;
    VibrationUtils vibrationUtils;

    public MovableOverlayView(Context mContext, TransparentAreaView transparentAreaView) {
        super(mContext);
        this.mContext = mContext;
        this.transparentAreaView = transparentAreaView;
        init();
    }

    public MovableOverlayView(Context mContext, AttributeSet attrs) {
        super(mContext, attrs);
        this.mContext = mContext;
        init();
    }

    public MovableOverlayView(Context mContext, AttributeSet attrs, int defStyleAttr) {
        super(mContext, attrs, defStyleAttr);
        this.mContext = mContext;
        init();
    }

    private void init() {
        setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.attr_background_objeto_camara, null));
        addOverlayTextView();
        setupVibrator();
        setupGestureDetector();
        setupTouchListener();
        setupLayoutListener();
    }

    private void addOverlayTextView() {
        TextView textView = new TextView(getContext());
        textView.setBackgroundResource(R.drawable.attr_background_texto_camara);
        textView.setText("La lectura debe ir dentro del recuadro");
        textView.setTextSize(13f);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setLetterSpacing(0.07f);
        textView.setPadding(0, 2, 0, 0);
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER_HORIZONTAL;
        textView.setLayoutParams(params);
        addView(textView);
    }

    private void setupVibrator() {
        vibrationUtils = VibrationUtils.getInstance(mContext);
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent event) {
                isMovable = true;
                dX = getX() - event.getRawX();
                dY = getY() - event.getRawY();
                vibrationUtils.vibrateClicked();
            }

            @Override
            public boolean onDown(MotionEvent e) {
                if (externalTouchListener != null) {
                    MotionEvent adjustedEvent = MotionEvent.obtain(e);
                    adjustedEvent.setLocation(e.getRawX() - 10, e.getRawY() - 230);
                    externalTouchListener.onTouch(null, adjustedEvent);
                    adjustedEvent.recycle();
                }
                return true;
            }
        });
    }

    private void setupTouchListener() {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }

                if (event.getAction() == MotionEvent.ACTION_MOVE && isMovable) {
                    float newX = event.getRawX() + dX;
                    float miPosicionY = event.getRawY() + dY;
                    float limite_top = OverlayView.DEFAULT_LIMIT_OVERLAY_POSITION_IN_Y_TOP;
                    float limite_bot = ((View) getParent()).getHeight() - (height_overlay + OverlayView.DEFAULT_LIMIT_OVERLAY_POSITION_IN_Y_BOT);

                    if (miPosicionY <= limite_bot && miPosicionY >= limite_top) {
                        updatePositionView(view, OverlayView.DEFAULT_LIMIT_OVERLAY_POSITION_IN_X, miPosicionY);
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    isMovable = false;
                }
                return false;
            }
        });
    }

    private void updatePositionView(View v, float positionX, float positionY) {
        if (v != null) {
            v.animate()
                    .x(positionX)
                    .y(positionY)
                    .setDuration(0)
                    .start();
        }
        updateTransparentRect(positionX, positionY);
    }

    private void setupLayoutListener() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                adjustSizeLayout();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void adjustSizeLayout() {
        View parent = (View) getParent();
        if (parent != null) {
            LayoutParams layoutParams = (LayoutParams) getLayoutParams();
            height_overlay = parent.getHeight() * OverlayView.DEFAULT_OVERLAY_SIZE_HEIGHT_BASED_PERCENTAGE;
            layoutParams.width = (int) (parent.getWidth() - (OverlayView.DEFAULT_LIMIT_OVERLAY_POSITION_IN_X * 2));
            layoutParams.height = (int) (parent.getHeight() * OverlayView.DEFAULT_OVERLAY_SIZE_HEIGHT_BASED_PERCENTAGE);
            layoutParams.gravity = Gravity.CENTER;
            setLayoutParams(layoutParams);
        }
    }

    public void setTouchListener(OnTouchListener listener) {
        this.externalTouchListener = listener;
    }

    private void updateTransparentRect(float left, float top) {
        if (transparentAreaView != null) {
            float right = left + getWidth();
            float bottom = top + getHeight();
            transparentAreaView.setTransparentRect(new RectF(left, top, right, bottom));
        }
    }
}