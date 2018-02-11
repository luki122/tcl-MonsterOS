/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.android.camera.util.CameraUtil;
import com.tct.camera.R;

import java.util.Locale;

/**
 * Created by sdduser on 16-4-1.
 */
public class CameraKeyHelpTipCling  extends HelpTipCling{
    private Button mArabicCameraKeyBtn;
    private Button mCameraKeyBtn;

    public CameraKeyHelpTipCling(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mArabicCameraKeyBtn = (Button)findViewById(R.id.camerakey_next);
        mCameraKeyBtn = (Button)findViewById(R.id.next);

        if(mIsLayoutDirectionRtl) {
            if(mArabicCameraKeyBtn != null){
                mArabicCameraKeyBtn.setVisibility(View.VISIBLE);
            }

            if(mCameraKeyBtn !=null){
                mCameraKeyBtn.setVisibility(View.GONE);
            }
        }else {
            if(mArabicCameraKeyBtn != null){
                mArabicCameraKeyBtn.setVisibility(View.GONE);
            }

            if(mCameraKeyBtn !=null){
                mCameraKeyBtn.setVisibility(View.VISIBLE);
            }
        }
    }
}
