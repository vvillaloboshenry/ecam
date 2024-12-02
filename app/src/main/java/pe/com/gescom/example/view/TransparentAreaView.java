package pe.com.gescom.example.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import pe.com.gescom.ecam.R;

public class TransparentAreaView extends View {
    private Paint paint;
    private RectF transparentRectF;
    private Path path;
    private float cornerRadius = 18f;
    private Canvas mCanvas;
    private int type_background_for_overlay = OverlayView.BACKGROUND_TRANSPARENT;
    private boolean isCanvasReady = false;

    public TransparentAreaView(Context context) {
        super(context);
        init();
    }

    public TransparentAreaView(Context context, @OverlayView.OverlayType int type) {
        super(context);
        init();
        setTypeOverlay(type);
    }

    public TransparentAreaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TransparentAreaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        path = new Path();
    }

    public int getTypeOverlay() {
        return this.type_background_for_overlay;
    }

    public void setTypeOverlay(@OverlayView.OverlayType int type) {
        type_background_for_overlay = type;
        if (isCanvasReady) {
            setBackgroundOverlay(type_background_for_overlay);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        isCanvasReady = true;

        if (transparentRectF == null) {
            initializeTransparentRect();
        }

        path.reset();
        path.addRoundRect(transparentRectF, cornerRadius, cornerRadius, Path.Direction.CW);
        setBackgroundOverlay(type_background_for_overlay);
        mCanvas.drawPath(path, paint);
    }

    private void initializeTransparentRect() {
        float rectWidth = getWidth();
        float rectHeight = getHeight() * OverlayView.DEFAULT_OVERLAY_SIZE_HEIGHT_BASED_PERCENTAGE;
        float left = ((getWidth() - rectWidth) / 2);
        float top = ((getHeight() - rectHeight) / 2);
        float right = left + rectWidth;
        float bottom = top + rectHeight;
        transparentRectF = new RectF(left + OverlayView.DEFAULT_LIMIT_OVERLAY_POSITION_IN_X, top, right - OverlayView.DEFAULT_LIMIT_OVERLAY_POSITION_IN_X, bottom);
    }

    private void setBackgroundOverlay(int val) {
        if (mCanvas == null) {
            return;
        }
        switch (val) {
            case OverlayView.BACKGROUND_GONE:
                mCanvas.drawColor(0x00000000);
                break;
            case OverlayView.BACKGROUND_TRANSPARENT:
                mCanvas.drawColor(ContextCompat.getColor(getContext(), R.color.overlay_transparent));
                break;
            case OverlayView.BACKGROUND_OPACO:
                mCanvas.drawColor(ContextCompat.getColor(getContext(), R.color.overlay_opaco));
                break;
            case OverlayView.BACKGROUND_FUNNY:
                break;
        }
        invalidate();
    }

    public void setTransparentRect(RectF rect) {
        transparentRectF = rect;
        invalidate();
    }

    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        invalidate();
    }
}
