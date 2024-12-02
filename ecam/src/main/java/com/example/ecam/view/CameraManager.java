package com.example.ecam.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import java.io.File;
import java.lang.ref.WeakReference;


public class CameraManager {
    private static CameraManager instance;
    public static final int QUALITY_DEFAULT = 100;
    public static final ReturnType RETURNTYPE_DEFAULT = ReturnType.BITMAP;

    public enum ReturnType {
        FILE_PATH, BASE64, FILE, BITMAP
    }

    public enum Quality {
        NORMAL, BUENA, MUY_BUENA
    }

    private WeakReference<AppCompatActivity> activityRef;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Object callback;
    private int quality = QUALITY_DEFAULT;
    private ReturnType returnType = RETURNTYPE_DEFAULT;

    private CameraManager() {
    }

    private void setActivity(AppCompatActivity activity) {
        this.activityRef = new WeakReference<>(activity);
    }

    private void setActivityResultLauncher(ActivityResultLauncher<Intent> launcher) {
        this.activityResultLauncher = launcher;
    }

    /**
     * Inicializar CameraManager
     *
     * @param activity
     * @return
     */
    public static CameraManager with(AppCompatActivity activity) {
        return getInstance(activity);
    }

    /**
     * Metodo estático para obtener o crear una instancia de CameraManager.
     *
     * @param activity
     * @return
     */
    public static synchronized CameraManager getInstance(AppCompatActivity activity) {
        if (instance == null) {
            instance = new CameraManager();
            ActivityResultLauncher<Intent> launcher = activity.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> instance.handleResult(result)
            );
            instance.setActivity(activity);
            instance.setActivityResultLauncher(launcher);
        } else if (instance.activityRef.get() != activity) {
            if (activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                throw new IllegalStateException("El contexto debe configurarse antes de que la actividad esté STARTED o RESUMED.");
            }
            ActivityResultLauncher<Intent> launcher = activity.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> instance.handleResult(result)
            );
            instance.setActivity(activity);
            instance.setActivityResultLauncher(launcher);
        }
        return instance;
    }

    public static synchronized CameraManager get() {
        return instance;
    }

    public CameraManager quality(int quality) {
        this.quality = quality;
        return this;
    }

    public CameraManager resultType(ReturnType returnType) {
        this.returnType = returnType;
        return this;
    }

    public CameraManager result(CallbackBase64 callback) {
        this.callback = callback;
        return this;
    }

    public CameraManager result(CallbackBitmap callback) {
        this.callback = callback;
        return this;
    }

    public CameraManager result(CallbackFile callback) {
        this.callback = callback;
        return this;
    }

    public CameraManager result(CallbackFilePath callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Metodo para iniciar la cámara.
     */
    public void start() {
        AppCompatActivity activity = activityRef.get();
        if (activity == null) {
            throw new IllegalStateException("La actividad no está configurada o ha sido destruida.");
        }
        if (activityResultLauncher == null) {
            throw new IllegalStateException("CameraManager.activityResultLauncher no está inicializado correctamente.");
        }
        Intent cameraIntent = new Intent(activity, CamaraActivity.class);
        cameraIntent.putExtra("quality", quality);
        cameraIntent.putExtra("returnType", returnType.name());
        activityResultLauncher.launch(cameraIntent);
        activity.overridePendingTransition(0, 0);
    }

    protected void handleResult(ActivityResult activityResult) {
        Intent data = activityResult.getData();
        int resultCode = activityResult.getResultCode();
        if (resultCode == Activity.RESULT_OK && data != null) {

            try {
                Object resultData = switch (returnType) {
                    case BITMAP -> data.getParcelableExtra("bitmap");
                    case BASE64 -> data.getStringExtra("base64");
                    case FILE -> data.getStringExtra("file");
                    case FILE_PATH -> data.getStringExtra("filePath");
                };

                if (callback != null) {
                    if (returnType == ReturnType.BITMAP && callback instanceof CallbackBitmap && resultData instanceof Bitmap) {
                        ((CallbackBitmap) callback).onSuccess((Bitmap) resultData);
                    }
                    if (returnType == ReturnType.BASE64 && callback instanceof CallbackBase64 && resultData instanceof String) {
                        ((CallbackBase64) callback).onSuccess((String) resultData);
                    }
                    if (returnType == ReturnType.FILE && callback instanceof CallbackFile && resultData instanceof File) {
                        ((CallbackFile) callback).onSuccess((File) resultData);
                    }
                    if (returnType == ReturnType.FILE_PATH && callback instanceof CallbackFilePath && resultData instanceof String) {
                        ((CallbackFilePath) callback).onSuccess((String) resultData);
                    }
                }
            } catch (Exception e) {
                if (callback != null) {
                    if (callback instanceof CallbackBitmap) {
                        ((CallbackBitmap) callback).onError(e);
                    }
                    if (callback instanceof CallbackBase64) {
                        ((CallbackBase64) callback).onError(e);
                    }
                    if (callback instanceof CallbackFile) {
                        ((CallbackFile) callback).onError(e);
                    }
                    if (callback instanceof CallbackFilePath) {
                        ((CallbackFilePath) callback).onError(e);
                    }
                }
            }
        } else if (resultCode != Activity.RESULT_OK) {
            if (callback != null) {
                if (callback instanceof CallbackBitmap) {
                    ((CallbackBitmap) callback).onCanceled("Operacion cancelada");
                }
                if (callback instanceof CallbackBase64) {
                    ((CallbackBase64) callback).onCanceled("Operacion cancelada");
                }
                if (callback instanceof CallbackFile) {
                    ((CallbackFile) callback).onCanceled("Operacion cancelada");
                }
                if (callback instanceof CallbackFilePath) {
                    ((CallbackFilePath) callback).onCanceled("Operacion cancelada");
                }
            }
        }
    }

    public void release() {
        if (activityRef != null) {
            activityRef.clear(); // Elimina la referencia débil
        }
        instance = null;
        this.activityResultLauncher = null; // Libera recursos adicionales
        this.callback = null;
    }

    public interface CallbackBitmap {
        void onSuccess(Bitmap result);

        void onCanceled(String message);

        void onError(Exception e);
    }

    public interface CallbackBase64 {
        void onSuccess(String result);

        void onCanceled(String message);

        void onError(Exception e);
    }

    public interface CallbackFile {
        void onSuccess(File result);

        void onCanceled(String message);

        void onError(Exception e);
    }

    public interface CallbackFilePath {
        void onSuccess(String result);

        void onCanceled(String message);

        void onError(Exception e);
    }
}