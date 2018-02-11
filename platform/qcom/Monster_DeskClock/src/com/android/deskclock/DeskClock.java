/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.VisibleForTesting;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.deskclock.alarms.AlarmStateManager;
import com.android.deskclock.events.Events;
import com.android.deskclock.provider.Alarm;
import com.android.deskclock.stopwatch.StopwatchFragment;
import com.android.deskclock.stopwatch.StopwatchService;
import com.android.deskclock.stopwatch.Stopwatches;
import com.android.deskclock.stopwatch.TCLStopwatchFragment;
import com.android.deskclock.timer.TCLTimerFragment;
import com.android.deskclock.timer.TimerFragment;
import com.android.deskclock.timer.TimerObj;
import com.android.deskclock.timer.Timers;
import com.android.deskclock.view.NoScrollViewPager;
import com.android.deskclock.worldclock.CityObj;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

import mst.app.MstActivity;
import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.FloatingActionButton;

/**
 * DeskClock clock view for desk docks.
 */
public class DeskClock extends MstActivity implements LabelDialogFragment.TimerLabelDialogHandler,
        LabelDialogFragment.AlarmLabelDialogHandler, OnClickListener {

    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "DeskClock";

    // Alarm action for midnight (so we can update the date display).
    private static final String KEY_SELECTED_TAB = "selected_tab";
    public static final String SELECT_TAB_INTENT_EXTRA = "deskclock.select.tab";

    // Request code used when SettingsActivity is launched.
    private static final int REQUEST_CHANGE_SETTINGS = 1;

    public static final int ALARM_TAB_INDEX = 0;
    public static final int CLOCK_TAB_INDEX = 1;
    public static final int STOPWATCH_TAB_INDEX = 2;
    public static final int TIMER_TAB_INDEX = 3;

    // Tabs indices are switched for right-to-left since there is no
    // native support for RTL in the ViewPager.
    public static final int RTL_ALARM_TAB_INDEX = 3;
    public static final int RTL_CLOCK_TAB_INDEX = 2;
    public static final int RTL_STOPWATCH_TAB_INDEX = 1;
    public static final int RTL_TIMER_TAB_INDEX = 0;

    // private ActionBar mActionBar;
    private Menu mMenu;
    private NoScrollViewPager mViewPager;
    private FloatingActionButton mFab;
    private ImageButton mLeftButton;
    private ImageButton mRightButton;

    private TabsAdapter mTabsAdapter;
    private int mSelectedTab;
    private boolean mActivityResumed;

    private RelativeLayout alarm_layout;
    private RelativeLayout clock_layout;
    private RelativeLayout stopwatch_layout;
    private RelativeLayout timer_layout;
    private RelativeLayout setting_layout;

    private ImageView img_alarm;
    private ImageView img_clock;
    private ImageView img_stopwatch;
    private ImageView img_timer;

    private LinearLayout actionbar_head_layout;
    private View red_line;
    private int with_5;
    private int off_set_with;
//    private BottomNavigationView clock_delete;

    @Override
    public void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        if (DEBUG)
            Log.d(LOG_TAG, "onNewIntent with intent: " + newIntent);

        // update our intent so that we can consult it to determine whether or
        // not the most recent launch was via a dock event
        setIntent(newIntent);

        // Timer receiver may ask to go to the timers fragment if a timer
        // expired.
        int tab = newIntent.getIntExtra(SELECT_TAB_INTENT_EXTRA, -1);
        if (tab != -1) {
            if (mViewPager != null) {
                // mActionBar.setSelectedNavigationItem(tab);
                mViewPager.setCurrentItem(tab);
            }
        }
    }

    private void initViews() {
        setMstContentView(R.layout.desk_clock);

        red_line = findViewById(R.id.red_line);

        with_5 =  getWindow().getWindowManager().getDefaultDisplay().getWidth()/5;//屏幕５等份
        off_set_with = (with_5 - Utils.dp2px(this,17))/2;//红线的宽度是17

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mLeftButton = (ImageButton) findViewById(R.id.left_button);
        mRightButton = (ImageButton) findViewById(R.id.right_button);

        alarm_layout = (RelativeLayout) findViewById(R.id.alarm_layout);
        clock_layout = (RelativeLayout) findViewById(R.id.clock_layout);
        stopwatch_layout = (RelativeLayout) findViewById(R.id.stopwatch_layout);
        timer_layout = (RelativeLayout) findViewById(R.id.timer_layout);
        setting_layout = (RelativeLayout) findViewById(R.id.setting_layout);

        img_alarm = (ImageView) findViewById(R.id.img_alarm);
        img_clock = (ImageView) findViewById(R.id.img_clock);
        img_stopwatch = (ImageView) findViewById(R.id.img_stopwatch);
        img_timer = (ImageView) findViewById(R.id.img_timer);

        alarm_layout.setOnClickListener(this);
        clock_layout.setOnClickListener(this);
        stopwatch_layout.setOnClickListener(this);
        timer_layout.setOnClickListener(this);
        setting_layout.setOnClickListener(this);

        if (mTabsAdapter == null) {
            mViewPager = (NoScrollViewPager) findViewById(R.id.desk_clock_pager);
            // Keep all four tabs to minimize jank.
            mViewPager.setOffscreenPageLimit(4);
            // Set Accessibility Delegate to null so ViewPager doesn't intercept
            // movements and
            // prevent the fab from being selected.
            mViewPager.setAccessibilityDelegate(null);
            mTabsAdapter = new TabsAdapter(this, mViewPager);
            createTabs(mSelectedTab);
        }

        mFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getSelectedFragment().onFabClick(view);
            }
        });
        mLeftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getSelectedFragment().onLeftButtonClick(view);
            }
        });
        mRightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getSelectedFragment().onRightButtonClick(view);
            }
        });

        // mActionBar.setSelectedNavigationItem(mSelectedTab);
        mViewPager.setCurrentItem(mSelectedTab);

        red_line.setTranslationX(with_5*mSelectedTab+off_set_with);

        actionbar_head_layout = (LinearLayout) findViewById(R.id.actionbar_head_layout);
//        clock_delete = (BottomNavigationView) findViewById(R.id.clock_delete);


//        clock_delete.setNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//
//            @Override
//            public boolean onNavigationItemSelected(MenuItem arg0) {
//
//                deleteClick();
//
//                return false;
//            }
//        });
//        clock_delete.setOnClickListener(this);
        // TimeZone tz = TimeZone.getDefault();
        // CityObj default_city = Utils.getCityByTZId(this, tz.getID());
        getToolbar().setVisibility(View.GONE);
    }

    public boolean isDeleteMode = false;
    
    public boolean isDeleteMode(){
        return isDeleteMode;
    }
    
    public void setDeletMode(boolean is) {
        isDeleteMode = is;

        BottomNavigationView mview;

        if (isDeleteMode) {
//            actionbar_head_layout.setVisibility(View.INVISIBLE);
            showActionMode(true);


            mFab.setVisibility(View.GONE);
//            clock_delete.setVisibility(View.VISIBLE);
        } else {
//            actionbar_head_layout.setVisibility(View.VISIBLE);
            mFab.setVisibility(View.VISIBLE);
//            clock_delete.setVisibility(View.GONE);
            showActionMode(false);
        }

        mViewPager.setNoScroll(isDeleteMode);

    }



    private ActionModeListener listener = new ActionModeListener(){
        @Override
        public void onActionItemClicked(ActionMode.Item item) {

            int id = item.getItemId();

            switch (id){
                case ActionMode.POSITIVE_BUTTON:
                    selectAll(isSelectAll);
                    break;
                case ActionMode.NAGATIVE_BUTTON:
                    setDeletMode(false);
                    switch (mSelectedTab) {
                        case DeskClock.ALARM_TAB_INDEX:
                            final int rtlSafePosition = getRtlPosition(mSelectedTab);
                            final AlarmClockFragment f = (AlarmClockFragment) mTabsAdapter.getItem(rtlSafePosition);
                            f.setDeleteMode(false);
                            break;
                        case DeskClock.CLOCK_TAB_INDEX:
                            final int pos = getRtlPosition(mSelectedTab);
                            final ClockFragment clock_fragment = (ClockFragment) mTabsAdapter.getItem(pos);
                            clock_fragment.setFragmentDeleteMode(false);
                            break;
                    }
                    break;
            }



        }

        @Override
        public void onActionModeShow(ActionMode actionMode) {

        }

        @Override
        public void onActionModeDismiss(ActionMode actionMode) {

        }
    };

    @VisibleForTesting
    DeskClockFragment getSelectedFragment() {
        return (DeskClockFragment) mTabsAdapter.getItem(getRtlPosition(mSelectedTab));
    }

    private void createTabs(int selectedIndex) {
        // mActionBar = getSupportActionBar();

        if (mViewPager != null) {
            // mActionBar.setDisplayOptions(0);
            // mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            // final Tab alarmTab = mActionBar.newTab();
            //
            // alarmTab.setCustomView(R.layout.tcl_tab_layout);
            // //title_alarm = (TextView)
            // alarmTab.getCustomView().findViewById(R.id.text_title);
            // title_alarm.setText(R.string.menu_alarm);
            //
            // alarmTab.setIcon(R.drawable.ic_tab_alarm);
            // alarmTab.setContentDescription(R.string.menu_alarm);
            mTabsAdapter.addTab(img_alarm, Utils.isLOrLater() ? AlarmClockFragmentPostL.class
                    : AlarmClockFragmentPreL.class, ALARM_TAB_INDEX);

            // final Tab clockTab = mActionBar.newTab();
            // clockTab.setCustomView(R.layout.tcl_tab_layout);
            // // title_clock = (TextView)
            // clockTab.getCustomView().findViewById(R.id.text_title);
            // title_clock.setText(R.string.menu_clock);
            //
            // clockTab.setIcon(R.drawable.ic_tab_clock);
            // // clockTab.setContentDescription(R.string.menu_clock);
            mTabsAdapter.addTab(img_clock, ClockFragment.class, CLOCK_TAB_INDEX);

            // final Tab timerTab = mActionBar.newTab();
            //
            // timerTab.setCustomView(R.layout.tcl_tab_layout);
            // //title_timer = (TextView)
            // timerTab.getCustomView().findViewById(R.id.text_title);
            // title_timer.setText(R.string.menu_timer);
            //
            // timerTab.setIcon(R.drawable.ic_tab_timer);
            // // timerTab.setContentDescription(R.string.menu_timer);
            //
            // final Tab stopwatchTab = mActionBar.newTab();
            //
            // stopwatchTab.setCustomView(R.layout.tcl_tab_layout);
            // //title_stopwatch = (TextView)
            // stopwatchTab.getCustomView().findViewById(R.id.text_title);
            // title_stopwatch.setText(R.string.menu_stopwatch);
            //
            // stopwatchTab.setIcon(R.drawable.ic_tab_stopwatch);
            // stopwatchTab.setContentDescription(R.string.menu_stopwatch);
            mTabsAdapter.addTab(img_stopwatch, TCLStopwatchFragment.class, STOPWATCH_TAB_INDEX);
            // mTabsAdapter.addTab(img_stopwatch, StopwatchFragment.class,
            // STOPWATCH_TAB_INDEX);
            // mTabsAdapter.addTab(timerTab, TimerFragment.class,
            // TIMER_TAB_INDEX);
            mTabsAdapter.addTab(img_timer, TCLTimerFragment.class, TIMER_TAB_INDEX);
            // mTabsAdapter.addTab(img_timer, TimerFragment.class,
            // TIMER_TAB_INDEX);
            // mActionBar.setSelectedNavigationItem(selectedIndex);
            mTabsAdapter.notifySelectedPage(selectedIndex);

            // final Tab setTab = mActionBar.newTab();
            // setTab.setIcon(R.drawable.ic_menu_more);
            // setTab.setTabListener(new TabListener() {
            //
            // @Override
            // public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
            // // startActivityForResult(new Intent(DeskClock.this,
            // // SettingsActivity.class),
            // // REQUEST_CHANGE_SETTINGS);
            // }
            //
            // @Override
            // public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
            // startActivityForResult(new Intent(DeskClock.this,
            // TCLClockSettingActivity.class),
            // REQUEST_CHANGE_SETTINGS);
            // }
            //
            // @Override
            // public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
            // startActivityForResult(new Intent(DeskClock.this,
            // TCLClockSettingActivity.class),
            // REQUEST_CHANGE_SETTINGS);
            // }
            // });
            // getSupportActionBar().addTab(setTab);

        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // testAlarm("activity onCreate start ");


        setVolumeControlStream(AudioManager.STREAM_ALARM);

        Utils.setStatusBarColor(this,R.color.white);

        if (icicle != null) {
//            mSelectedTab = icicle.getInt(KEY_SELECTED_TAB, CLOCK_TAB_INDEX);
            mSelectedTab = icicle.getInt(KEY_SELECTED_TAB, ALARM_TAB_INDEX);//chg zouxu 20161026
        } else {
//            mSelectedTab = CLOCK_TAB_INDEX;//chg zouxu 20161026
            mSelectedTab = ALARM_TAB_INDEX;//chg zouxu 20161026

            // Set the background color to initially match the theme value so
            // that we can
            // smoothly transition to the dynamic color.
            // setBackgroundColor(getResources().getColor(R.color.default_background),
            // false /* animate */);
        }

        // Timer receiver may ask the app to go to the timer fragment if a timer
        // expired
        Intent i = getIntent();
        if (i != null) {
            int tab = i.getIntExtra(SELECT_TAB_INTENT_EXTRA, -1);
            if (tab != -1) {
                mSelectedTab = tab;
            }
        }



        initViews();

        setActionModeListener(listener);

        setHomeTimeZone();

        // We need to update the system next alarm time on app startup because
        // the
        // user might have clear our data.
        // testAlarm("activity updateNextAlarm start ");
        AlarmStateManager.updateNextAlarm(this);
        // testAlarm("activity onCreate end ");

    }

    @Override
    protected void onResume() {
        super.onResume();

        // We only want to show notifications for stopwatch/timer when the app
        // is closed so
        // that we don't have to worry about keeping the notifications in
        // perfect sync with
        // the app.
        Intent stopwatchIntent = new Intent(getApplicationContext(), StopwatchService.class);
        stopwatchIntent.setAction(Stopwatches.KILL_NOTIF);
        startService(stopwatchIntent);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Timers.NOTIF_APP_OPEN, true);
        editor.apply();
        Intent timerIntent = new Intent();
        timerIntent.setAction(Timers.NOTIF_IN_USE_CANCEL);
        sendBroadcast(timerIntent);
        mActivityResumed = true;
        selectTab(mSelectedTab);
    }

    @Override
    public void onPause() {
        mActivityResumed = false;
        Intent intent = new Intent(getApplicationContext(), StopwatchService.class);
        intent.setAction(Stopwatches.SHOW_NOTIF);
        startService(intent);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Timers.NOTIF_APP_OPEN, false);
        editor.apply();
        Utils.showInUseNotifications(this);

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // outState.putInt(KEY_SELECTED_TAB,
        // mActionBar.getSelectedNavigationIndex());
        outState.putInt(KEY_SELECTED_TAB, mSelectedTab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // We only want to show it as a menu in landscape, and only for
        // clock/alarm fragment.
        mMenu = menu;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // if (mActionBar.getSelectedNavigationIndex() == ALARM_TAB_INDEX
            // || mActionBar.getSelectedNavigationIndex() == CLOCK_TAB_INDEX) {
            if (mSelectedTab == ALARM_TAB_INDEX || mSelectedTab == CLOCK_TAB_INDEX) {
                // Clear the menu so that it doesn't get duplicate items in case
                // onCreateOptionsMenu
                // was called multiple times.
                menu.clear();
                getMenuInflater().inflate(R.menu.desk_clock_menu, menu);
            }
            // Always return true for landscape, regardless of whether we've
            // inflated the menu, so
            // that when we switch tabs this method will get called and we can
            // inflate the menu.
            return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateMenu(menu);
        return true;
    }

    private void updateMenu(Menu menu) {
        // Hide "help" if we don't have a URI for it.
        MenuItem help = menu.findItem(R.id.menu_item_help);
        if (help != null) {
            Utils.prepareHelpMenuItem(this, help);
        }

        // Hide "lights out" for timer.
        MenuItem nightMode = menu.findItem(R.id.menu_item_night_mode);
        if (mSelectedTab == ALARM_TAB_INDEX) {// mActionBar.getSelectedNavigationIndex()
                                              // == ALARM_TAB_INDEX) {
            nightMode.setVisible(false);
        } else if (mSelectedTab == CLOCK_TAB_INDEX) {// mActionBar.getSelectedNavigationIndex()
                                                     // == CLOCK_TAB_INDEX) {
            nightMode.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (processMenuClick(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Recreate the activity if any settings have been changed
        if (requestCode == REQUEST_CHANGE_SETTINGS && resultCode == RESULT_OK) {
            recreate();
        }
    }

    private boolean processMenuClick(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_item_settings:
            startActivityForResult(new Intent(DeskClock.this, SettingsActivity.class), REQUEST_CHANGE_SETTINGS);
            return true;
        case R.id.menu_item_help:
            Intent i = item.getIntent();
            if (i != null) {
                try {
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    // No activity found to match the intent - ignore
                }
            }
            return true;
        case R.id.menu_item_night_mode:
            startActivity(new Intent(DeskClock.this, ScreensaverActivity.class));
        default:
            break;
        }
        return true;
    }

    /**
     * Insert the local time zone as the Home Time Zone if one is not set
     */
    private void setHomeTimeZone() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String homeTimeZone = prefs.getString(SettingsActivity.KEY_HOME_TZ, "");
        if (!homeTimeZone.isEmpty()) {
            return;
        }
        homeTimeZone = TimeZone.getDefault().getID();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SettingsActivity.KEY_HOME_TZ, homeTimeZone);
        editor.apply();
        Log.v(LOG_TAG, "Setting home time zone to " + homeTimeZone);
    }

    public void registerPageChangedListener(DeskClockFragment frag) {
        if (mTabsAdapter != null) {
            mTabsAdapter.registerPageChangedListener(frag);
        }
    }

    public void unregisterPageChangedListener(DeskClockFragment frag) {
        if (mTabsAdapter != null) {
            mTabsAdapter.unregisterPageChangedListener(frag);
        }
    }

    /**
     * Adapter for wrapping together the ActionBar's tab with the ViewPager
     */
    private class TabsAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

        private static final String KEY_TAB_POSITION = "tab_position";

        final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(Class<?> _class, int position) {
                clss = _class;
                args = new Bundle();
                args.putInt(KEY_TAB_POSITION, position);
            }

            public int getPosition() {
                return args.getInt(KEY_TAB_POSITION, 0);
            }
        }

        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
        // ActionBar mMainActionBar;
        Context mContext;
        ViewPager mPager;
        // Used for doing callbacks to fragments.
        HashSet<String> mFragmentTags = new HashSet<String>();

        public TabsAdapter(Activity activity, ViewPager pager) {
            super(activity.getFragmentManager());
            mContext = activity;
            // mMainActionBar = activity.getSupportActionBar();
            mPager = pager;
            mPager.setAdapter(this);
            mPager.setOnPageChangeListener(this);
        }

        @Override
        public Fragment getItem(int position) {
            // Because this public method is called outside many times,
            // check if it exits first before creating a new one.
            final String name = makeFragmentName(R.id.desk_clock_pager, position);
            Fragment fragment = getFragmentManager().findFragmentByTag(name);
            if (fragment == null) {
                TabInfo info = mTabs.get(getRtlPosition(position));
                fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
                if (fragment instanceof TimerFragment) {
                    ((TimerFragment) fragment).setFabAppearance();
                    ((TimerFragment) fragment).setLeftRightButtonAppearance();
                }
            }
            return fragment;
        }

        /**
         * Copied from:
         * android/frameworks/support/v13/java/android/support/v13/app
         * /FragmentPagerAdapter.java#94 Create unique name for the fragment so
         * fragment manager knows it exist.
         */
        private String makeFragmentName(int viewId, int index) {
            return "android:switcher:" + viewId + ":" + index;
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        public void addTab(ImageView tab, Class<?> clss, int position) {
            TabInfo info = new TabInfo(clss, position);
            tab.setTag(info);
            // tab.setTabListener(this);
            mTabs.add(info);
            // mMainActionBar.addTab(tab);
            notifyDataSetChanged();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // Do nothing
            if(positionOffset!=0)
            {
                red_line.setTranslationX(with_5*(position+positionOffset)+off_set_with);
            }
        }


        @Override
        public void onPageSelected(int position) {
            // Set the page before doing the menu so that onCreateOptionsMenu
            // knows what page it is.
            // mMainActionBar.setSelectedNavigationItem(getRtlPosition(position));
            notifyPageChanged(position);
            selectTab(position);
            mSelectedTab = position;

            // Only show the overflow menu for alarm and world clock.
            if (mMenu != null) {
                // Make sure the menu's been initialized.
                if (position == ALARM_TAB_INDEX || position == CLOCK_TAB_INDEX) {
                    mMenu.setGroupVisible(R.id.menu_items, true);
                    onCreateOptionsMenu(mMenu);
                } else {
                    mMenu.setGroupVisible(R.id.menu_items, false);
                }
            }

            updateSelectFragment();

            red_line.setTranslationX(with_5*position+off_set_with);

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // Do nothing
        }

        // @Override
        // public void onTabReselected(Tab tab, FragmentTransaction arg1) {
        // // Do nothing
        // }

        // @Override
        // public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // final TabInfo info = (TabInfo) tab.getTag();
        // final int position = info.getPosition();
        // final int rtlSafePosition = getRtlPosition(position);
        // mSelectedTab = position;
        // if (mActivityResumed) {
        // switch (mSelectedTab) {
        // case ALARM_TAB_INDEX:
        // Events.sendAlarmEvent(R.string.action_show,
        // R.string.label_deskclock);
        // break;
        // case CLOCK_TAB_INDEX:
        // Events.sendClockEvent(R.string.action_show,
        // R.string.label_deskclock);
        // break;
        // case TIMER_TAB_INDEX:
        // Events.sendTimerEvent(R.string.action_show,
        // R.string.label_deskclock);
        // break;
        // case STOPWATCH_TAB_INDEX:
        // Events.sendStopwatchEvent(R.string.action_show,
        // R.string.label_deskclock);
        // break;
        // }
        // }
        //
        // final DeskClockFragment f = (DeskClockFragment)
        // getItem(rtlSafePosition);
        // if (f != null) {
        // f.setFabAppearance();
        // f.setLeftRightButtonAppearance();
        // }
        // mPager.setCurrentItem(rtlSafePosition);
        // }

        // @Override
        // public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
        // // Do nothing
        // }

        public void notifySelectedPage(int page) {
            notifyPageChanged(page);
        }

        private void notifyPageChanged(int newPage) {
            for (String tag : mFragmentTags) {
                final FragmentManager fm = getFragmentManager();
                DeskClockFragment f = (DeskClockFragment) fm.findFragmentByTag(tag);
                if (f != null) {
                    f.onPageChanged(newPage);
                }
            }
        }

        public void registerPageChangedListener(DeskClockFragment frag) {
            String tag = frag.getTag();
            if (mFragmentTags.contains(tag)) {
                //Log.wtf(LOG_TAG, "Trying to add an existing fragment " + tag);
            } else {
                mFragmentTags.add(frag.getTag());
            }
            // Since registering a listener by the fragment is done sometimes
            // after the page
            // was already changed, make sure the fragment gets the current page
            // frag.onPageChanged(mMainActionBar.getSelectedNavigationIndex());
            frag.onPageChanged(mSelectedTab);
        }

        public void unregisterPageChangedListener(DeskClockFragment frag) {
            mFragmentTags.remove(frag.getTag());
        }

    }

    /**
     * Called by the LabelDialogFormat class after the dialog is finished. *
     */
    @Override
    public void onDialogLabelSet(TimerObj timer, String label, String tag) {
        Fragment frag = getFragmentManager().findFragmentByTag(tag);
        if (frag instanceof TimerFragment) {
            ((TimerFragment) frag).setLabel(timer, label);
        }
    }

    /**
     * Called by the LabelDialogFormat class after the dialog is finished. *
     */
    @Override
    public void onDialogLabelSet(Alarm alarm, String label, String tag) {
        Fragment frag = getFragmentManager().findFragmentByTag(tag);
        if (frag instanceof AlarmClockFragment) {
            ((AlarmClockFragment) frag).setLabel(alarm, label);
        }
    }

    public int getSelectedTab() {
        return mSelectedTab;
    }

    private boolean isRtl() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL;
    }

    private int getRtlPosition(int position) {
        if (isRtl()) {
            switch (position) {
            case TIMER_TAB_INDEX:
                return RTL_TIMER_TAB_INDEX;
            case CLOCK_TAB_INDEX:
                return RTL_CLOCK_TAB_INDEX;
            case STOPWATCH_TAB_INDEX:
                return RTL_STOPWATCH_TAB_INDEX;
            case ALARM_TAB_INDEX:
                return RTL_ALARM_TAB_INDEX;
            default:
                break;
            }
        }
        return position;
    }

    public FloatingActionButton getFab() {
        return mFab;
    }

    public ImageButton getLeftButton() {
        return mLeftButton;
    }

    public ImageButton getRightButton() {
        return mRightButton;
    }

    private void selectTab(int pos) {
        switch (pos) {
        case ALARM_TAB_INDEX:

            img_alarm.setAlpha(1.0f);
            img_clock.setAlpha(0.25f);
            img_stopwatch.setAlpha(0.25f);
            img_timer.setAlpha(0.25f);
//
//            img_alarm.setImageResource(R.drawable.ic_alarm_select);
//            img_clock.setImageResource(R.drawable.ic_language_normal);
//            img_stopwatch.setImageResource(R.drawable.ic_stopwatch_normal);
//            img_timer.setImageResource(R.drawable.ic_count_down_normal);

            break;
        case CLOCK_TAB_INDEX:
//            img_alarm.setImageResource(R.drawable.ic_alarm_normal);
//            img_clock.setImageResource(R.drawable.ic_language_select);
//            img_stopwatch.setImageResource(R.drawable.ic_stopwatch_normal);
//            img_timer.setImageResource(R.drawable.ic_count_down_normal);

            img_alarm.setAlpha(0.25f);
            img_clock.setAlpha(1.0f);
            img_stopwatch.setAlpha(0.25f);
            img_timer.setAlpha(0.25f);

            break;
        case STOPWATCH_TAB_INDEX:
//            img_alarm.setImageResource(R.drawable.ic_alarm_normal);
//            img_clock.setImageResource(R.drawable.ic_language_normal);
//            img_stopwatch.setImageResource(R.drawable.ic_stopwatch_select);
//            img_timer.setImageResource(R.drawable.ic_count_down_normal);
            img_alarm.setAlpha(0.25f);
            img_clock.setAlpha(0.25f);
            img_stopwatch.setAlpha(1.0f);
            img_timer.setAlpha(0.25f);

            break;
        case TIMER_TAB_INDEX:
//            img_alarm.setImageResource(R.drawable.ic_alarm_normal);
//            img_clock.setImageResource(R.drawable.ic_language_normal);
//            img_stopwatch.setImageResource(R.drawable.ic_stopwatch_normal);
//            img_timer.setImageResource(R.drawable.ic_count_down_select);

            img_alarm.setAlpha(0.25f);
            img_clock.setAlpha(0.25f);
            img_stopwatch.setAlpha(0.25f);
            img_timer.setAlpha(1.0f);

            break;
        }
    }

    @Override
    public void onClick(View arg0) {

        int id = arg0.getId();
        switch (id) {
        case R.id.alarm_layout:
            mSelectedTab = ALARM_TAB_INDEX;
            Events.sendAlarmEvent(R.string.action_show, R.string.label_deskclock);
            break;
        case R.id.clock_layout:
            mSelectedTab = CLOCK_TAB_INDEX;
            Events.sendClockEvent(R.string.action_show, R.string.label_deskclock);
            break;
        case R.id.stopwatch_layout:
            mSelectedTab = STOPWATCH_TAB_INDEX;
            Events.sendTimerEvent(R.string.action_show, R.string.label_deskclock);
            break;
        case R.id.timer_layout:
            mSelectedTab = TIMER_TAB_INDEX;
            Events.sendStopwatchEvent(R.string.action_show, R.string.label_deskclock);
            break;
        case R.id.setting_layout:
            startActivityForResult(new Intent(DeskClock.this, TCLClockSettingActivity.class), REQUEST_CHANGE_SETTINGS);
            return;
        }
        updateSelectFragment();

    }

    private void updateSelectFragment() {
        final int rtlSafePosition = getRtlPosition(mSelectedTab);

        final DeskClockFragment f = (DeskClockFragment) mTabsAdapter.getItem(rtlSafePosition);
        if (f != null) {
            f.setFabAppearance();
            f.setLeftRightButtonAppearance();
        }
        mViewPager.setCurrentItem(rtlSafePosition);
    }

    private boolean isSelectAll;

    public void updateSelectCount(int count, int allCount) {
        getActionMode().setTitle(String.format(getString(R.string.str_have_select),count));
        if (count == allCount) {
            getActionMode().setPositiveText(getString(R.string.str_not_select_all));
        } else {
            getActionMode().setPositiveText(getString(R.string.str_select_all));
        }
        isSelectAll = count == allCount ? false : true;
    }

    private void selectAll(boolean isSelectAll) {
        switch (mSelectedTab) {
        case DeskClock.ALARM_TAB_INDEX:
            final int rtlSafePosition = getRtlPosition(mSelectedTab);
            final AlarmClockFragment f = (AlarmClockFragment) mTabsAdapter.getItem(rtlSafePosition);
            f.selectAll(isSelectAll);
            break;
        case DeskClock.CLOCK_TAB_INDEX:
            final int pos = getRtlPosition(mSelectedTab);
            final ClockFragment clock_fragment = (ClockFragment) mTabsAdapter.getItem(pos);
            clock_fragment.selectAll(isSelectAll);
            break;
        }
    }

    private void deleteClick() {
        switch (mSelectedTab) {
        case DeskClock.ALARM_TAB_INDEX:
            final int rtlSafePosition = getRtlPosition(mSelectedTab);
            final AlarmClockFragment f = (AlarmClockFragment) mTabsAdapter.getItem(rtlSafePosition);
            f.deleteClick();
            break;
        case DeskClock.CLOCK_TAB_INDEX:
            final int pos = getRtlPosition(mSelectedTab);
            final ClockFragment clock_fragment = (ClockFragment) mTabsAdapter.getItem(pos);
            clock_fragment.deleteClick();
            break;
        }
    }
    // private void testAlarm(String tag){
    // String alarm = Utils.getNextAlarm(this);
    // if(alarm != null){
    // Log.i("testAlarm", tag+" alarm = "+alarm);
    // } else {
    // Log.i("testAlarm", tag+" alarm == null");
    // }
    // }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(isDeleteMode() && keyCode == KeyEvent.KEYCODE_BACK){
            setDeletMode(false);
            switch (mSelectedTab) {
                case DeskClock.ALARM_TAB_INDEX:
                    final int rtlSafePosition = getRtlPosition(mSelectedTab);
                    final AlarmClockFragment f = (AlarmClockFragment) mTabsAdapter.getItem(rtlSafePosition);
                    f.setDeleteMode(false);
                    break;
                case DeskClock.CLOCK_TAB_INDEX:
                    final int pos = getRtlPosition(mSelectedTab);
                    final ClockFragment clock_fragment = (ClockFragment) mTabsAdapter.getItem(pos);
                    clock_fragment.setFragmentDeleteMode(false);
                    break;
            }

            return true;

        }

        return super.onKeyDown(keyCode, event);
    }
}
