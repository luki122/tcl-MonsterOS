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

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.utils.StorageQueryUtils;

/**
 * Created by hftang on 2/1/16.
 */
public class PathProgressTwoFirstLayout extends LinearLayout {

    private static final String TAG = cn.tcl.filemanager.view.PathProgressLayout.class.getSimpleName();
    private Context mContext;
    private TextView mFirstView;
    private TextView mFirstStorageView;
    private ProgressBar mProgressBar;
    private StorageQueryUtils storageQueryUtils;

    public PathProgressTwoFirstLayout(Context context) {
        this(context, null);
    }

    public PathProgressTwoFirstLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PathProgressTwoFirstLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PathProgressTwoFirstLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.path_progress_two_first_layout, this, true);
        // String str = String.format(getResources().getString(R.string.xx),getCurrentSize());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        storageQueryUtils = new StorageQueryUtils(getContext());
        mFirstView = (TextView) findViewById(R.id.first);
        mFirstStorageView = (TextView) findViewById(R.id.first_storage);
        mProgressBar = (ProgressBar) findViewById(R.id.first_progressbar);
        int mPercent = (int) (((storageQueryUtils.getPhoneTolSize() - storageQueryUtils.getPhoneAvailableSize()) / (float) storageQueryUtils.getPhoneTolSize()) * 100);
        setProgressNew(mPercent);
    }

    public void setPhoneOrSdName(String name){
        mFirstView.setText(name);
    }

    public void setPhoneStorage(String storage) {
        mFirstStorageView.setText(mContext.getString(R.string.free_space) + storage);
    }

    public void setProgressNew(int progress) {
        mProgressBar.setProgress(progress);
    }
}