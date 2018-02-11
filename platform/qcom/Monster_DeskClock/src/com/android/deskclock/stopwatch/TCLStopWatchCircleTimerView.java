package com.android.deskclock.stopwatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.android.deskclock.R;
import com.android.deskclock.Utils;
import com.android.deskclock.stopwatch.Stopwatches;

/**
 * Class to draw a circle for timers and stopwatches.
 * These two usages require two different animation modes:
 * Timer counts down. In this mode the animation is counter-clockwise and stops at 0.
 * Stopwatch counts up. In this mode the animation is clockwise and will run until stopped.
 */
public class TCLStopWatchCircleTimerView extends View {

    private int mAccentColor;
    private int mWhiteColor;
    private int centerWhite ;
    private long mIntervalTime = 0;
    private long mIntervalStartTime = -1;
    private long mMarkerTime = -1;
    private long mCurrentIntervalTime = 0;
    private long mAccumulatedTime = 0;
    private boolean mPaused = false;
    private boolean mAnimate = false;
    private static float mStrokeSize = 2;
    private float mRedStrokeSize = 1;
    private static float mDotRadius = 6;
    private static float mMarkerStrokeSize = 2;
    private final Paint mPaint = new Paint();
    private final Paint mFill = new Paint();
    private final RectF mArcRect = new RectF();
    private float mRadiusOffset;   // amount to remove from radius to account for markers on circle
    private float mScreenDensity;
    
    private float redDotPading = 4;

    // Stopwatch mode is the default.
    private boolean mTimerMode = false;
    
    private int[] doughnutColors = { Color.parseColor("#229f5555"), Color.parseColor("#ff9f5555")}; 
    
    @SuppressWarnings("unused")
    public TCLStopWatchCircleTimerView(Context context) {
        this(context, null);
    }

    public TCLStopWatchCircleTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setIntervalTime(long t) {
        mIntervalTime = t;
        postInvalidate();
    }

    public void setMarkerTime(long t) {
        mMarkerTime = t;
        //postInvalidate(); //delete zouxu
    }

    public void reset() {
        mIntervalStartTime = -1;
        mMarkerTime = -1;
        startAngle = 0;//add zouxu
        arc_angle = 0;
        //Log.i("stopwatch", "reset  startAngle 0 ");
        postInvalidate();
    }
    public void startIntervalAnimation() {
        mIntervalStartTime = Utils.getTimeNow();
        mAnimate = true;
        invalidate();
        mPaused = false;
    }
    public void stopIntervalAnimation() {
        mAnimate = false;
        mIntervalStartTime = -1;
        mAccumulatedTime = 0;
    }

    public boolean isAnimating() {
        return (mIntervalStartTime != -1);
    }

    public void pauseIntervalAnimation() {
        mAnimate = false;
        mAccumulatedTime += Utils.getTimeNow() - mIntervalStartTime;
        mPaused = true;
    }

    public void abortIntervalAnimation() {
        mAnimate = false;
    }

    public void setPassedTime(long time, boolean drawRed) {
        // The onDraw() method checks if mIntervalStartTime has been set before drawing any red.
        // Without drawRed, mIntervalStartTime should not be set here at all, and would remain at -1
        // when the state is reconfigured after exiting and re-entering the application.
        // If the timer is currently running, this drawRed will not be set, and will have no effect
        // because mIntervalStartTime will be set when the thread next runs.
        // When the timer is not running, mIntervalStartTime will not be set upon loading the state,
        // and no red will be drawn, so drawRed is used to force onDraw() to draw the red portion,
        // despite the timer not running.
        mCurrentIntervalTime = mAccumulatedTime = time;
        if (drawRed) {
            mIntervalStartTime = Utils.getTimeNow();
        }
        postInvalidate();
    }



    private void init(Context c) {

        Resources resources = c.getResources();
        mStrokeSize = resources.getDimension(R.dimen.circletimer_gray_circle_size);
        mRedStrokeSize = resources.getDimension(R.dimen.circletimer_circle_size);
        redDotPading = resources.getDimension(R.dimen.dotPading);
        float dotDiameter = resources.getDimension(R.dimen.circletimer_dot_size);
        mMarkerStrokeSize = resources.getDimension(R.dimen.circletimer_marker_size);
        mRadiusOffset = Utils.calculateRadiusOffset(
                mStrokeSize, dotDiameter, mMarkerStrokeSize);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mWhiteColor = resources.getColor(R.color.clock_white);
        centerWhite = resources.getColor(R.color.white);
//        mAccentColor = Utils.obtainStyledColor(c, R.attr.colorAccent, Color.RED); chg zouxu 20160829
        mAccentColor = resources.getColor(R.color.clock_red);
        mScreenDensity = resources.getDisplayMetrics().density;
        mFill.setAntiAlias(true);
        mFill.setStyle(Paint.Style.FILL);
        mFill.setColor(mAccentColor);
        mDotRadius = dotDiameter / 2f;
    }

    public void setTimerMode(boolean mode) {
        mTimerMode = mode;
    }

     long startAngle = 0;//开始旋转的角度 原生是270也就是垂直向上12点方向 add zouxu
     long arc_angle = 0;//弧度的角度
    
    @Override
    public void onDraw(Canvas canvas) {
        int xCenter = getWidth() / 2 + 1;
        int yCenter = getHeight() / 2;
        
        float radius = Math.min(xCenter, yCenter) - mRadiusOffset;
        
        mArcRect.top = yCenter - radius;
        mArcRect.bottom = yCenter + radius;
        mArcRect.left =  xCenter - radius;
        mArcRect.right = xCenter + radius;

        
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(centerWhite);
        canvas.drawCircle (xCenter, yCenter, radius, mPaint);//画中间白色圆
        
        
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeSize);

        if (mIntervalStartTime == -1 && !mAnimate) {
            // just draw a complete white circle, no red arc needed
            mPaint.setColor(mWhiteColor);
            canvas.drawCircle (xCenter, yCenter, radius, mPaint);
            if (mTimerMode) {
                drawRedDot(canvas, 0f, xCenter, yCenter, radius);
            }
            if(arc_angle>0){//add zouxu
                drawArc(canvas);
                float redPercent = arc_angle/360.0f;
                drawRedDot(canvas, redPercent, xCenter, yCenter, radius);
            } else {
                drawRedDot(canvas, 0f, xCenter, yCenter, radius);//开始默认的红色圆点 add zouxu
            }
        } else {
            if (mAnimate) {
                mCurrentIntervalTime = Utils.getTimeNow() - mIntervalStartTime + mAccumulatedTime;
            }
            //draw a combination of red and white arcs to create a circle
            
            /**
            float redPercent = (float)mCurrentIntervalTime / (float)mIntervalTime;
            // prevent timer from doing more than one full circle
            redPercent = (redPercent > 1 && mTimerMode) ? 1 : redPercent;

            
            arc_angle =arc_angle+5;
            
            if(arc_angle>=270){
                arc_angle = 270;
                startAngle = startAngle+5;
            }
            redPercent = arc_angle/360.0f;
            
            float whitePercent = 1 - (redPercent > 1 ? 1 : redPercent);
            // draw red arc here
            mPaint.setColor(mAccentColor);
            mPaint.setStrokeWidth(mRedStrokeSize);
            if (mTimerMode){
//                mPaint.setColor(mWhiteColor);//add zouxu 20160826 exchange
                canvas.drawArc (mArcRect, startAngle, - redPercent * 360 , false, mPaint);
            } else {
//                canvas.drawArc (mArcRect, startAngle, + redPercent * 360 , false, mPaint);
                canvas.drawArc (mArcRect, startAngle, arc_angle , false, mPaint);
            }
            
            // draw white arc here
            mPaint.setStrokeWidth(mStrokeSize);
            mPaint.setColor(mWhiteColor);
            if (mTimerMode) {
//                mPaint.setColor(mAccentColor);//add zouxu 20160826 exchange
                canvas.drawArc(mArcRect, startAngle, + whitePercent * 360, false, mPaint);
            } else {
//                canvas.drawArc(mArcRect, startAngle + (1 - whitePercent) * 360,
//                        whitePercent * 360, false, mPaint);
                canvas.drawArc(mArcRect, startAngle +arc_angle,
                        360-arc_angle, false, mPaint);
            } **/

//            if (mMarkerTime != -1 && radius > 0 && mIntervalTime != 0) {
//                mPaint.setStrokeWidth(mMarkerStrokeSize);
////                float angle = (float)(mMarkerTime % mIntervalTime) / (float)mIntervalTime * 360;
//                // draw 2dips thick marker
//                // the formula to draw the marker 1 unit thick is:
//                // 180 / (radius * Math.PI)
//                // after that we have to scale it by the screen density
//                
//                canvas.drawArc (mArcRect, startAngle + angle, mScreenDensity *
//                        (float) (360 / (radius * Math.PI)) , false, mPaint);//delete zouxu  for test
//            }
            
            
            
            drawArc(canvas);
            float redPercent = arc_angle/360.0f;
            drawRedDot(canvas, redPercent, xCenter, yCenter, radius);
        }
        if (mAnimate) {
            postInvalidateOnAnimation();
        }
   }
    
    public void drawArc(Canvas canvas){
        float redPercent = (float)mCurrentIntervalTime / (float)mIntervalTime;
        // prevent timer from doing more than one full circle
        redPercent = (redPercent > 1 && mTimerMode) ? 1 : redPercent;
        
        arc_angle =arc_angle+5;
        
        if(arc_angle>=270){
            arc_angle = 270;
            startAngle = startAngle+5;
        }
        redPercent = arc_angle/360.0f;
        
        float whitePercent = 1 - (redPercent > 1 ? 1 : redPercent);
        // draw red arc here
        mPaint.setColor(mAccentColor);
        
        mPaint.setStrokeWidth(mRedStrokeSize);
        
        if (mTimerMode){
//            mPaint.setColor(mWhiteColor);//add zouxu 20160826 exchange
            canvas.drawArc (mArcRect, startAngle, - redPercent * 360 , false, mPaint);
        } else {
//            canvas.drawArc (mArcRect, startAngle, + redPercent * 360 , false, mPaint);
            int num = 10;
            for(int i=0;i<num;i++){
                float arc_angle_slpit = (float)arc_angle/num;
                float offset = arc_angle_slpit*i;
                mPaint.setStrokeWidth(mRedStrokeSize*(i+1)/num);
                canvas.drawArc (mArcRect, startAngle+offset, arc_angle_slpit , false, mPaint);
            }
        }
        
        // draw white arc here
        mPaint.setStrokeWidth(mStrokeSize);
        mPaint.setColor(mWhiteColor);
        if (mTimerMode) {
//            mPaint.setColor(mAccentColor);//add zouxu 20160826 exchange
            canvas.drawArc(mArcRect, startAngle, + whitePercent * 360, false, mPaint);
        } else {
//            canvas.drawArc(mArcRect, startAngle + (1 - whitePercent) * 360,
//                    whitePercent * 360, false, mPaint);
            canvas.drawArc(mArcRect, startAngle +arc_angle,
                    360-arc_angle, false, mPaint);
        }
        
    }

    protected void drawRedDot(
            Canvas canvas, float degrees, int xCenter, int yCenter, float radius) {
        mPaint.setColor(mAccentColor);
        float dotPercent;
        if (mTimerMode) {
            dotPercent = startAngle - degrees * 360;
        } else {
            dotPercent = startAngle + degrees * 360;
        }
        
        radius = radius - redDotPading;
        final double dotRadians = Math.toRadians(dotPercent);
        canvas.drawCircle(xCenter + (float) (radius * Math.cos(dotRadians)),
                yCenter + (float) (radius * Math.sin(dotRadians)), mDotRadius, mFill);
        
        //Log.i("stopwatch", "startAngle = "+startAngle+",arc_angle="+arc_angle);

    }

    public static final String PREF_CTV_PAUSED  = "_ctv_paused";
    public static final String PREF_CTV_INTERVAL  = "_ctv_interval";
    public static final String PREF_CTV_INTERVAL_START = "_ctv_interval_start";
    public static final String PREF_CTV_CURRENT_INTERVAL = "_ctv_current_interval";
    public static final String PREF_CTV_ACCUM_TIME = "_ctv_accum_time";
    public static final String PREF_CTV_TIMER_MODE = "_ctv_timer_mode";
    public static final String PREF_CTV_MARKER_TIME = "_ctv_marker_time";
    
    public static final String START_ANGLE = "start_angle";//add zouxu
    public static final String ARC_ANGLE = "arc_angle";//add zouxu
    public static final String IS_ANIM = "is_animate";//add zouxu

    // Since this view is used in multiple places, use the key to save different instances
    public void writeToSharedPref(SharedPreferences prefs, String key) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean (key + PREF_CTV_PAUSED, mPaused);
        editor.putLong (key + PREF_CTV_INTERVAL, mIntervalTime);
        editor.putLong (key + PREF_CTV_INTERVAL_START, mIntervalStartTime);
        editor.putLong (key + PREF_CTV_CURRENT_INTERVAL, mCurrentIntervalTime);
        editor.putLong (key + PREF_CTV_ACCUM_TIME, mAccumulatedTime);
        editor.putLong (key + PREF_CTV_MARKER_TIME, mMarkerTime);
        editor.putLong (key + START_ANGLE, startAngle);//add zouxu
        editor.putLong (key + ARC_ANGLE, arc_angle);//add zouxu
        editor.putBoolean (key + IS_ANIM, mAnimate);
        editor.putBoolean (key + PREF_CTV_TIMER_MODE, mTimerMode);
        //Log.i("stopwatch", "writeToSharedPref startAngle = "+startAngle);
        editor.apply();
    }

    public void readFromSharedPref(SharedPreferences prefs, String key) {
        mPaused = prefs.getBoolean(key + PREF_CTV_PAUSED, false);
        mIntervalTime = prefs.getLong(key + PREF_CTV_INTERVAL, 0);
        mIntervalStartTime = prefs.getLong(key + PREF_CTV_INTERVAL_START, -1);
        mCurrentIntervalTime = prefs.getLong(key + PREF_CTV_CURRENT_INTERVAL, 0);
        mAccumulatedTime = prefs.getLong(key + PREF_CTV_ACCUM_TIME, 0);
        mMarkerTime = prefs.getLong(key + PREF_CTV_MARKER_TIME, -1);
        mTimerMode = prefs.getBoolean(key + PREF_CTV_TIMER_MODE, false);
//        mAnimate = (mIntervalStartTime != -1 && !mPaused);

    }
    
    public void readStartAngleFormSharePre(SharedPreferences prefs, String key){//add zouxu
         startAngle = prefs.getLong(key + START_ANGLE, 0);
         arc_angle = prefs.getLong(key + ARC_ANGLE, 0);
         mAnimate = prefs.getBoolean(key + IS_ANIM, false);
         //Log.i("stopwatch", "readStartAngleFormSharePre startAngle="+startAngle);
    }

    public void clearSharedPref(SharedPreferences prefs, String key) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove (Stopwatches.PREF_START_TIME);
        editor.remove (Stopwatches.PREF_ACCUM_TIME);
        editor.remove (Stopwatches.PREF_STATE);
        editor.remove (key + PREF_CTV_PAUSED);
        editor.remove (key + PREF_CTV_INTERVAL);
        editor.remove (key + PREF_CTV_INTERVAL_START);
        editor.remove (key + PREF_CTV_CURRENT_INTERVAL);
        editor.remove (key + PREF_CTV_ACCUM_TIME);
        editor.remove (key + PREF_CTV_MARKER_TIME);
        editor.remove (key + PREF_CTV_TIMER_MODE);
        editor.remove (key + START_ANGLE);//add zouxu
        editor.remove (key + ARC_ANGLE);//add zouxu
        editor.apply();
    }
}
