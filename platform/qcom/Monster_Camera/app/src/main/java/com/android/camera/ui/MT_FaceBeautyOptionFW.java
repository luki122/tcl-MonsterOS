/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;
import com.tct.camera.R;
/**
 * Created by hoperun on 4/21/16.
 */
public class MT_FaceBeautyOptionFW extends  MT_FaceBeautyOption {

    public MT_FaceBeautyOptionFW(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSeekBar = (SeekBar) findViewById(R.id.customseekbar_mt);
        if (mSeekBar == null) {
            Log.d("MT_FaceBeautyOptionFW", "BZDB: mSeekBar==null");
        }
        mCustomSeekBar = findViewById(R.id.seekbar_mt_fw);
        if (mCustomSeekBar == null) {
            Log.d("MT_FaceBeautyOptionFW", "BZDB: mCustomSeekBar==null");
        }
        mFacebeautyMenu = (RotateImageView)findViewById(R.id.face_beauty_menu_mt_fw);
        if (mFacebeautyMenu == null) {
            Log.d("MT_FaceBeautyOptionFW", "BZDB: mFacebeautyMenu==null");
        }
        mFacebeautyMenu.setOnClickListener(this);
    }
}
