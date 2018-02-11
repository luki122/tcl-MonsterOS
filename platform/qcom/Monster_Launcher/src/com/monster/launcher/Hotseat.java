/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.monster.launcher;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;
import android.widget.TextView;

public class Hotseat extends FrameLayout
        implements Stats.LaunchSourceProvider{
    public static String TAG = Hotseat.class.getSimpleName();

    private CellLayout mContent;

    private Launcher mLauncher;

    private int mAllAppsButtonRank;

    private final boolean mHasVerticalHotseat;

    //add by xiangzx
    private Scroller mScroller;

    public Hotseat(Context context) {
        this(context, null);
    }

    public Hotseat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Hotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLauncher = (Launcher) context;
        mHasVerticalHotseat = mLauncher.getDeviceProfile().isVerticalBarLayout();
        mScroller = new Scroller(context,new DecelerateInterpolator(1.5f));
    }

    CellLayout getLayout() {
        return mContent;
    }

    /**
     * Returns whether there are other icons than the all apps button in the hotseat.
     */
    public boolean hasIcons() {
        return mContent.getShortcutsAndWidgets().getChildCount() > 1;
    }

    /**
     * Registers the specified listener on the cell layout of the hotseat.
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mContent.setOnLongClickListener(l);
    }
  
    /* Get the orientation invariant order of the item in the hotseat for persistence. */
    int getOrderInHotseat(int x, int y) {
        return mHasVerticalHotseat ? (mContent.getCountY() - y - 1) : x;
    }

    /* Get the orientation specific coordinates given an invariant order in the hotseat. */
    int getCellXFromOrder(int rank) {
        return mHasVerticalHotseat ? 0 : rank;
    }

    int getCellYFromOrder(int rank) {
        return mHasVerticalHotseat ? (mContent.getCountY() - (rank + 1)) : 0;
    }

    public boolean isAllAppsButtonRank(int rank) {
        //return rank == mAllAppsButtonRank;
        return LauncherAppState.getInstance().isShowAllAppsInWorkspace()?false:(rank == mAllAppsButtonRank);
    }

    public void relayoutContent(){
        //reLayout(mContent.getChildCount());
        onExitHotseat(null,CellLayout.MODE_DRAG_OVER);
        mHotseatDragState = HotseatDragState.NONE;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        DeviceProfile grid = mLauncher.getDeviceProfile();

        mAllAppsButtonRank = grid.inv.hotseatAllAppsRank;
        mContent = (CellLayout) findViewById(R.id.layout);
        if (grid.isLandscape && !grid.isLargeTablet) {
            mContent.setGridSize(1, (int) grid.inv.numHotseatIcons);
        } else {
            mContent.setGridSize((int) grid.inv.numHotseatIcons, 1);
        }
        mContent.setIsHotseat(true);

        resetLayout();
    }

    void resetLayout() {
        mContent.removeAllViewsInLayout();
        // Add the Apps button
        boolean isShowAllAppsInWorkspace = LauncherAppState.getInstance().isShowAllAppsInWorkspace();
        if(!isShowAllAppsInWorkspace) {
            Context context = getContext();

            LayoutInflater inflater = LayoutInflater.from(context);
            TextView allAppsButton = (TextView)
                    inflater.inflate(R.layout.all_apps_button, mContent, false);
            Drawable d = context.getResources().getDrawable(R.drawable.all_apps_button_icon);

            mLauncher.resizeIconDrawable(d);
            allAppsButton.setCompoundDrawables(null, d, null, null);

            allAppsButton.setContentDescription(context.getString(R.string.all_apps_button_label));
            allAppsButton.setOnKeyListener(new HotseatIconKeyEventListener());
            if (mLauncher != null) {
                mLauncher.setAllAppsButton(allAppsButton);
                allAppsButton.setOnTouchListener(mLauncher.getHapticFeedbackTouchListener());
                allAppsButton.setOnClickListener(mLauncher);
                allAppsButton.setOnLongClickListener(mLauncher);
                allAppsButton.setOnFocusChangeListener(mLauncher.mFocusHandler);
            }

            // Note: We do this to ensure that the hotseat is always laid out in the orientation of
            // the hotseat in order regardless of which orientation they were added
            int x = getCellXFromOrder(mAllAppsButtonRank);
            int y = getCellYFromOrder(mAllAppsButtonRank);
            CellLayout.LayoutParams lp = new CellLayout.LayoutParams(x, y, 1, 1);
            lp.canReorder = false;
            mContent.addViewToCellLayout(allAppsButton, -1, allAppsButton.getId(), lp, true);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // We don't want any clicks to go through to the hotseat unless the workspace is in
        // the normal state.
        if (mLauncher.getWorkspace().workspaceInModalState()) {
            return true;
        }
        return false;
    }

    @Override
    public void fillInLaunchSourceData(Bundle sourceData) {
        sourceData.putString(Stats.SOURCE_EXTRA_CONTAINER, Stats.CONTAINER_HOTSEAT);
    }

    public Scroller getScroller() {
        return mScroller;
    }

    @Override
    public void computeScroll() {
        computeScrollHelper();
    }

    protected boolean computeScrollHelper() {
        if (mScroller.computeScrollOffset()) {
            if (getScrollX() != mScroller.getCurrX()) {
                scrollTo(mScroller.getCurrX(), 0);
            }
            invalidate();
            return true;
        }
        return false;
    }

    private static class ScrollInterpolator implements Interpolator {
        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t*t*t*t*t + 1 ;
        }
    }
    //modify by xiejun ,For Hotseat devide equally.
    HotseatDragState mHotseatDragState = HotseatDragState.NONE;
    public void reLayout(int childcount)
    {
        if(mContent!=null){
            mContent.reLayout(childcount);
        }

    }

    public void onEnterHotseat(View dragItem,int dragmode){
        if ((dragItem instanceof FolderIcon))
        {
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams)dragItem.getLayoutParams();
            lp.tmpCellX = lp.cellX;
            lp.tmpCellY = lp.cellY;
        }
        if (!HotseatDragState.IN.equals(mHotseatDragState)) {
            Log.i(TAG, "onEnterHotseat");
            mHotseatDragState = HotseatDragState.IN;
            this.mContent.revertCellLayout(CellLayout.MODE_DRAG_OVER, HotseatDragState.IN, dragItem,this);
        }
    }


    public void onExitHotseat(View dragItem,int dragmode) {

        if (!HotseatDragState.OUT.equals(mHotseatDragState)) {
            Log.i(TAG, "onExitHotseat");
            mHotseatDragState = HotseatDragState.OUT;
            this.mContent.revertCellLayout(CellLayout.MODE_DRAG_OVER, HotseatDragState.OUT, dragItem, this);
        }
    }

    public int  getDeltaGap(int currentCount,int finalCount){
        int availableWidth =mContent.getRight() - mContent.getLeft() - mContent.getPaddingLeft() - mContent.getPaddingRight();
        int availableHeight = mContent.getBottom() - mContent.getTop() -mContent.getPaddingTop() - mContent.getPaddingBottom();
        int cellWidth = mContent.getCellWidth();
        int cellHeight = mContent.getCellHeight();
        //TODO：xiejun ,the size of cell will  be saved here ;
        return (calcGap(currentCount,availableWidth,cellWidth,cellHeight)-calcGap(finalCount,availableWidth,cellWidth,cellHeight));
    }

    public int calcGap(int visibleCount,int availableWidth,int cellWidth,int cellHeight){
        int numcolomes = (int)LauncherAppState.getInstance().getInvariantDeviceProfile().numHotseatIcons;
        if (visibleCount > numcolomes) {
            visibleCount = numcolomes;
        }
        int wGap =(availableWidth - cellWidth * visibleCount) / (visibleCount+1);
        return wGap;

    }

    public void setHotseatDragState(HotseatDragState state) {
        mHotseatDragState = state;
    }

    public HotseatDragState getHotseatDragState() {
        return mHotseatDragState;
    }

    public float getTranslateYByScale(float scale){
        return (this.getHeight()-this.getHeight()*scale);
    }


    public enum HotseatDragState {
        NONE("Drag_NONE", 0),
        IN("Drag_In", 1),
        OUT("Drag_OUT", 2);

        private String name;
        private int state;

        HotseatDragState(String name, int state) {
            this.name = name;
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public String toString() {
            return "" + name + ":" + state;
        }
    }
}
