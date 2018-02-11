/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.android.camera.debug.Log;
import com.tct.camera.R;

/**
 * Created by Sean Scott on 10/22/16.
 */
public class ZoomScalesBar extends CustomizeScalesBar {

    private static final Log.Tag TAG = new Log.Tag("ZoomScalesBar");

    private final int SCALE_STEP = 18;
    private final int DEFAULT_STEP = 0;
    private final int COMBINATION = 1;


    public ZoomScalesBar(Context context) {
        super(context);
    }

    public ZoomScalesBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZoomScalesBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean isScaleLineIllegal(ScaleLine line) {
        if (line == null) {
            return true;
        }
        return (line.startX != line.endX || line.startY >= line.endY);
    }

    @Override
    protected void setProperty() {
        Resources res = getResources();

        setGeneralProperty(SCALE_STEP, DEFAULT_STEP, COMBINATION);

        int scaleBarWidth = res.getDimensionPixelSize(R.dimen.zoom_scale_bar_width);
        int scaleBarHeight = res.getDimensionPixelSize(R.dimen.zoom_bar_height);
        int horizontalPadding = res.getDimensionPixelSize(R.dimen.zoom_scale_padding_horizontal);
        int verticalPadding = res.getDimensionPixelSize(R.dimen.zoom_scale_padding_vertical);
        setBarProperty(true, scaleBarWidth, scaleBarHeight, 0, 0, 0, 0,
                horizontalPadding, verticalPadding);

        int mainScaleHeight = res.getDimensionPixelSize(R.dimen.zoom_scale_main_height);
        int subScaleHeight = res.getDimensionPixelSize(R.dimen.zoom_scale_sub_height);
        int scaleWidth = res.getDimensionPixelSize(R.dimen.zoom_scale_width);
        int scaleInterval = res.getDimensionPixelSize(R.dimen.zoom_scale_interval);
        int selectedColor = res.getColor(R.color.zoom_sidebar_selected_color);
        int unselectedColor = res.getColor(R.color.zoom_sidebar_unselected_color);
        setScalesProperty(mainScaleHeight, subScaleHeight, scaleWidth,
                scaleInterval, selectedColor, unselectedColor);

        Drawable pointer = null; // No drawable available now.
        int pointerWidth = res.getDimensionPixelSize(R.dimen.zoom_scale_pointer_width);
        int pointerHeight = res.getDimensionPixelSize(R.dimen.zoom_scale_pointer_height);
        int pointerPadding = res.getDimensionPixelSize(R.dimen.zoom_scale_pointer_padding);
        int pointerColor = res.getColor(R.color.zoom_sidebar_pointer_color);
        setPointerProperty(pointer, pointerWidth, pointerHeight,
                pointerPadding, pointerColor);
    }

    @Override
    protected ScaleLine[] buildBasicScales() {
        int steps = getSteps();
        int mainScaleHeight = getMainScaleHeight();
        int subScaleHeight = getSubScaleHeight();
        int scaleWidth = getScaleWidth();
        int interval = getScaleInterval();

        ScaleLine[] scaleLines = new ScaleLine[steps + 1];
        int x = getHorizontalPadding();
        int centerV = (getBarHeight() - 2 * getVerticalPadding()) / 2;
        int startMainY = centerV - mainScaleHeight/2;
        int startSubY = centerV - subScaleHeight/2;
        int endMainY = startMainY + mainScaleHeight;
        int endSubY = startSubY + subScaleHeight;

        // The scale lines are layout from left to right.
        for (int index = 0; index <= steps; index ++) {
            if (isMainScale(index)) {
                scaleLines[index] = new ScaleLine(x, startMainY, x, endMainY);
            } else {
                scaleLines[index] = new ScaleLine(x, startSubY, x, endSubY);
            }
            x += (interval + scaleWidth);
        }
        return scaleLines;
    }

    @Override
    protected ScaleLine getScalePosition(int index) {
        if (index > getSteps()) {
            Log.e(TAG, "Index over step number.");
            return null;
        }
        ScaleLine[] scaleLines = getScales();
        if (scaleLines == null) {
            scaleLines = buildBasicScales();
        }
        return scaleLines[index];
    }

    @Override
    protected Path getPointerPath(int index) {
        // The position for certain pointer triangle is fixed and
        // won't be change because of the animation.
        ScaleLine line = getScalePosition(index);
        if (isScaleLineIllegal(line)) {
            return null;
        }

        int pointerWidth = getPointerWidth();
        int pointerHeight = getPointerHeight();
        int pointerPadding = getPointerPadding();

        PointF a = new PointF();
        PointF b = new PointF();
        PointF c = new PointF();

        a.x = line.startX;
        b.x = a.x - pointerWidth/2;
        c.x = b.x + pointerWidth;
        c.y = pointerPadding;
        b.y = c.y;
        a.y = c.y + pointerHeight;

        Path trianglePath = new Path();
        trianglePath.moveTo(a.x, a.y);
        trianglePath.lineTo(b.x, b.y);
        trianglePath.lineTo(c.x, c.y);
        trianglePath.close();

        return trianglePath;
    }

    @Override
    protected Rect getPointerBound(int index) {
        ScaleLine line = getScalePosition(index);
        if (isScaleLineIllegal(line)) {
            return new Rect();
        }

        int pointerWidth = getPointerWidth();
        int pointerHeight = getPointerHeight();
        int pointerPadding = getPointerPadding();
        int left, top, right, bottom;

        left = (int) (line.startX - pointerWidth/2);
        top = pointerPadding;
        right = left + pointerWidth;
        bottom = top + pointerHeight;

        return new Rect(left, top, right, bottom);
    }
}
