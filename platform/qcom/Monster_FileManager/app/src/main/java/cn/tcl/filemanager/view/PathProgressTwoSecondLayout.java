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
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.StorageQueryUtils;

/**
 * Created by hftang on 2/1/16.
 */
public class PathProgressTwoSecondLayout extends LinearLayout {

    private static final String TAG = PathProgressLayout.class.getSimpleName();
    private Context mContext;
    private TextView mFirstView;
    private TextView mSecondStorageView;
    private ProgressBar mProgressBar;
    private StorageQueryUtils storageQueryUtils;
    private MountManager mMountManager;

    public PathProgressTwoSecondLayout(Context context) {
        this(context, null);
    }

    public PathProgressTwoSecondLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PathProgressTwoSecondLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PathProgressTwoSecondLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.path_progress_two_second_layout, this, true);
        // String str = String.format(getResources().getString(R.string.xx),getCurrentSize());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int mPercent = 0;
        mMountManager = new MountManager();
        storageQueryUtils = new StorageQueryUtils(getContext());
        mFirstView = (TextView) findViewById(R.id.second);
        mSecondStorageView = (TextView) findViewById(R.id.second_storage);
        mProgressBar = (ProgressBar) findViewById(R.id.second_progressbar);
        if(mMountManager.isSDCardMounted()) {
            mPercent = (int) (((storageQueryUtils.getSdTolSize() - storageQueryUtils.getSdAvailableSize()) / (float) storageQueryUtils.getSdTolSize()) * 100);
        }
        setProgressNew(mPercent);
    }

    public void setSdStorage(String storage) {
        mSecondStorageView.setText(mContext.getString(R.string.free_space) + storage);
    }

    public void setProgressNew(int progress) {
        mProgressBar.setProgress(progress);
    }
}