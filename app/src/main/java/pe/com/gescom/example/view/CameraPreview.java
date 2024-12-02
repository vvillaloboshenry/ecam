package pe.com.gescom.example.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import pe.com.gescom.example.util.CameraUtils;
import pe.com.gescom.example.util.VibrationUtils;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {
    private static final String TAG_CLASS = "CameraPreview";
    private final Context mContext;
    private VibrationUtils vibrationUtils;

    //private boolean esSeguroTomarFoto = false;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int mCurrentZoom = -1;
    private int mMaxZoom = -1;
    private boolean isZoomSupported = false;
    private boolean mAutoStabilizeEnabled = false;
    private boolean isFlashOn = false;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size optimalSize;

    public CameraPreview(Context context, Camera mCamera) {
        super(context);
        this.mContext = context;
        this.mCamera = mCamera;
        init();
    }

    private void init() {
        Camera.Parameters parameters = this.getInstanceCamera().getParameters();

        // Obtener los tamaños soportados de la vista previa de la cámara
        mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();

        //for (Camera.Size str : mSupportedPreviewSizes)
            //Log.e(TAG_CLASS, str.width + "/" + str.height);

        // Obtener si el Zoom es soportado en el dispositivo
        this.isZoomSupported = parameters.isZoomSupported();
        if (this.isZoomSupported) {
            // Obtener el Zoom Maximo Soportado
            this.mMaxZoom = parameters.getMaxZoom();
        }

        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
        this.mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        setupVibrator();
    }

    private void setupVibrator() {
        vibrationUtils = VibrationUtils.getInstance(mContext);
    }

    private boolean isWaiting = false;
    private long lastMovementTime = 0;
    private int movementCount = 0;
    private final Queue<Float> movementQueue = new LinkedList<>();

    private static final float ACCELERATION_THRESHOLD = 20f;
    private static final int RESET_TIME = 800; // Tiempo necesario para permitir volver a usar la linterna
    private static final int CANTIDAD_MOVIMIENTOS_PARA_ACTIVAR_FLASH = 2;
    private static final int CANTIDAD_TIEMPO_ENTRE_MOVIMIENTO_PARA_ACTIVAR_FLASH = 450;

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO: Funcion para agregar al laboratorio gescom <- en beta
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];

            long now = System.currentTimeMillis();

            // Resetear si el tiempo entre movimientos es mayor a 250ms
            if ((now - lastMovementTime) > CANTIDAD_TIEMPO_ENTRE_MOVIMIENTO_PARA_ACTIVAR_FLASH && movementCount > 0) {
                lastMovementTime = 0;
                movementCount = 0;
                movementQueue.clear();
            }

            if (Math.abs(x) > ACCELERATION_THRESHOLD && !isWaiting) {
                if (!movementQueue.isEmpty()) {
                    float lastMovement = movementQueue.peek();
                    if ((lastMovement > 0 && x < 0) || (lastMovement < 0 && x > 0)) {
                        movementCount++;
                        lastMovementTime = now;
                        movementQueue.clear();
                        Log.d("Sensor", "Cambio de dirección detectado. Conteo de movimientos: " + movementCount);
                    }
                }
                movementQueue.offer(x); // Guardar el valor en la cola

                // Si se alcanza el número requerido de movimientos, alternar el flash
                if (movementCount >= CANTIDAD_MOVIMIENTOS_PARA_ACTIVAR_FLASH) {
                    alternarFlash();
                    isWaiting = true;
                    movementCount = 0;
                    lastMovementTime = 0;
                    movementQueue.clear();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isWaiting = false;
                        }
                    }, RESET_TIME);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Manejar cambios en la precisión del sensor si es necesario
    }

    public void alternarFlash() {
        if (isFlashOn) {
            turnOffFlash();
        } else {
            turnOnFlash();
        }
    }

    public Camera getInstanceCamera() {
        return this.mCamera;
    }

    public void startPreview(SurfaceHolder holder) {
        try {
            CamaraActivity.esSeguroTomarFoto = true;
            this.getInstanceCamera().setPreviewDisplay(holder);
            this.mCamera.startPreview();
        } catch (Exception e) {
            CamaraActivity.esSeguroTomarFoto = false;
            throw new RuntimeException(e);
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.i(TAG_CLASS, "surfaceCreated");
       /* this.setCameraDisplayOrientation();
        this.startPreview(holder);*/
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG_CLASS, "surfaceChanged: format=" + format + " width=" + width + " height=" + height);
        if (this.mHolder.getSurface() == null) {
            return;
        }
        try {
            this.getInstanceCamera().stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.configureCamera();
            this.setCameraDisplayOrientation(); // poner el preview en la orientacion correcta
            mCamera.setPreviewDisplay(holder);
            CamaraActivity.esSeguroTomarFoto = true;
            mCamera.startPreview();
        } catch (Exception e) {
            CamaraActivity.esSeguroTomarFoto = false;
            Log.e(TAG_CLASS, "Error starting camera preview: " + e.getMessage());
        }
    }

    private void configureCamera() {
        Camera.Parameters parameters = this.mCamera.getParameters();

        // Establecer el tamaño de vista previa en los parámetros de la cámara
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);

        // Habilitar la estabilización de video si está soportada
        if (parameters.isVideoStabilizationSupported()) {
            parameters.setVideoStabilization(true);
        }

        // Configurar el rango de FPS
        List<int[]> supportedFpsRanges = parameters.getSupportedPreviewFpsRange();
        int[] optimalFpsRange = supportedFpsRanges.get(supportedFpsRanges.size() - 1); // obtener el rango máximo de FPS
        parameters.setPreviewFpsRange(optimalFpsRange[0], optimalFpsRange[1]);

        // Establecer los parámetros actualizados en la cámara
        this.mCamera.setParameters(parameters);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        // Elegir el tamaño de vista previa deseado
        int rotation = ((Activity) mContext).getWindowManager().getDefaultDisplay().getRotation();
        if ((Surface.ROTATION_0 == rotation || Surface.ROTATION_180 == rotation)) {
            optimalSize = getOptimalPreviewSize(mSupportedPreviewSizes, height, width);//portrait
        } else {
            optimalSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);//landscape
        }

        if (optimalSize == null) {
            return;
        }

        float ratio;
        if (optimalSize.height >= optimalSize.width) {
            ratio = (float) optimalSize.height / (float) optimalSize.width;
        } else {
            ratio = (float) optimalSize.width / (float) optimalSize.height;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && ((Activity) mContext).isInMultiWindowMode()) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || !(Surface.ROTATION_0 == rotation || Surface.ROTATION_180 == rotation)) {
                setMeasuredDimension(width, (int) (width / ratio));
            } else {
                setMeasuredDimension((int) (height / ratio), height);
            }
        } else {
            if ((Surface.ROTATION_0 == rotation || Surface.ROTATION_180 == rotation)) {
                Log.e("---", "onMeasure: " + height + " - " + width * ratio);
                //2264 - 2400.0 pix c -- yes
                //2240 - 2560.0 samsung -- yes
                //1582 - 1440.0 pix 2 -- no
                //1864 - 2048.0 sam tab -- yes
                //848 - 789.4737 iball -- no
                //1640 - 1600.0 nexus 7 -- no
                //1093 - 1066.6667 lenovo -- no
                //if width * ratio is > height, need to minus toolbar height
                if ((width * ratio) < height)
                    setMeasuredDimension(width, (int) (width * ratio));
                else
                    setMeasuredDimension(width, (int) (width * ratio));
            } else {
                setMeasuredDimension((int) (height * ratio), height);
            }
        }
        requestLayout();
    }

    // Método para calcular el tamaño óptimo de la vista previa
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        Log.i("getOptimalPreviewSize", "Supported sizes: " + sizes);
        Log.i("getOptimalPreviewSize", "Optimal size: " + (optimalSize != null ? optimalSize.width + "x" + optimalSize.height : "null"));
        return optimalSize;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.i(TAG_CLASS, "surfaceDestroyed");
        CamaraActivity.esSeguroTomarFoto = false;
        if (getInstanceCamera() != null) {
            getInstanceCamera().stopPreview();
        }
        sensorManager.unregisterListener(this);
        // empty. Take care of releasing the Camera preview in your activity.
    }

    private void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(CameraUtils.findFirstBackFacingCamera(), info);

        int rotation = ((WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        int result = CameraUtils.getDisplayOrientation(rotation, info);

        this.getInstanceCamera().setDisplayOrientation(result);
    }

    public boolean isFlashOn() {
        return isFlashOn;
    }

    public void performShortVibration() {
        vibrationUtils.vibrate(250);
    }

    public void turnOnFlash() {
        if (!isFlashOn) {
            if (getInstanceCamera() != null) {
                performShortVibration();
                Camera.Parameters parameters = this.getInstanceCamera().getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                this.getInstanceCamera().setParameters(parameters);
                isFlashOn = true;
            }
        }
    }

    private void performVibration() {
        long[] pattern = {0, 100, 100, 0};
        vibrationUtils.vibrate(pattern);
    }

    public void turnOffFlash() {
        if (isFlashOn) {
            if (getInstanceCamera() != null) {
                performVibration();
                Camera.Parameters parameters = this.getInstanceCamera().getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                getInstanceCamera().setParameters(parameters);
                isFlashOn = false;
            }
        }
    }

    public int getCurrentZoom() {
        return this.mCurrentZoom;
    }

    public int getMaxZoom() {
        return this.mMaxZoom;
    }

    public boolean isZoomSupported() {
        return this.isZoomSupported;
    }

    private int calculateZoomLevel(float scale) {
        int maxZoom = this.getMaxZoom();
        int newZoom = this.getCurrentZoom() + (int) ((scale - 1) * maxZoom);

        return Math.max(0, Math.min(newZoom, maxZoom));
    }

    public void applyZoom(float scale) {
        int zoomLevel = calculateZoomLevel(scale);
        applyZoom(zoomLevel);
    }

    public void applyZoom(int zoomValue) {
        if (isValidZoomValue(zoomValue)) {
            setCameraZoom(zoomValue);
        }
    }

    public boolean isValidZoomValue(int zoomValue) {
        return this.getInstanceCamera() != null && this.getMaxZoom() > 0 && zoomValue >= 0 && zoomValue <= this.getMaxZoom() && mCurrentZoom != zoomValue;
    }

    private void setCameraZoom(int zoomValue) {
        //Log.i("setCameraZoom", "zoomValue: " + zoomValue);
        Camera.Parameters parameters = this.getInstanceCamera().getParameters();
        parameters.setZoom(zoomValue);
        getInstanceCamera().setParameters(parameters);
        mCurrentZoom = zoomValue;
    }

      /*  public void setCameraZoom(final int zoomValue) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long startTime = System.currentTimeMillis();
                    //   Log.i("setCameraZoom", "Iniciando setCameraZoom con zoomValue: " + zoomValue);

                    synchronized (mCamera) {
                        if (mCurrentZoom != zoomValue) {
                            mParams.setZoom(zoomValue);
                            getInstanceCamera().setParameters(mParams);
                            mCurrentZoom = zoomValue;
                        }
                    }

                    long endTime = System.currentTimeMillis();
                    //    Log.i("setCameraZoom", "setCameraZoom terminado en " + (endTime - startTime) + " ms");
                }
            }).start();
        }*/

    // Método para verificar si la estabilización está habilitada
    public boolean isAutoStabilizeEnabled() {
        return mAutoStabilizeEnabled;
    }

    // Método para habilitar o deshabilitar la estabilización de la imagen
    public void activateAutoStabilize(boolean stabilize) { // hace que el brillo de las pantallas no destelle la camara
        Camera.Parameters parameters = this.getInstanceCamera().getParameters();
        if (parameters != null) {
            parameters.setAutoExposureLock(stabilize);
            parameters.setAutoWhiteBalanceLock(stabilize);
            getInstanceCamera().setParameters(parameters);
            mAutoStabilizeEnabled = stabilize;
        }
    }

    // Método para ajustar el enfoque manualmente
    public void setFocusMode(String focusMode) {
        Camera.Parameters parameters = this.getInstanceCamera().getParameters();
        if (parameters != null) {
            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            if (supportedFocusModes.contains(focusMode)) {
                parameters.setFocusMode(focusMode);
                getInstanceCamera().setParameters(parameters);
            }
        }
    }
}
