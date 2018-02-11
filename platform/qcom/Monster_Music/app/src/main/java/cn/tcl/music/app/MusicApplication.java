package cn.tcl.music.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import com.tcl.statisticsdk.agent.StatisticsAgent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.service.MusicPlayBackService;
import cn.tcl.music.util.ActivityHelper;
import cn.tcl.music.util.Connectivity;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.NetWrokChangeManager;
import cn.tcl.music.util.PreferenceUtil;
import cn.tcl.music.util.Utils;

public class MusicApplication extends Application {
    private static final String TAG = MusicApplication.class.getSimpleName();
    private static MusicApplication mApp;

    private static final String FORCE_STOP_PACKAGE = "forceStopPackage";
    private static final int STATISTICS_TIME_OUT = 30000;
    protected static int sTotalActivitiesNum;
    private CountDownTimer mCountDownTimer;
    private long mMillisUntilFinished;
    private TextView mTimeShowTv;
    public boolean mIsNetworkDialogShowed;
    private Set<AppBackgroundListener> mAppBgListeners = Collections.synchronizedSet(new HashSet<AppBackgroundListener>());

    public interface AppBackgroundListener {
        void appGoToBackground(Activity activity);

        void appComeToForeground(Activity activity);
    }

    public void registerAppBackgroundListener(AppBackgroundListener listener) {
        mAppBgListeners.add(listener);
    }

    public void unRegisterAppBackgroundListener(AppBackgroundListener listener) {
        mAppBgListeners.remove(listener);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        if (isDataSaver()) {
            PreferenceUtil.saveValue(this, PreferenceUtil.NODE_NETWORK_SWITCH, PreferenceUtil.KEY_NETWORK_SWITCH, CommonConstants.NO_OPEN);
        }
        ActivityHelper.initInstance(this);
        startService(new Intent(this, MusicPlayBackService.class));

        if (getApplicationContext().getResources().getBoolean(R.bool.def_musiccn_statistics_function_enable)) {
            StatisticsAgent.init(getApplicationContext());
            StatisticsAgent.setDebugMode(true);
            StatisticsAgent.setAutoTraceActivity(false);
            StatisticsAgent.setSessionTimeOut(getApplicationContext(), STATISTICS_TIME_OUT);
        }
        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {

            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            public void onActivityDestroyed(Activity activity) {
            }

            public void onActivityPaused(Activity activity) {
                activityHidden(activity);
            }

            public void onActivityResumed(Activity activity) {
                activityShown(activity);
            }

            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            public void onActivityStarted(Activity activity) {
                activityShown(activity);
            }

            public void onActivityStopped(Activity activity) {
                activityHidden(activity);
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void initData() {
        mApp = this;
        sTotalActivitiesNum = 0;
        mMillisUntilFinished = 0;
        mIsNetworkDialogShowed = false;
    }

    public static MusicApplication getApp() {
        return mApp;
    }

    public CountDownTimer getCountDownTimer() {
        return mCountDownTimer;
    }

    public void startCountDownTime(long milliSecond) {
        mCountDownTimer = new CountDownTimer(milliSecond, CommonConstants.SECOND_MILLISECOND) {

            @Override
            public void onTick(long millisUntilFinished) {
                mMillisUntilFinished = millisUntilFinished;
                updateCountDownShowTime(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                forceStopAPK();
            }
        };
        mCountDownTimer.start();
    }

    public void forceStopAPK() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        Method forceStopPackage = null;
        try {
            forceStopPackage = am.getClass().getDeclaredMethod(FORCE_STOP_PACKAGE, String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        forceStopPackage.setAccessible(true);
        try {
            forceStopPackage.invoke(am, getPackageName());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void cancelCountDownTime() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mMillisUntilFinished = 0;
        }
    }

    public void setCountDownTimeShow(TextView timeShow) {
        mTimeShowTv = timeShow;
        if (mMillisUntilFinished != 0) {
            updateCountDownShowTime(mMillisUntilFinished);
        } else {
            mTimeShowTv.setVisibility(View.GONE);
        }
    }

    private void updateCountDownShowTime(long millisUntilFinished) {
        if (mTimeShowTv != null) {
            int secondsUntilFinished = (int) millisUntilFinished / CommonConstants.SECOND_MILLISECOND;
            int restMinutes = secondsUntilFinished / CommonConstants.MINUTE_TO_SECOND;
            int restSeconds = secondsUntilFinished % CommonConstants.MINUTE_TO_SECOND;
            mTimeShowTv.setText(getResources().getString(R.string.date_format, restMinutes, restSeconds));
        }
    }

    static public boolean isInBackground() {
        return (sTotalActivitiesNum <= 0);
    }


    // override it if necessary
    protected void activityHidden(Activity activity) {
        --sTotalActivitiesNum;
        if (sTotalActivitiesNum == 0) {
            mIsNetworkDialogShowed = false;
            synchronized (mAppBgListeners) {
                for (AppBackgroundListener listener : mAppBgListeners)
                    listener.appGoToBackground(activity);
            }
        }
    }

    // override it if necessary
    protected void activityShown(Activity activity) {
        ++sTotalActivitiesNum;
        if (isDataSaver()) {
            PreferenceUtil.saveValue(this, PreferenceUtil.NODE_NETWORK_SWITCH, PreferenceUtil.KEY_NETWORK_SWITCH, CommonConstants.NO_OPEN);
        }
        if (sTotalActivitiesNum >= 1) {
            synchronized (mAppBgListeners) {
                for (AppBackgroundListener listener : mAppBgListeners)
                    listener.appComeToForeground(activity);
            }
        }
    }

    public boolean isDataSaver() {
        return Utils.isSDK24() && Connectivity.isConnectedMobile(this) && Connectivity.isDataSaverEnabled(this);
    }

    public static boolean isNetWorkCanUsed() {
        return Connectivity.isConnected(getApp());
    }
}
