package com.android.external.plantform;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

import com.android.ex.camera2.portability.util.SystemProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import dalvik.system.DexClassLoader;

/**
 * Created by bin.zhang on 12/4/15.
 */
public class ExtBuild {
    private static final String TAG = "ExtBuild";

    public static final int DEFAULT = 0;
    public static final int QCOM = 1;
    public static final int MTK = 2;
    public static final int MTK_MT6755 = 3;
    public static final int IDOL4_MINI = 4; // MODIFIED by shunyin.zhang, 2016-06-29,BUG-2421416

    private static final String EXTCAMERA_JIRS_DIR = "plantform";

    private static final String EXTCAMERA_MT6755_LIB = "extcamera_mt6755_dex.jar";
    private static final String EXTCAMERA_MT6755_CLASS_EXTCAMERA = "com.android.external.plantform.mtk.ExtCamera";

    private static int mValue = DEFAULT;

    /* MODIFIED-BEGIN by shunyin.zhang, 2016-07-07,BUG-2479909*/
    private static int mDevice = DEFAULT;

    public static final String PLATFORM = "ro.board.platform";
    private static String mPlatform;

    public static String getPlatform() {
        if (mPlatform == null) {
            mPlatform = SystemProperties.get(PLATFORM, "");
        }
        return mPlatform;
    }

    public static boolean isPlatformMTK() { // MODIFIED by jianying.zhang, 2016-11-10,BUG-3398235
        String target = getPlatform();
        if (target != null && (target.startsWith("mt") || target.startsWith("MT"))) {
            return true;
        }
        return false;
    }

    public static void init() {
        mValue = 0;
        mDevice = 0;
        /* MODIFIED-END by shunyin.zhang,BUG-2479909*/
        Log.w(TAG, "DEVICE=" + Build.DEVICE);
        Log.w(TAG, "HARDWARE=" + Build.HARDWARE);
        Log.w(TAG, "PRODUCT=" + Build.PRODUCT);

        if ("mt6755".equalsIgnoreCase(Build.HARDWARE)) {
            mValue = MTK_MT6755;
        }

        /* MODIFIED-BEGIN by shunyin.zhang, 2016-06-29,BUG-2421416*/
        if ("idol4_mini".equalsIgnoreCase(Build.DEVICE)) {
            mDevice = IDOL4_MINI; // MODIFIED by shunyin.zhang, 2016-07-07,BUG-2479909
        }
        /* MODIFIED-END by shunyin.zhang,BUG-2421416*/
    }

    public static int device() {
        return mValue;
    }

    /* MODIFIED-BEGIN by shunyin.zhang, 2016-07-07,BUG-2479909*/
    public static int buildDevice() {
        return mDevice;
    }
    /* MODIFIED-END by shunyin.zhang,BUG-2479909*/

    public static IExtCamera createCamera(Camera camera, Context context) {
        switch (ExtBuild.device()) {
            case ExtBuild.DEFAULT:
                return null;
            case ExtBuild.MTK_MT6755:
                return doCreateExtCamera(camera, context, EXTCAMERA_MT6755_LIB, EXTCAMERA_MT6755_CLASS_EXTCAMERA);
        }
        return null;
    }

    private static File doGetFilePath(Context context, String jarFileName){
        File path = context.getDir(EXTCAMERA_JIRS_DIR, Context.MODE_PRIVATE);
        //File path = new File( Environment.getExternalStorageDirectory().getPath() + File.separator + EXTCAMERA_JIRS_DIR);

        if (path == null) {
            return null;
        }
        if (path.exists() == false || path.isFile()) {
            path.mkdirs();
        }

        File dexFileFullName = new File(path.getPath(), jarFileName);
        Log.w(TAG, "dexFileFullName=" + dexFileFullName);
        if (dexFileFullName.exists()) {
            return dexFileFullName;
        }

        try {
            InputStream ios=context.getAssets().open(EXTCAMERA_JIRS_DIR + File.separator + jarFileName);
            FileOutputStream fos= new FileOutputStream(dexFileFullName);
            byte[] buffer=new byte[4096];
            int i=-1;
            while((i=ios.read(buffer,0,buffer.length))>0){
                fos.write(buffer, 0, i);
            }
            ios.close();
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "" + e);
            return null;
        }
        return dexFileFullName;
    }


    private static IExtCamera doCreateExtCamera(Camera camera, Context context, String jarFileName, String className) {
        IExtCamera extCamera = null;
        if (context == null) {
            Log.e(TAG, "context is null!!!");
            return extCamera;
        }

        File dexFileFullName = doGetFilePath(context, jarFileName);
        if (dexFileFullName == null) {
            return extCamera;
        }

        Log.w(TAG, "dexFileFullName=" + dexFileFullName + " check file OK!");

        if (dexFileFullName.exists() && dexFileFullName.isFile()) {
            String fullName = dexFileFullName.getAbsolutePath();
            String fullPath = dexFileFullName.getParent();
            Log.w(TAG, "fullName=" + fullName);
            Log.w(TAG, "fullPath=" + fullPath);

            //DexClassLoader dexClassLoader = new DexClassLoader(fullName, fullPath, null, ClassLoader.getSystemClassLoader().getParent());
            DexClassLoader dexClassLoader = new DexClassLoader(fullName, fullPath, null, context.getClassLoader());
            Class libProviderClazz = null;
            try {
                libProviderClazz = dexClassLoader.loadClass(className);
                extCamera = (IExtCamera)libProviderClazz.newInstance();
                if (extCamera == null) {
                    Log.e(TAG, "mExtCamera is null, new instance fail");
                }
                extCamera.create(camera);
            } catch (Exception exception) {
                Log.e(TAG, "mExtCamera is null, exception");
                exception.printStackTrace();
                extCamera = null;
            }
        }
        return extCamera;
    }
}
