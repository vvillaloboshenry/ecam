package pe.com.gescom.ecam.util;

import android.hardware.Camera;
import android.view.Surface;

public class CameraUtils {

    public static int findFirstBackFacingCamera() {
        int foundId = -1;
        int numCams = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int camId = 0; camId < numCams; camId++) {
            Camera.getCameraInfo(camId, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                foundId = camId;
                break;
            }
        }
        return foundId;
    }

    public static int getDisplayOrientation(int rotation, Camera.CameraInfo info) {
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }
}
