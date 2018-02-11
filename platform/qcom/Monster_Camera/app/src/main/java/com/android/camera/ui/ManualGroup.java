
package com.android.camera.ui;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.android.camera.CaptureLayoutHelper;
import com.android.camera.ShutterButton;
import com.android.camera.app.CameraAppUI;
import com.android.camera.widget.FloatingActionsMenu;
import com.tct.camera.R;

public class ManualGroup extends FrameLayout
        implements PreviewOverlay.OnPreviewTouchedListener,
        ShutterButton.OnShutterButtonListener, CameraAppUI.OnModeOptionsVisibilityChangedListener {
    private CaptureLayoutHelper mCaptureLayoutHelper = null;
    private FloatingActionsMenu mFloatingActionsMenu;

    public ManualGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mFloatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mCaptureLayoutHelper == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            RectF uncoveredPreviewRect = mCaptureLayoutHelper.getUncoveredPreviewRect();
            super.onMeasure(MeasureSpec.makeMeasureSpec(
                            (int) uncoveredPreviewRect.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec((int) uncoveredPreviewRect.height(),
                            MeasureSpec.EXACTLY));
        }

    }

    public void setCaptureLayoutHelper(CaptureLayoutHelper helper) {
        mCaptureLayoutHelper = helper;
    }

    @Override
    public void onPreviewTouched(MotionEvent ev) {
        mFloatingActionsMenu.collapse();
    }

    @Override
    public void onShutterButtonClick() {
        mFloatingActionsMenu.collapse();
    }

    @Override
    public void onShutterButtonLongClick() {
    }

    @Override
    public void onShutterCoordinate(TouchCoordinate coord) {
    }

    @Override
    public void onShutterButtonFocus(boolean pressed) {
        mFloatingActionsMenu.collapse();
    }
    @Override
    public void onModeOptionsVisibilityChanged(int vis) {
        setVisibility(vis);
    }
}
