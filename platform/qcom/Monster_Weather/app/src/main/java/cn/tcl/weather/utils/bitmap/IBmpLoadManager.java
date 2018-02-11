package cn.tcl.weather.utils.bitmap;

import android.app.Activity;

import com.gapp.common.obj.IManager;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-11-16.
 * $desc
 */
public interface IBmpLoadManager<T extends IBmpLoadManager.IBmpLoadItem> extends IManager {

    /**
     * load bmp
     *
     * @param activity the actvity you loaded
     * @param Item
     */
    void loadBmp(Activity activity, T Item);

    interface IBmpLoadItem extends Runnable {
        String getKey();
    }
}
