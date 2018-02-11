/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.filtershow.controller;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.editors.Editor;
import com.android.gallery3d.ui.CustomSeekBar;

public class TitledSlider implements Control {
    private final String LOGTAG = "ParametricEditor";
    private CustomSeekBar mSeekBar;
    private TextView mControlName;
    private TextView mControlValue;
    protected ParameterInteger mParameter;
    Editor mEditor;
    View mTopView;
    protected int mLayoutID = R.layout.filtershow_control_title_slider;

    @Override
    public void setUp(ViewGroup container, Parameter parameter, Editor editor) {
        container.removeAllViews();
        mEditor = editor;
        Context context = container.getContext();
        mParameter = (ParameterInteger) parameter;
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTopView = inflater.inflate(mLayoutID, container, true);
        mTopView.setVisibility(View.VISIBLE);
        mSeekBar = (CustomSeekBar) mTopView.findViewById(R.id.controlValueSeekBar);
        mControlName = (TextView) mTopView.findViewById(R.id.controlName);
        mControlValue = (TextView) mTopView.findViewById(R.id.controlValue);
        updateUI();
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSeekBar.onStopTouch();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSeekBar.onStartTouch();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mParameter != null) {
                    mParameter.setValue(progress + mParameter.getMinimum());
                    if (mControlName != null) {
                        mControlName.setText(mParameter.getParameterName());
                    }
                    if (mControlValue != null) {
                        mControlValue.setText(Integer.toString(mParameter.getValue()));
                    }
                    mEditor.commitLocalRepresentation();
                }
                mSeekBar.onProgress(mParameter.getValue());
            }
        });
        // TCL ShenQianfeng Begin on 2016.08.30
        // Annotated Below:
        /*
        mSeekBar.setScreenWidth(context.getResources().getDimensionPixelSize(
                R.dimen.photo_edit_graduated_seek_widht));
        */
        // TCL ShenQianfeng End on 2016.08.30
        mSeekBar.onWindowFocusChanged(mSeekBar.isFocused());
    }

    @Override
    public void setPrameter(Parameter parameter) {
        mParameter = (ParameterInteger) parameter;
        if (mSeekBar != null)
            updateUI();
    }

    @Override
    public void updateUI() {
        if (mControlName != null && mParameter.getParameterName() != null) {
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-26,PR908052 begin
            mControlName.setText(mParameter.getParameterName());
            //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-01-26,PR908052 end
        }
        if (mControlValue != null) {
            mControlValue.setText(
                    Integer.toString(mParameter.getValue()));
        }
        mSeekBar.setMax(mParameter.getMaximum() - mParameter.getMinimum());
        mSeekBar.setProgress(mParameter.getValue() - mParameter.getMinimum(), 0);
        mEditor.commitLocalRepresentation();
    }

    @Override
    public View getTopView() {
        return mTopView;
    }
}
