package com.android.systemui.recents.views;

import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.RecentsMemoryInfo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class MstClearAllView extends ImageView{

	private Paint mPaint = new Paint(); 
	private Path mPath = new Path();
	private RectF oval = new RectF(0, 0, 138, 138);  
	private float mSweepAngle = 0f;
	private boolean mIsRun = false;
	private boolean mIsClearing = false;
	private static final int MSG_SEND_ANIMENDLISTENER = 1;
	private float mTotalMemSize;
	private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEND_ANIMENDLISTENER:
					if(mOnAnimEndListener != null) {
						mOnAnimEndListener.onAnimEnd();
					}
                    break;
                default:
                    super.handleMessage(msg);
            }
            super.handleMessage(msg);
        }
	};
	private Runnable moveThread = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(mIsRun){
                try {  
                    Thread.sleep(20);  
                } catch (InterruptedException e) {  
                    // TODO Auto-generated catch block  
                    e.printStackTrace();  
                }
                if(mSweepAngle <= 0 || !mIsClearing) {
                	mIsClearing = false;
                	mSweepAngle += 5;
                } else {
                	mSweepAngle -= 5;
                }
                float usedPercent = 1 - RecentsMemoryInfo.getmem_unused(mContext, Recents.getSystemServices().getActivityManager()) / mTotalMemSize;
				if(mSweepAngle / 360 >= usedPercent && !mIsClearing) {
					mSweepAngle = usedPercent * 360;
					mIsRun = false;
					mHandler.sendEmptyMessage(MSG_SEND_ANIMENDLISTENER);
				}
                postInvalidate();  
            }  
		}
	};
	
    public interface OnAnimEndListener  {  
        void onAnimEnd(); 
    }
    private OnAnimEndListener mOnAnimEndListener;  
    public void setOnAnimEndListener(OnAnimEndListener onAnimEndListener)  {  
        this.mOnAnimEndListener = onAnimEndListener;  
    }
	
    public MstClearAllView(Context context) {
        this(context, null);
    }

    public MstClearAllView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MstClearAllView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MstClearAllView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mTotalMemSize = RecentsMemoryInfo.getmem_total();
    }
    
    public void setViewClipPercent(float percent) {
    	//Log.d("111111", "--setViewClipPercent = " + percent);
    	mSweepAngle = percent * 360;
    }
    
    public void startCircleAnim() {
    	if(!mIsRun) {
    		mIsRun = true;
    		mIsClearing = true;
    		new Thread(moveThread).start();
    	}
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	// TODO Auto-generated method stub
    	super.onDraw(canvas);
        canvas.save();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3.0f);
        mPaint.setColor(Color.parseColor("#66c378"));
        mPath.reset();
        mPath.arcTo(oval, -90, mSweepAngle);
        //canvas.clipRect(mRect);  
        //canvas.clipRect(mRect2, Region.Op.UNION);
        canvas.clipPath(mPath);
        //the circle radius is a half of the MstClearAllView's src drawable width mimus the StrokeWidth
        canvas.drawCircle(69, 69, 66, mPaint);  
        canvas.restore();  
    }
}
