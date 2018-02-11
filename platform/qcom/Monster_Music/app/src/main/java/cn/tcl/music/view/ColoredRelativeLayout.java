package cn.tcl.music.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import cn.tcl.music.R;
import cn.tcl.music.util.LogUtil;

public class ColoredRelativeLayout extends HorizontalScrollView {

    private TextView mTextViewDelete;

    private int mScrollWidth;
    private Boolean mIsOpen = false;
    private Boolean mOnce = false;
    private IonSlidingButtonListener mIonSlidingButtonListener;

    public interface IonSlidingButtonListener {
        void onMenuIsOpen(View view);

        void onDownOrMove(ColoredRelativeLayout slidingButtonView);
    }



    private final static int DEFAULT_COLOR = Color.TRANSPARENT;

    private int mColor = DEFAULT_COLOR;

    public ColoredRelativeLayout(Context context) {
        super(context);
    }

    public ColoredRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColoredRelativeLayout(Context context, AttributeSet attrs,
                                 int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
        this.setOverScrollMode(OVER_SCROLL_NEVER);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ColoredRelativeLayout(Context context, AttributeSet attrs,
                                 int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
        this.setOverScrollMode(OVER_SCROLL_NEVER);
    }

    public void setSlidingButtonListener(IonSlidingButtonListener listener) {
        mIonSlidingButtonListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!mOnce) {
            mTextViewDelete = (TextView) findViewById(R.id.tv_delete);
            mOnce = true;
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        for (int i = 0; i < mScrollWidth; i++) {
            mTextViewDelete.setTranslationX(i - mScrollWidth);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (mIonSlidingButtonListener == null) {
                    return false;
                }
                mIonSlidingButtonListener.onDownOrMove(this);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                changeScrollx();
                return true;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    public void openMenu() {
        if (mIsOpen) {
            return;
        }
        this.smoothScrollTo(mScrollWidth, 0);
        mIsOpen = true;
        mIonSlidingButtonListener.onMenuIsOpen(this);
    }

    public void changeScrollx() {
        if (getScrollX() >= (mScrollWidth / 2)) {
            this.smoothScrollTo(mScrollWidth, 0);
            mIsOpen = true;
            mIonSlidingButtonListener.onMenuIsOpen(this);
        } else {
            this.smoothScrollTo(0, 0);
            mIsOpen = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            this.scrollTo(0, 0);
            mScrollWidth = mTextViewDelete.getWidth();
        }
    }

    public void closeMenu() {
        if (!mIsOpen) {
            return;
        }
        this.smoothScrollTo(0, 0);
        mIsOpen = false;
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColoredRelativeLayout);
        mColor = a.getColor(R.styleable.ColoredRelativeLayout_colorBackground, DEFAULT_COLOR);
        setWillNotDraw(false);
        a.recycle();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(mColor);
        try {
            super.draw(canvas);
        } catch (Exception e) {
            LogUtil.e(ColoredRelativeLayout.class.getSimpleName(), "Exception : " + e.getMessage());
        }
    }

    public void setColorBackground(int color) {
        mColor = color;
        invalidate();
    }
}
