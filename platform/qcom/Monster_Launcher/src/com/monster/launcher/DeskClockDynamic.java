package com.monster.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;

import com.monster.launcher.dynamic.DynamicIconFactory;
import com.monster.launcher.dynamic.IDynamicIcon;

import java.util.TimeZone;

/**
 * Created by antino on 16-6-21.
 */

public class DeskClockDynamic implements IDynamicIcon{
    private Context mContext;
    private Time mCalendar;

    private Drawable mHourHand;
    private Drawable mMinuteHand;
    private Drawable mSencondHand;
    private Drawable mDial;
    private Bitmap mClock;
    private int mIconSize;

    private boolean mAttached;

    private final Handler mHandler = new Handler();
    private float mMinutes;
    private float mHour;
    private float mSeconds;
    private boolean mChanged;
    private BubbleTextView mView;
    private BubbleTextView mAllappView;
    private boolean isFirst=true;
    public DeskClockDynamic() {
   }

  /*   public DeskClockDynamic(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeskClockDynamic(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final Resources r = context.getResources();
        mHourHand = r.getDrawable(R.drawable.clock_hand_hour);
        mMinuteHand = r.getDrawable(R.drawable.clock_hand_minute);
        mSencondHand = r.getDrawable(R.drawable.clock_hand_second);
        mDial = r.getDrawable(R.drawable.clock_dial);
        mCalendar = new Time();
        run();
    }*/

    public void run() {
        onTimeChanged();
       /* stopRunnable();
        mHandler.post(tickRunnable);*/
    }

    /*private Runnable tickRunnable = new Runnable() {
        public void run() {
       ItemInfo info  = (ItemInfo) mView.getTag();

            //postInvalidate();
                if(isFirst){
                    updateDynamicIcon();
                    isFirst = false;
                }else {
                    if (info instanceof ShortcutInfo) {
                        if(info.container < 0) {
                           int currentWorkspaceScreen = mView.mLauncher.getCurrentWorkspaceScreen();
                            if (info.screenId ==  mView.mLauncher.getWorkspace().getScreenIdForPageIndex(currentWorkspaceScreen))
                                updateDynamicIcon();
                        }else {
                        Folder folder  =  mView.mLauncher.getOpenFolder();
                            if(folder!=null&&folder.mInfo.id==info.container){
                                updateDynamicIcon();
                            }
                        }
                    } else if(info instanceof AppInfo){
                        if(Launcher.State.APPS==mView.mLauncher.mState)
                            updateDynamicIcon();
                    } else {

                        updateDynamicIcon();
                    }

                }
            mHandler.postDelayed(tickRunnable, 500);
        }
    };*/

    @Override
    public boolean init(Context context, BubbleTextView bubbleTextView, ItemInfo info,boolean isAllapps) {
        final Resources r = context.getResources();
        mHourHand = r.getDrawable(R.drawable.clock_hand_hour);
        mMinuteHand = r.getDrawable(R.drawable.clock_hand_minute);
        mSencondHand = r.getDrawable(R.drawable.clock_hand_second);
        mDial = r.getDrawable(R.drawable.clock_dial);
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
        updateDynamicIcon();
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
            stopRunnable();
        Log.e("liuzuo73","mIntentReceiver is unregistered");
    }catch (Exception e){
        Log.e("liuzuo","unregistered failed");
    }
    }

    private void stopRunnable(){
       /* mHandler.removeCallbacks(tickRunnable);*/
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
       // mView.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
    }

    @Override
    public boolean updateDynamicIcon() {
        onTimeChanged();
        return true;
    }

    @Override
    public void cleanupdateDynamicIcon() {
        onDetachedFromWindow();
        stopRunnable();
    }

    private void updateContentDescription(Time time) {
        final int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR;
        String contentDescription = DateUtils.formatDateTime(mContext,
                time.toMillis(false), flags);
        mView.setContentDescription(contentDescription);
    }

    private void onTimeChanged() {
        mCalendar.setToNow();
        Log.d("liuzuo73","onTimeChanged"+mCalendar.toString());

        int hour = mCalendar.hour;
        int minute = mCalendar.minute;
        //int second = mCalendar.second;
            mMinutes = minute /*+ mSeconds / 60.0f*/;
            mHour = hour + mMinutes / 60.0f;
            mSeconds = mCalendar.second;
            mChanged = true;
            updateClockIcon();
            updateContentDescription(mCalendar);
    }

    private void updateClockIcon() {
        mClock = Bitmap.createBitmap(mDial.getIntrinsicWidth(),mDial.getIntrinsicHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(mClock);
                boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }
        int availableWidth = mDial.getIntrinsicWidth();
        int availableHeight =mDial.getIntrinsicHeight() ;

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        final Drawable dial = mDial;
        int w = dial.getIntrinsicWidth();
        int h = dial.getIntrinsicHeight();

        boolean scaled = false;

        if (availableWidth < w || availableHeight < h) {
            scaled = true;
            float scale = Math.min((float) availableWidth / (float) w,
                    (float) availableHeight / (float) h);
            canvas.save();

            canvas.scale(scale, scale, x, y);
        }

        if (changed) {
            dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        dial.draw(canvas);

        canvas.save();
        canvas.rotate(mHour / 12.0f * 360.0f, x, y);
        final Drawable hourHand = mHourHand;
        if (changed) {
            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();
            hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        hourHand.draw(canvas);
        canvas.restore();

        canvas.save();
        canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);

        final Drawable minuteHand = mMinuteHand;
        if (changed) {
            w = minuteHand.getIntrinsicWidth();
            h = minuteHand.getIntrinsicHeight();
            minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        minuteHand.draw(canvas);
        canvas.restore();

        canvas.save();
        canvas.rotate(mSeconds / 60.0f * 360.0f, x, y);
        /*final Drawable secondHand = mSencondHand;
        if (changed) {
            w = secondHand.getIntrinsicWidth();
            h = secondHand.getIntrinsicHeight();
            secondHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        secondHand.draw(canvas);
        */
        canvas.restore();
        if (scaled) {
            canvas.restore();
        }
//        Matrix matrix=new Matrix();
//        float scaleX=(float)mIconSize/availableWidth;
//        float scaleY=(float)mIconSize/availableHeight;
//        matrix.postScale(scaleX,scaleY);
//        Bitmap scalebmp=Bitmap.createBitmap(mClock,0,0,availableWidth,availableHeight,matrix,true);
        Bitmap scalebmp=Utilities.createIconBitmap(mClock,mContext);
        if(mView!=null)
        mView.setIcon(new FastBitmapDrawable(scalebmp),mIconSize);

        if(mAllappView!=null) {
            Object tag = mAllappView.getTag();

            if (tag!=null && tag instanceof AppInfo) {
                AppInfo appInfo = (AppInfo) tag;
                if (DynamicIconFactory.getInstance().isDynamicIcon(appInfo) != 10) ;
                mAllappView.setIcon(new FastBitmapDrawable(scalebmp), mIconSize);
            }
        }

        mView.updateFolderIcon();
        Log.d("liuzuo124","onTimeChanged setIcon");
        Log.e("liuzuo73","onTimeChanged setIcon view="+mView.hashCode());
    }

/*    private void updateFolderIcon() {
        Log.d("liuzuo73","updateFolderIcon0 ");
        if(mView.getTag() instanceof ShortcutInfo){
           ShortcutInfo shortcutInfo = (ShortcutInfo) mView.getTag();
           if(shortcutInfo!=null&&shortcutInfo.rank<4&&shortcutInfo.container>0){
               Log.d("liuzuo73","updateFolderIcon1 ");
               Launcher launcher = (Launcher) mContext;
               final ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                       launcher.getWorkspace().getAllShortcutAndWidgetContainers();
               int childCount ;
               View view ;
               Object tag ;
               for (ShortcutAndWidgetContainer layout : childrenLayouts) {
                   childCount = layout.getChildCount();
                   for (int j = 0; j < childCount; j++) {
                       view = layout.getChildAt(j);
                       tag = view.getTag();
                       if (Log.DEBUG_UNREAD) {

                       }
                       if (tag instanceof FolderInfo) {
                           Log.d("liuzuo73","updateFolderIcon2 ");
                           FolderInfo folderInfo= (FolderInfo) tag;
                           if(folderInfo.contents.contains(shortcutInfo)) {
                               Log.d("liuzuo73","updateFolderIcon3 ");
                               ((FolderIcon) view).invalidate();
                           }
                       }
                   }
               }
           }
        }
           Log.d("liuzuo73","updateFolderIcon setIcon");


    }*/


    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("liuzuo73","deskClock onReceive ");
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
            }else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                stopRunnable();
            }else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                run();
            }
            updateDynamicIcon();

        }
    };

    private synchronized void registerReceiver(){
        if (!mAttached) {
            IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            if(mContext!=null) {
                mContext.registerReceiver(mIntentReceiver,filter);
            }
            mAttached = true;
            Log.d("liuzuo73","deskClock registerReceiver");
        }
    }
}
