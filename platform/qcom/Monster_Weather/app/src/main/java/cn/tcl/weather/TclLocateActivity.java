/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import cn.tcl.weather.bean.City;
import cn.tcl.weather.service.UpdateService;
import cn.tcl.weather.viewhelper.AbsAddCityVh;
import cn.tcl.weather.viewhelper.VhFactory;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-9.
 * add or search city
 */
public class TclLocateActivity extends TclBaseActivity implements ILocateActivity{

    private AbsAddCityVh mAddCityVh;
    private InputMethodManager mInputMethodManager;
    private String TAG = "TclLocateActivity";

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            // Judge whether is edittext
            if (isShouldHideSoftInput(view, ev)) {
                hideSoftInputFromWindow(view);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * Judge whether hide soft input
     *
     * @param view  The clicked view
     * @param event Motion event
     * @return
     */
    public boolean isShouldHideSoftInput(View view, MotionEvent event) {
        if (view != null && (view instanceof EditText)) {
            int[] leftTop = {0, 0};
            view.getLocationOnScreen(leftTop);

            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + view.getBottom();
            int right = left + view.getWidth();

            if (event.getRawX() > left && event.getRawX() < right && event.getRawY() > top && event.getRawY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public void hideSoftInputFromWindow(View view) {
        if(mInputMethodManager.isActive())
            mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAddCityVh = VhFactory.newVhInstance(VhFactory.ADD_CITY_VH, this);
        mAddCityVh.init();
        setContentView(mAddCityVh.getView());
        bindUpdateService(this);
        // Get input manager, hide it
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }


    private void bindUpdateService(Context context) {
        Intent bindServiceIntent = new Intent(context, UpdateService.class);
        context.bindService(bindServiceIntent, mSerivceConn, Context.BIND_AUTO_CREATE);
    }

    private void unbindUpdateService(Context context) {
        context.unbindService(mSerivceConn);
    }


    @Override
    protected void onDestroy() {
        unbindUpdateService(this);
        mAddCityVh.recycle();
        super.onDestroy();
    }

    private ServiceConnection mSerivceConn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAddCityVh.setUpdateService(((UpdateService.UpdateBinder) service).getService());
        }
    };


    @Override
    public void addCity(City city) {
        mAddCityVh.addCity(city);
    }
}
