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
package com.monster.launcher.allapps;

import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.launcher.BaseRecyclerView;
import com.monster.launcher.BaseRecyclerViewFastScrollBar;
import com.monster.launcher.DeviceProfile;
import com.monster.launcher.IChangeColors;
import com.monster.launcher.LauncherAppState;
import com.monster.launcher.Log;
import com.monster.launcher.R;
import com.monster.launcher.Stats;
import com.monster.launcher.Utilities;
import com.monster.launcher.util.Thunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A RecyclerView with custom fast scroll support for the all apps view.
 */
public class AllAppsRecyclerView extends BaseRecyclerView
        implements Stats.LaunchSourceProvider,IChangeColors.IItemColorChange{

    private static final int FAST_SCROLL_MODE_JUMP_TO_FIRST_ICON = 0;
    private static final int FAST_SCROLL_MODE_FREE_SCROLL = 1;

    private static final int FAST_SCROLL_BAR_MODE_DISTRIBUTE_BY_ROW = 0;
    private static final int FAST_SCROLL_BAR_MODE_DISTRIBUTE_BY_SECTIONS = 1;

    private AlphabeticalAppsList mApps;
    private int mNumAppsPerRow;

    //Begin add by xiangzx
    private static final String malphabetIndexStr = "☆,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,#";
    public static final int ALPHABET_INDEX = 0;
    public static final int SCROLL_INDEX = 1;
    private ArrayList<String> mAlphabetIndexList = new ArrayList<String>(27);
    private Context mContext;
    private float mSideIndexY;
    private LinearLayout alphabetIndexLayout;
    private float mIndexTextSize;
    private int mAlphaIndexItemHeight;
    private int pressColor;
    private int currColor;
    private String currFastSectionName;
    private int fastscrollDownOffset;
    //private GestureDetector mGestureDetector;
    //End add by xiangzx

    @Thunk BaseRecyclerViewFastScrollBar.FastScrollFocusableView mLastFastScrollFocusedView;
    @Thunk int mPrevFastScrollFocusedPosition = -1;
    @Thunk int mFastScrollFrameIndex;
    @Thunk final int[] mFastScrollFrames = new int[10];

    private final int mFastScrollMode = FAST_SCROLL_MODE_JUMP_TO_FIRST_ICON;
    private int mScrollBarMode = FAST_SCROLL_BAR_MODE_DISTRIBUTE_BY_ROW;

    private ScrollPositionState mScrollPosState = new ScrollPositionState();

    private AllAppsBackgroundDrawable mEmptySearchBackground;
    private int mEmptySearchBackgroundTopOffset;

    public AllAppsRecyclerView(Context context) {
        this(context, null);
    }

    public AllAppsRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AllAppsRecyclerView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr);
        mAlphabetIndexList.addAll(Arrays.asList(malphabetIndexStr.split(",")));
        mContext = context;
        Resources resources = mContext.getResources();
        mIndexTextSize = resources.getDimensionPixelSize(R.dimen.container_alphabet_index_size);
        fastscrollDownOffset = resources.getDimensionPixelSize(R.dimen.container_fastscroll_movedown_offset);
        mAlphaIndexItemHeight = resources.getDimensionPixelSize(R.dimen.all_apps_alphaindex_item_height);
        //mGestureDetector = new GestureDetector(mContext, new ScrollDetectorListener());
    }

    /**
     * Sets the list of apps in this view, used to determine the fastscroll position.
     */
    public void setApps(AlphabeticalAppsList apps) {
        mApps = apps;
    }

    /**
     * Sets the number of apps per row in this recycler view.
     */
    public void setNumAppsPerRow(DeviceProfile grid, int numAppsPerRow) {
        mNumAppsPerRow = numAppsPerRow;

        RecyclerView.RecycledViewPool pool = getRecycledViewPool();
        int approxRows = (int) Math.ceil(grid.availableHeightPx / grid.allAppsIconSizePx);
        pool.setMaxRecycledViews(AllAppsGridAdapter.EMPTY_SEARCH_VIEW_TYPE, 1);
        //pool.setMaxRecycledViews(AllAppsGridAdapter.SEARCH_MARKET_DIVIDER_VIEW_TYPE, 1);
        pool.setMaxRecycledViews(AllAppsGridAdapter.SEARCH_MARKET_VIEW_TYPE, 1);
        pool.setMaxRecycledViews(AllAppsGridAdapter.ICON_VIEW_TYPE, approxRows * mNumAppsPerRow);
        pool.setMaxRecycledViews(AllAppsGridAdapter.PREDICTION_ICON_VIEW_TYPE, mNumAppsPerRow);
        pool.setMaxRecycledViews(AllAppsGridAdapter.SECTION_BREAK_VIEW_TYPE, approxRows);
    }

    /**
     * Scrolls this recycler view to the top.
     */
    public void scrollToTop() {
        // Ensure we reattach the scrollbar if it was previously detached while fast-scrolling
        if (mScrollbar.isThumbDetached()) {
            mScrollbar.reattachThumbToScroll();
        }
        scrollToPosition(0);
    }

    /**
     * We need to override the draw to ensure that we don't draw the overscroll effect beyond the
     * background bounds.
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.clipRect(mBackgroundPadding.left, mBackgroundPadding.top,
                getWidth() - mBackgroundPadding.right,
                getHeight() - mBackgroundPadding.bottom);
        super.dispatchDraw(canvas);
    }

    @Override
    public void onDraw(Canvas c) {
        // Draw the background
        if (mEmptySearchBackground != null && mEmptySearchBackground.getAlpha() > 0) {
            c.clipRect(mBackgroundPadding.left, mBackgroundPadding.top,
                    getWidth() - mBackgroundPadding.right,
                    getHeight() - mBackgroundPadding.bottom);

            mEmptySearchBackground.draw(c);
        }

        super.onDraw(c);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who); //who == mEmptySearchBackground ||
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateEmptySearchBackgroundBounds();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Bind event handlers
        addOnItemTouchListener(this);
    }

    @Override
    public void fillInLaunchSourceData(Bundle sourceData) {
        sourceData.putString(Stats.SOURCE_EXTRA_CONTAINER, Stats.CONTAINER_ALL_APPS);
        if (mApps.hasFilter()) {
            sourceData.putString(Stats.SOURCE_EXTRA_SUB_CONTAINER,
                    Stats.SUB_CONTAINER_ALL_APPS_SEARCH);
        } else {
            sourceData.putString(Stats.SOURCE_EXTRA_SUB_CONTAINER,
                    Stats.SUB_CONTAINER_ALL_APPS_A_Z);
        }
    }

  //Begin add by xiangzx for AppIndex mode change
    public void setAppIndexMode(int mode){

        if(alphabetIndexLayout == null) {
            ViewGroup parent = (ViewGroup) this.getParent();
            alphabetIndexLayout = (LinearLayout) parent.findViewById(R.id.sideIndex);
        }
        switch (mode){
            case ALPHABET_INDEX:
                int childCount = alphabetIndexLayout.getChildCount();
                if  (childCount == 28){
                    return;
                }
                alphabetIndexLayout.removeAllViews();
                TextView tmpTV;
                int indexListSize = mAlphabetIndexList.size();
                for (int i = 0; i < indexListSize; i++) {
                    String tmpLetter = mAlphabetIndexList.get(i);
                    tmpTV = new TextView(mContext);
                    tmpTV.setText(tmpLetter);
                    tmpTV.setGravity(Gravity.CENTER);
                    tmpTV.setTextSize(mIndexTextSize);
                    tmpTV.setTypeface(Typeface.create("monster-normal", Typeface.NORMAL));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mAlphaIndexItemHeight, 1);
                    tmpTV.setLayoutParams(params);
                    alphabetIndexLayout.addView(tmpTV);
                }
                changeColors(LauncherAppState.getInstance().getWindowGlobalVaule().getAllColors());
                alphabetIndexLayout.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mSideIndexY = event.getY();
                        switch (event.getAction()){
                            case MotionEvent.ACTION_DOWN:
                                int indexChildCount = alphabetIndexLayout.getChildCount();
                                for (int i = 0; i < indexChildCount; i++) {
                                    TextView tmpTV = (TextView) alphabetIndexLayout.getChildAt(i);
                                    tmpTV.setTextColor(pressColor);
                                }
                                displayListItem();
                                break;

                            case MotionEvent.ACTION_MOVE:
                                displayListItem();
                                break;
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                                int childCount = alphabetIndexLayout.getChildCount();
                                for (int i = 0; i < childCount; i++) {
                                    TextView tmpTV = (TextView) alphabetIndexLayout.getChildAt(i);
                                    tmpTV.setTextColor(currColor);
                                }
                                //mScrollbar.getPopup().animateVisibility(false);
                                onFastScrollCompleted();
                        }
                        return true;
                    }
                });

                break;
            case SCROLL_INDEX:
                alphabetIndexLayout.removeAllViews();
                alphabetIndexLayout.setVisibility(View.GONE);

                break;
        }
    }

    public void displayListItem() {
        int sideIndexHeight = alphabetIndexLayout.getHeight();
        // compute number of pixels for every side index item
        double pixelPerIndexItem =(double) sideIndexHeight * 1.0 / mAlphabetIndexList.size();

        // compute the item index for given event position belongs to
        int itemPosition = (int) (mSideIndexY / pixelPerIndexItem);

        // get the item (we can do it since we know item index)
        if (itemPosition >= 0 && itemPosition < mAlphabetIndexList.size()) {

            String indexName = mAlphabetIndexList.get(itemPosition);
            List<AlphabeticalAppsList.FastScrollSectionInfo> fastScrollSections = mApps.getFastScrollerSections();
            float touchFraction = -1;
            if("☆".equals(indexName) && !mApps.mPredictedApps.isEmpty()){
                touchFraction = 0;
            }
            else{
                int size = fastScrollSections.size();
                for (int i = 0; i < size; i++) {
                    AlphabeticalAppsList.FastScrollSectionInfo info = fastScrollSections.get(i);
                    if (info.sectionName.equals(indexName)) {
                        touchFraction = info.touchFraction;
                        break;
                    }
                }
            }
            if(touchFraction >= 0){
                this.getParent().requestDisallowInterceptTouchEvent(true);
                //mScrollbar.getPopup().animateVisibility(true);
                scrollToPositionAtProgress(touchFraction);
                //mScrollbar.getPopup().setSectionName(indexName);
                //this.invalidate(mScrollbar.getPopup().updateFastScrollerBounds(this, (int)mSideIndexY));
            }
        }
    }

    public void focusCurrentIndex(String currName){
        currFastSectionName = currName;
        if(alphabetIndexLayout == null || mPrevFastScrollFocusedPosition != -1) return;
        int size = alphabetIndexLayout.getChildCount();
        for(int i=0; i<size; i++){
            TextView textView = (TextView)alphabetIndexLayout.getChildAt(i);
            if(textView.getText().toString().equals(currName)){
                if(pressColor == mContext.getResources().getColor(R.color.all_apps_index_color_click_black)){
                    textView.setTextColor(0xff000000);
                }else {
                    textView.setTextColor(0xffffffff);
                }
            }else{
                textView.setTextColor(currColor);
            }
        }
    }

    //End add by xiangzx for AppIndex mode change


    @Override
    public int getMaxScrollbarWidth() {
        if(alphabetIndexLayout == null || alphabetIndexLayout.getChildCount() ==0) {
            return super.getMaxScrollbarWidth();
        }else{
            return alphabetIndexLayout.getWidth();
        }
    }

    public void onSearchResultsChanged() {
        // Always scroll the view to the top so the user can see the changed results
        scrollToTop();

        /*if (mApps.hasNoFilteredResults()) {
            if (mEmptySearchBackground == null) {
                mEmptySearchBackground = new AllAppsBackgroundDrawable(getContext());
                mEmptySearchBackground.setAlpha(0);
                mEmptySearchBackground.setCallback(this);
                updateEmptySearchBackgroundBounds();
            }
            mEmptySearchBackground.animateBgAlpha(1f, 150);
        } else if (mEmptySearchBackground != null) {
            // For the time being, we just immediately hide the background to ensure that it does
            // not overlap with the results
            mEmptySearchBackground.setBgAlpha(0f);
        }*/
    }

    /**
     * Maps the touch (from 0..1) to the adapter position that should be visible.
     */
    @Override
    public String scrollToPositionAtProgress(float touchFraction) {

        //add by xiangzx
        if(mEnableDrawScrollBar) {
            mScrollBarMode = FAST_SCROLL_BAR_MODE_DISTRIBUTE_BY_ROW;
        }else{
            mScrollBarMode = FAST_SCROLL_BAR_MODE_DISTRIBUTE_BY_SECTIONS;
        }

        int rowCount = mApps.getNumAppRows();
        if (rowCount == 0) {
            return "";
        }

        // Stop the scroller if it is scrolling
        stopScroll();

        // Find the fastscroll section that maps to this touch fraction
        List<AlphabeticalAppsList.FastScrollSectionInfo> fastScrollSections =
                mApps.getFastScrollerSections();
        AlphabeticalAppsList.FastScrollSectionInfo lastInfo = fastScrollSections.get(0);
        if (mScrollBarMode == FAST_SCROLL_BAR_MODE_DISTRIBUTE_BY_ROW) {
            for (int i = 1; i < fastScrollSections.size(); i++) {
                AlphabeticalAppsList.FastScrollSectionInfo info = fastScrollSections.get(i);
                if (info.touchFraction > touchFraction) {
                    break;
                }
                lastInfo = info;
            }
        } else if (mScrollBarMode == FAST_SCROLL_BAR_MODE_DISTRIBUTE_BY_SECTIONS){
            lastInfo = fastScrollSections.get(Math.round(touchFraction * fastScrollSections.size()));
        } else {
            throw new RuntimeException("Unexpected scroll bar mode");
        }

        // Map the touch position back to the scroll of the recycler view
        getCurScrollState(mScrollPosState);
        int availableScrollHeight = getAvailableScrollHeight(rowCount, mScrollPosState.rowHeight);
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        if (mFastScrollMode == FAST_SCROLL_MODE_FREE_SCROLL) {
            layoutManager.scrollToPositionWithOffset(0, (int) -(availableScrollHeight * touchFraction));
        }

        if (mPrevFastScrollFocusedPosition != lastInfo.fastScrollToItem.position) {
            mPrevFastScrollFocusedPosition = lastInfo.fastScrollToItem.position;

            // Reset the last focused view
            if (mLastFastScrollFocusedView != null) {
                mLastFastScrollFocusedView.setFastScrollFocused(false, true);
                mLastFastScrollFocusedView = null;
            }

            if (mFastScrollMode == FAST_SCROLL_MODE_JUMP_TO_FIRST_ICON) {
                smoothSnapToPosition(mPrevFastScrollFocusedPosition, mScrollPosState);
            } else if (mFastScrollMode == FAST_SCROLL_MODE_FREE_SCROLL) {
                final ViewHolder vh = findViewHolderForPosition(mPrevFastScrollFocusedPosition);
                if (vh != null &&
                        vh.itemView instanceof BaseRecyclerViewFastScrollBar.FastScrollFocusableView) {
                    mLastFastScrollFocusedView =
                            (BaseRecyclerViewFastScrollBar.FastScrollFocusableView) vh.itemView;
                    mLastFastScrollFocusedView.setFastScrollFocused(true, true);
                }
            } else {
                throw new RuntimeException("Unexpected fast scroll mode");
            }
        }
        return lastInfo.sectionName;
    }

    @Override
    public void onFastScrollCompleted() {
        super.onFastScrollCompleted();
        // Reset and clean up the last focused view
        if (mLastFastScrollFocusedView != null) {
            mLastFastScrollFocusedView.setFastScrollFocused(false, true);
            mLastFastScrollFocusedView = null;
        }
        mPrevFastScrollFocusedPosition = -1;

        focusCurrentIndex(currFastSectionName); //add by xiangzx
    }

    /**
     * Updates the bounds for the scrollbar.
     */
    @Override
    public void onUpdateScrollbar(int dy) {
        List<AlphabeticalAppsList.AdapterItem> items = mApps.getAdapterItems();

        // Skip early if there are no items or we haven't been measured
        if (items.isEmpty() || mNumAppsPerRow == 0) {
            mScrollbar.setThumbOffset(-1, -1);
            return;
        }

        // Find the index and height of the first visible row (all rows have the same height)
        int rowCount = mApps.getNumAppRows();
        getCurScrollState(mScrollPosState);
        if (mScrollPosState.rowIndex < 0) {
            mScrollbar.setThumbOffset(-1, -1);
            return;
        }

        // Only show the scrollbar if there is height to be scrolled
        int availableScrollBarHeight = getAvailableScrollBarHeight();
        int availableScrollHeight = getAvailableScrollHeight(mApps.getNumAppRows(), mScrollPosState.rowHeight);
        if (availableScrollHeight <= 0) {
            mScrollbar.setThumbOffset(-1, -1);
            return;
        }

        // Calculate the current scroll position, the scrollY of the recycler view accounts for the
        // view padding, while the scrollBarY is drawn right up to the background padding (ignoring
        // padding)
        int scrollY = getPaddingTop() +
                (mScrollPosState.rowIndex * mScrollPosState.rowHeight) - mScrollPosState.rowTopOffset;
        int scrollBarY = mBackgroundPadding.top +
                (int) (((float) scrollY / availableScrollHeight) * availableScrollBarHeight);

        if (mScrollbar.isThumbDetached()) {
            int scrollBarX;
            if (Utilities.isRtl(getResources())) {
                scrollBarX = mBackgroundPadding.left;
            } else {
                scrollBarX = getWidth() - mBackgroundPadding.right - mScrollbar.getThumbWidth();
            }

            if (mScrollbar.isDraggingThumb()) {
                // If the thumb is detached, then just update the thumb to the current
                // touch position
                mScrollbar.setThumbOffset(scrollBarX, (int) mScrollbar.getLastTouchY());
            } else {
                int thumbScrollY = mScrollbar.getThumbOffset().y;
                int diffScrollY = scrollBarY - thumbScrollY;
                if (diffScrollY * dy > 0f) {
                    // User is scrolling in the same direction the thumb needs to catch up to the
                    // current scroll position.  We do this by mapping the difference in movement
                    // from the original scroll bar position to the difference in movement necessary
                    // in the detached thumb position to ensure that both speed towards the same
                    // position at either end of the list.
                    if (dy < 0) {
                        int offset = (int) ((dy * thumbScrollY) / (float) scrollBarY);
                        thumbScrollY += Math.max(offset, diffScrollY);
                    } else {
                        int offset = (int) ((dy * (availableScrollBarHeight - thumbScrollY)) /
                                (float) (availableScrollBarHeight - scrollBarY));
                        thumbScrollY += Math.min(offset, diffScrollY);
                    }
                    thumbScrollY = Math.max(0, Math.min(availableScrollBarHeight, thumbScrollY));
                    mScrollbar.setThumbOffset(scrollBarX, thumbScrollY);
                    if (scrollBarY == thumbScrollY) {
                        mScrollbar.reattachThumbToScroll();
                    }
                } else {
                    // User is scrolling in an opposite direction to the direction that the thumb
                    // needs to catch up to the scroll position.  Do nothing except for updating
                    // the scroll bar x to match the thumb width.
                    mScrollbar.setThumbOffset(scrollBarX, thumbScrollY);
                }
            }
        } else {
            synchronizeScrollBarThumbOffsetToViewScroll(mScrollPosState, rowCount);
        }
    }

    /**
     * This runnable runs a single frame of the smooth scroll animation and posts the next frame
     * if necessary.
     */
    @Thunk Runnable mSmoothSnapNextFrameRunnable = new Runnable() {
        @Override
        public void run() {
            if (mFastScrollFrameIndex < mFastScrollFrames.length) {
                scrollBy(0, mFastScrollFrames[mFastScrollFrameIndex]);
                mFastScrollFrameIndex++;
                postOnAnimation(mSmoothSnapNextFrameRunnable);
            } else {
                // Animation completed, set the fast scroll state on the target view
                final ViewHolder vh = findViewHolderForPosition(mPrevFastScrollFocusedPosition);
                if (vh != null &&
                        vh.itemView instanceof BaseRecyclerViewFastScrollBar.FastScrollFocusableView &&
                        mLastFastScrollFocusedView != vh.itemView) {
                    if(mScrollBarMode != FAST_SCROLL_BAR_MODE_DISTRIBUTE_BY_SECTIONS || mApps.hasFilter()) {
                        mLastFastScrollFocusedView =
                                (BaseRecyclerViewFastScrollBar.FastScrollFocusableView) vh.itemView;
                        mLastFastScrollFocusedView.setFastScrollFocused(true, true);
                    }
                }

            }
        }
    };

    /**
     * Smoothly snaps to a given position.  We do this manually by calculating the keyframes
     * ourselves and animating the scroll on the recycler view.
     */
    private void smoothSnapToPosition(final int position, ScrollPositionState scrollPosState) {
        removeCallbacks(mSmoothSnapNextFrameRunnable);

        // Calculate the full animation from the current scroll position to the final scroll
        // position, and then run the animation for the duration.
        int curScrollY = getPaddingTop() + (scrollPosState.rowIndex * scrollPosState.rowHeight) - scrollPosState.rowTopOffset;
        int newScrollY = getScrollAtPosition(position, scrollPosState.rowHeight);
        if(mScrollBarMode == FAST_SCROLL_BAR_MODE_DISTRIBUTE_BY_SECTIONS) {
            curScrollY += scrollPosState.sectionIndex * 2 * mApps.SECTION_OFFSET;
        }
        if(curScrollY < newScrollY){
            newScrollY += fastscrollDownOffset;
        }
        int numFrames = mFastScrollFrames.length;
        for (int i = 0; i < numFrames; i++) {
            // TODO(winsonc): We can interpolate this as well.
            mFastScrollFrames[i] = (newScrollY - curScrollY) / numFrames;
        }
        mFastScrollFrameIndex = 0;
        postOnAnimation(mSmoothSnapNextFrameRunnable);
    }

    /**
     * Returns the current scroll state of the apps rows.
     */
    protected void getCurScrollState(ScrollPositionState stateOut) {
        stateOut.rowIndex = -1;
        stateOut.rowTopOffset = -1;
        stateOut.rowHeight = -1;
        stateOut.sectionIndex = -1;

        // Return early if there are no items or we haven't been measured
        List<AlphabeticalAppsList.AdapterItem> items = mApps.getAdapterItems();
        if (items.isEmpty() || mNumAppsPerRow == 0) {
            return;
        }

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int position = getChildPosition(child);
            if (position != NO_POSITION) {
                AlphabeticalAppsList.AdapterItem item = items.get(position);
                if (item.viewType == AllAppsGridAdapter.ICON_VIEW_TYPE ||
                        item.viewType == AllAppsGridAdapter.PREDICTION_ICON_VIEW_TYPE) {
                    stateOut.rowIndex = item.rowIndex;
                    stateOut.rowTopOffset = getLayoutManager().getDecoratedTop(child);
                    if(mScrollBarMode == FAST_SCROLL_BAR_MODE_DISTRIBUTE_BY_SECTIONS){
                        stateOut.rowTopOffset = child.getTop(); //modify by xiangzx
                    }
                    stateOut.rowHeight = child.getHeight();
                    List<AlphabeticalAppsList.FastScrollSectionInfo> fastScrollSections = mApps.getFastScrollerSections();
                    for(int k=0; k<fastScrollSections.size(); k++){
                        if(fastScrollSections.get(k).sectionName.equals(item.sectionName)){
                            stateOut.sectionIndex = k;
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * Returns the scrollY for the given position in the adapter.
     */
    private int getScrollAtPosition(int position, int rowHeight) {
        AlphabeticalAppsList.AdapterItem item = mApps.getAdapterItems().get(position);
        if (item.viewType == AllAppsGridAdapter.ICON_VIEW_TYPE ||
                item.viewType == AllAppsGridAdapter.PREDICTION_ICON_VIEW_TYPE) {
            //add by xiangzx
            int sectionIndex = 0;
            List<AlphabeticalAppsList.FastScrollSectionInfo> fastScrollSections =
                    mApps.getFastScrollerSections();
            for(int i=0;i<fastScrollSections.size();i++){
                if(fastScrollSections.get(i).sectionName.equals(item.sectionName)){
                    sectionIndex = i;
                    break;
                }
            }
            //int offset = item.rowIndex > 0 ? getPaddingTop() : 0;
            int offset = -mApps.SECTION_OFFSET;
            if(mScrollBarMode == FAST_SCROLL_BAR_MODE_DISTRIBUTE_BY_SECTIONS){
                offset += sectionIndex * 2 * mApps.SECTION_OFFSET;
            }
            return offset + item.rowIndex * rowHeight - mApps.SECTION_OFFSET;
        } else {
            return 0;
        }
    }

    /**
     * Updates the bounds of the empty search background.
     */
    private void updateEmptySearchBackgroundBounds() {
        if (mEmptySearchBackground == null) {
            return;
        }

        // Center the empty search background on this new view bounds
        int x = (getMeasuredWidth() - mEmptySearchBackground.getIntrinsicWidth()) / 2;
        int y = mEmptySearchBackgroundTopOffset;
        mEmptySearchBackground.setBounds(x, y,
                x + mEmptySearchBackground.getIntrinsicWidth(),
                y + mEmptySearchBackground.getIntrinsicHeight());
    }

    @Override
    public void changeColors(int[] colors) {
        if(alphabetIndexLayout != null) {
            int childCount = alphabetIndexLayout.getChildCount();
            if (childCount == 28) {
                boolean isTextColorBlack = colors[0]!= -1;
                for (int i = 0; i < childCount; i++) {
                    TextView textView = (TextView) alphabetIndexLayout.getChildAt(i);
                    if (isTextColorBlack) {
                        currColor = mContext.getResources().getColor(R.color.all_apps_index_color_black);
                        textView.setTextColor(currColor);
                        pressColor = mContext.getResources().getColor(R.color.all_apps_index_color_click_black);
                    } else {
                        currColor = mContext.getResources().getColor(R.color.all_apps_index_color_white);
                        textView.setTextColor(currColor);
                        pressColor = mContext.getResources().getColor(R.color.all_apps_index_color_click_white);
                    }
                }
            }
        }
    }

    //add by xiangzx
    public void changeIndexLayoutVisibility(boolean visible){
        if(alphabetIndexLayout == null){
            return;
        }
        int childCount = alphabetIndexLayout.getChildCount();
        if  (childCount == 28){
            alphabetIndexLayout.setVisibility(visible? View.VISIBLE:View.INVISIBLE);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent ev) {
        if(mEnableDrawScrollBar) {
            return super.onInterceptTouchEvent(rv,ev);
        }else{
            return mPrevFastScrollFocusedPosition != -1;
        }
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent e) {
        if(mGestureDetector.onTouchEvent(e)){
            Log.v("skg","onTouchEvent---------true");
            return true;
        }
        return super.onTouchEvent(e);
    }

    public final class ScrollDetectorListener implements GestureDetector.OnGestureListener{
        private float mDownX;
        private float mDownY;
        private boolean hasOccurs;


        @Override
        public boolean onDown(MotionEvent e) {
            Log.v("skg","onDown:"+e);
            hasOccurs = false;
            mDownX = e.getX();
            mDownY = e.getY();
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.v("skg","(e2.getX() - mDownX)="+(e2.getX() - mDownX)+",  (e2.getY() - mDownY)="+(e2.getY() - mDownY));
            if(!hasOccurs && e2.getX() - mDownX > 400 && Math.abs(e2.getY() - mDownY) < 100){
                Log.v("skg","onScroll enter");
                hasOccurs = true;
                new Thread(){
                    public void run() {
                        try{
                            Instrumentation inst = new Instrumentation();
                            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                        }
                        catch (Exception e) {
                            Log.e("Exception when onBack", e.toString());
                        }
                    }
                }.start();
                return true;
            }

            if(hasOccurs){
               return true;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.v("skg", "onFling() called with: " + "e1 = [" + e1 + "], e2 = [" + e2 + "], velocityX = [" + velocityX + "], velocityY = [" + velocityY + "]");
            if(!hasOccurs && velocityX > 0 && Math.abs(e2.getY() - mDownY) < 100){
                Log.v("skg","onFling enter");
                new Thread(){
                    public void run() {
                        try{
                            Instrumentation inst = new Instrumentation();
                            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                        }
                        catch (Exception e) {
                            Log.e("Exception when onBack", e.toString());
                        }
                    }
                }.start();
                return true;
            }
            return false;
        }
    }*/
}
