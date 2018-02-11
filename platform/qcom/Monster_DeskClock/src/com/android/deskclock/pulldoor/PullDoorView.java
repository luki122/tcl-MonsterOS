package com.android.deskclock.pulldoor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class PullDoorView extends RelativeLayout {//向上推门效果

    private Context mContext;
    private Context mActivity;
    private Scroller mScroller;

    private int mScreenWidth = 0;

    private int mScreenHeigh = 0;

    private int mLastDownY = 0;

    private int mCurryY;

    private int mDelY;

    public boolean mCloseFlag = false;
    
    private PullDoorCallback callBack;
    
    public void setCallBack(PullDoorCallback callback){
        callBack = callback;
    }

    // 欢迎页的背景，通过bg_index来进行选择
    //int[] mBackgroundIndex = { R.drawable.bg1, R.drawable.liuyifei1, R.drawable.liuyifei2 };

    public PullDoorView(Context context) {
        super(context);
        mContext = context;
        setupView();
    }

    public PullDoorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
//        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ToolBar);
//        bg_index = typedArray.getInt(R.styleable.ToolBar_bgIndex, 0);
        setupView();
    }

    @SuppressLint("NewApi")
    private void setupView() {
        // 这个Interpolator你可以设置别的 我这里选择的是有弹跳效果的Interpolator
        Interpolator polator = new BounceInterpolator();
        mScroller = new Scroller(mContext, polator);
        // 获取屏幕分辨率
        WindowManager wm = (WindowManager) (mContext.getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        mScreenHeigh = dm.heightPixels;
        mScreenWidth = dm.widthPixels;
        // 这里你一定要设置成透明背景,不然会影响你看到底层布局
        this.setBackgroundColor(Color.argb(0, 0, 0, 0));
//        mImgView = new ImageView(mContext);
//        mImgView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//        mImgView.setScaleType(ImageView.ScaleType.CENTER_CROP);// 填充整个屏幕
        //mImgView.setImageResource(mBackgroundIndex[bg_index]); // 选择背景
        //addView(mImgView);
    }

    // 设置推动门背景
    public void setBgImage(int id) {
//        mImgView.setImageResource(id);
    }

    // 设置推动门背景
    public void setBgImage(Drawable drawable) {
//        mImgView.setImageDrawable(drawable);
    }

    // 推动门的动画
    public void startBounceAnim(int startY, int dy, int duration) {
        mScroller.startScroll(0, startY, 0, dy, duration);
        invalidate();
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mLastDownY = (int) event.getY();
            return true;
        case MotionEvent.ACTION_MOVE:
            mCurryY = (int) event.getY();
            mDelY = mCurryY - mLastDownY;
            if (mDelY < 0) {// 上滑有效,把当前View向上滑动
                scrollTo(0, -mDelY);
            } else {// 如果向下画的距离超过了1/3,把上一个页面弄出来
                if (this.getScrollY() == 0 && mDelY > mScreenHeigh / 3) {
                }
            }
            break;
        case MotionEvent.ACTION_UP:
            mCurryY = (int) event.getY();
            mDelY = mCurryY - mLastDownY;
            if (mDelY < 0) {
                if (Math.abs(mDelY) > mScreenHeigh / 3) {
                    // 向上滑动超过半个屏幕高的时候 开启向上消失动画
                    startBounceAnim(this.getScrollY(), mScreenHeigh, 450);
                    mCloseFlag = true;// 提示需要关闭此页面
                } else {
                    // 向上滑动未超过半个屏幕高的时候 开启向下弹动动画
                    startBounceAnim(this.getScrollY(), -this.getScrollY(), 1000);
                }
            } else {// 修正误差，原作者没有这句，测试时发现，在慢慢放下来的时候，会有无法覆盖全屏幕的情况，看起来像是对底部做了Marging，需要修正一下
                startBounceAnim(this.getScrollY(), -this.getScrollY(), 450);
            }

            break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {

        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            // 不要忘记更新界面
            postInvalidate();
        } else {
            if (mCloseFlag) {// 关闭此页面，
                this.setVisibility(View.GONE);
                if(callBack != null){
                    callBack.finishIncreaseAnim();
                }
            }
        }
    }

}