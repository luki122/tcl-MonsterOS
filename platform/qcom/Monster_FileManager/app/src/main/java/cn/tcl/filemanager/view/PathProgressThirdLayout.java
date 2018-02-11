/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.manager.MountManager;

public class PathProgressThirdLayout extends LinearLayout {

    private static final String TAG = PathProgressLayout.class.getSimpleName();
    private Context mContext;
    private TextView mFirstView;
    private TextView mSecondStorageView;
    private ProgressBar mProgressBar;


    public PathProgressThirdLayout(Context context) {
        this(context, null);
    }

    public PathProgressThirdLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PathProgressThirdLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PathProgressThirdLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.path_progress_two_third_layout, this, true);
        // String str = String.format(getResources().getString(R.string.xx),getCurrentSize());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mFirstView = (TextView) findViewById(R.id.third);
        mSecondStorageView = (TextView) findViewById(R.id.third_storage);
        mProgressBar = (ProgressBar) findViewById(R.id.third_progressbar);
    }

    public void setSdAndExternalStorage(String storage) {
        mSecondStorageView.setText(mContext.getString(R.string.free_space) + storage);
    }

    public void setProgressNew(int progress) {
        mProgressBar.setProgress(progress);
    }
}