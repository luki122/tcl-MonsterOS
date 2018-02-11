package com.android.gallery3d.ui;

//SQF add this file on 2016.08.08

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextPaint;
import android.graphics.Movie;
import com.android.gallery3d.common.Utils;
import android.graphics.Bitmap.Config;
import com.android.gallery3d.glrenderer.CanvasTexture;

public class GifTexture extends CanvasTexture {
    
    private long mMovieStart = 0;
    private MovieData mMovieData;
    
    public static boolean isGif(String path){
        if(null == path) return false;
        int l = path.length();
        if(l <= 4) return false;
        return path.substring(l - 4).equalsIgnoreCase(".gif");
    }

    public boolean isValid(){
        return (null != mMovieData.mMovie);
    }
    public float getDrawLeft(){
        return mMovieData.mDrawLeft;
    }
    public float getDrawTop(){
        return mMovieData.mDrawTop;
    }

    public GifTexture(int w,int h,MovieData data) {
        super(w,h);
        mMovieData = data;
        mAlwaysUpdate = true;
    }
 
    @Override
    protected void onDraw(Canvas canvas, Bitmap backing) {
        long now = android.os.SystemClock.uptimeMillis();
        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        if (mMovieData.mMovie != null) {
            int dur = mMovieData.mMovie.duration();

            if (dur == 0) {
                dur = 1000;
            }
            
            int relTime = (int)((now - mMovieStart) % dur);
            mMovieData.mMovie.setTime(relTime);
            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            canvas.drawColor(0xff000000);
            canvas.scale(mMovieData.mScale,mMovieData.mScale);
            mMovieData.mMovie.draw(canvas, 0,0);
            canvas.restore();
        }

    }
    
}

