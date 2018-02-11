package com.monster.launcher;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.monster.launcher.dynamic.IDynamicIcon;

import java.util.TimeZone;

/**
 * Created by antino on 16-6-21.
 */
public class CalendarDynamic implements IDynamicIcon {

    private Context mContext;
    private Time mCalendar;

    private int mColor;
    private Bitmap mBitmap;
    private int mTextSize;
    private Drawable mBackground;
    private Drawable mBackgroundUp;
    private boolean mAttached;
    private BubbleTextView mAllappView;
    private int mIconSize;


    private final Handler mHandler = new Handler();
    private BubbleTextView mView;
    public CalendarDynamic() {
    }



    public void run() {
        updateDynamicIcon();
        //mHandler.post(tickRunnable);
    }

    private Runnable tickRunnable = new Runnable() {
        public void run() {
           // updateDynamicIcon();
        }
    };

    @Override
    public boolean init(Context context, BubbleTextView bubbleTextView, ItemInfo info,boolean isAllapps) {

        final Resources r = context.getResources();
        mCalendar = new Time();
        mContext=context;
        if(isAllapps){
            mAllappView=bubbleTextView;
        }else {
            mView = bubbleTextView;
        }

        if(mView!=null&&mView.mIconSize!=0){
            mIconSize = mView.mIconSize;
        }else if (mAllappView!=null&&mAllappView.mIconSize!=0){
            mIconSize = mAllappView.mIconSize;
        }
        mColor = r.getColor(R.color.calendar_text);
        mBackground=r.getDrawable(R.drawable.dyn_calendar_bg);
        mBackgroundUp=r.getDrawable(R.drawable.dyn_calendar_up);
        mTextSize = (int) r.getDimension(R.dimen.calender_icon_text_size);
        run();
        registerReceiver();
        return true;
    }
    @Override
    public void onAttachedToWindow(boolean register) {
        if(register)
        registerReceiver();

        run();
    }

    @Override
    public void onDetachedFromWindow() {
        try {
            if (mAttached) {
                mContext.unregisterReceiver(mIntentReceiver);
                mAttached = false;
            }
        }catch (Exception e){
            Log.e("liuzuo","mIntentReceiver is unregistered");
        }
    }



    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        // mView.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean updateDynamicIcon() {
        onTimeChanged(false);
        return true;
    }

    @Override
    public void cleanupdateDynamicIcon() {
        onDetachedFromWindow();
      //  mHandler.removeCallbacks(tickRunnable);
    }

    private void updateContentDescription(Time time) {
        final int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR;
        String contentDescription = DateUtils.formatDateTime(mContext,
                time.toMillis(false), flags);
        mView.setContentDescription(contentDescription);
    }

    private void onTimeChanged(boolean isFromBroadcast) {
        mCalendar.setToNow();

        updateCalendarIcon(isFromBroadcast);
        updateContentDescription(mCalendar);
    }

    private void updateCalendarIcon(boolean isFromBroadcast) {
        if(mIconSize==0)
            return;
        mBitmap = Bitmap.createBitmap(mIconSize,mIconSize, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(mBitmap);
        int w = mBackground.getIntrinsicWidth();
        int h = mBackground.getIntrinsicHeight();
        int x = mIconSize / 2;
        int y = mIconSize / 2;

        if ((mIconSize < w )|| (mIconSize < h)) {
            float scale = Math.min((float) mIconSize / (float) w,
                    (float) mIconSize / (float) h);
            canvas.save();

            canvas.scale(scale, scale, x, y);
        }


        mBackground.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));

        mBackground.draw(canvas);
        canvas.restore();
        String dayString = String.valueOf(mCalendar.monthDay);
        //final float mDensity = mContext.getResources().getDisplayMetrics().density;

        Paint mDatePaint = new Paint();
        mDatePaint.setTypeface(Typeface.create("monster-normal",Typeface.NORMAL));
        mDatePaint.setTextSize(mTextSize);
        mDatePaint.setColor(mColor);
        mDatePaint.setAntiAlias(true);
        mDatePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        Rect rect = new Rect();
        mDatePaint.getTextBounds(dayString,0,dayString.length(),rect);
        int rectWidth = rect.right - rect.left;
        int rectHeight = rect.bottom - rect.top;
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight() ;
        int offsetX=0;
        if(mCalendar.monthDay%10==1){
            offsetX=4;
        }
        canvas.drawText(dayString,(width - rectWidth)/2-rect.left -offsetX,(height - rectHeight)/2 - rect.top,mDatePaint);
        if ((mIconSize < w )|| (mIconSize < h)) {
            float scale = Math.min((float) mIconSize / (float) w,
                    (float) mIconSize / (float) h);
            canvas.save();

            canvas.scale(scale, scale, x, y);
        }
        mBackgroundUp.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        mBackgroundUp.draw(canvas);
        canvas.restore();
        Bitmap scalebmp= Utilities.createIconBitmap(mBitmap,mContext);
        Log.d("liuzuo73","updateCalendarIcon="+dayString+"   mCalendar="+mCalendar.monthDay);
        if(mView!=null)
            mView.setIcon(new FastBitmapDrawable(scalebmp),mIconSize);
        if(mAllappView!=null)
            mAllappView.setIcon(new FastBitmapDrawable(scalebmp),mIconSize);
        mView.updateFolderIcon();
        if(isFromBroadcast) {
            AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
            createAddAnimation(mView, anim);
            anim.start();
        }
    }



    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("liuzuo73","calendar onReceive ");
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
            }

            updateDynamicIcon();

        }
    };
    private AnimatorSet createAddAnimation(View icon, AnimatorSet anim) {
        Resources resources = mContext.getResources();
        Animator iconAlpha = ObjectAnimator.ofFloat(icon, "alpha", 0f, 1f);
        iconAlpha.setDuration(R.integer.folder_import_icon_duration);
        iconAlpha.setStartDelay(resources.getInteger(R.integer.folder_import_icon_delay));
        iconAlpha.setInterpolator(new AccelerateInterpolator(1.5f));
        anim.play(iconAlpha);
        return anim;
    }
    private synchronized void registerReceiver(){
        if (!mAttached) {
            IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_DATE_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            if(mContext!=null) {
                mContext.registerReceiver(mIntentReceiver,filter);
            }
            mAttached = true;
            Log.d("liuzuo73","calendar registerReceiver");
            Log.e("liuzuo73","calendar registerReceiver="+this.hashCode()+" mView="+mView.hashCode());
        }


    }
}
