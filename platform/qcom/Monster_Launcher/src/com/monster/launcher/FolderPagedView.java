/**
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

package com.monster.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.monster.launcher.PageIndicator.PageMarkerResources;
import com.monster.launcher.Workspace.ItemOperator;
import com.monster.launcher.util.Thunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FolderPagedView extends PagedView {

    private static final String TAG = "FolderPagedView";

    private static final boolean ALLOW_FOLDER_SCROLL = true;

    private static final int REORDER_ANIMATION_DURATION = 230;
    private static final int START_VIEW_REORDER_DELAY = 30;
    private static final float VIEW_REORDER_DELAY_FACTOR = 0.9f;

    private static final int PAGE_INDICATOR_ANIMATION_START_DELAY = 300;
    private static final int PAGE_INDICATOR_ANIMATION_STAGGERED_DELAY = 150;
    private static final int PAGE_INDICATOR_ANIMATION_DURATION = 400;

    // This value approximately overshoots to 1.5 times the original size.
    private static final float PAGE_INDICATOR_OVERSHOOT_TENSION = 4.9f;

    /**
     * Fraction of the width to scroll when showing the next page hint.
     */
    private static final float SCROLL_HINT_FRACTION = 0.07f;

    private static final int[] sTempPosArray = new int[2];

    public final boolean mIsRtl;

    private final LayoutInflater mInflater;
    private final IconCache mIconCache;

    @Thunk
    final HashMap<View, Runnable> mPendingAnimations = new HashMap<>();

    private final int mMaxCountX;
    private final int mMaxCountY;
    private final int mMaxItemsPerPage;

    private int mAllocatedContentSize;
    private int mGridCountX;
    private int mGridCountY;

    private Folder mFolder;
    private FocusIndicatorView mFocusIndicatorView;
    private FocusHelper.PagedFolderKeyEventListener mKeyListener;

    protected PageIndicator mPageIndicator;//liuzuo protected>>private



    //M:liuzuo add addIcon begin
    private TextView mFolderAddIcon ;
    private View.OnClickListener clickListener;
    private int mWidthGap;
    AnimatorSet mIconAnima;
    //M:liuzuo add addIcon end
    public FolderPagedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LauncherAppState app = LauncherAppState.getInstance();

        InvariantDeviceProfile profile = app.getInvariantDeviceProfile();
        mMaxCountX = profile.numFolderColumns;
        mMaxCountY = profile.numFolderRows;

        mMaxItemsPerPage = mMaxCountX * mMaxCountY;

        mInflater = LayoutInflater.from(context);
        mIconCache = app.getIconCache();

        mIsRtl = Utilities.isRtl(getResources());
        mWidthGap=getResources().getDimensionPixelSize(R.dimen.folder_width_gap);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);

        setEdgeGlowColor(getResources().getColor(R.color.folder_edge_effect_color));
    }

    public void setFolder(Folder folder) {
        mFolder = folder;
        mFocusIndicatorView = (FocusIndicatorView) folder.findViewById(R.id.focus_indicator);
        mKeyListener = new FocusHelper.PagedFolderKeyEventListener(folder);
        mPageIndicator = (PageIndicatorCircle) folder.findViewById(R.id.folder_page_indicator);
        mPageIndicator.setpagedView(this);//lijun add
    }

    /**
     * Sets up the grid size such that {@param count} items can fit in the grid.
     * The grid size is calculated such that countY <= countX and countX = ceil(sqrt(count)) while
     * maintaining the restrictions of {@link #mMaxCountX} &amp; {@link #mMaxCountY}.
     */
    private void setupContentDimensions(int count) {
        mAllocatedContentSize = count;
        boolean done;
    /*    if (count >= mMaxItemsPerPage) {
            mGridCountX = mMaxCountX;
            mGridCountY = mMaxCountY;
            done = true;
        } else {
            //M:liuzuo change folder size begin
            mGridCountX = mMaxCountX;
            done=false;
            //M:liuzuo change folder size end
        }

        while (!done) {
            int oldCountX = mGridCountX;
            int oldCountY = mGridCountY;
            if (mGridCountX * mGridCountY < count) {
                // Current grid is too small, expand it
                if ((mGridCountX <= mGridCountY || mGridCountY == mMaxCountY) && mGridCountX < mMaxCountX) {
                    mGridCountX++;
                } else if (mGridCountY < mMaxCountY) {
                    mGridCountY++;
                }
                if (mGridCountY == 0) mGridCountY++;
            } else if ((mGridCountY - 1) * mGridCountX >= count *//*&& mGridCountY >= mGridCountX*//*) {
                mGridCountY = Math.max(0, mGridCountY - 1);
            } else if ((mGridCountX - 1) * mGridCountY >= count) {
                mGridCountX = Math.max(0, mGridCountX - 1);
            }

            //done = mGridCountX == oldCountX && mGridCountY == oldCountY;
        }*/
    //M:liuzuo change folder size begin
    mGridCountX = mMaxCountX;
    mGridCountY = mMaxCountY;
    //M:liuzuo change folder size end
        // Update grid size
        for (int i = getPageCount() - 1; i >= 0; i--) {
            getPageAt(i).setGridSize(mGridCountX, mGridCountY);
        }
    }

    /**
     * Binds items to the layout.
     * @return list of items that could not be bound, probably because we hit the max size limit.
     */
    public ArrayList<ShortcutInfo> bindItems(ArrayList<ShortcutInfo> items) {
        ArrayList<View> icons = new ArrayList<View>();
        ArrayList<ShortcutInfo> extra = new ArrayList<ShortcutInfo>();

        for (ShortcutInfo item : items) {
            if (!ALLOW_FOLDER_SCROLL && icons.size() >= mMaxItemsPerPage) {
                extra.add(item);
            } else {
                icons.add(createNewView(item));
            }
        }
        arrangeChildren(icons, icons.size(), false);
        return extra;
    }

    /**
     * Create space for a new item at the end, and returns the rank for that item.
     * Also sets the current page to the last page.
     */
    public int allocateRankForNewItem(ShortcutInfo info) {
        int rank = getItemCount();
        ArrayList<View> views = new ArrayList<View>(mFolder.getItemsInReadingOrder());
        views.add(rank, null);
        arrangeChildren(views, views.size(), false);
        setCurrentPage(rank / mMaxItemsPerPage);
        return rank;
    }
//liuzuo begin
public int allocateRankForNewItem(ShortcutInfo info,boolean isSetpage) {
    int rank = getItemCount();
    ArrayList<View> views = new ArrayList<View>(mFolder.getItemsInReadingOrder());
    views.add(rank, null);
    arrangeChildren(views, views.size(), false);
    if(isSetpage)
    setCurrentPage(rank / mMaxItemsPerPage);
    return rank;
}
//liuzuo end
    public View createAndAddViewForRank(ShortcutInfo item, int rank) {
        View icon = createNewView(item);
        addViewForRank(icon, item, rank);
        return icon;
    }

    /**
     * Adds the {@param view} to the layout based on {@param rank} and updated the position
     * related attributes. It assumes that {@param item} is already attached to the view.
     */
    public void addViewForRank(View view, ShortcutInfo item, int rank) {
        int pagePos = rank % mMaxItemsPerPage;
        int pageNo = rank / mMaxItemsPerPage;

        item.rank = rank;
        item.cellX = pagePos % mGridCountX;
        item.cellY = pagePos / mGridCountX;

        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view.getLayoutParams();
        lp.cellX = item.cellX;
        lp.cellY = item.cellY;
        getPageAt(pageNo).addViewToCellLayout(
                view, -1, mFolder.mLauncher.getViewIdForItem(item), lp, true);
    }

    @SuppressLint("InflateParams")
    public View createNewView(ShortcutInfo item) {
        //M:liuzuo Dynamic icon begin
        DeviceProfile grid = ((Launcher) getContext()).getDeviceProfile();
        ShortcutFactory instance = ShortcutFactory.getInstance();
        BubbleTextView textView=instance.createShortcut(mInflater,null,item);
//        final BubbleTextView textView = (BubbleTextView) mInflater.inflate(
//                R.layout.folder_application, null, false);
        textView.setCompoundDrawablePadding(grid.iconDrawablePaddingPx);
        //M:liuzuo Dynamic icon end

        //lijun add start for unread
        mFolder.mFolderIcon.updateFolderUnreadNum(item.intent.getComponent(), item.unreadNum,item.user.getUser());
        //lijun add end
        textView.applyFromShortcutInfo(item, mIconCache);
        textView.setOnClickListener(mFolder);
        textView.setOnLongClickListener(mFolder);
        textView.setOnFocusChangeListener(mFocusIndicatorView);
        textView.setOnKeyListener(mKeyListener);
        textView.setLayoutParams(new CellLayout.LayoutParams(
                item.cellX, item.cellY, item.spanX, item.spanY));
        return textView;
    }

    @Override
    public CellLayout getPageAt(int index) {
        return (CellLayout) getChildAt(index);
    }

    public void removeCellLayoutView(View view) {
        for (int i = getChildCount() - 1; i >= 0; i --) {
            getPageAt(i).removeView(view);
        }
    }

    public CellLayout getCurrentCellLayout() {
        return getPageAt(getNextPage());
    }

    private CellLayout createAndAddNewPage() {
        DeviceProfile grid = ((Launcher) getContext()).getDeviceProfile();
        CellLayout page = new CellLayout(getContext());
        //M:liuzuo begin
        //page.setCellDimensions(grid.folderCellWidthPx, grid.folderCellHeightPx);
        /*int folderCellWidthPx=(grid.widthPx-getRight()-getPaddingLeft()-
                page.getPaddingLeft()-page.getPaddingRight()
        -(mMaxCountX-1 )* mWidthGap)/mMaxCountX;*/
        page.setCellDimensions(grid.folderCellWidthPx, grid.folderCellHeightPx,mWidthGap,mWidthGap);
    //    page.setPadding(getPaddingLeft(),getPaddingTop(),getPaddingRight(),0);
        //M:liuzuo end
        page.getShortcutsAndWidgets().setMotionEventSplittingEnabled(false);
        page.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        page.setClipChildren(false);
        page.setInvertIfRtl(true);
        page.setGridSize(mGridCountX, mGridCountY);

        addView(page, -1, generateDefaultLayoutParams());
        return page;
    }

    @Override
    protected int getChildGap() {
        return getPaddingLeft() + getPaddingRight();
    }

    public void setFixedSize(int width, int height) {
        width -= (getPaddingLeft() + getPaddingRight());
        height -= (getPaddingTop() + getPaddingBottom());
        for (int i = getChildCount() - 1; i >= 0; i --) {
            ((CellLayout) getChildAt(i)).setFixedSize(width, height);
        }
    }

    public void removeItem(View v) {
        for (int i = getChildCount() - 1; i >= 0; i --) {
            getPageAt(i).removeView(v);
        }
    }

    /**
     * Updates position and rank of all the children in the view.
     * It essentially removes all views from all the pages and then adds them again in appropriate
     * page.
     *
     * @param list the ordered list of children.
     * @param itemCount if greater than the total children count, empty spaces are left
     * at the end, otherwise it is ignored.
     *
     */
    public void arrangeChildren(ArrayList<View> list, int itemCount) {
        arrangeChildren(list, itemCount, true);
    }

    @SuppressLint("RtlHardcoded")
    private void arrangeChildren(ArrayList<View> list, int itemCount, boolean saveChanges) {
        ArrayList<CellLayout> pages = new ArrayList<CellLayout>();
        for (int i = 0; i < getChildCount(); i++) {
            CellLayout page = (CellLayout) getChildAt(i);
            page.removeAllViews();
            pages.add(page);
        }
        setupContentDimensions(itemCount);

        Iterator<CellLayout> pageItr = pages.iterator();
        CellLayout currentPage = null;

        int position = 0;
        int newX, newY, rank;

        rank = 0;
        for (int i = 0; i < itemCount; i++) {
            View v = list.size() > i ? list.get(i) : null;
            if (currentPage == null || position >= mMaxItemsPerPage) {
                // Next page
                if (pageItr.hasNext()) {
                    currentPage = pageItr.next();
                } else {
                    currentPage = createAndAddNewPage();
                }
                position = 0;
            }

            if (v != null) {
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v.getLayoutParams();
                newX = position % mGridCountX;
                newY = position / mGridCountX;
                ItemInfo info = (ItemInfo) v.getTag();
                if (info.cellX != newX || info.cellY != newY || info.rank != rank) {
                    info.cellX = newX;
                    info.cellY = newY;
                    info.rank = rank;
                    if (saveChanges) {
                        LauncherModel.addOrMoveItemInDatabase(getContext(), info,
                                mFolder.mInfo.id, 0, info.cellX, info.cellY);
                    }
                }
                lp.cellX = info.cellX;
                lp.cellY = info.cellY;
                currentPage.addViewToCellLayout(
                        v, -1, mFolder.mLauncher.getViewIdForItem(info), lp, true);

                if (rank < FolderIcon.NUM_ITEMS_IN_PREVIEW && v instanceof BubbleTextView) {
                    ((BubbleTextView) v).verifyHighRes();
                }
            }

            rank ++;
            position++;
        }

        // Remove extra views.
        boolean removed = false;
        while (pageItr.hasNext()) {
            removeView(pageItr.next());
            removed = true;
        }
        if (removed) {
            setCurrentPage(0);
        }

        //setEnableOverscroll(getPageCount() > 1);

        // Update footer
        mPageIndicator.setVisibility(getPageCount() > 1 ? VISIBLE : GONE);
        // Set the gravity as LEFT or RIGHT instead of START, as START depends on the actual text.
        //M:liuzuo change the indicator  begin
       /* mFolder.mFolderName.setGravity(getPageCount() > 1 ?
                (mIsRtl ? Gravity.RIGHT : Gravity.LEFT) : Gravity.CENTER_HORIZONTAL);*/
        //M:liuzuo change the indicator  end
    }

    public int getDesiredWidth() {
        return getPageCount() > 0 ?
                (getPageAt(0).getDesiredWidth() + getPaddingLeft() + getPaddingRight()) : 0;
    }

    public int getDesiredHeight()  {
        return  getPageCount() > 0 ?
                (getPageAt(0).getDesiredHeight() + getPaddingTop() + getPaddingBottom()) : 0;
    }

    public int getItemCount() {
        int lastPageIndex = getChildCount() - 1;
        if (lastPageIndex < 0) {
            // If there are no pages, nothing has yet been added to the folder.
            return 0;
        }
        return getPageAt(lastPageIndex).getShortcutsAndWidgets().getChildCount()
                + lastPageIndex * mMaxItemsPerPage;
    }

    /**
     * @return the rank of the cell nearest to the provided pixel position.
     */
    public int findNearestArea(int pixelX, int pixelY) {
        int pageIndex = getNextPage();
        CellLayout page = getPageAt(pageIndex);
        page.findNearestArea(pixelX, pixelY, 1, 1, sTempPosArray);
        if (mFolder.isLayoutRtl()) {
            sTempPosArray[0] = page.getCountX() - sTempPosArray[0] - 1;
        }
        return Math.min(mAllocatedContentSize - 1,
                pageIndex * mMaxItemsPerPage + sTempPosArray[1] * mGridCountX + sTempPosArray[0]);
    }

    @Override
    protected PageMarkerResources getPageIndicatorMarker(int pageIndex) {
        //int color =LauncherAppState.getInstance().getWindowGlobalVaule().getTextExtraColor();
        boolean isBlackText = LauncherAppState.getInstance().getWindowGlobalVaule().isBlackText(false);
        if(isBlackText){
            return new PageMarkerResources(R.drawable.ic_pageindicator_current_black,
                    R.drawable.ic_pageindicator_default_black);
        }else{
            return new PageMarkerResources(R.drawable.ic_pageindicator_current_white,
                    R.drawable.ic_pageindicator_default_white);
        }
    }

    public boolean isFull() {
        return !ALLOW_FOLDER_SCROLL && getItemCount() >= mMaxItemsPerPage;
    }

    public View getLastItem() {
        if (getChildCount() < 1) {
            return null;
        }
        ShortcutAndWidgetContainer lastContainer = getCurrentCellLayout().getShortcutsAndWidgets();
        int lastRank = lastContainer.getChildCount() - 1;
        if (mGridCountX > 0) {
            return lastContainer.getChildAt(lastRank % mGridCountX, lastRank / mGridCountX);
        } else {
            return lastContainer.getChildAt(lastRank);
        }
    }

    /**
     * Iterates over all its items in a reading order.
     * @return the view for which the operator returned true.
     */
    public View iterateOverItems(ItemOperator op) {
        for (int k = 0 ; k < getChildCount(); k++) {
            CellLayout page = getPageAt(k);
            for (int j = 0; j < page.getCountY(); j++) {
                for (int i = 0; i < page.getCountX(); i++) {
                    View v = page.getChildAt(i, j);
                    if ((v != null) && op.evaluate((ItemInfo) v.getTag(), v, this)) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    public String getAccessibilityDescription() {
        return String.format(getContext().getString(R.string.folder_opened),
                mGridCountX, mGridCountY);
    }

    /**
     * Sets the focus on the first visible child.
     */
    public void setFocusOnFirstChild() {
        View firstChild = getCurrentCellLayout().getChildAt(0, 0);
        if (firstChild != null) {
            firstChild.requestFocus();
        }
    }

    @Override
    protected void notifyPageSwitchListener() {
        super.notifyPageSwitchListener();
        if (mFolder != null) {
            mFolder.updateTextViewFocus();
        }
    }

    /**
     * Scrolls the current view by a fraction
     */
    public void showScrollHint(int direction) {
        float fraction = (direction == DragController.SCROLL_LEFT) ^ mIsRtl
                ? -SCROLL_HINT_FRACTION : SCROLL_HINT_FRACTION;
        int hint = (int) (fraction * getWidth());
        int scroll = getScrollForPage(getNextPage()) + hint;
        int delta = scroll - getScrollX();
        if (delta != 0) {
            mScroller.setInterpolator(new DecelerateInterpolator());
            mScroller.startScroll(getScrollX(), 0, delta, 0, Folder.SCROLL_HINT_DURATION);
            invalidate();
        }
    }

    public void clearScrollHint() {
        if (getScrollX() != getScrollForPage(getNextPage())) {
            snapToPage(getNextPage());
        }
    }

    /**
     * Finish animation all the views which are animating across pages
     */
    public void completePendingPageChanges() {
        if (!mPendingAnimations.isEmpty()) {
            HashMap<View, Runnable> pendingViews = new HashMap<>(mPendingAnimations);
            for (Map.Entry<View, Runnable> e : pendingViews.entrySet()) {
                e.getKey().animate().cancel();
                e.getValue().run();
            }
        }
    }

    public boolean rankOnCurrentPage(int rank) {
        int p = rank / mMaxItemsPerPage;
        return p == getNextPage();
    }

    @Override
    protected void onPageBeginMoving() {
        super.onPageBeginMoving();
        getVisiblePages(sTempPosArray);
        for (int i = sTempPosArray[0]; i <= sTempPosArray[1]; i++) {
            verifyVisibleHighResIcons(i);
        }
    }

    /**
     * Ensures that all the icons on the given page are of high-res
     */
    public void verifyVisibleHighResIcons(int pageNo) {
        CellLayout page = getPageAt(pageNo);
        if (page != null) {
            ShortcutAndWidgetContainer parent = page.getShortcutsAndWidgets();
            for (int i = parent.getChildCount() - 1; i >= 0; i--) {
                ((BubbleTextView) parent.getChildAt(i)).verifyHighRes();
            }
        }
    }

    public int getAllocatedContentSize() {
        return mAllocatedContentSize;
    }

    /**
     * Reorders the items such that the {@param empty} spot moves to {@param target}
     */
    public void realTimeReorder(int empty, int target) {
        completePendingPageChanges();
        int delay = 0;
        float delayAmount = START_VIEW_REORDER_DELAY;

        // Animation only happens on the current page.
        int pageToAnimate = getNextPage();

        int pageT = target / mMaxItemsPerPage;
        int pagePosT = target % mMaxItemsPerPage;

        if (pageT != pageToAnimate) {
            Log.e(TAG, "Cannot animate when the target cell is invisible");
        }
        int pagePosE = empty % mMaxItemsPerPage;
        int pageE = empty / mMaxItemsPerPage;

        int startPos, endPos;
        int moveStart, moveEnd;
        int direction;

        if (target == empty) {
            // No animation
            return;
        } else if (target > empty) {
            // Items will move backwards to make room for the empty cell.
            direction = 1;

            // If empty cell is in a different page, move them instantly.
            if (pageE < pageToAnimate) {
                moveStart = empty;
                // Instantly move the first item in the current page.
                moveEnd = pageToAnimate * mMaxItemsPerPage;
                // Animate the 2nd item in the current page, as the first item was already moved to
                // the last page.
                startPos = 0;
            } else {
                moveStart = moveEnd = -1;
                startPos = pagePosE;
            }

            endPos = pagePosT;
        } else {
            // The items will move forward.
            direction = -1;

            if (pageE > pageToAnimate) {
                // Move the items immediately.
                moveStart = empty;
                // Instantly move the last item in the current page.
                moveEnd = (pageToAnimate + 1) * mMaxItemsPerPage - 1;

                // Animations start with the second last item in the page
                startPos = mMaxItemsPerPage - 1;
            } else {
                moveStart = moveEnd = -1;
                startPos = pagePosE;
            }

            endPos = pagePosT;
        }

        // Instant moving views.
        while (moveStart != moveEnd) {
            int rankToMove = moveStart + direction;
            int p = rankToMove / mMaxItemsPerPage;
            int pagePos = rankToMove % mMaxItemsPerPage;
            int x = pagePos % mGridCountX;
            int y = pagePos / mGridCountX;

            final CellLayout page = getPageAt(p);
            final View v = page.getChildAt(x, y);
            if (v != null) {
                if (pageToAnimate != p) {
                    page.removeView(v);
                    addViewForRank(v, (ShortcutInfo) v.getTag(), moveStart);
                } else {
                    // Do a fake animation before removing it.
                    final int newRank = moveStart;
                    final float oldTranslateX = v.getTranslationX();

                    Runnable endAction = new Runnable() {

                        @Override
                        public void run() {
                            mPendingAnimations.remove(v);
                            v.setTranslationX(oldTranslateX);
                            ((CellLayout) v.getParent().getParent()).removeView(v);
                            addViewForRank(v, (ShortcutInfo) v.getTag(), newRank);
                        }
                    };
                    v.animate()
                        .translationXBy((direction > 0 ^ mIsRtl) ? -v.getWidth() : v.getWidth())
                        .setDuration(REORDER_ANIMATION_DURATION)
                        .setStartDelay(0)
                        .withEndAction(endAction);
                    mPendingAnimations.put(v, endAction);
                }
            }
            moveStart = rankToMove;
        }

        if ((endPos - startPos) * direction <= 0) {
            // No animation
            return;
        }

        CellLayout page = getPageAt(pageToAnimate);
        for (int i = startPos; i != endPos; i += direction) {
            int nextPos = i + direction;
            View v = page.getChildAt(nextPos % mGridCountX, nextPos / mGridCountX);
            if (v != null) {
                ((ItemInfo) v.getTag()).rank -= direction;
            }
            if (page.animateChildToPosition(v, i % mGridCountX, i / mGridCountX,
                    REORDER_ANIMATION_DURATION, delay, true, true)) {
                delay += delayAmount;
                delayAmount *= VIEW_REORDER_DELAY_FACTOR;
            }
        }
    }

    public void setMarkerScale(float scale) {
        int count  = mPageIndicator.getChildCount();
        for (int i = 0; i < count; i++) {
            View marker = mPageIndicator.getChildAt(i);
            marker.animate().cancel();
            marker.setScaleX(scale);
            marker.setScaleY(scale);
        }
    }

    public void animateMarkers() {
        int count  = mPageIndicator.getChildCount();
        Interpolator interpolator = new OvershootInterpolator(PAGE_INDICATOR_OVERSHOOT_TENSION);
        for (int i = 0; i < count; i++) {
            mPageIndicator.getChildAt(i).animate().scaleX(1).scaleY(1)
                .setInterpolator(interpolator)
                .setDuration(PAGE_INDICATOR_ANIMATION_DURATION)
                .setStartDelay(PAGE_INDICATOR_ANIMATION_STAGGERED_DELAY * i
                        + PAGE_INDICATOR_ANIMATION_START_DELAY);
        }
    }

    public int itemsPerPage() {
        return mMaxItemsPerPage;
    }

    @Override
    protected void getEdgeVerticalPostion(int[] pos) {
        pos[0] = 0;
        pos[1] = getViewportHeight();
    }
  //M:liuzuo add addIcon  begin
  private void getFolderAddTextView() {

      if (mFolderAddIcon == null) {
          mFolderAddIcon = (TextView) mInflater.inflate(R.layout.app_icon, this, false);
          mFolderAddIcon.setTextColor(LauncherAppState.getInstance().getWindowGlobalVaule().getTextExtraColor());
          //mFolderAddIcon = (TextView)ShortcutFactory.getInstance().createIcon(mInflater,R.layout.app_icon, this);
          Drawable d = getResources().getDrawable(R.drawable.folder_add_icon);
          Bitmap v = Utilities.createIconBitmap(d, getContext());
          mFolderAddIcon.setCompoundDrawablesWithIntrinsicBounds(null,
                  new FastBitmapDrawable(v), null, null);
          mFolderAddIcon.setText(R.string.folder_importmode_addicon);
          FolderAddInfo folderCell = new FolderAddInfo(-1, -1);
          DeviceProfile grid = ((Launcher) getContext()).getDeviceProfile();
          mFolderAddIcon.setCompoundDrawablePadding(grid.iconDrawablePaddingPx);
          mFolderAddIcon.setTag(folderCell);
          mFolderAddIcon.setOnClickListener(clickListener);
          this.setOnClickListener(clickListener);
          mFolderAddIcon.setLayoutParams(new CellLayout.LayoutParams(
                  folderCell.cellX, folderCell.cellY, folderCell.spanX, folderCell.spanY));
      }

  }
    public void setFolderClickListener(View.OnClickListener listener){
        clickListener = listener;
    }

    public void attachFolderAddView(boolean animated) {
        if (mFolderAddIcon == null) {
            getFolderAddTextView();
        }
        if(!mFolder.mLauncher.getImportMode()&&!(mFolder.mLauncher.getWorkspace().getState()==Workspace.State.OVERVIEW) )
           showAddView(animated);
    }

    private void showAddView(boolean animated) {
        if(mFolderAddIcon != null){
            FolderAddInfo info = (FolderAddInfo) mFolderAddIcon.getTag();
            onlyDetachAddView(false);
            if(animated) {
                AnimatorSet animatorSet = LauncherAnimUtils.createAnimatorSet();
                if(mIconAnima!=null&&mIconAnima.isRunning()) {
                    mIconAnima.cancel();
                }

                mIconAnima = animatorSet;
                createAddIconAnimation(mFolderAddIcon, mIconAnima);
                mFolderAddIcon.setVisibility(INVISIBLE);
                mIconAnima.start();
            }else {
                mFolderAddIcon.setAlpha(1.0f);
            }
            addViewForRank(mFolderAddIcon,info,allocateRankForNewItem());
            mFolder.setFolderBackground(false);
            // mFolder.centerAboutIcon();
        }
    }


    public void deleteCellLayout(boolean removelayout){
        if(mFolderAddIcon != null && mFolderAddIcon.getParent() != null) {
            CellLayout oldLayout = (CellLayout) mFolderAddIcon.getParent().getParent();
            oldLayout.onlyRemoveView(mFolderAddIcon);
            if(removelayout) {
                removeView(oldLayout);
            } else {
                FolderAddInfo info = (FolderAddInfo) mFolderAddIcon.getTag();
                if(info != null && info.cellX == 0 && info.cellY == 0){
                    removeView(oldLayout);
                }
            }
        }
    }
    public void hideAddView(boolean animated){
        if(mFolderAddIcon != null){
            FolderAddInfo info = (FolderAddInfo) mFolderAddIcon.getTag();
            if(info != null && info.cellX == 0 && info.cellY == 0){
                deleteCellLayout(true);
            } else {
                onlyDetachAddView(animated);
            }
            mFolder.setFolderBackground(true);
            mPageIndicator.setVisibility(getPageCount() > 1 ? VISIBLE : GONE);
        }
    }
    public void onlyDetachAddView(boolean animated){
        int rank = 0;
    if(animated) {
        AnimatorSet animatorSet = LauncherAnimUtils.createAnimatorSet();
        if(mIconAnima!=null&&mIconAnima.isRunning())
            mIconAnima.cancel();
        rank = getItemCount()-1;
        mIconAnima = animatorSet;
        removeAddIconAnimation(mFolderAddIcon, mIconAnima);
        mIconAnima.start();

    }else {
        removeItem(mFolderAddIcon);
        rank =getItemCount();
    }
        setupContentDimensions(rank);

    }

    class FolderAddInfo extends ItemInfo{

        public FolderAddInfo(int x, int y) {
            cellX = x;
            cellY = y;
        }
    }

    public int allocateRankForNewItem() {
        int rank = getItemCount();
        ArrayList<View> views = new ArrayList<View>(mFolder.getItemsInReadingOrder());
        views.add(rank, null);
        arrangeAddIcon(views.size());
        return rank;
    }

    private void arrangeAddIcon(int itemCount) {
        ArrayList<CellLayout> pages = new ArrayList<CellLayout>();
        for (int i = 0; i < getChildCount(); i++) {
            CellLayout page = (CellLayout) getChildAt(i);
            pages.add(page);
        }

        Iterator<CellLayout> pageItr = pages.iterator();
        CellLayout currentPage = null;

        setupContentDimensions(itemCount);

        int position = 0;


        for (int i = 0; i < itemCount; i++) {
            if (currentPage == null || position >= mMaxItemsPerPage) {
                // Next page
                if (pageItr.hasNext()) {
                    currentPage = pageItr.next();
                } else {
                    currentPage = createAndAddNewPage();
                }
                position = 0;
            }


            position++;
            Log.d("liuzuo8","itemCount= "+itemCount);
        }

        // Remove extra views.
        boolean removed = false;
        while (pageItr.hasNext()) {
            removeView(pageItr.next());
            removed = true;
        }
        if (removed) {
            setCurrentPage(0);
        }

        //setEnableOverscroll(getPageCount() > 1);
        // Update footer
         mPageIndicator.setVisibility(getPageCount() > 1 ? VISIBLE : GONE);

    }
    public void addViewForRank(View view, ItemInfo item, int rank) {
        int pagePos = rank % mMaxItemsPerPage;
        int pageNo = rank / mMaxItemsPerPage;

        item.rank = rank;
        item.cellX = pagePos % mGridCountX;
        item.cellY = pagePos / mGridCountX;

        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view.getLayoutParams();
        lp.cellX = item.cellX;
        lp.cellY = item.cellY;
        getPageAt(pageNo).addViewToCellLayout(
                view, -1, mFolder.mLauncher.getViewIdForItem(item), lp, true);
    }
    //M:liuzuo add addIcon  end

    //M:liuzuo add the folderImportMode begin

    public void onAddInfo(ArrayList<ShortcutInfo> items) {
        Log.d("liuzuo6","onAddInfo"+items.size());
        AnimatorSet anim = LauncherAnimUtils.createAnimatorSet();
        for (int i = 0; i < items.size(); i++) {
            ShortcutInfo child =  items.get(i);
            View icon=createAndAddViewForRank(child, allocateRankForNewItem(child));
            createAddAppAnimation(icon,anim);
            mFolder.mItemsInvalidated = true;
            LauncherModel.addOrMoveItemInDatabase(
                    mFolder.mLauncher, child, mFolder.getFolderInfo().id, 0, child.cellX, child.cellY);
        }
       // anim.start();
    }



    private AnimatorSet createAddAppAnimation(View icon, AnimatorSet anim) {
        Resources resources = getResources();
        Animator iconAlpha = ObjectAnimator.ofFloat(icon, "alpha", 0f, 1f);
        iconAlpha.setDuration(resources.getInteger(R.integer.folder_import_icon_duration));
        iconAlpha.setStartDelay(resources.getInteger(R.integer.folder_import_icon_delay));
        iconAlpha.setInterpolator(new AccelerateInterpolator(1.5f));
        anim.play(iconAlpha);
        return anim;
    }


    private void createAddIconAnimation(final TextView folderAddIcon, AnimatorSet mIconAnimat) {
        Animator iconAlpha = ObjectAnimator.ofFloat(folderAddIcon, "alpha", 0f, 1f);
        iconAlpha.setDuration(getResources().getInteger(R.integer.folder_add_icon_duration));
        iconAlpha.setStartDelay(getResources().getInteger(R.integer.folder_import_icon_delay));
        iconAlpha.setInterpolator(new AccelerateInterpolator(1.5f));
        iconAlpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                folderAddIcon.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
       /* PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0 , 1.0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0 , 1.0f);
        Animator iconScale =
                LauncherAnimUtils.ofPropertyValuesHolder(folderAddIcon, scaleX, scaleY);
        iconScale.setDuration(getResources().getInteger(R.integer.folder_add_icon_duration));
        iconScale.setStartDelay(getResources().getInteger(R.integer.folder_import_icon_delay));*/
        mIconAnimat.play(iconAlpha);
//        mIconAnimat.play(iconScale);
    }

    public int getMaxCountX() {
        return mMaxCountX;
    }

    public int getMaxCountY() {
        return mMaxCountY;
    }
    public int getMaxItemsPerPage() {
        return mMaxItemsPerPage;
    }
    public TextView getFolderAddIcon() {
        if (mFolderAddIcon==null)
            getFolderAddTextView();
        return mFolderAddIcon;
    }
    private void removeAddIconAnimation(final TextView folderAddIcon, AnimatorSet mIconAnimat) {
        Animator iconAlpha = ObjectAnimator.ofFloat(folderAddIcon, "alpha", 1f, 0f);
        iconAlpha.setDuration(getResources().getInteger(R.integer.folder_add_icon_remove_duration));
        iconAlpha.setInterpolator(new AccelerateInterpolator(1.5f));
        iconAlpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mFolderAddIcon.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                removeItem(mFolderAddIcon);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                removeItem(mFolderAddIcon);
            }
        });
    /*    PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1.0f , 0);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1.0f , 0);
        Animator iconScale =
                LauncherAnimUtils.ofPropertyValuesHolder(folderAddIcon, scaleX, scaleY);
        iconScale.setDuration(getResources().getInteger(R.integer.folder_add_icon_duration));
        iconScale.setStartDelay(getResources().getInteger(R.integer.folder_import_icon_delay));
        mIconAnimat.play(iconScale);*/
        mIconAnimat.play(iconAlpha);
    }
    public void ainmationCancel(){
        if(mIconAnima!=null&&mIconAnima.isRunning())
            mIconAnima.cancel();
    }
    //M:liuzuo add the folderImportMode end
 @Override
    public void onColorChanged(int[] colors) {
        //super.onColorChanged(colors);
    }
}
