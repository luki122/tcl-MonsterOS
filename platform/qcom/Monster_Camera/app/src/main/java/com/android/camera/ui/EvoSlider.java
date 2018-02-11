package com.android.camera.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.android.camera.debug.Log;
import com.tct.camera.R;

/**
 * Created by sichao.hu on 8/21/15.
 */
public class EvoSlider extends View{

    private final int TOUCH_GAP;//The slider should show on the right of the finger_pointed location,  or left in case of not enough room on the screen , other than right on it , this GAP
    private final int SLIDER_LENGTH;
    private final float BUTTON_RADIUS;
    private final float DASH;
    private static final float MAX_FRACTION=1.0f;
    private static final float MIN_FRACTION=0.3f;
    private static float mFraction =MAX_FRACTION;
    private static final float ALPHA_GRADIENT_START=1;
    private static final float ALPHA_GRADIENT_END=0.5f;
    private static final int ALPHA_GRADIENT_DURATION=200;
    private static final int STAY_GRADIENT_DELAY=1000;

    private final ValueAnimator mGradientAnimation=new ValueAnimator();
    private boolean mNeedShowStroke=false;
    private Paint mDashPaint;
    private Paint mSolidPaint;

    private float mAlpha=1;

    private final Drawable mExposureIconDrawable;
    public EvoSlider(Context context ,AttributeSet attrs){
        super(context,attrs);
        TOUCH_GAP=(int)context.getResources().getDimension(R.dimen.evo_slider_gap);
        SLIDER_LENGTH=(int)context.getResources().getDimension(R.dimen.evo_slider_length);
        BUTTON_RADIUS=(int)context.getResources().getDimension(R.dimen.evo_slider_icon_radius);
        DASH=context.getResources().getDimension(R.dimen.evo_slider_stroke_dash);
        mExposureIconDrawable=context.getResources().getDrawable(R.drawable.ic_focus_exposure);
        this.setVisibility(View.GONE);
        initAnimation();
    }


    public EvoSlider(Context context) {
        super(context);
        TOUCH_GAP=(int)context.getResources().getDimension(R.dimen.evo_slider_gap);
        SLIDER_LENGTH=(int)context.getResources().getDimension(R.dimen.evo_slider_length);
        BUTTON_RADIUS=(int)context.getResources().getDimension(R.dimen.evo_slider_icon_radius);
        DASH=context.getResources().getDimension(R.dimen.evo_slider_stroke_dash);
        mExposureIconDrawable=context.getResources().getDrawable(R.drawable.ic_focus_exposure);
        this.setVisibility(View.GONE);
        initAnimation();
    }

    private void initAnimation(){
        mGradientAnimation.setFloatValues(ALPHA_GRADIENT_START,ALPHA_GRADIENT_END);
        mGradientAnimation.setDuration(ALPHA_GRADIENT_DURATION);
        mGradientAnimation.setStartDelay(STAY_GRADIENT_DELAY);
        mGradientAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAlpha = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        mGradientAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    public interface EVOChangedListener{
        public void onEVOChanged(int value);
    }


    EVOChangedListener mEVOListener;
    public void setEvoListener(EVOChangedListener listener){
        mEVOListener=listener;
    }


    private static final Log.Tag TAG=new Log.Tag("EvoSlider");
    private float getFraction(){
        return mFraction;
    }

    private float mStep=0;

    /**
     * Method to control slider indicator , meanwhile it will callback calculated step value from float coordinate
     * @param deltaX
     * @param deltaY
     */
    public void slideTo(float deltaX,float deltaY){
        mGradientAnimation.cancel();
        mAlpha=ALPHA_GRADIENT_START;
        float deltaStep=0;
        if(ORIENTATION_PORTRAIT==mOrientation){
            deltaStep=deltaY* getFraction();
        }else{
            deltaStep=deltaX* getFraction();
        }
        if(deltaStep==0){
            return;
        }
        float expectValue=mStep+deltaStep;
        if(expectValue> mUpperBoundForSlider ||expectValue< mLowerBoundForSlider){
            return;
        }
        mStep+=deltaStep;
        if(mEVOListener!=null){
            float realValue=(mStep-mOrigin)*((float)(mMaxCompensationSteps - mMinCompensationSteps)/(mUpperBoundForSlider - mLowerBoundForSlider));//float metric=(maxValue-minValue)/(maxStep-minStep)

            int totalSteps=(int)realValue;

            float tolerance = realValue - (float)totalSteps;
            if (tolerance >= 0.5) {//half of 1 integer step
                totalSteps += 1;
            }else if(tolerance<=-0.5){
                totalSteps-=1;
            }
            if(totalSteps> mMaxCompensationSteps){
                totalSteps= mMaxCompensationSteps;
            }
            if(totalSteps< mMinCompensationSteps){
                totalSteps= mMinCompensationSteps;
            }
            mEVOListener.onEVOChanged(totalSteps);
        }
        mNeedShowStroke = true;

        invalidate();
    }

    private int mUpperBoundForSlider =0;//the upper bound for UI of the slider
    private int mLowerBoundForSlider =0;// the lower bound for UI of the slider
    private int mMaxCompensationSteps =0;//max value for compensation
    private int mMinCompensationSteps =0;//min value for compensation
    private float mOrigin=0;//this value indicates for the initial coordinate ,mapped to ZERO exposure compensation, for the slider
    /**
     * Set the Max and Min exposure compensation to map the slider (compensation=EVO/step)
     * @param maxCompensation a positive integer number indicates for the max exposure compensation value
     * @param minCompensation a negative integer number indicates for the minimum exposure compensation value
     */
    public void setValueBound(int maxCompensation,int minCompensation){
        mUpperBoundForSlider =SLIDER_LENGTH;
        mLowerBoundForSlider =0;
        mMaxCompensationSteps =maxCompensation;
        mMinCompensationSteps =minCompensation;
        mOrigin=SLIDER_LENGTH*Math.abs(minCompensation)/(Math.abs(maxCompensation)+Math.abs(minCompensation));
    }


    public void resetSlider(int value){
        if(value==0) {
            mStep = mOrigin;
        }else{
            mStep=SLIDER_LENGTH*(value- mMinCompensationSteps)/(mMaxCompensationSteps - mMinCompensationSteps);
        }
        mNeedShowStroke=false;
        if(mEVOListener!=null){
            mEVOListener.onEVOChanged(value);
        }
    }

    public void resetSlider(){
        resetSlider(0);
    }

    private float mX;
    private float mY;

    /**
     * Set the finger_pointed coordinate to initialize the slider location
     * @param x
     * @param y
     */
    public synchronized void setCoord(float x,float y){
        mX=x;
        mY=y;
        invalidate();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(visibility==VISIBLE){
            mAlpha=ALPHA_GRADIENT_START;
            mGradientAnimation.start();
        }else{
            mNeedShowStroke=false;
        }
        invalidate();

    }

    private int mBoundRight=0;
    private int mBoundBottom=0;
    public synchronized void setBound(int right,int bottom){
        mBoundRight=right;
        mBoundBottom=bottom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getPointerCount()!=1){
            return false;
        }
        if(event.getAction()==MotionEvent.ACTION_MOVE){
            if(event.getHistorySize()==0){
                return true;
            }else{
                float startX=event.getHistoricalX(0);
                float endX=event.getX();
                float startY=event.getHistoricalY(0);
                float endY=event.getY();
                this.slideTo(endX-startX, startY-endY);//the top left of view coordinate is (0,0) , so once it's coming close to bottom , the coordinate value of Y is becoming larger
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    private Paint initPaint(boolean needDash){
        Paint paint =new Paint();
        paint.setColor(Color.WHITE);
        if(needDash) {
            DashPathEffect effect=new DashPathEffect(new float[]{DASH, DASH}, 1);
            paint.setPathEffect(effect);
        }
        paint.setStyle(Style.STROKE);

        paint.setStrokeWidth(1.0f);
        return paint;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.setAlpha(mAlpha);
        if(mDashPaint ==null||mSolidPaint==null){
            mDashPaint=initPaint(true);
            mSolidPaint=initPaint(false);
        }
        PointF start=getSliderStart();
        PointF end=getSliderEnd();
        PointF buttonCoord=getSliderButtonCoord();
//        Path path=new Path();
//        path.moveTo(start.x, start.y);
//
//        path.lineTo(end.x, end.y);
        Path dashPath=new Path();
        dashPath.moveTo(start.x,start.y);
        dashPath.lineTo(buttonCoord.x, buttonCoord.y);
        Path solidPath=new Path();
        solidPath.moveTo(end.x, end.y);
        solidPath.lineTo(buttonCoord.x,buttonCoord.y);
        if(mNeedShowStroke) {
            canvas.drawPath(solidPath,mSolidPaint);
            canvas.drawPath(dashPath, mDashPaint);
        }
        Rect bound=new Rect((int)(buttonCoord.x-BUTTON_RADIUS),
                (int)(buttonCoord.y-BUTTON_RADIUS),
                (int)(buttonCoord.x+BUTTON_RADIUS),
                (int)(buttonCoord.y+BUTTON_RADIUS));
        mExposureIconDrawable.setBounds(bound);
        mExposureIconDrawable.draw(canvas);

    }

    private static final int ORIENTATION_PORTRAIT=0;
    private static final int ORIENTATION_LANDSCAPE=1;
    private int mOrientation=ORIENTATION_PORTRAIT;
    private PointF getSliderCenterCoord(){//Only used in onDraw , which is already synchronized under this class
        PointF p=new PointF();
        if(ORIENTATION_PORTRAIT==mOrientation){
            if(mX+TOUCH_GAP>mBoundRight){
                p.x=mX-TOUCH_GAP;
            }else{
                p.x=mX+TOUCH_GAP;
            }
            p.y=(int)mY;
        }else{
            if(mY+TOUCH_GAP>mBoundBottom){
                p.y=mY+TOUCH_GAP;
            }else{
                p.y=mY-TOUCH_GAP;
            }
            p.x=mX;
        }
        return p;
    }

    private PointF getSliderStart(){
        PointF center=getSliderCenterCoord();
        PointF startPoint=new PointF();
        if(ORIENTATION_PORTRAIT==mOrientation){
            startPoint.y=center.y-SLIDER_LENGTH/2;
            startPoint.x=center.x;
        }else{
            startPoint.x=center.x+SLIDER_LENGTH/2;
            startPoint.y=center.y;
        }
        return startPoint;
    }




    private PointF getSliderEnd(){
        PointF center=getSliderCenterCoord();
        PointF endPoint=new PointF();
        if(ORIENTATION_PORTRAIT==mOrientation){
            endPoint.y=center.y+SLIDER_LENGTH/2;
            endPoint.x=center.x;
        }else{
            endPoint.x=center.x-SLIDER_LENGTH/2;
            endPoint.y=center.y;
        }
        return endPoint;
    }

    private PointF getSliderButtonCoord(){
        PointF end=getSliderEnd();
        PointF buttonCoord=new PointF();
        if(ORIENTATION_PORTRAIT==mOrientation){
            buttonCoord.x=end.x;
            buttonCoord.y=end.y-mStep;
        }else{
            buttonCoord.y=end.y;
            buttonCoord.x=end.x-mStep;
        }
        return buttonCoord;
    }

}
