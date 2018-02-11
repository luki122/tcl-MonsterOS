/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.viewhelper;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.gapp.common.utils.BitmapManager;
import com.leon.tools.view.AndroidUtils;
import com.leon.tools.view.UiController;

import cn.tcl.weather.ActivityFactory;
import cn.tcl.weather.MainActivity;
import cn.tcl.weather.R;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.service.ILocator;
import cn.tcl.weather.service.UpdateService;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-5.
 * $desc
 */
public class OtherFirstRunVh extends UiController implements MainActivity.IMainVh {
    private BitmapManager mBitmapMannager;
    private MainActivity mActivity;
    private Handler mHandler = new Handler();

    OtherFirstRunVh(MainActivity activity) {
        super(activity, R.layout.other_first_run_layout);
        mActivity = activity;
    }

    @Override
    public void init() {
        mBitmapMannager = new BitmapManager(mActivity);
        mBitmapMannager.init();
        if (!mActivity.requestPermissions()) {
            AndroidUtils.sendMessageCallback(MainActivity.REQUEST_CODE_LOCATION, UpdateService.START_LOCATING, null);
        }
    }

    private Runnable mShowToggleButtton = new Runnable() {
        @Override
        public void run() {
            toggleButton(mActivity.getString(R.string.manual_add_city));
            findViewById(R.id.user_guidance).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent intent = new Intent(mActivity, LocateActivity.class);
                    //mActivity.startActivity(intent);
                    ActivityFactory.jumpToActivity(ActivityFactory.LOCATE_ACTIVITY, mActivity, null);
                }
            });
        }
    };

    private ILocator.LocateObserver mLocateObserver = new ILocator.LocateObserver() {
        @Override
        public void onLocating(int state) {
            Button btn = findViewById(R.id.user_guidance);
            mHandler.removeCallbacks(mShowToggleButtton);// rmove toggle button show

            if (state == ILocator.STATE_LOCATING) {
                showText(mActivity.getString(R.string.loading_data));
                showImage(R.drawable.smile);
                toggleButton("");
                mHandler.postDelayed(mShowToggleButtton, 15 * 1000);//delay 15 seconds show toggle button
            } else if (state == ILocator.STATE_FAILED_USELESS_NETWORK || state == ILocator.STATE_REQUEST_FAILED) {
                showText((state == ILocator.STATE_FAILED_USELESS_NETWORK) ? mActivity.getString(R.string.network_failed) : mActivity.getString(R.string.try_again));
                toggleButton(mActivity.getString(R.string.try_again));
                showImage(R.drawable.sad);

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Restart locate
                        WeatherCNApplication.getWeatherCnApplication().sendMessage(UpdateService.START_LOCATING, null);
                    }
                });
            } else if (state == ILocator.STATE_FAILED_LOCATE_FAILED || state == ILocator.STATE_FAILED_NO_PERMISSION) {
                showText(mActivity.getString(R.string.locate_failed));
                toggleButton(mActivity.getString(R.string.manual_add_city));
                showImage(R.drawable.sad);

                // If user haven't permission to locate
                if (!AndroidUtils.grantedPermission(mActivity, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    WeatherCNApplication.getWeatherCnApplication().sendMessage(UpdateService.START_LOCATING, null);
                }
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Intent intent = new Intent(mActivity, LocateActivity.class);
                        //mActivity.startActivity(intent);
                        ActivityFactory.jumpToActivity(ActivityFactory.LOCATE_ACTIVITY, mActivity, null);
                    }
                });
            }
        }
    };

    @Override
    public void setUpdateService(UpdateService updateService) {
        updateService.regiestLocateObserver(mLocateObserver);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {

    }

    public void recycle() {
        mBitmapMannager.recycle();
    }

    public void showImage(int resID) {
        Bitmap bitmap = mBitmapMannager.generateBitmap(resID);
        ImageView iv = findViewById(R.id.locate_state);
        iv.setImageBitmap(bitmap);
    }

    public void showText(String hint) {
        setTextToTextView(R.id.hint, hint);
    }

    public void toggleButton(String message) {
        Button btn = findViewById(R.id.user_guidance);
        btn.setVisibility(TextUtils.isEmpty(message) ? View.GONE : View.VISIBLE);
        btn.setText(message);
    }
}