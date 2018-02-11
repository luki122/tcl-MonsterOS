package com.android.external.plantform.mtk;

import com.android.external.plantform.IExtGestureCallback;
import com.android.external.plantform.IExtCamera;
import com.android.external.plantform.IExtPanoramaMoveCallback;
import com.android.external.plantform.IExtPanoramaCallback;

import android.hardware.Camera;
import android.util.Log;

public class ExtCamera implements IExtCamera {
    private Camera mCamera;
    private IExtGestureCallback mGestureCallback;
    private IExtPanoramaCallback mPanoramaCallback;
    private IExtPanoramaMoveCallback mPanoramaMoveCallback;
    private static String TAG = "CameraEx";

    private android.hardware.Camera.GestureCallback mGcb = new android.hardware.Camera.GestureCallback() {
        public void onGesture() {
            DLOG("GestureCallback.onGesture()");
            if (mGestureCallback != null) {
                mGestureCallback.onGesture();
            }
        }
    };

    private android.hardware.Camera.AutoRamaCallback mArcb = new android.hardware.Camera.AutoRamaCallback() {
        public void onCapture(byte[] jpegData) {
            DLOG("AutoRamaCallback.onCapture()");
            if (mPanoramaCallback != null) {
                mPanoramaCallback.onCapture(jpegData);
            }
        }
    };

    private android.hardware.Camera.AutoRamaMoveCallback mArvcb = new android.hardware.Camera.AutoRamaMoveCallback() {
        public void onFrame(int xx, int yy) {
            DLOG("AutoRamaMoveCallback.onFrame()");
            if (mPanoramaMoveCallback != null) {
                mPanoramaMoveCallback.onFrame(xx, yy);
            }
        }
    };

    public void func() {
        DLOG("func()");
    }

    public void CameraEx() {
        DLOG("CameraEx()");
    }

    public void create(Camera camera) {
        DLOG("create()");
        mCamera = camera;
    };

    public void setGestureCallback(IExtGestureCallback cb) {
        DLOG("setGestureCallback()");
        mGestureCallback = cb;
        if (cb == null) {
            mCamera.setGestureCallback(null);
        } else {
            mCamera.setGestureCallback(mGcb);
        }
    }

    public void startGestureDetection() {
        DLOG("startGestureDetection()");
        mCamera.startGestureDetection();
    }

    public void stopGestureDetection() {
        DLOG("stopGestureDetection()");
        mCamera.stopGestureDetection();
    }

    public void destroy() {
        DLOG("destroy()");
        mCamera = null;
    }

    public void startRama(int num) {
        DLOG("startRama()");
        mCamera.startAutoRama(num);
    }

    public void stopRama(int isMerge) {
        DLOG("stopRama()");
        mCamera.stopAutoRama(isMerge);
    }

    public void setRamaCallback(IExtPanoramaCallback cb) {
        DLOG("setRamaCallback()");
        mPanoramaCallback = cb;
        if (cb == null) {
            mCamera.setAutoRamaCallback(null);
        } else {
            mCamera.setAutoRamaCallback(mArcb);
        }
    }

    public void setRamaMoveCallback(IExtPanoramaMoveCallback cb) {
        DLOG("setRamaMoveCallback()");
        mPanoramaMoveCallback = cb;
        if (cb == null) {
            mCamera.setAutoRamaMoveCallback(null);
        } else {
            mCamera.setAutoRamaMoveCallback(mArvcb);
        }
    }

    private static void DLOG(String str) {
        Log.e(TAG, str);
    }
}
