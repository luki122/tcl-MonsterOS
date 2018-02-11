package com.android.external;

import java.lang.reflect.Method;
import java.util.HashMap;

import android.media.MediaRecorder;
import android.util.Log;

public class ExtendMediaRecorder {
    private static MediaRecorder mMediaRecorder;

    private static ExtendMediaRecorder sInstance;

    public synchronized static ExtendMediaRecorder getInstance(
            MediaRecorder recorder) {
        if (sInstance == null || recorder != mMediaRecorder) {
            sInstance = new ExtendMediaRecorder(recorder);
        }
        return sInstance;
    }

    private HashMap<String, Method> mMediaRecorderMethodMap = new HashMap<String, Method>();

    private static final String TAG = "ExtendMediaRecorder";

    private ExtendMediaRecorder(MediaRecorder recorder) {
        mMediaRecorder = recorder;
        Class<MediaRecorder> cameraClz = MediaRecorder.class;
        Method[] methods = cameraClz.getMethods();
        for (Method method : methods) {
            mMediaRecorderMethodMap.put(method.getName(), method);
        }
    }

    private Object invoke(String methodName, Object... params) {
        Method method = mMediaRecorderMethodMap.get(methodName);

        if (method != null) {
            try {
                method.setAccessible(true);
                return method.invoke(mMediaRecorder, params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * pause video recorder
     */
    public void pause() {
        if (mMediaRecorderMethodMap.containsKey("pause")) {
            invoke("pause");
        } else if(mMediaRecorderMethodMap.containsKey("tct_pause")){
            invoke("tct_pause");
        } else if (mMediaRecorderMethodMap.containsKey("setParametersExtra")) {
            invoke("setParametersExtra", "media-param-pause=1");
        } else{
            Log.d(TAG,"pause failed!");
        }
    }
}
