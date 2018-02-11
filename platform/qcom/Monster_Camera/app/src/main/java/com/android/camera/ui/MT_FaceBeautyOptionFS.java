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
public class MT_FaceBeautyOptionFS extends  MT_FaceBeautyOption {

    public MT_FaceBeautyOptionFS(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSeekBar = (SeekBar) findViewById(R.id.customseekbar_mt);
        if (mSeekBar == null) {
            Log.d("MT_FaceBeautyOptionFS", "BZDB: mSeekBar==null");
        }
        mCustomSeekBar = findViewById(R.id.seekbar_mt_fs);
        if (mCustomSeekBar == null) {
            Log.d("MT_FaceBeautyOptionFS", "BZDB: mCustomSeekBar==null");
        }

        mFacebeautyMenu = (RotateImageView)findViewById(R.id.face_beauty_menu_mt_fs);
        if (mFacebeautyMenu == null) {
            Log.d("MT_FaceBeautyOptionFS", "BZDB: mFacebeautyMenu==null");
        }
        mFacebeautyMenu.setOnClickListener(this);
    }
}
