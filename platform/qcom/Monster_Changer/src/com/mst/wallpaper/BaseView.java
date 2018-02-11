package com.mst.wallpaper;

import android.content.Context;

public interface BaseView<T> {

    void setPresenter(T presenter);

    Context getContext();
    
    void updateView(Object... resout);
}
