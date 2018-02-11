package com.android.camera.ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tct.camera.R;

/**
 * Created by sdduser on 10/13/15.
 */
public class RecordTimeLayout extends RotateLayout {
    private TextView mRecordingTime;
    private TextView mTimeLeftView;
    private final static int[] mMarginTop = new int[4];
    private final static int[] mMarginLeft = new int[1];
    private final static int[] mMarginBottom = new int[1];
    private final static int[] mMarginRight = new int[2];

    public RecordTimeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = getResources();
        mMarginTop[0] = res.getDimensionPixelSize(R.dimen.video_recordtime_margin_top_ori_0);
        mMarginTop[1] = res.getDimensionPixelSize(R.dimen.video_recordtime_margin_top_ori_90);
        mMarginTop[2] =
                // ZoomBar.ROTATABLE ? res.getDimensionPixelSize(R.dimen.video_recordtime_margin_top_ori_180) :
                res.getDimensionPixelSize(R.dimen.video_recordtime_margin_top_ori_180) +
                        res.getDimensionPixelSize(R.dimen.video_recordtime_margin_top_ori_180_offset);
        mMarginTop[3] = res.getDimensionPixelSize(R.dimen.video_recordtime_margin_top_ori_270);
        mMarginLeft[0] = res.getDimensionPixelSize(R.dimen.video_recordtime_margin_left_ori_0);
        mMarginBottom[0] = res.getDimensionPixelSize(R.dimen.video_recordtime_margin_bottom_ori_0);
        mMarginRight[0] = res.getDimensionPixelSize(R.dimen.video_recordtime_margin_right_ori_0);

        mMarginRight[1] =
//                ZoomBar.ROTATABLE ? res.getDimensionPixelSize(R.dimen.video_recordtime_margin_right_ori_270) :
                res.getDimensionPixelSize(R.dimen.video_recordtime_margin_right_ori_270) +
                        res.getDimensionPixelSize(R.dimen.video_recordtime_margin_top_ori_180_offset);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRecordingTime = (TextView)mChild.findViewById(R.id.recording_time);
        mTimeLeftView = (TextView)mChild.findViewById(R.id.time_left_view);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        setRecordingTimeLocation(getOrientation());
        super.onMeasure(widthSpec, heightSpec);
    }

    private void setRecordingTimeLocation(int orientation) {
        if(mRecordingTime == null) return;

        RelativeLayout.LayoutParams mRecordingTimeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        RelativeLayout.LayoutParams mTimeLeftViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
//        params.gravity = Gravity.CENTER;
        mRecordingTimeParams.leftMargin
                = mTimeLeftViewParams.leftMargin
                = mMarginLeft[0];
        mRecordingTimeParams.rightMargin
                = mTimeLeftViewParams.rightMargin
                = mMarginRight[0];
        mRecordingTimeParams.bottomMargin
                = mTimeLeftViewParams.bottomMargin
                = mMarginBottom[0];
        if(orientation == 0){
            mRecordingTimeParams.topMargin
                    = mTimeLeftViewParams.topMargin
                    = mMarginTop[0];
        }else if(orientation == 90){
            mRecordingTimeParams.topMargin
                    = mTimeLeftViewParams.topMargin
                    = mMarginTop[1];
        } else if(orientation == 180 ){
            mRecordingTimeParams.topMargin
                    = mTimeLeftViewParams.topMargin
                    = mMarginTop[2];
        }else if (orientation == 270){
            mRecordingTimeParams.topMargin
                    = mTimeLeftViewParams.topMargin
                    = mMarginTop[3];
            mTimeLeftViewParams.rightMargin = mMarginRight[1];
        }
        mRecordingTimeParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRecordingTimeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mRecordingTime.setLayoutParams(mRecordingTimeParams);

        mTimeLeftViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mTimeLeftViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mTimeLeftView.setLayoutParams(mTimeLeftViewParams);
    }
}
