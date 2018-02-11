package cn.tcl.music.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import cn.tcl.music.view.*;

public class SquareRelativeLayout extends RelativeLayout {

    private Drawable mFocusedDrawable;

    public SquareRelativeLayout(Context context) {
        super(context);
        init();
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs,
                                int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mFocusedDrawable = getResources().getDrawable(android.R.drawable.screen_background_light_transparent);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mFocusedDrawable.setBounds(0, 0, w, h);
    }

    @Override
    public boolean isInTouchMode() {
        if (getParent() instanceof cn.tcl.music.view.ContextMenuReyclerView)
            return ((cn.tcl.music.view.ContextMenuReyclerView) getParent()).isInTouchMode();
        return super.isInTouchMode();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (isFocused()) {
            mFocusedDrawable.draw(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        int sizeMax = Math.max(measuredWidth, measuredHeight);
        int sizeMeasureSpec = MeasureSpec.makeMeasureSpec(sizeMax, MeasureSpec.EXACTLY);
        super.onMeasure(sizeMeasureSpec, sizeMeasureSpec);
    }
}