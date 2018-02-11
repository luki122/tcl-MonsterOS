package com.android.gallery3d.ui;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.media.MediaDataSource;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import com.android.gallery3d.R;
import com.android.gallery3d.glrenderer.BitmapTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.GLPaint;

public class StoryView extends GLView {
    
    private final Layout mLayout = new Layout();
    private Context mContext;
    private int WINDOW_WIDTH = 0;
    private int WINDOW_HEIGHT = 0;
    private Bitmap bitmap;
    private BitmapTexture mBitmapTexture;
    private ArrayList<MediaDataSource> list;
    private GLPaint mBitmapPaint;
    private GLPaint mTextPaint;
    private ScrollerHelper mScroller;
    public StoryView(Context context){
        mContext = context;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        WINDOW_WIDTH = metrics.widthPixels;
        WINDOW_HEIGHT = metrics.heightPixels;
        bitmap = BitmapFactory.decodeResource(mContext.getApplicationContext().getResources(), R.drawable.mst_seekbar_thumb, null);
        mBitmapTexture = new BitmapTexture(bitmap);
        mBitmapPaint = new GLPaint();
        mTextPaint = new GLPaint();
        mTextPaint.setColor(Color.BLACK);
        mScroller = new ScrollerHelper(mContext);
    }
    
    @Override
    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        super.onLayout(changeSize, left, top, right, bottom);
    }
    
    @Override
    protected boolean onTouch(MotionEvent event) {
        return super.onTouch(event);
    }
    
    @Override
    protected void render(GLCanvas canvas) {
        super.render(canvas);
        for (int i = 0; i < list.size(); i++) {
            renderItem(i, canvas);
        }
        
    }
    
    private void renderItem(int position, GLCanvas canvas){
        
    }
    
    private class Layout {
        private int mVisibleStart;
        private int mVisibleEnd;
        private int mSlotCount;
        private int mSlotWidth;
        private int mSlotHeight;
        private int mWidth;
        private int mHeight;
        private int mContentLength;
        private int mScrollPosition;
    }
}
