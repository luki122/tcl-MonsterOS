package com.android.gallery3d.ui;

import android.view.MenuItem;
import android.view.View;

public interface MstToolBarListener {
    //tool bar back
    public void onMstToolbarNavigationClicked(View view);
    public boolean onMstToolbarMenuItemClicked(MenuItem item);
}
