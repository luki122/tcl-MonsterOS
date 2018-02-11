package com.gapp.common.animation.connector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.gapp.common.animation.IServantConnecter;
import com.gapp.common.animation.view.SnowSprite;
import com.gapp.common.interpolator.InterpolatorXY;
import com.gapp.common.interpolator.LinearInterpolatorXy;
import com.gapp.common.scroller.SingelScroller;
import com.gapp.common.scroller.XyScroller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-10-31.
 * $desc
 */
public class SnowConnector extends View implements IServantConnecter {

    public final static int CONTROLL_STATE_CONNECTED = 11;

    public final static int CONTROLL_STATE_ATTACHED = 12;

    //    /**
//     * servant state
//     */
    public final static int SERVANT_STATE_RESET = 10;
//    public final static int SERVANT_STATE_LAYOUT = 11;

    private final static float ADDING＿SIZE = 0.95f;
    private final static float R_SCALE = (float) (Math.sqrt(2) / 2.0f);

    private final static int STATE_NULL = 0;
    private final static int STATE_FULL = 1;
    private final static int STATE_ANMATING = 2;
    private final static int STATE_MOVING = 3;


    private int mColumnCounts = 10;
    private int mRawCounts = 10;

    private float[][] mCheckerboard;

    private float mCheckerboardItemWidth, mCheckerboardItemHeight;

    private int mWidth, mHeight;
    private int mPx, mPy;

    private float[] mScaleRange = new float[]{0f, 1f};

    private int mMaxSize = 40;

    private int mStates = STATE_NULL;

    private ArrayList<SnowBall> mSnowBalls = new ArrayList<>(30);

    private Paint mPaint = new Paint();


    private InnerServantConnecter mInnerServantConnecter = new InnerServantConnecter();


    public SnowConnector(Context context) {
        super(context);
    }

    public SnowConnector(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SnowConnector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SnowConnector(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setScaleRange(float min, float max) {
        mScaleRange[0] = min;
        mScaleRange[1] = max;
    }


    public void setMaxSize(int size) {
        mMaxSize = size;
    }

    /**
     * set the width of this connector, you should set it before system call {@link #layout(int, int, int, int)}
     *
     * @param w
     * @param h
     */
    public void setConnectorSize(int w, int h) {
        mWidth = w;
        mHeight = h;
        layout(mPx, mPy, mWidth, mHeight);
    }

    /**
     * set the position of this connector in parent
     *
     * @param px
     * @param py
     */
    public void setPosition(int px, int py) {
        mPx = px;
        mPy = py;
        layout(px, py, getWidth(), getHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mWidth != 0 && mHeight != 0)
            setMeasuredDimension(mWidth, mHeight);
        else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setCheckerboard(int raw, int column) {
        if (mColumnCounts != column || raw != mRawCounts) {
            mColumnCounts = column;
            mRawCounts = raw;
            mCheckerboard = new float[raw][column];
            caculateItemSize();
        }
    }

    private void caculateItemSize() {
        mCheckerboardItemWidth = ((float) getWidth() - getPaddingLeft() - getPaddingRight()) / ((float) mColumnCounts);
        mCheckerboardItemHeight = ((float) getHeight() - getPaddingTop() - getPaddingBottom()) / ((float) mRawCounts);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            layout(mPx, mPy, mPx + mWidth, mPy + mHeight);
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.FILL);
            setCheckerboard(mRawCounts, mColumnCounts);
            caculateItemSize();
        }
    }

    private int getCurrentState() {
        if (STATE_ANMATING != mStates) {
            if (mSnowBalls.size() >= mMaxSize) {
                mStates = STATE_FULL;
            } else if (getTranslationY() != 0) {
                mStates = STATE_MOVING;
            } else {
                mStates = STATE_NULL;
            }
        } else if (mSnowBalls.isEmpty()) {
            mStates = STATE_NULL;
        }
        return mStates;
    }

    private int caculateBallRaw(float px, float radius) {
        if (null == mCheckerboard)
            return -1;
        if (px - radius > getPaddingLeft() && px + radius < getWidth() - getPaddingRight()) {
            px -= getPaddingLeft();

            float r = R_SCALE * radius;

            int columnLeft = getRightColumn((int) ((px - r) / mCheckerboardItemWidth));
            int columnRight = getRightColumn((int) ((px + r) / mCheckerboardItemWidth));

            int width = (columnRight - columnRight + 1);

            columnLeft = getRightColumn(columnLeft - width);
            columnRight = getRightColumn(columnRight + width);

            int row = -1;
            float sum;
            for (int i = mRawCounts - 1; i >= 0; i--) {
                sum = 0f;
                for (int c = columnLeft; c <= columnRight; c++) {
                    sum += mCheckerboard[i][c];
                }
                if (sum < (columnRight - columnLeft + 1) * ADDING＿SIZE) {
                    row = i;
                    break;
                }
            }
            return row;
        }
        return -1;
    }

    private int getRightRow(int row) {
        if (row < 0)
            row = 0;
        else if (row >= mRawCounts)
            row = mRawCounts - 1;
        return row;
    }

    private int getRightColumn(int column) {
        if (column < 0)
            column = 0;
        else if (column >= mColumnCounts)
            column = mColumnCounts - 1;
        return column;
    }

    private boolean addBallSpace(float px, float py, float radius, float alpha) {
        if (null == mCheckerboard)
            return false;

        px -= getPaddingLeft();
        py -= getPaddingTop();

        float r = R_SCALE * radius;
        int rowTop = getRightRow((int) ((py - r) / mCheckerboardItemHeight));
        int rowBottom = getRightRow((int) ((py + r) / mCheckerboardItemHeight));

        int columnLeft = getRightColumn((int) ((px - r) / mCheckerboardItemWidth));
        int columnRight = getRightColumn((int) ((px + r) / mCheckerboardItemWidth));

        RectF rect = new RectF(px - r, py - r, px + r, py + r);
        for (int row = rowTop; row <= rowBottom; row++) {
            for (int column = columnLeft; column <= columnRight; column++) {
                if (mCheckerboard[row][column] < 1.0f) {
                    mCheckerboard[row][column] += getRatio(rect, row, column) * alpha;
                    if (mCheckerboard[row][column] > 1.0f)
                        mCheckerboard[row][column] = 1.0f;
                }
            }
        }
        return true;
    }


    private float getRatio(RectF rect, int raw, int column) {
        float x = column * mCheckerboardItemWidth;
        float y = raw * mCheckerboardItemHeight;
        RectF boardRect = new RectF(x, y, x + mCheckerboardItemWidth, y + mCheckerboardItemHeight);

        RectF insertRect = new RectF();

        if (insertRect.setIntersect(rect, boardRect)) {
            return (insertRect.width() * insertRect.height()) / (boardRect.width() * boardRect.height());
        }
        return 0;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        for (SnowBall ball : mSnowBalls)
            ball.draw(canvas, mPaint);
    }

    private float caculateEndY(float px, float radius) {
        int row = caculateBallRaw(px, radius);
        if (-1 != row) {
            return row * mCheckerboardItemHeight + mCheckerboardItemHeight / 2.0f + getPaddingTop();
        }
        return -1f;// returh -1f means failed
    }

    @Override
    public void connectServants(List<IServant> servants) {
        mInnerServantConnecter.connectServants(servants);
    }

    @Override
    public void onServantStateChanged(IServant sprite, int state) {
        mInnerServantConnecter.onServantStateChanged(sprite, state);
    }

    @Override
    public void pause() {
        mInnerServantConnecter.pause();
    }

    @Override
    public void resume() {
        mInnerServantConnecter.resume();
    }

    @Override
    public void running() {
        mInnerServantConnecter.running();
    }

    @Override
    public void freeServant(IServant servant) {
        mInnerServantConnecter.freeServant(servant);
    }

    @Override
    public void init() {
        mInnerServantConnecter.init();
    }

    @Override
    public void recycle() {
        mInnerServantConnecter.recycle();
    }

    @Override
    public void onTrimMemory(int level) {
        mInnerServantConnecter.onTrimMemory(level);
    }


    /////////////////////////////////////////snow balls
    private final static int BALL_MAX_ALPHA = 255;
    private final static int BALL_STATE_NULL = 0;
    private final static int BALL_STATE_ANIMATE = 1;
    private final static float BALL_OFFSET_DISTANCE = 140f * 5;
    private final static int BALL_ANMATE_TIME_MILLS = 800;


    public void startBallAnimation() {
        mInnerServantConnecter.startBallAnimation();
    }

    private class SnowBall {
        private float mPx;
        private float mPy;
        private float mRadius;
        private int mAlpha = BALL_MAX_ALPHA;
        private float mOffsetX, mOffsetY;
        private int mState = BALL_STATE_NULL;
        private int mDrawAlpha;
        private XyScroller mXyScroller = new XyScroller();
        private SingelScroller mAlphaScroller = new SingelScroller();

        SnowBall(float px, float py, float radius, int alpha) {
            mPx = px;
            mPy = py;
            mRadius = radius;
            mAlpha = alpha;
            mDrawAlpha = mAlpha;
            mXyScroller.setInterpolatorXy(new LinearInterpolatorXy(), new DecelerateInterpolator());
            mAlphaScroller.setInterpolator(new AccelerateInterpolator());
        }

        SnowBall(float px, float py, float radius, int alpha, InterpolatorXY interpolatorXy) {
            this(px, py, radius, alpha);
            mXyScroller.setInterpolatorXy(interpolatorXy);
        }

        public void start() {
            if (mState == BALL_STATE_NULL) {
                mState = BALL_STATE_ANIMATE;
                float sourceY = getHeight();
                float vy = mPy - sourceY;
                vy = vy / getPaddingBottom();
                vy = vy / mRadius;
                mXyScroller.start(mOffsetX, mOffsetY, 0, vy * BALL_OFFSET_DISTANCE, BALL_ANMATE_TIME_MILLS);
                mAlphaScroller.start(mAlpha, -mAlpha, BALL_ANMATE_TIME_MILLS);
            }
        }

        public void run() {
            if (mState == BALL_STATE_ANIMATE) {
                if (mAlphaScroller.computeScrollOffset())
                    mDrawAlpha = (int) mAlphaScroller.getCurrent();

                if (mXyScroller.computeScrollOffset()) {
                    mOffsetX = mXyScroller.getCurrentX() - getTranslationX();
                    mOffsetY = mXyScroller.getCurrentY() - getTranslationY();
                    postInvalidate();
                } else {
                    mSnowBalls.remove(this);
                    postInvalidate();
                }
            }
        }

        public void pause() {
            if (mState == BALL_STATE_ANIMATE) {
                mXyScroller.pause();
                mAlphaScroller.pause();
            }
        }

        public void resume() {
            if (mState == BALL_STATE_ANIMATE) {
                mAlphaScroller.resume();
                mXyScroller.resume();
            }
        }


        private void draw(Canvas canvas, Paint paint) {
            canvas.save();
            paint.setAlpha(mDrawAlpha);
            canvas.translate(mOffsetX, mOffsetY);
            canvas.drawCircle(mPx, mPy, mRadius, paint);
            canvas.restore();
        }
    }


    private class SnowPayload {
        float mEndx, mEndy;
        SnowSprite mSprite;
    }

    private class InnerServantConnecter implements IServantConnecter {

        private int[] mLocation = new int[2];
        private int[] mChildLocation = new int[2];

        private LinkedList<SnowPayload> mSnowPayloads = new LinkedList<>();

        @Override
        public void connectServants(List<IServant> servants) {
            for (IServant servant : servants) {
                if (servant instanceof SnowSprite) {
                    servant.onControlByConnecter(SnowConnector.this, CONTROLL_STATE_CONNECTED);
                }
            }
        }


        public void startBallAnimation() {
            mStates = STATE_ANMATING;

            for (SnowBall ball : mSnowBalls)
                ball.start();
            // clearn eckeckboard and payloads
            mSnowPayloads.clear();
            for (int raw = 0; raw < mRawCounts; raw++) {
                for (int column = 0; column < mColumnCounts; column++)
                    mCheckerboard[raw][column] = 0f;
            }
        }

        @Override
        public void onServantStateChanged(IServant servant, int state) {
            switch (state) {
                case SERVANT_STATE_ADD:// if servant is added
                    if (servant instanceof SnowSprite)
                        servant.onControlByConnecter(SnowConnector.this, CONTROLL_STATE_CONNECTED);
                    break;
                case SERVANT_STATE_REMOVE:// if servant is removed
                    break;
                case SERVANT_STATE_RESET:

                    if (STATE_NULL != getCurrentState())
                        return;

                    SnowSprite sprite = (SnowSprite) servant;
                    float scale = sprite.getScaleX();
                    if (scale < mScaleRange[0] || scale > mScaleRange[1])// if snow ball's scale is not in ange , do not add it
                        return;

                    getLocationOnScreen(mLocation);
                    sprite.getLocationOnScreen(mChildLocation);
                    final float radius = sprite.getWidth() * scale / 2.0f;// caculate the radius of snow
                    float px = mChildLocation[0] - mLocation[0] + radius;
                    float py = caculateEndY(px, radius);
                    if (py > 0) {// it can added
                        SnowPayload snowPayload = new SnowPayload();
                        snowPayload.mEndx = px;
                        snowPayload.mEndy = py;
                        snowPayload.mSprite = sprite;
                        mSnowPayloads.add(snowPayload);
                    }
                    break;
            }
        }

        @Override
        public void pause() {
            for (SnowBall ball : mSnowBalls)
                ball.pause();
        }

        @Override
        public void resume() {
            for (SnowBall ball : mSnowBalls)
                ball.resume();
        }

        @Override
        public void running() {
            if (STATE_NULL == getCurrentState()) {// if current state is null, the add it
                getLocationOnScreen(mLocation);
                if (mLocation[1] > 0f) {
                    for (SnowPayload payload : new ArrayList<>(mSnowPayloads)) {
                        final SnowSprite sprite = payload.mSprite;
                        float radius = sprite.getWidth() * sprite.getScaleX() / 2.0f;
                        sprite.getLocationOnScreen(mChildLocation);
                        float offsetY = mChildLocation[1] - mLocation[1] + radius - payload.mEndy;
                        if (offsetY >= 0) {// add to top
                            mSnowPayloads.remove(payload);
                            float px = mChildLocation[0] - mLocation[0] + radius;
                            float py = mChildLocation[1] - mLocation[1] + radius;
                            addBallSpace(px, py, radius, sprite.getAlpha());
                            mSnowBalls.add(new SnowBall(px, py, radius, (int) (sprite.getAlpha() * 255)));
                            payload.mSprite.onControlByConnecter(SnowConnector.this, CONTROLL_STATE_ATTACHED);
                            invalidate();
                        }
                    }
                }
            }

            for (SnowBall ball : new ArrayList<>(mSnowBalls))
                ball.run();
        }

        @Override
        public void freeServant(IServant servant) {

        }

        @Override
        public void init() {

        }

        @Override
        public void recycle() {

        }

        @Override
        public void onTrimMemory(int level) {

        }
    }
}
