package pe.com.gescom.ecam.view;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import pe.com.gescom.ecam.R;
import pe.com.gescom.ecam.util.CameraUtils;
import pe.com.gescom.ecam.util.Constantes;
import pe.com.gescom.ecam.util.VibrationUtils;

public class CamaraActivity extends AppCompatActivity implements View.OnClickListener, OrientationChangeListener, View.OnLongClickListener {
    public static final String TAG_CLASS = "CamaraActivity";
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int calidad = 95;
    public static boolean esSeguroTomarFoto = false;
    private static boolean retorneUnaFoto = false;

    private ImageView ivFlashCamara, focusCamera;
    private ShapeableImageView ivTomarFoto;
    private TextView titleActivity;

    // Menu Zoom Container 1
    private CardView cvZoomContainer1;
    private CardView cvMenu1ZoomX0, cvMenu1ZoomX1, cvMenu1ZoomX2;
    private TextView tvMenu1ZoomX0, tvMenu1ZoomX1, tvMenu1ZoomX2;
    private TextView tvMenu1ZoomX0_x, tvMenu1ZoomX1_x, tvMenu1ZoomX2_x;

    // Menu Zoom Container 2
    private LinearLayout llFooterContainer1;
    private CardView cvMenu2ZoomX0, cvMenu2ZoomX1, cvMenu2ZoomX2, cvMenu2ZoomX4, cvMenu2ZoomX10;
    private TextView tvMenu2ZoomX0, tvMenu2ZoomX1, tvMenu2ZoomX2, tvMenu2ZoomX4, tvMenu2ZoomX10;
    List<Integer> cardViewListZoom = new ArrayList<>();

    private FrameLayout frameOverlayLayer, frameCamPreviewLayer, frameFocusLayer;

    // Menu Modos de Camara
    private LinearLayout llFooterContainer2;
    private CardView cvModoCamaraDivertido, cvModoCamaraDespejado, cvModoCamaraTransparente, cvModoCamaraConcentracion;

    private ImageView ivCerrarCamara;

    private MovableOverlayView movableOverlayView;
    private TransparentAreaView transparentAreaView;
    private ScaleGestureDetector zoomGestureDetector;

    private static final int MENU1_SELECTED_COLOR = Color.parseColor("#48000000");
    private static final int MENU1_NO_SELECTED_COLOR = Color.parseColor("#00000000");

    private static final int MENU2_SELECTED_COLOR_CV = Color.parseColor("#2A2929");
    private static final int MENU2_NO_SELECTED_COLOR_CV = Color.parseColor("#00000000");

    private static final int MENU2_SELECTED_COLOR_TEXT = Color.parseColor("#f9d71c");
    private static final int MENU2_NO_SELECTED_COLOR_TEXT = Color.parseColor("#FFFFFF");

    private static final float MENU1_SELECTED_TEXT_SIZE = 12.5f;
    private static final float MENU1_NO_SELECTED_TEXT_SIZE = 12f;

    private Context context;
    private Constantes constantes;
    private Camera mCamera;
    private CameraPreview mirrorView;
    private VibrationUtils vibrationUtils;

    private OrientationEventListener orientationEventListener;

    private int backFacingCameraId = 0;
    private boolean defaultTagVisibility = true;
    private int defaultTagTitle = 1;

    public void showTagTitle(boolean tagVisibility) {
        this.defaultTagVisibility = tagVisibility;
    }

    public void tagTitle(int tagTitle) {
        this.defaultTagTitle = tagTitle;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camara);
        Log.i(TAG_CLASS, "onCreate Called");

        // Inicializar el OrientationEventListener
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if ((orientation >= 0 && orientation < 35) || (orientation >= 325)) {
                    onPortrait();
                } else {
                    onLandscape();
                }
            }
        };

        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
        } else {
            onPortrait();
            Log.d(TAG_CLASS, "El dispositivo no soporta la detección de orientación");
            orientationEventListener.disable();
        }

        context = this;
        constantes = new Constantes(this);
        initView();
        initEventsView();
        if (!Constantes.checkPermissions(this, Constantes.PERMISSIONS_CAMERA)) {
            ActivityCompat.requestPermissions(this, Constantes.PERMISSIONS_CAMERA, Constantes.CODE_REQUEST_PERMISSIONS_CAMERA);
        }
        setupVibrator();
        setupCamera();
        starDemo();
    }

    private void initView() {
        // Layers Camara
        frameOverlayLayer = findViewById(R.id.flOverlayLayer);
        frameFocusLayer = findViewById(R.id.flFocusLayer);
        frameCamPreviewLayer = findViewById(R.id.flCamPreviewLayer);

        // inicio: menu zoom
        cvZoomContainer1 = findViewById(R.id.cvZoomContainer1);

        cvMenu1ZoomX0 = findViewById(R.id.cvMenu1ZoomX0);
        cvMenu1ZoomX1 = findViewById(R.id.cvMenu1ZoomX1);
        cvMenu1ZoomX2 = findViewById(R.id.cvMenu1ZoomX2);

        tvMenu1ZoomX0 = findViewById(R.id.tvMenu1ZoomX0);
        tvMenu1ZoomX1 = findViewById(R.id.tvMenu1ZoomX1);
        tvMenu1ZoomX2 = findViewById(R.id.tvMenu1ZoomX2);

        tvMenu1ZoomX0_x = findViewById(R.id.tvMenu1ZoomX0_x);
        tvMenu1ZoomX1_x = findViewById(R.id.tvMenu1ZoomX1_x);
        tvMenu1ZoomX2_x = findViewById(R.id.tvMenu1ZoomX2_x);

        // inicio: menu zoom
        llFooterContainer1 = findViewById(R.id.llFooterContainer1);

        cvMenu2ZoomX0 = findViewById(R.id.cvMenu2ZoomX0);
        cvMenu2ZoomX1 = findViewById(R.id.cvMenu2ZoomX1);
        cvMenu2ZoomX2 = findViewById(R.id.cvMenu2ZoomX2);
        cvMenu2ZoomX4 = findViewById(R.id.cvMenu2ZoomX4);
        cvMenu2ZoomX10 = findViewById(R.id.cvMenu2ZoomX10);

        tvMenu2ZoomX0 = findViewById(R.id.tvMenu2ZoomX0);
        tvMenu2ZoomX1 = findViewById(R.id.tvMenu2ZoomX1);
        tvMenu2ZoomX2 = findViewById(R.id.tvMenu2ZoomX2);
        tvMenu2ZoomX4 = findViewById(R.id.tvMenu2ZoomX4);
        tvMenu2ZoomX10 = findViewById(R.id.tvMenu2ZoomX10);

        // agrego mi lista de botones zoom
        cardViewListZoom.add(R.id.cvMenu1ZoomX0);
        cardViewListZoom.add(R.id.cvMenu1ZoomX1);
        cardViewListZoom.add(R.id.cvMenu1ZoomX2);
        cardViewListZoom.add(R.id.cvMenu2ZoomX0);
        cardViewListZoom.add(R.id.cvMenu2ZoomX1);
        cardViewListZoom.add(R.id.cvMenu2ZoomX2);
        cardViewListZoom.add(R.id.cvMenu2ZoomX4);
        cardViewListZoom.add(R.id.cvMenu2ZoomX10);

        ivCerrarCamara = findViewById(R.id.ivCerrarCamara);

        titleActivity = findViewById(R.id.titleActivity);
        setTagTitle(this.defaultTagTitle);
        setVisibilityTitleActivity(this.defaultTagVisibility);
        ivTomarFoto = findViewById(R.id.ivTomarFoto);

        ivFlashCamara = findViewById(R.id.ivFlashCamara);
        ivFlashCamara.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_flash_off, null));

        // Agrego el Overlay a la actividad
        agregarOverlay();

        llFooterContainer2 = findViewById(R.id.llFooterContainer2);
        cvModoCamaraDivertido = findViewById(R.id.cvModoCamaraDivertido);
        cvModoCamaraDespejado = findViewById(R.id.cvModoCamaraDespejado);
        cvModoCamaraTransparente = findViewById(R.id.cvModoCamaraTransparente);
        cvModoCamaraConcentracion = findViewById(R.id.cvModoCamaraConcentracion);
        llFooterContainer2.setVisibility(View.VISIBLE);

        setupStyleDefaultMenuZoom();
        setupStyleDefaultModosCamara();
    }

    //Normal, Buena, Muy buena
    private int quality;
    private CameraManager.ReturnType returnType;

    public void starDemo() {
        if (getInstanceMirrorView() != null && getInstanceMirrorView().isZoomSupported()) {
            mirrorView.applyZoom(ZoomLevel.ZOOM_1);
        }

        Intent intent = getIntent();
        quality = intent.getIntExtra("quality", CameraManager.QUALITY_DEFAULT);
        String returnTypeStr = intent.getStringExtra("returnType");
        if (returnTypeStr != null) {
            returnType = CameraManager.ReturnType.valueOf(returnTypeStr);
        } else {
            returnType = CameraManager.ReturnType.BITMAP;
        }
    }

    public void agregarOverlay() {
        // Crear la vista transparente
        transparentAreaView = new TransparentAreaView(context);
        frameOverlayLayer.addView(transparentAreaView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        // Crear la vista movible
        movableOverlayView = new MovableOverlayView(context, transparentAreaView);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        frameOverlayLayer.addView(movableOverlayView, params);

        frameOverlayLayer.setOnTouchListener((v, event) -> {
            if (event.getPointerCount() > 1) { // Es un Zoom Touch
                zoomGestureDetector.onTouchEvent(event);
            } else {
                if (event.getAction() == MotionEvent.ACTION_DOWN) { // Es un Focus Touch
                    focusTouchEvent(event);
                }
            }
            return true;
        });

        movableOverlayView.setTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                focusTouchEvent(event);
            }
            return false;
        });
    }

    public void setupStyleDefaultMenuZoom() {
        cvZoomContainer1.setVisibility(View.VISIBLE);
        cvMenu1ZoomX1.setCardBackgroundColor(MENU1_SELECTED_COLOR);
        tvMenu1ZoomX1.setText("1");
        tvMenu1ZoomX1.setTextSize(TypedValue.COMPLEX_UNIT_SP, MENU1_SELECTED_TEXT_SIZE);
        tvMenu1ZoomX1_x.setVisibility(View.VISIBLE);
        llFooterContainer1.setVisibility(View.INVISIBLE);
    }

    public void setupStyleDefaultModosCamara() {
        cvModoCamaraTransparente.setCardBackgroundColor(MENU2_SELECTED_COLOR_CV);
    }

    private void setupVibrator() {
        vibrationUtils = VibrationUtils.getInstance(context);
    }

    private void initEventsView() {
        ivTomarFoto.setOnClickListener(this);
        ivTomarFoto.setOnLongClickListener(this);

        // eventos zooms
        cvMenu1ZoomX0.setOnClickListener(this);
        cvMenu1ZoomX0.setOnLongClickListener(this);
        cvMenu1ZoomX1.setOnClickListener(this);
        cvMenu1ZoomX1.setOnLongClickListener(this);
        cvMenu1ZoomX2.setOnClickListener(this);
        cvMenu1ZoomX2.setOnLongClickListener(this);
        cvMenu2ZoomX0.setOnClickListener(this);
        cvMenu2ZoomX0.setOnLongClickListener(this);
        cvMenu2ZoomX1.setOnClickListener(this);
        cvMenu2ZoomX1.setOnLongClickListener(this);
        cvMenu2ZoomX2.setOnClickListener(this);
        cvMenu2ZoomX2.setOnLongClickListener(this);
        cvMenu2ZoomX4.setOnClickListener(this);
        cvMenu2ZoomX4.setOnLongClickListener(this);
        cvMenu2ZoomX10.setOnClickListener(this);
        cvMenu2ZoomX10.setOnLongClickListener(this);

        ivFlashCamara.setOnClickListener(this);
        ivFlashCamara.setOnLongClickListener(this);

        cvModoCamaraDivertido.setOnClickListener(this);
        cvModoCamaraDespejado.setOnClickListener(this);
        cvModoCamaraTransparente.setOnClickListener(this);
        cvModoCamaraConcentracion.setOnClickListener(this);

        ivCerrarCamara.setOnClickListener(this);
        zoomGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
    }

    @Override
    public void onPortrait() {
        findViewById(R.id.ivIconoAdvertencia).setVisibility(View.GONE);
        findViewById(R.id.ivMensajeAdvertencia).setVisibility(View.GONE);
        ivTomarFoto.setEnabled(true);
    }

    @Override
    public void onLandscape() {
        findViewById(R.id.ivIconoAdvertencia).setVisibility(View.VISIBLE);
        findViewById(R.id.ivMensajeAdvertencia).setVisibility(View.VISIBLE);
        ivTomarFoto.setEnabled(false);
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.ivTomarFoto) {
            Animation shake = AnimationUtils.loadAnimation(context, R.anim.anim_shake_for_take_photo);
            v.startAnimation(shake);
            new Handler().postDelayed(() -> vibrationUtils.vibrateClicked(), 50);
        } else {
            vibrationUtils.vibrateClicked();
        }
        return false;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            super.onScale(detector);
            float scaleFactor = detector.getScaleFactor();

            if (getInstanceMirrorView() != null) {
                mirrorView.applyZoom(scaleFactor);
            }
            return true;
        }
    }

    public void focusTouchEvent(MotionEvent event) {
        crearImagenFocusCamara(event);
        iniciarAnimacionEstablecerFocus();
    }

    // Método para crear una nueva imagen de enfoque
    private void crearImagenFocusCamara(MotionEvent event) {
        if (focusCamera != null) {
            frameFocusLayer.removeView(focusCamera);
        }

        float x = event.getX();
        float y = event.getY();

        focusCamera = new ImageView(context);
        focusCamera.setImageResource(R.drawable.ic_focus_camara);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) x - 40;
        layoutParams.topMargin = (int) y - 40;
        focusCamera.setLayoutParams(layoutParams);

        frameFocusLayer.addView(focusCamera);
    }

    private void iniciarAnimacionEstablecerFocus() {
        // Crear la animación compuesta (escala y fade in)
        AnimationSet animationSet = new AnimationSet(true);

        // Animación de escala
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(500);

        // Animación de fade in
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(500);

        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);

        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                alternarEstabilizacionCamara();
                focusCamara();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                iniciarAnimacionDesvanecerFocus();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        // Iniciar la animación
        focusCamera.startAnimation(animationSet);
    }

    private void iniciarAnimacionDesvanecerFocus() {
        Animation animacionDesvanecer = AnimationUtils.loadAnimation(context, R.anim.anim_desvanecer_for_focus_cam);
        animacionDesvanecer.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                frameFocusLayer.removeView(focusCamera);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        focusCamera.startAnimation(animacionDesvanecer);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constantes.CODE_REQUEST_PERMISSIONS_CAMERA) {
            List<Integer> listPermissions = new ArrayList<>();
            for (int i : grantResults) listPermissions.add(i);
            if (listPermissions.contains((Integer) PackageManager.PERMISSION_DENIED))
                ActivityCompat.requestPermissions(this, Constantes.PERMISSIONS_CAMERA, Constantes.CODE_REQUEST_PERMISSIONS_CAMERA);
        }
    }

    private void setupCamera() {
        backFacingCameraId = CameraUtils.findFirstBackFacingCamera();
        if (backFacingCameraId == -1) {
            Toast.makeText(context, "No se encontro una camara trasera", Toast.LENGTH_SHORT).show();
            return;
        }
        iniciarCamara();
    }

    private boolean isAppInForeground() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
    }

    private void iniciarCamara() {
        try {
            if (isAppInForeground()) {
                mCamera = Camera.open(backFacingCameraId);
                if (getInstanceCamera() != null) {
                    if (getInstanceMirrorView() == null) {
                        mirrorView = new CameraPreview(this, mCamera);
                    }
                    frameCamPreviewLayer.removeAllViews();
                    frameCamPreviewLayer.addView(mirrorView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CameraPreview getInstanceMirrorView() {
        return mirrorView;
    }

    public Camera getInstanceCamera() {
        return mCamera;
    }

    public boolean esSeguroTomarFoto() {
        return esSeguroTomarFoto;
    }

    private final Handler zoomOptionsHandler = new Handler();
    private Runnable zoomOptionsRunnable = new Runnable() {
        @Override
        public void run() {
            llFooterContainer1.setVisibility(View.GONE);
            llFooterContainer2.setVisibility(View.VISIBLE);
        }
    };

    private static final long DELAY_MILLIS = 2000;

    @Override
    public void onClick(View v) {
        if (cardViewListZoom.contains(v.getId())) {
            llFooterContainer1.setVisibility(View.VISIBLE);
            llFooterContainer2.setVisibility(View.GONE);

            // Cancelar cualquier acción pendiente de ocultar el contenedor de zoom
            if (zoomOptionsRunnable != null) {
                zoomOptionsHandler.removeCallbacks(zoomOptionsRunnable);
            }

            zoomOptionsHandler.postDelayed(zoomOptionsRunnable, DELAY_MILLIS);

            cvMenu1ZoomX0.setCardBackgroundColor(MENU1_NO_SELECTED_COLOR);
            tvMenu1ZoomX0.setText(".5");
            tvMenu1ZoomX0.setTextSize(TypedValue.COMPLEX_UNIT_SP, MENU1_NO_SELECTED_TEXT_SIZE);
            tvMenu1ZoomX0_x.setVisibility(View.GONE);

            cvMenu1ZoomX1.setCardBackgroundColor(MENU1_NO_SELECTED_COLOR);
            tvMenu1ZoomX1.setText("1");
            tvMenu1ZoomX1.setTextSize(TypedValue.COMPLEX_UNIT_SP, MENU1_NO_SELECTED_TEXT_SIZE);
            tvMenu1ZoomX1_x.setVisibility(View.GONE);

            cvMenu1ZoomX2.setCardBackgroundColor(MENU1_NO_SELECTED_COLOR);
            tvMenu1ZoomX2.setText("2");
            tvMenu1ZoomX2.setTextSize(TypedValue.COMPLEX_UNIT_SP, MENU1_NO_SELECTED_TEXT_SIZE);
            tvMenu1ZoomX2_x.setVisibility(View.GONE);

            cvMenu2ZoomX0.setCardBackgroundColor(MENU2_NO_SELECTED_COLOR_CV);
            tvMenu2ZoomX0.setTextColor(MENU2_NO_SELECTED_COLOR_TEXT);
            cvMenu2ZoomX1.setCardBackgroundColor(MENU2_NO_SELECTED_COLOR_CV);
            tvMenu2ZoomX1.setTextColor(MENU2_NO_SELECTED_COLOR_TEXT);
            cvMenu2ZoomX2.setCardBackgroundColor(MENU2_NO_SELECTED_COLOR_CV);
            tvMenu2ZoomX2.setTextColor(MENU2_NO_SELECTED_COLOR_TEXT);
            cvMenu2ZoomX4.setCardBackgroundColor(MENU2_NO_SELECTED_COLOR_CV);
            tvMenu2ZoomX4.setTextColor(MENU2_NO_SELECTED_COLOR_TEXT);
            cvMenu2ZoomX10.setCardBackgroundColor(MENU2_NO_SELECTED_COLOR_CV);
            tvMenu2ZoomX10.setTextColor(MENU2_NO_SELECTED_COLOR_TEXT);

            int id = v.getId();// menu lateral
            if (id == R.id.cvMenu1ZoomX0 || id == R.id.cvMenu2ZoomX0) {
                cvMenu1ZoomX0.setCardBackgroundColor(MENU1_SELECTED_COLOR);
                tvMenu1ZoomX0.setText("0.5");
                tvMenu1ZoomX0.setTextSize(TypedValue.COMPLEX_UNIT_SP, MENU1_SELECTED_TEXT_SIZE);
                tvMenu1ZoomX0_x.setVisibility(View.VISIBLE);

                cvMenu2ZoomX0.setCardBackgroundColor(MENU2_SELECTED_COLOR_CV);
                tvMenu2ZoomX0.setTextColor(MENU2_SELECTED_COLOR_TEXT);

                if (getInstanceMirrorView() != null && getInstanceMirrorView().isZoomSupported()) {
                    mirrorView.applyZoom(ZoomLevel.ZOOM_0);
                }
            } else if (id == R.id.cvMenu1ZoomX1 || id == R.id.cvMenu2ZoomX1) {
                cvMenu1ZoomX1.setCardBackgroundColor(MENU1_SELECTED_COLOR);
                tvMenu1ZoomX1.setText("1");
                tvMenu1ZoomX1.setTextSize(TypedValue.COMPLEX_UNIT_SP, MENU1_SELECTED_TEXT_SIZE);
                tvMenu1ZoomX1_x.setVisibility(View.VISIBLE);

                cvMenu2ZoomX1.setCardBackgroundColor(MENU2_SELECTED_COLOR_CV);
                tvMenu2ZoomX1.setTextColor(MENU2_SELECTED_COLOR_TEXT);

                if (getInstanceMirrorView() != null && getInstanceMirrorView().isZoomSupported()) {
                    mirrorView.applyZoom(ZoomLevel.ZOOM_1);
                }
            } else if (id == R.id.cvMenu1ZoomX2 || id == R.id.cvMenu2ZoomX2) {
                cvMenu1ZoomX2.setCardBackgroundColor(MENU1_SELECTED_COLOR);
                tvMenu1ZoomX2.setText("2");
                tvMenu1ZoomX2.setTextSize(TypedValue.COMPLEX_UNIT_SP, MENU1_SELECTED_TEXT_SIZE);
                tvMenu1ZoomX2_x.setVisibility(View.VISIBLE);

                cvMenu2ZoomX2.setCardBackgroundColor(MENU2_SELECTED_COLOR_CV);
                tvMenu2ZoomX2.setTextColor(MENU2_SELECTED_COLOR_TEXT);

                if (getInstanceMirrorView() != null && getInstanceMirrorView().isZoomSupported()) {
                    mirrorView.applyZoom(ZoomLevel.ZOOM_2);
                }
            } else if (id == R.id.cvMenu2ZoomX4) {
                cvMenu1ZoomX2.setCardBackgroundColor(MENU1_SELECTED_COLOR);
                tvMenu1ZoomX2.setText("4");
                tvMenu1ZoomX2.setTextSize(TypedValue.COMPLEX_UNIT_SP, MENU1_SELECTED_TEXT_SIZE);
                tvMenu1ZoomX2_x.setVisibility(View.VISIBLE);

                cvMenu2ZoomX4.setCardBackgroundColor(MENU2_SELECTED_COLOR_CV);
                tvMenu2ZoomX4.setTextColor(MENU2_SELECTED_COLOR_TEXT);
                if (getInstanceMirrorView() != null && getInstanceMirrorView().isZoomSupported()) {
                    mirrorView.applyZoom(ZoomLevel.ZOOM_4);
                }
            } else if (id == R.id.cvMenu2ZoomX10) {
                cvMenu1ZoomX2.setCardBackgroundColor(MENU1_SELECTED_COLOR);
                tvMenu1ZoomX2.setText("10");
                tvMenu1ZoomX2.setTextSize(TypedValue.COMPLEX_UNIT_SP, MENU1_SELECTED_TEXT_SIZE);
                tvMenu1ZoomX2_x.setVisibility(View.VISIBLE);

                cvMenu2ZoomX10.setCardBackgroundColor(MENU2_SELECTED_COLOR_CV);
                tvMenu2ZoomX10.setTextColor(MENU2_SELECTED_COLOR_TEXT);

                if (getInstanceMirrorView() != null && getInstanceMirrorView().isZoomSupported()) {
                    mirrorView.applyZoom(ZoomLevel.ZOOM_10);
                }
            }
        } else {
            int id = v.getId();
            if (id == R.id.ivTomarFoto) {
                vibrationUtils.vibrateClicked();
                tomarFoto();
            } else if (id == R.id.ivFlashCamara) {
                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Toast.makeText(this, "Tu dispositivo no tiene flash.", Toast.LENGTH_SHORT).show();
                    return;
                }
                flashOnOff();
            } else if (id == R.id.ivCerrarCamara) {
                this.finish();
            } else if (id == R.id.cvModoCamaraDivertido) {
                cvModoCamaraDespejado.setCardBackgroundColor(MENU2_NO_SELECTED_COLOR_CV);
                cvModoCamaraTransparente.setCardBackgroundColor(MENU2_NO_SELECTED_COLOR_CV);
                cvModoCamaraConcentracion.setCardBackgroundColor(MENU2_NO_SELECTED_COLOR_CV);

                transparentAreaView.setTypeOverlay(OverlayView.BACKGROUND_FUNNY);
            } else if (id == R.id.cvModoCamaraDespejado) {
                cvModoCamaraDespejado.setCardBackgroundColor(MENU2_SELECTED_COLOR_CV);
                cvModoCamaraTransparente.setCardBackgroundColor(MENU2_NO_SELECTED_COLOR_CV);
                cvModoCamaraConcentracion.setCardBackgroundColor(MENU2_NO_SELECTED_COLOR_CV);

                transparentAreaView.setTypeOverlay(OverlayView.BACKGROUND_GONE);
            } else if (id == R.id.cvModoCamaraTransparente) {
                cvModoCamaraDespejado.setCardBackgroundColor(MENU2_NO_SELECTED_COLOR_CV);
                cvModoCamaraTransparente.setCardBackgroundColor(MENU2_SELECTED_COLOR_CV);
                cvModoCamaraConcentracion.setCardBackgroundColor(MENU2_NO_SELECTED_COLOR_CV);

                transparentAreaView.setTypeOverlay(OverlayView.BACKGROUND_TRANSPARENT);
            } else if (id == R.id.cvModoCamaraConcentracion) {
                cvModoCamaraDespejado.setCardBackgroundColor(MENU2_NO_SELECTED_COLOR_CV);
                cvModoCamaraTransparente.setCardBackgroundColor(MENU2_NO_SELECTED_COLOR_CV);
                cvModoCamaraConcentracion.setCardBackgroundColor(MENU2_SELECTED_COLOR_CV);

                transparentAreaView.setTypeOverlay(OverlayView.BACKGROUND_OPACO);
            }
        }
    }

    public void tomarFoto() {
        if (esSeguroTomarFoto() && getInstanceCamera() != null) {
            esSeguroTomarFoto = false;
            getInstanceCamera().takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    esSeguroTomarFoto = false;
                    Bitmap bitmap = null;
                    Bitmap resizeBitMap = null;
                    try {
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                        // Obtener la rotación correcta y rotar la imagen capturada
                        bitmap = rotarBitmap(bitmap, obtenerOrientacionCamara());

                        // Recortar la imagen para eliminar los márgenes
                        bitmap = recortarImagen(bitmap);

                        // Dibujar el cuadrado en la imagen
                        bitmap = dibujarRectangulo(bitmap);

                        // Redimensionar la imagen a 433x574 píxeles manteniendo la mejor calidad posible 250000
                        // Redimensionar la imagen a 480x623 píxeles manteniendo la mejor calidad posible 300000 (actual)
                        // Redimensionar la imagen a 613x814 píxeles manteniendo la mejor calidad posible 500000
                        resizeBitMap = resizeBitmap(bitmap, 250000);

                        capturarFoto(resizeBitMap);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        if (bitmap != null) {
                            bitmap.recycle();
                            bitmap = null;
                        }
                        releaseCamera();
                        finish();
                    }
                }
            });
        }
    }

    private void capturarFoto(Bitmap photo) {
        try {
            Intent resultIntent = new Intent();
            File file = null;
            switch (returnType) {
                case FILE_PATH:
                    file = saveBitmapToFile(photo, calidad);
                    resultIntent.putExtra("filePath", file.getAbsolutePath());
                    break;
                case BASE64:
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, calidad, stream);
                    String base64 = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
                    resultIntent.putExtra("base64", base64);
                    break;
                case FILE:
                    file = saveBitmapToFile(photo, calidad);
                    resultIntent.putExtra("file", file);
                    break;
                case BITMAP:
                    resultIntent.putExtra("bitmap", photo);
                    break;
            }
            retorneUnaFoto = true;
            setResult(Activity.RESULT_OK, resultIntent);
        } catch (Exception e) {
            retorneUnaFoto = false;
            setResult(Activity.RESULT_CANCELED);
        }
    }

    public void flashOnOff() {
        if (getInstanceMirrorView() != null) {
            if (getInstanceMirrorView().isFlashOn()) {
                apagarFlashCamara();
                ivFlashCamara.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_flash_off, null));
            } else {
                encenderFlashCamara();
                ivFlashCamara.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_flash_on, null));
            }
        }
    }

    private void masZoom() {
        if (getInstanceMirrorView() != null) {
            int zoom = getInstanceMirrorView().getCurrentZoom() + 7;
            mirrorView.applyZoom(zoom);
        }
    }

    private void menosZoom() {
        if (getInstanceMirrorView() != null) {
            int zoom = getInstanceMirrorView().getCurrentZoom() - 7;
            mirrorView.applyZoom(zoom);
        }
    }

    //https://slideplayer.com/slide/13237024/79/images/28/Drawing+a+Rectangle+canvas.drawRect+%2850%2C20%2C150%2C60%2Cpaint%29%3B+50+X+20+40.jpg
    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

        }
    };

    private void setVisibilityTitleActivity(boolean visibility) {
        if (visibility) {
            titleActivity.setVisibility(View.VISIBLE);
        } else {
            titleActivity.setVisibility(View.GONE);
        }
    }

    private void setTagTitle(int tagValue) {
        switch (tagValue) {
            case Tag.LECTURA:
                titleActivity.setText(R.string.toma_una_foto_a_la_lectura);
                break;
            case Tag.MEDIDOR:
                titleActivity.setText(R.string.toma_una_foto_al_medidor);
                break;
        }
    }

   /* @Override
    public boolean onTouch(View v, MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
         if (event.getPointerCount() > 1) {
         soy zoom
         }
        v.performClick();

        return false;
    }*/

    private void focusCamara() {
        if (getInstanceMirrorView() != null) {
            mirrorView.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); // Modo automatico de focus
        }
    }

    // Método para alternar la estabilización
    private void alternarEstabilizacionCamara() {
        if (getInstanceMirrorView() != null) {
            mirrorView.activateAutoStabilize(!getInstanceMirrorView().isAutoStabilizeEnabled());
        }
    }

    public Bitmap recortarImagen(Bitmap originalBitmap) {
        float porcentajeRecorteHorizontal = 0.13f;
        float porcentajeRecorteVertical = 0.13f;
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        // Calcular la cantidad de píxeles que representan el 10% de la altura total
        int margenRecorteVertical = (int) (height * porcentajeRecorteVertical);
        // Calcular la cantidad de píxeles que representan el 10% de la anchura total
        int margenRecorteHorizontal = (int) (width * porcentajeRecorteHorizontal);

        // Calcular las nuevas dimensiones de la imagen recortada
        int nuevaAltura = height - 2 * margenRecorteVertical;
        int nuevaAnchura = width - 2 * margenRecorteHorizontal;

        return Bitmap.createBitmap(originalBitmap, margenRecorteHorizontal, margenRecorteVertical, nuevaAnchura, nuevaAltura);
    }

    public File saveBitmapToFile(Bitmap bitmap, int quality) {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DCIM), System.currentTimeMillis() + ".jpeg");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public static Bitmap rotarBitmap(Bitmap source, int rotation) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * Resize version 1 usando scalado nativo android
     *
     * @param originalBitmap
     * @param maxPixels
     * @return
     */
    public Bitmap resizeBitmap(Bitmap originalBitmap, int maxPixels) {
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        // Calcular la relación de aspecto
        float aspectRatio = (float) originalWidth / originalHeight;

        // Calcular las nuevas dimensiones manteniendo la relación de aspecto
        int newWidth = (int) Math.sqrt(maxPixels * aspectRatio);
        int newHeight = (int) (newWidth / aspectRatio);

        // Redimensionar la imagen original con las nuevas dimensiones
        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }

    /**
     * Resize version 2 usando interpolacion bicubica
     *
     * @param originalBitmap
     * @param maxPixels
     * @return
     */
    public Bitmap resizeBitmapWithHighQuality(Bitmap originalBitmap, int maxPixels) {
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        // Calcular la relación de aspecto
        float aspectRatio = (float) originalWidth / originalHeight;

        // Calcular las nuevas dimensiones manteniendo la relación de aspecto
        int newWidth = (int) Math.sqrt(maxPixels * aspectRatio);
        int newHeight = (int) (newWidth / aspectRatio);

        // Redimensionar la imagen utilizando la interpolación bicúbica
        Rect srcRect = new Rect(0, 0, originalWidth, originalHeight);
        Rect destRect = new Rect(0, 0, newWidth, newHeight);

        // Crear un nuevo bitmap para la imagen redimensionada
        Bitmap resizedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        // Crear el objeto de pintura con la interpolación bicúbica activada, antialiasing y dithering
        Paint paint = new Paint();
        paint.setFilterBitmap(true); // interpolación bicúbica activada
        paint.setAntiAlias(true);  // Suaviza los bordes
        paint.setDither(true);     // Reduce el efecto de bandas en gradientes

        // Crear el canvas para dibujar la imagen redimensionada
        Canvas canvas = new Canvas(resizedBitmap);
        canvas.drawBitmap(originalBitmap, srcRect, destRect, paint);

        return resizedBitmap;
    }

    public Bitmap dibujarRectangulo(Bitmap bitmap) {
        // Obtener el tamaño y posición del Overlay en su contenedor
        Rect overlayRect = new Rect();
        movableOverlayView.getGlobalVisibleRect(overlayRect);

        float overlayRight = overlayRect.right;
        float overlayBottom = overlayRect.bottom;
        float overlayLeft = overlayRect.left;
        float overlayTop = overlayRect.top;

        // Obtener el tamaño del contenedor del Overlay
        ViewGroup container = (ViewGroup) movableOverlayView.getParent();
        float containerWidth = container.getWidth();
        float containerHeight = container.getHeight();

        // Obtener el tamaño del Bitmap
        float bitmapWidth = bitmap.getWidth();
        float bitmapHeight = bitmap.getHeight();

        // Calcular proporciones
        double widthRatio = ((double) bitmapWidth / containerWidth);
        double heightRatio = ((double) bitmapHeight / containerHeight);

        // Convertir coordenadas del Overlay a coordenadas del Bitmap
        double bitmapLeft = overlayLeft * widthRatio;
        double bitmapTop = overlayTop * heightRatio;
        double bitmapRight = overlayRight * widthRatio;
        double bitmapBottom = overlayBottom * heightRatio;

        // Dibujar el rectángulo en la imagen
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect((float) bitmapLeft, (float) bitmapTop, (float) bitmapRight, (float) bitmapBottom, paint);

        return bitmap;
    }

    private int obtenerOrientacionCamara() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(backFacingCameraId, info);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        return CameraUtils.getDisplayOrientation(rotation, info);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    public void encenderFlashCamara() {
        if (getInstanceMirrorView() != null) {
            getInstanceMirrorView().turnOnFlash();
        }
    }

    public void apagarFlashCamara() {
        if (getInstanceMirrorView() != null) {
            getInstanceMirrorView().turnOffFlash();
        }
    }

    protected void onResume() {
        super.onResume();
        Log.i(TAG_CLASS, "onResume Called");
        if (getInstanceCamera() == null) {
            iniciarCamara();
        }
    }

    @Override
    protected void onPause() { // La actividad actual aun es visible parcialmente por el usuario
        super.onPause();
        Log.i(TAG_CLASS, "onPause Called");
    }

    @Override
    protected void onStop() { // la actividad actual ya no es visible para el usuario, inicia una nueva actividad o cambia a otra aplicación
        super.onStop();
        Log.i(TAG_CLASS, "onStop Called");
        if (getInstanceMirrorView() != null && getInstanceMirrorView().isFlashOn()) {
            apagarFlashCamara();
        }
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG_CLASS, "onDestroy Called");
        orientationEventListener.disable();
        releaseCamera();
        if (!retorneUnaFoto) {
            setResult(Activity.RESULT_CANCELED);
        }
    }

    private void releaseCamera() { // modificar este metodo . si es onStop que se detenga solo el preview y si finaliza que borre todas las instancias
        if (getInstanceCamera() != null && getInstanceMirrorView() != null) {
            try {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mirrorView.getHolder().removeCallback(mirrorView);
                mCamera.release();
                mCamera = null;
                mirrorView = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
