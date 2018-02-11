/* MODIFIED-BEGIN by yuanxing.tan, 2016-09-12, BUG-2861353*/
/* Copyright (C) 2016 Tcl Corporation Limited */


package com.morpho.core.wrapper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;

import com.android.camera.debug.Log;
import com.morpho.core.Error;
/* MODIFIED-BEGIN by yuanxing.tan, 2016-08-10,BUG-2705515*/
import com.morpho.core.InitParam;
import com.morpho.core.MorphoPanoramaGP;

import java.nio.ByteBuffer;
/* MODIFIED-END by yuanxing.tan,BUG-2705515*/

public class MorphoPanoramaGPWrapper {
    private static final Log.Tag TAG=new Log.Tag("MorphoPanoGP");


    public static final int GUIDE_TYPE_HORIZONTAL       = 0;
    public static final int GUIDE_TYPE_VERTICAL         = 1;
/* MODIFIED-BEGIN by yuanxing.tan, 2016-09-12,BUG-2861353*/

    public static final int DIRECTION_HORIZONTAL        = 0;    /* 撮影開始後、方向確定 */
    public static final int DIRECTION_VERTICAL          = 1;    /* 撮影開始後、方向確定 */
    public static final int DIRECTION_HORIZONTAL_LEFT   = 2;
    public static final int DIRECTION_HORIZONTAL_RIGHT  = 3;
    public static final int DIRECTION_VERTICAL_UP       = 4;
    public static final int DIRECTION_VERTICAL_DOWN     = 5;
    public static final int DIRECTION_AUTO              = 6;	/* 撮影開始後、方向確定 */


    /* MODIFIED-END by yuanxing.tan,BUG-2861353*/
    public static final int STATUS_STITCHING                = 0;
    public static final int STATUS_OUT_OF_MEMORY            = 1;
    public static final int STATUS_ALIGN_FAILURE            = 2;
    public static final int STATUS_STOPPED_BY_ERROR         = 3;
    public static final int STATUS_WARNING_NEED_TO_STOP     = 4;
    public static final int STATUS_WARNING_TOO_FAST         = 5;
    public static final int STATUS_WARNING_TOO_FAR          = 6;
    public static final int STATUS_WARNING_ALIGN_FAILURE    = 7;
    public static final int STATUS_WARNING_TOO_FAR_1        = 8;
    public static final int STATUS_WARNING_TOO_FAR_2        = 9;
    public static final int STATUS_WARNING_REVERSE          = 10;
    public static final int STATUS_WHOLE_AREA_COMPLETE      = 11;

    public static final int USE_IMAGE_NONE     = -1;
    public static final int USE_IMAGE_NORMAL   = 0;
    public static final int USE_IMAGE_FORCE    = 1;

    public static final int ROTATE_0   = 0;
    public static final int ROTATE_90  = 1;
    public static final int ROTATE_180 = 2;
    public static final int ROTATE_270 = 3;

    public static final int SENSOR_TYPE_GYROSCOPE       = 0;
    public static final int SENSOR_TYPE_ROTATION_VECTOR = 1;
/* MODIFIED-BEGIN by yuanxing.tan, 2016-09-12,BUG-2861353*/

    public static final int USE_SENSOR_FOR_ALIGNMENT_WHEN_FAILED = 0;

    // private.
    private int mNative = 0;
    /* MODIFIED-END by yuanxing.tan,BUG-2861353*/

    // for Rect class.
    private static final int RECT_LEFT_OFFSET   = 0;
    private static final int RECT_TOP_OFFSET    = 1;
    private static final int RECT_RIGHT_OFFSET  = 2;
    private static final int RECT_BOTTOM_OFFSET = 3;
    private static final int RECT_INFO_SIZE     = 4;

    public static final int STILL_IMAGE_FORMAT_JPEG     = ImageFormat.JPEG;
    public static final int STILL_IMAGE_FORMAT_YVU420SP = ImageFormat.NV21;

    public static class ImageSize {
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-09-12,BUG-2861353*/
        public int width;
        public int height;
        public void setSize(int w, int h) { width = w; height = h; }
        /* MODIFIED-END by yuanxing.tan,BUG-2861353*/
    }

//    public static class MotionData {
//        public byte[] data;
//        public MotionData(){
//            data = new byte[256];
//        }
//    }

    public String getVersion()
    {
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-08-10,BUG-2705515*/
        return mMorphoPanoramaGP.nativeGetVersion();
    }

    public int calcImageSize(InitParam param, double goal_angle)
    {
        return mMorphoPanoramaGP.nativeCalcImageSize(param, goal_angle);
    }

    public int saveJpeg(String path, byte[] raw_data, String format, int width, int height, int orientation)
    {
        return mMorphoPanoramaGP.nativeSaveJpeg(path, raw_data, format, width, height, orientation);
    }

    public int decodeJpeg(String path, byte[] output_data, String format, int width, int height)
    {
        return mMorphoPanoramaGP.nativeDecodeJpeg(path, output_data, format, width, height);
    }

    private MorphoPanoramaGP mMorphoPanoramaGP;
    private Context mContext;
    public MorphoPanoramaGPWrapper(Context context){
        mContext=context;
        mMorphoPanoramaGP = new MorphoPanoramaGP();
        int ret = mMorphoPanoramaGP.createNativeObject();
        if (ret != 0)
        {
            mNative = ret;
        } else {
            mNative = 0;
            ret = com.morpho.core.Error.ERROR_MALLOC;
        }
    }

    public int initialize(InitParam param, int[] buffer_size)
    {
        int ret = Error.MORPHO_OK;
        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeInitialize(mNative, param, buffer_size); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int finish()
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            /* MODIFIED-BEGIN by yuanxing.tan, 2016-08-10,BUG-2705515*/
            ret = mMorphoPanoramaGP.nativeFinish(mNative);
            mMorphoPanoramaGP.deleteNativeObject(mNative);
            /* MODIFIED-END by yuanxing.tan,BUG-2705515*/
            mNative = 0;
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }
    /* MODIFIED-BEGIN by yuanxing.tan, 2016-09-12,BUG-2861353*/
    //too_fast_thresholdå–å€¼èŒƒå›´0-32768ï¼Œå€¼è¶Šå°è¶Šçµæ•ï¼Œæˆ‘æµ‹è¯•çš„æ—¶å€™2500ç”¨èµ·æ¥çµæ•åº¦è¿˜å¥½ã€‚
    public int setTooFastThreshold(int too_fast_threshold)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeSetTooFastThreshold(mNative, too_fast_threshold); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
            /* MODIFIED-END by yuanxing.tan,BUG-2861353*/
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int start()
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeStart(mNative); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int attachPreview(byte[] input_image, int use_image, int[] image_id, byte[] motion_data, int[] status, Bitmap preview_image)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeAttachPreview(mNative, input_image, use_image, image_id, motion_data, status, preview_image); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int attachStillImage(byte[] input_image, int image_id, byte[] motion_data)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeAttachStillImage(mNative, input_image, image_id, motion_data); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int attachStillImageExt(ByteBuffer input_image, int image_id, ByteBuffer motion_data)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeAttachStillImageExt(mNative, input_image, image_id, motion_data); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int attachStillImageRaw(ByteBuffer input_image, int image_id, ByteBuffer motion_data)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeAttachStillImageRaw(mNative, input_image, image_id, motion_data); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int attachSetJpegForCopyingExif(ByteBuffer input_image)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeSetJpegForCopyingExif(mNative, input_image); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int end()
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeEnd(mNative); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int getBoundingRect(Rect rect)
    {
        int ret = Error.MORPHO_OK;
        int[] rect_info = new int[RECT_INFO_SIZE];

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeGetBoundingRect(mNative, rect_info); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
            if (ret == Error.MORPHO_OK)
            {
                rect.set(rect_info[RECT_LEFT_OFFSET],
                        rect_info[RECT_TOP_OFFSET],
                        rect_info[RECT_RIGHT_OFFSET],
                        rect_info[RECT_BOTTOM_OFFSET]);
            }
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        if (ret != Error.MORPHO_OK)
        {
            rect.set(0, 0, 0, 0);
        }

        return ret;
    }

    public int getClippingRect(Rect rect)
    {
        int ret = Error.MORPHO_OK;
        int[] rect_info = new int[RECT_INFO_SIZE];

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeGetClippingRect(mNative, rect_info); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
            if (ret == Error.MORPHO_OK)
            {
                rect.set(rect_info[RECT_LEFT_OFFSET],
                        rect_info[RECT_TOP_OFFSET],
                        rect_info[RECT_RIGHT_OFFSET],
                        rect_info[RECT_BOTTOM_OFFSET]);
            }
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        if (ret != Error.MORPHO_OK)
        {
            rect.set(0, 0, 0, 0);
        }

        return ret;
    }

    public int getUsedHeapSize(int[] used_heap_size)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeGetUsedHeapSize(mNative, used_heap_size); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int getUseSensorAssist(int use_case, int[] enable)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeGetUseSensorAssist(mNative, use_case, enable); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int setMotionlessThreshold(int motionless_threshold)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeSetMotionlessThreshold(mNative, motionless_threshold); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int setAngleMatrix(double[] matrix, int sensor_type)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeSetAngleMatrix(mNative, matrix, sensor_type); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int getCurrentDirection(int[] direction)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeGetCurrentDirection(mNative, direction); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int setUseSensorAssist(int use_case, int enable)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeSetUseSensorAssist(mNative, use_case, enable); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int setBrightnessCorrection(int corect)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.setBrightnessCorrection(mNative, corect); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int setUseSensorThreshold(int threshold)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeSetUseSensorThreshold(mNative, threshold); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int getGuidancePos(Point attached, Point guide)
    {
        int ret = Error.MORPHO_OK;
        int[] pos = new int[4];

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeGetGuidancePos(mNative, pos); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
            attached.set(pos[0], pos[1]);
            guide.set(pos[2], pos[3]);
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int getImageSize(ImageSize sPreview, ImageSize sOutput)
    {
        int ret = Error.MORPHO_OK;
        int[] preview = new int[2];
        int[] output = new int[2];

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeGetImageSize(mNative, preview, output); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
            sPreview.setSize(preview[0], preview[1]);
            sOutput.setSize(output[0], output[1]);
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int saveOutputJpeg(String path, Rect rect, int orientation, int[] progress)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = mMorphoPanoramaGP.nativeSaveOutputJpeg(mNative, path, rect.left, rect.top, rect.right, rect.bottom, orientation , progress); // MODIFIED by yuanxing.tan, 2016-08-10,BUG-2705515
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }
}
/* MODIFIED-END by yuanxing.tan,BUG-2861353*/
