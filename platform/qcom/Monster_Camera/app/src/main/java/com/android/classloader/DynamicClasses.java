package com.android.classloader;

import android.content.Context;

import com.android.camera.app.CameraServices;
import com.android.camera.debug.Log;
import com.android.camera.settings.SettingsManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by sichao.hu on 11/4/15.
 */
public class DynamicClasses {

    private static final Log.Tag TAG=new Log.Tag("DynamicClasses");


    private static final String ASSETS="external";
    private static final String ASSETS_FOLDER="external/";
    private static final String ASSETS_MORPHO_DEX_PATH="morpho.dex";
    private static final String ASSETS_CRUNCHFISH_DEX_PATH="crunchfish.dex";
    private static final String ASSETS_LIB_MEM_ALLOCATOR="libmorpho_memory_allocator.so";
    private static final String ASSETS_LIB_PANO_GP="libmorpho_panorama_gp.so";
    private static final String ASSETS_LIB_A3D="libtouchless_a3d.so";
    private static final String ASSETS_LIB_A3D_JNI="libtouchless_a3d_jni.so";

    private static final String DYNAMIC_LIB_VERSION="dynamic_lib_version";

    private static final int VERSION_NOT_FOUND=0;

    private static final int VERSION_INIT_WITH_64BIT=1;

    private static final int CURRENT_VERSION=VERSION_INIT_WITH_64BIT;

    public static String getMorphoDexPath(Context context){
        return getFilePath(context,ASSETS_MORPHO_DEX_PATH);
    }


    public static String getMorphoLibDir(Context context){
        getFilePath(context,ASSETS_LIB_MEM_ALLOCATOR);
        getFilePath(context,ASSETS_LIB_PANO_GP);
        return getLocalPath(context);
    }

    private static final String MORPHOGP_WRAPPER_CLASS_PATH ="com.morpho.core.MorphoPanoramaGP";
    public static String getMorphogpClassPath(){
        return MORPHOGP_WRAPPER_CLASS_PATH;
    }

    private static final String MORPHO_NATIVE_MEM_ALLOCATOR_CLASS_PATH="com.morpho.utils.NativeMemoryAllocator";
    public static String getMorphoNativeAllocatorClassPath(){
        return MORPHO_NATIVE_MEM_ALLOCATOR_CLASS_PATH;
    }

    public static boolean isMorphoPanoValid(Context context){
        try {
            String[] assets=context.getAssets().list(ASSETS);
            for(String assetName:assets){
                Log.w(TAG,"asset : "+assetName);
                if(assetName.equals(ASSETS_MORPHO_DEX_PATH)){
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        Log.w(TAG,"assets no match for morpho");

        return false;
    }

    public static boolean isCrunchFishGestureValid(Context context){
        try {
            String[] assets=context.getAssets().list(ASSETS);
            for(String assetName:assets){
                Log.w(TAG,"asset : "+assetName);
                if(assetName.equals(ASSETS_CRUNCHFISH_DEX_PATH)){
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        Log.w(TAG,"assets no match for morpho");

        return false;
    }



    public static String getCrunchfishLibDir(Context context){
        getFilePath(context,ASSETS_LIB_A3D);
        getFilePath(context,ASSETS_LIB_A3D_JNI);
        return getLocalPath(context);
    }

    public static String getCrunchfishDexPath(Context context){
        return getFilePath(context,ASSETS_CRUNCHFISH_DEX_PATH);
    }

    private static final String CRUNCH_FISH_CORE="com.crunchfish.core.GestureInstructionImpl";
    public static String getCrunchFishCoreClassPath(){
        return CRUNCH_FISH_CORE;
    }

    private static Integer mVersion=null;
    private static final String getLocalPath(Context context){
        String localPath=context.getFilesDir().getAbsolutePath();
        StringBuilder sb=new StringBuilder().append(localPath).append(File.separator).append(ASSETS_FOLDER);
        String composedPath=sb.toString();
        File dir=new File(composedPath);
        SettingsManager settingsManager=((CameraServices)context.getApplicationContext()).getSettingsManager();
        if(!dir.exists()){
            boolean result=dir.mkdirs();
            settingsManager.set(SettingsManager.SCOPE_GLOBAL, DYNAMIC_LIB_VERSION,CURRENT_VERSION);
            mVersion=CURRENT_VERSION;
            Log.w(TAG,"mkdir success ?"+result);
        }else{
            if(mVersion!=null){
                return composedPath;
            }

            mVersion=settingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,DYNAMIC_LIB_VERSION,VERSION_NOT_FOUND);
            Log.v(TAG,"mVersion is "+mVersion);
            if(mVersion<CURRENT_VERSION){
                Log.v(TAG,String.format("original version is %d,current version is %d",mVersion,CURRENT_VERSION));
                clearDirectory(dir);
                settingsManager.set(SettingsManager.SCOPE_GLOBAL, DYNAMIC_LIB_VERSION,CURRENT_VERSION);
                mVersion=CURRENT_VERSION;
            }
            Log.w(TAG,"dir exists: "+dir.getAbsolutePath());
        }

        return composedPath;
    }

    /**
     * Clear all the files containing within the directory , keep the directory still
     * @param file
     */
    private static void clearDirectory(File file){
        if(file.isDirectory()){
            File[] files=file.listFiles();
            for(File fNode:files){
                deleteFile(fNode);
            }
        }
    }

    private static void deleteFile(File file){
        if(file.isDirectory()){
            File[] files=file.listFiles();
            for(File fNode:files){
                deleteFile(fNode);
            }
            file.delete();
        }else{
            file.delete();
        }
    }

    private static final String getFilePath(Context context,String assetsFileName){

        // local dir path is  /data/data/com.***.camera/exteranl/
        String composedFilePath= getLocalPath(context);
        String targetPath=composedFilePath+assetsFileName;
        File file=new File(targetPath);
        if(file.exists()){
            Log.w(TAG,"file exists: "+file.getAbsolutePath());
            return targetPath;
        }

        try {
            Log.w(TAG,"start write file "+targetPath);
            String assetsPath=ASSETS_FOLDER+assetsFileName;
            InputStream ios=context.getAssets().open(assetsPath);
            Log.w(TAG,"open assets success :"+assetsPath);
            FileOutputStream fos=new FileOutputStream(file);
            byte[] buffer=new byte[4096];
            int i=-1;
            while((i=ios.read(buffer,0,buffer.length))>0){
                fos.write(buffer, 0, i);
            }
            ios.close();
            fos.close();
        } catch (IOException e) {
            Log.e(TAG,String.format("initialize lib for %s failed :"+e.getMessage(),targetPath));
        }

        return targetPath;
    }

}
