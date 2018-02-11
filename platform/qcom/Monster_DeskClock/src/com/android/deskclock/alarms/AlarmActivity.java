/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.deskclock.alarms;

import mst.app.dialog.AlertDialog;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.android.deskclock.AnimatorUtils;
import com.android.deskclock.LogUtils;
import com.android.deskclock.R;
import com.android.deskclock.SettingsActivity;
import com.android.deskclock.Utils;
import com.android.deskclock.Util.SharePreferencesUtils;
import com.android.deskclock.alarms.ShakeListener.OnShakeListener;
import com.android.deskclock.events.Events;
import com.android.deskclock.provider.AlarmInstance;
import com.android.deskclock.pulldoor.PullDoorCallback;
import com.android.deskclock.pulldoor.PullDoorView;
import com.android.deskclock.view.MyHalfCircleViewNew;
import com.android.deskclock.widget.CircleView;

public class AlarmActivity extends Activity implements View.OnClickListener, View.OnTouchListener,
        PullDoorCallback {

    private static final String LOGTAG = AlarmActivity.class.getSimpleName();

    private static final String POWER_OFF_ALARM = "powerOffAlarm";

    private static final String POWER_OFF_ALARM_MODE = "power_off_alarm_mode";

    private static final String ACTION_POWER_OFF_ALARM = "org.codeaurora.alarm.action.POWER_OFF_ALARM";

    private static final TimeInterpolator PULSE_INTERPOLATOR = PathInterpolatorCompat.create(0.4f, 0.0f, 0.2f, 1.0f);
    private static final TimeInterpolator REVEAL_INTERPOLATOR = PathInterpolatorCompat.create(0.0f, 0.0f, 0.2f, 1.0f);

    private static final int PULSE_DURATION_MILLIS = 1000;
    private static final int ALARM_BOUNCE_DURATION_MILLIS = 500;
    private static final int ALERT_REVEAL_DURATION_MILLIS = 500;
    private static final int ALERT_FADE_DURATION_MILLIS = 500;
    private static final int ALERT_DISMISS_DELAY_MILLIS = 2000;

    private static final float BUTTON_SCALE_DEFAULT = 0.7f;
    private static final int BUTTON_DRAWABLE_ALPHA_DEFAULT = 165;

    public static boolean mIsPowerOffAlarm = false;

    private static final int SHUTDOWN_ALARM_VIEW = 1;
    private static final int SHUTDOWN_POWER_OFF = 2;

    private Context mContext;

    private boolean mIsSoonze = false;
    private boolean mIsPowerOffing = false;


//    // zouxu  [APP][Alarm]Enable Power off alarm   begin
//    public static final String ACTION_REQUEST_SHUTDOWN_ALARM = "android.intent.action.request_shutdown_alarm";
//    private static final String gesture_hand = "gesture_hand";
//    // zouxu  [APP][Alarm]Enable Power off alarm   end

    private TextView tv_hint;
    private TextView tv_snooze;
    private int sleep_count = 0;// 睡眠次数

    private Handler mBootHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SHUTDOWN_ALARM_VIEW:
                LogUtils.v(LOGTAG, "SHUTDOWN_ALARM_VIEW finish before sleep 500ms");
                finish();
                break;

            case SHUTDOWN_POWER_OFF:
                LogUtils.v(LOGTAG, "SHUTDOWN_POWER_OFF directly power off");
                powerOff();
                break;

            default:// normally will not go here
            }
        }
    };

    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(!mAlarmHandled){
                dismissWithNoAnim();
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            LogUtils.v(LOGTAG, "Received broadcast: %s", action);

            if (!mAlarmHandled) {
                switch (action) {
                case AlarmService.ALARM_SNOOZE_ACTION:
                    snooze();
                    break;
                case AlarmService.ALARM_DISMISS_ACTION:
                    dismiss();
                    break;
                case AlarmService.ALARM_DONE_ACTION:
                    if (!mIsPowerOffAlarm || mIsSoonze) {
                        finish();
                    }
                    break;
                default:
                    LogUtils.i(LOGTAG, "Unknown broadcast: %s", action);
                    break;
                }
            } else {
                LogUtils.v(LOGTAG, "Ignored broadcast: %s", action);
            }
        }
    };

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.i("Finished binding to AlarmService");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.i("Disconnected from AlarmService");
        }
    };

    private AlarmInstance mAlarmInstance;
    private boolean mAlarmHandled;
    private String mVolumeBehavior;
    private int mCurrentHourColor;
    private boolean mReceiverRegistered;
    /** Whether the AlarmService is currently bound */
    private boolean mServiceBound;

    private AccessibilityManager mAccessibilityManager;

    private ViewGroup mAlertView;
    private TextView mAlertTitleView;
    private TextView mAlertInfoView;

    private ViewGroup mContentView;
    private ImageView mAlarmButton;
    private ImageView mSnoozeButton;
    private ImageView mDismissButton;
    private TextView mHintView;

    private ValueAnimator mAlarmAnimator;
    private ValueAnimator mSnoozeAnimator;
    private ValueAnimator mDismissAnimator;
    //private ValueAnimator mPulseAnimator;//delete zouxu 20160914

    private boolean is_remind_later = false;
    private ImageView arrow_1;
    private ImageView arrow_2;
    private ImageView arrow_3;
    private MyHalfCircleViewNew my_half_circcle;
    private ImageView img_icon;
    
    private boolean is_can_shake = true;//滑动的时候不允许摇一摇功能

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("zouxu","AlarmActivity onCreate = ");

        Uri intentData = getIntent().getData();
        String intentAction = getIntent().getAction();

        if (intentAction == ACTION_POWER_OFF_ALARM) {
            mIsPowerOffAlarm = true;
        }

        mContext = getApplicationContext();

        if (mIsPowerOffAlarm) {
            mAlarmInstance = AlarmInstance.getFirstAlarmInstance(mContext.getContentResolver());

            Settings.System.putInt(mContext.getContentResolver(), POWER_OFF_ALARM_MODE, 1);

        } else if (intentData != null) {
            long instanceId = AlarmInstance.getId(intentData);
            mAlarmInstance = AlarmInstance.getInstance(this.getContentResolver(), instanceId);
        }

        if (mAlarmInstance == null) {
            // The alarm was deleted before the activity got created, so just
            // finish()
            LogUtils.e(LOGTAG, "Error displaying alarm for intent: %s", getIntent());
            finish();
            return;
        } else if (!mIsPowerOffAlarm && mAlarmInstance.mAlarmState != AlarmInstance.FIRED_STATE) {
            LogUtils.i(LOGTAG, "Skip displaying alarm for instance: %s", mAlarmInstance);
            finish();
            return;
        }

        LogUtils.i(LOGTAG, "Displaying alarm for instance: %s", mAlarmInstance);

        // Get the volume/camera button behavior setting
        mVolumeBehavior = PreferenceManager.getDefaultSharedPreferences(this).getString(
                SettingsActivity.KEY_VOLUME_BEHAVIOR, SettingsActivity.DEFAULT_VOLUME_BEHAVIOR);

        Log.i("zouxu","mIsPowerOffAlarm = "+mIsPowerOffAlarm);


        if (mIsPowerOffAlarm) {
            getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            );
        }


//        // zouxu   [APP][Alarm]Enable Power off alarm  begin
//        final Window win = getWindow();
//        String bootAlarm = SystemProperties.get("sys.boot.alarm");
//        if (bootAlarm != null && bootAlarm.equals("1")) {
//            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                    | WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }
//        // zouxu   [APP][Alarm]Enable Power off alarm  end

        // Hide navigation bar to minimize accidental tap on Home key
        hideNavigationBar();

        // Close dialogs and window shade, so this is fully visible
        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        // In order to allow tablets to freely rotate and phones to stick
        // with "nosensor" (use default device orientation) we have to have
        // the manifest start with an orientation of unspecified" and only limit
        // to "nosensor" for phones. Otherwise we get behavior like in b/8728671
        // where tablets start off in their default orientation and then are
        // able to freely rotate.
        if (!getResources().getBoolean(R.bool.config_rotateAlarmAlert)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }

        mAccessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE|0x000000f0
                            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.WHITE);
            // 透明导航栏
            // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        setContentView(R.layout.alarm_activity);

        mAlertView = (ViewGroup) findViewById(R.id.alert);
        mAlertTitleView = (TextView) mAlertView.findViewById(R.id.alert_title);
        mAlertInfoView = (TextView) mAlertView.findViewById(R.id.alert_info);

        mContentView = (ViewGroup) findViewById(R.id.content);
        mAlarmButton = (ImageView) mContentView.findViewById(R.id.alarm);
        mSnoozeButton = (ImageView) mContentView.findViewById(R.id.snooze);
        mDismissButton = (ImageView) mContentView.findViewById(R.id.dismiss);
        mHintView = (TextView) mContentView.findViewById(R.id.hint);

        final TextView titleView = (TextView) mContentView.findViewById(R.id.title);
        final TextClock digitalClock = (TextClock) mContentView.findViewById(R.id.digital_clock);
        //final CircleView pulseView = (CircleView) mContentView.findViewById(R.id.pulse);

        titleView.setText(mAlarmInstance.getLabelOrDefault(this));
        Utils.setTimeFormat(this, digitalClock, getResources().getDimensionPixelSize(R.dimen.main_ampm_font_size));

        mCurrentHourColor = Utils.getCurrentHourColor();
        // getWindow().setBackgroundDrawable(new
        // ColorDrawable(mCurrentHourColor));//去掉原来的背景

        mAlarmButton.setOnTouchListener(this);
        mSnoozeButton.setOnClickListener(this);
        mDismissButton.setOnClickListener(this);

        mAlarmAnimator = AnimatorUtils.getScaleAnimator(mAlarmButton, 1.0f, 0.0f);
        mSnoozeAnimator = getButtonAnimator(mSnoozeButton, Color.WHITE);
        mDismissAnimator = getButtonAnimator(mDismissButton, mCurrentHourColor);
//        mPulseAnimator = ObjectAnimator.ofPropertyValuesHolder(//delete zouxu 20160914
//                pulseView,
//                PropertyValuesHolder.ofFloat(CircleView.RADIUS, 0.0f, pulseView.getRadius()),
//                PropertyValuesHolder.ofObject(CircleView.FILL_COLOR, AnimatorUtils.ARGB_EVALUATOR,
//                        ColorUtils.setAlphaComponent(pulseView.getFillColor(), 0)));
//        mPulseAnimator.setDuration(PULSE_DURATION_MILLIS);
//        mPulseAnimator.setInterpolator(PULSE_INTERPOLATOR);
//        mPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
//        mPulseAnimator.start();

        if (mAlarmInstance != null && mIsPowerOffAlarm) {
            AlarmStateManager.setFiredState(getApplicationContext(), mAlarmInstance);
        }

        // if (!TextUtils.isEmpty(mAlarmInstance.mLabel)) {
        // mAlertTitleView.setText(mAlarmInstance.mLabel);
        // }
        is_remind_later = SharePreferencesUtils.istRemindLaterALarm(this, mAlarmInstance.mAlarmId);
//        my_pull_door = (PullDoorView) findViewById(R.id.my_pull_door);
//        my_pull_door.setCallBack(this);
        tv_hint = (TextView) findViewById(R.id.tv_hint);
        tv_snooze = (TextView) findViewById(R.id.tv_snooze);
        if (is_remind_later) {
            tv_snooze.setVisibility(View.VISIBLE);
        } else {
            tv_snooze.setVisibility(View.INVISIBLE);
        }

        sleep_count = SharePreferencesUtils.getSleepCount(this, mAlarmInstance.mAlarmId);
        if (sleep_count == 0) {
            tv_snooze.setText(getString(R.string.str_sleep1)+ AlarmStateManager.getSnoozedMinutes(this) + getString(R.string.timer_min));
        } else if (sleep_count == 1) {
            tv_snooze.setText(getString(R.string.str_sleep2) + AlarmStateManager.getSnoozedMinutes(this) + getString(R.string.timer_min));
        } else if (sleep_count == 2) {
            tv_snooze.setText(R.string.str_sleep3);
        }

//        Animation ani = new AlphaAnimation(0f, 1f);
//        ani.setDuration(1500);
//        ani.setRepeatMode(Animation.REVERSE);
//        ani.setRepeatCount(Animation.INFINITE);
//        tv_hint.startAnimation(ani);// 上滑关闭提示动画

        if (is_remind_later && sleep_count < 2) {// 有稍后提醒的功能 并且睡眠次数<２才监听摇一摇手机
            ShakeListener shakeListener = new ShakeListener(this);
            shakeListener.setOnShakeListener(new OnShakeListener() {
                public void onShake() {
                    if (!mIsSoonze && is_can_shake) {
                        // sleep_count++;
                        // SharePreferencesUtils.setSleepCount(AlarmActivity.this,
                        // mAlarmInstance.mAlarmId, sleep_count);
                        
                        my_half_circcle.startSnoozeAnim();
                        
                    }
                }
            });
        }
        
        arrow_1 = (ImageView)findViewById(R.id.arrow_1);
        arrow_2 = (ImageView)findViewById(R.id.arrow_2);
        arrow_3 = (ImageView)findViewById(R.id.arrow_3);
        my_half_circcle = (MyHalfCircleViewNew)findViewById(R.id.my_half_circcle);
        img_icon = (ImageView)findViewById(R.id.img_icon);
        my_half_circcle.setCallBack(this);

        arrow_1.setAlpha(0.24f);
        arrow_2.setAlpha(0.24f);
        arrow_3.setAlpha(0.24f);

        mAnimHandler.postDelayed(mRunnable, 300);
        imgIconAnim();
        
        Log.i("zouxu", "mAlarmInstance.mAlarmId = " + mAlarmInstance.mAlarmId + "　mAlarmInstance.mId ＝ "
                + mAlarmInstance.mId);

        new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    Thread.sleep(5*60*1000);
                }catch (Exception e){

                }
                mHandler.sendEmptyMessage(1);
            }
        }).start();


    }
    
    
    private Handler mAnimHandler = new Handler(){};
    private Runnable mRunnable = new Runnable() {
        
        @Override
        public void run() {
            arrowAni();
            mAnimHandler.postDelayed(mRunnable, 1200);
        }
    };


    @Override
    protected void onStart() {
        super.onStart();

        // Bind to AlarmService
        bindService(new Intent(this, AlarmService.class), mConnection, Context.BIND_AUTO_CREATE);
        mServiceBound = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Uri intentData = getIntent().getData();

        String intentAction = getIntent().getAction();
        if (intentAction == ACTION_POWER_OFF_ALARM) {
            mIsPowerOffAlarm = true;
        }

        if (!mIsPowerOffAlarm && intentData != null) {
            // Re-query for AlarmInstance in case the state has changed
            // externally
            final long instanceId = AlarmInstance.getId(getIntent().getData());
            mAlarmInstance = AlarmInstance.getInstance(getContentResolver(), instanceId);

            if (mAlarmInstance == null) {
                LogUtils.i(LOGTAG, "No alarm instance for instanceId: %d", instanceId);
                finish();
                return;
            }

            // Verify that the alarm is still firing before showing the activity
            if (!mIsPowerOffAlarm && mAlarmInstance.mAlarmState != AlarmInstance.FIRED_STATE) {
                LogUtils.i(LOGTAG, "Skip displaying alarm for instance: %s", mAlarmInstance);
                finish();
                return;
            }
        }

        if (!mReceiverRegistered) {
            // Register to get the alarm done/snooze/dismiss intent.
            final IntentFilter filter = new IntentFilter(AlarmService.ALARM_DONE_ACTION);
            filter.addAction(AlarmService.ALARM_SNOOZE_ACTION);
            filter.addAction(AlarmService.ALARM_DISMISS_ACTION);
            registerReceiver(mReceiver, filter);
            mReceiverRegistered = true;
        }

        resetAnimations();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unbindAlarmService();

        // Skip if register didn't happen to avoid IllegalArgumentException
        if (mReceiverRegistered) {
            unregisterReceiver(mReceiver);
            mReceiverRegistered = false;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsPowerOffAlarm) {
            // Boot alarm should not be destroyed before being handled.
            if (!mIsPowerOffing) {
                if (!mAlarmHandled) {
                    Settings.System.putInt(mContext.getContentResolver(), POWER_OFF_ALARM_MODE, 0);
                    mIsPowerOffAlarm = false;
                    LogUtils.d(LOGTAG, "onDestroy setSnoozeState = " + mAlarmInstance);
                    AlarmStateManager.setSnoozeState(this, mAlarmInstance, false);
                }
            }
        }

    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent keyEvent) {
        // Do this in dispatch to intercept a few of the system keys.
        LogUtils.v(LOGTAG, "dispatchKeyEvent: %s", keyEvent);

        switch (keyEvent.getKeyCode()) {
        // Volume keys and camera keys dismiss the alarm.
        case KeyEvent.KEYCODE_POWER:
        case KeyEvent.KEYCODE_VOLUME_UP:
        case KeyEvent.KEYCODE_VOLUME_DOWN:
        case KeyEvent.KEYCODE_VOLUME_MUTE:
        case KeyEvent.KEYCODE_CAMERA:
        case KeyEvent.KEYCODE_FOCUS:
            if (!mAlarmHandled && keyEvent.getAction() == KeyEvent.ACTION_UP) {
//                switch (mVolumeBehavior) {
//                case SettingsActivity.VOLUME_BEHAVIOR_SNOOZE:
//                    snooze();
//                    break;
//                case SettingsActivity.VOLUME_BEHAVIOR_DISMISS:
//                    dismiss();
//                    break;
//                default:
//                    break;
//                }
//                AlarmKlaxon.stop(this);//关闭声音
                AlarmKlaxon.stopRingTone(this);//关闭声音
            }
            return true;
        default:
            return super.dispatchKeyEvent(keyEvent);
        }
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss.
    }

    @Override
    public void onClick(View view) {
        if (mAlarmHandled) {
            LogUtils.v(LOGTAG, "onClick ignored: %s", view);
            return;
        }
        LogUtils.v(LOGTAG, "onClick: %s", view);

        // If in accessibility mode, allow snooze/dismiss by double tapping on
        // respective icons.
        if (mAccessibilityManager != null && mAccessibilityManager.isTouchExplorationEnabled()) {
            if (view == mSnoozeButton) {
                snooze();
            } else if (view == mDismissButton) {
                dismiss();
            }
            return;
        }

        if (view == mSnoozeButton) {
            hintSnooze();
        } else if (view == mDismissButton) {
            hintDismiss();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mAlarmHandled) {
            LogUtils.v(LOGTAG, "onTouch ignored: %s", motionEvent);
            return false;
        }

        final int[] contentLocation = { 0, 0 };
        mContentView.getLocationOnScreen(contentLocation);

        final float x = motionEvent.getRawX() - contentLocation[0];
        final float y = motionEvent.getRawY() - contentLocation[1];

        final int alarmLeft = mAlarmButton.getLeft() + mAlarmButton.getPaddingLeft();
        final int alarmRight = mAlarmButton.getRight() - mAlarmButton.getPaddingRight();

        final float snoozeFraction, dismissFraction;
        if (mContentView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            snoozeFraction = getFraction(alarmRight, mSnoozeButton.getLeft(), x);
            dismissFraction = getFraction(alarmLeft, mDismissButton.getRight(), x);
        } else {
            snoozeFraction = getFraction(alarmLeft, mSnoozeButton.getRight(), x);
            dismissFraction = getFraction(alarmRight, mDismissButton.getLeft(), x);
        }
        setAnimatedFractions(snoozeFraction, dismissFraction);

        switch (motionEvent.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            LogUtils.v(LOGTAG, "onTouch started: %s", motionEvent);

            // Stop the pulse, allowing the last pulse to finish.
            //mPulseAnimator.setRepeatCount(0);
            break;
        case MotionEvent.ACTION_UP:
            LogUtils.v(LOGTAG, "onTouch ended: %s", motionEvent);

            if (snoozeFraction == 1.0f) {
                snooze();
            } else if (dismissFraction == 1.0f) {
                dismiss();
            } else {
                if (snoozeFraction > 0.0f || dismissFraction > 0.0f) {
                    // Animate back to the initial state.
                    AnimatorUtils.reverse(mAlarmAnimator, mSnoozeAnimator, mDismissAnimator);
                } else if (mAlarmButton.getTop() <= y && y <= mAlarmButton.getBottom()) {
                    // User touched the alarm button, hint the dismiss action
                    hintDismiss();
                }

                // Restart the pulse.
//                mPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
//                if (!mPulseAnimator.isStarted()) {
//                    mPulseAnimator.start();
//                }
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            resetAnimations();
            break;
        default:
            break;
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void hintSnooze() {
        final int alarmLeft = mAlarmButton.getLeft() + mAlarmButton.getPaddingLeft();
        final int alarmRight = mAlarmButton.getRight() - mAlarmButton.getPaddingRight();
        final float translationX = Math.max(mSnoozeButton.getLeft() - alarmRight, 0)
                + Math.min(mSnoozeButton.getRight() - alarmLeft, 0);
        getAlarmBounceAnimator(translationX,
                translationX < 0.0f ? R.string.description_direction_left : R.string.description_direction_right)
                .start();
    }

    private void hintDismiss() {
        final int alarmLeft = mAlarmButton.getLeft() + mAlarmButton.getPaddingLeft();
        final int alarmRight = mAlarmButton.getRight() - mAlarmButton.getPaddingRight();
        final float translationX = Math.max(mDismissButton.getLeft() - alarmRight, 0)
                + Math.min(mDismissButton.getRight() - alarmLeft, 0);
        getAlarmBounceAnimator(translationX,
                translationX < 0.0f ? R.string.description_direction_left : R.string.description_direction_right)
                .start();
    }

    /**
     * Set animators to initial values and restart pulse on alarm button.
     */
    private void resetAnimations() {
        // Set the animators to their initial values.
        setAnimatedFractions(0.0f /* snoozeFraction */, 0.0f /* dismissFraction */);
        // Restart the pulse.
//        mPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
//        if (!mPulseAnimator.isStarted()) {
//            mPulseAnimator.start();
//        }
    }

    /**
     * Perform snooze animation and send snooze intent.
     */
    private void snooze() {
        mIsSoonze = true;

        mAlarmHandled = true;
        LogUtils.v(LOGTAG, "Snoozed: %s", mAlarmInstance);

        final int accentColor = Utils.obtainStyledColor(this, R.attr.colorAccent, Color.RED);
        setAnimatedFractions(1.0f /* snoozeFraction */, 0.0f /* dismissFraction */);

        final int snoozeMinutes = AlarmStateManager.getSnoozedMinutes(this);
        final String infoText = getResources().getQuantityString(R.plurals.alarm_alert_snooze_duration, snoozeMinutes,
                snoozeMinutes);
        final String accessibilityText = getResources().getQuantityString(R.plurals.alarm_alert_snooze_set,
                snoozeMinutes, snoozeMinutes);

        getAlertAnimator(mSnoozeButton, R.string.alarm_alert_snoozed_text, infoText, accessibilityText, accentColor,
                accentColor).start();

        AlarmStateManager.setSnoozeState(this, mAlarmInstance, false /* showToast */);

        Events.sendAlarmEvent(R.string.action_dismiss, R.string.label_deskclock);

        // Unbind here, otherwise alarm will keep ringing until activity
        // finishes.
        unbindAlarmService();
    }

    /**
     * Perform dismiss animation and send dismiss intent.
     */
    private void dismiss() {
        mAlarmHandled = true;
        LogUtils.v(LOGTAG, "Dismissed: %s", mAlarmInstance);

        setAnimatedFractions(0.0f /* snoozeFraction */, 1.0f /* dismissFraction */);

        getAlertAnimator(mDismissButton, R.string.alarm_alert_off_text, null /* infoText */,
                getString(R.string.alarm_alert_off_text) /* accessibilityText */, Color.WHITE, mCurrentHourColor)
                .start();

        AlarmStateManager.setDismissState(this, mAlarmInstance);

        Events.sendAlarmEvent(R.string.action_dismiss, R.string.label_deskclock);

        // Unbind here, otherwise alarm will keep ringing until activity
        // finishes.
        unbindAlarmService();

        if (mIsPowerOffAlarm) {
            showPowerOffDialog();
        }
    }

    /**
     * Unbind AlarmService if bound.
     */
    private void unbindAlarmService() {
        if (mServiceBound) {
            unbindService(mConnection);
            mServiceBound = false;
        }
    }

    private void setAnimatedFractions(float snoozeFraction, float dismissFraction) {
        final float alarmFraction = Math.max(snoozeFraction, dismissFraction);
        AnimatorUtils.setAnimatedFraction(mAlarmAnimator, alarmFraction);
        AnimatorUtils.setAnimatedFraction(mSnoozeAnimator, snoozeFraction);
        AnimatorUtils.setAnimatedFraction(mDismissAnimator, dismissFraction);
    }

    private float getFraction(float x0, float x1, float x) {
        return Math.max(Math.min((x - x0) / (x1 - x0), 1.0f), 0.0f);
    }

    private ValueAnimator getButtonAnimator(ImageView button, int tintColor) {
        return ObjectAnimator.ofPropertyValuesHolder(button, PropertyValuesHolder.ofFloat(View.SCALE_X,
                BUTTON_SCALE_DEFAULT, 1.0f), PropertyValuesHolder.ofFloat(View.SCALE_Y, BUTTON_SCALE_DEFAULT, 1.0f),
                PropertyValuesHolder.ofInt(AnimatorUtils.BACKGROUND_ALPHA, 0, 255), PropertyValuesHolder.ofInt(
                        AnimatorUtils.DRAWABLE_ALPHA, BUTTON_DRAWABLE_ALPHA_DEFAULT, 255), PropertyValuesHolder
                        .ofObject(AnimatorUtils.DRAWABLE_TINT, AnimatorUtils.ARGB_EVALUATOR, Color.WHITE, tintColor));
    }

    private ValueAnimator getAlarmBounceAnimator(float translationX, final int hintResId) {
        final ValueAnimator bounceAnimator = ObjectAnimator.ofFloat(mAlarmButton, View.TRANSLATION_X,
                mAlarmButton.getTranslationX(), translationX, 0.0f);
        bounceAnimator.setInterpolator(AnimatorUtils.DECELERATE_ACCELERATE_INTERPOLATOR);
        bounceAnimator.setDuration(ALARM_BOUNCE_DURATION_MILLIS);
        bounceAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                mHintView.setText(hintResId);
                if (mHintView.getVisibility() != View.VISIBLE) {
                    mHintView.setVisibility(View.VISIBLE);
                    ObjectAnimator.ofFloat(mHintView, View.ALPHA, 0.0f, 1.0f).start();
                }
            }
        });
        return bounceAnimator;
    }

    private Animator getAlertAnimator(final View source, final int titleResId, final String infoText,
            final String accessibilityText, final int revealColor, final int backgroundColor) {
        final ViewGroup containerView = (ViewGroup) findViewById(android.R.id.content);

        final Rect sourceBounds = new Rect(0, 0, source.getHeight(), source.getWidth());
        containerView.offsetDescendantRectToMyCoords(source, sourceBounds);

        final int centerX = sourceBounds.centerX();
        final int centerY = sourceBounds.centerY();

        final int xMax = Math.max(centerX, containerView.getWidth() - centerX);
        final int yMax = Math.max(centerY, containerView.getHeight() - centerY);

        final float startRadius = Math.max(sourceBounds.width(), sourceBounds.height()) / 2.0f;
        final float endRadius = (float) Math.sqrt(xMax * xMax + yMax * yMax);

        final CircleView revealView = new CircleView(this).setCenterX(centerX).setCenterY(centerY)
                .setFillColor(revealColor);
        containerView.addView(revealView);

        // TODO: Fade out source icon over the reveal (like LOLLIPOP version).

        final Animator revealAnimator = ObjectAnimator.ofFloat(revealView, CircleView.RADIUS, startRadius, endRadius);
        revealAnimator.setDuration(ALERT_REVEAL_DURATION_MILLIS);
        revealAnimator.setInterpolator(REVEAL_INTERPOLATOR);
        revealAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                mAlertView.setVisibility(View.VISIBLE);
                mAlertTitleView.setText(titleResId);

                if (infoText != null) {
                    mAlertInfoView.setText(infoText);
                    mAlertInfoView.setVisibility(View.VISIBLE);
                }
                mContentView.setVisibility(View.GONE);

                getWindow().setBackgroundDrawable(new ColorDrawable(backgroundColor));
            }
        });

        final ValueAnimator fadeAnimator = ObjectAnimator.ofFloat(revealView, View.ALPHA, 0.0f);
        fadeAnimator.setDuration(ALERT_FADE_DURATION_MILLIS);
        fadeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                containerView.removeView(revealView);
            }
        });

        final AnimatorSet alertAnimator = new AnimatorSet();
        alertAnimator.play(revealAnimator).before(fadeAnimator);
        alertAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                mAlertView.announceForAccessibility(accessibilityText);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if ((!mIsPowerOffAlarm && !mIsPowerOffing) || mIsSoonze) {
                            finish();
                        }
                    }
                }, ALERT_DISMISS_DELAY_MILLIS);
            }
        });

        return alertAnimator;
    }

    /**
     * Implement power off function immediately.
     */
    private void powerOff() {
        Intent requestShutdown = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
        requestShutdown.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        requestShutdown.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(requestShutdown);
    }

    private void showPowerOffDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.power_on_text).setTitle(R.string.alarm_list_title);
        builder.setPositiveButton(R.string.power_on_yes_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Settings.System.putInt(mContext.getContentResolver(), POWER_OFF_ALARM_MODE, 0);
                mIsPowerOffAlarm = false;
                mBootHandler.sendEmptyMessage(SHUTDOWN_ALARM_VIEW);
            }
        });
        builder.setNegativeButton(R.string.power_on_no_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Settings.System.putInt(mContext.getContentResolver(), POWER_OFF_ALARM_MODE, 0);
                mIsPowerOffAlarm = false;
                mIsPowerOffing = true;
                mBootHandler.sendEmptyMessage(SHUTDOWN_POWER_OFF);
            }
        });

        AlertDialog poweroffDialog = builder.create();
        poweroffDialog.setCancelable(false);
        poweroffDialog.setCanceledOnTouchOutside(false);
        poweroffDialog.getWindow()
                .addFlags(
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        poweroffDialog.show();
    }

    @Override
    public void finishIncreaseAnim() {
        // TODO Auto-generated method stub
        dismissWithNoAnim();
    }

    private void dismissWithNoAnim() {
        mAlarmHandled = true;
        LogUtils.v(LOGTAG, "Dismissed: %s", mAlarmInstance);

        setAnimatedFractions(0.0f /* snoozeFraction */, 1.0f /* dismissFraction */);

//        getAlertAnimator(mDismissButton, R.string.alarm_alert_off_text, null /* infoText */,  //delete zouxu 20160914
//                getString(R.string.alarm_alert_off_text) /* accessibilityText */, Color.WHITE, mCurrentHourColor)
//                .start();

        AlarmStateManager.setDismissState(this, mAlarmInstance);

        Events.sendAlarmEvent(R.string.action_dismiss, R.string.label_deskclock);

        // Unbind here, otherwise alarm will keep ringing until activity
        // finishes.
        unbindAlarmService();

        if (mIsPowerOffAlarm) {
            showPowerOffDialog();
        }

        if ((!mIsPowerOffAlarm && !mIsPowerOffing) || mIsSoonze) {
            finish();
        }
    }

    private void snoozeWithNoAnim() {
        mIsSoonze = true;
        mAlarmHandled = true;
        LogUtils.v(LOGTAG, "Snoozed: %s", mAlarmInstance);

        final int accentColor = Utils.obtainStyledColor(this, R.attr.colorAccent, Color.RED);
        // setAnimatedFractions(1.0f /* snoozeFraction */, 0.0f /*
        // dismissFraction */);

        final int snoozeMinutes = AlarmStateManager.getSnoozedMinutes(this);
        final String infoText = getResources().getQuantityString(R.plurals.alarm_alert_snooze_duration, snoozeMinutes,
                snoozeMinutes);
        final String accessibilityText = getResources().getQuantityString(R.plurals.alarm_alert_snooze_set,
                snoozeMinutes, snoozeMinutes);

        // getAlertAnimator(mSnoozeButton, R.string.alarm_alert_snoozed_text,
        // infoText, accessibilityText, accentColor,
        // accentColor).start();

        AlarmStateManager.setSnoozeState(this, mAlarmInstance, false /* showToast */);

        Events.sendAlarmEvent(R.string.action_dismiss, R.string.label_deskclock);

        // Unbind here, otherwise alarm will keep ringing until activity
        // finishes.
        unbindAlarmService();
        if ((!mIsPowerOffAlarm && !mIsPowerOffing) || mIsSoonze) {
            Toast.makeText(this, snoozeMinutes + getString(R.string.str_sleep_toast), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void arrowAni(){
        ObjectAnimator anim = ObjectAnimator.ofFloat(arrow_1, "zx", 0.0F, 1.2F).setDuration(800);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();
                
                float alpha1=0.24f;
                float alpha2 = 0.24f;
                float alpha3 = 0.24f;
                
                if(cVal<=0.3){
                    alpha1 =0.24f+0.76f*cVal*3.0f;// arrow_1 ---0.24~1.0
                    alpha2 = 0.24f;
                    alpha3 = 0.24f;
                } else if(cVal>0.3&& cVal<=0.6){//arrow1 --- 1~0.24
                    alpha1 = (cVal-0.3f)*(0.24f-1f)/(0.6f-0.3f)+1.0f;//arrow1 --- 1~0.24
                    alpha2 = 0.24f+(cVal*0.3f)*(1.0f-0.24f)/(0.6f-0.3f);//arrow2 ---- 0.24~1
                    alpha3 = 0.24f;
                } else if(cVal>0.6f && cVal<=0.9){
                    alpha1 = 0.24f;
                    alpha2 = 1.0f+(cVal-0.6f)*(0.24f-1.0f)/(0.9f-0.6f);//arrow2 --- 1~0.24
                    alpha3 = 0.24f+(cVal-0.6f)*(1.0f-0.24f)/(0.9f-0.6f);//arrow3 --- 0.24~1.0
                } else if(cVal>0.9f && cVal<=1.2){
                    alpha1 = 0.24f;
                    alpha2 = 0.24f;
                    alpha3 = 1.0f+(cVal-0.9f)*(0.24f-1.0f)/(1.2f-0.9f);//arrow3 --- 1.0~0.24
                }
                arrow_1.setAlpha(alpha1);
                arrow_2.setAlpha(alpha2);
                arrow_3.setAlpha(alpha3);
            }
        });
        anim.start();
    }

    
    public void imgIconAnim(){
        final TranslateAnimation animation = new TranslateAnimation(0, 0,0, 30); 
        animation.setDuration(180);//设置动画持续时间
        animation.setRepeatCount(Animation.INFINITE);//设置重复次数
        animation.setRepeatMode(Animation.REVERSE);//设置反方向执行 
        img_icon.setAnimation(animation);
        animation.startNow();
    }

    
    @Override
    public void finishDecreaseAnim() {
        arrow_1.setVisibility(View.VISIBLE);
        arrow_2.setVisibility(View.VISIBLE);
        arrow_3.setVisibility(View.VISIBLE);
        is_can_shake = true;
    }

    @Override
    public void onMoveing(int height, float progress) {
        
        is_can_shake = false;
        int off_set_height = 0;
        if(height>0){
            off_set_height = height*7/10;
        } else {
            off_set_height = height;
        }
        
        float my_progress = 1.0f-progress;
        
        img_icon.setAlpha(my_progress);
        tv_hint.setAlpha(my_progress);
        tv_snooze.setAlpha(my_progress);
        
        img_icon.setTranslationY(-off_set_height);
        tv_snooze.setTranslationY(-off_set_height);
        tv_hint.setTranslationY(-off_set_height);
        
        arrow_1.setVisibility(View.INVISIBLE);
        arrow_2.setVisibility(View.INVISIBLE);
        arrow_3.setVisibility(View.INVISIBLE);
    }

    @Override
    public void finishSnoozeAnim() {
        snoozeWithNoAnim();
    }

//    // zouxu  [APP][Alarm]Enable Power off alarm   begin
//    public void AlarmBootPowerOff(){
//        Log.d("zouxu","AlarmActivity AlarmBootPowerOff ACTION_REQUEST_SHUTDOWN_ALARM");
//        if (Utils.isAlarmBoot()) {
//            sendBroadcast(new Intent(ACTION_REQUEST_SHUTDOWN_ALARM));
//        }
//    }
//    // zouxu  [APP][Alarm]Enable Power off alarm   end

}
