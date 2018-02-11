/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.tcl.transfer.R;
import mst.app.MstActivity;

public class BaseActivity extends MstActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onNavigationClicked(View view) {
        this.finish();
    }
}
