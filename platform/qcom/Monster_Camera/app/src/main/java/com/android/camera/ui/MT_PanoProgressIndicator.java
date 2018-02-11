package com.android.camera.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.tct.camera.R;

/**
 * Created by hoperun on 12/25/15.
 */
public class MT_PanoProgressIndicator {
    private static final String TAG = "ProgressIndicator";

    private static int sIndicatorMarginLong = 0;
    private static int sIndicatorMarginShort = 0;
    private int mBlockPadding = 0;
    public int mBlockNumber = 9;

    public View mProgressView;
    public ImageView mProgressBars;

    public MT_PanoProgressIndicator(Activity activity, int blockNumber, int[] drawBlockSizes) {
        mBlockPadding = 4;

        mProgressView = activity.findViewById(R.id.progress_indicator);
        mProgressView.setVisibility(View.VISIBLE);
        mProgressBars = (ImageView) activity.findViewById(R.id.progress_bars);
        mBlockNumber = blockNumber;
        int[] blockSizes = new int[blockNumber];
        System.arraycopy(drawBlockSizes, 0, blockSizes, 0, blockNumber);
        Resources res = activity.getResources();
        final float scale = res.getDisplayMetrics().density;
        if (scale != 1.0f) {
            mBlockPadding = (int) (mBlockPadding * scale + 0.5f);
            for (int i = 0; i < mBlockNumber; i++) {
                blockSizes[i] = (int) (drawBlockSizes[i] * scale + 0.5f);
            }
        }
        mProgressBars.setImageDrawable(new MT_PanoProgressBarDrawable(activity, mProgressBars,
                blockSizes, mBlockPadding));
        getIndicatorMargin();
    }

    public MT_PanoProgressIndicator(Activity activity) {
        mProgressView = activity.findViewById(R.id.progress_indicator);
        if (mProgressView == null) {
            Log.w(TAG, "mProgressView is null,return!");
            return;
        }
        mProgressView.setVisibility(View.VISIBLE);
        mProgressBars = (ImageView) activity.findViewById(R.id.progress_bars);
    }

    public void setVisibility(int visibility) {
        mProgressView.setVisibility(visibility);
    }

    public void setProgress(int progress) {
        mProgressBars.setImageLevel(progress);
    }

    private void getIndicatorMargin() {
        if (sIndicatorMarginLong == 0 && sIndicatorMarginShort == 0) {
            Resources res = mProgressView.getResources();
            sIndicatorMarginLong = res
                    .getDimensionPixelSize(R.dimen.pano_progress_indicator_bottom_long);
            sIndicatorMarginShort = res
                    .getDimensionPixelSize(R.dimen.pano_progress_indicator_bottom_short);
        }
        Log.d(TAG, "[getIndicatorMargin]sIndicatorMarginLong = " + sIndicatorMarginLong
                + " sIndicatorMarginShort = " + sIndicatorMarginShort);
    }

    public void setOrientation(int orientation) {
        LinearLayout progressViewLayout = (LinearLayout) mProgressView;
        RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(
                progressViewLayout.getLayoutParams());
        int activityOrientation = mProgressView.getResources().getConfiguration().orientation;
        if ((Configuration.ORIENTATION_LANDSCAPE == activityOrientation && (orientation == 0 || orientation == 180))
                || (Configuration.ORIENTATION_PORTRAIT == activityOrientation && (orientation == 90 || orientation == 270))) {
            rp.setMargins(rp.leftMargin, rp.topMargin, rp.rightMargin, sIndicatorMarginShort);
        } else {
            rp.setMargins(rp.leftMargin, rp.topMargin, rp.rightMargin, sIndicatorMarginLong);
        }

        rp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        progressViewLayout.setLayoutParams(rp);
        progressViewLayout.requestLayout();
    }
}