package com.xy.smartsms.iface;

import android.app.Activity;
import android.view.View;
import android.widget.ListView;
import mst.widget.MstListView;

public interface IXYSmartSmsHolder {

    public Activity getActivityContext();
    public boolean isEditAble();
    public MstListView getListView();
    public boolean isScrolling();
    public View findViewById(int viewId);
    public void onXyUiSmsEvent(int eventType);
    public boolean isNotifyComposeMessage();
    
}
