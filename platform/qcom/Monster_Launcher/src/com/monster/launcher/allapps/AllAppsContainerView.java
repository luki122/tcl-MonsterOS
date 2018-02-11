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

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.UserHandle;
import android.support.v7.widget.RecyclerView;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monster.launcher.AppInfo;
import com.monster.launcher.BaseContainerView;
import com.monster.launcher.CellLayout;
import com.monster.launcher.DeleteDropTarget;
import com.monster.launcher.DeviceProfile;
import com.monster.launcher.DragSource;
import com.monster.launcher.DropTarget;
import com.monster.launcher.Folder;
import com.monster.launcher.IChangeLauncherColor;
import com.monster.launcher.ItemInfo;
import com.monster.launcher.Launcher;
import com.monster.launcher.LauncherAppState;
import com.monster.launcher.LauncherTransitionable;
import com.monster.launcher.LocationDropTarget;
import com.monster.launcher.Log;
import com.monster.launcher.R;
import com.monster.launcher.Utilities;
import com.monster.launcher.Workspace;
import com.monster.launcher.util.ComponentKey;
import com.monster.launcher.util.Thunk;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * A merge algorithm that merges every section indiscriminately.
 */
final class FullMergeAlgorithm implements AlphabeticalAppsList.MergeAlgorithm {

    @Override
    public boolean continueMerging(AlphabeticalAppsList.SectionInfo section,
           AlphabeticalAppsList.SectionInfo withSection,
           int sectionAppCount, int numAppsPerRow, int mergeCount) {
        // Don't merge the predicted apps
        if (section.firstAppItem.viewType != AllAppsGridAdapter.ICON_VIEW_TYPE) {
            return false;
        }
        // Otherwise, merge every other section
        return true;
    }
}

/**
 * The logic we use to merge multiple sections.  We only merge sections when their final row
 * contains less than a certain number of icons, and stop at a specified max number of merges.
 * In addition, we will try and not merge sections that identify apps from different scripts.
 */
final class SimpleSectionMergeAlgorithm implements AlphabeticalAppsList.MergeAlgorithm {

    private int mMinAppsPerRow;
    private int mMinRowsInMergedSection;
    private int mMaxAllowableMerges;
    private CharsetEncoder mAsciiEncoder;

    public SimpleSectionMergeAlgorithm(int minAppsPerRow, int minRowsInMergedSection, int maxNumMerges) {
        mMinAppsPerRow = minAppsPerRow;
        mMinRowsInMergedSection = minRowsInMergedSection;
        mMaxAllowableMerges = maxNumMerges;
        mAsciiEncoder = Charset.forName("US-ASCII").newEncoder();
    }

    @Override
    public boolean continueMerging(AlphabeticalAppsList.SectionInfo section,
           AlphabeticalAppsList.SectionInfo withSection,
           int sectionAppCount, int numAppsPerRow, int mergeCount) {
        // Don't merge the predicted apps
        if (section.firstAppItem.viewType != AllAppsGridAdapter.ICON_VIEW_TYPE) {
            return false;
        }

        // Continue merging if the number of hanging apps on the final row is less than some
        // fixed number (ragged), the merged rows has yet to exceed some minimum row count,
        // and while the number of merged sections is less than some fixed number of merges
        int rows = sectionAppCount / numAppsPerRow;
        int cols = sectionAppCount % numAppsPerRow;

        // Ensure that we do not merge across scripts, currently we only allow for english and
        // native scripts so we can test if both can just be ascii encoded
        boolean isCrossScript = false;
        if (section.firstAppItem != null && withSection.firstAppItem != null) {
            isCrossScript = mAsciiEncoder.canEncode(section.firstAppItem.sectionName) !=
                    mAsciiEncoder.canEncode(withSection.firstAppItem.sectionName);
        }
        return (0 < cols && cols < mMinAppsPerRow) &&
                rows < mMinRowsInMergedSection &&
                mergeCount < mMaxAllowableMerges &&
                !isCrossScript;
    }
}

/**
 * The all apps view container.
 */
public class AllAppsContainerView extends BaseContainerView implements DragSource,
        LauncherTransitionable, View.OnTouchListener, View.OnLongClickListener,
        AllAppsSearchBarController.Callbacks, IChangeLauncherColor {

    private static final int MIN_ROWS_IN_MERGED_SECTION_PHONE = 3;
    private static final int MAX_NUM_MERGES_PHONE = 2;

    @Thunk
    Launcher mLauncher;
    @Thunk AlphabeticalAppsList mApps;
    private AllAppsGridAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.ItemDecoration mItemDecoration;

    @Thunk View mContent;
    @Thunk View mContainerView;
    @Thunk View mRevealView;
    @Thunk AllAppsRecyclerView mAppsRecyclerView;
    @Thunk AllAppsSearchBarController mSearchBarController;
    private ViewGroup mSearchBarContainerView;
    private View mSearchBarView;
    private SpannableStringBuilder mSearchQueryBuilder = null;

    private int mSectionNamesMargin;
    private int mNumAppsPerRow;
    private int mNumPredictedAppsPerRow;
    private int mRecyclerViewTopBottomPadding;
    // This coordinate is relative to this container view
    private final Point mBoundsCheckLastTouchDownPos = new Point(-1, -1);
    // This coordinate is relative to its parent
    private final Point mIconLastTouchPos = new Point();
   //add by xiangzx
    private int mDefaultNameMargin;
    private int startInset;
    private int availableWidth;
    private int itemGap;

    private View.OnClickListener mSearchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent searchIntent = (Intent) v.getTag();
            mLauncher.startActivitySafely(v, searchIntent, null);
        }
    };

    public AllAppsContainerView(Context context) {
        this(context, null);
    }

    public AllAppsContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources res = context.getResources();

        mLauncher = (Launcher) context;

        //modify by xiangzx
        //mSectionNamesMargin = res.getDimensionPixelSize(R.dimen.all_apps_grid_view_start_margin);
        mDefaultNameMargin = mSectionNamesMargin = res.getDimensionPixelSize(R.dimen.all_apps_grid_view_start_margin);
        Log.i("AllAppsContainerView","mDefaultNameMargin = "+mDefaultNameMargin);

        mApps = new AlphabeticalAppsList(context);
        mAdapter = new AllAppsGridAdapter(mLauncher, mApps, this, mLauncher, this);
        mApps.setAdapter(mAdapter);
        mLayoutManager = mAdapter.getLayoutManager();
        mItemDecoration = mAdapter.getItemDecoration();
        mRecyclerViewTopBottomPadding =
                res.getDimensionPixelSize(R.dimen.all_apps_list_top_bottom_padding);

        mSearchQueryBuilder = new SpannableStringBuilder();
        Selection.setSelection(mSearchQueryBuilder, 0);
    }

    /**
     * Sets the current set of predicted apps.
     */
    public void setPredictedApps(List<ComponentKey> apps) {
        mApps.setPredictedApps(apps);
    }

    /**
     * Sets the current set of apps.
     */
    public void setApps(List<AppInfo> apps) {
        mApps.setApps(apps);
    }

    /**
     * Adds new apps to the list.
     */
    public void addApps(List<AppInfo> apps) {
        mApps.addApps(apps);
    }

    /**
     * Updates existing apps in the list
     */
    public void updateApps(List<AppInfo> apps) {
        mApps.updateApps(apps);
    }

    /**
     * Removes some apps from the list.
     */
    public void removeApps(List<AppInfo> apps) {
        mApps.removeApps(apps);
    }

    /**
     * Sets the search bar that shows above the a-z list.
     */
    public void setSearchBarController(AllAppsSearchBarController searchController) {
        if (mSearchBarController != null) {
            throw new RuntimeException("Expected search bar controller to only be set once");
        }
        mSearchBarController = searchController;
        mSearchBarController.initialize(mApps, this);

        // Add the new search view to the layout
        View searchBarView = searchController.getView(mSearchBarContainerView);
        mSearchBarContainerView.addView(searchBarView);
        mSearchBarContainerView.setVisibility(View.VISIBLE);
        mSearchBarView = searchBarView;
        setHasSearchBar();

        updateBackgroundAndPaddings();
    }

    public ViewGroup getSearchBarContainerView(){
        return mSearchBarContainerView;
    }

    /**
     * Scrolls this list view to the top.
     */
    public void scrollToTop() {
        mAppsRecyclerView.scrollToTop();
    }

    /**
     * Returns the content view used for the launcher transitions.
     */
    public View getContentView() {
        return mContainerView;
    }

    /**
     * Returns the all apps search view.
     */
    public View getSearchBarView() {
        return mSearchBarView;
    }

    /**
     * Returns the reveal view used for the launcher transitions.
     */
    public View getRevealView() {
        return mRevealView;
    }

    /**
     * Returns an new instance of the default app search controller.
     */
    public AllAppsSearchBarController newDefaultAppSearchController() {
        return new DefaultAppSearchController(getContext(), this, mAppsRecyclerView);
    }

    /**
     * Focuses the search field and begins an app search.
     */
    public void startAppsSearch() {
        if (mSearchBarController != null) {
            mSearchBarController.focusSearchField();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        boolean isRtl = Utilities.isRtl(getResources());
        mAdapter.setRtl(isRtl);
        mContent = findViewById(R.id.content);

        // This is a focus listener that proxies focus from a view into the list view.  This is to
        // work around the search box from getting first focus and showing the cursor.
        View.OnFocusChangeListener focusProxyListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mAppsRecyclerView.requestFocus();
                }
            }
        };
        mSearchBarContainerView = (ViewGroup) findViewById(R.id.search_box_container);
        mSearchBarContainerView.setOnFocusChangeListener(focusProxyListener);
        mContainerView = findViewById(R.id.all_apps_container);
        mContainerView.setOnFocusChangeListener(focusProxyListener);
        mRevealView = findViewById(R.id.all_apps_reveal);

        // Load the all apps recycler view
        mAppsRecyclerView = (AllAppsRecyclerView) findViewById(R.id.apps_list_view);
        mAppsRecyclerView.setApps(mApps);
        mAppsRecyclerView.setLayoutManager(mLayoutManager);
        mAppsRecyclerView.setAdapter(mAdapter);
        mAppsRecyclerView.setHasFixedSize(true);
        if (mItemDecoration != null) {
            mAppsRecyclerView.addItemDecoration(mItemDecoration);
        }

        updateBackgroundAndPaddings();
    }

    @Override
    public void onBoundsChanged(Rect newBounds) {
        mLauncher.updateOverlayBounds(newBounds);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Update the number of items in the grid before we measure the view
        availableWidth = !mContentBounds.isEmpty() ? mContentBounds.width() :
                MeasureSpec.getSize(widthMeasureSpec);
        DeviceProfile grid = mLauncher.getDeviceProfile();

        /*Begin modify by xiangzx to set different layout for different language */
        //grid.updateAppsViewNumCols(getResources(), availableWidth);
        Locale curLocale = mLauncher.getResources().getConfiguration().locale;
        if(!(curLocale.equals(Locale.SIMPLIFIED_CHINESE)||curLocale.getLanguage().equals(Locale.ENGLISH.getLanguage()))){
            mSectionNamesMargin = 0;
            mAppsRecyclerView.setAppIndexMode(mAppsRecyclerView.SCROLL_INDEX);
            mAppsRecyclerView.mEnableDrawScrollBar = true;
        }else{
            mSectionNamesMargin = mDefaultNameMargin;
            mAppsRecyclerView.setAppIndexMode(mAppsRecyclerView.ALPHABET_INDEX);
            mAppsRecyclerView.mEnableDrawScrollBar = false;
        }
        mAdapter.setNameMargin(mSectionNamesMargin);
        grid.updateAppsViewNumCols(getResources(), availableWidth, mSectionNamesMargin);
        /*End modify by xiangzx to set different layout for different language */

        if (mNumAppsPerRow != grid.allAppsNumCols ||
                mNumPredictedAppsPerRow != grid.allAppsNumPredictiveCols) {
            mNumAppsPerRow = grid.allAppsNumCols;
            mNumPredictedAppsPerRow = grid.allAppsNumPredictiveCols;

            // If there is a start margin to draw section names, determine how we are going to merge
            // app sections
            boolean mergeSectionsFully = mSectionNamesMargin == 0 || !grid.isPhone;
            AlphabeticalAppsList.MergeAlgorithm mergeAlgorithm = mergeSectionsFully ?
                    new FullMergeAlgorithm() :
                    new SimpleSectionMergeAlgorithm(//(int) Math.ceil(mNumAppsPerRow / 2f), modify by xiangzx
                            1, MIN_ROWS_IN_MERGED_SECTION_PHONE, MAX_NUM_MERGES_PHONE);

            mAppsRecyclerView.setNumAppsPerRow(grid, mNumAppsPerRow);
            mAdapter.setNumAppsPerRow(mNumAppsPerRow);
            mApps.setNumAppsPerRow(mNumAppsPerRow, mNumPredictedAppsPerRow, mergeAlgorithm);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //add by xiangzx
        startInset = Math.max(mSectionNamesMargin, mAppsRecyclerView.getMaxScrollbarWidth());
        if(hasFilters()){
            setRecyclerViewPaddingInFilters();
        }else{
            setRecyclerViewPadding();
        }
    }

    /**
     * Update the background and padding of the Apps view and children.  Instead of insetting the
     * container view, we inset the background and padding of the recycler view to allow for the
     * recycler view to handle touch events (for fast scrolling) all the way to the edge.
     */
    @Override
    protected void onUpdateBackgroundAndPaddings(Rect searchBarBounds, Rect padding) {
        boolean isRtl = Utilities.isRtl(getResources());

        // TODO: Use quantum_panel instead of quantum_panel_shape
        //modify by xiangzx
        /*InsetDrawable background = new InsetDrawable(
                getResources().getDrawable(R.drawable.quantum_panel_shape), padding.left, 0,
                padding.right, 0);
        Rect bgPadding = new Rect();
        background.getPadding(bgPadding);
        mContainerView.setBackground(background);
        mRevealView.setBackground(background.getConstantState().newDrawable());
        mAppsRecyclerView.updateBackgroundPadding(bgPadding);
        mAdapter.updateBackgroundPadding(bgPadding);*/

        // Hack: We are going to let the recycler view take the full width, so reset the padding on
        // the container to zero after setting the background and apply the top-bottom padding to
        // the content view instead so that the launcher transition clips correctly.

        mContent.setPadding(0, padding.top, 0, padding.bottom);
        mContainerView.setPadding(0, 0, 0, 0);

        // Pad the recycler view by the background padding plus the start margin (for the section
        // names)
        startInset = Math.max(mSectionNamesMargin, mAppsRecyclerView.getMaxScrollbarWidth());
        int topBottomPadding = mRecyclerViewTopBottomPadding;
        if (isRtl) {
            mAppsRecyclerView.setPadding(getResources().getDimensionPixelSize(R.dimen.all_apps_recycleview_right_padding),//padding.left,
                    /*topBottomPadding*/ 0, padding.right + startInset, topBottomPadding);
        } else {
            if(hasFilters()){
                setRecyclerViewPaddingInFilters();
            }else{
                setRecyclerViewPadding();
            }
        }

        // Inset the search bar to fit its bounds above the container
        if (mSearchBarView != null) {
            Rect backgroundPadding = new Rect();
            if (mSearchBarView.getBackground() != null) {
                mSearchBarView.getBackground().getPadding(backgroundPadding);
            }
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)
                    mSearchBarContainerView.getLayoutParams();
            lp.leftMargin = searchBarBounds.left - backgroundPadding.left;
            lp.topMargin = searchBarBounds.top - backgroundPadding.top;
            lp.rightMargin = (getMeasuredWidth() - searchBarBounds.right) - backgroundPadding.right;
            mSearchBarContainerView.requestLayout();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Determine if the key event was actual text, if so, focus the search bar and then dispatch
        // the key normally so that it can process this key event
        if (!mSearchBarController.isSearchFieldFocused() &&
                event.getAction() == KeyEvent.ACTION_DOWN) {
            final int unicodeChar = event.getUnicodeChar();
            final boolean isKeyNotWhitespace = unicodeChar > 0 &&
                    !Character.isWhitespace(unicodeChar) && !Character.isSpaceChar(unicodeChar);
            if (isKeyNotWhitespace) {
                boolean gotKey = TextKeyListener.getInstance().onKeyDown(this, mSearchQueryBuilder,
                        event.getKeyCode(), event);
                if (gotKey && mSearchQueryBuilder.length() > 0) {
                    mSearchBarController.focusSearchField();
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return handleTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return handleTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mIconLastTouchPos.set((int) ev.getX(), (int) ev.getY());
                break;
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        // Return early if this is not initiated from a touch
        if (!v.isInTouchMode()) return false;
        // When we have exited all apps or are in transition, disregard long clicks
        if (!mLauncher.isAppsViewVisible() ||
                mLauncher.getWorkspace().isSwitchingState()) return false;
        // Return if global dragging is not enabled
        if (!mLauncher.isDraggingEnabled()) return false;

        // Start the drag
        mLauncher.getWorkspace().beginDragShared(v, mIconLastTouchPos, this, false);

        //modify by xiangzx
        if(LauncherAppState.getInstance().isShowAllAppsInWorkspace()) {
            mSearchBarContainerView.setVisibility(View.INVISIBLE);
            mLauncher.getSearchDropTargetBar().bringToFront();
        }else{
            // Enter spring loaded mode
            mLauncher.enterSpringLoadedDragMode();
            mSearchBarContainerView.setVisibility(View.VISIBLE);
        }

        return false;
    }

    @Override
    public boolean supportsFlingToDelete() {
        return true;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return true;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        DeviceProfile grid = mLauncher.getDeviceProfile();
        return (float) grid.allAppsIconSizePx / grid.iconSizePx;
    }

    @Override
    public void onFlingToDeleteCompleted() {
        // We just dismiss the drag when we fling, so cleanup here
        if(!LauncherAppState.getInstance().isShowAllAppsInWorkspace()) {
            mLauncher.exitSpringLoadedDragModeDelayed(true,
                    Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
        }
        mLauncher.unlockScreenOrientation(false);
    }

    @Override
    public void onDropCompleted(View target, DropTarget.DragObject d, boolean isFlingToDelete,
            boolean success) {
        //modify by xiangzx
      if(!LauncherAppState.getInstance().isShowAllAppsInWorkspace()) {
       if (isFlingToDelete || !success || (target != mLauncher.getWorkspace() &&
                !(target instanceof DeleteDropTarget) && !(target instanceof Folder))) {
            // Exit spring loaded mode if we have not successfully dropped or have not handled the
            // drop in Workspace
            mLauncher.exitSpringLoadedDragModeDelayed(true,
                    Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
        }
            mLauncher.unlockScreenOrientation(false);

            // Display an error message if the drag failed due to there not being enough space on the
            // target layout we were dropping on.
            if (!success) {
            boolean showOutOfSpaceMessage = false;
            if (target instanceof Workspace) {
                int currentScreen = mLauncher.getCurrentWorkspaceScreen();
                Workspace workspace = (Workspace) target;
                CellLayout layout = (CellLayout) workspace.getChildAt(currentScreen);
                ItemInfo itemInfo = (ItemInfo) d.dragInfo;
                if (layout != null) {
                    showOutOfSpaceMessage =
                            !layout.findCellForSpan(null, itemInfo.spanX, itemInfo.spanY);
                }
            }
            if (showOutOfSpaceMessage) {
                mLauncher.showOutOfSpaceMessage(false);
            }
                d.deferDragViewCleanupPostAnimation = false;
            }
        }else {
            //add by xiangzx,when drag view from allAppsContainer, still in APPS mode
            mSearchBarContainerView.setVisibility(View.VISIBLE);
            mLauncher.unlockScreenOrientation(false);
            if (!success) {
                d.deferDragViewCleanupPostAnimation = false;
            }
        }
    }

    @Override
    public void onLauncherTransitionPrepare(Launcher l, boolean animated, boolean toWorkspace) {
        // Do nothing
    }

    @Override
    public void onLauncherTransitionStart(Launcher l, boolean animated, boolean toWorkspace) {
        // Do nothing
    }

    @Override
    public void onLauncherTransitionStep(Launcher l, float t) {
        // Do nothing
    }

    @Override
    public void onLauncherTransitionEnd(Launcher l, boolean animated, boolean toWorkspace) {
        if (toWorkspace) {
            // Reset the search bar and base recycler view after transitioning home
            mSearchBarController.reset();
            mAppsRecyclerView.reset();
        }
    }

    /**
     * Handles the touch events to dismiss all apps when clicking outside the bounds of the
     * recycler view.
     */
    private boolean handleTouchEvent(MotionEvent ev) {
        DeviceProfile grid = mLauncher.getDeviceProfile();
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mContentBounds.isEmpty()) {
                    // Outset the fixed bounds and check if the touch is outside all apps
                    Rect tmpRect = new Rect(mContentBounds);
                    tmpRect.inset(-grid.allAppsIconSizePx / 2, 0);
                    if (ev.getX() < tmpRect.left || ev.getX() > tmpRect.right) {
                        mBoundsCheckLastTouchDownPos.set(x, y);
                        return true;
                    }
                } else {
                    // Check if the touch is outside all apps
                    if (ev.getX() < getPaddingLeft() ||
                            ev.getX() > (getWidth() - getPaddingRight())) {
                        mBoundsCheckLastTouchDownPos.set(x, y);
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mBoundsCheckLastTouchDownPos.x > -1) {
                    ViewConfiguration viewConfig = ViewConfiguration.get(getContext());
                    float dx = ev.getX() - mBoundsCheckLastTouchDownPos.x;
                    float dy = ev.getY() - mBoundsCheckLastTouchDownPos.y;
                    float distance = (float) Math.hypot(dx, dy);
                    if (distance < viewConfig.getScaledTouchSlop()) {
                        // The background was clicked, so just go home
                        Launcher launcher = (Launcher) getContext();
                        launcher.showWorkspace(true);
                        return true;
                    }
                }
                // Fall through
            case MotionEvent.ACTION_CANCEL:
                mBoundsCheckLastTouchDownPos.set(-1, -1);
                break;
        }
        return false;
    }

    @Override
    public void onSearchResult(String query, ArrayList<ComponentKey> apps) {
        if (apps != null) {
            mApps.setOrderedFilter(apps);
            mAdapter.setLastSearchQuery(query);
            mAppsRecyclerView.onSearchResultsChanged();
        }
    }

    @Override
    public void clearSearchResult() {
        mApps.setOrderedFilter(null);
        mAppsRecyclerView.onSearchResultsChanged();

        // Clear the search query
        mSearchQueryBuilder.clear();
        mSearchQueryBuilder.clearSpans();
        Selection.setSelection(mSearchQueryBuilder, 0);
    }

    //lijun add for unread
    public void updateAppsUnreadChanged(ComponentName componentName, int unreadNum, UserHandle user) {
        List<AlphabeticalAppsList.AdapterItem> mAdapterItems = mApps.getAdapterItems();
        final int size = mAdapterItems.size();
        AlphabeticalAppsList.AdapterItem adapterItem = null;
        for (int i = 0; i < size; i++) {
            adapterItem = mAdapterItems.get(i);
            boolean isCurrentUser = true;
            if (user != null && adapterItem.appInfo != null && adapterItem.appInfo.user != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                isCurrentUser = adapterItem.appInfo.user.getUser().equals(user);
            }
            if (isCurrentUser && adapterItem.appInfo != null && adapterItem.appInfo.intent != null
                    && adapterItem.appInfo.intent.getComponent().equals(componentName)) {
                adapterItem.appInfo.unreadNum = unreadNum;
            }
        }
        // Refresh the recycler view
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }
    /*Begin add by xiangzx to reponse back key in apps search*/
    public boolean hasFilters(){
        return mApps.hasFilter();
    }

    public void clearFilters(){
        if(mSearchBarController instanceof DefaultAppSearchController) {
            DefaultAppSearchController defaultAppSearchController = (DefaultAppSearchController)mSearchBarController;
            defaultAppSearchController.hideSearchField(true, defaultAppSearchController.mFocusRecyclerViewRunnable);
        }
    }

    public void hideInputMethod(){
        if(mSearchBarController instanceof DefaultAppSearchController) {
            DefaultAppSearchController defaultAppSearchController = (DefaultAppSearchController)mSearchBarController;
            defaultAppSearchController.hideInputMethod();
        }
    }

    public boolean isSearchFiledFocused(){
        return mSearchBarController.isSearchFieldFocused();
    }
    /*End add by xiangzx to reponse back key in apps search*/

    @Override
    public void onColorChanged(int[] colors) {
        mLauncher.getAppSearchView().changeColors(colors);
        mSearchBarController.changeColors(colors);
        mAdapter.changeColors(colors);
        if(mAppsRecyclerView != null) {
            mAppsRecyclerView.changeColors(colors);
        }
        mSearchBarContainerView.setBackgroundColor(colors[4]);
    }

    public void setMarketApp(){
        mAdapter.setMarketApp();
    }

    //add by xiangzx
    public void setRecyclerViewPaddingInFilters(){
        DeviceProfile grid = mLauncher.getDeviceProfile();
        itemGap = (availableWidth - grid.allAppsIconSizePx * mNumAppsPerRow)/(mNumAppsPerRow+1);
        mAppsRecyclerView.setPadding(itemGap, 0, 0, mRecyclerViewTopBottomPadding);
    }

    public void setRecyclerViewPadding(){
        mAppsRecyclerView.setPadding(startInset, 0, getResources().getDimensionPixelSize(R.dimen.all_apps_recycleview_right_padding), mRecyclerViewTopBottomPadding);
    }

    public void setEmptyViewRightMargin(View view){
        view.setTranslationX(-itemGap/2);
    }
}
