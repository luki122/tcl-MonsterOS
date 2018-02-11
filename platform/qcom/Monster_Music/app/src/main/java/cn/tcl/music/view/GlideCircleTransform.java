package cn.tcl.music.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/12/29 13:21
 * @copyright TCL-MIE
 */
public class GlideCircleTransform  extends BitmapTransformation {

    public GlideCircleTransform(Context context) {
        super(context);
    }
    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return roundCrop(pool, toTransform);
    }
    private  Bitmap roundCrop(BitmapPool pool, Bitmap source) {
        if (source == null){
            return null;
        }
        Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        canvas.drawCircle(source.getWidth()/2, source.getHeight()/2, source.getWidth()/2, paint);
        return result;
    }
    @Override
    public String getId() {
        return getClass().getName();
    }
}
