package com.android.deskclock.view;

import com.android.deskclock.Utils;
import com.android.deskclock.pulldoor.PullDoorCallback;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MyHalfCircleViewNew extends View {

    private Context m_context;
    public MyHalfCircleViewNew(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        m_context = context;
        init();
    }
    
    public MyHalfCircleViewNew(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        m_context = context;
        init();
    }

    public MyHalfCircleViewNew(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_context = context;
        init();
    }

    public MyHalfCircleViewNew(Context context) {
        super(context);
        m_context = context;
        init();
    }

    
    private Paint mPaint ;
    
    private int off_set_height;//滑动高度的偏移量
    
    private int init_bt_white_height;//底部白色矩形初始化高度
    private int bt_white_height;//底部白色矩形高度
    private int init_arc_height;//弧度的初始化高度
    private int max_arc_height_off_set ;//弧度最大增加的高度
    
    private int  max_height_left;//除去白矩形和弧度剩下的最大的高度
    
    private PullDoorCallback my_back;//动画完成的回调
    
    private boolean isSnoozeAnim = false;//摇一摇后的关闭动画

    public void init(){
        mPaint = new Paint();
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        init_bt_white_height = bt_white_height = Utils.dp2px(m_context, 200);
        init_arc_height  = Utils.dp2px(m_context, 80);
        max_arc_height_off_set =  Utils.dp2px(m_context, 50);
        
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int with = getWidth();
        int height = getHeight();
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(Color.WHITE);
        
        int arc_height = 0;

        
        if(!isSnoozeAnim){
            float progress = off_set_height*1.0f/(height - init_bt_white_height );
            if(progress<1){
                int progress_int = (int)(255*(1.0f-progress));
                mPaint.setAlpha(progress_int);
            } else {
                mPaint.setAlpha(0);
            }
            
            max_height_left = height - init_bt_white_height ;
            
            
            if(off_set_height>=max_arc_height_off_set){//当偏移量超过弧度的最大偏移量的时候
                bt_white_height = init_bt_white_height+off_set_height-max_arc_height_off_set;
                int arc_max_height = init_arc_height+max_arc_height_off_set;
//                arc_height = arc_max_height-arc_max_height*(off_set_height-arc_max_height)/max_height_left;
                arc_height = arc_max_height-(off_set_height - max_arc_height_off_set)*arc_max_height/(max_height_left-max_arc_height_off_set);
                
            } else {
                bt_white_height = init_bt_white_height;
                arc_height = init_arc_height+off_set_height;
            }
        } else {
            
            float progress = off_set_height*1.0f/(init_bt_white_height+init_arc_height );
            if(progress<1){
                int progress_int = (int)(255*(1.0f-progress));
                mPaint.setAlpha(progress_int);
            } else {
                mPaint.setAlpha(0);
            }

            
            if(off_set_height<=init_arc_height){
                arc_height = init_arc_height - off_set_height;
                bt_white_height = init_bt_white_height;
            } else {
                arc_height = 0;
                bt_white_height = init_bt_white_height +init_arc_height - off_set_height;
            }
        }

        
        int white_start_x = 0;
        int white_start_y = height-bt_white_height;
        int white_end_x = with;
        int white_end_y = height;
        
        canvas.drawRect(white_start_x,white_start_y,white_end_x,white_end_y,mPaint);//画底部的白色矩形
        
        Path path = new Path();
        path.moveTo(white_start_x,white_start_y);//起点
        
        
        int x1 = with/2;
        int y1 = white_start_y - arc_height;
        
        int x3 = with;
        int y3 = white_start_y;
        
        path.quadTo(x1, y1, x3, y3);
        
        canvas.drawPath(path,mPaint);
        
//        mPaint.setColor(Color.BLUE);
//        canvas.drawCircle(x1, y1, 5, mPaint);
//        Log.i("zouxu", "progress="+progress);
    }
    
    
    

    float down_y;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            down_y = event.getY();
            break;
        case MotionEvent.ACTION_MOVE:
            
            float move_y = event.getY();
            
            float off_set_y = move_y -down_y;
            if(off_set_y<0){
                off_set_height = -(int)off_set_y;
            } else {
                off_set_height = 0;
            }
            if(my_back !=null){
                
                float progress = off_set_height*1.0f/(getHeight() - init_bt_white_height );
                
                my_back.onMoveing(off_set_height,progress);
            }
            invalidate();
            break;
        case MotionEvent.ACTION_UP:
            actionUp();
            break;

        default:
            break;
        }
        
        return true;
    }
    
    public void actionUp(){
        final int last_off_set_height = off_set_height;
        if(off_set_height>max_height_left*2/5){
            int max_left = getHeight() - init_bt_white_height ;
            final  int left_height =Math.abs(max_left - off_set_height);
             ObjectAnimator anim = ObjectAnimator.ofFloat(this, "zx", 0.0F, 1.0F).setDuration((int)(500*left_height/max_left));
             anim.addUpdateListener(new AnimatorUpdateListener() {
                 @Override
                 public void onAnimationUpdate(ValueAnimator animation) {
                     float cVal = (Float) animation.getAnimatedValue();
                     off_set_height =(int)(last_off_set_height+left_height*cVal);
                     if(my_back !=null){
                         float progress = off_set_height*1.0f/(getHeight() - init_bt_white_height );
                         my_back.onMoveing(off_set_height,progress);
                     }
                     invalidate();
                 }
             });
             anim.addListener(new AnimatorListenerAdapter() {
                 @Override
                 public void onAnimationEnd(Animator animation) {
                     super.onAnimationEnd(animation);
                     if(my_back != null){
                         my_back.finishIncreaseAnim();
                     }
                 }
             });
             anim.start();

         } else {
             ObjectAnimator anim = ObjectAnimator.ofFloat(this, "zx", 1.0F, 0.0F).setDuration(500);
             anim.addUpdateListener(new AnimatorUpdateListener() {
                 @Override
                 public void onAnimationUpdate(ValueAnimator animation) {
                     float cVal = (Float) animation.getAnimatedValue();
                     off_set_height = (int)(last_off_set_height*cVal);
                     invalidate();
                     if(my_back !=null){
                         float progress = off_set_height*1.0f/(getHeight() - init_bt_white_height );
                         my_back.onMoveing(off_set_height,progress);
                     }
                 }
             });
             anim.addListener(new AnimatorListenerAdapter() {
                 @Override
                 public void onAnimationEnd(Animator animation) {
                     super.onAnimationEnd(animation);
                     if(my_back != null){
                         my_back.finishDecreaseAnim();
                     }
                 }
             });
             anim.start();
         }

    }
    
    public void startSnoozeAnim(){
        isSnoozeAnim = true;
        
        final int max_off_set_height = init_bt_white_height+init_arc_height;
        
        ObjectAnimator anim = ObjectAnimator.ofFloat(this, "zx", 0.0f, 1.0F).setDuration(500);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();
                off_set_height = (int)(max_off_set_height*cVal);
                invalidate();
                if(my_back !=null){
                    my_back.onMoveing(-off_set_height,cVal);
                }
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(my_back != null){
                    my_back.finishSnoozeAnim();
                }
            }
        });
        anim.start();

        
    }
    
    public void setCallBack(PullDoorCallback mCallBack){
        my_back = mCallBack;
    }

}
