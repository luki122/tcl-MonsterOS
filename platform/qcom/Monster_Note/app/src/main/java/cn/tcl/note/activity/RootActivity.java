/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.activity;

import android.view.View;

import mst.app.MstActivity;
import mst.widget.toolbar.Toolbar;

public class RootActivity extends MstActivity {
    protected Toolbar mToolBar;

    protected void initToolBar(int title) {
        mToolBar = getToolbar();
        mToolBar.setTitle(title);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
