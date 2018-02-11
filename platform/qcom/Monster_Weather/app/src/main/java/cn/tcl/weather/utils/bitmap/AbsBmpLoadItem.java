package cn.tcl.weather.utils.bitmap;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

import cn.tcl.weather.utils.LogUtils;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-11-16.
 * $desc
 */

public abstract class AbsBmpLoadItem implements IBmpLoadManager.IBmpLoadItem {

    protected Activity mActivity;
    protected Handler mHandler;
    protected ActivityDrawableGenerator mGenerator;

    void setActivity(Activity activity, Handler handler, ActivityDrawableGenerator generator) {
        mActivity = activity;
        mHandler = handler;
        mGenerator = generator;
        onSetActivity();
    }


    void reset() {
        onReset();
    }

    @Override
    public void run() {
        mGenerator.regiestLoadItem(this);
    }

    protected abstract void onReset();

    protected abstract void onSetActivity();


    public static abstract class DrawableLoadItem extends AbsBmpLoadItem {

        private final Drawable mDefaultDrawable;
        private Drawable mCurrentDrawable;
        private Drawable mGenerateDrawable;

        public DrawableLoadItem() {
            this(new ColorDrawable(Color.WHITE));
        }

        public DrawableLoadItem(Drawable defaultDrawable) {
            mDefaultDrawable = defaultDrawable;
        }

        @Override
        protected final void onSetActivity() {
            setCurrentDrawable(null);
        }

        @Override
        protected final void onReset() {
            LogUtils.timerStart();
            clearDrawable();
            setCurrentDrawable(null);
            mGenerator.unregiestLoadItem(this);
            LogUtils.timerEnd();
        }

        protected abstract void clearDrawable();

        void setCurrentDrawable(Drawable drawable) {
            setCurrentDrawable(drawable, true);
        }

        void setCurrentDrawable(Drawable drawable, boolean isInMain) {
            LogUtils.timerStart();
            if (drawable == null)
                drawable = mDefaultDrawable;
            if (drawable != mCurrentDrawable) {
                mCurrentDrawable = drawable;
                if (isInMain) {
                    onSetDrawable(mCurrentDrawable);
                } else {
                    mHandler.removeCallbacks(mMainRunnable);
                    mHandler.post(mMainRunnable);
                }
            }
            LogUtils.timerEnd();
        }

        protected abstract void onSetDrawable(Drawable drawable);

        private final Runnable mMainRunnable = new Runnable() {
            @Override
            public void run() {
                onSetDrawable(mCurrentDrawable);
            }
        };
    }


    public static abstract class AbsResBmpLoadItem extends DrawableLoadItem {

        protected int mLastResId;
        protected int mResID;

        public AbsResBmpLoadItem setResId(int resId) {
            mLastResId = mResID;
            mResID = resId;
            return this;
        }

        boolean isChanged() {
            return mLastResId != mResID;
        }

        public int getResId() {
            return mResID;
        }


        int getLastResId() {
            return mLastResId;
        }
    }


    public static class ResFileViewBgLoadItem extends AbsResBmpLoadItem {

        private final View mView;

        public ResFileViewBgLoadItem(View view) {
            mView = view;
        }

        @Override
        protected void clearDrawable() {
            LogUtils.timerStart();
            LogUtils.timerEnd();
        }

        @Override
        protected void onSetDrawable(Drawable drawable) {
            LogUtils.timerStart();
            mView.setBackgroundDrawable(drawable);
            LogUtils.timerEnd();
        }

        @Override
        public String getKey() {
            return mView.toString();
        }
    }
}
