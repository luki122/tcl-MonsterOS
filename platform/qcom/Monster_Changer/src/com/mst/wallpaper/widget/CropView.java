package com.mst.wallpaper.widget;


import com.mst.wallpaper.widget.TiledImageRenderer.TileSource;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;


public class CropView extends TiledImageView implements OnScaleGestureListener{

    private static final float MAX_SCALE_VALUE_CAN = 28.0f;
    private static final float MAX_SCALE_VALUE = 20.0f;
    private ScaleGestureDetector mScaleGestureDetector;
    private long mTouchDownTime;
    private float mFirstX, mFirstY;
    private float mLastX, mLastY;
    private float mCenterX, mCenterY;
    private float mMinScale;
    private boolean mTouchEnabled = true;
    private RectF mTempEdges = new RectF();
    private float[] mTempPoint = new float[] { 0, 0 };
    private float[] mTempCoef = new float[] { 0, 0 };
    private float[] mTempAdjustment = new float[] { 0, 0 };
    private float[] mTempImageDims = new float[] { 0, 0 };
    private float[] mTempRendererCenter = new float[] { 0, 0 };
    TouchCallback mTouchCallback;
    Matrix mRotateMatrix;
    Matrix mInverseRotateMatrix;
    private boolean mCanCrop = true;

    public interface TouchCallback {
        void onTouchDown();
        void onTap();
        void onTouchUp();
    }

    public CropView(Context context) {
        this(context, null);
    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        mRotateMatrix = new Matrix();
        mInverseRotateMatrix = new Matrix();
    }

    private float[] getImageDims() {
        final float imageWidth = mRenderer.source.getImageWidth();
        final float imageHeight = mRenderer.source.getImageHeight();
        float[] imageDims = mTempImageDims;
        imageDims[0] = imageWidth;
        imageDims[1] = imageHeight;
        mRotateMatrix.mapPoints(imageDims);
        imageDims[0] = Math.abs(imageDims[0]);
        imageDims[1] = Math.abs(imageDims[1]);
        return imageDims;
    }

    public int getImageWidth() {
    	if (mRenderer.source == null) {
			return 0;
		}
		return mRenderer.source.getImageWidth();
	}
    
    public int getImageHeight() {
    	if (mRenderer.source == null) {
			return 0;
		}
    	return mRenderer.source.getImageHeight();
	}
    
    public boolean canCrop() {
		return mCanCrop;
	}
    
    private void getEdgesHelper(RectF edgesOut) {
        final float width = getWidth();
        final float height = getHeight();
        final float[] imageDims = getImageDims();
        final float imageWidth = imageDims[0];
        final float imageHeight = imageDims[1];

        float initialCenterX = mRenderer.source.getImageWidth() / 2f;
        float initialCenterY = mRenderer.source.getImageHeight() / 2f;

        float[] rendererCenter = mTempRendererCenter;
        rendererCenter[0] = mCenterX - initialCenterX;
        rendererCenter[1] = mCenterY - initialCenterY;
        mRotateMatrix.mapPoints(rendererCenter);
        rendererCenter[0] += imageWidth / 2;
        rendererCenter[1] += imageHeight / 2;

        final float scale = mRenderer.scale;
        float centerX = (width / 2f - rendererCenter[0] + (imageWidth - width) / 2f)
                * scale + width / 2f;
        float centerY = (height / 2f - rendererCenter[1] + (imageHeight - height) / 2f)
                * scale + height / 2f;
        float leftEdge = centerX - imageWidth / 2f * scale;
        float rightEdge = centerX + imageWidth / 2f * scale;
        float topEdge = centerY - imageHeight / 2f * scale;
        float bottomEdge = centerY + imageHeight / 2f * scale;

        edgesOut.left = leftEdge;
        edgesOut.right = rightEdge;
        edgesOut.top = topEdge;
        edgesOut.bottom = bottomEdge;
    }

    public int getImageRotation() {
        return mRenderer.rotation;
    }

    public RectF getCrop() {
        final RectF edges = mTempEdges;
        getEdgesHelper(edges);
        final float scale = mRenderer.scale;

        float cropLeft = -edges.left / scale;
        float cropTop;
        float cropRight = cropLeft + getWidth() / scale;
        float cropBottom;
        if (mWallpaperType == WALLPAPER_TYPE_PORT) {
        	cropTop = (-edges.top) / scale < 1 ? 1.0f : (-edges.top) / scale;
        	cropBottom = cropTop + getHeight() / scale;
		}else {
			cropTop = (-edges.top + getHeight() / 4) / scale < 1 ? 1.0f : (-edges.top + getHeight() / 4) / scale;
			cropBottom = cropTop + getHeight() / 2 / scale;
		}
        
        return new RectF(cropLeft, cropTop, cropRight, cropBottom);
    }

    public Point getSourceDimensions() {
        return new Point(mRenderer.source.getImageWidth(), mRenderer.source.getImageHeight());
    }

    public void setTileSource(TileSource source, Runnable isReadyCallback, int wallpaperType) {
    	mWallpaperType = wallpaperType;
        super.setTileSource(source, isReadyCallback);
        mCenterX = mRenderer.centerX;
        mCenterY = mRenderer.centerY;
        mRotateMatrix.reset();
        mRotateMatrix.setRotate(mRenderer.rotation);
        mInverseRotateMatrix.reset();
        mInverseRotateMatrix.setRotate(-mRenderer.rotation);
        updateMinScale(getWidth(), getHeight(), source/*, true*/);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateMinScale(w, h, mRenderer.source/*, false*/);
    }

    public void setScale(float scale) {
        synchronized (mLock) {
            mRenderer.scale = scale;
        }
    }

    private void updateMinScale(int w, int h, TileSource source/*, boolean resetScale*/) {
        synchronized (mLock) {
            /*if (resetScale) {
                mRenderer.scale = 1;
            }*/
            if (source != null) {
                final float[] imageDims = getImageDims();
                final float imageWidth = imageDims[0];
                final float imageHeight = imageDims[1];
                if (mWallpaperType == WALLPAPER_TYPE_PORT) {
                	mMinScale = Math.max(w / imageWidth, h / imageHeight);
				}else {
					if (2 * w / imageWidth < h / imageHeight) {
						mMinScale = h / (2 * imageHeight);
					}else if (imageWidth <= w && w / imageWidth >= h / imageHeight && (imageWidth < imageHeight)) {
						mMinScale = Math.max(w / imageWidth, h / imageHeight);
					}else {
						mMinScale = Math.min(w / imageWidth, h / imageHeight);
					}
				}
                mRenderer.scale = mMinScale;
                
            }
        }
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        // Don't need the lock because this will only fire inside of
        // onTouchEvent
        mRenderer.scale *= detector.getScaleFactor();
        if (mRenderer.scale < mMinScale) {
        	float tempScale;
        	if (getImageWidth() < getImageHeight()) {
				tempScale = mMinScale - getWidth() * 0.08f / getImageWidth();
			}else {
				tempScale = mMinScale - getHeight() * 0.08f / getImageHeight();
			}
        	mRenderer.scale = ( mRenderer.scale < tempScale ) ? tempScale : mRenderer.scale;
		}else {
			mRenderer.scale = ( mRenderer.scale > MAX_SCALE_VALUE_CAN ) ? MAX_SCALE_VALUE_CAN : Math.max(mMinScale, mRenderer.scale);
		}
        invalidate();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    	if (mRenderer.scale < mMinScale) {
			mRenderer.scale = mMinScale;
		}else {
			mRenderer.scale = ( mRenderer.scale > MAX_SCALE_VALUE ) ? MAX_SCALE_VALUE : Math.max(mMinScale, mRenderer.scale);
		}
    	invalidate();
    }

    @SuppressLint("NewApi")
	public void moveToLeft() {
        if (getWidth() == 0 || getHeight() == 0) {
            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        moveToLeft();
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
        }
        final RectF edges = mTempEdges;
        getEdgesHelper(edges);
        final float scale = mRenderer.scale;
        mCenterX += Math.ceil(edges.left / scale);
        updateCenter();
    }

    private void updateCenter() {
        mRenderer.centerX = Math.round(mCenterX);
        mRenderer.centerY = Math.round(mCenterY);
    }

    public void setTouchEnabled(boolean enabled) {
        mTouchEnabled = enabled;
    }

    public void setTouchCallback(TouchCallback cb) {
        mTouchCallback = cb;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        final boolean pointerUp = action == MotionEvent.ACTION_POINTER_UP;
        final int skipIndex = pointerUp ? event.getActionIndex() : -1;

        // Determine focal point
        float sumX = 0, sumY = 0;
        final int count = event.getPointerCount();
        for (int i = 0; i < count; i++) {
            if (skipIndex == i)
                continue;
            sumX += event.getX(i);
            sumY += event.getY(i);
        }
        final int div = pointerUp ? count - 1 : count;
        float x = sumX / div;
        float y = sumY / div;

        if (action == MotionEvent.ACTION_DOWN) {
            mFirstX = x;
            mFirstY = y;
            mTouchDownTime = System.currentTimeMillis();
            if (mTouchCallback != null) {
                mTouchCallback.onTouchDown();
            }
        } else if (action == MotionEvent.ACTION_UP) {
            ViewConfiguration config = ViewConfiguration.get(getContext());

            float squaredDist = (mFirstX - x) * (mFirstX - x) + (mFirstY - y) * (mFirstY - y);
            float slop = config.getScaledTouchSlop() * config.getScaledTouchSlop();
            long now = System.currentTimeMillis();
            if (mTouchCallback != null) {
                // only do this if it's a small movement
                if (squaredDist < slop &&
                        now < mTouchDownTime + ViewConfiguration.getTapTimeout()) {
                    mTouchCallback.onTap();
                }
                mTouchCallback.onTouchUp();
            }
        }

        if (!mTouchEnabled) {
            return true;
        }

        synchronized (mLock) {
            mScaleGestureDetector.onTouchEvent(event);
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                	mCanCrop = false;
                    float[] point = mTempPoint;
                    point[0] = (mLastX - x) / mRenderer.scale;
                    point[1] = (mLastY - y) / mRenderer.scale;
                    mInverseRotateMatrix.mapPoints(point);
                    
                    if (mRenderer.source != null) {
                    	float imageWidth = mRenderer.source.getImageWidth();
                        float imageHeight = mRenderer.source.getImageHeight();
                        if (mCenterX <= 0 || mCenterX >= imageWidth || mCenterY <= 0 || mCenterY >= imageHeight) {
                        	
    					}else {
    						mCenterX += point[0];
    	                    mCenterY += point[1];
    						updateCenter();
    						invalidate();
    						if (mCenterX < 0) {
								mCenterX = 0;
							}else if (mCenterX > imageWidth) {
								mCenterX = imageWidth;
							}
    						if (mCenterY < 0) {
								mCenterY = 0;
							}else if (mCenterY > imageHeight) {
								mCenterY = imageHeight;
							}
    					}
					}else {
						mCenterX += point[0];
	                    mCenterY += point[1];
						updateCenter();
						invalidate();
					}
                    break;
                case MotionEvent.ACTION_UP:
                		upEvent();
                	break;
            }
        }

        mLastX = x;
        mLastY = y;
        return true;
    }
    
	private void upEvent() {
		if (mRenderer.source != null) {
			// Adjust position so that the wallpaper covers the entire area
			// of the screen
			final RectF edges = mTempEdges;
			getEdgesHelper(edges);
			final float scale = mRenderer.scale;

			float[] coef = mTempCoef;
			coef[0] = 1;
			coef[1] = 1;
			mRotateMatrix.mapPoints(coef);
			float[] adjustment = mTempAdjustment;
			mTempAdjustment[0] = 0;
			mTempAdjustment[1] = 0;
			if (edges.left > 0) {
				adjustment[0] = edges.left / scale;
			}
			if (edges.right < getWidth()) {
				adjustment[0] = (edges.right - getWidth()) / scale;
			}
			if (edges.top > 0) {
				if (mWallpaperType == WALLPAPER_TYPE_PORT) {
					adjustment[1] = FloatMath.ceil(edges.top / scale);
				} else {
					if (edges.top > getHeight() / 4) {
						adjustment[1] = FloatMath
								.ceil((edges.top - getHeight() / 4) / scale);
					}
				}
			}
			if (edges.bottom < getHeight()) {
				if (mWallpaperType == WALLPAPER_TYPE_PORT) {
					adjustment[1] = (edges.bottom - getHeight()) / scale;
				} else {
					if (edges.bottom < 3 * getHeight() / 4) {
						adjustment[1] = (edges.bottom - 3 * getHeight() / 4)
								/ scale;
					}
				}
			}
			for (int dim = 0; dim <= 1; dim++) {
				if (coef[dim] > 0)
					adjustment[dim] = FloatMath.ceil(adjustment[dim]);
			}

			mInverseRotateMatrix.mapPoints(adjustment);
			mCenterX += adjustment[0];
			mCenterY += adjustment[1];
			updateCenter();
			invalidate();
		}
		mCanCrop = true;
	}
    
	public void resume() {
		if (!mCanCrop) {
			upEvent();
		}
	}

}
