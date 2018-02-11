/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-3.
 * the view to contain the image which need to be trimmed.
 */
public class CropView extends ImageView {

    private static final String TAG = CropView.class.getSimpleName();

    private BitmapDrawable mDrawable = null;

    private FloatRectDrawable mFloatRectDrawable = null;

    private Rect mCropRect = null;

    private Rect mCropBound = null;

    private FloatQuadDrawable mFloatQuadDrawable = null;

    private Quad mCropQuad = null;

    private int currentEdge = 0;

    private boolean isRegular = true;

    private int cropTouchOffsetX = 0, cropTouchOffsetY = 0;

    private int cropOffsetX = 0, cropOffsetY = 0;

    private boolean isTouchInSquare = false;

    private static final int minWidth = 280, minHeight = 210;

    private float mScale = 1.0f;

    private int cropWidth = 0, cropHeight = 0;

    private int mCornerDiameter = getResources().getDimensionPixelSize(R.dimen.radius_cut_pic_circle);

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFloatRectDrawable = new FloatRectDrawable(context);
        this.mFloatQuadDrawable = new FloatQuadDrawable(context);
    }

    /**
     * set image to display
     *
     * @param background the image
     * @return the borders of image
     */
    private Rect setBackground(Bitmap background) {

        int width = getWidth();
        int height = getHeight();

        MeetingLog.i(TAG,"setBackground->viewWidth:" +width +" height:"+height);


        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();

        MeetingLog.i(TAG,"zoomBitmap:width "+ bgWidth + " height "+bgHeight);

        // get the scale
        mScale = getScale(background, width, height);

        Bitmap mBitmap = zoomBitmap(background, mScale);

        int mapWidth = mBitmap.getWidth();
        int mapHeight = mBitmap.getHeight();

        cropOffsetX = (getWidth() - mapWidth) / 2;
        cropOffsetY = (getHeight() - mapHeight) / 2;

        Rect cropBound = new Rect(cropOffsetX, cropOffsetY, cropOffsetX + mapWidth, cropOffsetY + mapHeight);

        mDrawable = new BitmapDrawable(getResources(), mBitmap);
        mDrawable.setBounds(cropBound);

        return cropBound;
    }

    public float getScale() {
        return mScale;
    }

    /**
     * set the bitmap which need to trim
     *
     * @param bitmap the bitmap before
     */
    public void setBitmap(Bitmap bitmap) {
        mCropBound = setBackground(bitmap);
    }

    /**
     * set default trim rule -- full screen
     */
    public void setCrop() {
        isRegular = true;
        mCropRect = new Rect(mCropBound);
        invalidate();
    }

    /**
     * set the crop bound to full screen
     */
    public void setFull(boolean isRegular) {
        this.isRegular = isRegular;
        if (isRegular) {
            mCropRect.set(mCropBound);
        } else {
            if (mCropQuad == null) {
                mCropQuad = new Quad(mCropBound);
            } else {
                mCropQuad.set(mCropBound);
            }
        }
        invalidate();
    }

    public void setCrop(float[] bound) {
        int[] intBound = new int[8];
        for (int i = 0;i<bound.length;i++) {
            intBound[i] = (int)bound[i];
        }
        isRegular = false;
        mCropQuad = new Quad(intBound);
        mCropQuad.scaleIn(mScale);
        mCropQuad.offset(cropOffsetX, cropOffsetY);
        invalidate();
    }

    public int[] getCropBound() {
        if (isRegular) {
            int[] bound = new int[8];
            Rect boundRect;

            if (mCropRect == null) return bound;

            int left = mCropRect.left - cropOffsetX;
            int right = mCropRect.right - cropOffsetX;
            int top = mCropRect.top - cropOffsetY;
            int bottom = mCropRect.bottom - cropOffsetY;

            boundRect = new Rect((int) (left / mScale), (int) (top / mScale), (int) (right / mScale), (int) (bottom / mScale));

            bound[0] = bound[6] = boundRect.left;
            bound[2] = bound[4] = boundRect.right;
            bound[1] = bound[3] = boundRect.top;
            bound[5] = bound[7] = boundRect.bottom;

            return bound;
        } else {
            Quad quad = new Quad(mCropQuad);
            quad.offset(-cropOffsetX, -cropOffsetY);
            quad.scaleIn(1 / mScale);
            return quad.getBound();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawable == null) return;
        canvas.drawColor(Color.BLACK);
        mDrawable.draw(canvas);

        if (isRegular) {
            if (mCropRect == null) return;
            canvas.save();
            canvas.clipRect(mCropRect, Region.Op.DIFFERENCE);

            canvas.drawColor(Color.parseColor("#a0000000"));
            canvas.restore();

            mFloatRectDrawable.setBounds(mCropRect);
            mFloatRectDrawable.draw(canvas);
        } else {
            if (mCropQuad == null) return;
            canvas.save();

            canvas.clipPath(mCropQuad.getPath(), Region.Op.DIFFERENCE);
            canvas.drawColor(Color.parseColor("#a0000000"));
            canvas.restore();

            mFloatQuadDrawable.setBounds(mCropQuad);
            mFloatQuadDrawable.draw(canvas);
        }
    }

    /**
     * get the bitmap after scaled
     *
     * @param bitmap the original pic
     * @param scale  the scale
     * @return the bitmap after scaled
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * get the scale
     *
     * @param bitmap the original pic
     * @param width  the target width
     * @param height the target height
     * @return scale
     */
    private float getScale(Bitmap bitmap, int width, int height) {
        float scaleWidth = (float) (width - 2 * mCornerDiameter - 2 * 2f) / bitmap.getWidth();
        float scaleHeight = (float) (height - 2 * mCornerDiameter - 2 * 2f )/bitmap.getHeight();
        return scaleHeight > scaleWidth ? scaleWidth : scaleHeight;
    }

    private static float setInBoundX(float value, Rect bound) {
        float ret = value;
        if (value < bound.left) ret = bound.left;
        else if (value > bound.right) ret = bound.right;
        return ret;
    }

    private static float setInBoundY(float value, Rect bound) {
        float ret = value;
        if (value < bound.top) ret = bound.top;
        else if (value > bound.bottom) ret = bound.bottom;
        return ret;
    }

    private float pointX = 0, pointY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mDrawable == null) return false;

        if (isRegular) {

            if (mCropRect == null) return false;
            Rect cropMoveBound = new Rect(mCropBound.left, mCropBound.top, mCropBound.right - mCropRect.width(), mCropBound.bottom - mCropRect.height());
            int left = mCropRect.left;
            int top = mCropRect.top;
            int right = mCropRect.right;
            int bottom = mCropRect.bottom;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pointX = event.getX();
                    pointY = event.getY();
                    currentEdge = mFloatRectDrawable.getTouchEdge((int) pointX, (int) pointY);
                    isTouchInSquare = mCropRect.contains((int) pointX, (int) pointY);
                    cropTouchOffsetX = (int) (pointX - mCropRect.left);
                    cropTouchOffsetY = (int) (pointY - mCropRect.top);

                    cropWidth = mCropRect.width();
                    cropHeight = mCropRect.height();
                    break;
                case MotionEvent.ACTION_MOVE:
                    pointX = event.getX() - cropTouchOffsetX;
                    pointY = event.getY() - cropTouchOffsetY;

                    if (currentEdge == FloatRectDrawable.EDGE_MOVE_IN) {
                        if (isTouchInSquare)
                            mCropRect.offsetTo((int) setInBoundX(pointX, cropMoveBound), (int) setInBoundY(pointY, cropMoveBound));
                    } else {
                        switch (currentEdge) {
                            case FloatRectDrawable.EDGE_LT:
                                left = (int) pointX;
                                top = (int) pointY;
                                break;

                            case FloatRectDrawable.EDGE_RT:
                                top = (int) pointY;
                                right = (int) pointX + cropWidth;
                                break;

                            case FloatRectDrawable.EDGE_LB:
                                left = (int) pointX;
                                bottom = (int) pointY + cropHeight;
                                break;

                            case FloatRectDrawable.EDGE_RB:
                                right = (int) pointX + cropWidth;
                                bottom = (int) pointY + cropHeight;
                                break;

                            case FloatRectDrawable.EDGE_LEFT:
                                left = (int) pointX;
                                break;

                            case FloatRectDrawable.EDGE_RIGHT:
                                right = (int) pointX + cropWidth;
                                break;

                            case FloatRectDrawable.EDGE_TOP:
                                top = (int) pointY;
                                break;

                            case FloatRectDrawable.EDGE_BOTTOM:
                                bottom = (int) pointY + cropHeight;
                                break;

                            case FloatRectDrawable.EDGE_MOVE_OUT:
                                break;

                            default:
                                break;
                        }
                        left = (int) setInBoundX(left, mCropBound);
                        right = (int) setInBoundX(right, mCropBound);
                        top = (int) setInBoundY(top, mCropBound);
                        bottom = (int) setInBoundY(bottom, mCropBound);

                        if ((right - left) > minWidth)
                            mCropRect.set(left, mCropRect.top, right, mCropRect.bottom);

                        if ((bottom - top) > minHeight)
                            mCropRect.set(mCropRect.left, top, mCropRect.right, bottom);
                    }
                    mCropRect.sort();
                    break;
                default:
                    break;
            }
        } else {
            if (mCropQuad == null) return false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pointX = event.getX();
                    pointY = event.getY();
                    currentEdge = mFloatQuadDrawable.getTouchEdge((int) event.getX(), (int) event.getY());
                    isTouchInSquare = currentEdge == FloatQuadDrawable.EDGE_MOVE_IN;
                    break;
                case MotionEvent.ACTION_MOVE:

                    float dx = event.getX() - pointX;
                    float dy = event.getY() - pointY;

                    pointX = event.getX();
                    pointY = event.getY();

                    if (isTouchInSquare) {
                        mCropQuad.offset(mCropQuad.stillInside(mCropBound, (int) dx, true) ? (int) dx : 0, mCropQuad.stillInside(mCropBound, (int) dy, false) ? (int) dy : 0);
                    } else {

                        switch (currentEdge) {
                            case FloatQuadDrawable.EDGE_LT:
                                mCropQuad.lt.offset(mCropQuad.stillInside(mCropBound, mCropQuad.lt, (int) dx, true) ? (int) dx : 0,
                                        mCropQuad.stillInside(mCropBound, mCropQuad.lt, (int) dy, false) ? (int) dy : 0);
                                break;

                            case FloatQuadDrawable.EDGE_RT:
                                mCropQuad.rt.offset(mCropQuad.stillInside(mCropBound, mCropQuad.rt, (int) dx, true) ? (int) dx : 0,
                                        mCropQuad.stillInside(mCropBound, mCropQuad.rt, (int) dy, false) ? (int) dy : 0);
                                break;

                            case FloatQuadDrawable.EDGE_LB:
                                mCropQuad.lb.offset(mCropQuad.stillInside(mCropBound, mCropQuad.lb, (int) dx, true) ? (int) dx : 0,
                                        mCropQuad.stillInside(mCropBound, mCropQuad.lb, (int) dy, false) ? (int) dy : 0);
                                break;

                            case FloatQuadDrawable.EDGE_RB:
                                mCropQuad.rb.offset(mCropQuad.stillInside(mCropBound, mCropQuad.rb, (int) dx, true) ? (int) dx : 0,
                                        mCropQuad.stillInside(mCropBound, mCropQuad.rb, (int) dy, false) ? (int) dy : 0);
                                break;

                            case FloatQuadDrawable.EDGE_TOP:

                                if (!mCropQuad.stillInside(mCropBound, mCropQuad.lt, (int) dx, true) ||
                                        !mCropQuad.stillInside(mCropBound, mCropQuad.rt, (int) dx, true)) dx = 0;
                                if (!mCropQuad.stillInside(mCropBound, mCropQuad.lt, (int) dy, false) ||
                                        !mCropQuad.stillInside(mCropBound, mCropQuad.rt, (int) dy, false)) dy = 0;

                                mCropQuad.lt.offset((int)dx, (int)dy);
                                mCropQuad.rt.offset((int)dx, (int)dy);
                                break;

                            case FloatQuadDrawable.EDGE_BOTTOM:
                                if (!mCropQuad.stillInside(mCropBound, mCropQuad.lb, (int) dx, true) ||
                                        !mCropQuad.stillInside(mCropBound, mCropQuad.rb, (int) dx, true)) dx = 0;
                                if (!mCropQuad.stillInside(mCropBound, mCropQuad.lb, (int) dy, false) ||
                                        !mCropQuad.stillInside(mCropBound, mCropQuad.rb, (int) dy, false)) dy = 0;

                                mCropQuad.lb.offset((int)dx, (int)dy);
                                mCropQuad.rb.offset((int)dx, (int)dy);
                                break;

                            case FloatQuadDrawable.EDGE_LEFT:
                                if (!mCropQuad.stillInside(mCropBound, mCropQuad.lt, (int) dx, true) ||
                                        !mCropQuad.stillInside(mCropBound, mCropQuad.lb, (int) dx, true)) dx = 0;
                                if (!mCropQuad.stillInside(mCropBound, mCropQuad.lt, (int) dy, false) ||
                                        !mCropQuad.stillInside(mCropBound, mCropQuad.lb, (int) dy, false)) dy = 0;

                                mCropQuad.lt.offset((int)dx, (int)dy);
                                mCropQuad.lb.offset((int)dx, (int)dy);
                                break;

                            case FloatQuadDrawable.EDGE_RIGHT:
                                if (!mCropQuad.stillInside(mCropBound, mCropQuad.rt, (int) dx, true) ||
                                        !mCropQuad.stillInside(mCropBound, mCropQuad.rb, (int) dx, true)) dx = 0;
                                if (!mCropQuad.stillInside(mCropBound, mCropQuad.rt, (int) dy, false) ||
                                        !mCropQuad.stillInside(mCropBound, mCropQuad.rb, (int) dy, false)) dy = 0;

                                mCropQuad.rt.offset((int)dx, (int)dy);
                                mCropQuad.rb.offset((int)dx, (int)dy);
                                break;

                            case FloatQuadDrawable.EDGE_MOVE_OUT:
                                break;

                            default:
                                break;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        invalidate();
        return true;
    }

    public class FloatRectDrawable extends Drawable {

        private Context mContext;
        private Paint cropRectLinePaint = new Paint();
        private Paint cornerLinePaint = new Paint();
        private TextPaint textPaint;

        private int touchAreaOffset = 0;
        private float halfCornerRough = 5.0f;
        private int cornerLineWidth = 16 * (int) halfCornerRough;
        private int cornerLineHeight = (int) (0.618 * cornerLineWidth);

        public static final int EDGE_LT = 1;
        public static final int EDGE_LB = 2;
        public static final int EDGE_RT = 3;
        public static final int EDGE_RB = 4;
        public static final int EDGE_MOVE_IN = 5;
        public static final int EDGE_MOVE_OUT = 6;
        public static final int EDGE_LEFT = 7;
        public static final int EDGE_RIGHT = 8;
        public static final int EDGE_TOP = 9;
        public static final int EDGE_BOTTOM = 10;

        public FloatRectDrawable(Context context) {
            super();
            this.mContext = context;
            touchAreaOffset = dipToPix(mContext, 80.0f);
            cropRectLinePaint.setStrokeWidth(2.0f);
            cropRectLinePaint.setStyle(Style.STROKE);
            cropRectLinePaint.setAntiAlias(true);
            cropRectLinePaint.setColor(Color.WHITE);

            cornerLinePaint.setStrokeWidth(halfCornerRough * 2);
            cornerLinePaint.setStyle(Style.STROKE);
            cornerLinePaint.setAntiAlias(true);
            cornerLinePaint.setColor(Color.WHITE);

            textPaint = new TextPaint();
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(30);
            textPaint.setColor(Color.RED);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect mRect = new Rect(getBounds());
            mRect.inset(touchAreaOffset / 2, touchAreaOffset / 2);

            canvas.drawRect(mRect, cropRectLinePaint);

            canvas.drawLines(new float[]{
                    mRect.left, mRect.top + mRect.height() / 3, mRect.right, mRect.top + mRect.height() / 3,
                    mRect.left, mRect.bottom - mRect.height() / 3, mRect.right, mRect.bottom - mRect.height() / 3,
                    mRect.left + mRect.width() / 3, mRect.top, mRect.left + mRect.width() / 3, mRect.bottom,
                    mRect.right - mRect.width() / 3, mRect.top, mRect.right - mRect.width() / 3, mRect.bottom,
            }, cropRectLinePaint);

            canvas.drawLine(mRect.left - halfCornerRough, mRect.top, mRect.left + cornerLineWidth, mRect.top, cornerLinePaint);
            canvas.drawLine(mRect.left, mRect.top, mRect.left, mRect.top + cornerLineHeight, cornerLinePaint);
            canvas.drawLine(mRect.right - cornerLineWidth, mRect.top, mRect.right + halfCornerRough, mRect.top, cornerLinePaint);
            canvas.drawLine(mRect.right, mRect.top, mRect.right, mRect.top + cornerLineHeight, cornerLinePaint);
            canvas.drawLine(mRect.left - halfCornerRough, mRect.bottom, mRect.left + cornerLineWidth, mRect.bottom, cornerLinePaint);
            canvas.drawLine(mRect.left, mRect.bottom - cornerLineHeight, mRect.left, mRect.bottom, cornerLinePaint);
            canvas.drawLine(mRect.right - cornerLineWidth, mRect.bottom, mRect.right + halfCornerRough, mRect.bottom, cornerLinePaint);
            canvas.drawLine(mRect.right, mRect.bottom - cornerLineHeight, mRect.right, mRect.bottom, cornerLinePaint);
        }

        @Override
        public void setBounds(Rect bounds) {
            super.setBounds(new Rect(
                    bounds.left - touchAreaOffset / 2,
                    bounds.top - touchAreaOffset / 2,
                    bounds.right + touchAreaOffset / 2,
                    bounds.bottom + touchAreaOffset / 2));
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter cf) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }

        public int dipToPix(Context context, float dpValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }

        /**
         * get the position of the float rect when this view is touched
         *
         * @param eventX touch x
         * @param eventY touch y
         * @return the position of the rect
         */
        public int getTouchEdge(int eventX, int eventY) {

            boolean x_left = getBounds().left <= eventX && eventX < (getBounds().left + touchAreaOffset);
            boolean x_center = (getBounds().left + touchAreaOffset) <= eventX && eventX < (getBounds().right - touchAreaOffset);
            boolean x_right = (getBounds().right - touchAreaOffset) <= eventX && eventX < getBounds().right;

            boolean y_top = getBounds().top <= eventY && eventY < (getBounds().top + touchAreaOffset);
            boolean y_center = (getBounds().top + touchAreaOffset) <= eventY && eventY < (getBounds().bottom - touchAreaOffset);
            boolean y_bottom = (getBounds().bottom - touchAreaOffset) <= eventY && eventY < getBounds().bottom;

            if (x_left && y_top)
                return EDGE_LT;
            else if (x_left && y_center)
                return EDGE_LEFT;
            else if (x_left && y_bottom)
                return EDGE_LB;

            else if (x_center && y_top)
                return EDGE_TOP;
            else if (x_center && y_center)
                return EDGE_MOVE_IN;
            else if (x_center && y_bottom)
                return EDGE_BOTTOM;

            else if (x_right && y_top)
                return EDGE_RT;
            else if (x_right && y_center)
                return EDGE_RIGHT;
            else if (x_right && y_bottom)
                return EDGE_RB;

            else return EDGE_MOVE_OUT;

        }
    }

    public class FloatQuadDrawable extends Drawable {

        private Context mContext;
        private Paint cropQuadPaint, cropQuadCornerPaint,cropQuadStrokePaint;
        private Quad cropQuad;
        private int touchAreaOffset = 0;

        public static final int EDGE_LT = 1;
        public static final int EDGE_LB = 2;
        public static final int EDGE_RT = 3;
        public static final int EDGE_RB = 4;
        public static final int EDGE_MOVE_IN = 5;
        public static final int EDGE_MOVE_OUT = 6;
        public static final int EDGE_LEFT = 7;
        public static final int EDGE_RIGHT = 8;
        public static final int EDGE_TOP = 9;
        public static final int EDGE_BOTTOM = 10;

        public FloatQuadDrawable(Context context) {

            this.mContext = context;
            touchAreaOffset = dipToPix(mContext, 40);

            cropQuad = new Quad();

            cropQuadPaint = new Paint();
            cropQuadPaint.setStrokeWidth(2.0f);
            cropQuadPaint.setStyle(Style.STROKE);
            cropQuadPaint.setAntiAlias(true);
            cropQuadPaint.setColor(Color.WHITE);

            cropQuadCornerPaint = new Paint();
            cropQuadCornerPaint.setStrokeWidth(2.0f);
            cropQuadCornerPaint.setStyle(Style.FILL);
            cropQuadCornerPaint.setAntiAlias(true);
            cropQuadCornerPaint.setColor(Color.WHITE);

            cropQuadStrokePaint = new Paint();
            cropQuadStrokePaint.setStrokeWidth(2.0f);
            cropQuadStrokePaint.setStyle(Style.FILL);
            cropQuadStrokePaint.setAntiAlias(true);
            cropQuadStrokePaint.setColor(Color.WHITE);
            cropQuadStrokePaint.setStyle(Style.STROKE);
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawPath(cropQuad.getPath(), cropQuadPaint);

            canvas.drawCircle(cropQuad.getTopCenter().x, cropQuad.getTopCenter().y, mCornerDiameter, cropQuadCornerPaint);
            canvas.drawCircle(cropQuad.getBottomCenter().x, cropQuad.getBottomCenter().y, mCornerDiameter, cropQuadCornerPaint);
            canvas.drawCircle(cropQuad.getLeftCenter().x, cropQuad.getLeftCenter().y, mCornerDiameter, cropQuadCornerPaint);
            canvas.drawCircle(cropQuad.getRightCenter().x, cropQuad.getRightCenter().y, mCornerDiameter, cropQuadCornerPaint);

            canvas.drawCircle(cropQuad.getTopCenter().x, cropQuad.getTopCenter().y, mCornerDiameter, cropQuadStrokePaint);
            canvas.drawCircle(cropQuad.getBottomCenter().x, cropQuad.getBottomCenter().y, mCornerDiameter, cropQuadStrokePaint);
            canvas.drawCircle(cropQuad.getLeftCenter().x, cropQuad.getLeftCenter().y, mCornerDiameter, cropQuadStrokePaint);
            canvas.drawCircle(cropQuad.getRightCenter().x, cropQuad.getRightCenter().y, mCornerDiameter, cropQuadStrokePaint);

            canvas.drawCircle(cropQuad.lt.x, cropQuad.lt.y, mCornerDiameter, cropQuadCornerPaint);
            canvas.drawCircle(cropQuad.rt.x, cropQuad.rt.y, mCornerDiameter, cropQuadCornerPaint);
            canvas.drawCircle(cropQuad.rb.x, cropQuad.rb.y, mCornerDiameter, cropQuadCornerPaint);
            canvas.drawCircle(cropQuad.lb.x, cropQuad.lb.y, mCornerDiameter, cropQuadCornerPaint);

            canvas.drawCircle(cropQuad.lt.x, cropQuad.lt.y, mCornerDiameter, cropQuadStrokePaint);
            canvas.drawCircle(cropQuad.rt.x, cropQuad.rt.y, mCornerDiameter, cropQuadStrokePaint);
            canvas.drawCircle(cropQuad.rb.x, cropQuad.rb.y, mCornerDiameter, cropQuadStrokePaint);
            canvas.drawCircle(cropQuad.lb.x, cropQuad.lb.y, mCornerDiameter, cropQuadStrokePaint);
        }

        public void setBounds(Quad bounds) {
            this.cropQuad = bounds;
        }

        /**
         * get the position of the float rect when this view is touched
         *
         * @param eventX touch x
         * @param eventY touch y
         * @return the position of the rect
         */
        public int getTouchEdge(int eventX, int eventY) {
            Rect rect = new Rect();
            rect.inset(touchAreaOffset, touchAreaOffset);
            rect.sort();

            rect.offsetTo(cropQuad.lt.x - touchAreaOffset, cropQuad.lt.y - touchAreaOffset);
            boolean lt = rect.contains(eventX, eventY);

            rect.offsetTo(cropQuad.rt.x - touchAreaOffset, cropQuad.rt.y - touchAreaOffset);
            boolean rt = rect.contains(eventX, eventY);

            rect.offsetTo(cropQuad.rb.x - touchAreaOffset, cropQuad.rb.y - touchAreaOffset);
            boolean rb = rect.contains(eventX, eventY);

            rect.offsetTo(cropQuad.lb.x - touchAreaOffset, cropQuad.lb.y - touchAreaOffset);
            boolean lb = rect.contains(eventX, eventY);

            rect.offsetTo(cropQuad.getTopCenter().x - touchAreaOffset, cropQuad.getTopCenter().y - touchAreaOffset);
            boolean top = rect.contains(eventX, eventY);
            rect.offsetTo(cropQuad.getBottomCenter().x - touchAreaOffset, cropQuad.getBottomCenter().y - touchAreaOffset);
            boolean bottom = rect.contains(eventX, eventY);
            rect.offsetTo(cropQuad.getLeftCenter().x - touchAreaOffset, cropQuad.getLeftCenter().y - touchAreaOffset);
            boolean left = rect.contains(eventX, eventY);
            rect.offsetTo(cropQuad.getRightCenter().x - touchAreaOffset, cropQuad.getRightCenter().y - touchAreaOffset);
            boolean right = rect.contains(eventX, eventY);

            if (lt) return EDGE_LT;
            else if (rt) return EDGE_RT;
            else if (rb) return EDGE_RB;
            else if (lb) return EDGE_LB;

            else if (top) return EDGE_TOP;
            else if (bottom) return EDGE_BOTTOM;
            else if (left) return EDGE_LEFT;
            else if (right) return EDGE_RIGHT;

            else if (mCropQuad.contains(new Point(eventX, eventY))) return EDGE_MOVE_IN;
            else return EDGE_MOVE_OUT;
        }

        public int dipToPix(Context context, float dpValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter cf) {

        }

        @Override
        public int getOpacity() {
            return 0;
        }
    }

    /**
     * Irregular quadrilateral
     */
    private class Quad {
        public Point lt = new Point();
        public Point lb = new Point();
        public Point rt = new Point();
        public Point rb = new Point();

        public Quad() {

        }

        public Quad(Quad quad) {
            lt.x = quad.lt.x;
            lt.y = quad.lt.y;
            rt.x = quad.rt.x;
            rt.y = quad.rt.y;
            rb.x = quad.rb.x;
            rb.y = quad.rb.y;
            lb.x = quad.lb.x;
            lb.y = quad.lb.y;
        }

        public Quad(int[] bound) {
            if (bound.length != 8)
                return;
            lt.x = bound[0];
            lt.y = bound[1];
            rt.x = bound[2];
            rt.y = bound[3];

            rb.x = bound[4];
            rb.y = bound[5];
            lb.x = bound[6];
            lb.y = bound[7];
        }

        public Quad(Rect rect) {
            set(rect);
        }

        public int[] getBound() {
            return new int[]{lt.x, lt.y, rt.x, rt.y, rb.x, rb.y, lb.x, lb.y};
        }

        public void offset(int x, int y) {
            lt.offset(x, y);
            rt.offset(x, y);
            rb.offset(x, y);
            lb.offset(x, y);
        }

        public void scaleIn(float scale) {
            lt.x = (int) (lt.x * scale);
            lt.y = (int) (lt.y * scale);
            rt.x = (int) (rt.x * scale);
            rt.y = (int) (rt.y * scale);
            rb.x = (int) (rb.x * scale);
            rb.y = (int) (rb.y * scale);
            lb.x = (int) (lb.x * scale);
            lb.y = (int) (lb.y * scale);
        }

        public void set(Rect rect) {
            this.lt.x = this.lb.x = rect.left;
            this.lt.y = this.rt.y = rect.top;
            this.lb.y = this.rb.y = rect.bottom - 1;
            this.rt.x = this.rb.x = rect.right - 1;
        }

        public Path getPath() {
            Path path = new Path();
            path.moveTo(lt.x, lt.y);
            path.lineTo(rt.x, rt.y);
            path.lineTo(rb.x, rb.y);
            path.lineTo(lb.x, lb.y);
            path.close();
            return path;
        }

        public Point getTopCenter() {
            return new Point((lt.x + rt.x) / 2, (lt.y + rt.y) / 2);
        }

        public Point getBottomCenter() {
            return new Point((lb.x + rb.x) / 2, (lb.y + rb.y) / 2);
        }

        public Point getLeftCenter() {
            return new Point((lt.x + lb.x) / 2, (lt.y + lb.y) / 2);
        }

        public Point getRightCenter() {
            return new Point((rt.x + rb.x) / 2, (rt.y + rb.y) / 2);
        }

        public double getLength(Point a, Point b) {
            return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
        }

        public double getSquare(Point a, Point b, Point c) {
            double A = getLength(b, c);
            double B = getLength(a, c);
            double C = getLength(a, b);
            double S = (A + B + C) / 2;
            return Math.sqrt(S * (S - A) * (S - B) * (S - C));
        }

        public boolean contains(Point p) {
            double AOB = getSquare(lt, rt, p);
            double BOC = getSquare(rt, rb, p);
            double COD = getSquare(lb, rb, p);
            double DOA = getSquare(lt, lb, p);

            double S_Quad = getSquare(lt, rt, rb) + getSquare(lt, lb, rb);

            double S_Points = AOB + BOC + COD + DOA;

            return S_Quad + 1 >= S_Points;
        }

        public boolean stillInside(Rect rect, int offset, boolean X) {
            int offsetX = X ? offset : 0;
            int offsetY = !X ? offset : 0;
            return rect.contains(lt.x + offsetX, lt.y + offsetY) && rect.contains(rt.x + offsetX, rt.y + offsetY)
                    && rect.contains(rb.x + offsetX, rb.y + offsetY) && rect.contains(lb.x + offsetX, lb.y + offsetY);
        }

        public boolean stillInside(Rect rect, Point who, int offset, boolean X) {
            int offsetX = X ? offset : 0;
            int offsetY = !X ? offset : 0;
            return rect.contains(who.x + offsetX, who.y + offsetY);
        }

    }

}
