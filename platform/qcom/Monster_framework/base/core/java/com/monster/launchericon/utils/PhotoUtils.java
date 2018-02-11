package com.monster.launchericon.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;


/**
 * Created by antino on 16-7-20.
 */
public class PhotoUtils {
    public static Bitmap drawable2bitmap(Drawable dw) {
        //TODO:???? is best?
        if(dw==null)return null;
        // 创建新的位图
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

    public Drawable clipDrawable(Drawable drawable) {
        return null;
    }

    public Rect calcBounds(Bitmap srcBitmap) {
        Rect rect = new Rect();
        return rect;
    }

    public int getResourceId(String packageName, String resourceName,
                             Context context, int defaultValue) {
        return -1;
    }

    public static int getDimen(String packageName, String resourceName,
                               Context context,int defaultValue){
        int dimen =1;
        int resId =0;
        Resources resources = context.getResources();
        if (resources != null) {
            resId = resources.getIdentifier(resourceName, "dimen",
                    packageName);
        }
        if(resId==0){
            return defaultValue;
        }
        dimen = resources.getDimensionPixelSize(resId);
        Log.i(PKGIcongetter.TAG,"getDimen="+dimen+"  resId = "+resId);
        return dimen;
    }


    public static boolean getBoolean(String packageName, String resourceName,
                                 Context context,boolean defaultValue){
        boolean  result =false;
        int resId =0;
        Resources resources = context.getResources();
        if (resources != null) {
            resId = resources.getIdentifier(resourceName, "bool",
                    packageName);

        }
        if(resId==0){
            return defaultValue;
        }
        result = resources.getBoolean(resId);
        Log.i(PKGIcongetter.TAG,"resId = "+resId+"  resourceName = "+resourceName+"  ,  context = "+context+"  , result =  "+result);
        return result;

    }

    public static Drawable getIconDrawable(String packageName, String resourceName,
                                           Context context,int iconDpi) {
        Drawable drawable = null;
        Resources resources = context.getResources();
        if (resources != null) {
            int resId = resources.getIdentifier(resourceName, "drawable",
                    packageName);
            Log.i("icons","resId = "+resId);
            if (resId == 0) {
                return null;
            }
            drawable = resources.getDrawableForDensity(resId,iconDpi);
        }
        return drawable;
    }

    public static Bitmap maskBitmap(Bitmap source, Bitmap mask,Rect bound) {
        Bitmap src = source;
        Bitmap result = mask;
        int srcWidth = bound.right-bound.left;
        int srcHeight = bound.bottom-bound.top;
        int resultWidth = mask.getWidth();
        int resultHeight = mask.getHeight();
        Paint paint = new Paint();
        if(srcWidth!=resultWidth||srcHeight!=resultHeight){
            float scalW= ((float)resultWidth)/srcWidth;
            float scalH= ((float)resultHeight)/srcHeight;
            float scale = (scalW<scalH?scalW:scalH);
            src = zoom(src,scale);
            if(src.getWidth()<resultWidth||src.getHeight()<resultHeight){
                Bitmap bitmap = Bitmap.createBitmap(resultWidth,resultHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.save();
                canvas.translate((bitmap.getWidth()-src.getWidth()) / 2,
                        (bitmap.getHeight()-src.getHeight()) / 2);
                canvas.drawBitmap(src,0,0, paint);
                canvas.restore();
                canvas.setBitmap(null);
                src = bitmap;
            }
        }
        Log.i("maskBitmap","2:src = "+src.getWidth()+"  "+src.getHeight()+"  , result = "+result.getWidth()+"  "+result.getHeight());
        // 创建画板
        Canvas canvas = new Canvas(mask);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas.save();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.translate((mask.getWidth()-src.getWidth()) / 2,
                (mask.getHeight()-src.getHeight()) / 2);
        canvas.drawBitmap(src,0,0, paint);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        canvas.restore();
        paint.setXfermode(null);
        canvas.setBitmap(null);
        return result;
    }

    public static Bitmap zoom(Bitmap bmpBg,float scale){
        Matrix matrix = new Matrix();
        matrix.postScale(scale,scale);
        Bitmap bm =Bitmap.createBitmap(bmpBg, 0, 0, bmpBg.getWidth(), bmpBg.getHeight(), matrix,
                true);
        return bm;
    }

    public static Bitmap  composite(Bitmap source, Bitmap bg,Rect bounds) {
        Bitmap b1 = bg;
        Bitmap b2 = source;
        if (!b1.isMutable()) {
            // 设置图片为背景为透明
            b1 = b1.copy(Bitmap.Config.ARGB_8888, true);
        }
        Log.i("composite","b2:("+b2.getWidth()+"  ,  "+b2.getHeight()+")"+" bg = ("+bg.getWidth()+" , "+bg.getHeight()+")"+" bounds = "+bounds);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        Canvas canvas = new Canvas(b1);
        int b1w = b1.getWidth();
        int b1h = b1.getHeight();
        int b2w = b2.getWidth();
        int b2h = b2.getHeight();
        int bx = (b1w - b2w) / 2;
        int by = (b1h - b2h) / 2;
        if(bounds!=null){
            bx = bounds.left;
            by = bounds.top;
        }
        canvas.drawBitmap(b2, bx, by, paint);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        // 叠加新图b2 并且居中
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        canvas.setBitmap(null);
        return b1;
    }

    public static  int calcClipBounds(Bitmap sourceBmp,Rect rect){
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
        /*if(l>correctedValue){
            l=l-correctedValue;
        }else{
            l=0;
        }
        if(r+correctedValue<width){
            r=r+correctedValue;
        }else{
            r=(int)width;
        }
        if(t>correctedValue){
            t=t-correctedValue;
        }else{
            t=0;
        }
        if(b+correctedValue<height){
            b=b+correctedValue;
        }else{
            b=(int)height;
        }*/
        rect.left =(int)(l/scale);
        rect.top =(int)(t/scale);
        rect.right =(int)(r/scale);
        rect.bottom =(int)(b/scale);
        Log.i("xxx","1:rect.left = "+rect.left+" ,  rect.right = "+rect.right+"  ,  rect.top = "+rect.top+"  ,  rect.bottom = "+rect.bottom
            +" sourceBmp = "+sourceBmp.getWidth()+"  "+sourceBmp.getHeight());
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
        Log.i("xxx","rect.left = "+rect.left+" ,  rect.right = "+rect.right+"  ,  rect.top = "+rect.top+"  ,  rect.bottom = "+rect.bottom
                +" sourceBmp = "+sourceBmp.getWidth()+"  "+sourceBmp.getHeight());
        Log.i("xxx","rect = "+toString(rect)+" sourceBmp = "+sourceBmp.getWidth()+"  "+sourceBmp.getHeight());
        return (int)(s/(scale*scale));
    }

    public static Drawable clipDrawable(Drawable drawable,Resources res,Rect rect){
        //TODO:Here will be make some misstakes.
        Log.i("clipDrawable","rect = "+toString(rect));
        Bitmap bg = Bitmap.createBitmap(Math.abs(rect.right - rect.left)+1,
                Math.abs(rect.top - rect.bottom)+1, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);
        canvas.save();
        Bitmap oldBitmap = drawable2bitmap(drawable);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas.drawBitmap(oldBitmap, -rect.left, -rect.top,paint);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
        canvas.restore();
        return new BitmapDrawable(res,bg);
    }

    static String  toString(Rect r){
        if(r!=null){
            return "(l,t,r,b) = "+"( "+r.left+" ,  "+r.top+" , "+r.right+" , "+r.bottom+" )";
        }
        return null;
    }

    public static int bitmapToRGB(Bitmap bm){
        int w = bm.getWidth();
        int h = bm.getHeight();
        Log.i("pppp","bm.getWidth() = "+bm.getWidth()+"  bm.getHeight() = "+bm.getHeight());
        //float factor = Math.min(160/bm.getWidth(),160/bm.getHeight());
        bm = zoom(bm,0.05f);
        w = bm.getWidth();
        h = bm.getHeight();
        int r =0;
        int g = 0;
        int b = 0;
        Integer color;
        for(int i=10;i<w;i+=1){
            for(int j=10;j<h;j+=1){
                int pixel = bm.getPixel( i , j );
                r += Color.red(pixel);
                g += Color.green(pixel);
                b +=Color.blue(pixel);
            }
        }
        int count = w*h;
        r = r/count;
        g = g/count;
        b = b/count;
        return Color.argb(0xff,r,g,b);
    }


}
