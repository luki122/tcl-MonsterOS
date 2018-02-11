package com.morpho.core;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import android.graphics.Bitmap;
import android.util.Log;

import com.android.classloader.CommonInstruction;

public class MorphoPanoramaGP implements CommonInstruction{
    // load library.
    private static final String TAG="MorphoPanoramaGP";
            
    static {
        try {
            System.loadLibrary("morpho_panorama_gp");
            Log.e(TAG, "load morpho lib success");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "load morpho pano lib failed :"+e.getMessage());
        }
    }
    
    public static final String createNativeObject="createNativeObject";
    public static final String deleteNativeObject="deleteNativeObject";
    public static final String nativeGetVersion="nativeGetVersion";
    public static final String nativeCalcImageSize="nativeCalcImageSize";
    public static final String nativeInitialize="nativeInitialize";
    public static final String nativeFinish="nativeFinish";
    public static final String nativeStart="nativeStart";
    public static final String nativeAttachPreview="nativeAttachPreview";
    public static final String nativeAttachStillImage="nativeAttachStillImage";
    public static final String nativeAttachStillImageExt="nativeAttachStillImageExt";
    public static final String nativeAttachStillImageRaw="nativeAttachStillImageRaw";
    public static final String nativeSetJpegForCopyingExif="nativeSetJpegForCopyingExif";
    public static final String nativeEnd="nativeEnd";
    public static final String nativeGetBoundingRect="nativeGetBoundingRect";
    public static final String nativeGetClippingRect="nativeGetClippingRect";
    public static final String nativeGetMotionlessThreshold="nativeGetMotionlessThreshold";
    public static final String nativeGetUsedHeapSize="nativeGetUsedHeapSize";
    public static final String nativeGetUseSensorAssist="nativeGetUseSensorAssist";
    public static final String nativeSetMotionlessThreshold="nativeSetMotionlessThreshold";
    public static final String nativeSetUseSensorAssist="nativeSetUseSensorAssist";
    public static final String nativeSetAngleMatrix="nativeSetAngleMatrix";
    public static final String nativeGetCurrentDirection="nativeGetCurrentDirection";
    public static final String setBrightnessCorrection="setBrightnessCorrection";
    public static final String nativeSetUseSensorThreshold="nativeSetUseSensorThreshold";
    public static final String nativeGetGuidancePos="nativeGetGuidancePos";
    public static final String nativeGetImageSize="nativeGetImageSize";
    public static final String nativeSaveOutputJpeg="nativeSaveOutputJpeg";
    public static final String nativeSaveJpeg="nativeSaveJpeg";
    public static final String nativeDecodeJpeg="nativeDecodeJpeg";
    private static final String nativeSetTooFastThreshold = "nativeSetTooFastThreshold";



    @Override
    public Callable<Object> getFunctionPointer(final String msg, final Object... parameters) {
        Callable<Object> callable=new Callable<Object>(){
            @Override
            public Object call() throws Exception {
                switch(msg){
                case createNativeObject:{
                    return createNativeObject();
                }
                case deleteNativeObject:{
                    long mNative=(long)parameters[0];
                    deleteNativeObject(mNative);
                    return null;
                }
                case nativeGetVersion:{
                    return nativeGetVersion();
                }
                case nativeCalcImageSize:{
                    InitParam param=(InitParam)parameters[0];
                    double goal_angle=(double)parameters[1];
                    return nativeCalcImageSize(param,goal_angle);
                }
                case nativeInitialize:{
                    long mNative=(long)parameters[0];
                    InitParam param=(InitParam)parameters[1];
                    int[] buffer_size=(int[])parameters[2];
                    return nativeInitialize(mNative,param,buffer_size);
                }
                case nativeFinish:{
                    long mNative=(long)parameters[0];
                    return nativeFinish(mNative);
                }
                case nativeStart:{
                    long mNative=(long)parameters[0];
                    return nativeStart(mNative);
                }
                case nativeAttachPreview:{
                    long mNative=(long)parameters[0];
                    byte[] input_image=(byte[])parameters[1];
                    int use_image=(int)parameters[2];
                    int[] image_id=(int[])parameters[3];
                    byte[] motion_data=(byte[])parameters[4];
                    int[] status=(int[])parameters[5];
                    Bitmap preview_image=(Bitmap)parameters[6];
                    return nativeAttachPreview(mNative, input_image, use_image, image_id, motion_data, status, preview_image);
                }
                case  nativeAttachStillImage:{
                    long mNative=(long)parameters[0];
                    byte[] input_image=(byte[])parameters[1];
                    int image_id=(int)parameters[2];
                    byte[] motion_data=(byte[])parameters[3];
                    return nativeAttachStillImage(mNative, input_image, image_id, motion_data);
                }
                case nativeAttachStillImageExt:{
                    long mNative=(long)parameters[0];
                    ByteBuffer input_image=(ByteBuffer)parameters[1];
                    int image_id=(int)parameters[2];
                    ByteBuffer motion_data=(ByteBuffer)parameters[3];
                    return nativeAttachStillImageExt(mNative, input_image, image_id, motion_data);
                }
                case nativeAttachStillImageRaw:{
                    long mNative=(long)parameters[0];
                    ByteBuffer input_image=(ByteBuffer)parameters[1];
                    int image_id=(int)parameters[2];
                    ByteBuffer motion_data=(ByteBuffer)parameters[3];
                    return nativeAttachStillImageRaw(mNative, input_image, image_id, motion_data);
                }
                case nativeSetJpegForCopyingExif:{
                    long mNative=(long)parameters[0];
                    ByteBuffer input_image=(ByteBuffer)parameters[1];
                    return nativeSetJpegForCopyingExif(mNative, input_image);
                }
                case nativeEnd:{
                    long mNative=(long)parameters[0];
                    return nativeEnd(mNative);
                }
                case nativeGetBoundingRect:{
                    long mNative=(long)parameters[0];
                    int[] rect_info=(int[])parameters[1];
                    return nativeGetBoundingRect(mNative,rect_info);
                }
                case nativeGetClippingRect:{
                    long mNative=(long)parameters[0];
                    int[] rect_info=(int[])parameters[1];
                    return nativeGetClippingRect(mNative, rect_info);
                }
                case nativeGetMotionlessThreshold:{
                    long mNative=(long)parameters[0];
                    int[] motionless_threshold=(int[])parameters[1];
                    return nativeGetMotionlessThreshold(mNative, motionless_threshold);
                }
                case nativeGetUsedHeapSize:{
                    long mNative=(long)parameters[0];
                    int[] used_heap_size=(int[])parameters[1];
                    return nativeGetUsedHeapSize(mNative, used_heap_size);
                }
                case nativeGetUseSensorAssist:{
                    long mNative=(long)parameters[0];
                    int use_case=(int)parameters[1];
                    int[] enable=(int[])parameters[2];
                    return nativeGetUseSensorAssist(mNative, use_case, enable);
                }
                case nativeSetMotionlessThreshold:{
                    long mNative=(long)parameters[0];
                    int motionless_threshold=(int)parameters[1];
                    return nativeSetMotionlessThreshold(mNative, motionless_threshold);
                }
                case nativeSetUseSensorAssist:{
                    long mNative=(long)parameters[0];
                    int use_case=(int)parameters[1];
                    int enable=(int)parameters[2];
                    return nativeSetUseSensorAssist(mNative, use_case, enable);
                }
                case nativeSetAngleMatrix:{
                    long mNative=(long)parameters[0];
                    double[] matrix=(double[])parameters[1];
                    int sensor_type=(int)parameters[2];
                    return nativeSetAngleMatrix(mNative, matrix, sensor_type);
                }
                case nativeGetCurrentDirection:{
                    long mNative=(long)parameters[0];
                    int[] direction=(int[])parameters[1];
                    return nativeGetCurrentDirection(mNative, direction);
                }
                case setBrightnessCorrection:{
                    long mNative=(long)parameters[0];
                    int correct=(int)parameters[1];
                    return setBrightnessCorrection(mNative, correct);
                }
                case nativeSetUseSensorThreshold:{
                    long mNative=(long)parameters[0];
                    int threshold=(int)parameters[1];
                    return nativeSetUseSensorThreshold(mNative, threshold);
                }
                case nativeGetGuidancePos:{
                    long mNative=(long)parameters[0];
                    int[] pos=(int[])parameters[1];
                    return nativeGetGuidancePos(mNative, pos);
                }
                case nativeGetImageSize:{
                    long mNative=(long)parameters[0];
                    int[] previewSize=(int[])parameters[1];
                    int[] resultSize=(int[])parameters[2];
                    return nativeGetImageSize(mNative, previewSize, resultSize);
                }
                case nativeSaveOutputJpeg:{
                    long mNative=(long)parameters[0];
                    String path=(String)parameters[1];
                    int left=(int)parameters[2];
                    int top=(int)parameters[3];
                    int right=(int)parameters[4];
                    int bottom=(int)parameters[5];
                    int orientation=(int)parameters[6];
                    int[] progress=(int[])parameters[7];
                    return nativeSaveOutputJpeg(mNative, path, left, top, right, bottom, orientation, progress);
                }
                case nativeSaveJpeg:{
                    String path=(String)parameters[0];
                    byte[] raw_data=(byte[])parameters[1];
                    String format=(String)parameters[2];
                    int width=(int)parameters[3];
                    int height=(int)parameters[4];
                    int orientation=(int)parameters[5];
                    return nativeSaveJpeg(path, raw_data, format, width, height, orientation);
                }
                case nativeDecodeJpeg:{
                    String path=(String)parameters[0];
                    byte[] output_data=(byte[])parameters[1];
                    String format=(String)parameters[2];
                    int width=(int)parameters[3];
                    int height=(int)parameters[4];
                    return nativeDecodeJpeg(path, output_data, format, width, height);
                }
                case nativeSetTooFastThreshold:{
                    long mNative=(long)parameters[0];
                    int too_fast_threshold=(int)parameters[1];
                    return nativeSetTooFastThreshold(mNative, too_fast_threshold);
                }
                default:
                    return null;
                }
            }

        };
        return callable;
    }
    private final native long createNativeObject();
    private final native void deleteNativeObject(long mNative);
    private final native static String nativeGetVersion();
    private final native static int nativeCalcImageSize(InitParam param, double goal_angle);
    private final native int nativeInitialize(long mNative, InitParam param, int[] buffer_size);
    private final native int nativeFinish(long mNative);
    private final native int nativeStart(long mNative);
    private final native int nativeAttachPreview(long mNative, byte[] input_image, int use_image, int[] image_id, byte[] motion_data, int[] status, Bitmap preview_image);
    private final native int nativeAttachStillImage(long mNative, byte[] input_image, int image_id, byte[] motion_data);
    private final native int nativeAttachStillImageExt(long mNative, ByteBuffer input_image, int image_id, ByteBuffer motion_data);
    private final native int nativeAttachStillImageRaw(long mNative, ByteBuffer input_image, int image_id, ByteBuffer motion_data);
    private final native int nativeSetJpegForCopyingExif(long mNative, ByteBuffer input_image);
    private final native int nativeEnd(long mNative);
    private final native int nativeGetBoundingRect(long mNative, int[] rect_info);
    private final native int nativeGetClippingRect(long mNative, int[] rect_info);
    private final native int nativeGetMotionlessThreshold(long mNative, int[] motionless_threshold);
    private final native int nativeGetUsedHeapSize(long mNative, int[] used_heap_size);
    private final native int nativeGetUseSensorAssist(long mNative, int use_case, int[] enable);
    private final native int nativeSetMotionlessThreshold(long mNative, int motionless_threshold);
    private final native int nativeSetUseSensorAssist(long mNative, int use_case, int enable);
    private final native int nativeSetAngleMatrix(long mNative, double[] matrix, int sensor_type);
    private final native int nativeGetCurrentDirection(long mNative, int[] direction);
    private final native int setBrightnessCorrection(long mNative, int correct);
    private final native int nativeSetUseSensorThreshold(long mNative, int threshold);
    private final native int nativeGetGuidancePos(long mNative, int[] pos);
    private final native int nativeGetImageSize(long mNative, int[] previewSize, int[] resultSize);
    private final native int nativeSaveOutputJpeg(long mNative, String path, int left, int top, int right, int bottom, int orientation, int[] progress);
    private final native static int nativeSaveJpeg(String path, byte[] raw_data, String format, int width, int height, int orientation);
    private final native static int nativeDecodeJpeg(String path, byte[] output_data, String format, int width, int height);
    private final native int nativeSetTooFastThreshold(long mNative, int too_fast_threshold);
}
