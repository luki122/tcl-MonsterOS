package com.monster.market.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by xiaobin on 16-8-15.
 */
public class BitmapUtil {

    /**
     * 将指定的图片旋转指定的角度
     *
     * @param bitmap
     * @param angle
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int angle) {
        if (bitmap != null && angle > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            Bitmap roatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        /*    if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }*/
            bitmap = roatedBitmap;
        }

        return bitmap;
    }

}
