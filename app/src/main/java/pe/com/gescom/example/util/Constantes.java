package pe.com.gescom.example.util;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.view.ContextThemeWrapper;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import pe.com.gescom.ecam.R;

public class Constantes {
    private Context context;
    public static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 842;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 843;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 8575;
    public static final int MY_PERMISSIONS_Multiple = 8585;
    // PERMISOS CAMERA ACTIVITY
    public static final int CODE_REQUEST_PERMISSIONS_CAMERA = 105;
    public static final String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES};

    public Constantes(Context context) {
        this.context = context;
        PolicyAndroid();
    }

    private void PolicyAndroid() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public ProgressDialog createProgressDialog(String Mensaje) {
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog));
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(Mensaje);
        progressDialog.setTitle("AppLecturas");
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

    public ProgressDialog createProgressDialog1() {
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Enviando Datos...");
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

    public void Mensaje(String mensaje) {
        Dialog dialogCarga;
        DialogAvisos.Builder CustomDialog = new DialogAvisos.Builder(context);
        CustomDialog.setMessage(mensaje).setTitle("AppLecturas");
        dialogCarga = CustomDialog.create();
        if (!((Activity) context).isFinishing()) {
            dialogCarga.show();
            dialogCarga.setCanceledOnTouchOutside(true);
        }
    }

    public void Mensaje(String title, String mensaje) {
        Dialog dialogCarga;
        DialogAvisos.Builder CustomDialog = new DialogAvisos.Builder(context);
        CustomDialog.setMessage(mensaje).setTitle(title);
        dialogCarga = CustomDialog.create();
        if (!((Activity) context).isFinishing()) {
            dialogCarga.show();
            dialogCarga.setCanceledOnTouchOutside(true);
        }
    }

    public void MensajeBloqueo(String mensaje) {
        Dialog dialogCarga;
        DialogAvisos.Builder CustomDialog = new DialogAvisos.Builder(context);
        CustomDialog.setMessage(mensaje).setTitle("AppLecturas");
        dialogCarga = CustomDialog.create();
        if (!((Activity) context).isFinishing()) {
            dialogCarga.show();
            dialogCarga.setCanceledOnTouchOutside(false);
        }
    }

    public String fechaActual() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return df3.format(c.getTime());
    }

    public void GrabarSharedPreferences(SharedPreferences sharedPreferences, String key, String mensaje) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, mensaje);
        editor.apply();
    }

    public static boolean checkPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void MostrarDialog(String mensaje, DialogInterface.OnClickListener onClickListenerPositive, DialogInterface.OnClickListener onClickListenerNegative) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        mBuilder.setTitle(context.getString(R.string.app_name));
        mBuilder.setMessage(mensaje);
        mBuilder.setPositiveButton("ok", onClickListenerPositive);
        mBuilder.setNegativeButton("no", onClickListenerNegative);
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }
}
