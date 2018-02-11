/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.fasttransfer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import cn.tcl.transfer.R;

public class ConnectBackground extends RelativeLayout{

    private int pColor;
    private float pRadius;
    private int pDurationTime;
    private int startX = 0;
    private int startY = 0;
    private int endX = 0;
    private int endY = 0;
    private Context mContext;
    private Paint paint;
    private boolean animationRunning=false;
    private AnimatorSet animatorSet;
    private Animator.AnimatorListener lastAnimatorListener;
    private ArrayList<Animator> animatorList=new ArrayList<Animator>();
    private ArrayList<RippleView> rippleViewList=new ArrayList<RippleView>();
    private static final int DEFAULT_DURATION_TIME = 300;
    private static final int POINT_NUMBER = 12;
    public ConnectBackground(Context context) {
        super(context);
        mContext = context;
    }

    public ConnectBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        if (null == attrs) {
            throw new IllegalArgumentException("Attributes should be provided to this view,");
        }
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ConnectBackground);
        pColor=typedArray.getColor(R.styleable.ConnectBackground_p_color, getResources().getColor(R.color.connectlColor));
        pRadius=typedArray.getDimension(R.styleable.ConnectBackground_p_radius,getResources().getDimension(R.dimen.pointRadius));
        pDurationTime=typedArray.getInt(R.styleable.ConnectBackground_p_duration,DEFAULT_DURATION_TIME);
        typedArray.recycle();
        init(context);
    }

    public ConnectBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (null == attrs) {
            throw new IllegalArgumentException("Attributes should be provided to this view,");
        }
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ConnectBackground);
        pColor=typedArray.getColor(R.styleable.ConnectBackground_p_color, getResources().getColor(R.color.connectlColor));
        pRadius=typedArray.getDimension(R.styleable.ConnectBackground_p_radius,getResources().getDimension(R.dimen.pointRadius));
        pDurationTime=typedArray.getInt(R.styleable.ConnectBackground_p_duration,DEFAULT_DURATION_TIME);
        typedArray.recycle();
        init(context);
    }

    private void init(final Context context) {
        if (isInEditMode())
            return;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(pColor);

        animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorList.clear();
        rippleViewList.clear();
        removeAllViews();
        //setCenter(context);

        for(int i=0;i<POINT_NUMBER;i++){
            LayoutParams params=new LayoutParams((int)(i+pRadius),(int)(i+pRadius));
            RippleView rippleView=new RippleView(getContext());
            rippleView.setLayoutParams(params);
            setLayout(rippleView,startX+(int)((float)i*(float)(endX - startX)/(float)POINT_NUMBER),startY+(int)((float)i*(float)(endY - startY)/(float)POINT_NUMBER));
            addView(rippleView);
            rippleViewList.add(rippleView);
            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(rippleView, "Alpha", 0f, 1.0f);
            //alphaAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            alphaAnimator.setRepeatMode(ObjectAnimator.RESTART);
            alphaAnimator.setDuration(pDurationTime);
            animatorList.add(alphaAnimator);
            if (i == POINT_NUMBER -1) {
                lastAnimatorListener = new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        for(RippleView rippleView:rippleViewList){
                            rippleView.setAlpha(0);
                        }
                        if (animationRunning == true) {
                            animatorSet.start();
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                };
            }
        }
        animatorSet.playSequentially(animatorList);
        animatorSet.addListener(lastAnimatorListener);
    }

    public void setLayout(View view, int x, int y)
    {
        MarginLayoutParams margin=new MarginLayoutParams(view.getLayoutParams());
        margin.setMargins(x,y, x+margin.width, y+margin.height);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
        view.setLayoutParams(layoutParams);
    }

    public void setCenter(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Rect frame = new Rect();
        getWindowVisibleDisplayFrame(frame);
        int stateHeight = frame.top;
        endX = width / 2;
        endY = (height - stateHeight) / 2;
    }

    public void setStart(Context context,int x,int y) {
        startX = x;
        startY = y;
        init(context);
    }

    public void setEnd(Context context,int x,int y) {
        endX = x;
        endY = y;
        init(context);
    }

    private class RippleView extends View{

        public RippleView(Context context) {
            super(context);
            this.setVisibility(View.INVISIBLE);
            this.setAlpha(0);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int radius=(Math.min(getWidth(),getHeight()))/2;
            canvas.drawCircle(radius,radius,radius,paint);
        }
    }

    public void startRippleAnimation(){
        if(!isRippleAnimationRunning()){
            for(RippleView rippleView:rippleViewList){
                rippleView.setVisibility(VISIBLE);
            }
            animatorSet.start();
            animationRunning=true;
        }
    }

    public void stopRippleAnimation(){
        if(isRippleAnimationRunning()){
            animationRunning=false;
            animatorSet.end();
            for(RippleView rippleView:rippleViewList){
                rippleView.setVisibility(INVISIBLE);
            }
        }
    }

    public boolean isRippleAnimationRunning(){
        return animationRunning;
    }
}
