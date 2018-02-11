package com.gapp.common.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

/**
 * 集成Android中相关的工具
 *
 * @author zhanghong
 * @time 2015-6-8
 */
public class AnimationUtils {
    private final static String TAG = "BitmapUtils";

    private final static int BMP_MAX_HEIGHT = 1920;
    private final static int BMP_MAX_WIDTH = 1080;


    private static boolean IsDebug = true;


    /**
     * 控制图片大小
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的宽度
        final int width = options.outWidth;
        final int height = options.outHeight;

        if (0 == reqWidth || reqWidth > BMP_MAX_WIDTH) {
            reqWidth = BMP_MAX_WIDTH;
        }

        if (0 == reqHeight || reqHeight > BMP_MAX_HEIGHT) {
            reqHeight = BMP_MAX_HEIGHT;
        }

        int inSampleSizeW = 1;
        int inSampleSizeH = 1;
        if (width > reqWidth && reqWidth > 0) {
            // 计算出实际宽度和目标宽度的比率
            inSampleSizeW = Math.round((float) width / (float) reqWidth);
        }

        if (height > reqHeight && reqHeight > 0) {
            inSampleSizeH = Math.round((float) height / (float) reqHeight);
        }

        if(width * height * 4 > 10*1024*1024){
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        }

        final int size = inSampleSizeW > inSampleSizeH ? inSampleSizeW : inSampleSizeH;

        return size;
    }

    /**
     * 获取图片所占内存
     *
     * @param bitmap
     * @return
     */
    public static int getBmpSize(Bitmap bitmap) {
        if (null == bitmap) {
            return 0;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {// API
            // 12
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public static Bitmap decodeSampledBitmapFromResource(Context context, int resId, int reqWidth, int reqHeight) {
        return decodeSampledBitmapFromResource(context.getResources(), resId, reqWidth, reqHeight);
    }

    /**
     * 从资源获取图片
     *
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeBitmap(Resources res, int resId) {
        return decodeSampledBitmapFromResource(res, resId, 0, 0);
    }

    /**
     * 从路径获取图片
     *
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromPath(String pathName, int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }


}
