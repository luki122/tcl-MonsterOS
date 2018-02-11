package com.android.mms.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.android.mms.R;

//tangyisen
public class ComposeMessageSendRemind extends View
{
    private Context mContext;
    
    private float mCircleRadius;
    
    private float mCircleDiameter;
    
    private float mCircleInterval;
    
    private Paint mPaint;
    
    private int mCircleColorOne;
    
    private int mCircleColorTwo;
    
    private int mCircleColorThree;
    
    private int count;
    
    public ComposeMessageSendRemind(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
        init(context);
    }
    
    public ComposeMessageSendRemind(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
    }
    
    public ComposeMessageSendRemind(Context context)
    {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }
    
    private void init(Context context)
    {
        mContext = context;
        mCircleDiameter = context.getResources().getDimension(R.dimen.message_send_progress_circle_diameter);
        mCircleRadius = mCircleDiameter / 2;
        mCircleInterval = context.getResources().getDimension(R.dimen.message_send_progress_circle_interval);
        mCircleColorOne = context.getResources().getColor(R.color.message_send_progress_circle_color_one);
        mCircleColorTwo = context.getResources().getColor(R.color.message_send_progress_circle_color_two);
        mCircleColorThree = context.getResources().getColor(R.color.message_send_progress_circle_color_three);
        mPaint = new Paint();
        // 消除锯齿
        mPaint.setAntiAlias(true);
        // setWillNotDraw(false);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = (int)mCircleDiameter;
        int width = (int)(3 * mCircleDiameter + 2 * mCircleInterval);
        setMeasuredDimension(width, height);
    }
    
    @Override
    protected void onDraw(Canvas canvas)
    {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        
        // 绘制动画
        int style = count % 3;
        if (0 == style)
        {
            mPaint.setColor(mCircleColorOne);
            canvas.drawCircle(mCircleRadius, mCircleRadius, mCircleRadius, mPaint);
            mPaint.setColor(mCircleColorTwo);
            canvas.drawCircle(3 * mCircleRadius + mCircleInterval, mCircleRadius, mCircleRadius, mPaint);
            mPaint.setColor(mCircleColorThree);
            canvas.drawCircle(mCircleRadius + 2 * (2 * mCircleRadius + mCircleInterval),
                mCircleRadius,
                mCircleRadius,
                mPaint);
        }
        else if (1 == style)
        {
            mPaint.setColor(mCircleColorThree);
            canvas.drawCircle(mCircleRadius, mCircleRadius, mCircleRadius, mPaint);
            mPaint.setColor(mCircleColorOne);
            canvas.drawCircle(3 * mCircleRadius + mCircleInterval, mCircleRadius, mCircleRadius, mPaint);
            mPaint.setColor(mCircleColorTwo);
            canvas.drawCircle(mCircleRadius + 2 * (2 * mCircleRadius + mCircleInterval),
                mCircleRadius,
                mCircleRadius,
                mPaint);
        }
        else
        {
            mPaint.setColor(mCircleColorTwo);
            canvas.drawCircle(mCircleRadius, mCircleRadius, mCircleRadius, mPaint);
            mPaint.setColor(mCircleColorThree);
            canvas.drawCircle(3 * mCircleRadius + mCircleInterval, mCircleRadius, mCircleRadius, mPaint);
            mPaint.setColor(mCircleColorOne);
            canvas.drawCircle(mCircleRadius + 2 * (2 * mCircleRadius + mCircleInterval),
                mCircleRadius,
                mCircleRadius,
                mPaint);
        }
        
        // 200ms循环播放
        postDelayed(new Runnable()
        {
            
            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                count++;
                postInvalidate();
            }
        }, 250);
    }
    
}
