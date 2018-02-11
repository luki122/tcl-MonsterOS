package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.android.camera.DeviceInfo;
import com.android.camera.app.AppController;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.tct.camera.R;

/**
 * Created by sdduser on 10/29/15.
 */
public class IntentReviewControls extends RelativeLayout implements View.OnClickListener{
    private final AppController mAppController;

    private ImageButton mCancelButton;
    private ImageButton mDoneButton;
    private ImageButton mRetakeButton;
    private ImageButton mReviewButton;

    private int mOrientation = 0;

    private boolean mFromVideoUI;
    private Context mContext;
    public IntentReviewControls(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAppController = (AppController) context;
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCancelButton = (ImageButton) this.findViewById(R.id.button_cancel);
        if (mCancelButton != null) {
            mCancelButton.setOnClickListener(this);
        }

        mDoneButton = (ImageButton) this.findViewById(R.id.button_done);
        if (mDoneButton != null) {
            mDoneButton.setOnClickListener(this);
        }

        if(CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_VDF_REVIEW_UI_CUSTOMIZE, true)){
            mDoneButton.setImageResource(R.drawable.ic_ok_vdf);
            mCancelButton.setImageResource(R.drawable.ic_cancel_vdf);
        }

        mRetakeButton = (ImageButton) this.findViewById(R.id.button_retake);
        if (mRetakeButton != null) {
            mRetakeButton.setOnClickListener(this);
        }


        mReviewButton = (ImageButton) this.findViewById(R.id.button_review);
        if (mReviewButton != null) {
            mReviewButton.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mCancelButton) {
            mAppController.intentReviewCancel();
        } else if (v == mDoneButton) {
            mAppController.intentReviewDone();
        } else if (v == mRetakeButton) {
            mAppController.intentReviewRetake();
        } else if (v == mReviewButton) {
            mAppController.intentReviewPlay();
        }
    }

    public void show(boolean showCancel, boolean showDone, boolean showRetake, boolean showReview) {
        this.setVisibility(View.VISIBLE);
        if (showCancel && mCancelButton != null) {
            mCancelButton.setVisibility(View.VISIBLE);
        }
        if (showDone && mDoneButton != null) {
            mDoneButton.setVisibility(View.VISIBLE);
        }
        if (showRetake && mRetakeButton != null) {
            mRetakeButton.setVisibility(View.VISIBLE);
        }
        if (showReview && mReviewButton != null) {
            mReviewButton.setVisibility(View.VISIBLE);
        }
    }

    public void hide() {
        this.setVisibility(View.GONE);
        if (mCancelButton != null) {
            mCancelButton.setVisibility(View.GONE);
        }
        if (mDoneButton != null) {
            mDoneButton.setVisibility(View.GONE);
        }
        if (mRetakeButton != null) {
            mRetakeButton.setVisibility(View.GONE);
        }
        if (mReviewButton != null) {
            mReviewButton.setVisibility(View.GONE);
        }
    }

    public void setLayoutOrientation(int ori) {
        if (mOrientation != ori) {
            mOrientation = ori;
            resetLayoutParam();
        }
    }

    private void resetLayoutParam() {
        int navBarHeight = mAppController.getCameraAppUI().getNavigationHeight();
        int padding = mFromVideoUI ? navBarHeight :
                navBarHeight + (int)mContext.getResources().getDimension(
                        R.dimen.intent_review_cancel_done_button_margin);

        if (padding == 0) return;
        switch (mOrientation) {
            case 0:
                this.setPadding(0, 0, 0, 0);
                break;
            case 90:
                if(mAppController.isReversibleWorking()){
                    this.setPadding(0, 0, padding, 0);
                }else {
                    this.setPadding(padding, 0, 0, 0);
                }
                break;
            case 180:
                if(DeviceInfo.isReversibleOn(mContext.getContentResolver())){
                    this.setPadding(0, 0, 0, 0);
                }else {
                    this.setPadding(0, padding, 0, 0);
                }
                break;
            case 270:
                if(mAppController.isReversibleWorking()){
                    this.setPadding(padding, 0, 0, 0);
                }else {
                    this.setPadding(0, 0, padding, 0);
                }
                break;
            default:
                this.setPadding(0, 0, 0, 0);
                break;
        }
        requestLayout();
    }

    public void setFromVideoUI(boolean fromVideoUI){
        this.mFromVideoUI = fromVideoUI;
    }
}
