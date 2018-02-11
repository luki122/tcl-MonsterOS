package com.monster.launchericon.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by antino on 16-11-9.
 */
public class PhotoUtils {
    public static Bitmap drawable2bitmap(Drawable dw) {
        //TODO:???? is best?
        if(dw==null)return null;
        // 创建新的位图
        int w = dw.getIntrinsicWidth();
        int h = dw.getIntrinsicHeight();
        if(w<=0||h<=0){
            return null;
        }
        Bitmap bg = Bitmap.createBitmap(dw.getIntrinsicWidth(),
                dw.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        // 创建位图画板
        Canvas canvas = new Canvas(bg);
        // 绘制图形
        dw.setBounds(0, 0, dw.getIntrinsicWidth(), dw.getIntrinsicHeight());
        dw.draw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        // 释放资源
        canvas.setBitmap(null);
        return bg;
    }

    public static Bitmap composite(Drawable source, Drawable mask, Drawable bg, Rect clipBounds, Rect iconPosition){
        if(source==null||mask==null||bg==null||clipBounds==null)return null;
        //a:first mask the source
        int srcWidth = clipBounds.right-clipBounds.left;
        int srcHeight = clipBounds.bottom-clipBounds.top;
        int resultWidth = mask.getIntrinsicWidth();
        int resultHeight = mask.getIntrinsicWidth();
        Paint paint = new Paint();
        Bitmap srcBmp = (source instanceof BitmapDrawable)?((BitmapDrawable) source).getBitmap():PhotoUtils.drawable2bitmap(source);
        Bitmap maskBmp = (mask instanceof BitmapDrawable)?((BitmapDrawable) mask).getBitmap():PhotoUtils.drawable2bitmap(mask);
        Bitmap bgBmp = (bg instanceof BitmapDrawable)?((BitmapDrawable) bg).getBitmap():PhotoUtils.drawable2bitmap(bg);
        if (!maskBmp.isMutable()) {
            // 设置图片为背景为透明
            maskBmp = maskBmp.copy(Bitmap.Config.ARGB_8888, true);
        }
        if (!srcBmp.isMutable()) {
            // 设置图片为背景为透明
            srcBmp = srcBmp.copy(Bitmap.Config.ARGB_8888, true);
        }

        if(srcWidth!=resultWidth||srcHeight!=resultHeight){
            float scalW= ((float)resultWidth)/srcWidth;
            float scalH= ((float)resultHeight)/srcHeight;
            float scale = (scalW<scalH?scalW:scalH);
            srcBmp = zoom(srcBmp,scale);
            if(srcBmp.getWidth()<resultWidth||srcBmp.getHeight()<resultHeight) {
                Bitmap bitmap = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.save();
                canvas.translate((bitmap.getWidth() - srcBmp.getWidth()) / 2,
                        (bitmap.getHeight() - srcBmp.getHeight()) / 2);
                canvas.drawBitmap(srcBmp, 0, 0, paint);
                canvas.restore();
                canvas.setBitmap(null);
                srcBmp = bitmap;
            }
        }
        Canvas canvas = new Canvas(maskBmp);
        paint.reset();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas.save();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.translate((maskBmp.getWidth()-srcBmp.getWidth()) / 2,
                (maskBmp.getHeight()-srcBmp.getHeight()) / 2);
        canvas.drawBitmap(srcBmp,0,0, paint);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        canvas.restore();
        paint.setXfermode(null);
        canvas.setBitmap(null);
        paint.reset();
        //b:composit a with bg
        int bx = (maskBmp.getWidth() - bgBmp.getWidth()) / 2;
        int by = (maskBmp.getHeight() - bgBmp.getHeight()) / 2;
        if(iconPosition!=null){
            bx = iconPosition.left;
            by = iconPosition.top;
        }
        if (!bgBmp.isMutable()) {
            // 设置图片为背景为透明
            bgBmp = bgBmp.copy(Bitmap.Config.ARGB_8888, true);
        }
        Canvas canvas2 = new Canvas(bgBmp);
        // 叠加新图b2 并且居中
        canvas2.save(Canvas.ALL_SAVE_FLAG);
        canvas2.drawBitmap(maskBmp, -bx, -by, paint);
        canvas2.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        canvas2.restore();
        canvas2.setBitmap(null);
        return bgBmp;
    }

    public static Bitmap compositeBitmap(Bitmap source, Bitmap mask, Bitmap bg, Rect clipBounds, Rect iconPosition){
        if(source==null||mask==null||bg==null||clipBounds==null)return null;
        //a:first mask the source
        int srcWidth = clipBounds.right-clipBounds.left;
        int srcHeight = clipBounds.bottom-clipBounds.top;
        int resultWidth = mask.getWidth();
        int resultHeight = mask.getHeight();
        Paint paint = new Paint();
        Bitmap srcBmp = source;
        Bitmap maskBmp = mask;
        Bitmap bgBmp = bg;
        if (!maskBmp.isMutable()) {
            // 设置图片为背景为透明
            maskBmp = maskBmp.copy(Bitmap.Config.ARGB_8888, true);
        }
        if (!srcBmp.isMutable()) {
            // 设置图片为背景为透明
            srcBmp = srcBmp.copy(Bitmap.Config.ARGB_8888, true);
        }

        if(srcWidth!=resultWidth||srcHeight!=resultHeight){
            float scalW= ((float)resultWidth)/srcWidth;
            float scalH= ((float)resultHeight)/srcHeight;
            float scale = (scalW<scalH?scalW:scalH);
            srcBmp = zoom(srcBmp,scale);
            if(srcBmp.getWidth()<resultWidth||srcBmp.getHeight()<resultHeight) {
                Bitmap bitmap = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.save();
                canvas.translate((bitmap.getWidth() - srcBmp.getWidth()) / 2,
                        (bitmap.getHeight() - srcBmp.getHeight()) / 2);
                canvas.drawBitmap(srcBmp, 0, 0, paint);
                canvas.restore();
                canvas.setBitmap(null);
                srcBmp = bitmap;
            }
        }
        Canvas canvas = new Canvas(maskBmp);
        paint.reset();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas.save();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.translate((maskBmp.getWidth()-srcBmp.getWidth()) / 2,
                (maskBmp.getHeight()-srcBmp.getHeight()) / 2);
        canvas.drawBitmap(srcBmp,0,0, paint);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        canvas.restore();
        paint.setXfermode(null);
        canvas.setBitmap(null);
        paint.reset();
        //b:composit a with bg
        int bx = (maskBmp.getWidth() - bgBmp.getWidth()) / 2;
        int by = (maskBmp.getHeight() - bgBmp.getHeight()) / 2;
        if(iconPosition!=null){
            bx = iconPosition.left;
            by = iconPosition.top;
        }
        if (!bgBmp.isMutable()) {
            // 设置图片为背景为透明
            bgBmp = bgBmp.copy(Bitmap.Config.ARGB_8888, true);
        }
        Canvas canvas2 = new Canvas(bgBmp);
        // 叠加新图b2 并且居中
        canvas2.save(Canvas.ALL_SAVE_FLAG);
        canvas2.drawBitmap(maskBmp, -bx, -by, paint);
        canvas2.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        canvas2.restore();
        canvas2.setBitmap(null);
        return bgBmp;
    }

    public static Bitmap zoom(Bitmap bmpBg,float scale){
        if(bmpBg == null)return null;
        Matrix matrix = new Matrix();
        matrix.postScale(scale,scale);
        Bitmap bm =Bitmap.createBitmap(bmpBg, 0, 0, bmpBg.getWidth(), bmpBg.getHeight(), matrix,
                true);
        return bm;
    }

    public static Bitmap zoom(Bitmap bmpBg,float width,float hight){
        if(bmpBg == null)return null;
        float scaleX = width/bmpBg.getWidth();
        float scaleY = hight/bmpBg.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scaleX,scaleY);
        Bitmap bm =Bitmap.createBitmap(bmpBg, 0, 0, bmpBg.getWidth(), bmpBg.getHeight(), matrix,
                true);
        return bm;
    }

    public static int calcClipBounds(Bitmap sourceBmp,Rect rect){
        float DEST_SIZE = 48f; // TODO magic number is good enough for now, should go with display info
        Bitmap scaleBmp = null;
        float sWidth = sourceBmp.getWidth();
        float sHeight = sourceBmp.getHeight();
        float scale = DEST_SIZE / Math.max(sWidth, sHeight);
        int destWidth = (int) (sWidth * scale);
        int destHeight = (int) (sHeight * scale);
        scaleBmp = Bitmap.createScaledBitmap(sourceBmp, destWidth, destHeight, false);
        float width = scaleBmp.getWidth();
        float height = scaleBmp.getHeight();
        int l = -1;
        int r = -1;
        int t = -1;
        int b = -1;
        int i, j;
        int s=0;
        for (i = 0; i < height; ++i) {
            for (j = 0; j < width; ++j) {
                if ((scaleBmp.getPixel(j, i) >>> 24) > 10) {
                    if (l < 0 || l > j)
                        l = j;
                    if (r < 0 || r < j)
                        r = j;
                    if (t < 0 || t > i)
                        t = i;
                    if (b < 0 || b < i)
                        b = i;
                    ++s;
                }
            }
        }
        if(scaleBmp!=null&&!scaleBmp.isRecycled()){
            scaleBmp.recycle();
            scaleBmp=null;
        }
        int correctedValue =5;
        rect.left =(int)(l/scale);
        rect.top =(int)(t/scale);
        rect.right =(int)(r/scale);
        rect.bottom =(int)(b/scale);
        if(rect.left>correctedValue){
            rect.left=rect.left-correctedValue;
        }else{
            rect.left=0;
        }
        if(rect.right+correctedValue<sWidth){
            rect.right=rect.right+correctedValue;
        }else{
            rect.right=(int)sWidth;
        }
        if(rect.top>correctedValue){
            rect.top=rect.top-correctedValue;
        }else{
            rect.top=0;
        }
        if(rect.bottom+correctedValue<sHeight){
            rect.bottom=rect.bottom+correctedValue;
        }else{
            rect.bottom=(int)sHeight;
        }
        return (int)(s/(scale*scale));
    }


    static String  toString(Rect r){
        if(r!=null){
            return "(l,t,r,b) = "+"( "+r.left+" ,  "+r.top+" , "+r.right+" , "+r.bottom+" )";
        }
        return null;
    }

    public static Drawable zoomDrawable(Resources res, Drawable drawable, float scale) {
        if(drawable == null)return null;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap oldbmp = drawable2bitmap(drawable);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);
        return new BitmapDrawable(res,newbmp);
    }

}
