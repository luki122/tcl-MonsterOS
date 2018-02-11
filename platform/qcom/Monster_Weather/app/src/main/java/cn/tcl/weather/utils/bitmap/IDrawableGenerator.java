package cn.tcl.weather.utils.bitmap;

import android.graphics.drawable.Drawable;

import com.gapp.common.obj.IManager;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-11-16.
 * $desc
 */
public interface IDrawableGenerator<T extends IBmpLoadManager.IBmpLoadItem> extends IManager {

    /**
     * @param item
     * @return a drawable. that can generate
     */
    void regiestLoadItem(T item);
    /**
     * unregiest item
     *
     * @param item
     */
    void unregiestLoadItem(T item);

}
