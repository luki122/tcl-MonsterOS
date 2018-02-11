package cn.tcl.meetingassistant.view;
/* Copyright (C) 2016 Tcl Corporation Limited */

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.log.MeetingLog;
import cn.tcl.meetingassistant.utils.DensityUtil;

public class MatrixImageView extends ImageView {
    private final static String TAG = MatrixImageView.class.getSimpleName();

    private GestureDetector mGestureDetector;
    private final Matrix mMatrix = new Matrix();
    private MatrixTouchListener mListener;
    private Bitmap mBitmap;
    private float DEFAULT_PADDING_TOP = DensityUtil.dip2px(this.getContext(),21);//dp

    float mViewHeight;
    float mViewWidth;
    float mImageHeight;
    float mImageWidth;
    float mTransX;
    float mTransY;
    float mScale;
    float mPaddingTop;
    float mRealHeight;
    float mRealWidth;
    boolean mImageVertical;

    public MatrixImageView(Context context) {
        this(context, null);
    }

    public MatrixImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MatrixImageView);
        mPaddingTop = a.getDimension(R.styleable.MatrixImageView_matrix_paddingTop, DEFAULT_PADDING_TOP);
        a.recycle();
        mListener = new MatrixTouchListener();
        setOnTouchListener(mListener);
        mGestureDetector = new GestureDetector(getContext(), new GestureListener(mListener));
        setBackgroundColor(Color.TRANSPARENT);
        setScaleType(ScaleType.FIT_CENTER);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        setScaleType(ScaleType.MATRIX);
        mMatrix.set(getInitMatrix());
        mListener.initCurrentMatrix();
        float[] values = new float[9];
        mMatrix.getValues(values);
        setImageMatrix(mMatrix);
        MeetingLog.d(TAG, "bitmap width=" + mMatrix);
    }

    private Matrix getInitMatrix(){
         mViewHeight = getMeasuredHeight();
         mViewWidth = getMeasuredWidth();
         mImageHeight = mBitmap.getHeight();
         mImageWidth = mBitmap.getWidth();

        if((mViewHeight - mPaddingTop)/mViewWidth < mImageHeight/mImageWidth){
            mRealHeight = mViewHeight - mPaddingTop;
            mRealWidth = mImageWidth/mImageHeight * mRealHeight;
            mTransX = (mViewWidth - mRealWidth) /2;
            mTransY = mPaddingTop;
            mImageVertical = true;
        }else {
            mRealWidth = mViewWidth;
            mRealHeight = mImageHeight/mImageWidth * mRealWidth;
            mTransX = 0;
            mTransY = (mViewHeight - mRealHeight) /2;
            mImageVertical = false;
        }
        mScale = mRealWidth / mImageWidth;

        Matrix matrix = new Matrix();
        matrix.setScale(mScale,mScale);
        matrix.postTranslate(mTransX,mTransY);
        MeetingLog.i(TAG,"getInitMatrix matrix = " + matrix);
        return matrix;
    }

    public class MatrixTouchListener implements OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {
        private static final int MODE_DRAG = 1;
        private static final int MODE_ZOOM = 2;
        private static final int MODE_UNABLE = 3;

        float mMaxScale = 6;
        float mDoubleClickScale = 2;

        private int mMode = 0;
        private float mStartDis;
        private Matrix mCurrentMatrix = new Matrix();
        private ScaleGestureDetector mScaleGestureDetector = null;
        private PointF startPoint = new PointF();
        private boolean isCheckLeftAndRight;
        private boolean isCheckTopAndBottom;

        public MatrixTouchListener() {
            mScaleGestureDetector = new ScaleGestureDetector(getContext(), this);
        }

        public void initCurrentMatrix() {
            mCurrentMatrix.set(getImageMatrix());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            MeetingLog.d(TAG, "touch=" + event);

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (isZoomChanged()) {
                        requestParentIntercept(true);
                    }
                    mMode = MODE_DRAG;
                    startPoint.set(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    reSetMatrix();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mMode == MODE_ZOOM) {
                        mScaleGestureDetector.onTouchEvent(event);
                        setZoomMatrix(event);
                    } else if (mMode == MODE_DRAG) {
                        setDragMatrix(event);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (mMode == MODE_UNABLE) return true;
                    mMode = MODE_ZOOM;
                    mStartDis = distance(event);
                    break;
                default:
                    break;
            }
            mScaleGestureDetector.onTouchEvent(event);
            return mGestureDetector.onTouchEvent(event);
        }

        public void setDragMatrix(MotionEvent event) {
            if (isZoomChanged()) {
                requestParentIntercept(true);
                float dx = event.getX() - startPoint.x;
                float dy = event.getY() - startPoint.y;
                MeetingLog.d(TAG, "drag x=" + dx + "  dy=" + dy);
                if (Math.sqrt(dx * dx + dy * dy) > 10f) {
                    MeetingLog.d(TAG, "drag img");
                    startPoint.set(event.getX(), event.getY());
                    RectF rectF = getMatrixRectF();
                    isCheckLeftAndRight = isCheckTopAndBottom = true;
                    if (rectF.width() < getWidth()) {
                        dx = 0;
                        isCheckLeftAndRight = false;
                    }
                    if (rectF.height() < getHeight()) {
                        dy = 0;
                        isCheckTopAndBottom = false;
                    }
                    mCurrentMatrix.postTranslate(dx, dy);
                    checkMatrixBounds();
                    setImageMatrix(mCurrentMatrix);
                }
            } else {
                requestParentIntercept(false);
            }
        }

        private void checkMatrixBounds() {
            RectF rect = getMatrixRectF();

            float deltaX = 0, deltaY = 0;
            final float viewWidth = getWidth();
            final float viewHeight = getHeight();
            if (rect.top > 0 && isCheckTopAndBottom) {
                deltaY = -rect.top;
            }
            if (rect.bottom < viewHeight && isCheckTopAndBottom) {
                deltaY = viewHeight - rect.bottom;
            }
            if (rect.left > 0 && isCheckLeftAndRight) {
                deltaX = -rect.left;
            }
            if (rect.right < viewWidth && isCheckLeftAndRight) {
                deltaX = viewWidth - rect.right;
            }
            mCurrentMatrix.postTranslate(deltaX, deltaY);
        }

        private void checkBorderAndCenterWhenScale() {

            RectF rect = getMatrixRectF();
            float deltaX = 0;
            float deltaY = 0;

            int width = getWidth();
            int height = getHeight();

            if (rect.width() >= width) {
                if (rect.left > 0) {
                    deltaX = -rect.left;
                }
                if (rect.right < width) {
                    deltaX = width - rect.right;
                }
            }
            if (rect.height() >= height) {
                if (rect.top > 0) {
                    deltaY = -rect.top;
                }
                if (rect.bottom < height) {
                    deltaY = height - rect.bottom;
                }
            }
            if (rect.width() < width) {
                deltaX = width * 0.5f - rect.right + 0.5f * rect.width();
            }
            if (rect.height() < height) {
                if(mImageVertical){
                    //float factor = (rect.height() - mRealHeight)/(height - mRealHeight) * mPaddingTop;
                    deltaY = 0.5f * (height + mPaddingTop - mPaddingBottom + rect.height()) - rect.bottom;
                }else {
                    deltaY = height * 0.5f - rect.bottom + 0.5f * rect.height();
                }
            }
            Log.e(TAG, "deltaX = " + deltaX + " , deltaY = " + deltaY);

            mCurrentMatrix.postTranslate(deltaX, deltaY);

        }

        private RectF getMatrixRectF() {
            Matrix matrix = mCurrentMatrix;
            RectF rect = new RectF();
            Drawable d = getDrawable();
            if (null != d) {
                rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                matrix.mapRect(rect);
            }
            return rect;
        }

        private boolean isZoomChanged() {
            float[] values = new float[9];
            getImageMatrix().getValues(values);
            float scale = values[Matrix.MSCALE_X];
            mMatrix.getValues(values);
            return Math.abs(1 - scale / values[Matrix.MSCALE_X]) > 0.01;
        }

        private void setZoomMatrix(MotionEvent event) {
            if (event.getPointerCount() < 2) return;
            float endDis = distance(event);
            if (endDis > 10f) {
                float scale = endDis / mStartDis;
                mStartDis = endDis;
                mCurrentMatrix.set(getImageMatrix());
                float[] values = new float[9];
                mCurrentMatrix.getValues(values);

                scale = checkMaxScale(scale, values);
                scaleImg(scale, mScaleGestureDetector.getFocusX(), mScaleGestureDetector.getFocusY());
            }
        }

        private void scaleImg(float scale, float px, float py) {
            MeetingLog.d(TAG, "scale");
            mCurrentMatrix.postScale(scale, scale, px, py);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mCurrentMatrix);
        }

        private void scaleImgWithAnima(final float startScale, final float endScale) {
            MeetingLog.d(TAG, "startScale=" + startScale + "  endScale=" + endScale + " current Matri=" + mCurrentMatrix);
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(startScale, endScale);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float scale = (float) valueAnimator.getAnimatedValue();
                    MeetingLog.d(TAG, "update scale=" + scale);
                    float[] oldValue = new float[9];
                    mCurrentMatrix.getValues(oldValue);
                    float scaleFactor = scale / oldValue[Matrix.MSCALE_X];
                    MeetingLog.d(TAG, "scaleFactor=" + scaleFactor);
                    mCurrentMatrix.postScale(scaleFactor, scaleFactor, mScaleGestureDetector.getFocusX(), mScaleGestureDetector.getFocusY());
                    MeetingLog.i(TAG,"onAnimationUpdate scaled mCurrentMatrix = " + mCurrentMatrix);
                    checkBorderAndCenterWhenScale();
                    setImageMatrix(mCurrentMatrix);
                }
            });
            valueAnimator.setDuration(500);
            valueAnimator.start();
        }

        private float checkMaxScale(float scale, float[] values) {
            if (scale * values[Matrix.MSCALE_X] > mMaxScale)
                scale = mMaxScale / values[Matrix.MSCALE_X];
            return scale;
        }


        private void reSetMatrix() {
            if (checkRest()) {
                float[] startValue = new float[9];
                mCurrentMatrix.getValues(startValue);
                float[] endValue = new float[9];
                mMatrix.getValues(endValue);
                scaleImgWithAnima(startValue[Matrix.MSCALE_X], endValue[Matrix.MSCALE_X]);
            }
        }


        private boolean checkRest() {
            float[] values = new float[9];
            getImageMatrix().getValues(values);
            float scale = values[Matrix.MSCALE_X];
            mMatrix.getValues(values);
            return scale < values[Matrix.MSCALE_X];
        }

        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        public void onDoubleClick(boolean hasAnima) {
            if (hasAnima) {
                float scale = isZoomChanged() ? 1 : mDoubleClickScale;
                float[] oldScale = new float[9];
                mCurrentMatrix.getValues(oldScale);
                float[] newScale = new float[9];
                mMatrix.getValues(newScale);
                scaleImgWithAnima(oldScale[Matrix.MSCALE_X], scale * newScale[Matrix.MSCALE_X]);
            } else {
                float scale = isZoomChanged() ? 1 : mDoubleClickScale;
                mCurrentMatrix.set(mMatrix);
                mCurrentMatrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mCurrentMatrix);
            }
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }

    private void requestParentIntercept(boolean result) {
        MeetingLog.d(TAG, "requestParentIntercept result=" + result);
        getParent().requestDisallowInterceptTouchEvent(result);
    }


    private class GestureListener extends SimpleOnGestureListener {
        private final MatrixTouchListener listener;

        public GestureListener(MatrixTouchListener listener) {
            this.listener = listener;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            listener.onDoubleClick(true);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

    }

}
