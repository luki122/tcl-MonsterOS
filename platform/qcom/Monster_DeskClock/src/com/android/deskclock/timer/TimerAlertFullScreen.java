/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.deskclock.timer;

import mst.app.MstActivity;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.android.deskclock.BaseActivity;
import com.android.deskclock.R;
import com.android.deskclock.TimerRingService;
import com.android.deskclock.Utils;
import com.android.deskclock.pulldoor.PullDoorCallback;
import com.android.deskclock.pulldoor.PullDoorView;
import com.android.deskclock.timer.TimerFullScreenFragment.OnEmptyListListener;
import com.android.deskclock.view.MyHalfCircleViewNew;

/**
 * Timer alarm alert: pops visible indicator. This activity is the version which
 * shows over the lock screen. This activity re-uses TimerFullScreenFragment GUI
 */
public class TimerAlertFullScreen extends Activity implements OnEmptyListListener, PullDoorCallback {

    private static final String TAG = "TimerAlertFullScreen";
    private static final String FRAGMENT = "timer";
    // private PullDoorView my_pull_door;
    private TextView tv_hint;
    private TextView tv_hint_pull;
    private ImageView arrow_1;
    private ImageView arrow_2;
    private ImageView arrow_3;
    private ImageView img_icon;
    private MyHalfCircleViewNew my_half_circcle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | 0x000000f0
                            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE
            );
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
//            window.setNavigationBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.WHITE);

            // getToolbar().setVisibility(View.GONE);
            // 透明导航栏
//             getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.timer_alert_full_screen);
        // setMstContentView(R.layout.timer_alert_full_screen);
        final View view = findViewById(R.id.fragment_container);
        // view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);//状态栏显示处于低能显示状态(low
        // profile模式)，状态栏上一些图标显示会被隐藏。

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        // win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        // );//chg zouxu 20160907
        // Turn on the screen unless we are being launched from the AlarmAlert
        // subclass as a result of the screen turning off.
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        // my_pull_door = (PullDoorView) findViewById(R.id.my_pull_door);
        tv_hint = (TextView) findViewById(R.id.tv_hint);
        tv_hint_pull = (TextView) findViewById(R.id.tv_hint_pull);
        img_icon = (ImageView) findViewById(R.id.img_icon);
        arrow_1 = (ImageView) findViewById(R.id.arrow_1);
        arrow_2 = (ImageView) findViewById(R.id.arrow_2);
        arrow_3 = (ImageView) findViewById(R.id.arrow_3);
        arrow_1.setAlpha(0.24f);
        arrow_2.setAlpha(0.24f);
        arrow_3.setAlpha(0.24f);

        my_half_circcle = (MyHalfCircleViewNew) findViewById(R.id.my_half_circcle);
        my_half_circcle.setCallBack(this);
        // Don't create overlapping fragments.
        if (getFragment() == null) {
            TimerFullScreenFragment timerFragment = new TimerFullScreenFragment();

            // Create fragment and give it an argument to only show
            // timers in STATE_TIMESUP state
            Bundle args = new Bundle();
            args.putBoolean(Timers.TIMESUP_MODE, true);

            timerFragment.setArguments(args);

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction().add(R.id.fragment_container, timerFragment, FRAGMENT).commit();
        }

        // Animation ani = new AlphaAnimation(0f, 1f);
        // ani.setDuration(1500);
        // ani.setRepeatMode(Animation.REVERSE);
        // ani.setRepeatCount(Animation.INFINITE);
        // tv_hint.startAnimation(ani);// 上滑关闭提示动画
        mHandler.postDelayed(mArrowRunnable, 100);
        // mHandler.postDelayed(mImgRunnable, 100);
        imgIconAnim();

//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                stopAllTimesUpTimers();
//            }
//        },10000);

        new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    Thread.sleep(5*60*1000);
                }catch (Exception e){

                }
//                stopAllTimesUpTimers();
                mHandler.sendEmptyMessage(1);
            }
        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Only show notifications for times-up when this activity closed.
        Utils.cancelTimesUpNotifications(this);
    }

    @Override
    public void onPause() {
        Utils.showTimesUpNotifications(this);

        super.onPause();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Handle key down and key up on a few of the system keys.
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        switch (event.getKeyCode()) {
        // Volume keys and camera keys stop all the timers
        case KeyEvent.KEYCODE_VOLUME_UP:
        case KeyEvent.KEYCODE_VOLUME_DOWN:
        case KeyEvent.KEYCODE_VOLUME_MUTE:
        case KeyEvent.KEYCODE_CAMERA:
        case KeyEvent.KEYCODE_FOCUS:
        case KeyEvent.KEYCODE_POWER:
            if (up) {
//                stopAllTimesUpTimers();//chg zouxu 20160921
                stopRingtone(this);
            }
            return true;

        default:
            break;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * this is called when a second timer is triggered while a previous alert
     * window is still active.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        TimerFullScreenFragment timerFragment = getFragment();
        if (timerFragment != null) {
            timerFragment.restartAdapter();
        }
        super.onNewIntent(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        ViewGroup viewContainer = (ViewGroup) findViewById(R.id.fragment_container);
        viewContainer.requestLayout();
        super.onConfigurationChanged(newConfig);
    }

    protected void stopAllTimesUpTimers() {
        TimerFullScreenFragment timerFragment = getFragment();
        if (timerFragment != null) {
            timerFragment.updateAllTimesUpTimers();
        }
    }

    @Override
    public void onEmptyList() {
        if (Timers.LOGGING) {
            Log.v(TAG, "onEmptyList");
        }
        onListChanged();
        finish();
    }

    @Override
    public void onListChanged() {
        Utils.showInUseNotifications(this);
    }

    private TimerFullScreenFragment getFragment() {
        return (TimerFullScreenFragment) getFragmentManager().findFragmentByTag(FRAGMENT);
    }

    @Override
    public void finishIncreaseAnim() {
        stopAllTimesUpTimers();
    }
    

    private int pos = 1;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
            stopAllTimesUpTimers();
        }
    };
    private Runnable mArrowRunnable = new Runnable() {

        @Override
        public void run() {
            arrowAni();
            mHandler.postDelayed(mArrowRunnable, 1200);
        }
    };

    // private Runnable mImgRunnable = new Runnable() {
    //
    // @Override
    // public void run() {
    // imgIconAnim();
    // mHandler.postDelayed(mImgRunnable, 700);
    // }
    // };

    public void arrowAni() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(arrow_1, "zx", 0.0F, 1.2F).setDuration(800);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();

                float alpha1 = 0.24f;
                float alpha2 = 0.24f;
                float alpha3 = 0.24f;

                if (cVal <= 0.3) {
                    alpha1 = 0.24f + 0.76f * cVal * 3.0f;// arrow_1 ---0.24~1.0
                    alpha2 = 0.24f;
                    alpha3 = 0.24f;
                } else if (cVal > 0.3 && cVal <= 0.6) {// arrow1 --- 1~0.24
                    alpha1 = (cVal - 0.3f) * (0.24f - 1f) / (0.6f - 0.3f) + 1.0f;// arrow1
                                                                                 // ---
                                                                                 // 1~0.24
                    alpha2 = 0.24f + (cVal * 0.3f) * (1.0f - 0.24f) / (0.6f - 0.3f);// arrow2
                                                                                    // ----
                                                                                    // 0.24~1
                    alpha3 = 0.24f;
                } else if (cVal > 0.6f && cVal <= 0.9) {
                    alpha1 = 0.24f;
                    alpha2 = 1.0f + (cVal - 0.6f) * (0.24f - 1.0f) / (0.9f - 0.6f);// arrow2
                                                                                   // ---
                                                                                   // 1~0.24
                    alpha3 = 0.24f + (cVal - 0.6f) * (1.0f - 0.24f) / (0.9f - 0.6f);// arrow3
                                                                                    // ---
                                                                                    // 0.24~1.0
                } else if (cVal > 0.9f && cVal <= 1.2) {
                    alpha1 = 0.24f;
                    alpha2 = 0.24f;
                    alpha3 = 1.0f + (cVal - 0.9f) * (0.24f - 1.0f) / (1.2f - 0.9f);// arrow3
                                                                                   // ---
                                                                                   // 1.0~0.24
                }
                arrow_1.setAlpha(alpha1);
                arrow_2.setAlpha(alpha2);
                arrow_3.setAlpha(alpha3);
            }
        });
        anim.start();
    }

    public void imgIconAnim() {
        final TranslateAnimation animation = new TranslateAnimation(0, 0, 0, 30);
        animation.setDuration(180);// 设置动画持续时间
        animation.setRepeatCount(Animation.INFINITE);// 设置重复次数
        animation.setRepeatMode(Animation.REVERSE);// 设置反方向执行
        img_icon.setAnimation(animation);
        animation.startNow();
    }

    @Override
    public void finishDecreaseAnim() {
        arrow_1.setVisibility(View.VISIBLE);
        arrow_2.setVisibility(View.VISIBLE);
        arrow_3.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMoveing(int height, float progress) {

        float my_progress = 1.0f - progress;

        int off_set_height = height * 7 / 10;

        img_icon.setAlpha(my_progress);
        tv_hint.setAlpha(my_progress);
        tv_hint_pull.setAlpha(my_progress);

        img_icon.setTranslationY(-off_set_height);
        tv_hint_pull.setTranslationY(-off_set_height);
        tv_hint.setTranslationY(-off_set_height);

        arrow_1.setVisibility(View.INVISIBLE);
        arrow_2.setVisibility(View.INVISIBLE);
        arrow_3.setVisibility(View.INVISIBLE);

//        setNavigationBar(my_progress);
    }

    @Override
    public void finishSnoozeAnim() {
        // TODO Auto-generated method stub

    }

    private void stopRingtone(final Context context) {//关闭铃声
        // Stop ringtone
        Intent si = new Intent();
        si.setClass(context, TimerRingService.class);
        context.stopService(si);
    }

//    public void setNavigationBar(float progress){
//
//        int alpa = (int)(255*progress);
//
//        Log.i("zouxu","setNavigationBar alpa="+alpa);
//
//        int color = Color.argb(alpa,255,255,255);
//        Window window = getWindow();
//        window.setNavigationBarColor(color);
//    }

}
