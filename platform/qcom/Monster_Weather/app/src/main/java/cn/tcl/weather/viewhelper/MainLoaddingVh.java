package cn.tcl.weather.viewhelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.leon.tools.view.UiController;

import cn.tcl.weather.MainActivity;
import cn.tcl.weather.R;
import cn.tcl.weather.service.UpdateService;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-9-27.
 * $desc
 */
public class MainLoaddingVh extends UiController implements MainActivity.IMainVh {

    private MainActivity mActivity;
    private Bitmap mBitmap;

    public MainLoaddingVh(MainActivity activity) {
        super(activity, R.layout.other_first_run_layout);
        mActivity = activity;
    }

    @Override
    public void init() {
        ((TextView) findViewById(R.id.hint)).setText(mActivity.getString(R.string.loading_data));
        findViewById(R.id.user_guidance).setVisibility(View.GONE);
        mBitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.smile);
        ((ImageView) findViewById(R.id.locate_state)).setImageBitmap(mBitmap);
    }

    @Override
    public void setUpdateService(UpdateService updateService) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void recycle() {
        mBitmap.recycle();
    }
}
