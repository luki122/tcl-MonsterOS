/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.transition.AutoTransition;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AbsListView.OnScrollListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.android.deskclock.Util.AppConst;
import com.android.deskclock.Util.SharePreferencesUtils;
import com.android.deskclock.alarms.AlarmStateManager;
import com.android.deskclock.events.Events;
import com.android.deskclock.net.tcl.LegalHolidayTask;
import com.android.deskclock.net.tcl.UpdateCallBack;
import com.android.deskclock.provider.Alarm;
import com.android.deskclock.provider.AlarmInstance;
import com.android.deskclock.provider.DaysOfWeek;
import com.android.deskclock.widget.ActionableToastBar;
import com.android.deskclock.widget.TextTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;

import mst.view.menu.bottomnavigation.BottomNavigationView;
import mst.widget.MstCheckListAdapter;

/**
 * AlarmClock application.
 */
public abstract class AlarmClockFragment extends DeskClockFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnTouchListener, UpdateCallBack {
    private static final float EXPAND_DECELERATION = 1f;
    private static final float COLLAPSE_DECELERATION = 0.7f;

    private static final int ANIMATION_DURATION = 300;
    private static final int EXPAND_DURATION = 300;
    private static final int COLLAPSE_DURATION = 250;

    private static final int ROTATE_180_DEGREE = 180;
    private static final float ALARM_ELEVATION = 8f;
    private static final float TINTED_LEVEL = 0.09f;

    private static final String KEY_EXPANDED_ID = "expandedId";
    private static final String KEY_REPEAT_CHECKED_IDS = "repeatCheckedIds";
    private static final String KEY_RINGTONE_TITLE_CACHE = "ringtoneTitleCache";
    private static final String KEY_SELECTED_ALARMS = "selectedAlarms";
    private static final String KEY_DELETED_ALARM = "deletedAlarm";
    private static final String KEY_UNDO_SHOWING = "undoShowing";
    private static final String KEY_PREVIOUS_DAY_MAP = "previousDayMap";
    private static final String KEY_SELECTED_ALARM = "selectedAlarm";

    private static final int REQUEST_CODE_RINGTONE = 1;
    public static final int REQUEST_CODE_PERMISSIONS = 2;
    private static final long INVALID_ID = -1;
    public static final String PREF_KEY_DEFAULT_ALARM_RINGTONE_URI = "default_alarm_ringtone_uri";

    // Use transitions only in API 21+
    private static final boolean USE_TRANSITION_FRAMEWORK = Utils.isLOrLater();

    // This extra is used when receiving an intent to create an alarm, but no
    // alarm details
    // have been passed in, so the alarm page should start the process of
    // creating a new alarm.
    public static final String ALARM_CREATE_NEW_INTENT_EXTRA = "deskclock.create.new";

    // This extra is used when receiving an intent to scroll to specific alarm.
    // If alarm
    // can not be found, and toast message will pop up that the alarm has be
    // deleted.
    public static final String SCROLL_TO_ALARM_INTENT_EXTRA = "deskclock.scroll.to.alarm";

    private FrameLayout mMainLayout;
    private ListView mAlarmsList;
    private AlarmItemAdapter mAdapter;
    private View mEmptyView;
    private View mFooterView;

    private Bundle mRingtoneTitleCache; // Key: ringtone uri, value: ringtone
    // title
    private ActionableToastBar mUndoBar;
    private View mUndoFrame;

    protected Alarm mSelectedAlarm;
    protected long mScrollToAlarmId = INVALID_ID;

    private Loader mCursorLoader = null;

    // Saved states for undo
    private Alarm mDeletedAlarm;
    protected Alarm mAddedAlarm;
    private boolean mUndoShowing;

    private Interpolator mExpandInterpolator;
    private Interpolator mCollapseInterpolator;

    private Transition mAddRemoveTransition;
    private Transition mRepeatTransition;
    private Transition mEmptyViewTransition;

    // Abstract methods to to be overridden by for post- and pre-L
    // implementations as necessary
    protected abstract void setTimePickerListener();

    protected abstract void showTimeEditDialog(Alarm alarm);

    protected abstract void startCreatingAlarm();

    private boolean disable_expend = true;// 关闭原生的展开界面 zouxu
    private HashMap<Long, TextView> alarmIdTextViewMap = new HashMap<Long, TextView>();
    private HashMap<Long, Alarm> alarmIdAlarmMap = new HashMap<Long, Alarm>();
    // private HashMap<Alarm, Boolean> deleteAlarmIdAlarmMap = new
    // HashMap<Alarm, Boolean>();
    private List<Alarm> deleteAlarmList = new ArrayList<Alarm>();


    private boolean isShowWhenToAlarm = false;//是否显示多少分钟后响铃 20160918

    private BottomNavigationView clock_delete;
    private View list_footer_view;

    protected void processTimeSet(int hourOfDay, int minute) {
        if (mSelectedAlarm == null) {
            // If mSelectedAlarm is null then we're creating a new alarm.
            Alarm a = new Alarm();
            a.alert = getDefaultRingtoneUri();
            if (a.alert == null) {
                a.alert = Uri.parse("content://settings/system/alarm_alert");
            }
            a.hour = hourOfDay;
            a.minutes = minute;
            a.enabled = true;

            mAddedAlarm = a;
            asyncAddAlarm(a);
        } else {
            mSelectedAlarm.hour = hourOfDay;
            mSelectedAlarm.minutes = minute;
            mSelectedAlarm.enabled = true;
            mScrollToAlarmId = mSelectedAlarm.id;
            asyncUpdateAlarm(mSelectedAlarm, true);
            mSelectedAlarm = null;
        }
    }

    public AlarmClockFragment() {
        // Basic provider required by Fragment.java
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mCursorLoader = getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        // Inflate the layout for this fragment

        final View v = inflater.inflate(R.layout.alarm_clock, container, false);

        long expandedId = INVALID_ID;
        long[] repeatCheckedIds = null;
        long[] selectedAlarms = null;
        Bundle previousDayMap = null;
        if (savedState != null) {
            expandedId = savedState.getLong(KEY_EXPANDED_ID);
            repeatCheckedIds = savedState.getLongArray(KEY_REPEAT_CHECKED_IDS);
            mRingtoneTitleCache = savedState.getBundle(KEY_RINGTONE_TITLE_CACHE);
            mDeletedAlarm = savedState.getParcelable(KEY_DELETED_ALARM);
            mUndoShowing = savedState.getBoolean(KEY_UNDO_SHOWING);
            selectedAlarms = savedState.getLongArray(KEY_SELECTED_ALARMS);
            previousDayMap = savedState.getBundle(KEY_PREVIOUS_DAY_MAP);
            mSelectedAlarm = savedState.getParcelable(KEY_SELECTED_ALARM);
        }

        mExpandInterpolator = new DecelerateInterpolator(EXPAND_DECELERATION);
        mCollapseInterpolator = new DecelerateInterpolator(COLLAPSE_DECELERATION);

        if (USE_TRANSITION_FRAMEWORK) {
            mAddRemoveTransition = new AutoTransition();
            mAddRemoveTransition.setDuration(ANIMATION_DURATION);

            mRepeatTransition = new AutoTransition();
            mRepeatTransition.setDuration(ANIMATION_DURATION / 2);
            mRepeatTransition.setInterpolator(new AccelerateDecelerateInterpolator());

            mEmptyViewTransition = new TransitionSet().setOrdering(TransitionSet.ORDERING_SEQUENTIAL)
                    .addTransition(new Fade(Fade.OUT)).addTransition(new Fade(Fade.IN)).setDuration(ANIMATION_DURATION);
        }

        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        // View menuButton = v.findViewById(R.id.menu_button); //去掉原生设置入口
        // if (menuButton != null) {
        // if (isLandscape) {
        // menuButton.setVisibility(View.GONE);
        // } else {
        // menuButton.setVisibility(View.VISIBLE);
        // setupFakeOverflowMenuButton(menuButton);
        // }
        // }
        list_footer_view = LayoutInflater.from(getActivity()).inflate(R.layout.blank_footer_view, container, false);
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


        mEmptyView = v.findViewById(R.id.alarms_empty_view);

        mMainLayout = (FrameLayout) v.findViewById(R.id.main);
        mAlarmsList = (ListView) v.findViewById(R.id.alarms_list);

        mUndoBar = (ActionableToastBar) v.findViewById(R.id.undo_bar);
        mUndoFrame = v.findViewById(R.id.undo_frame);
        mUndoFrame.setOnTouchListener(this);

        mFooterView = v.findViewById(R.id.alarms_footer_view);
        mFooterView.setOnTouchListener(this);

        mAdapter = new AlarmItemAdapter(getActivity(), expandedId, repeatCheckedIds, selectedAlarms, previousDayMap,
                mAlarmsList);
        mAdapter.registerDataSetObserver(new DataSetObserver() {

            private int prevAdapterCount = -1;

            @Override
            public void onChanged() {
                Log.i("zouxu", "DataSetObserver onChanged");

                final int count = mAdapter.getCount();
                if (mDeletedAlarm != null && prevAdapterCount > count) {
                    showUndoBar();
                }

                if (USE_TRANSITION_FRAMEWORK && ((count == 0 && prevAdapterCount > 0) || /*
                                                                                          * should
                                                                                          * fade
                                                                                          * in
                                                                                          */
                        (count > 0 && prevAdapterCount == 0) /* should fade out */)) {
                    TransitionManager.beginDelayedTransition(mMainLayout, mEmptyViewTransition);
                }
                mEmptyView.setVisibility(count == 0 ? View.VISIBLE : View.GONE);

                // Cache this adapter's count for when the adapter changes.
                prevAdapterCount = count;
                super.onChanged();
            }
        });

        if (mRingtoneTitleCache == null) {
            mRingtoneTitleCache = new Bundle();
        }

        mAlarmsList.setAdapter(mAdapter);
        mAlarmsList.addFooterView(list_footer_view);
        mAlarmsList.setVerticalScrollBarEnabled(true);
        mAlarmsList.setOnCreateContextMenuListener(this);

        if (mUndoShowing) {
            showUndoBar();
        }
        getDataFromNet();

        mAlarmsList.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView arg0, int arg1) {

                DeskClock m_act = (DeskClock) getActivity();

                switch (arg1) {
                    case OnScrollListener.SCROLL_STATE_IDLE:// 空闲状态
                        isMoving = false;
                        if (m_act != null && !m_act.isDeleteMode()) {
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
                DeskClock m_act = (DeskClock) getActivity();
                if (m_act != null && !m_act.isDeleteMode() && isMoving) {
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
        if (activity.getSelectedTab() == DeskClock.ALARM_TAB_INDEX) {
            setFabAppearance();
            setLeftRightButtonAppearance();
        }

        // Check if another app asked us to create a blank new alarm.
        final Intent intent = getActivity().getIntent();
        if (intent.hasExtra(ALARM_CREATE_NEW_INTENT_EXTRA)) {
            if (intent.getBooleanExtra(ALARM_CREATE_NEW_INTENT_EXTRA, false)) {
                // An external app asked us to create a blank alarm.
                startCreatingAlarm();
            }

            // Remove the CREATE_NEW extra now that we've processed it.
            intent.removeExtra(ALARM_CREATE_NEW_INTENT_EXTRA);
        } else if (intent.hasExtra(SCROLL_TO_ALARM_INTENT_EXTRA)) {
            long alarmId = intent.getLongExtra(SCROLL_TO_ALARM_INTENT_EXTRA, Alarm.INVALID_ID);
            if (alarmId != Alarm.INVALID_ID) {
                mScrollToAlarmId = alarmId;
                if (mCursorLoader != null && mCursorLoader.isStarted()) {
                    // We need to force a reload here to make sure we have the
                    // latest view
                    // of the data to scroll to.
                    mCursorLoader.forceLoad();
                }
            }

            // Remove the SCROLL_TO_ALARM extra now that we've processed it.
            intent.removeExtra(SCROLL_TO_ALARM_INTENT_EXTRA);
        }
        ((DeskClock) getActivity()).registerPageChangedListener(this);

        setTimePickerListener();

        IntentFilter filter = new IntentFilter();
        // filter.addAction(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        activity.registerReceiver(mIntentReceiver, filter);
        updateAllWhenTime();
    }

    private void hideUndoBar(boolean animate, MotionEvent event) {
        if (mUndoBar != null) {
            mUndoFrame.setVisibility(View.GONE);
            if (event != null && mUndoBar.isEventInToastBar(event)) {
                // Avoid touches inside the undo bar.
                return;
            }
            mUndoBar.hide(animate);
        }
        mDeletedAlarm = null;
        mUndoShowing = false;
    }

    private void showUndoBar() {
        if (true) {// 不显示撤销按钮
            return;
        }
        final Alarm deletedAlarm = mDeletedAlarm;
        mUndoFrame.setVisibility(View.VISIBLE);
        mUndoBar.show(new ActionableToastBar.ActionClickedListener() {
            @Override
            public void onActionClicked() {
                mAddedAlarm = deletedAlarm;
                mDeletedAlarm = null;
                mUndoShowing = false;

                asyncAddAlarm(deletedAlarm);
            }
        }, 0, getResources().getString(R.string.alarm_deleted), true, R.string.alarm_undo, true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_EXPANDED_ID, mAdapter.getExpandedId());
        outState.putLongArray(KEY_REPEAT_CHECKED_IDS, mAdapter.getRepeatArray());
        outState.putLongArray(KEY_SELECTED_ALARMS, mAdapter.getSelectedAlarmsArray());
        outState.putBundle(KEY_RINGTONE_TITLE_CACHE, mRingtoneTitleCache);
        outState.putParcelable(KEY_DELETED_ALARM, mDeletedAlarm);
        outState.putBoolean(KEY_UNDO_SHOWING, mUndoShowing);
        outState.putBundle(KEY_PREVIOUS_DAY_MAP, mAdapter.getPreviousDaysOfWeekMap());
        outState.putParcelable(KEY_SELECTED_ALARM, mSelectedAlarm);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ToastMaster.cancelToast();
    }

    @Override
    public void onPause() {
        super.onPause();
        // When the user places the app in the background by pressing "home",
        // dismiss the toast bar. However, since there is no way to determine if
        // home was pressed, just dismiss any existing toast bar when restarting
        // the app.
        hideUndoBar(false, null);
        Activity activity = getActivity();
        activity.unregisterReceiver(mIntentReceiver);
    }

    private void showLabelDialog(final Alarm alarm) {
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment prev = getFragmentManager().findFragmentByTag("label_dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        final LabelDialogFragment newFragment = LabelDialogFragment.newInstance(alarm, alarm.label, getTag());
        newFragment.show(ft, "label_dialog");
    }

    public void setLabel(Alarm alarm, String label) {
        alarm.label = label;
        asyncUpdateAlarm(alarm, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return Alarm.getAlarmsCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, final Cursor data) {

        mAdapter.swapCursor(data);
        if (mScrollToAlarmId != INVALID_ID) {
            scrollToAlarm(mScrollToAlarmId);
            mScrollToAlarmId = INVALID_ID;
        }
        //updateAllAlarmReal();//add zouxu 20161123
    }

    private boolean is_update_all_alrm = true;//一键换机后更新数据

    public void updateAllAlarmReal(){
        if(is_update_all_alrm){
            is_update_all_alrm = false;
            Cursor mCouCursor = mAdapter.getCursor();
            int old_pos = mCouCursor.getPosition();
            boolean is = mCouCursor.moveToFirst();
            while (is) {
                Alarm mAlarm = new Alarm(mCouCursor);
                asyncUpdateAlarm(mAlarm,false);
                if (!mCouCursor.moveToNext()) {
                    break;
                }
            }
            mCouCursor.moveToPosition(old_pos);
        }
    }


    /**
     * Scroll to alarm with given alarm id.
     *
     * @param alarmId The alarm id to scroll to.
     */
    private void scrollToAlarm(long alarmId) {
        int alarmPosition = -1;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            long id = mAdapter.getItemId(i);
            if (id == alarmId) {
                alarmPosition = i;
                break;
            }
        }

        if (alarmPosition >= 0) {
            mAdapter.setNewAlarm(alarmId);
            mAlarmsList.smoothScrollToPositionFromTop(alarmPosition, 0);
        } else {
            // Trying to display a deleted alarm should only happen from a
            // missed notification for
            // an alarm that has been marked deleted after use.
            Context context = getActivity().getApplicationContext();
            Toast toast = Toast.makeText(context, R.string.missed_alarm_has_been_deleted, Toast.LENGTH_LONG);
            ToastMaster.setToast(toast);
            toast.show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }

    private void launchRingTonePicker(Alarm alarm) {
        mSelectedAlarm = alarm;
        Uri oldRingtone = Alarm.NO_RINGTONE_URI.equals(alarm.alert) ? null : alarm.alert;
        final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, oldRingtone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
        startActivityForResult(intent, REQUEST_CODE_RINGTONE);
    }

    private void saveRingtoneUri(Intent intent) {
        Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        if (uri == null) {
            uri = Alarm.NO_RINGTONE_URI;
        }
        mSelectedAlarm.alert = uri;

        // Save the last selected ringtone as the default for new alarms
        setDefaultRingtoneUri(uri);

        asyncUpdateAlarm(mSelectedAlarm, false);

        // If the user chose an external ringtone and has not yet granted the
        // permission to read
        // external storage, ask them for that permission now.
        if (!AlarmUtils.hasPermissionToDisplayRingtoneTitle(getActivity(), uri)) {
            final String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(perms, REQUEST_CODE_PERMISSIONS);
        }
    }

    private Uri getDefaultRingtoneUri() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String ringtoneUriString = sp.getString(PREF_KEY_DEFAULT_ALARM_RINGTONE_URI, null);

        final Uri ringtoneUri;
        if (ringtoneUriString != null) {
            ringtoneUri = Uri.parse(ringtoneUriString);
        } else {
            ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getActivity(), RingtoneManager.TYPE_ALARM);
        }

        return ringtoneUri;
    }

    private void setDefaultRingtoneUri(Uri uri) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (uri == null) {
            sp.edit().remove(PREF_KEY_DEFAULT_ALARM_RINGTONE_URI).apply();
        } else {
            sp.edit().putString(PREF_KEY_DEFAULT_ALARM_RINGTONE_URI, uri.toString()).apply();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // The permission change may alter the cached ringtone titles so clear
        // them.
        // (e.g. READ_EXTERNAL_STORAGE is granted or revoked)
        mRingtoneTitleCache.clear();
    }

    private class AlarmItemAdapter extends CursorAdapter {
        private final Context mContext;
        private final LayoutInflater mFactory;
        private final Typeface mRobotoNormal;
        private final ListView mList;

        private long mExpandedId;
        private ItemHolder mExpandedItemHolder;
        private final HashSet<Long> mRepeatChecked = new HashSet<>();
        private final HashSet<Long> mSelectedAlarms = new HashSet<>();
        private Bundle mPreviousDaysOfWeekMap = new Bundle();

        private final boolean mHasVibrator;
        private final int mCollapseExpandHeight;

        private boolean isDeleteMode = false;

        private MstCheckListAdapter mProxy;

        public boolean isDeleteMode() {
            return isDeleteMode;
        }

        public void setDeleteMode(boolean deleteMode) {


            Log.i("zouxu","setDeleteMode 1 ! deleteMode="+deleteMode);
            mProxy.setChecked(deleteMode);

            if (deleteMode) {
                clock_delete.setVisibility(View.VISIBLE);
//                list_footer_view.setVisibility(View.GONE);
                mAlarmsList.removeFooterView(list_footer_view);
            } else {
                clock_delete.setVisibility(View.GONE);
//                list_footer_view.setVisibility(View.VISIBLE);
                mAlarmsList.addFooterView(list_footer_view);

            }


            isDeleteMode = deleteMode;
            DeskClock activity = (DeskClock) getActivity();
            if (activity != null) {
                activity.setDeletMode(deleteMode);
            }
            // deleteAlarmIdAlarmMap.clear();
            deleteAlarmList.clear();
            notifyDataSetChanged();
        }

        // Determines the order that days of the week are shown in the UI
        private int[] mDayOrder;

        // A reference used to create mDayOrder
        private final int[] DAY_ORDER = new int[]{Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
                Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY,};

        public class ItemHolder {

            // views for optimization
            LinearLayout alarmItem;
            LinearLayout slide_view;
            TextTime clock;
            TextView tomorrowLabel;
            Switch onoff;
            TextView daysOfWeek;
            TextView label;
            // View summary;
            // TextView clickableLabel;
            // LinearLayout repeatDays;
            // View hairLine;
            View collapseExpandArea;
            TextView text_when_to_alarm;
            CheckBox select_checkbox;

            // Other states
            Alarm alarm;
        }

        // Used for scrolling an expanded item in the list to make sure it is
        // fully visible.
        private long mScrollAlarmId = AlarmClockFragment.INVALID_ID;
        private final Runnable mScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (mScrollAlarmId != AlarmClockFragment.INVALID_ID) {
                    View v = getViewById(mScrollAlarmId);
                    if (v != null) {
                        Rect rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                        mList.requestChildRectangleOnScreen(v, rect, false);
                    }
                    mScrollAlarmId = AlarmClockFragment.INVALID_ID;
                }
            }
        };

        public AlarmItemAdapter(Context context, long expandedId, long[] repeatCheckedIds, long[] selectedAlarms,
                                Bundle previousDaysOfWeekMap, ListView list) {
            super(context, null, 0);
            mContext = context;
            mFactory = LayoutInflater.from(context);
            mList = list;

            Resources res = mContext.getResources();

            mRobotoNormal = Typeface.create("sans-serif", Typeface.NORMAL);

            mExpandedId = expandedId;
            if (repeatCheckedIds != null) {
                buildHashSetFromArray(repeatCheckedIds, mRepeatChecked);
            }
            if (previousDaysOfWeekMap != null) {
                mPreviousDaysOfWeekMap = previousDaysOfWeekMap;
            }
            if (selectedAlarms != null) {
                buildHashSetFromArray(selectedAlarms, mSelectedAlarms);
            }

            mHasVibrator = ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).hasVibrator();

            mCollapseExpandHeight = (int) res.getDimension(R.dimen.collapse_expand_height);

            setDayOrder();

            mProxy = new MstCheckListAdapter(){
                @Override
                protected View getCheckBox(int position,View itemView) {
                    ItemHolder viewHodler = (ItemHolder) itemView.getTag();
                    return viewHodler.select_checkbox;
                }

                @Override
                protected View getMoveView(int position,View itemView) {
                    ItemHolder viewHodler = (ItemHolder) itemView.getTag();
                    return viewHodler.slide_view;
                }

                @Override
                public int getCount() {
                    return 1;
                }

                @Override
                protected void onBindView(int position, View convertView) {

                }

                @Override
                public Object getItem(int position) {
                    return null;
                }

                @Override
                public long getItemId(int position) {
                    return 1;
                }

                @Override
                protected View onCreateView(int position, ViewGroup parent) {
                    return null;
                }
            };
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (!getCursor().moveToPosition(position)) {
                // May happen if the last alarm was deleted and the cursor
                // refreshed while the
                // list is updated.
                LogUtils.v("couldn't move cursor to position " + position);
                return null;
            }
            View v;
            if (convertView == null) {
                v = newView(mContext, getCursor(), parent);
            } else {
                v = convertView;
            }
            bindView(v, mContext, getCursor());

            mProxy.getView(position,v,parent);


            return v;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View view = mFactory.inflate(R.layout.alarm_time, parent, false);
            setNewHolder(view);
            return view;
        }

        /**
         * In addition to changing the data set for the alarm list, swapCursor
         * is now also responsible for preparing the transition for any
         * added/removed items.
         */
        @Override
        public synchronized Cursor swapCursor(Cursor cursor) {
            if (USE_TRANSITION_FRAMEWORK && (mAddedAlarm != null || mDeletedAlarm != null)) {
                TransitionManager.beginDelayedTransition(mAlarmsList, mAddRemoveTransition);
            }

            final Cursor c = super.swapCursor(cursor);

            mAddedAlarm = null;
            mDeletedAlarm = null;

            return c;
        }

        private void setDayOrder() {
            // Value from preferences corresponds to Calendar.<WEEKDAY> value
            // -1 in order to correspond to DAY_ORDER indexing
            final int startDay = Utils.getZeroIndexedFirstDayOfWeek(mContext);
            mDayOrder = new int[DaysOfWeek.DAYS_IN_A_WEEK];

            for (int i = 0; i < DaysOfWeek.DAYS_IN_A_WEEK; ++i) {
                mDayOrder[i] = DAY_ORDER[(startDay + i) % 7];
            }
        }

        private ItemHolder setNewHolder(View view) {
            // standard view holder optimization
            final ItemHolder holder = new ItemHolder();
            holder.alarmItem = (LinearLayout) view.findViewById(R.id.alarm_item);
            holder.slide_view = (LinearLayout) view.findViewById(R.id.slide_view);
            holder.tomorrowLabel = (TextView) view.findViewById(R.id.tomorrowLabel);
            holder.clock = (TextTime) view.findViewById(R.id.digital_clock);
            holder.onoff = (Switch) view.findViewById(R.id.onoff);
            holder.onoff.setTypeface(mRobotoNormal);
            // holder.onoff.setTrackResource(R.drawable.switch_track_selector);
            // holder.onoff.setThumbResource(R.drawable.switch_thumb_selector);
            holder.daysOfWeek = (TextView) view.findViewById(R.id.daysOfWeek);
            holder.label = (TextView) view.findViewById(R.id.label);
            // holder.summary = view.findViewById(R.id.summary);
            // holder.hairLine = view.findViewById(R.id.hairline);
            // holder.repeatDays = (LinearLayout)
            // view.findViewById(R.id.repeat_days);
            holder.collapseExpandArea = view.findViewById(R.id.collapse_expand);
            holder.text_when_to_alarm = (TextView) view.findViewById(R.id.text_when_to_alarm);
            holder.select_checkbox = (CheckBox) view.findViewById(R.id.select_checkbox);

            // Build button for each day.
            // for (int i = 0; i < 7; i++) {
            // final CompoundButton dayButton = (CompoundButton)
            // mFactory.inflate(R.layout.day_button,
            // holder.repeatDays, false /* attachToRoot */);
            // final int firstDay =
            // Utils.getZeroIndexedFirstDayOfWeek(mContext);
            // dayButton.setText(Utils.getShortWeekday(i, firstDay));
            // dayButton.setContentDescription(Utils.getLongWeekday(i,
            // firstDay));
            // holder.repeatDays.addView(dayButton);
            // }

            view.setTag(holder);// 测试不缓存
            return holder;

        }


        @Override
        public void bindView(final View view, Context context, final Cursor cursor) {
            final Alarm alarm = new Alarm(cursor);
            Object tag = view.getTag();
            if (tag == null) {
                // The view was converted but somehow lost its tag.
                tag = setNewHolder(view);
            }
            final ItemHolder itemHolder = (ItemHolder) tag;
            itemHolder.alarm = alarm;

            // We must unset the listener first because this maybe a recycled
            // view so changing the
            // state would affect the wrong alarm.
            itemHolder.onoff.setOnCheckedChangeListener(null);

            // Hack to workaround b/21459481: the SwitchCompat instance must be
            // detached from
            // its parent in order to avoid running the checked animation, which
            // may get stuck
            // when ListView calls View#jumpDrawablesToCurrentState() on a
            // recycled view.
            if (itemHolder.onoff.isChecked() != alarm.enabled) {

                final ViewGroup onoffParent = (ViewGroup) itemHolder.onoff.getParent();
                final int onoffIndex = onoffParent.indexOfChild(itemHolder.onoff);
                onoffParent.removeView(itemHolder.onoff);
                itemHolder.onoff.setChecked(alarm.enabled);
                onoffParent.addView(itemHolder.onoff, onoffIndex);
            }

            if (mSelectedAlarms.contains(itemHolder.alarm.id)) {
                setAlarmItemBackgroundAndElevation(itemHolder.alarmItem, true /* expanded */);
                // setDigitalTimeAlpha(itemHolder, true);
                itemHolder.onoff.setEnabled(false);
            } else {
                itemHolder.onoff.setEnabled(true);
                setAlarmItemBackgroundAndElevation(itemHolder.alarmItem, false /* expanded */);
                setDigitalTimeAlpha(itemHolder, itemHolder.onoff.isChecked());
            }
            itemHolder.clock.setFormat(mContext, mContext.getResources()
                    .getDimensionPixelSize(R.dimen.label_font_size));
            itemHolder.clock.setTime(alarm.hour, alarm.minutes);
            itemHolder.clock.setClickable(false);
            // itemHolder.clock.setOnClickListener(new View.OnClickListener()
            // {//禁止点击时间改变时间
            // @Override
            // public void onClick(View view) {
            // mSelectedAlarm = itemHolder.alarm;
            // showTimeEditDialog(alarm);
            // expandAlarm(itemHolder, true);
            // itemHolder.alarmItem.post(mScrollRunnable);
            // }
            // });

            final CompoundButton.OnCheckedChangeListener onOffListener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, final boolean checked) {
                    if (checked != alarm.enabled) {
                        // if (!isAlarmExpanded(alarm)) {
                        // // Only toggle this when alarm is collapsed
                        // setDigitalTimeAlpha(itemHolder, checked);
                        // }

                        // alarm.enabled = checked;
                        // asyncUpdateAlarm(alarm, alarm.enabled);

                        compoundButton.postDelayed(new Runnable() {// 开启开关的时候有轻微卡顿
                            // 暂时延时300ms执行看看效果

                            @Override
                            public void run() {
                                alarm.enabled = checked;
                                asyncUpdateAlarm(alarm, alarm.enabled);
                            }
                        }, 300);
                    }
                    if (!checked) {
                        SharePreferencesUtils.setSleepCount(getActivity(), alarm.id, 0);// 清除睡眠次数
                    } else {
                        SharePreferencesUtils.setSkipThisALarm(getActivity(), alarm.id, false);// 清除跳过该闹钟

                        Alarm getAlarmFromMap = alarmIdAlarmMap.get(alarm.id);// 更新什么时候响铃
                        if (getAlarmFromMap == null) {
                            alarmIdAlarmMap.put(alarm.id, alarm);
                            getAlarmFromMap = alarm;
                        }
                        getAlarmFromMap.whenAlarnTime = getWhenTime(getAlarmFromMap);
                        Log.i("zouxu", "onOffListener whenAlarnTime=" + getAlarmFromMap.whenAlarnTime);
                    }
                    Log.i("zouxu", "onOffListener:" + checked);
                }
            };

            alarm.is_remaind_later = SharePreferencesUtils.istRemindLaterALarm(getActivity(), alarm.id);
            alarm.isWorkDayAlarm = false;
            alarm.isNoWorkDayAlarm = false;
            if (SharePreferencesUtils.isWorkDayALarm(getActivity(), alarm.id)) {
                alarm.isWorkDayAlarm = true;
                String str_tomorrow_lab = getResources().getString(R.string.work_day);

                if (!TextUtils.isEmpty(alarm.label)) {//标注放在前面
                    str_tomorrow_lab = alarm.label + " " + str_tomorrow_lab;
                }

                itemHolder.tomorrowLabel.setText(str_tomorrow_lab);
                itemHolder.tomorrowLabel.setVisibility(View.VISIBLE);
            } else if (SharePreferencesUtils.isNoWorkDayALarm(getActivity(), alarm.id)) {
                alarm.isNoWorkDayAlarm = true;
                String str_tomorrow_lab = getResources().getString(R.string.no_work_day);//add zouxu 20160918
                if (!TextUtils.isEmpty(alarm.label)) {//标注放在前面
                    str_tomorrow_lab = alarm.label + " " + str_tomorrow_lab;
                }
                itemHolder.tomorrowLabel.setText(str_tomorrow_lab);
                itemHolder.tomorrowLabel.setVisibility(View.VISIBLE);
            } else if (mRepeatChecked.contains(alarm.id) || itemHolder.alarm.daysOfWeek.isRepeating()) {
                itemHolder.tomorrowLabel.setVisibility(View.GONE);
            } else {
                itemHolder.tomorrowLabel.setVisibility(View.VISIBLE);
                final Resources resources = getResources();
//                String labelText = Alarm.isTomorrow(alarm) ? resources.getString(R.string.alarm_tomorrow)
//                        : resources.getString(R.string.alarm_today);
                String labelText = resources.getString(R.string.one_time);

                if (!TextUtils.isEmpty(alarm.label)) {//标注放在前面
                    labelText = alarm.label + " " + labelText;
                }

                itemHolder.tomorrowLabel.setText(labelText);
            }
            itemHolder.onoff.setOnCheckedChangeListener(onOffListener);

            boolean expanded = isAlarmExpanded(alarm);
            if (expanded) {
                mExpandedItemHolder = itemHolder;
            }
            // itemHolder.summary.setVisibility(expanded ? View.GONE :
            // View.VISIBLE);
            // itemHolder.hairLine.setVisibility(expanded ? View.GONE :
            // View.VISIBLE);
            // itemHolder.arrow.setRotation(expanded ? ROTATE_180_DEGREE : 0);

            Alarm getAlarmFromMap = alarmIdAlarmMap.get(alarm.id);
            if (getAlarmFromMap == null) {
                alarmIdAlarmMap.put(alarm.id, alarm);
                getAlarmFromMap = alarm;
            }

            if (isShowWhenToAlarm) {
                itemHolder.text_when_to_alarm.setVisibility(View.VISIBLE);
                String whenAlarnTime = alarmIdAlarmMap.get(alarm.id).whenAlarnTime;
                if (getAlarmFromMap == null || getAlarmFromMap.whenAlarnTime == null) {
                    whenAlarnTime = getWhenTime(getAlarmFromMap);
                    getAlarmFromMap.whenAlarnTime = whenAlarnTime;
                }

                if (itemHolder.onoff.isChecked()) {
                    itemHolder.text_when_to_alarm.setText(whenAlarnTime);
                } else {
                    itemHolder.text_when_to_alarm.setText(R.string.str_alarm_disable);
                }
                alarmIdTextViewMap.put(alarm.id, itemHolder.text_when_to_alarm);
            } else {
                itemHolder.text_when_to_alarm.setVisibility(View.GONE);
            }

            // Add listener on the arrow to enable proper talkback
            // functionality.
            // Avoid setting content description on the entire card.
            // itemHolder.arrow.setOnClickListener(new View.OnClickListener() {
            // @Override
            // public void onClick(View view) {
            // if (isAlarmExpanded(alarm)) {
            // // Is expanded, make collapse call.
            // collapseAlarm(itemHolder, true);
            // } else {
            // // Is collapsed, make expand call.
            // expandAlarm(itemHolder, true);
            // }
            // }
            // });

            // Set the repeat text or leave it blank if it does not repeat.
            String daysOfWeekStr = alarm.daysOfWeek.toString(context, Utils.getFirstDayOfWeek(context));
            if (daysOfWeekStr != null && daysOfWeekStr.length() != 0) {

                if (!TextUtils.isEmpty(alarm.label)) {
                    daysOfWeekStr = alarm.label + " " + daysOfWeekStr;
                }

                itemHolder.daysOfWeek.setText(daysOfWeekStr);
                itemHolder.daysOfWeek.setContentDescription(alarm.daysOfWeek.toAccessibilityString(context,
                        Utils.getFirstDayOfWeek(context)));
                itemHolder.daysOfWeek.setVisibility(View.VISIBLE);
                itemHolder.daysOfWeek.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        expandAlarm(itemHolder, true);
                        itemHolder.alarmItem.post(mScrollRunnable);
                    }
                });

            } else {
                itemHolder.daysOfWeek.setVisibility(View.GONE);
            }

            if (alarm.isWorkDayAlarm || alarm.isNoWorkDayAlarm) {
                itemHolder.daysOfWeek.setVisibility(View.GONE);
            }

            if (alarm.label != null && alarm.label.length() != 0) {
                itemHolder.label.setText(alarm.label + "  ");
                // itemHolder.label.setVisibility(View.VISIBLE);
                itemHolder.label.setVisibility(View.GONE);// 隐藏标签
                itemHolder.label.setContentDescription(mContext.getResources().getString(R.string.label_description)
                        + " " + alarm.label);
                itemHolder.label.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        expandAlarm(itemHolder, true);
                        itemHolder.alarmItem.post(mScrollRunnable);
                    }
                });
            } else {
                itemHolder.label.setVisibility(View.GONE);
            }

            if (expanded) {
                expandAlarm(itemHolder, false);
            }

            itemHolder.alarmItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // if (isAlarmExpanded(alarm)) {
                    // collapseAlarm(itemHolder, true);
                    // } else {
                    // expandAlarm(itemHolder, true);
                    // }

                    if (isDeleteMode) {
                        itemHolder.select_checkbox.setChecked(!itemHolder.select_checkbox.isChecked());
                        return;
                    }

                    mSelectedAlarm = alarm;
                    Intent i = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("alarm", alarm);
                    i.putExtras(bundle);
                    i.setClass(getActivity(), SetAlarmActivity.class);
                    startActivityForResult(i, AppConst.REQUEST_ADD_ALARM);
                }
            });

            itemHolder.alarmItem.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View arg0) {
                    // showDeleteAlarmDialog(alarm);
                    if (!isDeleteMode) {
                        setDeleteMode(true);
                        itemHolder.select_checkbox.setChecked(true);
                    }
                    return true;
                }
            });


            if (isDeleteMode) {
                //itemHolder.select_checkbox.setVisibility(View.VISIBLE);
                itemHolder.onoff.setVisibility(View.GONE);
            } else {
                //itemHolder.select_checkbox.setVisibility(View.GONE);
//                itemHolder.select_checkbox.setChecked(false);
                itemHolder.onoff.setVisibility(View.VISIBLE);
            }

            itemHolder.select_checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                    // deleteAlarmIdAlarmMap.put(alarm, arg1);
                    if (arg1) {
                        addDeleteAlarm(alarm);
                    } else {
                        removeDeleteAlarm(alarm);
                    }
                    updateSelectCount();
                }
            });
            itemHolder.select_checkbox.setChecked(isExistDeleteAlarm(alarm));
        }

        private void setAlarmItemBackgroundAndElevation(LinearLayout layout, boolean expanded) {
            if (disable_expend) {
                return;
            }
            if (expanded) {
                layout.setBackgroundColor(getTintedBackgroundColor());
                ViewCompat.setElevation(layout, ALARM_ELEVATION);
            } else {
                layout.setBackgroundResource(R.drawable.alarm_background_normal);
                ViewCompat.setElevation(layout, 0f);
            }
        }

        private int getTintedBackgroundColor() {
            final int c = Utils.getCurrentHourColor();
            final int red = Color.red(c) + (int) (TINTED_LEVEL * (255 - Color.red(c)));
            final int green = Color.green(c) + (int) (TINTED_LEVEL * (255 - Color.green(c)));
            final int blue = Color.blue(c) + (int) (TINTED_LEVEL * (255 - Color.blue(c)));
            return Color.rgb(red, green, blue);
        }

        private void bindExpandArea(final ItemHolder itemHolder, final Alarm alarm) {
            // Views in here are not bound until the item is expanded.

            // if (alarm.label != null && alarm.label.length() > 0) {
            // itemHolder.clickableLabel.setText(alarm.label);
            // } else {
            // itemHolder.clickableLabel.setText(R.string.label);
            // }
            //
            // itemHolder.clickableLabel.setOnClickListener(new
            // View.OnClickListener() {
            // @Override
            // public void onClick(View view) {
            // showLabelDialog(alarm);
            // }
            // });

            // if (mRepeatChecked.contains(alarm.id) ||
            // itemHolder.alarm.daysOfWeek.isRepeating()) {
            // itemHolder.repeatDays.setVisibility(View.VISIBLE);
            // } else {
            // itemHolder.repeatDays.setVisibility(View.GONE);
            // }
            // itemHolder.repeat.setOnClickListener(new View.OnClickListener() {
            // @Override
            // public void onClick(View view) {
            // // Animate the resulting layout changes.
            // if (USE_TRANSITION_FRAMEWORK) {
            // TransitionManager.beginDelayedTransition(mList,
            // mRepeatTransition);
            // }
            //
            // final Calendar now = Calendar.getInstance();
            // final Calendar oldNextAlarmTime = alarm.getNextAlarmTime(now,
            // getActivity());
            //
            // final boolean checked = ((CheckBox) view).isChecked();
            // if (checked) {
            // // Show days
            // itemHolder.repeatDays.setVisibility(View.VISIBLE);
            // mRepeatChecked.add(alarm.id);
            //
            // // Set all previously set days
            // // or
            // // Set all days if no previous.
            // final int bitSet = mPreviousDaysOfWeekMap.getInt("" + alarm.id);
            // alarm.daysOfWeek.setBitSet(bitSet);
            // if (!alarm.daysOfWeek.isRepeating()) {
            // alarm.daysOfWeek.setDaysOfWeek(true, mDayOrder);
            // }
            // updateDaysOfWeekButtons(itemHolder, alarm.daysOfWeek);
            // } else {
            // // Hide days
            // itemHolder.repeatDays.setVisibility(View.GONE);
            // mRepeatChecked.remove(alarm.id);
            //
            // // Remember the set days in case the user wants it back.
            // final int bitSet = alarm.daysOfWeek.getBitSet();
            // mPreviousDaysOfWeekMap.putInt("" + alarm.id, bitSet);
            //
            // // Remove all repeat days
            // alarm.daysOfWeek.clearAllDays();
            // }
            //
            // // if the change altered the next scheduled alarm time, tell
            // // the user
            // final Calendar newNextAlarmTime = alarm.getNextAlarmTime(now,
            // getActivity());
            // final boolean popupToast =
            // !oldNextAlarmTime.equals(newNextAlarmTime);
            //
            // asyncUpdateAlarm(alarm, popupToast);
            // }
            // });

            updateDaysOfWeekButtons(itemHolder, alarm.daysOfWeek);
            // for (int i = 0; i < 7; i++) {
            // final int buttonIndex = i;

            // itemHolder.dayButtons[i].setOnClickListener(new
            // View.OnClickListener() {
            // @Override
            // public void onClick(View view) {
            // final boolean isActivated =
            // itemHolder.dayButtons[buttonIndex].isActivated();
            //
            // final Calendar now = Calendar.getInstance();
            // final Calendar oldNextAlarmTime = alarm.getNextAlarmTime(now,
            // getActivity());
            // alarm.daysOfWeek.setDaysOfWeek(!isActivated,
            // mDayOrder[buttonIndex]);
            //
            // if (!isActivated) {
            // turnOnDayOfWeek(itemHolder, buttonIndex);
            // } else {
            // turnOffDayOfWeek(itemHolder, buttonIndex);
            //
            // // See if this was the last day, if so, un-check the
            // // repeat box.
            // if (!alarm.daysOfWeek.isRepeating()) {
            // if (USE_TRANSITION_FRAMEWORK) {
            // // Animate the resulting layout changes.
            // TransitionManager.beginDelayedTransition(mList,
            // mRepeatTransition);
            // }
            //
            // itemHolder.repeat.setChecked(false);
            // itemHolder.repeatDays.setVisibility(View.GONE);
            // mRepeatChecked.remove(alarm.id);
            //
            // // Set history to no days, so it will be
            // // everyday when repeat is
            // // turned back on
            // mPreviousDaysOfWeekMap.putInt("" + alarm.id,
            // DaysOfWeek.NO_DAYS_SET);
            // }
            // }
            //
            // // if the change altered the next scheduled alarm time,
            // // tell the user
            // final Calendar newNextAlarmTime = alarm.getNextAlarmTime(now,
            // getActivity());
            // final boolean popupToast =
            // !oldNextAlarmTime.equals(newNextAlarmTime);
            //
            // asyncUpdateAlarm(alarm, popupToast);
            // }
            // });
            // }

            // if (!mHasVibrator) {
            // itemHolder.vibrate.setVisibility(View.INVISIBLE);
            // } else {
            // itemHolder.vibrate.setVisibility(View.VISIBLE);
            // if (!alarm.vibrate) {
            // itemHolder.vibrate.setChecked(false);
            // } else {
            // itemHolder.vibrate.setChecked(true);
            // }
            // }
            //
            // itemHolder.vibrate.setOnClickListener(new View.OnClickListener()
            // {
            // @Override
            // public void onClick(View v) {
            // alarm.vibrate = ((CheckBox) v).isChecked();
            // asyncUpdateAlarm(alarm, false);
            // }
            // });

            // final String ringtone;
            // if (Alarm.NO_RINGTONE_URI.equals(alarm.alert)) {
            // ringtone =
            // mContext.getResources().getString(R.string.silent_alarm_summary);
            // } else {
            // ringtone = getRingToneTitle(alarm.alert);
            // }
            // itemHolder.ringtone.setText(ringtone);
            // itemHolder.ringtone.setContentDescription(mContext.getResources().getString(R.string.ringtone_description)
            // + " " + ringtone);
            // itemHolder.ringtone.setOnClickListener(new View.OnClickListener()
            // {
            // @Override
            // public void onClick(View view) {
            // launchRingTonePicker(alarm);
            // }
            // });
        }

        // Sets the alpha of the digital time display. This gives a visual
        // effect
        // for enabled/disabled and expanded/collapsed alarm while leaving the
        // on/off switch more visible
        private void setDigitalTimeAlpha(ItemHolder holder, boolean enabled) {
            // float alpha = enabled ? 1f : 0.69f;
            // holder.clock.setAlpha(alpha);
            if (enabled) {
                holder.clock.setTextColor(getActivity().getColor(R.color.alarm_clock_clolor));
                holder.tomorrowLabel.setTextColor(getActivity().getColor(R.color.clock_gray));
                holder.daysOfWeek.setTextColor(getActivity().getColor(R.color.clock_gray));
            } else {
                holder.clock.setTextColor(getActivity().getColor(R.color.clock_gray));
                holder.tomorrowLabel.setTextColor(getActivity().getColor(R.color.clock_gray));
                holder.daysOfWeek.setTextColor(getActivity().getColor(R.color.clock_gray));
            }
        }

        private void updateDaysOfWeekButtons(ItemHolder holder, DaysOfWeek daysOfWeek) {
            HashSet<Integer> setDays = daysOfWeek.getSetDays();
            for (int i = 0; i < 7; i++) {
                if (setDays.contains(mDayOrder[i])) {
                    turnOnDayOfWeek(holder, i);
                } else {
                    turnOffDayOfWeek(holder, i);
                }
            }
        }

        private void turnOffDayOfWeek(ItemHolder holder, int dayIndex) {
            // final CompoundButton dayButton = holder.dayButtons[dayIndex];
            // dayButton.setActivated(false);
            // dayButton.setChecked(false);
            // dayButton.setTextColor(getResources().getColor(R.color.clock_white));
        }

        private void turnOnDayOfWeek(ItemHolder holder, int dayIndex) {
            // final CompoundButton dayButton = holder.dayButtons[dayIndex];
            // dayButton.setActivated(true);
            // dayButton.setChecked(true);
            // dayButton.setTextColor(Utils.getCurrentHourColor());
        }

        /**
         * Does a read-through cache for ringtone titles.
         *
         * @param uri The uri of the ringtone.
         * @return The ringtone title. {@literal null} if no matching ringtone
         * found.
         */
        private String getRingToneTitle(Uri uri) {
            // Try the cache first
            String title = mRingtoneTitleCache.getString(uri.toString());
            if (title == null) {
                // If the user cannot read the ringtone file, insert our own
                // name rather than the
                // ugly one returned by Ringtone.getTitle().
                if (!AlarmUtils.hasPermissionToDisplayRingtoneTitle(mContext, uri)) {
                    title = getString(R.string.custom_ringtone);
                } else {
                    // This is slow because a media player is created during
                    // Ringtone object creation.
                    final Ringtone ringTone = RingtoneManager.getRingtone(mContext, uri);
                    if (ringTone == null) {
                        LogUtils.i("No ringtone for uri %s", uri.toString());
                        return null;
                    }
                    title = ringTone.getTitle(mContext);
                }

                if (title != null) {
                    mRingtoneTitleCache.putString(uri.toString(), title);
                }
            }
            return title;
        }

        public void setNewAlarm(long alarmId) {
            if (mExpandedId != alarmId) {
                if (mExpandedItemHolder != null) {
                    collapseAlarm(mExpandedItemHolder, true);
                }
                mExpandedId = alarmId;
            }
        }

        /**
         * Expands the alarm for editing.
         *
         * @param itemHolder The item holder instance.
         */
        private void expandAlarm(final ItemHolder itemHolder, boolean animate) {
            // Skip animation later if item is already expanded
            if (disable_expend) {
                return;
            }

            animate &= mExpandedId != itemHolder.alarm.id;

            if (mExpandedItemHolder != null && mExpandedItemHolder != itemHolder && mExpandedId != itemHolder.alarm.id) {
                // Only allow one alarm to expand at a time.
                collapseAlarm(mExpandedItemHolder, animate);
            }

            bindExpandArea(itemHolder, itemHolder.alarm);

            mExpandedId = itemHolder.alarm.id;
            mExpandedItemHolder = itemHolder;

            // Scroll the view to make sure it is fully viewed
            mScrollAlarmId = itemHolder.alarm.id;

            // Save the starting height so we can animate from this value.
            final int startingHeight = itemHolder.alarmItem.getHeight();

            // Set the expand area to visible so we can measure the height to
            // animate to.
            setAlarmItemBackgroundAndElevation(itemHolder.alarmItem, true /* expanded */);
            // Show digital time in full-opaque when expanded, even when alarm
            // is disabled
            // setDigitalTimeAlpha(itemHolder, true /* enabled */);

            // itemHolder.arrow.setContentDescription(getString(R.string.collapse_alarm));

            if (!animate) {
                // Set the "end" layout and don't do the animation.
                // itemHolder.arrow.setRotation(ROTATE_180_DEGREE);
                // itemHolder.summary.setVisibility(View.GONE);
                // itemHolder.hairLine.setVisibility(View.GONE);
                return;
            }

            // Mark the alarmItem as having transient state to prevent it from
            // being recycled
            // while it is animating.
            itemHolder.alarmItem.setHasTransientState(true);

            // Add an onPreDrawListener, which gets called after measurement but
            // before the draw.
            // This way we can check the height we need to animate to before any
            // drawing.
            // Note the series of events:
            // * expandArea is set to VISIBLE, which causes a layout pass
            // * the view is measured, and our onPreDrawListener is called
            // * we set up the animation using the start and end values.
            // * the height is set back to the starting point so it can be
            // animated down.
            // * request another layout pass.
            // * return false so that onDraw() is not called for the single
            // frame before
            // the animations have started.
            final ViewTreeObserver observer = mAlarmsList.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    // We don't want to continue getting called for every
                    // listview drawing.
                    if (observer.isAlive()) {
                        observer.removeOnPreDrawListener(this);
                    }
                    // Calculate some values to help with the animation.
                    final int endingHeight = itemHolder.alarmItem.getHeight();
                    final int distance = endingHeight - startingHeight;
                    final int collapseHeight = itemHolder.collapseExpandArea.getHeight();

                    // Set the height back to the start state of the animation.
                    itemHolder.alarmItem.getLayoutParams().height = startingHeight;
                    // To allow the expandArea to glide in with the expansion
                    // animation, set a
                    // negative top margin, which will animate down to a margin
                    // of 0 as the height
                    // is increased.
                    // Note that we need to maintain the bottom margin as a
                    // fixed value (instead of
                    // just using a listview, to allow for a flatter hierarchy)
                    // to fit the bottom
                    // bar underneath.
                    itemHolder.alarmItem.requestLayout();

                    // Set up the animator to animate the expansion.
                    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(EXPAND_DURATION);
                    animator.setInterpolator(mExpandInterpolator);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            Float value = (Float) animator.getAnimatedValue();

                            // For each value from 0 to 1, animate the various
                            // parts of the layout.
                            itemHolder.alarmItem.getLayoutParams().height = (int) (value * distance + startingHeight);
                            // FrameLayout.LayoutParams expandParams =
                            // (FrameLayout.LayoutParams) itemHolder.expandArea
                            // .getLayoutParams();
                            // expandParams.setMargins(0, (int) -((1 - value) *
                            // distance), 0, collapseHeight);
                            // itemHolder.arrow.setRotation(ROTATE_180_DEGREE *
                            // value);
                            // itemHolder.summary.setAlpha(1 - value);
                            // itemHolder.hairLine.setAlpha(1 - value);

                            itemHolder.alarmItem.requestLayout();
                        }
                    });
                    // Set everything to their final values when the animation's
                    // done.
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            // Set it back to wrap content since we'd explicitly
                            // set the height.
                            itemHolder.alarmItem.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                            // itemHolder.arrow.setRotation(ROTATE_180_DEGREE);
                            // itemHolder.summary.setAlpha(1);
                            // itemHolder.hairLine.setAlpha(1);
                            // itemHolder.summary.setVisibility(View.GONE);
                            // itemHolder.hairLine.setVisibility(View.GONE);
                            itemHolder.alarmItem.setHasTransientState(false);
                        }
                    });
                    animator.start();

                    // Return false so this draw does not occur to prevent the
                    // final frame from
                    // being drawn for the single frame before the animations
                    // start.
                    return false;
                }
            });
        }

        private boolean isAlarmExpanded(Alarm alarm) {
            return false;
            // return mExpandedId == alarm.id; 禁止展开 zouxu
        }

        private void collapseAlarm(final ItemHolder itemHolder, boolean animate) {

            if (disable_expend) {
                return;
            }

            mExpandedId = AlarmClockFragment.INVALID_ID;
            mExpandedItemHolder = null;

            // Save the starting height so we can animate from this value.
            final int startingHeight = itemHolder.alarmItem.getHeight();

            // Set the expand area to gone so we can measure the height to
            // animate to.
            setAlarmItemBackgroundAndElevation(itemHolder.alarmItem, false /* expanded */);
            setDigitalTimeAlpha(itemHolder, itemHolder.onoff.isChecked());

            // itemHolder.arrow.setContentDescription(getString(R.string.expand_alarm));

            if (!animate) {
                // Set the "end" layout and don't do the animation.
                // itemHolder.arrow.setRotation(0);
                // itemHolder.summary.setAlpha(1);
                // itemHolder.summary.setVisibility(View.VISIBLE);
                return;
            }

            // Mark the alarmItem as having transient state to prevent it from
            // being recycled
            // while it is animating.
            itemHolder.alarmItem.setHasTransientState(true);

            // Add an onPreDrawListener, which gets called after measurement but
            // before the draw.
            // This way we can check the height we need to animate to before any
            // drawing.
            // Note the series of events:
            // * expandArea is set to GONE, which causes a layout pass
            // * the view is measured, and our onPreDrawListener is called
            // * we set up the animation using the start and end values.
            // * expandArea is set to VISIBLE again so it can be shown
            // animating.
            // * request another layout pass.
            // * return false so that onDraw() is not called for the single
            // frame before
            // the animations have started.
            final ViewTreeObserver observer = mAlarmsList.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (observer.isAlive()) {
                        observer.removeOnPreDrawListener(this);
                    }

                    // Calculate some values to help with the animation.
                    final int endingHeight = itemHolder.alarmItem.getHeight();
                    final int distance = endingHeight - startingHeight;

                    // Re-set the visibilities for the start state of the
                    // animation.
                    // itemHolder.summary.setVisibility(View.VISIBLE);
                    // itemHolder.summary.setAlpha(1);

                    // Set up the animator to animate the expansion.
                    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(COLLAPSE_DURATION);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            Float value = (Float) animator.getAnimatedValue();

                            // For each value from 0 to 1, animate the various
                            // parts of the layout.
                            itemHolder.alarmItem.getLayoutParams().height = (int) (value * distance + startingHeight);
                            // FrameLayout.LayoutParams expandParams =
                            // (FrameLayout.LayoutParams) itemHolder.expandArea
                            // .getLayoutParams();
                            // expandParams.setMargins(0, (int) (value *
                            // distance), 0, mCollapseExpandHeight);
                            // // itemHolder.arrow.setRotation(ROTATE_180_DEGREE
                            // * (1 - value));
                            // itemHolder.delete.setAlpha(value);
                            // itemHolder.summary.setAlpha(value);

                            itemHolder.alarmItem.requestLayout();
                        }
                    });
                    animator.setInterpolator(mCollapseInterpolator);
                    // Set everything to their final values when the animation's
                    // done.
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            // Set it back to wrap content since we'd explicitly
                            // set the height.
                            itemHolder.alarmItem.getLayoutParams().height = LayoutParams.WRAP_CONTENT;

                            // FrameLayout.LayoutParams expandParams =
                            // (FrameLayout.LayoutParams) itemHolder.expandArea
                            // .getLayoutParams();
                            // expandParams.setMargins(0, 0, 0,
                            // mCollapseExpandHeight);
                            //
                            // itemHolder.expandArea.setVisibility(View.GONE);
                            // itemHolder.arrow.setRotation(0);
                            itemHolder.alarmItem.setHasTransientState(false);
                        }
                    });
                    animator.start();

                    return false;
                }
            });
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        private View getViewById(long id) {
            for (int i = 0; i < mList.getCount(); i++) {
                View v = mList.getChildAt(i);
                if (v != null) {
                    ItemHolder h = (ItemHolder) (v.getTag());
                    if (h != null && h.alarm.id == id) {
                        return v;
                    }
                }
            }
            return null;
        }

        public long getExpandedId() {
            return mExpandedId;
        }

        public long[] getSelectedAlarmsArray() {
            int index = 0;
            long[] ids = new long[mSelectedAlarms.size()];
            for (long id : mSelectedAlarms) {
                ids[index] = id;
                index++;
            }
            return ids;
        }

        public long[] getRepeatArray() {
            int index = 0;
            long[] ids = new long[mRepeatChecked.size()];
            for (long id : mRepeatChecked) {
                ids[index] = id;
                index++;
            }
            return ids;
        }

        public Bundle getPreviousDaysOfWeekMap() {
            return mPreviousDaysOfWeekMap;
        }

        private void buildHashSetFromArray(long[] ids, HashSet<Long> set) {
            for (long id : ids) {
                set.add(id);
            }
        }
    }

    private static AlarmInstance setupAlarmInstance(Context context, Alarm alarm) {
        ContentResolver cr = context.getContentResolver();
        AlarmInstance newInstance = alarm.createInstanceAfter(Calendar.getInstance(), context);
        newInstance = AlarmInstance.addInstance(cr, newInstance);
        // Register instance to state manager
        AlarmStateManager.registerInstance(context, newInstance, true);
        return newInstance;
    }

    private void asyncDeleteAlarm(final Alarm alarm) {
        final Context context = AlarmClockFragment.this.getActivity().getApplicationContext();
        final AsyncTask<Void, Void, Void> deleteTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... parameters) {
                // Activity may be closed at this point , make sure data is
                // still valid
                if (context != null && alarm != null) {
                    Events.sendAlarmEvent(R.string.action_delete, R.string.label_deskclock);
                    ContentResolver cr = context.getContentResolver();
                    AlarmStateManager.deleteAllInstances(context, alarm.id);
                    Alarm.deleteAlarm(cr, alarm.id);
                }
                return null;
            }
        };
        // mUndoShowing = true;//一直不显示撤销
        mUndoShowing = false;
        alarmIdTextViewMap.remove(alarm.id);
//        deleteTask.execute();//zouxu 20161102
        deleteTask.executeOnExecutor(Executors.newCachedThreadPool());//zouxu 20161102
    }

    protected void asyncAddAlarm(final Alarm alarm) {
        final Context context = AlarmClockFragment.this.getActivity().getApplicationContext();
        final AsyncTask<Void, Void, AlarmInstance> updateTask = new AsyncTask<Void, Void, AlarmInstance>() {
            @Override
            protected AlarmInstance doInBackground(Void... parameters) {
                if (context != null && alarm != null) {
                    Events.sendAlarmEvent(R.string.action_create, R.string.label_deskclock);
                    ContentResolver cr = context.getContentResolver();

                    // Add alarm to db
                    Alarm newAlarm = Alarm.addAlarm(cr, alarm);
                    mScrollToAlarmId = newAlarm.id;

                    SharePreferencesUtils.setWorkDayALarm(getActivity(), newAlarm.id, alarm.isWorkDayAlarm);
                    SharePreferencesUtils.setNoWorkDayALarm(getActivity(), newAlarm.id, alarm.isNoWorkDayAlarm);
                    SharePreferencesUtils.setRemindLaterALarm(getActivity(), newAlarm.id, alarm.is_remaind_later);

                    // Create and add instance to db
                    if (newAlarm.enabled) {
                        return setupAlarmInstance(context, newAlarm);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(AlarmInstance instance) {
                if (instance != null) {
                    AlarmUtils.popAlarmSetToast(context, instance.getAlarmTime().getTimeInMillis());
                }
            }
        };
//        updateTask.execute();//zouxu 20161102
        updateTask.executeOnExecutor(Executors.newCachedThreadPool());//zouxu 20161102

    }

    protected void asyncUpdateAlarm(final Alarm alarm, final boolean popToast) {

        SharePreferencesUtils.setWorkDayALarm(getActivity(), alarm.id, alarm.isWorkDayAlarm);
        SharePreferencesUtils.setNoWorkDayALarm(getActivity(), alarm.id, alarm.isNoWorkDayAlarm);
        SharePreferencesUtils.setRemindLaterALarm(getActivity(), alarm.id, alarm.is_remaind_later);

        final Context context = AlarmClockFragment.this.getActivity().getApplicationContext();
        final AsyncTask<Void, Void, AlarmInstance> updateTask = new AsyncTask<Void, Void, AlarmInstance>() {
            @Override
            protected AlarmInstance doInBackground(Void... parameters) {
                Events.sendAlarmEvent(R.string.action_update, R.string.label_deskclock);
                ContentResolver cr = context.getContentResolver();

                // Dismiss all old instances
                AlarmStateManager.deleteAllInstances(context, alarm.id);

                // Update alarm
                boolean updateAlarm = Alarm.updateAlarm(cr, alarm);
                if (!updateAlarm) {
                    Log.e("zouxu", "Alarm.updateAlarm fail!!");
                }
                updateAlarmMap(alarm);
                if (alarm.enabled) {
                    return setupAlarmInstance(context, alarm);
                }

                return null;
            }

            @Override
            protected void onPostExecute(AlarmInstance instance) {
                if (popToast && instance != null) {
                    AlarmUtils.popAlarmSetToast(context, instance.getAlarmTime().getTimeInMillis());
                }
            }
        };
//        updateTask.execute();//zouxu 20161102
        updateTask.executeOnExecutor(Executors.newCachedThreadPool());//zouxu 20161102

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideUndoBar(true, event);
        return false;
    }

    @Override
    public void onFabClick(View view) {

        hideUndoBar(true, null);
        // startCreatingAlarm();
        addOrEditAlarm();// add zouxu
    }

    private void addOrEditAlarm() {
        Intent i = new Intent();
        i.putExtra("is_add_alarm", true);
        i.setClass(getActivity(), SetAlarmActivity.class);
        startActivityForResult(i, AppConst.REQUEST_ADD_ALARM);
    }

    @Override
    public void setFabAppearance() {
        final DeskClock activity = (DeskClock) getActivity();
        if (mFab == null || activity.getSelectedTab() != DeskClock.ALARM_TAB_INDEX) {
            return;
        }
        mFab.post(new Runnable() {

            @Override
            public void run() {
                if (mAdapter.isDeleteMode()) {
                    mFab.setVisibility(View.GONE);
                } else {
                    mFab.setVisibility(View.VISIBLE);
                }
            }
        });

        mFab.setIconDrawable(getActivity().getResources().getDrawable(R.drawable.ic_add));
//        mFab.setImageResource(R.drawable.ic_add);
        mFab.setContentDescription(getString(R.string.button_alarms));
    }

    @Override
    public void setLeftRightButtonAppearance() {
        final DeskClock activity = (DeskClock) getActivity();
        if (mLeftButton == null || mRightButton == null || activity.getSelectedTab() != DeskClock.ALARM_TAB_INDEX) {
            return;
        }
        mLeftButton.setVisibility(View.INVISIBLE);
        mRightButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_RINGTONE:
                    saveRingtoneUri(data);
                    break;
                default:
                    LogUtils.w("Unhandled request code in onActivityResult: " + requestCode);
            }
        }
        if (requestCode == AppConst.REQUEST_ADD_ALARM && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            boolean is_add_alarm = data.getBooleanExtra("is_add_alarm", false);
            Alarm a = data.getParcelableExtra("alarm");
            a.enabled = true;
            if (a != null) {
                if (is_add_alarm) {
                    asyncAddAlarm(a);
                } else {
                    asyncUpdateAlarm(a, true);
                }
            }
        }
        if (requestCode == AppConst.REQUEST_ADD_ALARM && resultCode == AppConst.RESULT_DELETE_ALARM) {
            if (data == null) {
                return;
            }
            Alarm a = data.getParcelableExtra("alarm");
            if (a != null) {
                asyncDeleteAlarm(a);
            }

        }
    }

    private void showDeleteAlarmDialog(final Alarm mAlarm) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.time_picker_set, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        asyncDeleteAlarm(mAlarm);
                        mDeletedAlarm = mAlarm;
                    }
                }).setNegativeButton(R.string.time_picker_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setMessage(getString(R.string.delete_this_alarm)).create();
        alertDialog.show();
    }

    private void updateAllAlarm() {// 获取网络数据后　可以用该方法进行更新数据

        Context context = getActivity();
        if (context == null) {
            return;
        }

        if (mAdapter.getCount() > 0 && isAlarmPage) {
            Iterator iter = alarmIdTextViewMap.keySet().iterator();
            while (iter.hasNext()) {
                Long alarmId = (Long) iter.next();
                TextView val = alarmIdTextViewMap.get(alarmId);
                String when_time = getWhenTime(alarmIdAlarmMap.get(alarmId));
                alarmIdAlarmMap.get(alarmId).whenAlarnTime = when_time;
                asyncUpdateAlarm(alarmIdAlarmMap.get(alarmId), false);
            }
            mAdapter.notifyDataSetChanged();
            Log.i("zouxu", "updateAllAlarm!!! mAdapter.notifyDataSetChanged");
        }
    }

    private void updateAllWhenTime() {// 更新还有多久响铃的方法

        if (!isShowWhenToAlarm) {
            return;
        }

        Context context = getActivity();
        if (context == null) {
            return;
        }

        if (mAdapter.getCount() > 0 && isAlarmPage) {
            Iterator iter = alarmIdTextViewMap.keySet().iterator();
            while (iter.hasNext()) {
                Long alarmId = (Long) iter.next();
                TextView val = alarmIdTextViewMap.get(alarmId);
                // AlarmInstance alarmInstance =
                // getAlarmInstance(getActivity(),
                // alarmIdAlarmMap.get(alarmId));
                String when_time = getWhenTime(alarmIdAlarmMap.get(alarmId));

                alarmIdAlarmMap.get(alarmId).whenAlarnTime = when_time;
            }
            mAdapter.notifyDataSetChanged();
            Log.i("zouxu", "updateAllWhenTime!!! mAdapter.notifyDataSetChanged");

        }
    }

    private AlarmInstance getAlarmInstance(Context context, Alarm alarm) {
        if (context == null) {
            return null;
        }
        ContentResolver cr = context.getContentResolver();
        AlarmInstance newInstance = alarm.createInstanceAfter(Calendar.getInstance(), context);
        // newInstance = AlarmInstance.addInstance(cr, newInstance);
        return newInstance;
    }

    private boolean isAlarmPage = true;

    @Override
    public void onPageChanged(int page) {
        if (page == DeskClock.ALARM_TAB_INDEX) {
            isAlarmPage = true;
        } else {
            isAlarmPage = false;
        }
    }

    private void updateAlarmMap(Alarm alarm) {
        Iterator iter = alarmIdTextViewMap.keySet().iterator();
        while (iter.hasNext()) {
            Long key = (Long) iter.next();
            TextView val = alarmIdTextViewMap.get(key);
            if (alarm.id == key) {
                AlarmInstance alarmInstance = getAlarmInstance(getActivity(), alarm);
                if (alarmInstance == null) {
                    return;
                }
                String when_time = getWhenTime(alarm);

                alarm.whenAlarnTime = when_time;
                alarmIdAlarmMap.get(key).minutes = alarm.minutes;
                alarmIdAlarmMap.get(key).hour = alarm.hour;
                alarmIdAlarmMap.get(key).daysOfWeek = alarm.daysOfWeek;
                alarmIdAlarmMap.get(key).whenAlarnTime = alarm.whenAlarnTime;
                // mAdapter.notifyDataSetChanged();//数据更新的时候会自动刷新　不用再刷新一遍
                return;
            }
        }

    }

    private String getWhenTime(Alarm alarm) {
        String when_time = "";
        AlarmInstance alarmInstance = getAlarmInstance(getActivity(), alarm);
        if (alarmInstance == null) {
            return "";
        }
        when_time = AlarmUtils.getAlarmWhenToRing(getActivity(), alarmInstance.getAlarmTime().getTimeInMillis());
        // Log.i("zouxu", "when_time 01: " + when_time);

        if (SharePreferencesUtils.isSkipThisAlarm(getActivity(), alarm.id)) {
            when_time = AlarmUtils.getAlarmWhenToRing(getActivity(),
                    alarm.getNextAlarmTime(alarmInstance.getAlarmTime(), getActivity()).getTimeInMillis());
            // Log.i("zouxu", "when_time 02: " + when_time);
        }
        return when_time;
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {// 监听时间变化
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // boolean changed = action.equals(Intent.ACTION_TIME_CHANGED)
            // || action.equals(Intent.ACTION_TIMEZONE_CHANGED)
            // || action.equals(Intent.ACTION_LOCALE_CHANGED);
            // if (changed) {
            updateAllWhenTime();
            // }
        }
    };

    public void selectAll(boolean isSelectAll) {// isSelectAll true 全选 false 全清
        // Iterator iter = deleteAlarmIdAlarmMap.keySet().iterator();
        // while (iter.hasNext()) {
        // Alarm key = (Alarm) iter.next();
        // deleteAlarmIdAlarmMap.put(key, true);
        // }
        if (isSelectAll) {
            Cursor mCouCursor = mAdapter.getCursor();
            int old_pos = mCouCursor.getPosition();
            boolean is = mCouCursor.moveToFirst();
            while (is) {
                Alarm mAlarm = new Alarm(mCouCursor);
                addDeleteAlarm(mAlarm);
                if (!mCouCursor.moveToNext()) {
                    break;
                }
            }
            mCouCursor.moveToPosition(old_pos);
        } else {
            deleteAlarmList.clear();
        }

        mAdapter.notifyDataSetChanged();
        Log.i("zouxu", "selectAll!!! mAdapter.notifyDataSetChanged");
    }

    public void deleteClick() {
        // Iterator iter = deleteAlarmIdAlarmMap.keySet().iterator();
        // while (iter.hasNext()) {
        // Alarm key = (Alarm) iter.next();
        // boolean val = deleteAlarmIdAlarmMap.get(key);
        // if (val) {
        // asyncDeleteAlarm(key);
        // mDeletedAlarm = key;
        // }
        // }
        for (int i = 0; i < deleteAlarmList.size(); i++) {
            Alarm mAlarm = deleteAlarmList.get(i);
            asyncDeleteAlarm(mAlarm);
            mDeletedAlarm = mAlarm;
        }
        mAdapter.setDeleteMode(false);
        DeskClock mclock = (DeskClock) getActivity();
        if (mclock != null) {
            mclock.setDeletMode(false);
        }
    }

    public void setDeleteMode(boolean is) {
        mAdapter.setDeleteMode(is);
    }

    public void updateSelectCount() {
        DeskClock mclock = (DeskClock) getActivity();
        if (mclock != null) {
            // int count = 0;
            // Iterator iter = deleteAlarmIdAlarmMap.keySet().iterator();
            // while (iter.hasNext()) {
            // Alarm key = (Alarm) iter.next();
            // boolean val = deleteAlarmIdAlarmMap.get(key);
            // if (val) {
            // count++;
            // }
            // }
            Cursor mCouCursor = mAdapter.getCursor();
            int old_pos = mCouCursor.getPosition();
            int allCount = mCouCursor.getCount();
            mCouCursor.moveToPosition(old_pos);

            mclock.updateSelectCount(deleteAlarmList.size(), allCount);
        }
    }

    private boolean isExistDeleteAlarm(Alarm mAlarm) {
        for (int i = 0; i < deleteAlarmList.size(); i++) {
            if (deleteAlarmList.get(i).id == mAlarm.id) {
                return true;
            }
        }
        return false;
    }

    private void removeDeleteAlarm(Alarm mAlarm) {
        for (int i = 0; i < deleteAlarmList.size(); i++) {
            if (deleteAlarmList.get(i).id == mAlarm.id) {
                deleteAlarmList.remove(i);
                return;
            }
        }
    }

    private void addDeleteAlarm(Alarm mAlarm) {
        for (int i = 0; i < deleteAlarmList.size(); i++) {
            if (deleteAlarmList.get(i).id == mAlarm.id) {// 已经添加后就不用添加
                return;
            }
        }
        deleteAlarmList.add(mAlarm);
    }

    private void getDataFromNet() {
        if (SharePreferencesUtils.isUpdatedThisDay(getActivity())) {// 判断今天有没有更新
            return;
        }
        LegalHolidayTask m_task = new LegalHolidayTask(getActivity(), this);
        m_task.execute();
    }

    @Override
    public void update() {// 获取网络数据后的回调
        updateAllAlarm();
    }

    private boolean is_show_fab = true;

    public void hideFabAnim() {

        if (mFab == null) {
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

        if (mFab == null || is_show_fab) {
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

}
