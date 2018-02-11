/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.activity;

import android.os.Bundle;

import cn.tcl.note.R;

public class ThemeActivity extends RootActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_theme);
        initToolBar(R.string.toolbar_theme);
    }
}
