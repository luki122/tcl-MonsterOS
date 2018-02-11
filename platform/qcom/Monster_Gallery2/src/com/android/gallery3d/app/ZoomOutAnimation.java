package com.android.gallery3d.app;

import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.PhotoView;
import com.android.gallery3d.util.LogUtil;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;

public class ZoomOutAnimation extends ZoomAnimation {
    
    public static final String TAG = "ZoomOutAnimation";
    
    private final int mToColor = Color.parseColor("#00FFFFFF");

    public ZoomOutAnimation(Bitmap bitmap, int bitmapRotation, RectF srcRect, RectF dstRect, int viewWidth, int viewHeight) {
        super(bitmap, bitmapRotation, srcRect, dstRect, ANIM_TYPE_ZOOM_OUT, viewWidth, viewHeight);
    }

    @Override
    public void start() {
        super.start();
        if(null != mZoomAnimationListener) {
            mZoomAnimationListener.onZoomStart(mAnimType);
        }
    }
    
    @Override
    public void transformBackground(GLCanvas canvas) {
        int color = getAnimatedPhotoBackground(Color.WHITE, mToColor);
        mAnimatedColorTexture.setColor(color);
        mAnimatedColorTexture.draw(canvas, 0, 0, mViewWidth, mViewHeight);
    }

    @Override
    public void transformImageRect(GLCanvas canvas) {
        
        float tempDrawRectCenterX = mSrcRect.centerX() + (mDstRect.centerX() - mSrcRect.centerX()) * mProgress;//mProgress
        float tempDrawRectCenterY = mSrcRect.centerY() + (mDstRect.centerY() - mSrcRect.centerY()) * mProgress;//mProgress
        
        float left = tempDrawRectCenterX - mSrcRect.width() / 2f - mHalfTransformWidth * mProgress; 
        float top = tempDrawRectCenterY - mSrcRect.height() / 2f - mHalfTransformHeight * mProgress;
        float right = tempDrawRectCenterX + mSrcRect.width() / 2f + mHalfTransformWidth * mProgress;
        float bottom = tempDrawRectCenterY + mSrcRect.height() / 2f + mHalfTransformHeight * mProgress;
        mTempDrawRect.set(left, top, right, bottom);
        //LogUtil.d(TAG, "mTempDrawRect ........." + mTempDrawRect + " mProgress:" + mProgress + "===============");
        
        int textureMinSide = Math.min(mTextureWidth, mTextureHeight);
        left = (mTextureWidth - textureMinSide) * mProgress / 2f;
        top = (mTextureHeight - textureMinSide) * mProgress / 2f;
        right = mTextureWidth - left;
        bottom = mTextureHeight - top;
        mTempBitmapRect.set(left, top, right, bottom);
        
        //LogUtil.d(TAG, "mTempBitmapRect  -----------: " + mTempBitmapRect + " mTextureWidth:" + mTextureWidth +  " mTextureHeight:" + mTextureHeight);

        float offsetX = mTempDrawRect.left + mTempDrawRect.width() / 2f;
        float offsetY = mTempDrawRect.top + mTempDrawRect.height() / 2f;
        mTempDrawRect.offset( - offsetX, - offsetY);
        
        if(mBitmapRotation % 180 != 0) {
            //e.g. RectF(-540.0, -720.0, 540.0, 720.0) change to (-720.0, -540.0, 720.0, 540.0)
            float tmpLeft = mTempDrawRect.top;
            float tmpTop = mTempDrawRect.left;
            float tmpRight = mTempDrawRect.bottom;
            float tmpBottom = mTempDrawRect.right;
            mTempDrawRect.set(tmpLeft, tmpTop, tmpRight, tmpBottom);
        }
        
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.rotate(mBitmapRotation, 0, 0, 1);
        canvas.drawTexture(mBitmapTexture, mTempBitmapRect, mTempDrawRect);
        canvas.restore();
        
        //LogUtil.d(TAG, " mTempBitmapRect:" + mTempBitmapRect + " mTempDrawRect:" + mTempDrawRect);
    }
    
}
