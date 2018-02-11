package com.android.camera;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.android.camera.debug.Log;

/**
 * Created by sichao.hu on 1/13/16.
 */
public class DeviceInfo {
    public static final String DEVICE= Build.DEVICE.toLowerCase();
    public static final String BRAND=Build.BRAND.toLowerCase();
    private static final String IDOL3="idol3";
    private static final String IDOL4="idol4";
    /* MODIFIED-BEGIN by feifei.xu, 2016-10-27,BUG-3166732*/
    private static final String IDOL5="idol5";
    private static final String SIMBA="simba";
    /* MODIFIED-END by feifei.xu,BUG-3166732*/
    public static final String TCL="tcl";
    private static final String REVERSIBLE_TAG="degree_rotation";
    private static final int REVERSIBLE_OFF=0;
    private static final int REVERSIBLE_ON=1;
    public static final Log.Tag TAG=new Log.Tag("DeviceInfo");

    public static boolean isTclDevice(){
        return TCL.equals(BRAND);
    }
    public static boolean isIdol3(){
        return DEVICE.startsWith(IDOL3);
    }
    public static boolean isIdol4(){
        return DEVICE.startsWith(IDOL4);
    }
    /* MODIFIED-BEGIN by feifei.xu, 2016-10-27,BUG-3166732*/
    public static boolean isIdol5(){
        return DEVICE.startsWith(IDOL5) || DEVICE.startsWith(SIMBA);
    }
    /* MODIFIED-END by feifei.xu,BUG-3166732*/

    public static Uri getReversibleSettingUri(){
        Uri uri=null;
        if (DeviceInfo.isIdol3()){
            uri = Settings.Global.getUriFor(REVERSIBLE_TAG);
        }else if(DeviceInfo.isIdol4()){
            uri=Settings.System.getUriFor(REVERSIBLE_TAG);
        /* MODIFIED-BEGIN by feifei.xu, 2016-10-27,BUG-3166732*/
        }else if(DeviceInfo.isIdol5()){
            uri=Settings.System.getUriFor(REVERSIBLE_TAG);
            /* MODIFIED-END by feifei.xu,BUG-3166732*/
        }
        return uri;
    }

    public static boolean isReversibleOn(ContentResolver resolver){
        if(DeviceInfo.isIdol3()){
            int reversibleTag=Settings.Global.getInt(resolver, REVERSIBLE_TAG, REVERSIBLE_OFF);
            return reversibleTag==REVERSIBLE_ON;
        }else if(DeviceInfo.isIdol4()){
            int reversibleTag=Settings.System.getInt(resolver, REVERSIBLE_TAG, REVERSIBLE_OFF);
            return reversibleTag==REVERSIBLE_ON;
        /* MODIFIED-BEGIN by feifei.xu, 2016-10-27,BUG-3166732*/
        }else if (DeviceInfo.isIdol5()) {
            int reversibleTag=Settings.System.getInt(resolver, REVERSIBLE_TAG, REVERSIBLE_OFF);
            return reversibleTag==REVERSIBLE_ON;
            /* MODIFIED-END by feifei.xu,BUG-3166732*/

        }
        return false;
    }

    /**
     * A method to update reversible settings , for limited devices
     * @param resolver
     * @param on
     * @return true if succeed to update reversible setting and false if failed
     */
    public static boolean updateReversibleSetting(ContentResolver resolver,boolean on){
        if(DeviceInfo.isIdol3()){
            try {
                Settings.Global.putInt(resolver, REVERSIBLE_TAG, on?REVERSIBLE_ON:REVERSIBLE_OFF);
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Not permitted:" + e.getMessage());
            }
        }else if(DeviceInfo.isIdol4()) {
            try {
                Settings.System.putInt(resolver, REVERSIBLE_TAG, on?REVERSIBLE_ON:REVERSIBLE_OFF);
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Not permitted:" + e.getMessage());
            }
        /* MODIFIED-BEGIN by feifei.xu, 2016-10-27,BUG-3166732*/
        }else if(DeviceInfo.isIdol5()) {
            try {
                Settings.System.putInt(resolver, REVERSIBLE_TAG, on?REVERSIBLE_ON:REVERSIBLE_OFF);
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Not permitted:" + e.getMessage());
            }
            /* MODIFIED-END by feifei.xu,BUG-3166732*/
        }
        return false;
    }

}
