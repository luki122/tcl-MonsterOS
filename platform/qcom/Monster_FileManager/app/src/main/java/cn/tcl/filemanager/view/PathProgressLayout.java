/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.tcl.filemanager.R;

/**
 * Created by hftang on 2/1/16.
 */
public class PathProgressLayout extends LinearLayout {

    private static final String TAG = PathProgressLayout.class.getSimpleName();
    private Context mContext;
    private RoundProgressBar mSizePercentPb;
    private TextView mSizePercent;
    private TextView mSizePercentMark;
    private TextView mSizeDetail;
    private TextView mPathName;
    private float progressBarWidth;
    private ImageView mIcPathIv;
    private ProgressBar mProgressBar;





    public PathProgressLayout(Context context) {
        this(context, null);
    }

    public PathProgressLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PathProgressLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PathProgressLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.path_progress_layout, this, true);
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.PathProgress);
        progressBarWidth = typedArray.getDimension(R.styleable.PathProgress_progress_width, 100);
        typedArray.recycle();
        // String str = String.format(getResources().getString(R.string.xx),getCurrentSize());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mSizePercent = (TextView) findViewById(R.id.size_percent);
        mSizeDetail = (TextView) findViewById(R.id.size);
        mPathName = (TextView) findViewById(R.id.path_name);
        mIcPathIv = (ImageView) findViewById(R.id.path_icon);
        mSizePercentMark = (TextView) findViewById(R.id.size_percent_mark);
        mProgressBar=(ProgressBar)findViewById(R.id.pb_progressbar);

    }



    public void setIcon(int icon) {
        this.mIcPathIv.setImageResource(icon);
    }

    public void setPathNameText(int pathNameTextId) {
        this.mPathName.setText(pathNameTextId);
    }

    public void setUsedSize(String freespace ) {
        mSizePercent.setText(freespace);
    }

    public void setProgressNew(int progress){
        mProgressBar.setProgress(progress);
    }

}
