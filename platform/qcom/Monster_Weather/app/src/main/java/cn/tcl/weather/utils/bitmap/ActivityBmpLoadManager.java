package cn.tcl.weather.utils.bitmap;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import com.gapp.common.thread.ThreadHandler;

import java.util.ArrayList;
import java.util.HashMap;

import cn.tcl.weather.utils.LogUtils;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-11-16.
 * <p>
 * a bmp manager , you should create it in application and main thread
 */
public class ActivityBmpLoadManager implements IBmpLoadManager<AbsBmpLoadItem> {

    private final static String TAG = "ActivityBmpLoadManager";

    //    private final ThreadHandler mThreadHandler = new ThreadHandler(TAG);
    private Handler mHandler = new Handler();
    private final ActivityNodeManager mActivityNodeManager = new ActivityNodeManager();
    private final Application mApplication;
    private ActivityDrawableGenerator mDrawableGenerator;

    public ActivityBmpLoadManager(Application app) {
        mApplication = app;
    }

//    public void setDefaultDrawableGenerator(IDrawableGenerator generator){
//
//    }

    @Override
    public void init() {
        mDrawableGenerator = new ActivityDrawableGenerator(mApplication);
        mDrawableGenerator.init();
//        mThreadHandler.init();
        mApplication.registerActivityLifecycleCallbacks(mActivityNodeManager);
    }

    @Override
    public void recycle() {
        mApplication.unregisterActivityLifecycleCallbacks(mActivityNodeManager);
//        mThreadHandler.recycle();
        mDrawableGenerator.recycle();
    }

    @Override
    public void onTrimMemory(int level) {
        mDrawableGenerator.onTrimMemory(level);
    }

    @Override
    public void loadBmp(Activity activity, AbsBmpLoadItem item) {
        mActivityNodeManager.loadBmp(activity, item);
    }

    private class ActivityNodeManager implements Application.ActivityLifecycleCallbacks {

        private HashMap<Activity, ActivityNode> mActvityNodes = new HashMap<>(24);


        private ActivityNode getActivityNode(Activity activity, boolean isCreateIfNull) {
            ActivityNode node = mActvityNodes.get(activity);
            if (isCreateIfNull && null == node) {
                node = new ActivityNode(activity);
                mActvityNodes.put(activity, node);
            }
            return node;
        }

        void loadBmp(Activity activity, AbsBmpLoadItem item) {
            ActivityNode node = getActivityNode(activity, true);
            node.addLoadBmp(item);
        }

        private ActivityNode getActivityNode(Activity activity) {
            return getActivityNode(activity, false);
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            ActivityNode node = getActivityNode(activity);
            if (null != node)
                node.onStart();
        }

        @Override
        public void onActivityResumed(Activity activity) {
            ActivityNode node = getActivityNode(activity);
            if (null != node)
                node.onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            ActivityNode node = getActivityNode(activity);
            if (null != node)
                node.onPause();
        }

        @Override
        public void onActivityStopped(Activity activity) {
            ActivityNode node = getActivityNode(activity);
            if (null != node)
                node.onStoped();
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            ActivityNode node = getActivityNode(activity);
            if (null != node) {
                node.onRecycle();
                mActvityNodes.remove(node);
            }
        }
    }


    private class ActivityNode {

        private final HashMap<String, AbsBmpLoadItem> mLoadItems = new HashMap<>(24);
        private final Activity mActvity;

        ActivityNode(Activity activity) {
            mActvity = activity;
        }

        public void addLoadBmp(AbsBmpLoadItem item) {
            AbsBmpLoadItem oldItem = mLoadItems.get(item.getKey());
            if (oldItem != item) {// if old item is not same as item or old item is null
                if (null != oldItem) {
                    oldItem.onReset();
                }
                item.setActivity(mActvity, mHandler, mDrawableGenerator);
                mLoadItems.put(item.getKey(), item);
            }
//            mThreadHandler.post(item);
            item.run();
        }

        void onPause() {
//            LogUtils.timerStart();
//            for (AbsBmpLoadItem item : mLoadItems.values())
//                item.reset();
//            LogUtils.timerEnd();
        }

        synchronized ArrayList<AbsBmpLoadItem> getLoadItems() {
            return new ArrayList<>(mLoadItems.values());
        }


        void onStart() {
            LogUtils.timerStart();
            for (AbsBmpLoadItem item : getLoadItems())
                addLoadBmp(item);
            LogUtils.timerEnd();
        }

        void onStoped() {
            LogUtils.timerStart();
            for (AbsBmpLoadItem item : getLoadItems())
                item.reset();
            LogUtils.timerEnd();
        }

        void onResume() {
//            LogUtils.timerStart();
//            for (AbsBmpLoadItem item : mLoadItems.values())
//                addLoadBmp(item);
//            LogUtils.timerEnd();
        }

        void onRecycle() {
            LogUtils.timerStart();
            for (AbsBmpLoadItem item : getLoadItems())
                item.reset();
            mLoadItems.clear();
            LogUtils.timerEnd();
        }
    }
}
