package com.android.external;

import android.hardware.Camera.Parameters;
import android.text.TextUtils;
import android.util.Log;

import com.android.camera.util.CameraUtil;
import com.android.external.plantform.ExtBuild;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtendParameters {
    private static Parameters mParameters;

    private static ExtendParameters mInstance;

    public synchronized static ExtendParameters getInstance(Parameters param) {
        if (mInstance == null || param != mParameters) {
            mInstance = new ExtendParameters(param);
        }
        return mInstance;
    }

    private HashMap<String, Method> mParamMethodMap = new HashMap<String, Method>();

    private static final String TAG = "ExtendParameters";

    private ExtendParameters(Parameters parameters) {
        mParameters = parameters;
        Class<Parameters> paramClass = Parameters.class;
        Method[] methods = paramClass.getMethods();
        for (Method method : methods) {
            mParamMethodMap.put(method.getName(), method);
        }
    }

    private void set(String method, Object value) {
        if (value == null) {
            return;
        }
        Method setMethod = mParamMethodMap.get(method);

        if (setMethod != null) {
            try {
                setMethod.setAccessible(true);
                setMethod.invoke(mParameters, value);
            } catch (Exception e) {
                Log.d(TAG,"set parameter failed!method:"+method.toString()+",value:"+value);
                e.printStackTrace();
            }
        }
    }

    private List<String> query(String method) {
        Method queryMethod = mParamMethodMap.get(method);
        if (queryMethod != null) {
            try {
                queryMethod.setAccessible(true);
                Object obj = queryMethod.invoke(mParameters);
                return (List<String>) obj;
            } catch (Exception e) {
                Log.d(TAG,"query failed!method:"+method.toString());
                e.printStackTrace();
            }
        }

        return new ArrayList<String>();
    }

    // //////////////////////////ISO Associate Setting Begin
    // /////////////////////////////////////

    /*
     * This method is used to convert ISO value automatically from Qualcomm
     * format to MTK ,and vice versa
     */

    public boolean isISOModeSupport() {
        if (mParamMethodMap.containsKey("getSupportedISOSpeed")
                || mParamMethodMap.containsKey("getSupportedIsoValues")) {
            return true;
        }
        return false;
    }

    public String parseISOValueFormat(String value) {
        boolean isQualcomm = false;
        if (mParamMethodMap.containsKey("getSupportedIsoValues")) {
            isQualcomm = true;
        }
        if("auto".equalsIgnoreCase(value)){
            return value;
        }

        String realValue = "";
        String reg = "[\\d]+";
        Pattern pat = Pattern.compile(reg);
        Matcher mat = pat.matcher(value);
        if (mat.find()) {
            realValue = mat.group();
        }
        if (isQualcomm) {
            return "ISO" + realValue;
        } else {
            return realValue;
        }
    }


    @Deprecated
    /**
     * Seems not working for manual , not used any more
     */
    public void setISOValue(String value) {
        if (mParamMethodMap.containsKey("setISOSpeed")) {
            set("setISOSpeed", value);
        } else if (mParamMethodMap.containsKey("setISOValue")) {
            set("setISOValue", value);
        }
    }

    public void setContinuousISOValue(int value){
    	if(!"iso".equals(mParameters.get("iso"))){
    		mParameters.set("iso", "manual");
    	}
    	mParameters.set("continuous-iso", value);
    }

    // //////////////////////////ISO Associate Setting
    // End////////////////////////////////////

    /*************************** ZSL Associate Setting *********************************/
    public String parseZSLValueFormat(String value) {
        // if (value == null || value.length() < 2) {
        // return null;
        // }
        // boolean isQcom = false;
        // if (mParamMethodMap.containsKey("setZSLMode")) {
        // isQcom = true;
        // }
        //
        // if (isQcom) {
        // value = value.toLowerCase();
        // } else {
        // value = value.replaceFirst(value.substring(0, 1),
        // value.substring(0, 1).toUpperCase());
        // }

        return value;
    }

    public List<String> getSupportedZSLValues() {
        if (mParamMethodMap.containsKey("getSupportedZSDMode")) {
            return query("getSupportedZSDMode");

        } else if (mParamMethodMap.containsKey("getSupportedZSLModes")) {
            return query("getSupportedZSLModes");
        }
        return new ArrayList<String>();
    }

    public void setZSLMode(String value) {
        if (value == null) {
            return;
        }
        boolean isQcom = false;
        boolean isMtk  = false;
        if (mParamMethodMap.containsKey("setZSDMode")) {
            set("setZSDMode", value);
            isMtk = true;
        } else if (mParamMethodMap.containsKey("setZSLMode")) {
            set("setZSLMode", value);
            isQcom = true;
        }

        // if platform is qcom,then set camera-mode
        if ((isMtk || isQcom) && mParamMethodMap.containsKey("setCameraMode")) {
            int cameraModeValue = 0;
            if (value.equalsIgnoreCase("on")) {
                cameraModeValue = 1;
            }
            set("setCameraMode", cameraModeValue);
        }
    }

    public boolean getZSLMode() {
        boolean isQcom = false;
        boolean isMtk  = false;
        boolean zslMode = true;
        String zsl = null;
        if (mParamMethodMap.containsKey("getZSDMode")) {
            zsl = get("getZSDMode");
            isMtk = true;
        } else if (mParamMethodMap.containsKey("getZSLMode")) {
            zsl = get("getZSLMode");
            isQcom = true;
        }
        if (TextUtils.equals(zsl, "on")) {
            return true;
        } else {
            return false;
        }
    }


    private String get(String method) {
        Method getMethod = mParamMethodMap.get(method);

        if (getMethod != null) {
            try {
                getMethod.setAccessible(true);
                return (String)getMethod.invoke(mParameters);
            } catch (Exception e) {
                Log.d(TAG, "set parameter failed!method:" + method.toString());
                e.printStackTrace();
            }
        }
        return null;
    }



    public static final String KEY_ISO_MODE = "iso";
    public static final String KEY_MIN_ISO = "min-iso";
    public static final String KEY_MAX_ISO = "max-iso";
    public static final String KEY_CONTINUOUS_ISO = "continuous-iso";
    public static final String KEY_MANUAL_ISO = "manual";
    public static final String KEY_AUTO_ISO = "auto";
    public static final String KEY_MIN_EXPOSURE_TIME = "min-exposure-time";
    public static final String KEY_MAX_EXPOSURE_TIME = "max-exposure-time";
    public static final String KEY_EXPOSURE_TIME = "exposure-time";
    public static final String KEY_ISO_VALUES="iso-values";


    public static final String KEY_MANUAL_FOCUS_POS_TYPE = "manual-focus-pos-type";
    public static final String KEY_MANUAL_FOCUS_POSITION = "manual-focus-position";
    public static final String KEY_MIN_FOCUS_SCALE = "min-focus-pos-ratio";
    public static final String KEY_MAX_FOCUS_SCALE = "max-focus-pos-ratio";
    public static final int MANUAL_FOCUS_POS_TYPE_SCALE = 2;
    public static final int MANUAL_FOCUS_POS_TYPE_DIOPTER = 3;
    public static final String FOCUS_MODE_MANUAL = "manual";
    public static final String KEY_FOCUS_MODE = "focus-mode";
    public static final String KEY_FOCUS_ENG_STEP = "afeng-pos";

    public void setISO(String isoValue){
        mParameters.set(KEY_ISO_MODE, isoValue);//only works for qualcomm
    }

    public void setManualISO(int isoValue){
        mParameters.set(KEY_CONTINUOUS_ISO, isoValue);//only works for qualcomm
    }


    public void setExposureTime(String exposureTime){
        mParameters.set(KEY_EXPOSURE_TIME, exposureTime);//only works for qualcomm
        if(ExtBuild.device() == ExtBuild.MTK_MT6755){
            mParameters.set("shutter-value",exposureTime);//only works for MTK document
        }
    }

    public int getMinISO(){
        if(mParameters.get(KEY_MIN_ISO)!=null) {
            return mParameters.getInt(KEY_MIN_ISO);
        }
        return 0;
    }

    public int getMaxISO(){
        if(mParameters.get(KEY_MAX_ISO)!=null){
            return mParameters.getInt(KEY_MAX_ISO);
        }
        if(ExtBuild.device() == ExtBuild.MTK_MT6755){
            return 1600;//only for MTK platform ,and the  value comes from  parameters flatten().
        }
        return 0;
    }

    /**
     * Query supported ISO values , if not supported , it would return an empty List (not null)
     * @return
     */
    public List<String> getSupportedISOValues(){
        List<String> supportedISOValues=new ArrayList<>();
        String isoValues=mParameters.get(KEY_ISO_VALUES);
        if(isoValues==null){
            isoValues = mParameters.get("iso-speed-values");
        }
        if(isoValues!=null){
            supportedISOValues=split(isoValues);
        }
        return supportedISOValues;

    }

    private static ArrayList<String> split(String str) {
        if (str == null) return null;

        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(',');
        splitter.setString(str);
        ArrayList<String> substrings = new ArrayList<String>();
        for (String s : splitter) {
            substrings.add(s);
        }
        return substrings;
    }

    public String getMinExposureTime(){

        String minExposureTime=mParameters.get(KEY_MIN_EXPOSURE_TIME);
        if(minExposureTime!=null){
            return minExposureTime;
        }
        return "0.0";
    }

    public String getMaxExposureTime(){
        String maxExposureTime=mParameters.get(KEY_MAX_EXPOSURE_TIME);
        if(maxExposureTime!=null){
            return maxExposureTime;
        }
        if(ExtBuild.device() == ExtBuild.MTK_MT6755){
            return "500.0";//only for MTK platform ,and the  value is ref-value.
        }
        return "0.0";
    }

    public void updateManualFocusPosition(int focusPosition){
        mParameters.set(KEY_MANUAL_FOCUS_POS_TYPE,MANUAL_FOCUS_POS_TYPE_SCALE);
        mParameters.set(KEY_MANUAL_FOCUS_POSITION, focusPosition);
        /* MODIFIED-BEGIN by peixin, 2016-06-20,BUG-2381322*/
        Log.i(TAG, "focusPosition value is : " + focusPosition);
        if(ExtBuild.device() == ExtBuild.MTK_MT6755){
            //only for MTK platform ,and the  value is ref-value.
            mParameters.set(KEY_FOCUS_MODE, FOCUS_MODE_MANUAL);
            mParameters.set(KEY_FOCUS_ENG_STEP, 1001-focusPosition);
            /* MODIFIED-END by peixin,BUG-2381322*/
        }
}

    public int getMinFocusScale(){
        if(mParameters.get(KEY_MIN_FOCUS_SCALE)!=null){
            return mParameters.getInt(KEY_MIN_FOCUS_SCALE);
        }
        return 0;
    }

    public int getMaxFocusScale(){
        if(mParameters.get(KEY_MAX_FOCUS_SCALE)!=null){
            return mParameters.getInt(KEY_MAX_FOCUS_SCALE);
        }
        if(ExtBuild.device() == ExtBuild.MTK_MT6755){
            return 1000;//only for MTK platform ,and the  value is ref-value.
        }
        return 0;
    }


    /*************************** Face Detection Associate Setting ********************/
    // This function is used by qcom
    public List<String> getSupportedFDModeValues() {
        return query("getSupportedFaceDetectionModes");
    }

    // This function is used by qcom
    public void setFaceDetectionMode(String value) {
        set("setFaceDetectionMode", value);
    }

    /*************************** Set Brightness Associate Setting ********************/
    // Set Brightness.
    public void SetBrightness(String value) {
        // TODO
        mParameters.set("luma-adaptation", value);
    }

    /*************************** TouchAf Associate Setting ********************/
    public List<String> getSupportedTouchAfAec() {
        // TODO
        return query("getSupportedTouchAfAec");
    }

    public void setTouchAfAec(String value) {
        // TODO
        set("setTouchAfAec", value);
    }

    /****************** Selectable Zone Af Associate Setting ********************/
    public List<String> getSupportedSelectableZoneAf() {
        // TODO
        return query("getSupportedSelectableZoneAf");
    }

    public void setSelectableZoneAf(String value) {
        // TODO
        set("setSelectableZoneAf", value);
    }

    /****************** wavelet denoise Associate Setting ********************/
    public List<String> getSupportedDenoiseModes() {
        // TODO
        return query("getSupportedDenoiseModes");
    }

    public void setDenoise(String value) {
        // TODO
        set("setDenoise", value);
    }

    /****************** Redeye Reduction Associate Setting ********************/
    public List<String> getSupportedRedeyeReductionModes() {
        // TODO
        return query("getSupportedRedeyeReductionModes");
    }

    public void setRedeyeReductionMode(String value) {
        // TODO
        set("setRedeyeReductionMode", value);
    }

    /****************** Set Saturation Associate Setting ********************/
    public void setSaturation(String value) {
        // TODO
        set("setSaturation", Integer.valueOf(value));
    }

    /****************** Set Saturation Associate Setting ********************/
    public void setContrast(String value) {
        // TODO
        set("setContrast", Integer.valueOf(value));
    }

    /****************** Set sharpness Associate Setting ********************/
    public void setSharpness(String value) {
        // TODO
        set("setSharpness", Integer.valueOf(value));
    }

    /****************** Set auto exposure Associate Setting ********************/
    public List<String> getSupportedAutoexposure() {
        // TODO
        return query("getSupportedAutoexposure");
    }

    public static enum ExposureMode{
        CENTER_WEIGHTED,
        FRAME_AVERAGE,
        SPOT_METERING,;


        @Override
        public String toString() {
            String parameterValue="";
            switch (this){
                case CENTER_WEIGHTED:
                    parameterValue="center-weighted";
                    break;
                case FRAME_AVERAGE:
                    parameterValue="frame-average";
                    break;
                case SPOT_METERING:
                    parameterValue="spot-metering";
                    break;
            }
            return parameterValue;
        }
    }

    public void setAutoExposure(ExposureMode mode) {
        String value=mode.toString();
        if(CameraUtil.isSupported(value, this.getSupportedAutoexposure())) {
            set("setAutoExposure", value);
        }
    }

    /********************set Video hdr**************************************************/
    public List<String> getSupportedVideoHDRModes(){
        //TODO;
        return query("getSupportedVideoHDRModes");
    }

    public void setVideoHDRMode(String value){
        //TODO
        set("setVideoHDRMode", value);
    }

    /*******************set burst shot in mtk *****************************************/
    private void setCaptureNum(int num){
    	set("setBurstShotNum",num);
    }

    /*******************set tintless*************************************************************/
    public void setTintless(String value){//value:"enable"/"disable"
        //TODO
        mParameters.set("tintless",value);
    }

    private static final String CAPTURE_MODE_CONTINUOUS_SHOT="continuousshot";
    private static final String CAPTURE_MODE_NORMAL="normal";
    public void setBurstShot(boolean enable){
    	final String setCaptureMode="setCaptureMode";
    	if(enable){
    		setCaptureNum(40);
    		set(setCaptureMode,CAPTURE_MODE_CONTINUOUS_SHOT);
    	}else{
    		setCaptureNum(1);
    		set(setCaptureMode,CAPTURE_MODE_NORMAL);
    	}
    }

    public static final String KEY_QC_SUPPORTED_FLIP_MODES = "flip-mode-values";

    public static List<String> getSupportedFlipMode(){
        String str = mParameters.get(KEY_QC_SUPPORTED_FLIP_MODES);
        if(str == null)
            return null;

        return split(str);
    }

    public static boolean isFlipSupported(String value){
        List<String> supported = getSupportedFlipMode();
        return supported == null ? false : supported.indexOf(value) >= 0;
    }

}
