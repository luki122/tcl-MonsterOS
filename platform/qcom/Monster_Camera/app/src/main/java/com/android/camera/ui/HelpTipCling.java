package com.android.camera.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
 /*MODIFIED-BEGIN by nie.lei, 2016-04-01,BUG-1899903*/
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.camera.HelpTip;
import com.android.camera.HelpTipsManager;
import com.android.camera.util.CameraUtil;
 /*MODIFIED-END by nie.lei,BUG-1899903*/
import com.tct.camera.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sdduser on 15-11-16.
 */
public class HelpTipCling extends FrameLayout {
    private static final int RING_WIDTH = 2;
    private static final float CIRCLE_BOLD = 7f;
    private static final int ERASE_COLOR = 0xFFFFFF;
     /*MODIFIED-BEGIN by nie.lei, 2016-04-01,BUG-1899903*/
    private static final int ARROW_ROTATION_180 = 180;
    public final boolean mIsLayoutDirectionRtl;
     /*MODIFIED-END by nie.lei,BUG-1899903*/
    private int mDrawRectangleRadiuX;
    private int mDrawRectangleRadiuY;
    private int mDrawRectangleEraseRadiuX;
    private int mDrawRectangleEraseRadiuY;

    private int mBackColor;
    private HelpTip mListener;
    private Paint mCirclePaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private int mDrawType;
    private RectF mRectF = new RectF();
    private View mPreViewOverLay;
    private View mFocusView;
    private int mLeft;
    private int mTop;
    private int mRight;
    private int mBottom;
    private float mRingWidth;
    private Paint mErasePaint;
    private View mDrawCircleView;
    private List<Rect> mHitClickRect = new ArrayList<Rect>();
    private float mDensity;
    private RelativeLayout mManualMenuTip;
     /*MODIFIED-BEGIN by nie.lei, 2016-04-01,BUG-1899903*/
    private ImageView mMenuArrow;
    private ImageView mHelpTipArrow;
     /*MODIFIED-END by nie.lei,BUG-1899903*/

    public HelpTipCling(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        initPainter();

        //get the denisity to set the ring width.
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(metrics);
        mDensity = metrics.density;
        mRingWidth = RING_WIDTH * metrics.density;

        mDrawRectangleRadiuX = getResources().getInteger(R.integer.help_tip_draw_rectangle_radius_x);
        mDrawRectangleRadiuY = getResources().getInteger(R.integer.help_tip_draw_rectangle_radius_y);
        mDrawRectangleEraseRadiuX = getResources().getInteger(R.integer.help_tip_draw_rectangle_erase_radius_x);
        mDrawRectangleEraseRadiuY = getResources().getInteger(R.integer.help_tip_draw_rectangle_erase_radius_y);
        mIsLayoutDirectionRtl = CameraUtil.isLayoutDirectionRtl(context); //MODIFIED by nie.lei, 2016-04-01,BUG-1899903
    }

    public void setListener(HelpTip listener, int drawType) {
        mListener = listener;
        mDrawType = drawType;
    }

    public void setPreViewOverLay(View preViewOverLay) {
        mPreViewOverLay = preViewOverLay;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
         /*MODIFIED-BEGIN by nie.lei, 2016-04-01,BUG-1899903*/
        mMenuArrow = (ImageView)findViewById(R.id.manual_menu_arrow);
        mHelpTipArrow = (ImageView)findViewById(R.id.manual_help_tip_arrow);
        if(mIsLayoutDirectionRtl) {
            if(mMenuArrow !=null){
                mMenuArrow.setRotationY(ARROW_ROTATION_180);
            }
            if(mHelpTipArrow !=null){
                mHelpTipArrow.setRotationY(ARROW_ROTATION_180);
            }
            requestLayout();
            invalidate();
        }
         /*MODIFIED-END by nie.lei,BUG-1899903*/

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        /* MODIFIED-BEGIN by xuan.zhou, 2016-04-26,BUG-1996414*/
        if (getWidth() > 0 && getHeight() > 0) {
            mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }
        /* MODIFIED-END by xuan.zhou,BUG-1996414*/
    }

    private void initPainter() {
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(getResources().getColor(R.color.help_tip_animation_circle_color));
        mCirclePaint.setStrokeWidth(CIRCLE_BOLD);
        mCirclePaint.setStyle(Paint.Style.STROKE);

        mBackColor = getResources().getColor(R.color.tourial_semitransparent);

        // init erasePaint
        mErasePaint = new Paint();
        mErasePaint.setXfermode(new PorterDuffXfermode(
                PorterDuff.Mode.MULTIPLY));
        mErasePaint.setColor(ERASE_COLOR);
        mErasePaint.setAntiAlias(true);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mListener == null) return false;
        int index = -1;
        if (mHitClickRect != null) {
            for (int i = 0; i <= mHitClickRect.size() - 1; i++) {
                if (mHitClickRect.get(i).contains((int) ev.getX(), (int) ev.getY())) {
                    index = i;
                    break;
                }
            }
        }

        if (index != -1) {
            mListener.clickHitRectResponse(index);
        }
        if (mPreViewOverLay != null) {
            return onTouchEvent(ev);
        }

        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mPreViewOverLay != null) {
            return mPreViewOverLay.onTouchEvent(ev);
        } else {
            return super.onTouchEvent(ev);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mListener == null) return;
        if (mDrawType == HelpTip.NO_DRAW) {
            drawHelpTipDefault(canvas);
        } else if (mDrawType == HelpTip.CIRCLE) {
            drawHelpTipCircle(canvas);
        } else if (mDrawType == HelpTip.RECTANGLE) {
            drawHelpTipRectangle(canvas);
        } else if (mDrawType == HelpTip.LINE) {
            drawHelpTipLine(canvas);
        }
    }

    private void drawHelpTipDefault(Canvas canvas) {
        mBitmap.eraseColor(Color.TRANSPARENT);
        mCanvas.drawColor(mBackColor);
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    private void drawHelpTipCircle(Canvas canvas) {
        mBitmap.eraseColor(Color.TRANSPARENT);
        mCanvas.drawColor(mBackColor);
        if (mFocusView != null) {
            mLeft = mFocusView.getLeft();
            mTop = mFocusView.getTop();
            mRight = mFocusView.getRight();
            mBottom = mFocusView.getBottom();

            int width = mRight - mLeft;
            int height = mBottom - mTop;

            float cx = (mLeft + mRight) / 2;
            float cy = (mTop + mBottom) / 2;
            float radius = width > height ? width / 2 : height / 2;

            // draw circle
            mCanvas.drawCircle(cx, cy, radius, mCirclePaint);
            // erase hole
            mCanvas.drawCircle(cx, cy, radius - mRingWidth, mErasePaint);
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
    }

    private void drawHelpTipRectangle(Canvas canvas) {
        mBitmap.eraseColor(Color.TRANSPARENT);
        mCanvas.drawColor(mBackColor);
        if (mFocusView != null) {
            Rect rect = new Rect();
            mFocusView.getHitRect(rect);
            mRectF.set(rect);

            if (mListener.getCurTipGroupId() == HelpTipsManager.MANUAL_GROUP) {
                mDrawRectangleRadiuX += getResources().getInteger(R.integer.manual_tip_menu_item_radius_increment_x);
                mDrawRectangleRadiuY += getResources().getInteger(R.integer.manual_tip_menu_item_radius_increment_y);
                mDrawRectangleEraseRadiuX += getResources().getInteger(R.integer.manual_tip_menu_item_erase_radius_increment_x);
                mDrawRectangleEraseRadiuY += getResources().getInteger(R.integer.manual_tip_menu_item_erase_radius_increment_y);
            }
            mCanvas.drawRoundRect(mRectF, mDrawRectangleRadiuX, mDrawRectangleRadiuY, mCirclePaint);
            mRectF.inset(mDensity, mDensity);
            mCanvas.drawRoundRect(mRectF, mDrawRectangleEraseRadiuX, mDrawRectangleEraseRadiuY, mErasePaint);
        }

        if (mDrawCircleView != null) {
            mLeft = mDrawCircleView.getLeft();
            mTop = mDrawCircleView.getTop();
            mRight = mDrawCircleView.getRight();
            mBottom = mDrawCircleView.getBottom();

            int width = mRight - mLeft;
            int height = mBottom - mTop;

            float cx = (mLeft + mRight) / 2;
            float cy = (mTop + mBottom) / 2;
            float radius = width > height ? width / 2 : height / 2;

            // draw circle
            mCanvas.drawCircle(cx, cy, radius, mCirclePaint);
            // erase hole
            mCanvas.drawCircle(cx, cy, radius - mRingWidth, mErasePaint);
        }

        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    private void drawHelpTipLine(Canvas canvas) {
        if (mFocusView != null) {
            mBitmap.eraseColor(Color.TRANSPARENT);
            mCanvas.drawColor(mBackColor);
            mLeft = mFocusView.getLeft();
            mTop = mFocusView.getTop();
            mRight = mFocusView.getRight();
            mBottom = mFocusView.getBottom();

            mCanvas.drawRect(0, mTop, mRight, mBottom, mCirclePaint);
            mCanvas.drawRect(0, mTop, mRight, mBottom, mErasePaint);
            mCanvas.drawLine(0, mTop, mRight, mTop, mCirclePaint);
            mCanvas.drawLine(0, mBottom, mRight, mBottom, mCirclePaint);
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
    }

    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (params instanceof LayoutParams) {
            LayoutParams clingLayoutParams = (LayoutParams) params;
            if (clingLayoutParams.isFocusView()) {
                this.mFocusView = child;
            }
            if (clingLayoutParams.isDrawCircleView()) {
                this.mDrawCircleView = child;
            }
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public void cleanDestroy() {
        mCirclePaint = null;
        mErasePaint = null;
        mFocusView = null;
        mDrawCircleView = null;
    }

    public void setHitRect(List<Rect> listRect) {
        mHitClickRect = listRect;
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {

        private boolean isFocusView = false;
        private boolean isCircleView = false;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.HelpTipCling);
            isFocusView = a.getBoolean(R.styleable.HelpTipCling_is_focus_view, false);
            isCircleView = a.getBoolean(R.styleable.HelpTipCling_is_circle_view, false);
            a.recycle();
        }

        public Boolean isFocusView() {
            return isFocusView;
        }

        public Boolean isDrawCircleView() {
            return isCircleView;
        }
    }
}
