package cn.tcl.music.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import cn.tcl.music.R;

public final class SamplesDrumLoopFeaturesBar extends LinearLayout {

    private final static int DEFAULT_COLOR = 0xFFf77811;

    public interface OnPageSelectedListener{
        void onPageSelected(int index);
    }

    private OnPageSelectedListener mPageSelectedListener;
    private Rect mIndicatorRect = new Rect();

    private int mIndicatorSize = 4;
    private int mIndicatorColor = DEFAULT_COLOR;

    private Paint mIndicatorPaint = new Paint();

    private View mSelectedView;

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int index = 0;
            switch (v.getId()) {
            case R.id.btn_samples:
                index = 0;
                break;
            case R.id.btn_drump_loops:
                index = 1;
                break;
            }
            mSelectedView = v;
            final int childCount = getChildCount();
            for(int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                child.setSelected(false);
            }

            v.setSelected(true);

            mIndicatorRect.set(v.getLeft(), 0, v.getRight(), mIndicatorSize);
            invalidate();

            if (mPageSelectedListener != null)
                mPageSelectedListener.onPageSelected(index);
        }
    };

    public SamplesDrumLoopFeaturesBar(Context context) {
        super(context);
        init(context, null);
    }

    public SamplesDrumLoopFeaturesBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SamplesDrumLoopFeaturesBar(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SamplesDrumLoopFeaturesBar(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MoreDjFeaturesBar);
            mIndicatorColor = a.getColor(R.styleable.MoreDjFeaturesBar_indicatorColor, DEFAULT_COLOR);
            mIndicatorSize = a.getDimensionPixelSize(R.styleable.MoreDjFeaturesBar_indicatorSize, 4);
            setWillNotDraw(false);
            a.recycle();
        }
        mIndicatorPaint.setColor(mIndicatorColor);
    }

    public void setOnPageSelectedListener(OnPageSelectedListener pageSelectedListener) {
        mPageSelectedListener = pageSelectedListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mIndicatorRect.isEmpty() && mSelectedView != null) {
            mIndicatorRect.set(mSelectedView.getLeft(), 0, mSelectedView.getRight(), mIndicatorSize);
        }
        canvas.drawRect(mIndicatorRect, mIndicatorPaint);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        final int numChildren = getChildCount();
        View firstChild = getChildAt(0);
        mIndicatorRect.set(firstChild.getLeft(), 0, firstChild.getRight(), mIndicatorSize);
        for (int i = 0; i < numChildren; i ++) {
            getChildAt(i).setOnClickListener(mOnClickListener);
        }

        invalidate();
    }
}
