/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.deskclock;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextClock;

import com.android.deskclock.Util.SharePreferencesUtils;
import com.android.deskclock.view.WorldClockView;
import com.android.deskclock.worldclock.Cities;
import com.android.deskclock.worldclock.CitiesActivity;
import com.android.deskclock.worldclock.CityObj;
import com.android.deskclock.worldclock.ClickInterface;
import com.android.deskclock.worldclock.TCLCitiesActivity;
import com.android.deskclock.worldclock.WorldClockAdapter;

import mst.view.menu.bottomnavigation.BottomNavigationView;

/**
 * Fragment that shows the clock (analog or digital), the next alarm info and
 * the world clock.
 */
public class ClockFragment extends DeskClockFragment implements OnSharedPreferenceChangeListener, ClickInterface {

    private static final String BUTTONS_HIDDEN_KEY = "buttons_hidden";
    private static final boolean PRE_L_DEVICE = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    private final static String TAG = "ClockFragment";

    private boolean mButtonsHidden = false;
    // private View mDigitalClock, mAnalogClock, mClockFrame, mHairline;
    private View mClockFrame;
    private AnalogClock mAnalogClock;
    private WorldClockAdapter mAdapter;
    private ListView mList;
    private SharedPreferences mPrefs;
    private String mDateFormat;
    private String mDateFormatForAccessibility;
    private String mDefaultClockStyle;
    private String mClockStyle;

    private WorldClockView mWorldView;
    
    private BottomNavigationView clock_delete;
    private View footerView;
    private String select_city_id;

    private boolean isReload = true;

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean changed = action.equals(Intent.ACTION_TIME_CHANGED)
                    || action.equals(Intent.ACTION_TIMEZONE_CHANGED) || action.equals(Intent.ACTION_LOCALE_CHANGED);
            if (changed) {
                // Utils.updateDate(mDateFormat, mDateFormatForAccessibility,
                // mClockFrame);
                if (mAdapter != null) {
                    // *CHANGED may modify the need for showing the Home City
                    if (mAdapter.hasHomeCity() != mAdapter.needHomeCity()) {
                        mAdapter.reloadData(context);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
                    // Locale change: update digital clock format and
                    // reload the cities list with new localized names
                    if (action.equals(Intent.ACTION_LOCALE_CHANGED)) {
                        // if (mDigitalClock != null) {
                        // Utils.setTimeFormat(context, (TextClock)
                        // mDigitalClock.findViewById(R.id.digital_clock),
                        // context.getResources().getDimensionPixelSize(R.dimen.main_ampm_font_size));
                        // }
                        mAdapter.loadCitiesDb(context);
                        mAdapter.notifyDataSetChanged();
                    }
                }
                Utils.setQuarterHourUpdater(mHandler, mQuarterHourUpdater);
            }
            Log.i("zouxu", "BroadcastReceiver action =" + action);
            if (changed || action.equals(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED)
                    || action.equals(Intent.ACTION_TIME_TICK)) {
                // Utils.refreshAlarm(getActivity(), mClockFrame);
                updateWorldView();
            }
        }
    };

    public void updateWorldView() {
        if (mWorldView != null) {
            List<CityObj> getList = mAdapter.getCitiesList();
            mWorldView.updateTime(getList, 0);
        }
    }

    private final Handler mHandler = new Handler();

    /* Register ContentObserver to see alarm changes for pre-L */
    private final ContentObserver mAlarmObserver = PRE_L_DEVICE ? new ContentObserver(mHandler) {
        @Override
        public void onChange(boolean selfChange) {
            // Utils.refreshAlarm(ClockFragment.this.getActivity(),
            // mClockFrame);
        }
    } : null;

    // Thread that runs on every quarter-hour and refreshes the date.
    private final Runnable mQuarterHourUpdater = new Runnable() {
        @Override
        public void run() {
            // Update the main and world clock dates
            // Utils.updateDate(mDateFormat, mDateFormatForAccessibility,
            // mClockFrame);
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
            Utils.setQuarterHourUpdater(mHandler, mQuarterHourUpdater);
        }
    };

    public ClockFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.clock_fragment, container, false);
        if (icicle != null) {
            mButtonsHidden = icicle.getBoolean(BUTTONS_HIDDEN_KEY, false);
        }
        mList = (ListView) v.findViewById(R.id.cities);
        clock_delete = (BottomNavigationView) v.findViewById(R.id.clock_delete);
        
//        clock_delete.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                deleteClick();
//            }
//        });
        clock_delete.setNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem arg0) {

                deleteClick();

                return false;
            }
        });

        
        mWorldView = (WorldClockView) v.findViewById(R.id.my_world_view);
        mList.setDivider(null);

        OnTouchListener longPressNightMode = new OnTouchListener() {
            private float mMaxMovementAllowed = -1;
            private int mLongPressTimeout = -1;
            private float mLastTouchX, mLastTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mMaxMovementAllowed == -1) {
                    mMaxMovementAllowed = ViewConfiguration.get(getActivity()).getScaledTouchSlop();
                    mLongPressTimeout = ViewConfiguration.getLongPressTimeout();
                }

                switch (event.getAction()) {
                case (MotionEvent.ACTION_DOWN):
                    long time = Utils.getTimeNow();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(getActivity(), ScreensaverActivity.class));
                        }
                    }, mLongPressTimeout);
                    mLastTouchX = event.getX();
                    mLastTouchY = event.getY();
                    return true;
                case (MotionEvent.ACTION_MOVE):
                    float xDiff = Math.abs(event.getX() - mLastTouchX);
                    float yDiff = Math.abs(event.getY() - mLastTouchY);
                    if (xDiff >= mMaxMovementAllowed || yDiff >= mMaxMovementAllowed) {
                        mHandler.removeCallbacksAndMessages(null);
                    }
                    break;
                default:
                    mHandler.removeCallbacksAndMessages(null);
                }
                return false;
            }
        };

        // On tablet landscape, the clock frame will be a distinct view.
        // Otherwise, it'll be added
        // on as a header to the main listview.
        mClockFrame = v.findViewById(R.id.main_clock_left_pane);
        // mHairline = v.findViewById(R.id.hairline);
        if (mClockFrame == null) {
            mClockFrame = inflater.inflate(R.layout.tcl_main_clock_frame, mList, false);
            // mHairline = mClockFrame.findViewById(R.id.hairline);
            // mHairline.setVisibility(View.VISIBLE);
            // mList.addHeaderView(mClockFrame, null, false);//delete zouxu
            // 20160905
            mAnalogClock = (AnalogClock) mClockFrame.findViewById(R.id.analog_clock);
            if(mAnalogClock !=null){
                mAnalogClock.setMainClock();
            }
        } else {
            // mHairline.setVisibility(View.GONE);
            // The main clock frame needs its own touch listener for night mode
            // now.
            // v.setOnTouchListener(longPressNightMode);//取消长按进入夜间模式
        }
        // mList.setOnTouchListener(longPressNightMode);//取消长按进入夜间模式

        // If the current layout has a fake overflow menu button, let the parent
        // activity set up its click and touch listeners.

        // View menuButton = v.findViewById(R.id.menu_button);//去掉原生设置入口
        // if (menuButton != null) {
        // setupFakeOverflowMenuButton(menuButton);
        // }

        // mDigitalClock = mClockFrame.findViewById(R.id.digital_clock);
        // Utils.setTimeFormat(getActivity(), (TextClock)
        // mDigitalClock.findViewById(R.id.digital_clock), getResources()
        // .getDimensionPixelSize(R.dimen.main_ampm_font_size));
        footerView = inflater.inflate(R.layout.blank_footer_view, mList, false);
        mList.addFooterView(footerView, null, false);
        mAdapter = new WorldClockAdapter(getActivity(), this);
        if (mAdapter.getCount() == 0) {
            // mHairline.setVisibility(View.GONE);
        }
        mList.setAdapter(mAdapter);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mDefaultClockStyle = getActivity().getResources().getString(R.string.default_clock_style);

        mList.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView arg0, int arg1) {
                
                
                DeskClock m_act =(DeskClock)getActivity();
                
                switch (arg1) {
                case OnScrollListener.SCROLL_STATE_IDLE:// 空闲状态
                    isMoving = false;
                    if(m_act!=null && !m_act.isDeleteMode()){
                        showFabAnim();
                    }
                    break;
//                case OnScrollListener.SCROLL_STATE_FLING:// 滚动状态
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 触摸后滚动
                    isMoving = true;
                    break;
                }
            }

            @Override
            public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
                
                
                DeskClock m_act =(DeskClock)getActivity();
                if(m_act!=null && !m_act.isDeleteMode() && isMoving){
                    hideFabAnim();
                }
            }
        });

        return v;
    }
    
    private boolean isMoving = false;

    @Override
    public void onResume() {
        super.onResume();

        final DeskClock activity = (DeskClock) getActivity();
        if (activity.getSelectedTab() == DeskClock.CLOCK_TAB_INDEX) {
            setFabAppearance();
            setLeftRightButtonAppearance();
        }

        mPrefs.registerOnSharedPreferenceChangeListener(this);
        mDateFormat = getString(R.string.abbrev_wday_month_day_no_year);
        mDateFormatForAccessibility = getString(R.string.full_wday_month_day_no_year);

        ((DeskClock) getActivity()).registerPageChangedListener(this);

        Utils.setQuarterHourUpdater(mHandler, mQuarterHourUpdater);
        // Besides monitoring when quarter-hour changes, monitor other actions
        // that
        // effect clock time
        IntentFilter filter = new IntentFilter();
        filter.addAction(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        activity.registerReceiver(mIntentReceiver, filter);

        // Resume can invoked after changing the cities list or a change in
        // locale
        if (mAdapter != null && isReload) {
            mAdapter.loadCitiesDb(activity);
            mAdapter.reloadData(activity);
            updateWorldView();
            isReload = false;
        }
        // Resume can invoked after changing the clock style.
        // View clockView = Utils.setClockStyle(activity, mDigitalClock,
        // mAnalogClock, SettingsActivity.KEY_CLOCK_STYLE);
        // mClockStyle = (clockView == mDigitalClock ? Utils.CLOCK_TYPE_DIGITAL
        // : Utils.CLOCK_TYPE_ANALOG);

//        mAnalogClock.setVisibility(View.VISIBLE);

        // Center the main clock frame if cities are empty.
        if (getView().findViewById(R.id.main_clock_left_pane) != null && mAdapter.getCount() == 0) {
            mList.setVisibility(View.GONE);
        } else {
            mList.setVisibility(View.VISIBLE);
        }
        mAdapter.notifyDataSetChanged();

        if(select_city_id!=null){//定位到选择的哪个城市

            for(int i=0;i<mAdapter.getCityList().size();i++){
                if(select_city_id.equals(mAdapter.getCityList().get(i).mCityId)){
                    mList.smoothScrollToPosition(i);
                    break;
                }

            }

        }

        // Utils.updateDate(mDateFormat, mDateFormatForAccessibility,
        // mClockFrame);
        // Utils.refreshAlarm(activity, mClockFrame);
        if (PRE_L_DEVICE) {
            activity.getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.NEXT_ALARM_FORMATTED), false, mAlarmObserver);
        }
        updateWorldView();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        Utils.cancelQuarterHourUpdater(mHandler, mQuarterHourUpdater);
        Activity activity = getActivity();
        activity.unregisterReceiver(mIntentReceiver);
        if (PRE_L_DEVICE) {
            activity.getContentResolver().unregisterContentObserver(mAlarmObserver);
        }
        ((DeskClock) getActivity()).unregisterPageChangedListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(BUTTONS_HIDDEN_KEY, mButtonsHidden);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key == SettingsActivity.KEY_CLOCK_STYLE) {
            mClockStyle = prefs.getString(SettingsActivity.KEY_CLOCK_STYLE, mDefaultClockStyle);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFabClick(View view) {

        final Activity activity = getActivity();
        startActivityForResult(new Intent(activity, TCLCitiesActivity.class),0);
    }

    @Override
    public void setFabAppearance() {
        final DeskClock activity = (DeskClock) getActivity();
        if (mFab == null || activity.getSelectedTab() != DeskClock.CLOCK_TAB_INDEX) {
            return;
        }
        
        if(!activity.isDeleteMode()){
            mFab.setVisibility(View.VISIBLE);
        } else {
            mFab.setVisibility(View.GONE);
        }
        mFab.setIconDrawable(getActivity().getResources().getDrawable(R.drawable.ic_add));
//        mFab.setImageResource(R.drawable.ic_add);
        mFab.setContentDescription(getString(R.string.button_cities));
    }

    @Override
    public void setLeftRightButtonAppearance() {
        final DeskClock activity = (DeskClock) getActivity();
        if (mLeftButton == null || mRightButton == null || activity.getSelectedTab() != DeskClock.CLOCK_TAB_INDEX) {
            return;
        }
        mLeftButton.setVisibility(View.INVISIBLE);
        mRightButton.setVisibility(View.INVISIBLE);
    }

    private void setDeleteMode(boolean is) {
        
        if(is){
            clock_delete.setVisibility(View.VISIBLE);
            mList.removeFooterView(footerView);
        } else {
            clock_delete.setVisibility(View.GONE);
            mList.addFooterView(footerView);
        }
        
        DeskClock activity = (DeskClock) getActivity();
        if (activity != null) {
            activity.setDeletMode(is);
        }
    }

    public void selectAll(boolean isSelectAll) {
        mAdapter.setSelectAll(isSelectAll);
        updateSelectCount();
    }

    public void deleteClick() {
        List<CityObj> list = mAdapter.getCityList();
        HashMap<String, CityObj> select_city = new HashMap<String, CityObj>();
        for (int i = 0; i < list.size(); i++) {
            CityObj city = list.get(i);
            if (!city.isSelecte) {
                select_city.put(city.mCityId, city);
            } else {
                // list.remove(city);
                SharePreferencesUtils.setShowThisCity(getActivity(), city.mCityId, false);// 清除显示的城市
            }
        }
        Cities.saveCitiesToSharedPrefs(PreferenceManager.getDefaultSharedPreferences(getActivity()), select_city);
        Intent i = new Intent(Cities.WORLDCLOCK_UPDATE_INTENT);
        getActivity().sendBroadcast(i);
        setDeleteMode(false);
        mAdapter.setDeleteMode(false);
        mAdapter.reloadData(getActivity());
        updateWorldView();
    }

    public void setFragmentDeleteMode(boolean is) {
        mAdapter.setDeleteMode(is);
        if(is){
            clock_delete.setVisibility(View.VISIBLE);
            mList.removeFooterView(footerView);
        } else {
            clock_delete.setVisibility(View.GONE);
            mList.addFooterView(footerView);
        }
    }

    @Override
    public void onItemLongClick(int pos) {
        if (!mAdapter.isDeleteMode()) {
            mAdapter.getCityList().get(pos).isSelecte = true;
            mAdapter.setDeleteMode(true);
            setDeleteMode(true);
            updateSelectCount();
        }
    }

    public void updateSelectCount() {
        List<CityObj> list = mAdapter.getCityList();
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            CityObj city = list.get(i);
            if (city.isSelecte) {
                count++;
            }
        }
        DeskClock activity = (DeskClock) getActivity();
        if (activity != null) {
            activity.updateSelectCount(count, list.size());
        }
    }

    @Override
    public void upateCount() {
        updateSelectCount();
    }

    @Override
    public void onPageChanged(int page) {
        if (page != DeskClock.CLOCK_TAB_INDEX) {
            if (mWorldView != null) {
                mWorldView.setShowLocalTime(true);
            }
        }
    }

    private boolean is_show_fab = true;

    public void hideFabAnim() {
        
        if(mFab == null){
            return;
        }
        
        if (is_show_fab) {
//            ScaleAnimation animation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
//                    Animation.RELATIVE_TO_SELF, 0.5f);
//            animation.setDuration(300);
//            mFab.startAnimation(animation);
//            animation.setAnimationListener(new AnimationListener() {
//
//                @Override
//                public void onAnimationStart(Animation arg0) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation arg0) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animation arg0) {
//                    mFab.setVisibility(View.GONE);
//                }
//            });
            
            mFab.setVisibility(View.GONE);

            is_show_fab = false;
        }
    }

    public void showFabAnim() {

        if(mFab == null || is_show_fab){
            return;
        }
        
//        ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
//                Animation.RELATIVE_TO_SELF, 0.5f);
//        animation.setDuration(300);
//        animation.setAnimationListener(new AnimationListener() {
//
//            @Override
//            public void onAnimationStart(Animation arg0) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation arg0) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation arg0) {
//
//                DeskClock act = (DeskClock) getActivity();
//                if (act != null) {
//                    if (act.getSelectedTab() == DeskClock.STOPWATCH_TAB_INDEX
//                            || act.getSelectedTab() == DeskClock.TIMER_TAB_INDEX) {
//                        mFab.setVisibility(View.GONE);
//                    } else {
//                        mFab.setVisibility(View.VISIBLE);
//                    }
//                }
//            }
//        });
//        mFab.startAnimation(animation);
        
        
        DeskClock act = (DeskClock) getActivity();
        if (act != null) {
            if (act.getSelectedTab() == DeskClock.STOPWATCH_TAB_INDEX
                    || act.getSelectedTab() == DeskClock.TIMER_TAB_INDEX) {
                mFab.setVisibility(View.GONE);
            } else {
                mFab.setVisibility(View.VISIBLE);
            }
        }

        
        is_show_fab = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {//选择城市返回

        if(data == null){
            return;
        }

        select_city_id = data.getStringExtra("select_city_id");

        isReload = true;

        super.onActivityResult(requestCode, resultCode, data);
    }
}
