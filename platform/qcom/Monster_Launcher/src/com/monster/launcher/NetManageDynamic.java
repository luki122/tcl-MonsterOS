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
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.monster.launcher.dynamic.IDynamicIcon;

/**
 * 创建于 cailiuzuo on 16-10-31 下午5:41.
 * 作者
 */
public class NetManageDynamic implements IDynamicIcon {

    private Context mContext;
    private int mColor;
    private int mIconId;
    private int mIconSize;
    private String mTemp;
    private Bitmap mBitmap;
    private int mTextSize;
    private int mTextUnitSize;
    private int mLineSpacingExtra;
    private final String TAG = "liuzuo914";
    private boolean mAttached;
    private BubbleTextView mView;
    private BubbleTextView mAllappView;
    private static final String WEATHER_CN_PACKAGE_NAME = "package_name";
    private String mWeatherPackageName;
    private final String ACTION_REQUEST = "com.monster.netmanage.update_dataplan_icon.action";
    private final String ACTION_CHANGE = "com.monster.netmanage.update_dataplan.action";
    private static final String NETMANGE_CN_ICON_ID = "data_icon";
    private static final String NETMANGE_CN_UNIT = "remain_data_unit";
    private static final String SPLIT = " ";
    private Time mCalendar;
    private int mHour;

    private ItemInfo mItemInfo;

    public NetManageDynamic() {
    }


    public boolean run() {
        Log.d("liuzuo914", "weather sendBroadcast");
        mContext.sendBroadcast(new Intent(ACTION_REQUEST));
        return updateDynamicIcon();
    }


    @Override
    public boolean init(Context context, BubbleTextView bubbleTextView, ItemInfo info, boolean isAllapps) {
        final Resources r = context.getResources();
        mContext = context;
        if (isAllapps) {
            mAllappView = bubbleTextView;
        } else {
            mView = bubbleTextView;
        }

        if (mView != null && mView.mIconSize != 0) {
            mIconSize = mView.mIconSize;
        } else if (mAllappView != null && mAllappView.mIconSize != 0) {
            mIconSize = mAllappView.mIconSize;
        }
        mItemInfo = info;
        mColor = r.getColor(R.color.calendar_text);
        mTextSize = (int) r.getDimension(R.dimen.net_manage_icon_text_size);
        mLineSpacingExtra = r.getDimensionPixelSize(R.dimen.weather_icon_text_LineSpacingExtra);
        mTextUnitSize = (int) r.getDimension(R.dimen.net_manage_icon_text_unit_size);
        mCalendar =new Time();
        registerReceiver();
        return run();
    }

    @Override
    public void onAttachedToWindow(boolean register) {
        if (register)
            registerReceiver();

        run();
    }

    @Override
    public void onDetachedFromWindow() {
        try {
            if (mAttached) {
                mContext.unregisterReceiver(mIntentReceiver);
                mAttached = false;
                Log.d(TAG, "weather unregisterReceiver");
            }
        } catch (Exception e) {
            Log.e("liuzuo", "mIntentReceiver is unregistered");
        }
    }


    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        // mView.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean updateDynamicIcon() {
        return onWeatherChanged(false);
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

    private boolean onWeatherChanged(boolean isFromBroadcast) {

        return updateWeatherIcon(isFromBroadcast);
        // updateContentDescription(mCalendar);
    }

    private boolean updateWeatherIcon(boolean isFromBroadcast) {
        if (mIconSize == 0)
            return false;
        Log.d("liuzuo914", "updateWeatherIcon" + "  mIconId=" + mIconId + "  mWeatherPackageName= " + mWeatherPackageName);
        mBitmap = Bitmap.createBitmap(mIconSize, mIconSize, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(mBitmap);
        Drawable icon = null;
        if (mIconId != 0 && mWeatherPackageName != null) {
            Log.d("liuzuo914", "packageContext");
            Context packageContext = null;
            try {
                packageContext = mContext.createPackageContext(mWeatherPackageName, Context.CONTEXT_IGNORE_SECURITY);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("liuzuo914", "NameNotFoundException");
            }
            if (packageContext != null) {
                Resources resources = packageContext.getResources();
                icon = resources.getDrawable(mIconId);
            }
            if (icon != null) {
                int w = icon.getIntrinsicWidth();
                int h = icon.getIntrinsicHeight();
                int x = mIconSize / 2;
                int y = mIconSize / 2;

                if ((mIconSize < w) || (mIconSize < h)) {
                    float scale = Math.min((float) mIconSize / (float) w,
                            (float) mIconSize / (float) h);
                    canvas.save();

                    canvas.scale(scale, scale, x, y);

                }
                icon.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
                icon.draw(canvas);
                canvas.restore();
                if (mTemp != null && mTemp.length() > 0) {
                    //final float mDensity = mContext.getResources().getDisplayMetrics().density;

                    Paint mDatePaint = new Paint();
                    mDatePaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                    mDatePaint.setTextSize(mTextSize);
                    mDatePaint.setColor(mColor);
                    mDatePaint.setAntiAlias(true);
                    mDatePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                    Rect rect = new Rect();
                    Rect rectUnit =new Rect();
                    String temp = new String();

                    String[] strings = mTemp.split(SPLIT);
                    for (int i = 0; i < strings.length - 1; i++) {
                        temp += strings[i];
                    }
                    mDatePaint.getTextBounds(temp, 0, temp.length(), rect);
                    mDatePaint.setTextSize(15);
                    String unit = strings[strings.length - 1];
                    mDatePaint.getTextBounds(unit, 0, unit.length(), rectUnit);
                    mDatePaint.setTextSize(mTextSize);
                    int rectWidth = rect.right - rect.left;
                    int rectHeight = rect.bottom - rect.top+rectUnit.bottom-rectUnit.top;
                    int width = mBitmap.getWidth();
                    int height = mBitmap.getHeight();


                    Log.d("liuzuo914", "temp= " + temp + "  strings[strings.length-1]=" + strings[strings.length-1] + "  mTextSize=" + mTextSize);
                    canvas.drawText(temp, (width - rectWidth) / 2 , (height - rectHeight) / 2 - rect.top, mDatePaint);
                    mDatePaint.setTextSize(mTextUnitSize);

                    canvas.drawText(unit,(width - rectUnit.right - rectUnit.left)/2 - rectUnit.left ,
                            (height  -rectHeight+rect.bottom - rect.top)/2 - rect.top+mLineSpacingExtra, mDatePaint);

                }
                Bitmap scalebmp = Utilities.createIconBitmap(mBitmap, mContext);
                if (mView != null)
                    mView.setIcon(new FastBitmapDrawable(scalebmp), mIconSize);
                if (mAllappView != null)
                    mAllappView.setIcon(new FastBitmapDrawable(scalebmp), mIconSize);
                mView.updateFolderIcon();
                if (isFromBroadcast) {
                    AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
                    createAddAnimation(mView, anim);
                    anim.start();
                }
                return true;
            }
            return false;
        } else {

            return false;
        }

    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {

            } else if (ACTION_CHANGE.equals(intent.getAction())) {
                Log.d(TAG, "onReceive ACTION_CHANGE=" + intent.getExtras().toString());
                int iconId = intent.getIntExtra(NETMANGE_CN_ICON_ID, 0);

                mWeatherPackageName = intent.getStringExtra(WEATHER_CN_PACKAGE_NAME);

                String temp = intent.getStringExtra(NETMANGE_CN_UNIT);
                Log.d(TAG, "mTemp=" + temp+" iconId="+iconId);
                if (mIconId != iconId || (temp != null && !temp.equals(mTemp))) {
                    mTemp = temp;
                    mIconId = iconId;
                    mCalendar.setToNow();
                    mHour = mCalendar.hour;
                    updateDynamicIcon();
                }
            } else if (ACTION_REQUEST.equals(intent.getAction())) {
                /*Log.d("liuzuo914","ACTION_REQUEST="+intent.getExtras().toString());*/
            } else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())){
                mContext.sendBroadcast(new Intent(ACTION_REQUEST));
            } else if(Intent.ACTION_TIME_TICK.equals(intent.getAction())){
                Log.d("liuzuo915","netmanage ACTION_TIME_TICK");
                mCalendar.setToNow();
                if(mHour!=mCalendar.hour) {
                    mHour = mCalendar.hour;
                    Log.d("liuzuo915","netmanage sendBroadcast");
                    mContext.sendBroadcast(new Intent(ACTION_REQUEST));
                }
            }


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

    private synchronized void registerReceiver() {
        if (!mAttached) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_REQUEST);
            filter.addAction(ACTION_CHANGE);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIME_TICK);
            if (mContext != null) {
                mContext.registerReceiver(mIntentReceiver, filter);
            }
            mAttached = true;
            Log.d(TAG, "weather registerReceiver");
        }
    }
}
