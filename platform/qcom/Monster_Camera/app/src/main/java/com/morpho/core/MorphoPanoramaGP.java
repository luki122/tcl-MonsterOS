/* Copyright (C) 2016 Tcl Corporation Limited */
package com.morpho.core;

import android.graphics.Bitmap;
import android.util.Log;

import com.morpho.core.InitParam;

import java.nio.ByteBuffer;


public class MorphoPanoramaGP {
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

    public final native int createNativeObject();
    public final native void deleteNativeObject(int mNative);
    public final native static String nativeGetVersion();
    public final native static int nativeCalcImageSize(InitParam param, double goal_angle);
    public final native int nativeInitialize(int mNative, InitParam param, int[] buffer_size);
    public final native int nativeFinish(int mNative);
    public final native int nativeStart(int mNative);
    public final native int nativeAttachPreview(int mNative, byte[] input_image, int use_image, int[] image_id, byte[] motion_data, int[] status, Bitmap preview_image);
    public final native int nativeAttachStillImage(int mNative, byte[] input_image, int image_id, byte[] motion_data);
    public final native int nativeAttachStillImageExt(int mNative, ByteBuffer input_image, int image_id, ByteBuffer motion_data);
    public final native int nativeAttachStillImageRaw(int mNative, ByteBuffer input_image, int image_id, ByteBuffer motion_data);
    public final native int nativeSetJpegForCopyingExif(int mNative, ByteBuffer input_image);
    public final native int nativeEnd(int mNative);
    public final native int nativeGetBoundingRect(int mNative, int[] rect_info);
    public final native int nativeGetClippingRect(int mNative, int[] rect_info);
    public final native int nativeGetMotionlessThreshold(int mNative, int[] motionless_threshold);
    public final native int nativeGetUsedHeapSize(int mNative, int[] used_heap_size);
    public final native int nativeGetUseSensorAssist(int mNative, int use_case, int[] enable);
    public final native int nativeSetMotionlessThreshold(int mNative, int motionless_threshold);
    public final native int nativeSetUseSensorAssist(int mNative, int use_case, int enable);
    public final native int nativeSetAngleMatrix(int mNative, double[] matrix, int sensor_type);
    public final native int nativeGetCurrentDirection(int mNative, int[] direction);
    public final native int setBrightnessCorrection(int mNative, int correct);
    public final native int nativeSetUseSensorThreshold(int mNative, int threshold);
    public final native int nativeGetGuidancePos(int mNative, int[] pos);
    public final native int nativeGetImageSize(int mNative, int[] previewSize, int[] resultSize);
    public final native int nativeSaveOutputJpeg(int mNative, String path, int left, int top, int right, int bottom, int orientation, int[] progress);
    public final native static int nativeSaveJpeg(String path, byte[] raw_data, String format, int width, int height, int orientation);
    public final native static int nativeDecodeJpeg(String path, byte[] output_data, String format, int width, int height);
    public final native int nativeSetTooFastThreshold(int mNative, int too_fast_threshold);
}
