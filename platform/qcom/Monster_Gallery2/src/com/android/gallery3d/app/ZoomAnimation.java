package com.android.gallery3d.app;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.android.gallery3d.anim.Animation;
import com.android.gallery3d.glrenderer.BitmapTexture;
import com.android.gallery3d.glrenderer.ColorTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.ui.AnimationTime;
import com.android.gallery3d.ui.BezierInterpolator;
import com.android.gallery3d.ui.PhotoView;
import com.android.gallery3d.ui.ZoomAnimationListener;
import com.android.gallery3d.util.ColorUtil;
import com.android.gallery3d.util.LogUtil;

public abstract class ZoomAnimation extends Animation {
    
    public static final String TAG = "ZoomAnimation";
    
    protected ZoomAnimationListener mZoomAnimationListener;
    
    protected float mProgress; // progress value interpolated.
    protected BitmapTexture mBitmapTexture;
    protected int mBitmapRotation;
    protected RectF mSrcRect;
    protected RectF mDstRect;
    protected int mAnimType = ANIM_TYPE_UNKNOWN;
    
    protected int mTextureWidth;
    protected int mTextureHeight;
    
    protected int mRotatedTextureWidth;
    protected int mRotatedTextureHeight;

    protected RectF mTempBitmapRect = new RectF();
    protected RectF mTempDrawRect = new RectF();
    
    protected float mHalfTransformWidth;
    protected float mHalfTransformHeight;
    
    protected int mFromBackgroudColor;
    protected int mToBackgroundColor;
    
    protected Interpolator mInterpolator;
    protected int mViewWidth;
    protected int mViewHeight;

    public static final int ANIM_TYPE_UNKNOWN = 0;
    public static final int ANIM_TYPE_ZOOM_IN = 1;
    public static final int ANIM_TYPE_ZOOM_OUT = 2;

    protected ColorTexture mAnimatedColorTexture = new ColorTexture(Color.WHITE);

    public ZoomAnimation(Bitmap bitmap, int bitmapRotation, RectF srcRect, RectF dstRect, int animType, int viewWidth, int viewHeight) {
        mBitmapTexture = (bitmap != null && ! bitmap.isRecycled()) ? new BitmapTexture(bitmap) : null;
        mBitmapRotation = bitmapRotation;
        mSrcRect = srcRect;
        mDstRect = dstRect;
        mAnimType = animType;
        mInterpolator = BezierInterpolator.mDefaultBezierInterpolator;//new AccelerateDecelerateInterpolator();
        //modify begin by liaoanhua
        setDuration(200);
        //modify end
        mHalfTransformWidth = (mDstRect.width() - mSrcRect.width()) / 2;
        mHalfTransformHeight = (mDstRect.height() - mSrcRect.height()) / 2;
        mViewWidth = viewWidth;
        mViewHeight = viewHeight;
        
        //AccelerateDecelerateInterpolator dddd;
    }
    
    public void setAnimationListener(ZoomAnimationListener listener) {
        mZoomAnimationListener = listener;
    }

    @Override
    protected void onCalculate(float progress) {
        mProgress = mInterpolator.getInterpolation(progress);
        mZoomAnimationListener.onZoomProgress(mAnimType, progress);
    }

    public boolean applyZoom(GLCanvas canvas) {
        boolean more = calculate(AnimationTime.get());
        if( ! mBitmapTexture.isContentValid()) {
            mBitmapTexture.updateContent(canvas);
            mTextureWidth = mBitmapTexture.getWidth();
            mTextureHeight = mBitmapTexture.getHeight();
            mRotatedTextureWidth = PhotoView.getRotated(mBitmapRotation, mBitmapTexture.getWidth(), mBitmapTexture.getHeight());
            mRotatedTextureHeight = PhotoView.getRotated(mBitmapRotation, mBitmapTexture.getHeight(), mBitmapTexture.getWidth());
        }
        transformBackground(canvas);
        transformImageRect(canvas);
        if( ! more) {
            forceStop();
            mZoomAnimationListener.onZoomEnd(mAnimType);
        }
        return more;
    }
    
    protected abstract void transformBackground(GLCanvas canvas);
    protected abstract void transformImageRect(GLCanvas canvas);
    
    public int getAnimatedPhotoBackground(int fromBgColor, int toBgColor) {
        return ColorUtil.getAnimatedColor(fromBgColor, toBgColor, mProgress);
    }

    public int getAnimationType() {
        return mAnimType;
    }
    
    public float getProgress() {
        return mProgress;
    }
}
