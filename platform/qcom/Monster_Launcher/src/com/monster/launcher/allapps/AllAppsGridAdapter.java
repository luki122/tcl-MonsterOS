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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.view.accessibility.AccessibilityRecordCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.net.Uri;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.monster.launcher.AppInfo;
import com.monster.launcher.BubbleTextView;
import com.monster.launcher.IChangeColors;
import com.monster.launcher.Launcher;
import com.monster.launcher.LauncherAppState;
import com.monster.launcher.Log;
import com.monster.launcher.R;
import com.monster.launcher.ShortcutFactory;
import com.monster.launcher.Utilities;
import com.monster.launcher.util.Thunk;

import java.util.HashMap;
import java.util.List;


/**
 * The grid view adapter of all the apps.
 */
public class AllAppsGridAdapter extends RecyclerView.Adapter<AllAppsGridAdapter.ViewHolder> implements IChangeColors.IItemColorChange{

    public static final String TAG = "AppsGridAdapter";
    private static final boolean DEBUG = false;

    // A section break in the grid
    public static final int SECTION_BREAK_VIEW_TYPE = 0;
    // A normal icon
    public static final int ICON_VIEW_TYPE = 1;
    // A prediction icon
    public static final int PREDICTION_ICON_VIEW_TYPE = 2;
    // The message shown when there are no filtered results
    public static final int EMPTY_SEARCH_VIEW_TYPE = 3;
    // A divider that separates the apps list and the search market button
    public static final int SEARCH_MARKET_DIVIDER_VIEW_TYPE = 4;
    // The message to continue to a market search when there are no filtered results
    public static final int SEARCH_MARKET_VIEW_TYPE = 5;

    /**
     * ViewHolder for each icon.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mContent;

        public ViewHolder(View v) {
            super(v);
            mContent = v;
        }
    }

    /**
     * A subclass of GridLayoutManager that overrides accessibility values during app search.
     */
    public class AppsGridLayoutManager extends GridLayoutManager {

        public AppsGridLayoutManager(Context context) {
            super(context, 1, GridLayoutManager.VERTICAL, false);
        }

        @Override
        public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(event);

            // Ensure that we only report the number apps for accessibility not including other
            // adapter views
            final AccessibilityRecordCompat record = AccessibilityEventCompat
                    .asRecord(event);
            record.setItemCount(mApps.getNumFilteredApps());
        }

        @Override
        public int getRowCountForAccessibility(RecyclerView.Recycler recycler,
                RecyclerView.State state) {
            if (mApps.hasNoFilteredResults()) {
                // Disregard the no-search-results text as a list item for accessibility
                return 0;
            } else {
                return super.getRowCountForAccessibility(recycler, state);
            }
        }
    }

    /**
     * Helper class to size the grid items.
     */
    public class GridSpanSizer extends GridLayoutManager.SpanSizeLookup {

        public GridSpanSizer() {
            super();
            setSpanIndexCacheEnabled(true);
        }

        @Override
        public int getSpanSize(int position) {
            switch (mApps.getAdapterItems().get(position).viewType) {
                case AllAppsGridAdapter.ICON_VIEW_TYPE:
                case AllAppsGridAdapter.PREDICTION_ICON_VIEW_TYPE:
                    return 1;
                default:
                    // Section breaks span the full width
                    return mAppsPerRow;
            }
        }
    }

    /**
     * Helper class to draw the section headers
     */
    public class GridItemDecoration extends RecyclerView.ItemDecoration {

        private static final boolean DEBUG_SECTION_MARGIN = false;
        private static final boolean FADE_OUT_SECTIONS = false;

        private HashMap<String, PointF> mCachedSectionBounds = new HashMap<>();
        private Rect mTmpBounds = new Rect();

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            if (mApps.hasFilter() || mAppsPerRow == 0) {
                return;
            }

            if (DEBUG_SECTION_MARGIN) {
                Paint p = new Paint();
                p.setColor(0x33ff0000);
                c.drawRect(mBackgroundPadding.left, 0, mBackgroundPadding.left + mSectionNamesMargin,
                        parent.getMeasuredHeight(), p);
            }

            List<AlphabeticalAppsList.AdapterItem> items = mApps.getAdapterItems();
            boolean hasDrawnPredictedAppsDivider = false;
            boolean showSectionNames = mSectionNamesMargin > 0;
            //add by xiangzx
            if(showSectionNames){
                hasDrawnPredictedAppsDivider = true;
            }
            int childCount = parent.getChildCount();
            int lastSectionTop = 0;
            int lastSectionHeight = 0;
            boolean hasFocusFirstSection = false; //add by xiangzx
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                ViewHolder holder = (ViewHolder) parent.getChildViewHolder(child);
                if (!isValidHolderAndChild(holder, child, items)) {
                    continue;
                }

                if (shouldDrawItemDivider(holder, items) && !hasDrawnPredictedAppsDivider) {
                    // Draw the divider under the predicted apps
                    int top = child.getTop() + child.getHeight() + mPredictionBarDividerOffset;
                    c.drawLine(mBackgroundPadding.left, top,
                            parent.getWidth() - mBackgroundPadding.right, top,
                            mPredictedAppsDividerPaint);
                    hasDrawnPredictedAppsDivider = true;

                }
                //else modify by xiangzx
                if (showSectionNames && shouldDrawItemSection(holder, i, items)) {
                    // At this point, we only draw sections for each section break;
                    int viewTopOffset = (2 * child.getPaddingTop());
                    int pos = holder.getPosition();
                    AlphabeticalAppsList.AdapterItem item = items.get(pos);
                    AlphabeticalAppsList.SectionInfo sectionInfo = item.sectionInfo;

                    // Draw all the sections for this index
                    String lastSectionName = item.sectionName;
                    for (int j = item.sectionAppIndex; j < sectionInfo.numApps; j++, pos++) {
                        AlphabeticalAppsList.AdapterItem nextItem = items.get(pos);
                        String sectionName = nextItem.sectionName;        //modify by xiangzx to not fix predictive section name
                        if (nextItem.sectionInfo != sectionInfo){ //|| (item.viewType == AllAppsGridAdapter.PREDICTION_ICON_VIEW_TYPE && j == mAppsPerRow)) {
                            break;
                        }
                        if (j > item.sectionAppIndex && sectionName.equals(lastSectionName)) {
                            continue;
                        }


                        // Find the section name bounds
                        PointF sectionBounds = getAndCacheSectionBounds(sectionName);

                        // Calculate where to draw the section
                        //int sectionBaseline = (int) (viewTopOffset + sectionBounds.y);
                        int sectionBaseline = (int) (sectionBounds.y + mSectionHeaderYOffset); //modify by xiangzx
                        int x = mIsRtl ?
                                parent.getWidth() - mBackgroundPadding.left - mSectionNamesMargin :
                                        mBackgroundPadding.left;
                        //x += (int) ((mSectionNamesMargin - sectionBounds.x) / 2f);
                        x += mSectionHeaderXOffset; //modify by xiangzx
                        //int y = child.getTop() + sectionBaseline;
                        int y = child.getTop(); //modify by xiangzx
                        // Determine whether this is the last row with apps in that section, if
                        // so, then fix the section to the row allowing it to scroll past the
                        // baseline, otherwise, bound it to the baseline so it's in the viewport
                        int appIndexInSection = items.get(pos).sectionAppIndex;
                        int nextRowPos = Math.min(items.size() - 1,
                                pos + mAppsPerRow - (appIndexInSection % mAppsPerRow));
                        AlphabeticalAppsList.AdapterItem nextRowItem = items.get(nextRowPos);
                        boolean fixedToRow = sectionName.equals(nextRowItem.sectionName);
                        //modify by xiangzx
                        int iconTextSize = mLauncher.getDeviceProfile().allAppsIconTextSizePx;
                        iconTextSize = iconTextSize + mContainerLeftIndexOffset;
                        //boolean preFixedToRow = !(item.viewType == PREDICTION_ICON_VIEW_TYPE);
                        //if (preFixedToRow) {
                            y = Math.max(sectionBaseline, y);
                            if(!fixedToRow) {
                                if (child.getBottom() <= sectionBaseline + iconTextSize) {
                                    y = child.getBottom() - iconTextSize;
                                }
                            }
                        //}

                        // In addition, if it overlaps with the last section that was drawn, then
                        // offset it so that it does not overlap
                        if (lastSectionHeight > 0 && y <= (lastSectionTop + lastSectionHeight)) {
                            y += lastSectionTop - y + lastSectionHeight;
                        }

                        // Draw the section header
                        if (FADE_OUT_SECTIONS) {
                            int alpha = 255;
                            if (fixedToRow) {
                                alpha = Math.min(255,
                                        (int) (255 * (Math.max(0, y) / (float) sectionBaseline)));
                            }
                            mSectionTextPaint.setAlpha(alpha);
                        }

//                        if(mLauncher.getResources().getString(R.string.allapps_recent_apps).equals(sectionName)){
//                            y += (child.getPaddingTop()/2);
//                            float defaultTextSize = mSectionTextPaint.getTextSize();
//                            mSectionTextPaint.setTextSize(mLauncher.getResources().getDimensionPixelSize(
//                                    R.dimen.all_apps_grid_section_recent_text_size));
//                            c.drawText(sectionName, x, y, mSectionTextPaint);
//                            mSectionTextPaint.setTextSize(defaultTextSize);
//                        }else {
                            if("☆".equals(sectionName)){
                                x -= mPredictionHeaderXOffset;
                            }
                            c.drawText(sectionName, x, y, mSectionTextPaint);
//                        }
                        if(!hasFocusFirstSection){
                            mLauncher.getAppsView().mAppsRecyclerView.focusCurrentIndex(sectionName);
                            hasFocusFirstSection = true;
                        }
                        lastSectionTop = y;
                        lastSectionHeight = (int) (sectionBounds.y + mSectionHeaderOffset);
                        lastSectionName = sectionName;
                    }
                    i += (sectionInfo.numApps - item.sectionAppIndex);
                }
            }
        }



        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                RecyclerView.State state) {
            //add by xiangzx
            if(mSectionNamesMargin > 0 && !mApps.hasFilter()) {
                int pos = parent.getChildAdapterPosition(view);
                AlphabeticalAppsList.AdapterItem item = mApps.getAdapterItems().get(pos);
                if (item.sectionAppIndex < mAppsPerRow) {
                    outRect.set(0, mApps.SECTION_OFFSET, 0, 0);
                }
            }
            if(mSectionNamesMargin > 0 && mApps.hasFilter() && mApps.getNumFilteredApps() > 0) {
                int pos = parent.getChildAdapterPosition(view);
                AlphabeticalAppsList.AdapterItem item = mApps.getAdapterItems().get(pos);
                if (pos < mAppsPerRow && item.viewType != SEARCH_MARKET_VIEW_TYPE) {
                    outRect.set(0, mApps.SECTION_OFFSET*2, 0, 0);
                }
            }
        }

        /**
         * Given a section name, return the bounds of the given section name.
         */
        private PointF getAndCacheSectionBounds(String sectionName) {
            PointF bounds = mCachedSectionBounds.get(sectionName);
            if (bounds == null) {
                mSectionTextPaint.getTextBounds(sectionName, 0, sectionName.length(), mTmpBounds);
                bounds = new PointF(mSectionTextPaint.measureText(sectionName), mTmpBounds.height());
                mCachedSectionBounds.put(sectionName, bounds);
            }
            return bounds;
        }

        /**
         * Returns whether we consider this a valid view holder for us to draw a divider or section for.
         */
        private boolean isValidHolderAndChild(ViewHolder holder, View child,
                List<AlphabeticalAppsList.AdapterItem> items) {
            // Ensure item is not already removed
            GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams)
                    child.getLayoutParams();
            if (lp.isItemRemoved()) {
                return false;
            }
            // Ensure we have a valid holder
            if (holder == null) {
                return false;
            }
            // Ensure we have a holder position
            int pos = holder.getPosition();
            if (pos < 0 || pos >= items.size()) {
                return false;
            }
            return true;
        }

        /**
         * Returns whether to draw the divider for a given child.
         */
        private boolean shouldDrawItemDivider(ViewHolder holder,
                List<AlphabeticalAppsList.AdapterItem> items) {
            int pos = holder.getPosition();
            return items.get(pos).viewType == AllAppsGridAdapter.PREDICTION_ICON_VIEW_TYPE && (items.get(pos).sectionAppIndex + 1) == mApps.mPredictedApps.size();
        }

        /**
         * Returns whether to draw the section for the given child.
         */
        private boolean shouldDrawItemSection(ViewHolder holder, int childIndex,
                List<AlphabeticalAppsList.AdapterItem> items) {
            int pos = holder.getPosition();
            AlphabeticalAppsList.AdapterItem item = items.get(pos);

            // Ensure it's an icon                                      //modify by xiangzx to draw prediction header
            if (!(item.viewType == AllAppsGridAdapter.ICON_VIEW_TYPE || item.viewType == AllAppsGridAdapter.PREDICTION_ICON_VIEW_TYPE)) {
                return false;
            }
            // Draw the section header for the first item in each section
            return (childIndex == 0) ||
                    (items.get(pos - 1).viewType == AllAppsGridAdapter.SECTION_BREAK_VIEW_TYPE);
        }
    }

    private Launcher mLauncher;
    private LayoutInflater mLayoutInflater;
    @Thunk
    AlphabeticalAppsList mApps;
    private GridLayoutManager mGridLayoutMgr;
    private GridSpanSizer mGridSizer;
    private GridItemDecoration mItemDecoration;
    private View.OnTouchListener mTouchListener;
    private View.OnClickListener mIconClickListener;
    private View.OnLongClickListener mIconLongClickListener;
    @Thunk final Rect mBackgroundPadding = new Rect();
    @Thunk int mPredictionBarDividerOffset;
    @Thunk int mAppsPerRow;
    @Thunk boolean mIsRtl;

    // The text to show when there are no search results and no market search handler.
    private String mEmptySearchMessage;
    // The name of the market app which handles searches, to be used in the format str
    // below when updating the search-market view.  Only needs to be loaded once.
    private String mMarketAppName;
    // The text to show when there is a market app which can handle a specific query, updated
    // each time the search query changes.
    private String mMarketSearchMessage;
    // The intent to send off to the market app, updated each time the search query changes.
    private Intent mMarketSearchIntent;
    // The last query that the user entered into the search field
    private String mLastSearchQuery;

    // Section drawing
    @Thunk int mSectionNamesMargin;
    @Thunk int mSectionHeaderOffset;
    @Thunk int mSectionHeaderXOffset;
    @Thunk int mSectionHeaderYOffset;
    @Thunk int mPredictionHeaderXOffset;
    @Thunk Paint mSectionTextPaint;
    @Thunk Paint mPredictedAppsDividerPaint;

    private int mContainerLeftIndexOffset;

    public AllAppsGridAdapter(Launcher launcher, AlphabeticalAppsList apps,
            View.OnTouchListener touchListener, View.OnClickListener iconClickListener,
            View.OnLongClickListener iconLongClickListener) {
        Resources res = launcher.getResources();
        mLauncher = launcher;
        mApps = apps;
        mEmptySearchMessage = res.getString(R.string.all_apps_loading_message);
        mGridSizer = new GridSpanSizer();
        mGridLayoutMgr = new AppsGridLayoutManager(launcher);
        mGridLayoutMgr.setSpanSizeLookup(mGridSizer);
        mItemDecoration = new GridItemDecoration();
        mLayoutInflater = LayoutInflater.from(launcher);
        mTouchListener = touchListener;
        mIconClickListener = iconClickListener;
        mIconLongClickListener = iconLongClickListener;
        mSectionNamesMargin = res.getDimensionPixelSize(R.dimen.all_apps_grid_view_start_margin);
        mSectionHeaderOffset = res.getDimensionPixelSize(R.dimen.all_apps_grid_section_y_offset);
        mSectionHeaderXOffset = res.getDimensionPixelSize(R.dimen.all_apps_headername_x_offset);
        mSectionHeaderYOffset = res.getDimensionPixelSize(R.dimen.all_apps_headername_y_offset);
        mPredictionHeaderXOffset = res.getDimensionPixelSize(R.dimen.all_apps_prediction_headername_x_offset);
        mContainerLeftIndexOffset = res.getDimensionPixelSize(R.dimen.container_left_index_offset);

        mSectionTextPaint = new Paint();
        mSectionTextPaint.setTextSize(res.getDimensionPixelSize(
                R.dimen.all_apps_grid_section_text_size));
        mSectionTextPaint.setColor(res.getColor(R.color.all_apps_grid_section_text_color));
        mSectionTextPaint.setTypeface(Typeface.create("monster-normal", Typeface.NORMAL));
        mSectionTextPaint.setAntiAlias(true);

        mPredictedAppsDividerPaint = new Paint();
        mPredictedAppsDividerPaint.setStrokeWidth(Utilities.pxFromDp(1f, res.getDisplayMetrics()));
        mPredictedAppsDividerPaint.setColor(0x1E000000);
        mPredictedAppsDividerPaint.setAntiAlias(true);
        mPredictionBarDividerOffset =
                (-res.getDimensionPixelSize(R.dimen.all_apps_prediction_icon_bottom_padding) +
                        res.getDimensionPixelSize(R.dimen.all_apps_icon_top_bottom_padding)) / 2;

        // Resolve the market app handling additional searches
      /*PackageManager pm = launcher.getPackageManager();
        ResolveInfo marketInfo = pm.resolveActivity(createMarketSearchIntent(""),
                PackageManager.MATCH_DEFAULT_ONLY);
        if (marketInfo != null) {
            mMarketAppName = marketInfo.loadLabel(pm).toString();
        }*/
    }

    //add by xiangzx
    public void setMarketApp(){
        PackageManager pm = mLauncher.getPackageManager();
        List<ResolveInfo> resolveList = pm.queryIntentActivities(createMarketSearchIntent(""), PackageManager.MATCH_DEFAULT_ONLY);
        if(!resolveList.isEmpty()){
            if(resolveList.size() == 1){
                mMarketAppName = resolveList.get(0).loadLabel(pm).toString();
            }else{
                mMarketAppName = mLauncher.getResources().getString(R.string.allapps_market_title);
            }
        }else{
            mMarketAppName = null;
        }
    }

   public void setNameMargin(int sectionNamesMargin){
        mSectionNamesMargin = sectionNamesMargin;
    }

    /**
     * Sets the number of apps per row.
     */
    public void setNumAppsPerRow(int appsPerRow) {
        mAppsPerRow = appsPerRow;
        mGridLayoutMgr.setSpanCount(appsPerRow);
    }

    /**
     * Sets whether we are in RTL mode.
     */
    public void setRtl(boolean rtl) {
        mIsRtl = rtl;
    }

    /**
     * Sets the last search query that was made, used to show when there are no results and to also
     * seed the intent for searching the market.
     */
    public void setLastSearchQuery(String query) {
        Resources res = mLauncher.getResources();
        String formatStr = res.getString(R.string.all_apps_no_search_results);
        mLastSearchQuery = query;
        mEmptySearchMessage = String.format(formatStr, query);
        if (mMarketAppName != null) {
            mMarketSearchMessage = String.format(res.getString(R.string.all_apps_search_market_message),
                    mMarketAppName);
            mMarketSearchIntent = createMarketSearchIntent(query);
        }else{
            mMarketSearchIntent = null;
        }
    }

    /**
     * Notifies the adapter of the background padding so that it can draw things correctly in the
     * item decorator.
     */
    public void updateBackgroundPadding(Rect padding) {
        mBackgroundPadding.set(padding);
    }

    /**
     * Returns the grid layout manager.
     */
    public GridLayoutManager getLayoutManager() {
        return mGridLayoutMgr;
    }

    /**
     * Returns the item decoration for the recycler view.
     */
    public RecyclerView.ItemDecoration getItemDecoration() {
        // We don't draw any headers when we are uncomfortably dense
        return mItemDecoration;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case SECTION_BREAK_VIEW_TYPE:
                return new ViewHolder(new View(parent.getContext()));
            case ICON_VIEW_TYPE: {
                BubbleTextView icon = (BubbleTextView) mLayoutInflater.inflate(
                        R.layout.all_apps_icon, parent, false);
                icon.setOnTouchListener(mTouchListener);
                icon.setOnClickListener(mIconClickListener);
                icon.setOnLongClickListener(mIconLongClickListener);
                icon.setLongPressTimeout(ViewConfiguration.get(parent.getContext())
                        .getLongPressTimeout());
                icon.setFocusable(true);
                return new ViewHolder(icon);
            }
            case PREDICTION_ICON_VIEW_TYPE: {
                BubbleTextView icon = (BubbleTextView) mLayoutInflater.inflate(
                        R.layout.all_apps_prediction_bar_icon, parent, false);
                icon.setOnTouchListener(mTouchListener);
                icon.setOnClickListener(mIconClickListener);
                icon.setOnLongClickListener(mIconLongClickListener);
                icon.setLongPressTimeout(ViewConfiguration.get(parent.getContext())
                        .getLongPressTimeout());
                icon.setFocusable(true);
                return new ViewHolder(icon);
            }
            case EMPTY_SEARCH_VIEW_TYPE:
                return new ViewHolder(mLayoutInflater.inflate(R.layout.all_apps_empty_search,
                        parent, false));
            case SEARCH_MARKET_DIVIDER_VIEW_TYPE:
                return new ViewHolder(mLayoutInflater.inflate(R.layout.all_apps_search_market_divider,
                        parent, false));
            case SEARCH_MARKET_VIEW_TYPE:
                View searchMarketContainer = mLayoutInflater.inflate(R.layout.all_apps_search_market,
                        parent, false);
                View searchMarketView = searchMarketContainer.findViewById(R.id.search_market_text);
                searchMarketView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mLauncher.startSearchFromAllApps(v, mMarketSearchIntent, mLastSearchQuery);
                    }
                });
                return new ViewHolder(searchMarketContainer);
            default:
                throw new RuntimeException("Unexpected view type");
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ICON_VIEW_TYPE: {
                AppInfo info = mApps.getAdapterItems().get(position).appInfo;
                BubbleTextView icon = (BubbleTextView) holder.mContent;
                icon.applyFromApplicationInfo(info);
                icon.setTextColor(LauncherAppState.getInstance().getWindowGlobalVaule().getTextColor());
                break;
            }
            case PREDICTION_ICON_VIEW_TYPE: {
                AppInfo info = mApps.getAdapterItems().get(position).appInfo;
                BubbleTextView icon = (BubbleTextView) holder.mContent;
                icon.applyFromApplicationInfo(info);
                icon.setTextColor(LauncherAppState.getInstance().getWindowGlobalVaule().getTextColor());
                break;
            }
            case EMPTY_SEARCH_VIEW_TYPE:
                TextView emptyViewText = (TextView) holder.mContent;
                emptyViewText.setText(mEmptySearchMessage);
                emptyViewText.setGravity(mApps.hasNoFilteredResults() ? Gravity.CENTER :
                        Gravity.START | Gravity.CENTER_VERTICAL);
                changeTextViewColor(emptyViewText, EMPTY_SEARCH_VIEW_TYPE);
                mLauncher.getAppsView().setEmptyViewRightMargin(emptyViewText);
                break;
            case SEARCH_MARKET_VIEW_TYPE:
                if(holder.mContent instanceof LinearLayout){
                   LinearLayout searchMarketContainer = (LinearLayout)holder.mContent;
                   TextView searchView = (TextView) searchMarketContainer.getChildAt(0);
                   if (mMarketSearchIntent != null) {
                    searchView.setVisibility(View.VISIBLE);
                    searchView.setContentDescription(mMarketSearchMessage);
                    //searchView.setGravity(mApps.hasNoFilteredResults() ? Gravity.CENTER : Gravity.START | Gravity.CENTER_VERTICAL);
                    searchView.setText(mMarketSearchMessage);
                    changeTextViewColor(searchView, SEARCH_MARKET_VIEW_TYPE);
                       mLauncher.getAppsView().setEmptyViewRightMargin(searchMarketContainer);
                 } else {
                    searchView.setVisibility(View.GONE);
                 }
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mApps.getAdapterItems().size();
    }

    @Override
    public int getItemViewType(int position) {
        AlphabeticalAppsList.AdapterItem item = mApps.getAdapterItems().get(position);
        return item.viewType;
    }

    /**
     * Creates a new market search intent.
     */
    private Intent createMarketSearchIntent(String query) {
        Uri marketSearchUri = Uri.parse("market://search")
                .buildUpon()
                .appendQueryParameter("q", query)
                .build();
        Intent marketSearchIntent = new Intent(Intent.ACTION_VIEW);
        marketSearchIntent.setData(marketSearchUri);
        return marketSearchIntent;
    }

    @Override
    public void changeColors(int[] colors) {
        if(colors[0] != -1){
            mSectionTextPaint.setColor(mLauncher.getResources().getColor(R.color.all_apps_sectionname_color_black));
        }else{
            mSectionTextPaint.setColor(mLauncher.getResources().getColor(R.color.all_apps_sectionname_color_white));
        }
    }

    private void changeTextViewColor(TextView textView, int type){
        boolean isBlacktext =LauncherAppState.getInstance().getWindowGlobalVaule().isBlackText();
        switch (type){
            case EMPTY_SEARCH_VIEW_TYPE:
                 if(isBlacktext){
                     textView.setTextColor(mLauncher.getResources().getColor(R.color.all_apps_sectionname_color_black));
                 }else{
                     textView.setTextColor(mLauncher.getResources().getColor(R.color.all_apps_sectionname_color_white));
                 }
                break;

            case SEARCH_MARKET_VIEW_TYPE:
                if(isBlacktext){
                    textView.setTextColor(mLauncher.getResources().getColor(R.color.all_apps_market_color_black));
                    textView.setBackground(mLauncher.getResources().getDrawable(R.drawable.allapps_market_textview_background_black));
                }else{
                    textView.setTextColor(mLauncher.getResources().getColor(R.color.all_apps_market_color_white));
                    textView.setBackground(mLauncher.getResources().getDrawable(R.drawable.allapps_market_textview_background_white));
                }
                break;
        }
    }
}
