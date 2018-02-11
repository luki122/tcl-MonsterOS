/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.monster.launcher.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.monster.launcher.IChangeColors;
import com.monster.launcher.Log;

import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monster.launcher.DeviceProfile;
import com.monster.launcher.InvariantDeviceProfile;
import com.monster.launcher.ItemInfo;
import com.monster.launcher.Launcher;
import com.monster.launcher.LauncherAppState;
import com.monster.launcher.LauncherAppWidgetProviderInfo;
import com.monster.launcher.R;
import com.monster.launcher.StylusEventHelper;
import com.monster.launcher.Utilities;
import com.monster.launcher.WidgetPreviewLoader;
import com.monster.launcher.compat.AppWidgetManagerCompat;

/**
 * Represents the individual cell of the widget inside the widget tray. The preview is drawn
 * horizontally centered, and scaled down if needed.
 *
 * This view does not support padding. Since the image is scaled down to fit the view, padding will
 * further decrease the scaling factor. Drag-n-drop uses the view bounds for showing a smooth
 * transition from the view to drag view, so when adding padding support, DnD would need to
 * consider the appropriate scaling factor.
 */
public class WidgetCell extends LinearLayout implements OnLayoutChangeListener,IChangeColors.IItemColorChange {

    private static final String TAG = "WidgetCell";
    private static final boolean DEBUG = false;

    private static final int FADE_IN_DURATION_MS = 90;

    /** Widget cell width is calculated by multiplying this factor to grid cell width. */
    private static final float WIDTH_SCALE = 0.96f;//lijun modify 2.6 to 1.0

    /** Widget preview width is calculated by multiplying this factor to the widget cell width. */
    public static final float PREVIEW_SCALE = 0.96f;//lijun modify : this need < 1.0

    private int mPresetPreviewSize;
    int cellSize;

    private WidgetImageView mWidgetImage;
    private TextView mWidgetName;
//    private TextView mWidgetDims;

    private String mDimensionsFormatString;
    private Object mInfo;

    private WidgetPreviewLoader mWidgetPreviewLoader;
    private WidgetPreviewLoader.PreviewLoadRequest mActiveRequest;
    private StylusEventHelper mStylusEventHelper;

    private Launcher mLauncher;

    public WidgetCell(Context context) {
        this(context, null);
    }

    public WidgetCell(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WidgetCell(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final Resources r = context.getResources();
        mLauncher = (Launcher) context;
        mStylusEventHelper = new StylusEventHelper(this);

        mDimensionsFormatString = r.getString(R.string.widget_dims_format);
        setContainerWidth();
        setWillNotDraw(false);
        setClipToPadding(false);
        setAccessibilityDelegate(LauncherAppState.getInstance().getAccessibilityDelegate());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
            final int widgetImageHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mPresetPreviewSize, MeasureSpec.EXACTLY);
            Paint textPaint = new Paint();
            DisplayMetrics dm = getResources().getDisplayMetrics();
            textPaint.setTextSize(Utilities.pxFromSp(11, dm));
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            int textHeight = (int) (fm.bottom - fm.top);
            final int widgetNameHeightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(textHeight, MeasureSpec.EXACTLY);

            mWidgetImage.measure(childWidthMeasureSpec, widgetImageHeightMeasureSpec);
            mWidgetName.measure(childWidthMeasureSpec, widgetNameHeightMeasureSpec);
//            this.setMeasuredDimension(widthSize,mPresetPreviewSize+textHeight+24);
        }

    }

    private void setContainerWidth() {
        DeviceProfile profile = mLauncher.getDeviceProfile();
        cellSize = (int) (profile.cellWidthPx * WIDTH_SCALE);
        mPresetPreviewSize = (int) (cellSize * PREVIEW_SCALE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mWidgetImage = (WidgetImageView) findViewById(R.id.widget_preview);
        mWidgetName = ((TextView) findViewById(R.id.widget_name));
//        mWidgetDims = ((TextView) findViewById(R.id.widget_dims));
    }

    /**
     * Called to clear the view and free attached resources. (e.g., {@link Bitmap}
     */
    public void clear() {
        if (DEBUG) {
            Log.d(TAG, "reset called on:" + mWidgetName.getText());
        }
        mWidgetImage.animate().cancel();
        mWidgetImage.setBitmap(null);
        mWidgetName.setText(null);
//        mWidgetDims.setText(null);

        if (mActiveRequest != null) {
            mActiveRequest.cleanup();
            mActiveRequest = null;
        }
    }

    /**
     * Apply the widget provider info to the view.
     */
    public void applyFromAppWidgetProviderInfo(LauncherAppWidgetProviderInfo info,
            WidgetPreviewLoader loader) {

        InvariantDeviceProfile profile =
                LauncherAppState.getInstance().getInvariantDeviceProfile();
        mInfo = info;
        // TODO(hyunyoungs): setup a cache for these labels.
        int hSpan = Math.min(info.spanX, profile.numColumns);
        int vSpan = Math.min(info.spanY, profile.numRows);
        String widgetName = AppWidgetManagerCompat.getInstance(getContext()).loadLabel(info);
        String widgetDims = String.format(mDimensionsFormatString, hSpan, vSpan);
//        mWidgetName.setText(AppWidgetManagerCompat.getInstance(getContext()).loadLabel(info));
        mWidgetName.setText(mergeDimsToName(widgetName,widgetDims));
//        mWidgetDims.setText(String.format(mDimensionsFormatString, hSpan, vSpan));
        mWidgetPreviewLoader = loader;
        setColors(LauncherAppState.getInstance().getWindowGlobalVaule().getTextColor());
    }

    /**
     * Apply the resolve info to the view.
     */
    public void applyFromResolveInfo(
            PackageManager pm, ResolveInfo info, WidgetPreviewLoader loader) {
        mInfo = info;
        CharSequence label = info.loadLabel(pm);
//        mWidgetName.setText(label);
        mWidgetName.setText(mergeDimsToName(label.toString(),String.format(mDimensionsFormatString, 1, 1)));
//        mWidgetDims.setText(String.format(mDimensionsFormatString, 1, 1));
        mWidgetPreviewLoader = loader;
        setColors(LauncherAppState.getInstance().getWindowGlobalVaule().getTextColor());
    }

    public int[] getPreviewSize() {
        int[] maxSize = new int[2];

        maxSize[0] = mPresetPreviewSize;
        maxSize[1] = mPresetPreviewSize;
        return maxSize;
    }

    public void applyPreview(Bitmap bitmap) {
        if (bitmap != null) {
            mWidgetImage.setBitmap(bitmap);
            mWidgetImage.setAlpha(0f);
            ViewPropertyAnimator anim = mWidgetImage.animate();
            anim.alpha(1.0f).setDuration(FADE_IN_DURATION_MS);
        }
    }

    public void ensurePreview() {
        if (mActiveRequest != null) {
            return;
        }
        int[] size = getPreviewSize();
        if (DEBUG) {
            Log.d(TAG, String.format("[tag=%s] ensurePreview (%d, %d):",
                    getTagToString(), size[0], size[1]));
        }
        mActiveRequest = mWidgetPreviewLoader.getPreview(mInfo, size[0], size[1], this);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
            int oldTop, int oldRight, int oldBottom) {
        removeOnLayoutChangeListener(this);
        ensurePreview();
    }

    public int getActualItemWidth() {
        ItemInfo info = (ItemInfo) getTag();
        int[] size = getPreviewSize();
        int cellWidth = mLauncher.getDeviceProfile().cellWidthPx;

        return Math.min(size[0], info.spanX * cellWidth);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = super.onTouchEvent(ev);
        if (mStylusEventHelper.checkAndPerformStylusEvent(ev)) {
            return true;
        }
        return handled;
    }

    /**
     * Helper method to get the string info of the tag.
     */
    private String getTagToString() {
        if (getTag() instanceof PendingAddWidgetInfo ||
                getTag() instanceof PendingAddShortcutInfo) {
            return getTag().toString();
        }
        return "";
    }

    @Override
    public void changeColors(int[] colors) {
        Log.i("cbb","changeColors  widget setColors： "+Integer.toHexString(colors[0]));
        setColors(colors[0]);
    }

    private void setColors(int color){
        mWidgetName.setTextColor(color);
//        mWidgetDims.setTextColor(color);
    }

    private static String mergeDimsToName(String widgetName , String dims) {
        String widgetDims = dims;
        widgetDims = widgetDims.replace('\u00d7','*');
        widgetDims = widgetDims.replace('X','*');
        widgetDims = widgetDims.replace('x','*');
        widgetDims = widgetDims.replace(" × ","*");
        widgetDims = widgetDims.replace(" * ","*");
        char[] widgetN = widgetName.toCharArray();
        int length = widgetN.length;
        for (int i = 0; i < length; i++) {
            if (widgetN[i] == 'x' || widgetN[i] == 'X' || widgetN[i] == '*' || widgetN[i] == '\u00d7') {
                if (i >= 1 && i <= length - 2 && Character.isDigit(widgetN[i - 1]) && Character.isDigit(widgetN[i + 1])) {
                    String left,right;
                    if (i >= 2 && i <= length - 3 && (('(' == widgetN[i - 2] && ')' == widgetN[i + 2]) || ('（' == widgetN[i - 2] && '）' == widgetN[i + 2]))) {
                        left = widgetName.substring(0, i - 2);
                        if(i == length-3){
                            right = "";
                        }else {
                            right = widgetName.substring(i + 3);
                        }
                    }else {
                        left = widgetName.substring(0, i - 1);
                        if(i == length-2){
                            right = "";
                        }else {
                            right = widgetName.substring(i + 2);
                        }
                    }
//                    return left + "(" + widgetDims + ")" + right;
                    return left  + widgetDims + right;
                }
            }
        }
//        return "(" + widgetDims + ")" + widgetName;
        return widgetDims + widgetName;
//        return widgetName;
    }
}
