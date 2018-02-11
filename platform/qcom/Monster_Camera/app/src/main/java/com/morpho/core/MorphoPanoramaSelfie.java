

package com.morpho.core;

import java.nio.ByteBuffer;


import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;

import com.android.camera.debug.Log;

public class MorphoPanoramaSelfie {

    private static final Log.Tag TAG = new Log.Tag("MorPanoSelfie");

    // load library.
    static {
        try {
            System.loadLibrary("morpho_panorama_selfie");
        } catch (UnsatisfiedLinkError e) {
//            DebugLog.e("MorphoPanoramaSelfie", "can't loadLibrary \r\n" + e.getMessage());
            Log.e(TAG, "can't loadLibrary \r\n" + e.getMessage());
        }
    }

    public static final int GUIDE_TYPE_HORIZONTAL       = 0;
    public static final int GUIDE_TYPE_VERTICAL         = 1;

    public static final int DIRECTION_HORIZONTAL        = 0;    /* 撮影開始後、方向確定 */
    public static final int DIRECTION_VERTICAL          = 1;    /* 撮影開始後、方向確定 */
    public static final int DIRECTION_HORIZONTAL_LEFT   = 2;
    public static final int DIRECTION_HORIZONTAL_RIGHT  = 3;
    public static final int DIRECTION_VERTICAL_UP       = 4;
    public static final int DIRECTION_VERTICAL_DOWN     = 5;
    public static final int DIRECTION_HORIZONTAL_CENTER        = 6;
    public static final int DIRECTION_VERTICAL_CENTER          = 7;
    public static final int DIRECTION_AUTO              = 8;    /* 撮影開始後、方向確定 */


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
    public static final int STATUS_WHOLE_AREA_COMPLETE      = 10;

    public static final int USE_IMAGE_NONE     = -1;
    public static final int USE_IMAGE_NORMAL   = 0;
    public static final int USE_IMAGE_FORCE    = 1;

    public static final int ROTATE_0   = 0;
    public static final int ROTATE_90  = 1;
    public static final int ROTATE_180 = 2;
    public static final int ROTATE_270 = 3;

    public static final int SENSOR_TYPE_GYROSCOPE       = 0;
    public static final int SENSOR_TYPE_ROTATION_VECTOR = 1;

    public static final int USE_SENSOR_FOR_ALIGNMENT_WHEN_FAILED = 0;

    // private.
    private long mNative = 0;

    // for Rect class.
    private static final int RECT_LEFT_OFFSET   = 0;
    private static final int RECT_TOP_OFFSET    = 1;
    private static final int RECT_RIGHT_OFFSET  = 2;
    private static final int RECT_BOTTOM_OFFSET = 3;
    private static final int RECT_INFO_SIZE     = 4;

    public static final int STILL_IMAGE_FORMAT_JPEG     = ImageFormat.JPEG;
    public static final int STILL_IMAGE_FORMAT_YVU420SP = ImageFormat.NV21;

    // Use the param in InitParam.java.
//    public static class InitParam {
//        public int mode;
//        public String format;
//        public int direction;
//        public int preview_width;
//        public int preview_height;
//        public int still_width;
//        public int still_height;
//
//        public double angle_of_view_degree;
//        public int preview_shrink_ratio;
//        public int preview_img_width;
//        public int preview_img_height;
//        public int dst_img_width;
//        public int dst_img_height;
//        public int output_rotation;
//        public int draw_cur_image;
//        public int use_threshold;
//        public InitParam(){
//        }
//    };

    public static class ImageSize {
        public int width;
        public int height;
        public void setSize(int w, int h) { width = w; height = h; }
    };

    public static class GuidePositions {
        public int num;
        public Point[] p = new Point[2];
        public GuidePositions() {
            p[0] = new Point();
            p[1] = new Point();
        }
    };

//    public static class MotionData {
//        public byte[] data;
//        public MotionData(){
//            data = new byte[256];
//        }
//    }

    public static String getVersion()
    {
        return nativeGetVersion();
    }

    public static int calcImageSize(InitParam param, double goal_angle)
    {
        return nativeCalcImageSize(param, goal_angle);
    }

    public static int saveJpeg(String path, byte[] raw_data, String format, int width, int height, int orientation)
    {
        return nativeSaveJpeg(path, raw_data, format, width, height, orientation);
    }

    public static int decodeJpeg(String path, byte[] output_data, String format, int width, int height)
    {
        return nativeDecodeJpeg(path, output_data, format, width, height);
    }

    public MorphoPanoramaSelfie(){
        int ret = Error.MORPHO_OK;
        mNative = createNativeObject();
        if (mNative == 0)
        {
            ret = Error.ERROR_MALLOC;
        }
    }

    public int initialize(InitParam param, int[] buffer_size)
    {
        int ret = Error.MORPHO_OK;
        if (mNative != 0)
        {
            ret = nativeInitialize(mNative, param, buffer_size);
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
            ret = nativeFinish(mNative);
            deleteNativeObject(mNative);
            mNative = 0;
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
            ret = nativeStart(mNative);
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
            ret = nativeAttachPreview(mNative, input_image, use_image, image_id, motion_data, status, preview_image);
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int setFaceRect(int face_num, Rect[] rect)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = nativeSetFaceRect(mNative, face_num, rect);
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
            ret = nativeAttachStillImage(mNative, input_image, image_id, motion_data);
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
            ret = nativeAttachStillImageExt(mNative, input_image, image_id, motion_data);
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
            ret = nativeAttachStillImageRaw(mNative, input_image, image_id, motion_data);
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
            ret = nativeSetJpegForCopyingExif(mNative, input_image);
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
            ret = nativeEnd(mNative);
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
            ret = nativeGetBoundingRect(mNative, rect_info);
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
            ret = nativeGetClippingRect(mNative, rect_info);
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
            ret = nativeGetUsedHeapSize(mNative, used_heap_size);
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
            ret = nativeGetUseSensorAssist(mNative, use_case, enable);
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
            ret = nativeSetMotionlessThreshold(mNative, motionless_threshold);
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
            ret = nativeSetAngleMatrix(mNative, matrix, sensor_type);
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
            ret = nativeGetCurrentDirection(mNative, direction);
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
            ret = nativeSetUseSensorAssist(mNative, use_case, enable);
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
            ret = setBrightnessCorrection(mNative, corect);
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
            ret = nativeSetUseSensorThreshold(mNative, threshold);
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int getGuidancePos(Point attached, GuidePositions guide)
    {
        int ret = Error.MORPHO_OK;
        int[] pos = new int[2+1+2*2]; // it' means Attach(x,y), GuideNum, Guide(x,y) * 2

        if (mNative != 0)
        {
            ret = nativeGetGuidancePos(mNative, pos);
            attached.set(pos[0], pos[1]);
            guide.num = pos[2];
            guide.p[0].set(pos[3], pos[4]);
            if (pos[2] > 1) {
                guide.p[1].set(pos[5], pos[6]);
            }
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
            ret = nativeGetImageSize(mNative, preview, output);
            sPreview.setSize(preview[0], preview[1]);
            sOutput.setSize(output[0], output[1]);
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    public int saveOutputJpeg(String path, Rect rect, int orientation, boolean beMirror)
    {
        int ret = Error.MORPHO_OK;

        if (mNative != 0)
        {
            ret = nativeSaveOutputJpeg(mNative, path, rect.left, rect.top, rect.right, rect.bottom, orientation, beMirror);
        }
        else
        {
            ret = Error.ERROR_STATE;
        }

        return ret;
    }

    private final native long createNativeObject();
    private final native void deleteNativeObject(long mNative);

    private final native static String nativeGetVersion();
    private final native static int nativeCalcImageSize(InitParam param, double goal_angle);

    private final native int nativeInitialize(long mNative, InitParam param, int[] buffer_size);
    private final native int nativeFinish(long mNative);
    private final native int nativeStart(long mNative);
    private final native int nativeSetFaceRect(long mNative, int face_num, Rect[] rect);
    private final native int nativeAttachPreview(long mNative, byte[] input_image, int use_image, int[] image_id, byte[] motion_data, int[] status, Bitmap preview_image);
    private final native int nativeAttachStillImage(long mNative, byte[] input_image, int image_id, byte[] motion_data);
    private final native int nativeAttachStillImageExt(long mNative, ByteBuffer input_image, int image_id, ByteBuffer motion_data);
    private final native int nativeAttachStillImageRaw(long mNative, ByteBuffer input_image, int image_id, ByteBuffer motion_data);
    private final native int nativeSetJpegForCopyingExif(long mNative, ByteBuffer input_image);
    private final native int nativeEnd(long mNative);
    private final native int nativeGetBoundingRect(long mNative, int[] rect_info);
    private final native int nativeGetClippingRect(long mNative, int[] rect_info);
    private final native int nativeGetMotionlessThreshold(long mNative, int[] motionless_threshold);
    private final native int nativeGetUseThreshold(long mNative, int[] use_threshold);
    private final native int nativeGetUsedHeapSize(long mNative, int[] used_heap_size);
    private final native int nativeGetUseSensorAssist(long mNative, int use_case, int[] enable);
    private final native int nativeSetMotionlessThreshold(long mNative, int motionless_threshold);
    private final native int nativeSetUseThreshold(long mNative, int use_threshold);
    private final native int nativeSetUseSensorAssist(long mNative, int use_case, int enable);
    private final native int nativeSetAngleMatrix(long mNative, double[] matrix, int sensor_type);
    private final native int nativeGetCurrentDirection(long mNative, int[] direction);
    private final native int setBrightnessCorrection(long mNative, int correct);
    private final native int nativeSetUseSensorThreshold(long mNative, int threshold);
    private final native int nativeGetGuidancePos(long mNative, int[] pos);
    private final native int nativeGetImageSize(long mNative, int[] previewSize, int[] resultSize);

    private final native int nativeSaveOutputJpeg(long mNative, String path, int left, int top, int right, int bottom, int orientation, boolean beMirror);
    private final native static int nativeSaveJpeg(String path, byte[] raw_data, String format, int width, int height, int orientation);
    private final native static int nativeDecodeJpeg(String path, byte[] output_data, String format, int width, int height);
}
