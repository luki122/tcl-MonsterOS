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

package com.android.deskclock.timer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import mst.widget.FloatingActionButton;
import mst.widget.TimePicker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.deskclock.AnimatorUtils;
import com.android.deskclock.DeskClock;
import com.android.deskclock.DeskClockFragment;
import com.android.deskclock.R;
import com.android.deskclock.TimerSetupView;
import com.android.deskclock.Utils;
import com.android.deskclock.VerticalViewPager;
import com.android.deskclock.events.Events;
import com.android.deskclock.view.NumberPickerView;
import com.android.deskclock.view.NumberPickerView.OnValueChangeListener;
import com.android.deskclock.view.loopView.LoopView;

public class TCLTimerFragment extends DeskClockFragment implements OnSharedPreferenceChangeListener, OnClickListener {
    // public static final long ANIMATION_TIME_MILLIS =
    // DateUtils.SECOND_IN_MILLIS / 3;
    //
    // private static final String KEY_SETUP_SELECTED = "_setup_selected";
    // private static final String KEY_ENTRY_STATE = "entry_state";
    // private static final int PAGINATION_DOTS_COUNT = 4;
    // private static final String CURR_PAGE = "_currPage";
    // private static final TimeInterpolator ACCELERATE_INTERPOLATOR = new
    // AccelerateInterpolator();
    // private static final TimeInterpolator DECELERATE_INTERPOLATOR = new
    // DecelerateInterpolator();
    // private static final long ROTATE_ANIM_DURATION_MILIS = 150;
    //
    // // Transitions are available only in API 19+
    // private static final boolean USE_TRANSITION_FRAMEWORK =
    // Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    //
    private boolean mTicking = false;
    // private TimerSetupView mSetupView;
    // private VerticalViewPager mViewPager;
    // private ImageButton mCancel;
    // private ViewGroup mContentView;
    // private View mTimerView;
    // private View mLastView;
    // private ImageView[] mPageIndicators = new
    // ImageView[PAGINATION_DOTS_COUNT];
    // private Transition mDeleteTransition;
    private SharedPreferences mPrefs;
    private NotificationManager mNotificationManager;
    private Bundle mViewState = null;
    private TimerObj mainTimerObj;

    // private final ViewPager.OnPageChangeListener mOnPageChangeListener =
    // new ViewPager.SimpleOnPageChangeListener() {
    // @Override
    // public void onPageSelected(int position) {
    // highlightPageIndicator(position);
    // TimerFragment.this.setTimerViewFabIcon(getCurrentTimer());
    // }
    // };

    private long showLastTime;
    
    private final Runnable mClockTick = new Runnable() {
        boolean mVisible = true;
        final static int TIME_PERIOD_MS = 1000;
        final static int TIME_DELAY_MS = 20;
        final static int SPLIT = TIME_PERIOD_MS / 2;

        @Override
        public void run() {
            // Setup for blinking
            final boolean visible = Utils.getTimeNow() % TIME_PERIOD_MS < SPLIT;
            final boolean toggle = mVisible != visible;
            mVisible = visible;
            // for (int i = 0; i < mAdapter.getCount(); i++) {
            final TimerObj t = mainTimerObj;// mAdapter.getTimerAt(i);
            if (t.mState == TimerObj.STATE_RUNNING || t.mState == TimerObj.STATE_TIMESUP) {
                final long timeLeft = t.updateTimeLeft(false);
                if (t.mView != null) {
                    t.mView.setTime(timeLeft, false);//chg zouxu  false强制更新 false并且显示百位数的时候界面不更新
                    showLastTime = timeLeft;


                    t.mView.startCircleRuning();

                }
                // Log.i("zouxu", "timeLeft1 = "+timeLeft);
                // // Update button every 1/2 second
                // // if (toggle) {
                // // final ImageButton addMinuteButton = (ImageButton)
                // t.mView.findViewById(R.id.reset_add);
                // // final boolean canAddMinute = TimerObj.MAX_TIMER_LENGTH -
                // t.mTimeLeft > TimerObj.MINUTE_IN_MILLIS;
                // // addMinuteButton.setEnabled(canAddMinute);
                // // }
                // }
                // MyTime myTime = new MyTime();
                // myTime.setTime(timeLeft, false, false);
                //
                // picker_hour.setValue(myTime.hour);
                // picker_min.setValue(myTime.min);
                // picker_sec.setValue(myTime.sec);
                
                //setPickerTime(timeLeft, true);//delete 20160826

//                Log.i("zouxu", "timeLeft2 = " + timeLeft);

            }
            if (t.mTimeLeft <= 0 && t.mState != TimerObj.STATE_RESTART) {
                t.setState(TimerObj.STATE_TIMESUP);
                Log.i("zouxu", "t.setState(TimerObj.STATE_TIMESUP);");
                if (t.mView != null) {
                    //t.mView.timesUp();//delete zouxu 20161102
                }
                // setBroadTimesUpCast();
            }
            // The blinking
            if (toggle && t.mView != null) {
                if (t.mState == TimerObj.STATE_TIMESUP) {
                    t.mView.setCircleBlink(mVisible);
                }
                if (t.mState == TimerObj.STATE_STOPPED) {
                    t.mView.setTextBlink(mVisible);
                    //t.writeToSharedPref(mPrefs);//add zouxu
                    if(showLastTime !=t.mTimeLeft ){//最后显示的时间和保存的时间不一样 保存的时间和显示的时间一致 20160829
                        //t.mView.setTime(t.mTimeLeft, false);
                        t.seLastShowTime(showLastTime);
                        showLastTime = t.mTimeLeft;
                    }

                }
            }
            time_picker.postDelayed(mClockTick, TIME_DELAY_MS);
        }
        // mTimerView.postDelayed(mClockTick, TIME_DELAY_MS);
        // }
    };

//    int last_min = 0;
//    int last_sec = 0;

//    public void setPickerTime(long timeLeft, boolean isSmooth) {
//        MyTime myTime = new MyTime();
//        myTime.setTime(timeLeft, false, false);
//
//        // picker_hour.setValue(myTime.hour);
//        if (isSmooth) {
//            if (last_min != myTime.min) {
//                picker_min.smoothScrollToValue(last_min, myTime.min, true);
//            }
//            if (last_sec != myTime.sec) {
//                Log.i("setPickerTime", "last_sec = " + last_sec + ",myTime.sec = " + myTime.sec);
//                picker_sec.smoothScrollToValue(last_sec, myTime.sec, true);
//            }
//        } else {
//            picker_min.setValue(myTime.min);
//            picker_sec.setValue(myTime.sec);
//            Log.i("setPickerTime", "myTime.min = "+myTime.min+",myTime.sec="+myTime.sec);
//        }
//        last_min = myTime.min;
//        last_sec = myTime.sec;
//    }

    // private NumberPicker picker_hour;
//    private NumberPickerView picker_min;
//    private NumberPickerView picker_sec;

//    private TextView text1;
//    private TextView text2;
//    private TextView text3;
    private FloatingActionButton img_start;
    private TextView img_cancle;
    private TextView img_pause;
    private TextView img_cancle_center;


    private TimerListItem mTimerView;


    private View picker_blank;
    
    private TimePicker time_picker;

    
    private void initView(View view) {
        // picker_hour = (NumberPicker) view.findViewById(R.id.picker_hour);
        picker_blank = view.findViewById(R.id.picker_blank);
        time_picker = (TimePicker)view.findViewById(R.id.time_picker);
        time_picker.setIs24HourView(true);
        
//        picker_min = (NumberPickerView) view.findViewById(R.id.picker_min);
//        picker_sec = (NumberPickerView) view.findViewById(R.id.picker_sec);
        time_picker = (TimePicker) view.findViewById(R.id.time_picker);

//        text1 = (TextView) view.findViewById(R.id.text1);
//        text2 = (TextView) view.findViewById(R.id.text2);
//        text3 = (TextView) view.findViewById(R.id.text3);

        img_pause = (TextView) view.findViewById(R.id.img_pause);
        img_cancle_center = (TextView) view.findViewById(R.id.img_cancle_center);

        img_start = (FloatingActionButton) view.findViewById(R.id.img_start);
        img_cancle = (TextView) view.findViewById(R.id.img_cancle);

//        text1.setOnClickListener(this);
//        text2.setOnClickListener(this);
//        text3.setOnClickListener(this);

        img_start.setOnClickListener(this);
        img_cancle.setOnClickListener(this);
        img_pause.setOnClickListener(this);
        img_cancle_center.setOnClickListener(this);

//        picker_min.setOnValueChangedListener(new OnValueChangeListener() {
//
//            @Override
//            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
//                last_min = newVal;
//            }
//        });
//        picker_sec.setOnValueChangedListener(new OnValueChangeListener() {
//
//            @Override
//            public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
//                last_sec = newVal;
//            }
//        });

        mTimerView = (TimerListItem) view.findViewById(R.id.timer_item);
        

        //initLoopView();//delete zouxu 20161102
        
    }
    
    private void initLoopView(){
        List<String> list_data = new ArrayList<String>();
        for(int i=0;i<60;i++){
            if(i<10){
                list_data.add("0"+i);
            } else {
                list_data.add(""+i);
            }
        }
        

        
        
//        loopview_sec.setViewPadding(0, 0, 100, 0);

//        loopview_sec.post(new Runnable() {
//            
//            @Override
//            public void run() {
//                loopview_sec.setViewPadding(0, 0, 100, 0);
//            }
//        });


    }

    private void initPicker() {
        // picker_hour.setMaxValue(99);
        // picker_hour.setMinValue(0);
        // picker_hour.setValue(0);

//        picker_min.setMaxValue(23);
//        picker_min.setMinValue(0);
//        picker_min.setValue(0);
//
//        picker_sec.setMaxValue(59);
//        picker_sec.setMinValue(0);
//        picker_sec.setValue(0);

    }

    private long getTime() {
//        return time_picker.getHour()*60 + time_picker.getMinute();
        return time_picker.getHour()*60*60 + time_picker.getMinute()*60;
    }

    public boolean setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        boolean result = false;
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass().getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText) child).setTextColor(color);
                    numberPicker.invalidate();
                    result = true;
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewState = savedInstanceState;
        // mainTimerObj = new TimerObj(p);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.tcl_timer_fragment, container, false);
        initView(view);
        initPicker();
        // mContentView = (ViewGroup) view;
        // mTimerView = view.findViewById(R.id.timer_view);
        // mSetupView = (TimerSetupView) view.findViewById(R.id.timer_setup);
        // mViewPager = (VerticalViewPager)
        // view.findViewById(R.id.vertical_view_pager);
        // mPageIndicators[0] = (ImageView)
        // view.findViewById(R.id.page_indicator0);
        // mPageIndicators[1] = (ImageView)
        // view.findViewById(R.id.page_indicator1);
        // mPageIndicators[2] = (ImageView)
        // view.findViewById(R.id.page_indicator2);
        // mPageIndicators[3] = (ImageView)
        // view.findViewById(R.id.page_indicator3);
        // mCancel = (ImageButton) view.findViewById(R.id.timer_cancel);
        // mCancel.setOnClickListener(new OnClickListener() {
        // @Override
        // public void onClick(View v) {
        // if (mAdapter.getCount() != 0) {
        // final AnimatorListenerAdapter adapter = new AnimatorListenerAdapter()
        // {
        // @Override
        // public void onAnimationEnd(Animator animation) {
        // mSetupView.reset(); // Make sure the setup is cleared for next time
        // mSetupView.setScaleX(1.0f); // Reset the scale for setup view
        // goToPagerView();
        // }
        // };
        // createRotateAnimator(adapter, false).start();
        // }
        // }
        // });
        // if (USE_TRANSITION_FRAMEWORK) {
        // mDeleteTransition = new AutoTransition();
        // mDeleteTransition.setDuration(ANIMATION_TIME_MILLIS / 2);
        // mDeleteTransition.setInterpolator(new
        // AccelerateDecelerateInterpolator());
        // }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Context context = getActivity();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof DeskClock) {
            DeskClock activity = (DeskClock) getActivity();
            activity.registerPageChangedListener(this);
        }

        mPrefs.registerOnSharedPreferenceChangeListener(this);

        if (mPrefs.getBoolean(Timers.REFRESH_UI_WITH_LATEST_DATA, false)) {
            // Clear the flag indicating the adapter is out of sync with the
            // database.
            mPrefs.edit().putBoolean(Timers.REFRESH_UI_WITH_LATEST_DATA, false).apply();
        }
        mainTimerObj = TimerObj.getTimerFromSharedPrefs(mPrefs);
        if (mainTimerObj == null) {
            Log.i("zouxu", "mainTimerObj === null");
        } else {
            Log.i("zouxu", "mainTimerObj !!= null");
            initTimeView();
        }

        boolean goToSetUpView = true;
        // Process extras that were sent to the app and were intended for the
        // timer fragment
        final Intent newIntent = getActivity().getIntent();
        if (newIntent != null && newIntent.getBooleanExtra(TimerFullScreenFragment.GOTO_SETUP_VIEW, false)) {
            goToSetUpView = true;
        } else if (newIntent != null && newIntent.getBooleanExtra(Timers.FIRST_LAUNCH_FROM_API_CALL, false)) {
            // We use this extra to identify if a. this activity is launched
            // from api call,
            // and b. this fragment is resumed for the first time. If both are
            // true,
            // we should show the timer view instead of setup view.
            goToSetUpView = false;
            // highlightPageIndicator(0);
            // Find the id of the timer to scroll to. Timers are loaded from
            // SharedPrefs using a
            // HashSet as an intermediary, so we need to find the position in
            // this specific
            // Adapter instance instead of just passing the position through the
            // intent.
            // final int timerPosition = ((TimerFragmentAdapter)
            // mViewPager.getAdapter())
            // .getTimerPosition(newIntent.getIntExtra(Timers.SCROLL_TO_TIMER_ID,
            // 0));
            // mViewPager.setCurrentItem(timerPosition);

            // Reset the extra to false to ensure when next time the fragment
            // resume,
            // we no longer care if it's from api call or not.
            newIntent.putExtra(Timers.FIRST_LAUNCH_FROM_API_CALL, false);
        } else {
            if (mViewState != null) {
                // final int currPage = mViewState.getInt(CURR_PAGE);
                // mViewPager.setCurrentItem(currPage);
                // highlightPageIndicator(currPage);
                // final boolean hasPreviousInput =
                // mViewState.getBoolean(KEY_SETUP_SELECTED, false);
                // goToSetUpView = hasPreviousInput || mAdapter.getCount() == 0;
                // mSetupView.restoreEntryState(mViewState, KEY_ENTRY_STATE);

                // if (mainTimerObj == null) {
                // goToSetUpView = true;
                // } else {
                // goToSetUpView = false;
                // }
            }
        }
        if (mainTimerObj == null) {
            goToSetUpView = true;
        } else {
            goToSetUpView = false;
        }
        
//        setPickerTime(0, false);//add zouxu 20160826
        
        time_picker.setHour(0);
        time_picker.setMinute(0);

        goToSetUpView();
        img_cancle_center.setVisibility(View.GONE);

        if (!goToSetUpView) {

            //setPickerTime(mainTimerObj.updateTimeLeft(false), false); //delete zouxu 20160826
            Log.i("zouxu", "mState = " + mainTimerObj.mState + ",TimerObj.STATE_TIMESUP=" + TimerObj.STATE_TIMESUP);
            if (mainTimerObj.mState == TimerObj.STATE_RUNNING ) {
                preperTick();// 显示取消 暂停
                img_pause.setText(R.string.str_pause);
//                img_pause.setImageResource(R.drawable.img_pause);
//                img_pause.setIconDrawable(getActivity().getResources().getDrawable(R.drawable.img_pause));
            } else if (mainTimerObj.mState == TimerObj.STATE_STOPPED) {// 显示取消
                                                                       // 恢复
                preperTick();
                img_pause.setText(R.string.str_rec);
//                img_pause.setImageResource(R.drawable.img_start);
//                img_pause.setIconDrawable(getActivity().getResources().getDrawable(R.drawable.img_start));

            } else if ( mainTimerObj.mState == TimerObj.STATE_RESTART) {
//                setPickerTime(0, false);
                
                time_picker.setHour(0);
                time_picker.setMinute(0);
                
                // deleteTimer(mainTimerObj);
                goToSetUpView();// 进设置时间界面 20160826
            } else if(mainTimerObj.mState == TimerObj.STATE_TIMESUP){
                preperTick();// 显示取消 暂停
                img_cancle_center.setVisibility(View.VISIBLE);
                img_pause.setVisibility(View.GONE);
                img_cancle.setVisibility(View.GONE);

            } else {
                img_start.setVisibility(View.VISIBLE);// 显示开始
                img_cancle.setVisibility(View.GONE);
                img_pause.setVisibility(View.GONE);
                img_pause.setText(R.string.str_pause);
//                img_pause.setImageResource(R.drawable.img_pause);
//                img_pause.setIconDrawable(getActivity().getResources().getDrawable(R.drawable.img_pause));

                // picker_hour.setEnabled(true);
//                picker_min.setTouchAble(true);
//                picker_sec.setTouchAble(true);
                showTimeView(false);// 隐藏TimeView 20160826
            }
        }
    }
    
    private void initTimeView(){
        mainTimerObj.mView = mTimerView;
        final long timeLeft = mainTimerObj.updateTimeLeft(false);
        final boolean drawWithColor = mainTimerObj.mState != TimerObj.STATE_RESTART;
        mainTimerObj.mView.set(mainTimerObj.mOriginalLength, timeLeft, drawWithColor);
        mainTimerObj.mView.setTime(timeLeft, true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof DeskClock) {
            ((DeskClock) getActivity()).unregisterPageChangedListener(this);
        }
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        // if (mAdapter != null) {
        // mAdapter.saveTimersToSharedPrefs();
        // }
        if (mainTimerObj != null) {
            TimerObj.putTimersInSharedPrefs(mPrefs, mainTimerObj);
        }
        stopClockTicks();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // if (mSetupView != null) {
        // outState.putBoolean(KEY_SETUP_SELECTED, mSetupView.getVisibility() ==
        // View.VISIBLE);
        // mSetupView.saveEntryState(outState, KEY_ENTRY_STATE);
        // }
        // outState.putInt(CURR_PAGE, mViewPager.getCurrentItem());
        mViewState = outState;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewState = null;
    }

    @Override
    public void onPageChanged(int page) {
        if (page == DeskClock.TIMER_TAB_INDEX) {
            mFab.setVisibility(View.GONE);
            mLeftButton.setVisibility(View.GONE);
            mRightButton.setVisibility(View.GONE);
        }
    }

    // Starts the ticks that animate the timers.
    private void startClockTicks() {
        time_picker.postDelayed(mClockTick, 20);
        mTicking = true;
    }

    // Stops the ticks that animate the timers.
    private void stopClockTicks() {
        if (mTicking) {
            time_picker.removeCallbacks(mClockTick);
            mTicking = false;
        }
    }

    private void preperTick() {

        // picker_hour.setEnabled(false);
//        picker_min.setTouchAble(false);
//        picker_sec.setTouchAble(false);

//        text1.setClickable(false);
//        text2.setClickable(false);
//        text3.setClickable(false);

        img_start.setVisibility(View.GONE);
        img_cancle.setVisibility(View.VISIBLE);
        img_pause.setVisibility(View.VISIBLE);

        showTimeView(true);

        // mTimerView.setVisibility(View.VISIBLE);
        // mSetupView.setVisibility(View.GONE);
        // mLastView = mTimerView;
        setLeftRightButtonAppearance();
        setFabAppearance();
        startClockTicks();
    }

    public void showTimeView(boolean is) {
        
        if (is) {
            mTimerView.setVisibility(View.VISIBLE);
            picker_blank.setVisibility(View.GONE);
        } else {
            mTimerView.setVisibility(View.GONE);
//            picker_blank.setVisibility(View.VISIBLE);
            picker_blank.setVisibility(View.GONE);
        }
        
//        setPickerTime(0, false);//add zouxu 20160826
        
        time_picker.setHour(0);
        time_picker.setMinute(0);


    }

    private void goToSetUpView() {
        // if (mAdapter.getCount() == 0) {
        // mCancel.setVisibility(View.INVISIBLE);
        // } else {
        // mCancel.setVisibility(View.VISIBLE);
        // }
        // mTimerView.setVisibility(View.GONE);
        // mSetupView.setVisibility(View.VISIBLE);
        // mSetupView.updateDeleteButtonAndDivider();
        // mSetupView.registerStartButton(mFab);
        // mLastView = mSetupView;

        img_start.setVisibility(View.VISIBLE);
        img_cancle.setVisibility(View.GONE);
        img_pause.setVisibility(View.GONE);
        img_cancle_center.setVisibility(View.GONE);

        showTimeView(false);
        
        // picker_hour.setValue(0);

        // int min_last = picker_min.getValue();
        // int sec_last = picker_sec.getValue();
        //
        // if(min_last !=0){
        // picker_min.smoothScrollToValue(min_last, 0, true);
        // }
        // if(sec_last !=0){
        // picker_sec.smoothScrollToValue(sec_last, 0, true);
        // }

        // picker_hour.setEnabled(true);
//        picker_min.setTouchAble(true);
//        picker_sec.setTouchAble(true);

//        text1.setClickable(true);
//        text2.setClickable(true);
//        text3.setClickable(true);

        setLeftRightButtonAppearance();
        setFabAppearance();
        stopClockTicks();
    }

    private void updateTimerState(TimerObj t, String action) {
        updateTimerState(t, action, true);
    }

    /**
     * @param update
     *            indicates whether to call updateNextTimesup in TimerReceiver.
     *            This is false only for label changes.
     */
    private void updateTimerState(final TimerObj t, String action, boolean update) {
        if (Timers.DELETE_TIMER.equals(action)) {
            // mAdapter.deleteTimer(t.mTimerId);
            // if (mAdapter.getCount() == 0) {
            // mSetupView.reset();
            // mainTimerObj.mView.stop();
            // mainTimerObj.deleteAllFromSharedPref(mPrefs);
            mainTimerObj.deleteFromSharedPref(mPrefs);
            goToSetUpView();
            // }
        } else {
            t.writeToSharedPref(mPrefs);
        }
        final Intent i = new Intent();
        i.setAction(action);
        i.putExtra(Timers.TIMER_INTENT_EXTRA, t.mTimerId);
        i.putExtra(Timers.UPDATE_NEXT_TIMESUP, update);
        // Make sure the receiver is getting the intent ASAP.
        i.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        getActivity().sendBroadcast(i);
    }

    // private void setTimerViewFabIcon(TimerObj timer) {
    // final DeskClock deskClock = (DeskClock) getActivity();
    // if (deskClock == null || timer == null || mFab == null) {
    // return;
    // }
    //
    // if (deskClock.getSelectedTab() != DeskClock.TIMER_TAB_INDEX) {
    // return;
    // }
    //
    // final Resources r = deskClock.getResources();
    // switch (timer.mState) {
    // case TimerObj.STATE_RUNNING:
    // mFab.setVisibility(View.VISIBLE);
    // mFab.setContentDescription(r.getString(R.string.timer_stop));
    // mFab.setImageResource(R.drawable.ic_fab_pause);
    // break;
    // case TimerObj.STATE_STOPPED:
    // case TimerObj.STATE_RESTART:
    // // It is possible for a Timer from an older version of Clock to be
    // // in STATE_DELETED and
    // // still exist in the list
    // case TimerObj.STATE_DELETED:
    // mFab.setVisibility(View.VISIBLE);
    // mFab.setContentDescription(r.getString(R.string.timer_start));
    // mFab.setImageResource(R.drawable.ic_fab_play);
    // break;
    // case TimerObj.STATE_TIMESUP: // time-up but didn't stopped, continue
    // // negative ticking
    // mFab.setVisibility(View.VISIBLE);
    // mFab.setContentDescription(r.getString(R.string.timer_stop));
    // mFab.setImageResource(R.drawable.ic_fab_stop);
    // break;
    // default:
    // }
    // }

    @Override
    public void onFabClick(View view) {
        // if (mLastView != mTimerView) {
        // // Timer is at Setup View, so fab is "play", rotate from setup view
        // to timer view
        // final AnimatorListenerAdapter adapter = new AnimatorListenerAdapter()
        // {
        // @Override
        // public void onAnimationStart(Animator animation) {
        // final int timerLength = mSetupView.getTime();
        // final TimerObj timerObj = new TimerObj(timerLength *
        // DateUtils.SECOND_IN_MILLIS,
        // getActivity());
        // timerObj.setState(TimerObj.STATE_RUNNING);
        // Events.sendTimerEvent(R.string.action_create,
        // R.string.label_deskclock);
        //
        // updateTimerState(timerObj, Timers.START_TIMER);
        // Events.sendTimerEvent(R.string.action_start,
        // R.string.label_deskclock);
        //
        // // Go to the newly created timer view
        // mAdapter.addTimer(timerObj);
        // mViewPager.setCurrentItem(0);
        // highlightPageIndicator(0);
        // }
        //
        // @Override
        // public void onAnimationEnd(Animator animation) {
        // mSetupView.reset(); // Make sure the setup is cleared for next time
        // mSetupView.setScaleX(1.0f); // Reset the scale for setup view
        // goToPagerView();
        // }
        // };
        // createRotateAnimator(adapter, false).start();
        // } else {
        // // Timer is at view pager, so fab is "play" or "pause" or
        // "square that means reset"
        // final TimerObj t = getCurrentTimer();
        // switch (t.mState) {
        // case TimerObj.STATE_RUNNING:
        // // Stop timer and save the remaining time of the timer
        // t.setState(TimerObj.STATE_STOPPED);
        // t.mView.pause();
        // t.updateTimeLeft(true);
        // updateTimerState(t, Timers.STOP_TIMER);
        // Events.sendTimerEvent(R.string.action_stop,
        // R.string.label_deskclock);
        // break;
        // case TimerObj.STATE_STOPPED:
        // case TimerObj.STATE_RESTART:
        // // It is possible for a Timer from an older version of Clock to be in
        // STATE_DELETED and
        // // still exist in the list
        // case TimerObj.STATE_DELETED:
        // // Reset the remaining time and continue timer
        // t.setState(TimerObj.STATE_RUNNING);
        // t.mStartTime = Utils.getTimeNow() - (t.mOriginalLength -
        // t.mTimeLeft);
        // t.mView.start();
        // updateTimerState(t, Timers.START_TIMER);
        // Events.sendTimerEvent(R.string.action_start,
        // R.string.label_deskclock);
        // break;
        // case TimerObj.STATE_TIMESUP:
        // if (t.mDeleteAfterUse) {
        // cancelTimerNotification(t.mTimerId);
        // // Tell receiver the timer was deleted.
        // // It will stop all activity related to the
        // // timer
        // t.setState(TimerObj.STATE_DELETED);
        // updateTimerState(t, Timers.DELETE_TIMER);
        // Events.sendTimerEvent(R.string.action_delete,
        // R.string.label_deskclock);
        // } else {
        // t.setState(TimerObj.STATE_RESTART);
        // t.mOriginalLength = t.mSetupLength;
        // t.mTimeLeft = t.mSetupLength;
        // t.mView.stop();
        // t.mView.setTime(t.mTimeLeft, false);
        // t.mView.set(t.mOriginalLength, t.mTimeLeft, false);
        // updateTimerState(t, Timers.RESET_TIMER);
        // cancelTimerNotification(t.mTimerId);
        // Events.sendTimerEvent(R.string.action_reset,
        // R.string.label_deskclock);
        // }
        // break;
        // }
        // setTimerViewFabIcon(t);
        // }
    }

    @Override
    public void setFabAppearance() {
        final DeskClock activity = (DeskClock) getActivity();
        if (mFab == null) {
            return;
        }

        if (activity.getSelectedTab() != DeskClock.TIMER_TAB_INDEX && activity.getSelectedTab() != DeskClock.STOPWATCH_TAB_INDEX) {
            
            if(!activity.isDeleteMode()){
            } else {
                mFab.setVisibility(View.GONE);
            }
            
            return;
        }
    }

    @Override
    public void setLeftRightButtonAppearance() {
        final DeskClock activity = (DeskClock) getActivity();
        if (mLeftButton == null || mRightButton == null || activity.getSelectedTab() != DeskClock.TIMER_TAB_INDEX) {
            return;
        }

        mLeftButton.setEnabled(true);
        mRightButton.setEnabled(true);
    }

    @Override
    public void onRightButtonClick(View view) {
        // Respond to add another timer
        // final AnimatorListenerAdapter adapter = new AnimatorListenerAdapter()
        // {
        // @Override
        // public void onAnimationEnd(Animator animation) {
        // mSetupView.reset();
        // mTimerView.setScaleX(1.0f); // Reset the scale for timer view
        // goToSetUpView();
        // }
        // };
        // createRotateAnimator(adapter, true).start();
    }

    @Override
    public void onLeftButtonClick(View view) {
        // Respond to delete timer
        // final TimerObj timer = getCurrentTimer();
        // if (timer == null) {
        // return; // Prevent NPE if user click delete faster than the fade
        // animation
        // }
        // if (timer.mState == TimerObj.STATE_TIMESUP) {
        // mNotificationManager.cancel(timer.mTimerId);
        // }
        // if (mAdapter.getCount() == 1) {
        // final AnimatorListenerAdapter adapter = new AnimatorListenerAdapter()
        // {
        // @Override
        // public void onAnimationEnd(Animator animation) {
        // mTimerView.setScaleX(1.0f); // Reset the scale for timer view
        // deleteTimer(timer);
        // }
        // };
        // createRotateAnimator(adapter, true).start();
        // } else {
        // if (USE_TRANSITION_FRAMEWORK) {
        // TransitionManager.beginDelayedTransition(mContentView,
        // mDeleteTransition);
        // }
        // deleteTimer(timer);
        // }
    }

    private void deleteTimer(TimerObj timer) {
        // Tell receiver the timer was deleted, it will stop all activity
        // related to the
        // timer
        timer.setState(TimerObj.STATE_DELETED);
        updateTimerState(timer, Timers.DELETE_TIMER);
        Events.sendTimerEvent(R.string.action_delete, R.string.label_deskclock);
        // When deleting a negative timer (hidden fab), since deleting will not
        // trigger
        // onResume(), in order to ensure the fab showing correctly, we need to
        // manually
        // set fab appearance here.
        // setFabAppearance();
        mainTimerObj = null;
    }

    // private void highlightPageIndicator(int position) {
    // final int count = mAdapter.getCount();
    // if (count <= PAGINATION_DOTS_COUNT) {
    // for (int i = 0; i < PAGINATION_DOTS_COUNT; i++) {
    // if (count < 2 || i >= count) {
    // mPageIndicators[i].setVisibility(View.GONE);
    // } else {
    // paintIndicator(i, position == i ? R.drawable.ic_swipe_circle_light :
    // R.drawable.ic_swipe_circle_dark);
    // }
    // }
    // } else {
    // /**
    // * If there are more than 4 timers, the top and/or bottom dot might need
    // to show a
    // * half fade, to indicate there are more timers in that direction.
    // */
    // final int aboveCount = position; // How many timers are above the current
    // timer
    // final int belowCount = count - position - 1; // How many timers are below
    // if (aboveCount < PAGINATION_DOTS_COUNT - 1) {
    // // There's enough room for the above timers, so top dot need not to fade
    // for (int i = 0; i < aboveCount; i++) {
    // paintIndicator(i, R.drawable.ic_swipe_circle_dark);
    // }
    // paintIndicator(position, R.drawable.ic_swipe_circle_light);
    // for (int i = position + 1; i < PAGINATION_DOTS_COUNT - 1 ; i++) {
    // paintIndicator(i, R.drawable.ic_swipe_circle_dark);
    // }
    // paintIndicator(PAGINATION_DOTS_COUNT - 1,
    // R.drawable.ic_swipe_circle_bottom);
    // } else {
    // // There's not enough room for the above timers, top dot needs to fade
    // paintIndicator(0, R.drawable.ic_swipe_circle_top);
    // for (int i = 1; i < PAGINATION_DOTS_COUNT - 2; i++) {
    // paintIndicator(i, R.drawable.ic_swipe_circle_dark);
    // }
    // // Determine which resource to use for the "second indicator" from the
    // bottom.
    // paintIndicator(PAGINATION_DOTS_COUNT - 2, belowCount == 0 ?
    // R.drawable.ic_swipe_circle_dark : R.drawable.ic_swipe_circle_light);
    // final int lastDotRes;
    // if (belowCount == 0) {
    // // The current timer is the last one
    // lastDotRes = R.drawable.ic_swipe_circle_light;
    // } else if (belowCount == 1) {
    // // There's only one timer below the current
    // lastDotRes = R.drawable.ic_swipe_circle_dark;
    // } else {
    // // There are more than one timer below, bottom dot needs to fade
    // lastDotRes = R.drawable.ic_swipe_circle_bottom;
    // }
    // paintIndicator(PAGINATION_DOTS_COUNT - 1, lastDotRes);
    // }
    // }
    // }

    private void paintIndicator(int position, int res) {
        // mPageIndicators[position].setVisibility(View.VISIBLE);
        // mPageIndicators[position].setImageResource(res);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        // if (prefs.equals(mPrefs)) {
        // if (key.equals(Timers.REFRESH_UI_WITH_LATEST_DATA) &&
        // prefs.getBoolean(key, false)) {
        // // Clear the flag forcing a refresh of the adapter to reflect
        // external changes.
        // mPrefs.edit().putBoolean(key, false).apply();
        // mAdapter.populateTimersFromPref();
        // mViewPager.setAdapter(mAdapter);
        // if (mViewState != null) {
        // final int currPage = mViewState.getInt(CURR_PAGE);
        // mViewPager.setCurrentItem(currPage);
        // highlightPageIndicator(currPage);
        // } else {
        // highlightPageIndicator(0);
        // }
        // setFabAppearance();
        // }
        // }
    }

    public void setLabel(TimerObj timer, String label) {
        timer.mLabel = label;
        updateTimerState(timer, Timers.TIMER_UPDATE, false);
        // Make sure the new label is visible.
        // mAdapter.populateTimersFromPref();
    }

    // public void onPlusOneButtonPressed(TimerObj t) {
    // switch (t.mState) {
    // case TimerObj.STATE_RUNNING:
    // t.addTime(TimerObj.MINUTE_IN_MILLIS);
    // long timeLeft = t.updateTimeLeft(false);
    // t.mView.setTime(timeLeft, false);
    // t.mView.setLength(timeLeft);
    // mAdapter.notifyDataSetChanged();
    // updateTimerState(t, Timers.TIMER_UPDATE);
    //
    // Events.sendTimerEvent(R.string.action_add_minute,
    // R.string.label_deskclock);
    // break;
    // case TimerObj.STATE_STOPPED:
    // t.setState(TimerObj.STATE_RESTART);
    // t.mTimeLeft = t.mSetupLength;
    // t.mOriginalLength = t.mSetupLength;
    // t.mView.stop();
    // t.mView.setTime(t.mTimeLeft, false);
    // t.mView.set(t.mOriginalLength, t.mTimeLeft, false);
    // updateTimerState(t, Timers.RESET_TIMER);
    //
    // Events.sendTimerEvent(R.string.action_reset, R.string.label_deskclock);
    // break;
    // case TimerObj.STATE_TIMESUP:
    // // +1 min when the time is up will restart the timer with 1 minute left.
    // t.setState(TimerObj.STATE_RUNNING);
    // t.mStartTime = Utils.getTimeNow();
    // t.mTimeLeft = t.mOriginalLength = TimerObj.MINUTE_IN_MILLIS;
    // t.mView.setTime(t.mTimeLeft, false);
    // t.mView.set(t.mOriginalLength, t.mTimeLeft, true);
    // t.mView.start();
    // updateTimerState(t, Timers.RESET_TIMER);
    // Events.sendTimerEvent(R.string.action_add_minute,
    // R.string.label_deskclock);
    //
    // updateTimerState(t, Timers.START_TIMER);
    // cancelTimerNotification(t.mTimerId);
    // break;
    // }
    // // This will change status of the timer, so update fab
    // setFabAppearance();
    // }

    private void cancelTimerNotification(int timerId) {
        mNotificationManager.cancel(timerId);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        int current_min =time_picker.getHour();
        int current_sec = time_picker.getMinute();
        switch (id) {
//        case R.id.text1:// 泡面
            // picker_hour.setValue(0);
//            picker_min.smoothScrollToValue(current_min, 3, true);
//            if (current_sec != 0) {
//                picker_sec.smoothScrollToValue(current_sec, 0, true);
//            }
//            break;
//        case R.id.text2:// 面膜
            // picker_hour.setValue(0);
            // picker_min.setValue(15);
            // picker_sec.setValue(0);
//            picker_min.smoothScrollToValue(current_min, 15, true);
//            if (current_sec != 0) {
//                picker_sec.smoothScrollToValue(current_sec, 0, true);
//            }
//            break;
//        case R.id.text3:// 午休
            // picker_hour.setValue(0);
            // picker_min.setValue(30);
            // picker_sec.setValue(0);
//            picker_min.smoothScrollToValue(current_min, 30, true);
//            if (current_sec != 0) {
//                picker_sec.smoothScrollToValue(current_sec, 0, true);
//            }
//            break;
        case R.id.img_start:// 开始
            startClick();
            break;
        case R.id.img_cancle:// 取消
        case R.id.img_cancle_center:// 取消
            cancleClick();
            break;
        case R.id.img_pause:// 暂停
            pauseClick();
            break;
        }
    }

    private void startClick() {

        long timerLength = getTime();
        if (timerLength <= 0) {
            Toast.makeText(getActivity(), getString(R.string.str_set_time_first), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mainTimerObj == null) {
            mainTimerObj = new TimerObj(timerLength * DateUtils.SECOND_IN_MILLIS, getActivity());
        } else {
            mainTimerObj.init(timerLength * DateUtils.SECOND_IN_MILLIS, mainTimerObj.mTimerId);
        }

//        setPickerTime(0, false);//点击开始后就清除数据 20160826
        
        time_picker.setHour(0);
        time_picker.setMinute(0);
        
        
        initTimeView();

        mainTimerObj.setState(TimerObj.STATE_RUNNING);
        Events.sendTimerEvent(R.string.action_create, R.string.label_deskclock);
        updateTimerState(mainTimerObj, Timers.START_TIMER);
        Events.sendTimerEvent(R.string.action_start, R.string.label_deskclock);

        img_pause.setText(R.string.str_pause);
//        img_pause.setImageResource(R.drawable.img_pause);
//        img_pause.setIconDrawable(getActivity().getResources().getDrawable(R.drawable.img_pause));

        preperTick();

        mainTimerObj.mView.start();

    }

    private void cancleClick() {
        final TimerObj timer = mainTimerObj;
        if (timer == null) {
            return; // Prevent NPE if user click delete faster than the fade
                    // animation
        }
        if (timer.mState == TimerObj.STATE_TIMESUP) {
            mNotificationManager.cancel(timer.mTimerId);
        }
        deleteTimer(timer);

//        int min_last = picker_min.getValue();
//        int sec_last = picker_sec.getValue();

//        if (min_last != 0) { //delete zouxu 20160826
//            picker_min.smoothScrollToValue(min_last, 0, true);
//        }
//        if (sec_last != 0) {//delete zouxu 20160826
//            picker_sec.smoothScrollToValue(sec_last, 0, true);
//        }
//        setPickerTime(0,false);
        time_picker.setHour(0);
        time_picker.setMinute(0);
        
        
    }

    private void pauseClick() {
        final TimerObj t = mainTimerObj;
        switch (t.mState) {
        case TimerObj.STATE_RUNNING:
            // Stop timer and save the remaining time of the timer
            t.setState(TimerObj.STATE_STOPPED);
            t.mView.pause();
            t.updateTimeLeft(true);
            updateTimerState(t, Timers.STOP_TIMER);
            Events.sendTimerEvent(R.string.action_stop, R.string.label_deskclock);
            img_pause.setText(R.string.str_rec);
//            img_pause.setImageResource(R.drawable.img_start);
//            img_pause.setIconDrawable(getActivity().getResources().getDrawable(R.drawable.img_start));

            break;
        case TimerObj.STATE_STOPPED:
        case TimerObj.STATE_RESTART:
            // It is possible for a Timer from an older version of Clock to be
            // in STATE_DELETED and
            // still exist in the list
        case TimerObj.STATE_DELETED:
            // Reset the remaining time and continue timer
            t.setState(TimerObj.STATE_RUNNING);
            t.mStartTime = Utils.getTimeNow() - (t.mOriginalLength - t.mTimeLeft);
            t.mView.start();
            updateTimerState(t, Timers.START_TIMER);
            Events.sendTimerEvent(R.string.action_start, R.string.label_deskclock);
            img_pause.setText(R.string.str_pause);
//            img_pause.setImageResource(R.drawable.img_pause);
//            img_pause.setIconDrawable(getActivity().getResources().getDrawable(R.drawable.img_pause));
            break;
        case TimerObj.STATE_TIMESUP:
            if (t.mDeleteAfterUse) {
                cancelTimerNotification(t.mTimerId);
                // Tell receiver the timer was deleted.
                // It will stop all activity related to the
                // timer
                t.setState(TimerObj.STATE_DELETED);
                updateTimerState(t, Timers.DELETE_TIMER);
                Events.sendTimerEvent(R.string.action_delete, R.string.label_deskclock);
            } else {
                t.setState(TimerObj.STATE_RESTART);
                t.mOriginalLength = t.mSetupLength;
                t.mTimeLeft = t.mSetupLength;
                t.mView.stop();
                t.mView.setTime(t.mTimeLeft, false);
                t.mView.set(t.mOriginalLength, t.mTimeLeft, false);
                updateTimerState(t, Timers.RESET_TIMER);
                cancelTimerNotification(t.mTimerId);
                Events.sendTimerEvent(R.string.action_reset, R.string.label_deskclock);
            }
            break;
        }
        // setTimerViewFabIcon(t);
        
//        setPickerTime(0,false);
        time_picker.setHour(0);
        time_picker.setMinute(0);

    }

    private void setBroadTimesUpCast() {// 主动发送TIMES_UP广播 关屏的时候无法触发暂时无用
        Intent intent = new Intent();
        intent.setAction(Timers.TIMES_UP);
        intent.setClass(getActivity(), TimerReceiver.class);
        // Time-critical, should be foreground
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        int timerId = (mainTimerObj == null) ? -1 : mainTimerObj.mTimerId;
        intent.putExtra(Timers.TIMER_INTENT_EXTRA, timerId);
        getActivity().sendBroadcast(intent);
    }

}
