package com.android.external;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;

/* MODIFIED-BEGIN by xuyang.liu, 2016-10-13,BUG-3110198*/
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import com.android.camera.debug.Log;
import com.android.ex.camera2.portability.CameraAgent;
/* MODIFIED-END by xuyang.liu,BUG-3110198*/
import com.android.external.plantform.ExtBuild;

public class ExtendCamera {
    private static Camera mCamera;

    private static ExtendCamera sInstance;

    public synchronized static ExtendCamera getInstance(Camera camera) {
        if (sInstance == null || camera != mCamera) {
            sInstance = new ExtendCamera(camera);
        }
        return sInstance;
    }

    private HashMap<String, Method> mCameraMethodMap = new HashMap<String, Method>();

    private static final Log.Tag TAG = new Log.Tag("ExtendCamera"); // MODIFIED by xuyang.liu, 2016-10-13,BUG-3110198

    private ExtendCamera(Camera camera) {
        mCamera = camera;
        Class<Camera> cameraClz = Camera.class;
        Method[] methods = cameraClz.getMethods();
        for (Method method : methods) {
            mCameraMethodMap.put(method.getName(), method);
        }
    }

    private boolean isSupport(String method){
    	return mCameraMethodMap.containsKey(method);
    }

    private void set(String method, Object value) {
        /* MODIFIED-BEGIN by xuyang.liu, 2016-10-13,BUG-3110198*/
        set(method, value, false);
    }

    private void set(String method, Object value, boolean paramNotNull) {
    /* MODIFIED-END by xuyang.liu,BUG-3110198*/
        if (method == null) {
            return;
        }
        Method setMethod = mCameraMethodMap.get(method);

        if (setMethod != null) {
            setMethod.setAccessible(true);
            try {
                if(value != null) {
                    setMethod.invoke(mCamera, value);
                /* MODIFIED-BEGIN by xuyang.liu, 2016-10-13,BUG-3110198*/
                }else if(paramNotNull) {
                    setMethod.invoke(mCamera, (Object)null);
                    /* MODIFIED-END by xuyang.liu,BUG-3110198*/
                }else{
                    setMethod.invoke(mCamera, (Object[])null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The return value stands for whether the burst shot needs to
     * request taking picture for every shot
     */
    public boolean needTakePicturePerShotDuringBurst(){
    	String setQualcommBurstshot="setLongshot";
    	return isSupport(setQualcommBurstshot);
    }

    public void setLongshot(boolean enable,Parameters param) {
    	String setQualcommBurstshot="setLongshot";
    	if(isSupport(setQualcommBurstshot)){
    		set("setLongshot",enable);
    	}else{
            if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
                // On mt6755 device, also need to invoke cancelContinuousShot,
                // tell Framework stop current burst shooting.
                if (enable == false) {
                    Method setMethod = mCameraMethodMap.get("cancelContinuousShot");
                    if (setMethod != null) {
                        setMethod.setAccessible(true);
                        try {
                            setMethod.invoke(mCamera);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    		ExtendParameters extCamera=ExtendParameters.getInstance(param);
    		extCamera.setBurstShot(enable);
    	}

    }

    public void cancelPreAllocBurst(){
        String cancelPreAllocMethodName="cancelPicture";
        if(isSupport(cancelPreAllocMethodName)){
            set(cancelPreAllocMethodName, null);
        }
    }

    /* MODIFIED-BEGIN by xuyang.liu, 2016-10-13,BUG-3110198*/
    public void setMetadataCb(final Object cb) {
        String setMetadataCb="setMetadataCb";
        ClassLoader loader=Camera.class.getClassLoader();
        Class<?>[] interfazzez=Camera.class.getClasses();
        Class<?> interfazz=null;
        for(Class<?> clazz :interfazzez){
            if(clazz.getName().equals("android.hardware.Camera$CameraMetaDataCallback")){
                interfazz=clazz;
                break;
            }
        }
        Object clazzInstance= Proxy.newProxyInstance(loader, new Class[]{interfazz}, new InvocationHandler() {

            @Override
            public Object invoke(Object arg0, Method method, Object[] arg2)
                    throws Throwable {

                switch (method.getName()) {
                    case "onCameraMetaData":
                        byte[] data = (byte[]) arg2[0];
                        int metadata[] = new int[3];
                        if (data.length >= 12) {
                            for (int i = 0; i < 3; i++) {
                                metadata[i] = byteToInt((byte[]) data, i * 4);
                                Log.e(TAG, "onCameraMetaData metadata[" + i + "] " + metadata[i]);
                            }
                        }
                        ((CameraAgent.CameraMetaDataCallback) cb).onCameraMetaData((metadata[2] & 0xFF) > 1);
                }
                return null;
            }
        });
        if(isSupport(setMetadataCb)){
            if (cb == null) {
                set(setMetadataCb, null, true);
            } else {
                set(setMetadataCb, clazzInstance);
            }
        }
    }
    private int byteToInt (byte[] b, int offset) {
        int value = 0;
        // bind 4 bytes to 1 int,eg {1,2,3,4} to 1234
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[(3-i) + offset] & 0x000000FF) << shift;
        }
        return value;
    }
    /* MODIFIED-END by xuyang.liu,BUG-3110198*/
}
