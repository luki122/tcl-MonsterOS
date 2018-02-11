package com.android.camera.ui;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.android.camera.CaptureLayoutHelper;
import com.tct.camera.R;

public class ManualGroupWrapper extends FrameLayout {

    private View mManualGroup;
    private CaptureLayoutHelper mCaptureLayoutHelper = null;

    public ManualGroupWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate() {
        mManualGroup = findViewById(R.id.manual_items_layout);
    }

    /**
     * Sets a capture layout helper to query layout rect from.
     */
    public void setCaptureLayoutHelper(CaptureLayoutHelper helper) {
        mCaptureLayoutHelper = helper;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mCaptureLayoutHelper == null) {
            return;
        }
        RectF uncoveredPreviewRect = mCaptureLayoutHelper.getUncoveredPreviewRect();

        mManualGroup.layout((int) uncoveredPreviewRect.left, (int) uncoveredPreviewRect.top,
                (int) uncoveredPreviewRect.right, (int) uncoveredPreviewRect.bottom);
    }
}
