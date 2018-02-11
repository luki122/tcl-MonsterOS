/* Copyright (C) 2016 Tcl Corporation Limited */
package com.leon.tools.view;

import android.content.Context;
import android.view.View;

/**
 * @author zhanghong
 */
public abstract class DataCoverUiController<ItemData> extends UiController {

    public DataCoverUiController(View view) {
        super(view);
    }

    public DataCoverUiController(Context context, int resLayout) {
        super(context, resLayout);
    }

    /**
     * @param data
     */
    public final void convertData(final ItemData data) {
        onConvertData(data);
    }

    /**
     * @param data
     * @param data
     */
    protected abstract void onConvertData(ItemData data);

}
