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

import android.animation.LayoutTransition;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import com.monster.launcher.widget.PendingAddWidgetInfo;

public class PageIndicatorCube extends PageIndicator implements DropTarget{
    @SuppressWarnings("unused")
    private static final String TAG = "PageIndicatorCube";

    public static final int LEFT_INDICATOR_INDEX = -1;
    public static final int RIGHT_INDICATOR_INDEX = Integer.MAX_VALUE;
    public static final int INVALID_INDICATOR_INDEX = -100;
    private static final int SNAP_OUT_DELAY = 150;
    private static final int SNAP_OUT_PAGE_DELAY = 1300;
    private static final int MSG_RESET_SNAP_OUT_LEFT_DELAY = 1;
    private static final int MSG_RESET_SNAP_OUT_RIGHT_DELAY = 2;
    private static final int DRAG_STATE_NORMAL = 1;
    private static final int DRAG_STATE_LEFT = 2;
    private static final int DRAG_STATE_RIGHT = 3;
    private static final int DRAG_STATE_OUT = 4;
    private int mDragState = DRAG_STATE_OUT;
    private int dragOutCounter = 0;//用于计数

    public static boolean dropToWorkspace = false;

    private Launcher mLauncher;
    private Workspace mWorkspace;
    private int LastWorkspaceIndex;
    private int LastDragOverIndex;
    private int curDragOverIndex;
    private int onDragOverIndex;
    private int size = 0;
    float[] mDragViewVisualCenter = new float[2];
    int[] mTargetCell = new int[2];
    private CellLayout mDropToLayout = null;
    private CellLayout mDragStartLayout = null;
    PageIndicatorMarker leftScrollIndicatorMarker;
    PageIndicatorMarker rightScrollIndicatorMarker;

    private int[] markerIds;
    boolean showLeftScrollIndicator;
    boolean showRightScrollIndicator;
    private int preSize = 0;
    public int getSize() {
        return size;
    }

    private boolean snapOutDelaying = false;//拖到左右箭头上的翻页
    private DragObject mDelayDragObject = null;

    int mCellWidth;
    int mCellHeight;

    Bitmap mDragOutline = null;
    boolean reCalculateWindowRange = false;

    private boolean enterDrawFrame = true;//进入到PageIndicatorCube无论onDragOverIndex == LastWorkspaceIndex

    private int pageCount;
    private int curPage = -1;
    boolean showFull = false;
    public void setmDragOutline(Bitmap mDragOutline) {
        this.mDragOutline = mDragOutline;
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
//                case MSG_RESET_DRAGOVER_DELAY:
//                    onDragOverDelay(mDelayDragObject);
//                    break;
                case MSG_RESET_SNAP_OUT_LEFT_DELAY:
                    mWorkspace.scrollLeft();
                    if(mLauncher!= null && mDragState != DRAG_STATE_OUT){
                        mLauncher.showPageIndicatorDiagital(mWorkspace.getNextPage());
                    }
//                    onDragOverDelay(mDelayDragObject);
                    if(dragOutCounter <= mMaxWindowSize && !isSnapToEnds()){
                        dragOutCounter ++;
                        mHandler.sendEmptyMessageDelayed(MSG_RESET_SNAP_OUT_LEFT_DELAY,SNAP_OUT_DELAY);
                    }else{
                        snapOutDelaying = false;
                    }
                    break;
                case MSG_RESET_SNAP_OUT_RIGHT_DELAY:
                    mWorkspace.scrollRight();
                    if(mLauncher!= null && mDragState != DRAG_STATE_OUT){
                        mLauncher.showPageIndicatorDiagital(mWorkspace.getNextPage());
                    }
//                    onDragOverDelay(mDelayDragObject);
                    if(dragOutCounter <= mMaxWindowSize && !isSnapToEnds()){
                        dragOutCounter ++;
                        mHandler.sendEmptyMessageDelayed(MSG_RESET_SNAP_OUT_RIGHT_DELAY,SNAP_OUT_DELAY);
                    }else{
                        snapOutDelaying = false;
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void cleanMessage(){
        mHandler.removeMessages(MSG_RESET_SNAP_OUT_LEFT_DELAY);
    }
    public void setmDragInfo(CellLayout.CellInfo mDragInfo) {
        reCalculateWindowRange = true;
        this.mDragInfo = mDragInfo;
    }

    private CellLayout.CellInfo mDragInfo;

    public void setmLauncher(Launcher mLauncher) {
        this.mLauncher = mLauncher;
    }

    public PageIndicatorCube(Context context) {
        this(context, null);
    }

    public PageIndicatorCube(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorCube(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.PageIndicator, defStyle, 0);
        mMaxWindowSize = a.getInteger(R.styleable.PageIndicator_windowSize, 6);
        mWindowRange[0] = -2;
        mWindowRange[1] = -2;
        mLayoutInflater = LayoutInflater.from(context);
        a.recycle();

        // Set the layout transition properties
        LayoutTransition transition = getLayoutTransition();
        transition.setDuration(175);
        if(context instanceof Launcher){
            mLauncher = (Launcher) context;
            mWorkspace = mLauncher.mWorkspace;
        }
        initLeftRightIndicator(false);
    }

    protected boolean initLeftRightIndicator(boolean force){
        if(!force && leftScrollIndicatorMarker != null && rightScrollIndicatorMarker != null){
            return true;
        }
        if(mWorkspace == null || mLayoutInflater == null) return false;

        PageMarkerResources markerLeft = mWorkspace.getPageIndicatorMarkerForCube(LEFT_INDICATOR_INDEX);
        PageMarkerResources markerRight = mWorkspace.getPageIndicatorMarkerForCube(RIGHT_INDICATOR_INDEX);
        leftScrollIndicatorMarker =
                (PageIndicatorMarker) mLayoutInflater.inflate(R.layout.page_indicator_cube_marker_left_right,
                        this, false);
        rightScrollIndicatorMarker =
                (PageIndicatorMarker) mLayoutInflater.inflate(R.layout.page_indicator_cube_marker_left_right,
                        this, false);
        leftScrollIndicatorMarker.isCube = true;
        leftScrollIndicatorMarker.setMarkerDrawables(markerLeft.activeId, markerLeft.inactiveId);
        leftScrollIndicatorMarker.markerId = LEFT_INDICATOR_INDEX;
        rightScrollIndicatorMarker.isCube = true;
        rightScrollIndicatorMarker.setMarkerDrawables(markerRight.activeId, markerRight.inactiveId);
        rightScrollIndicatorMarker.markerId = RIGHT_INDICATOR_INDEX;
        if(leftScrollIndicatorMarker != null && rightScrollIndicatorMarker != null){
            return true;
        }
        return false;
    }

    protected void enableLayoutTransitions() {
        LayoutTransition transition = getLayoutTransition();
        transition.enableTransitionType(LayoutTransition.APPEARING);
        transition.enableTransitionType(LayoutTransition.DISAPPEARING);
        transition.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
        transition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
    }

    protected void disableLayoutTransitions() {
        LayoutTransition transition = getLayoutTransition();
        transition.disableTransitionType(LayoutTransition.APPEARING);
        transition.disableTransitionType(LayoutTransition.DISAPPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
        transition.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
    }

    protected void offsetWindowCenterTo(int activeIndex, boolean allowAnimations) {
//        offsetWindowPageTo(activeIndex,allowAnimations);
        if (activeIndex < 0) {
            new Throwable().printStackTrace();
        }
        int windowStart,windowEnd;
        if(mWindowRange[0] >= mWindowRange[1] || mMarkers.size()<= mMaxWindowSize
                || reCalculateWindowRange){//the first access
            reCalculateWindowRange = false;
            int windowSize = Math.min(mMarkers.size(), mMaxWindowSize);
            int hWindowSize = (int) windowSize / 2;
            windowStart = Math.max(0, activeIndex - hWindowSize);
            windowEnd = Math.min(mMarkers.size(), windowStart + mMaxWindowSize) -1;
            windowStart = windowEnd - Math.min(mMarkers.size(), windowSize) + 1;
        }else{
            if(preSize != mMarkers.size()){
                if(mWindowRange[0] > activeIndex){
                    windowStart = activeIndex;
                }else{
                    windowStart = mWindowRange[0];
                }
                if((mMarkers.size()-windowStart) < mMaxWindowSize){
                    windowEnd = mMarkers.size()-1;
                }else{
                    windowEnd = windowStart + mMaxWindowSize -1;
                }
            }else {
                if (activeIndex < mWindowRange[0]) {
                    windowStart = activeIndex;
                    windowEnd = mWindowRange[1] - (mWindowRange[0] - activeIndex);
                } else if (activeIndex > mWindowRange[1]) {
                    windowEnd = activeIndex;
                    windowStart = mWindowRange[0] + (activeIndex - mWindowRange[1]);
                } else {
                    windowStart = mWindowRange[0];
                    windowEnd = mWindowRange[1];
                }
            }
        }

        if (preSize < mMarkers.size() && windowEnd == mMarkers.size() -2) {
            windowStart++;
            windowEnd++;
        } else if (preSize > mMarkers.size() && (windowEnd-windowStart+1)<mMaxWindowSize && windowStart>0) {
            windowStart --;
        }

        preSize = mMarkers.size();

        boolean windowMoved = (mWindowRange[0] != windowStart) ||
                (mWindowRange[1] != windowEnd);
        if (!allowAnimations) {
            disableLayoutTransitions();
        }
        if(initLeftRightIndicator(false)){
            showLeftScrollIndicator = (windowStart > 0);
            showRightScrollIndicator = (windowEnd < (mMarkers.size()-1));
        }
        showFull = (showLeftScrollIndicator || showRightScrollIndicator);
        int temp = showFull?1:0;
        markerIds = new int[mMaxWindowSize+2];
        if(showFull){
            markerIds[0] = LEFT_INDICATOR_INDEX;
            markerIds[mMaxWindowSize+1] = RIGHT_INDICATOR_INDEX;
        }
//        if(showRightScrollIndicator){
//            markerIds[windowEnd-windowStart+1+temp] = RIGHT_INDICATOR_INDEX;
//        }
        for(int i = windowStart ; i <= windowEnd ; i ++ ){
            markerIds[i-windowStart + temp] = i;
        }
        // Remove all the previous children that are no longer in the window
        removeView(rightScrollIndicatorMarker);
        for (int i = getChildCount() - 1; i >= 0; --i) {
            PageIndicatorMarker marker = (PageIndicatorMarker) getChildAt(i);
            int markerIndex = mMarkers.indexOf(marker);
            if (markerIndex < windowStart || markerIndex >= windowEnd) {
                removeView(marker);
            }
        }
        removeView(leftScrollIndicatorMarker);
        // Add all the new children that belong in the window

        if(showFull){
            addView(leftScrollIndicatorMarker, 0);
        }
        leftScrollIndicatorMarker.setVisibility(showLeftScrollIndicator?View.VISIBLE:View.INVISIBLE);
        for (int i = 0; i < mMarkers.size(); ++i) {
            PageIndicatorMarker marker = (PageIndicatorMarker) mMarkers.get(i);
            if (windowStart <= i && i <= windowEnd) {
                if (indexOfChild(marker) < 0) {
                    addView(marker, i - windowStart + temp);
                }
                if (i == activeIndex) {
                    marker.activate(true);
                } else {
                    marker.inactivate(false||mDragState == DRAG_STATE_LEFT||mDragState == DRAG_STATE_RIGHT);
                }
            } else {
                marker.inactivate(true);
            }
        }

        if(showFull){
            addView(rightScrollIndicatorMarker, windowStart - windowEnd + 2);
        }
        rightScrollIndicatorMarker.setVisibility(showRightScrollIndicator?View.VISIBLE:View.INVISIBLE);
        if (!allowAnimations) {
//            enableLayoutTransitions();
        }

        mWindowRange[0] = windowStart;
        mWindowRange[1] = windowEnd;
    }

    public void offsetWindowPageTo(int activeIndex, boolean allowAnimations){
        if (activeIndex < 0) {
            new Throwable().printStackTrace();
        }
        initLeftRightIndicator(false);
        int toPage = activeIndex/mMaxWindowSize;
        showFull = (pageCount > 1);
        showLeftScrollIndicator = (showFull && toPage != 0);
//        android.util.Log.e("---lijun---",android.util.Log.getStackTraceString(new Throwable()));
        showRightScrollIndicator = (showFull && toPage != pageCount-1);
        int start = toPage*mMaxWindowSize;
        int end = (toPage == pageCount-1)?(mMarkers.size()-1):((toPage+1)*mMaxWindowSize-1);
        if(showFull && (end+1)%mMaxWindowSize != 0){
            setHorizontalGravity(Gravity.LEFT);
        }else{
            setHorizontalGravity(Gravity.CENTER);
        }

        int temp = showFull?1:0;
        markerIds = new int[temp*2+end-start+1];
        if(showFull){
            markerIds[0] = LEFT_INDICATOR_INDEX;
            markerIds[markerIds.length-1] = RIGHT_INDICATOR_INDEX;
        }
        for(int i = start ; i <= end ; i ++ ){
            markerIds[i-start + temp] = i;
        }
        if(!checkChange()) {
            if (!isActivieIndex(activeIndex)) {
                for (int i = 0; i < getChildCount(); i++) {
                    PageIndicatorMarker marker = (PageIndicatorMarker) getChildAt(i);
                    if (markerIds[i] == activeIndex) {
                        marker.activate(true);
                    } else {
                        marker.inactivate(false);
                    }
                }
            }
        }else{
            removeAllViews();
            for(int i = 0 ; i < markerIds.length; i ++){
                PageIndicatorMarker marker;
                if(markerIds[i] == LEFT_INDICATOR_INDEX){
                    marker = leftScrollIndicatorMarker;
                }else if(markerIds[i] == RIGHT_INDICATOR_INDEX){
                    marker = rightScrollIndicatorMarker;
                }else{
                    marker = mMarkers.get(markerIds[i]);
                }
                if(markerIds[i] == activeIndex){
                    marker.activate(true);
                }else{
                    marker.inactivate(false);
                }
                addView(marker,i);
            }
//            for(int i = 0 ; i < getChildCount() ; i ++){
//                View v = getChildAt(i);
//                v.animate().alpha(1).setDuration(200).start();
//            }
            enableLayoutTransitions();
        }
        leftScrollIndicatorMarker.setVisibility(showLeftScrollIndicator?View.VISIBLE:View.INVISIBLE);
        rightScrollIndicatorMarker.setVisibility(showRightScrollIndicator?View.VISIBLE:View.INVISIBLE);

        curPage = toPage;
        mWindowRange[0] = start;
        mWindowRange[1] = end;
    }

    protected void addMarker(int index, PageMarkerResources marker, boolean allowAnimations) {
        index = Math.max(0, Math.min(index, mMarkers.size()));
        PageIndicatorMarker m =
            (PageIndicatorMarker) mLayoutInflater.inflate(R.layout.page_indicator_cube_marker,
                    this, false);
        m.isCube = true;
//        m.setMarkerDrawables(marker.activeId, marker.inactiveId);
        m.setMarkerDrawables(marker.framBitmap);
        m.markerId = index;

        mMarkers.add(index, m);
        size ++;
        pageCount = (size/mMaxWindowSize < size/(float)mMaxWindowSize)?size/mMaxWindowSize+1:size/mMaxWindowSize;
        offsetWindowCenterTo(mActiveMarkerIndex, allowAnimations);
    }

    @Override
    protected void updateMarker(int index, PageMarkerResources marker) {
        if(index >= mMarkers.size())return;
        PageIndicatorMarker m = mMarkers.get(index);
//        m.setMarkerDrawables(marker.activeId, marker.inactiveId);
        m.setMarkerDrawables(marker.framBitmap);
    }

    @Override
    protected void removeMarker(int index, boolean allowAnimations) {
        if (mMarkers.size() > 0) {
            index = Math.max(0, Math.min(mMarkers.size() - 1, index));
            mMarkers.remove(index);
            offsetWindowCenterTo(mActiveMarkerIndex, allowAnimations);
            size --;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int cw,ch;
        if(getChildCount() > 0){
            cw = mLauncher.getDeviceProfile().pageIndicatorCubeCellWidthPx;
            ch = mLauncher.getDeviceProfile().pageIndicatorCubeCellHeightPx;
        }else{
            cw = ch = 0;
        }
        if (cw != mCellWidth || ch != mCellHeight) {
            mCellWidth = cw;
            mCellHeight = ch;
        }
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    }

    @Override
    public boolean isDropEnabled() {
        return true;
    }

    @Override
    public void onDrop(DragObject dragObject) {
        Log.d(TAG,"onDrop");
        mDragViewVisualCenter = dragObject.getVisualCenter(mDragViewVisualCenter);
        if(mLauncher!= null)mLauncher.hidePageIndicatorDiagital();
        CellLayout dropTargetLayout = mDropToLayout;
        if (dropTargetLayout != null) {
            mWorkspace.mapPointFromSelfToChild(dropTargetLayout, mDragViewVisualCenter, null);
        }
        boolean foundCell = false;
        if (dragObject.dragSource != mWorkspace) {
            ItemInfo item = (ItemInfo) dragObject.dragInfo;
            int minSpanX = item.spanX;
            int minSpanY = item.spanY;
            dropTargetLayout.findVacantCell(minSpanX,minSpanY,mTargetCell);
            mWorkspace.onDropExternalForPageIndicatorCube(mTargetCell, dragObject.dragInfo, dropTargetLayout, false, dragObject);
        } else if(mDragInfo!=null){
            final View cell = mDragInfo.cell;

            Runnable resizeRunnable = null;
            if (dropTargetLayout != null && !dragObject.cancelled) {
                boolean hasMovedLayouts = (mWorkspace.getParentCellLayoutForView(cell) != dropTargetLayout);
                long container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
                long screenId = (mTargetCell[0] < 0) ?
                        mDragInfo.screenId : mWorkspace.getIdForScreen(dropTargetLayout);
                int spanX = mDragInfo != null ? mDragInfo.spanX : 1;
                int spanY = mDragInfo != null ? mDragInfo.spanY : 1;

                ItemInfo item = (ItemInfo) dragObject.dragInfo;
                int minSpanX = item.spanX;
                int minSpanY = item.spanY;
                if (item.minSpanX > 0 && item.minSpanY > 0) {
                    minSpanX = item.minSpanX;
                    minSpanY = item.minSpanY;
                }

                int[] resultSpan = new int[2];
                if(mWorkspace.getPageIndexForScreenId(screenId) == mWorkspace.getDragSourceIndex()){
                    foundCell = true;
                    mTargetCell[0] = mWorkspace.getSourceCell()[0];
                    mTargetCell[1] = mWorkspace.getSourceCell()[1];

                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                    mTargetCell[0] = lp.cellX;
                    mTargetCell[1] = lp.cellY;
                    CellLayout layout = (CellLayout) cell.getParent().getParent();
                    layout.markCellsAsOccupiedForView(cell);
                }else{
                    foundCell = dropTargetLayout.getEmptyLayPlace(minSpanX, minSpanY, spanX, spanY,
                            mTargetCell, resultSpan);
                }
                if (foundCell && (cell instanceof AppWidgetHostView) &&
                        (resultSpan[0] != item.spanX || resultSpan[1] != item.spanY)) {
                    item.spanX = resultSpan[0];
                    item.spanY = resultSpan[1];
                    AppWidgetHostView awhv = (AppWidgetHostView) cell;
                    AppWidgetResizeFrame.updateWidgetSizeRanges(awhv, mLauncher, resultSpan[0],
                            resultSpan[1]);
                }

                if(foundCell){
                    View v = mDragInfo==null?null:mDragInfo.cell;
                    if(v!=null&&v.getParent()!=null&&v.getParent().getParent()!=null&&mLauncher.isHotseatLayout((View)(v.getParent().getParent()))){
                        mLauncher.getHotseat().onExitHotseat(v,CellLayout.MODE_DRAG_OVER);
                    }
                }

                if (foundCell) {
                    dropToWorkspace = true;
                    if(mWorkspace.getScreenIdForPageIndex(mWorkspace.getCurrentPage()) != mWorkspace.getIdForScreen(dropTargetLayout)){
                        mWorkspace.snapToScreenId(mWorkspace.getIdForScreen(dropTargetLayout));
                        Log.d(TAG,"onDrop snapTo id " + mWorkspace.getIdForScreen(dropTargetLayout));
                    }
                    final ItemInfo info = (ItemInfo) cell.getTag();
                    Log.v("moveicon", "PageIndicatorCube-----title="+info.title+",info="+info+", mTargetCell : cellX="+mTargetCell[0]+", celly="+mTargetCell[1]+", hasMovedLayouts="+hasMovedLayouts);
                    if (hasMovedLayouts) {
                        // Reparent the view
                        CellLayout parentCell = mWorkspace.getParentCellLayoutForView(cell);
                        if (parentCell != null) {
                            parentCell.removeView(cell);
                        } else if (LauncherAppState.isDogfoodBuild()) {
                            throw new NullPointerException("mDragInfo.cell has null parent");
                        }
                        mWorkspace.addInScreen(cell, container, screenId, mTargetCell[0], mTargetCell[1],
                                info.spanX, info.spanY);
                    }

                    // update the item's position after drop
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                    lp.cellX = lp.tmpCellX = mTargetCell[0];
                    lp.cellY = lp.tmpCellY = mTargetCell[1];
                    lp.cellHSpan = item.spanX;
                    lp.cellVSpan = item.spanY;
                    lp.isLockedToGrid = true;

                    if (container != LauncherSettings.Favorites.CONTAINER_HOTSEAT &&
                            cell instanceof LauncherAppWidgetHostView) {
                        final CellLayout cellLayout = dropTargetLayout;
                        // We post this call so that the widget has a chance to be placed
                        // in its final location

                        final LauncherAppWidgetHostView hostView = (LauncherAppWidgetHostView) cell;
                        AppWidgetProviderInfo pInfo = hostView.getAppWidgetInfo();
                        if (pInfo != null && pInfo.resizeMode != AppWidgetProviderInfo.RESIZE_NONE
                                && !dragObject.accessibleDrag) {
                            final Runnable addResizeFrame = new Runnable() {
                                public void run() {
                                    DragLayer dragLayer = mLauncher.getDragLayer();
                                    dragLayer.addResizeFrame(info, hostView, cellLayout);
                                }
                            };
                            resizeRunnable = (new Runnable() {
                                public void run() {
                                    if (!mWorkspace.isPageMoving()) {
                                        addResizeFrame.run();
                                    } else {
                                        mWorkspace.mDelayedResizeRunnable = addResizeFrame;
                                    }
                                }
                            });
                        }
                    }

                    LauncherModel.modifyItemInDatabase(mLauncher, info, container, screenId, lp.cellX,
                            lp.cellY, item.spanX, item.spanY);
                } else {
                    // If we can't find a drop location, we return the item to its original position
                    CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
                    mTargetCell[0] = lp.cellX;
                    mTargetCell[1] = lp.cellY;
                    CellLayout layout = (CellLayout) cell.getParent().getParent();
                    layout.markCellsAsOccupiedForView(cell);
                }
            }
            final CellLayout parent = (CellLayout) cell.getParent().getParent();
            final Runnable finalResizeRunnable = resizeRunnable;
            // Prepare it to be animated into its new position
            // This must be called after the view has been re-parented
            final Runnable onCompleteRunnable = new Runnable() {
                @Override
                public void run() {
                    mWorkspace.mAnimatingViewIntoPlace = false;
                    mWorkspace.updateChildrenLayersEnabled(false);
                    if (finalResizeRunnable != null) {
                        finalResizeRunnable.run();
                    }
                }
            };

            mWorkspace.mAnimatingViewIntoPlace = true;
            if (dragObject.dragView.hasDrawn()) {
                final ItemInfo info = (ItemInfo) cell.getTag();
                boolean isWidget = info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET
                        || info.itemType == LauncherSettings.Favorites.ITEM_TYPE_CUSTOM_APPWIDGET;
                if(parent instanceof CellLayout){
                    if (isWidget) {
                        int animationType = Workspace.ANIMATE_INTO_POSITION_AND_DISAPPEAR;
                        mWorkspace.animateWidgetDrop(info, (CellLayout)parent, dragObject.dragView,
                                onCompleteRunnable, animationType, cell, false);
                    } else {
                        int duration = Workspace.ADJACENT_SCREEN_DROP_DURATION;
                        mLauncher.getDragLayer().animateViewIntoPosition(dragObject.dragView, cell, duration,
                                onCompleteRunnable, this);
                    }
                } else {
                    dragObject.deferDragViewCleanupPostAnimation = false;
                    cell.setVisibility(VISIBLE);
                }
            } else {
                dragObject.deferDragViewCleanupPostAnimation = false;
                cell.setVisibility(VISIBLE);
            }
            parent.onDropChild(cell);

        }
    }

    @Override
    public void onDragEnter(DragObject dragObject) {
        Log.d(TAG,"onDragEnter");
        LastDragOverIndex = -2;
        enterDrawFrame = true;
        mDragState = DRAG_STATE_NORMAL;
        LastWorkspaceIndex = mWorkspace.getCurrentPage();
        mDropToLayout = null;
        mWorkspace.refreshLastTargetViewCache();
        mDragStartLayout = (CellLayout) mWorkspace.getPageAt(mWorkspace.getCurrentPage());
        View v = mDragInfo!=null?mDragInfo.cell:null;
        if(dragObject!=null&&dragObject.dragInfo!=null&&v!=null&&v.getParent()!=null&&v.getParent().getParent()!=null&&mLauncher.isHotseatLayout((View)(v.getParent().getParent()))){
            mLauncher.getHotseat().onEnterHotseat(v,CellLayout.MODE_DRAG_OVER);
            ItemInfo item = (ItemInfo) dragObject.dragInfo;
            int minSpanX = item.spanX;
            int minSpanY = item.spanY;
            if (item.minSpanX > 0 && item.minSpanY > 0) {
                minSpanX = item.minSpanX;
                minSpanY = item.minSpanY;
            }
            int[] resultSpan = new int[2];
            CellLayout dropTargetLayout = mLauncher.getHotseat()
                    .getLayout();
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) mDragInfo.cell.getLayoutParams();
            mTargetCell[0] = lp.cellX;
            mTargetCell[1] = lp.cellY;
            mTargetCell = dropTargetLayout.performReorder(CellLayout.EXTRA_EMPTY_POSTION, CellLayout.EXTRA_EMPTY_POSTION, minSpanX,
                    minSpanY, 1, 1, mDragInfo.cell, mTargetCell,
                    resultSpan, CellLayout.MODE_SHOW_REORDER_HINT);
        }else{
            mLauncher.getHotseat().onExitHotseat(v,CellLayout.MODE_DRAG_OVER);
        }
    }

    @Override
    public void onDragOver(DragObject dragObject) {
        Log.i(TAG,"onDragOver "+dragObject.x);
        mDragViewVisualCenter = dragObject.getVisualCenter(mDragViewVisualCenter);
        int fi = findCurrentDragOverIndex(dragObject);
        if(fi == INVALID_INDICATOR_INDEX)return;
        curDragOverIndex = onDragOverIndex = fi;
        if(!mLauncher.getDragController().checkVelocityTrackerToPage()){
            return;
        }

        if(onDragOverIndex == LEFT_INDICATOR_INDEX){
            mHandler.removeMessages(MSG_RESET_SNAP_OUT_RIGHT_DELAY);
            mWorkspace.refreshLastTargetViewCache();
            if(!showLeftScrollIndicator){
                mHandler.removeMessages(MSG_RESET_SNAP_OUT_LEFT_DELAY);
                if(mLauncher!= null)mLauncher.hidePageIndicatorDiagital();
                mDropToLayout = (CellLayout) mWorkspace.getPageAt(LastWorkspaceIndex);
                mWorkspace.setCurrentDropLayout(null);
                mWorkspace.setCurrentDragOverlappingLayout(null);
                curDragOverIndex = 0;
                mDragState = DRAG_STATE_NORMAL;
            }else{
                if(mDragState == DRAG_STATE_LEFT){
                    if(!snapOutDelaying){
                        snapOutDelaying = true;
                        mHandler.removeMessages(MSG_RESET_SNAP_OUT_LEFT_DELAY);
                        dragOutCounter = 1;
                        mHandler.sendEmptyMessageDelayed(MSG_RESET_SNAP_OUT_LEFT_DELAY, SNAP_OUT_PAGE_DELAY);
                    }else{
                        return;
                    }
                }else{
                    mDragState = DRAG_STATE_LEFT;
                    dragOutCounter = 1;
                    snapOutDelaying = true;
                    mHandler.removeMessages(MSG_RESET_SNAP_OUT_LEFT_DELAY);
                    mHandler.sendEmptyMessageDelayed(MSG_RESET_SNAP_OUT_LEFT_DELAY, SNAP_OUT_DELAY);
                }
                if(mLauncher!= null)mLauncher.showPageIndicatorDiagital(mWorkspace.getNextPage());
            }
        }else if(onDragOverIndex == RIGHT_INDICATOR_INDEX){
            mHandler.removeMessages(MSG_RESET_SNAP_OUT_LEFT_DELAY);
            mWorkspace.refreshLastTargetViewCache();
            if(!showRightScrollIndicator){
                mHandler.removeMessages(MSG_RESET_SNAP_OUT_RIGHT_DELAY);
                if(mLauncher!= null)mLauncher.hidePageIndicatorDiagital();
                mDropToLayout = (CellLayout) mWorkspace.getPageAt(LastWorkspaceIndex);
                mWorkspace.setCurrentDropLayout(null);
                mWorkspace.setCurrentDragOverlappingLayout(null);
                curDragOverIndex = mWorkspace.getPageCount()-1;
                mDragState = DRAG_STATE_NORMAL;
            }else{
                if(mDragState == DRAG_STATE_RIGHT){
                    if(!snapOutDelaying){
                        snapOutDelaying = true;
                        mHandler.removeMessages(MSG_RESET_SNAP_OUT_RIGHT_DELAY);
                        dragOutCounter = 1;
                        mHandler.sendEmptyMessageDelayed(MSG_RESET_SNAP_OUT_RIGHT_DELAY, SNAP_OUT_PAGE_DELAY);
                    }else{
                        return;
                    }
                }else{
                    mDragState = DRAG_STATE_RIGHT;
                    dragOutCounter = 1;
                    snapOutDelaying = true;
                    mHandler.removeMessages(MSG_RESET_SNAP_OUT_RIGHT_DELAY);
                    mHandler.sendEmptyMessageDelayed(MSG_RESET_SNAP_OUT_RIGHT_DELAY, SNAP_OUT_DELAY);
                }
                if(mLauncher!= null)mLauncher.showPageIndicatorDiagital(mWorkspace.getNextPage());
            }
        }else if (onDragOverIndex != LastDragOverIndex) {
            mHandler.removeMessages(MSG_RESET_SNAP_OUT_LEFT_DELAY);
            mHandler.removeMessages(MSG_RESET_SNAP_OUT_RIGHT_DELAY);
            onDragOverDelay(dragObject);
            mDragState = DRAG_STATE_NORMAL;
            snapOutDelaying = false;
        }
        mDelayDragObject = dragObject;
        LastDragOverIndex = onDragOverIndex;
    }

    private void onDragOverDelay(DragObject dragObject) {
        Log.i(TAG,"onDragOverDelay "+dragObject.x);
        if(dragObject == null){
            return ;
        }
        if(snapOutDelaying ){
            snapOutDelaying = false;
            if ((onDragOverIndex == LEFT_INDICATOR_INDEX || onDragOverIndex == RIGHT_INDICATOR_INDEX) && onDragOverIndex == LastWorkspaceIndex) {
                LastWorkspaceIndex = 0;
            }
        }
//        Log.i("--lijun--","onDragOverIndex  "+onDragOverIndex);
        Log.i(TAG,"onDragOverDelay onDragOverIndex:"+onDragOverIndex+"  LastDragOverIndex:"+LastDragOverIndex + " enterDrawFrame:"+enterDrawFrame);
        if (onDragOverIndex != LastWorkspaceIndex || enterDrawFrame) {
            if (onDragOverIndex == LEFT_INDICATOR_INDEX) {
                if (!showLeftScrollIndicator) return;
                onDragOverIndex = markerIds[1] - 1;
            } else if (onDragOverIndex == RIGHT_INDICATOR_INDEX) {
                if (!showRightScrollIndicator) return;
                onDragOverIndex = markerIds[markerIds.length-2] + 1;
            }
            mWorkspace.snapToPage(onDragOverIndex);
            curDragOverIndex = onDragOverIndex;
            boolean foundCell = false;
            int[] targetCell = new int[4];
            int[] resultSpan = new int[2];
            CellLayout dropTargetLayout = (CellLayout) mWorkspace.getPageAt(onDragOverIndex);
            if (dropTargetLayout != null) {
                mWorkspace.setCurrentDropLayout(dropTargetLayout);
                mWorkspace.setCurrentDragOverlappingLayout(dropTargetLayout);

                ItemInfo item = (ItemInfo) dragObject.dragInfo;
                int spanX = item.spanX;
                int spanY = item.spanY;
                if (onDragOverIndex == mWorkspace.getDragSourceIndex()) {
                    foundCell = true;
                    mTargetCell[0] = mWorkspace.getSourceCell()[0];
                    mTargetCell[1] = mWorkspace.getSourceCell()[1];
                } else {
                    if (dragObject.dragSource instanceof Workspace || dragObject.dragSource instanceof Folder) {
                        resultSpan[0] = item.spanX;
                        resultSpan[1] = item.spanY;
                        foundCell = dropTargetLayout.findCellForSpan(mTargetCell, spanX, spanY);
                    } else {
                        int minSpanX = item.spanX;
                        int minSpanY = item.spanY;
                        if (item.minSpanX > 0 && item.minSpanY > 0) {
                            minSpanX = item.minSpanX;
                            minSpanY = item.minSpanY;
                        }
                        foundCell = dropTargetLayout.getEmptyLayPlace(minSpanX, minSpanY, spanX, spanY,
                                mTargetCell, resultSpan);
                    }
                }

                final View child = (mDragInfo == null) ? null : mDragInfo.cell;
                if (foundCell) {
                    targetCell[0] = mTargetCell[0];
                    targetCell[1] = mTargetCell[1];
                    targetCell[2] = resultSpan[0];
                    targetCell[3] = resultSpan[1];

                    dropTargetLayout.visualizeDropLocation(child, mDragOutline,
                            (int) mDragViewVisualCenter[0], (int) mDragViewVisualCenter[1],
                            mTargetCell[0], mTargetCell[1], item.spanX, item.spanY, false,
                            dragObject.dragView.getDragVisualizeOffset(), dragObject.dragView.getDragRegion());
                } else {
                    targetCell = null;
                }
            }
            if (dragObject.dragSource != mWorkspace) {
                if(!enterDrawFrame){
                    mWorkspace.refreshviewCache(LastWorkspaceIndex, null, null, false);
                }
                mWorkspace.refreshviewCache(onDragOverIndex, targetCell, null, false);
            } else {
                if (onDragOverIndex == mWorkspace.getDragSourceIndex()) {
                    mWorkspace.refreshviewCache(onDragOverIndex, mWorkspace.getSourceCell(), null, false);
                } else {
                    mWorkspace.refreshviewCache(onDragOverIndex, targetCell, null, false);
                }
                if(!enterDrawFrame && LastWorkspaceIndex != onDragOverIndex) {
                    if (LastWorkspaceIndex == mWorkspace.getDragSourceIndex()) {
                        mWorkspace.refreshviewCache(LastWorkspaceIndex, null, mWorkspace.getSourceCell(), false);
                    } else {
                        mWorkspace.refreshviewCache(LastWorkspaceIndex, null, null, false);
                    }
                }
            }
            LastWorkspaceIndex = onDragOverIndex;
            if (mLauncher != null) mLauncher.snapToPageIndicatorDiagital(onDragOverIndex);
            enterDrawFrame = false;
        }else{
//            mWorkspace.refreshviewCache(LastWorkspaceIndex, null, null, false);
        }
        if (mLauncher != null) mLauncher.showPageIndicatorDiagital(onDragOverIndex);
    }

    @Override
    public void onDragExit(DragObject dragObject) {
//        Log.i(TAG," onDragExit ");
        mDragState = DRAG_STATE_OUT;
        if(mLauncher!= null)mLauncher.hidePageIndicatorDiagital();
        mWorkspace.refreshviewCache(curDragOverIndex,null,null,false);
        if(curDragOverIndex != mWorkspace.targetCubeIndex){
            mWorkspace.refreshLastTargetViewCache();
        }

        mWorkspace.onResetScrollArea();
        mWorkspace.setCurrentDropLayout(null);
        mWorkspace.setCurrentDragOverlappingLayout(null);
    }

    @Override
    public void onFlingToDelete(DragObject dragObject, PointF vec) {
//        Log.d(TAG,"onFlingToDelete");
    }

    @Override
    public boolean acceptDrop(DragObject dragObject) {
        if (curDragOverIndex == LEFT_INDICATOR_INDEX) {
            curDragOverIndex = markerIds[1] - 1;
        } else if (curDragOverIndex == RIGHT_INDICATOR_INDEX) {
            curDragOverIndex = markerIds[markerIds.length - 2] + 1;
        }
        mDropToLayout = (CellLayout) mWorkspace.getPageAt(curDragOverIndex);
        mDragInfo = mWorkspace.getDragInfo();
        if (mDropToLayout == null) {
            if (mDragInfo != null) {
                final View cell = mDragInfo.cell;
                dragObject.deferDragViewCleanupPostAnimation = false;
                cell.setVisibility(VISIBLE);
            }
            cleanDragView(mDragInfo,dragObject);
            return false;
        }
        if (!mWorkspace.transitionStateShouldAllowDrop()) {
            Log.d(TAG,"acceptDrop cannot allow drop and clean dragview");
            cleanDragView(mDragInfo,dragObject);
            return false;
        }
        mWorkspace.mapPointFromSelfToChild(mDropToLayout, mDragViewVisualCenter, null);
        int spanX = 1;
        int spanY = 1;
        if (mDragInfo != null) {
            final CellLayout.CellInfo dragCellInfo = mDragInfo;
            spanX = dragCellInfo.spanX;
            spanY = dragCellInfo.spanY;
        } else {
            final ItemInfo dragInfo = (ItemInfo) dragObject.dragInfo;
            spanX = dragInfo.spanX;
            spanY = dragInfo.spanY;
        }

        int minSpanX = spanX;
        int minSpanY = spanY;
        if (dragObject.dragInfo instanceof PendingAddWidgetInfo) {
            minSpanX = ((PendingAddWidgetInfo) dragObject.dragInfo).minSpanX;
            minSpanY = ((PendingAddWidgetInfo) dragObject.dragInfo).minSpanY;
        }

        int[] resultSpan = new int[2];
        if (!mDropToLayout.findVacantCell(minSpanX, minSpanY, mTargetCell)) {
            mLauncher.showOutOfSpaceMessage(false);
            cleanDragView(mDragInfo,dragObject);
            return false;
        }
//        }
        long screenId = mWorkspace.getIdForScreen(mDropToLayout);
        if (screenId == Workspace.EXTRA_EMPTY_SCREEN_ID) {
            mWorkspace.commitExtraEmptyScreen();
        }
        return true;
    }

    private void cleanDragView(CellLayout.CellInfo dragInfo ,DragObject dragObject){
        if (dragInfo != null) {
            final View cell = dragInfo.cell;
            if(cell.getParent() == null || cell.getParent().getParent() == null){
                dragObject.deferDragViewCleanupPostAnimation = false;
                cell.setVisibility(VISIBLE);
                return;
            }
            final View parent = (View) cell.getParent().getParent();
            if (parent instanceof CellLayout) {
                ((CellLayout)parent).markCellsAsOccupiedForView(cell);
            }

            final ItemInfo info = (ItemInfo) cell.getTag();
            if (dragObject.dragView.hasDrawn() && cell.getParent() != null) {
                boolean isWidget = info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET
                        || info.itemType == LauncherSettings.Favorites.ITEM_TYPE_CUSTOM_APPWIDGET;
                if (parent instanceof CellLayout) {
                    if (isWidget) {
                        int animationType = Workspace.ANIMATE_INTO_POSITION_AND_DISAPPEAR;
                        mWorkspace.animateWidgetDrop(info, (CellLayout) parent, dragObject.dragView,
                                null, animationType, cell, false);
                    } else {
                        int duration = Workspace.ADJACENT_SCREEN_DROP_DURATION;
                        mLauncher.getDragLayer().animateViewIntoPosition(dragObject.dragView, cell, duration,
                                null, this);
                    }
                } else if (parent instanceof ShortcutAndWidgetContainer) {
                    //lijun need here 这里拖拽view弹会到widget容器的动画后面补充
                    dragObject.deferDragViewCleanupPostAnimation = false;
                    cell.setVisibility(VISIBLE);
                } else {
                    dragObject.deferDragViewCleanupPostAnimation = false;
                    cell.setVisibility(VISIBLE);
                }

            } else {
                dragObject.deferDragViewCleanupPostAnimation = false;
                cell.setVisibility(VISIBLE);
            }
        }
    }

    @Override
    public void prepareAccessibilityDrop() {
        Log.d(TAG,"prepareAccessibilityDrop");
    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        if(mLauncher!=null){
            mLauncher.getDragLayer().getDescendantRectRelativeToSelf(this, outRect);
        }
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {
        Log.d(TAG,"getLocationInDragLayer");
        if(mLauncher!=null) {
            mLauncher.getDragLayer().getLocationInDragLayer(this, loc);
        }
    }

    private int findCurrentDragOverIndex(DragObject dragObject){
        int obIndex = dragObject.x/(getWidth()/getChildCount());
        if(obIndex > markerIds.length -1){
            return INVALID_INDICATOR_INDEX;
        }
        return markerIds[obIndex];
    }

    public void setpagedView(PagedView pagedView) {
        super.setpagedView(pagedView);
        mWorkspace = (Workspace) pagedView;
    }

    public void setReCalculateWindowRange(boolean rc){
        reCalculateWindowRange = rc;
    }

    private boolean checkChange(){
        if(markerIds.length != getChildCount()){
            return true;
        }else{
            for(int i = 0 ; i < markerIds.length ; i ++ ){
                PageIndicatorMarker marker = (PageIndicatorMarker) getChildAt(i);
                if(marker.markerId != markerIds[i])return true;
            }
            return false;
        }
    }

    private boolean isActivieIndex(int index) {
        if (markerIds.length == getChildCount()) {
            for (int i = 0; i < markerIds.length; i++) {
                PageIndicatorMarker marker = (PageIndicatorMarker) getChildAt(i);
                if (marker.isActive() || markerIds[i] == index) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSnapToEnds(){
        if((mDragState == DRAG_STATE_LEFT && curDragOverIndex == 0)
                || (mDragState == DRAG_STATE_RIGHT && curDragOverIndex == mMarkers.size()-1) ){
            return true;
        }
        return false;
    }

    public int getActiveIndex(){
        return mActiveMarkerIndex;
    }
}
