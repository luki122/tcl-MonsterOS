package com.android.camera.ui;

import android.widget.Adapter;

import com.android.camera.app.ModuleManager;

/**
 * Created by sichao.hu on 11/17/15.
 */
public interface ModeStrip  extends Lockable{

    public static interface OnModeIdListener {
        public void onModeIdChanging();
        public void onModeIdChanged(int id);
    }


    public void init(ModuleManager moduleManager);

    public void setAdapter(Adapter adapter);

    public void notifyDatasetChanged();

    public void setModeIndexChangeListener(OnModeIdListener listener);

    public void attachScrollIndicator(ScrollIndicator scrollIndicator);

    public Integer lockView();

    public boolean unLockView(Integer token);

    public void setCurrentModeWithModeIndex(int modeIndex);

    public boolean isLocked();

    public void pause();

    public void resume();


}
