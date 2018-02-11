/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.monster.launcher.widget.WidgetCell;

public class DeviceProfile {

    public final InvariantDeviceProfile inv;

    // Device properties
    public final boolean isTablet;
    public final boolean isLargeTablet;
    public final boolean isPhone;
    public final boolean transposeLayoutWithOrientation;

    // Device properties in current orientation
    public final boolean isLandscape;
    public final int widthPx;
    public final int heightPx;
    public final int availableWidthPx;
    public final int availableHeightPx;

    // Overview mode
    private final int overviewModeMinIconZoneHeightPx;
    private final int overviewModeMaxIconZoneHeightPx;
    private final int overviewModeBarItemWidthPx;
    private final int overviewModeBarSpacerWidthPx;
    private final float overviewModeIconZoneRatio;

    // Workspace
    private int desiredWorkspaceLeftRightMarginPx;
    public final int edgeMarginPx;
    public final Rect defaultWidgetPadding;
    private final int pageIndicatorHeightPx;
    private final int defaultPageSpacingPx;
    private float dragViewScale;

    // Workspace icons
    public int iconSizePx;
    public int iconTextSizePx;
    public int iconDrawablePaddingPx;
    public int iconDrawablePaddingOriginalPx;

    public int cellWidthPx;
    public int cellHeightPx;

    // Folder
    public int folderBackgroundOffset;
    public int folderIconSizePx;
    public int folderCellWidthPx;
    public int folderCellHeightPx;
    private  int folderIconOffsetY;//liuzuo
    private  int folderIconOffsetYFromHotseat;//liuzuo
    private  int folderWidthGap;//liuzuo
    // Hotseat
    public int hotseatCellWidthPx;
    public int hotseatCellHeightPx;
    public int hotseatIconSizePx;
    public int hotseatBarHeightPx;//lijun change private to public

    // All apps
    public int allAppsNumCols;
    public int allAppsNumPredictiveCols;
    public int allAppsButtonVisualSize;
    public final int allAppsIconSizePx;
    public final int allAppsIconTextSizePx;
    public final float zoomInRate = 1.0f;

    // QSB
    private int searchBarSpaceWidthPx;
    private int searchBarSpaceHeightPx;

    private int searchBarSpaceHeightOffsety;//liuzuo
    //lijun add for PageIndicatorCube
    //PageIndicator
    public int pageIndicatorCubePanelWidthPx;
    public int pageIndicatorCubeCellWidthPx;
    public int pageIndicatorCubeCellHeightPx;
    public int pageIndicatorCubeCellXGap;

    //lijun add for WidgetContainerPageView
    public int widgetsContainerBarHeightPx;
    public int widgetsImageViewHeightPx;
    public int widgetsCellImageViewTopMarginPx;
    public int widgetsCellImageViewBottomMarginPx;

    public DeviceProfile(Context context, InvariantDeviceProfile inv,
            Point minSize, Point maxSize,
            int width, int height, boolean isLandscape) {

        this.inv = inv;
        this.isLandscape = isLandscape;

        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();

        // Constants from resources
        isTablet = res.getBoolean(R.bool.is_tablet);
        isLargeTablet = res.getBoolean(R.bool.is_large_tablet);
        isPhone = !isTablet && !isLargeTablet;

        // Some more constants
        transposeLayoutWithOrientation =
                res.getBoolean(R.bool.hotseat_transpose_layout_with_orientation);

        ComponentName cn = new ComponentName(context.getPackageName(),
                this.getClass().getName());
        defaultWidgetPadding = AppWidgetHostView.getDefaultPaddingForWidget(context, cn, null);
        edgeMarginPx = res.getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin);
        desiredWorkspaceLeftRightMarginPx = 2 * edgeMarginPx;
        pageIndicatorHeightPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_page_indicator_height);
        defaultPageSpacingPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_workspace_page_spacing);
        overviewModeMinIconZoneHeightPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_overview_min_icon_zone_height);
        overviewModeMaxIconZoneHeightPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_overview_max_icon_zone_height);
        overviewModeBarItemWidthPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_overview_bar_item_width);
        overviewModeBarSpacerWidthPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_overview_bar_spacer_width);
        overviewModeIconZoneRatio =
                res.getInteger(R.integer.config_dynamic_grid_overview_icon_zone_percentage) / 100f;
        iconDrawablePaddingOriginalPx =
                res.getDimensionPixelSize(R.dimen.dynamic_grid_icon_drawable_padding);
        folderWidthGap = res.getDimensionPixelSize(R.dimen.folder_background_width_gap);
        // AllApps uses the original non-scaled icon text size          //scale by xiangzx
        allAppsIconTextSizePx = (int)(Utilities.pxFromDp(inv.iconTextSize, dm) * zoomInRate);

        // AllApps uses the original non-scaled icon size
        allAppsIconSizePx = (int)(Utilities.pxFromDp(inv.iconSize, dm) * zoomInRate);

        // Determine sizes.
        widthPx = width;
        heightPx = height;
        if (isLandscape) {
            availableWidthPx = maxSize.x;
            availableHeightPx = minSize.y;
        } else {
            availableWidthPx = minSize.x;
            availableHeightPx = maxSize.x;//liuzuo  maxSize.y  >>  maxSize.x
        }

        //lijun add for pageIndicator px
        pageIndicatorCubeCellWidthPx = res.getDimensionPixelSize(R.dimen.pageindicator_cube_cell_width);
        pageIndicatorCubeCellHeightPx = res.getDimensionPixelSize(R.dimen.pageindicator_cube_cell_height);
        pageIndicatorCubeCellXGap = res.getDimensionPixelSize(R.dimen.pageindicator_cube_cell_left_right_margin);
        int maxSizeForPageIndicatorCube= res.getInteger(R.integer.config_maxNumberOfPageIndicatorsToShow_Cube);
        pageIndicatorCubePanelWidthPx = maxSizeForPageIndicatorCube *(pageIndicatorCubeCellWidthPx + pageIndicatorCubeCellXGap*2);
        //lijun add for WidgetsContainer px
        widgetsCellImageViewTopMarginPx = res.getDimensionPixelSize(R.dimen.widget_img_top_padding);
        widgetsCellImageViewBottomMarginPx = res.getDimensionPixelSize(R.dimen.widget_img_bottom_padding);
        widgetsImageViewHeightPx = (int) (cellWidthPx* WidgetCell.PREVIEW_SCALE);

        // Calculate the remaining vars
        updateAvailableDimensions(dm, res);
        computeAllAppsButtonSize(context);
    }

    /**
     * Determine the exact visual footprint of the all apps button, taking into account scaling
     * and internal padding of the drawable.
     */
    private void computeAllAppsButtonSize(Context context) {
        Resources res = context.getResources();
        float padding = res.getInteger(R.integer.config_allAppsButtonPaddingPercent) / 100f;
        allAppsButtonVisualSize = (int) (hotseatIconSizePx * (1 - padding));
    }

    private void updateAvailableDimensions(DisplayMetrics dm, Resources res) {
        // Check to see if the icons fit in the new available height.  If not, then we need to
        // shrink the icon size.
        float scale = 1f;
        int drawablePadding = iconDrawablePaddingOriginalPx;
        updateIconSize(1f, drawablePadding, res, dm);
        float usedHeight = (cellHeightPx * inv.numRows);

        // We only care about the top and bottom workspace padding, which is not affected by RTL.
        Rect workspacePadding = getWorkspacePadding(false /* isLayoutRtl */);
        int maxHeight = (availableHeightPx - workspacePadding.top - workspacePadding.bottom);
        if (usedHeight > maxHeight) {
            scale = maxHeight / usedHeight;
            drawablePadding = 0;
        }
        updateIconSize(scale, drawablePadding, res, dm);
    }
    int exttaPageIndicatorCirclePx=0;
    int exttaPageIndicatorCubePx=0;
    private void updateIconSize(float scale, int drawablePadding, Resources res,
                                DisplayMetrics dm) {
        iconSizePx = (int) (Utilities.pxFromDp(inv.iconSize, dm) * scale);
        iconTextSizePx = (int) (Utilities.pxFromSp(inv.iconTextSize, dm) * scale);
        iconDrawablePaddingPx = drawablePadding;
        hotseatIconSizePx = (int) (Utilities.pxFromDp(inv.hotseatIconSize, dm) * scale);

        // Search Bar
        searchBarSpaceWidthPx = Math.min(widthPx,
                res.getDimensionPixelSize(R.dimen.dynamic_grid_search_bar_max_width));
        searchBarSpaceHeightPx = getSearchBarTopOffset()
                + res.getDimensionPixelSize(R.dimen.dynamic_grid_search_bar_height);
        searchBarSpaceHeightOffsety =res.getDimensionPixelOffset(R.dimen.dynamic_grid_search_bar_height_offsety);
        // Calculate the actual text height
        Paint textPaint = new Paint();
        textPaint.setTextSize(iconTextSizePx);
        FontMetrics fm = textPaint.getFontMetrics();
        cellWidthPx = iconSizePx;
        cellHeightPx = iconSizePx + iconDrawablePaddingPx + (int) Math.ceil(fm.bottom - fm.top);
        final float scaleDps = res.getDimensionPixelSize(R.dimen.dragViewScale);
        dragViewScale = (iconSizePx + scaleDps) / iconSizePx;

        // Hotseat
        hotseatBarHeightPx = iconSizePx +2*edgeMarginPx;
        hotseatCellWidthPx = iconSizePx;
        hotseatCellHeightPx = iconSizePx;

        // Folder
        Rect workspacePadding = getWorkspacePadding(false);
        int cellHeight2Workspace = (availableHeightPx - workspacePadding.top - workspacePadding.bottom)/inv.numRows;
        int cellWeight2Workspace = (availableWidthPx - workspacePadding.left - workspacePadding.right-folderWidthGap)/inv.numFolderColumns;
        folderCellWidthPx = cellWeight2Workspace /*cellWidthPx + 3 * edgeMarginPx*/;
        folderCellHeightPx = (int) (cellHeight2Workspace*1.05f)  /*cellHeightPx + edgeMarginPx*/;
        folderBackgroundOffset =0;// -edgeMarginPx;
        folderIconSizePx = iconSizePx + 2 * -folderBackgroundOffset;
        exttaPageIndicatorCirclePx= ((cellHeight2Workspace-cellHeightPx)-(hotseatCellHeightPx-iconSizePx))/2;
        exttaPageIndicatorCubePx = (int)(cellHeight2Workspace*0.05f+hotseatCellHeightPx*0.05f+exttaPageIndicatorCirclePx*0.95f+pageIndicatorHeightPx*1.25f);
        folderIconOffsetY=(cellHeight2Workspace-cellHeightPx)/2;//liuzuo
        folderIconOffsetYFromHotseat= (int) (edgeMarginPx*1.5f);//liuzuo
        widgetsImageViewHeightPx = (int) (cellWidthPx* WidgetCell.PREVIEW_SCALE);
        widgetsContainerBarHeightPx = widgetsImageViewHeightPx + widgetsCellImageViewTopMarginPx +
                widgetsCellImageViewBottomMarginPx + (int) Math.ceil(fm.bottom - fm.top);
    }

    /**
     * @param recyclerViewWidth the available width of the AllAppsRecyclerView
     */
    public void updateAppsViewNumCols(Resources res, int recyclerViewWidth, int mNameMargin) {
        //modify by xiangzx
        //int appsViewLeftMarginPx =res.getDimensionPixelSize(R.dimen.all_apps_grid_view_start_margin);
        int appsViewLeftMarginPx = mNameMargin;

        int allAppsCellWidthGap =
                res.getDimensionPixelSize(R.dimen.all_apps_icon_width_gap);
        int availableAppsWidthPx = (recyclerViewWidth > 0) ? recyclerViewWidth : availableWidthPx;
        int numAppsCols = (availableAppsWidthPx - appsViewLeftMarginPx) /
                (allAppsIconSizePx + allAppsCellWidthGap);
        int numPredictiveAppCols = Math.max(inv.minAllAppsPredictionColumns, numAppsCols);
        allAppsNumCols = numAppsCols;
        allAppsNumPredictiveCols = numPredictiveAppCols;
    }

    /** Returns the search bar top offset */
    private int getSearchBarTopOffset() {
        if (isTablet && !isVerticalBarLayout()) {
            return 4 * edgeMarginPx;
        } else {
            //return 2 * edgeMarginPx;
            return 0;
        }
    }

    /** Returns the search bar bounds in the current orientation */
    public Rect getSearchBarBounds(boolean isLayoutRtl) {
        Rect bounds = new Rect();
        if (isLandscape && transposeLayoutWithOrientation) {
            if (isLayoutRtl) {
                bounds.set(availableWidthPx - searchBarSpaceHeightPx, edgeMarginPx,
                        availableWidthPx, availableHeightPx - edgeMarginPx);
            } else {
                bounds.set(0, edgeMarginPx, searchBarSpaceHeightPx,
                        availableHeightPx - edgeMarginPx);
            }
        } else {
            if (isTablet) {
                // Pad the left and right of the workspace to ensure consistent spacing
                // between all icons
                int width = getCurrentWidth();
                // XXX: If the icon size changes across orientations, we will have to take
                //      that into account here too.
                int gap = (int) ((width - 2 * edgeMarginPx -
                        (inv.numColumns * cellWidthPx)) / (2 * (inv.numColumns + 1)));
                bounds.set(edgeMarginPx + gap, getSearchBarTopOffset(),
                        availableWidthPx - (edgeMarginPx + gap),
                        searchBarSpaceHeightPx);
            } else {
                bounds.set(desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.left,
                        getSearchBarTopOffset(),
                        availableWidthPx - (desiredWorkspaceLeftRightMarginPx -
                        defaultWidgetPadding.right), searchBarSpaceHeightPx);
            }
        }
        return bounds;
    }

    /** Returns the workspace padding in the specified orientation */
    Rect getWorkspacePadding(boolean isLayoutRtl) {
        Rect searchBarBounds = getSearchBarBounds(isLayoutRtl);
        Rect padding = new Rect();
        if (isLandscape && transposeLayoutWithOrientation) {
            // Pad the left and right of the workspace with search/hotseat bar sizes
            if (isLayoutRtl) {
                padding.set(hotseatBarHeightPx, edgeMarginPx,
                        searchBarBounds.width(), edgeMarginPx);
            } else {
                padding.set(searchBarBounds.width(), edgeMarginPx,
                        hotseatBarHeightPx, edgeMarginPx);
            }
        } else {
            int indicatorHeight = (int) (pageIndicatorHeightPx*1.25);
            if (isTablet) {
                // Pad the left and right of the workspace to ensure consistent spacing
                // between all icons
                float gapScale = 1f + (dragViewScale - 1f) / 2f;
                int width = getCurrentWidth();
                int height = getCurrentHeight();
                int paddingTop = searchBarBounds.bottom;
                int paddingBottom = hotseatBarHeightPx + indicatorHeight;
                int availableWidth = Math.max(0, width - (int) ((inv.numColumns * cellWidthPx) +
                        (inv.numColumns * gapScale * cellWidthPx)));
                int availableHeight = Math.max(0, height - paddingTop - paddingBottom
                        - (int) (2 * inv.numRows * cellHeightPx));
                padding.set(availableWidth / 2, paddingTop + availableHeight / 2,
                        availableWidth / 2, paddingBottom + availableHeight / 2);
            } else {
                // Pad the top and bottom of the workspace with search/hotseat bar sizes
                padding.set(desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.left,
                        searchBarBounds.bottom-searchBarSpaceHeightOffsety,//liuzuo searchBarBounds.bottom*2/3,
                        desiredWorkspaceLeftRightMarginPx - defaultWidgetPadding.right,
                        hotseatBarHeightPx + indicatorHeight);
            }
        }
        return padding;
    }

    private int getWorkspacePageSpacing(boolean isLayoutRtl) {
        if ((isLandscape && transposeLayoutWithOrientation) || isLargeTablet) {
            // In landscape mode the page spacing is set to the default.
            return defaultPageSpacingPx;
        } else {
            // In portrait, we want the pages spaced such that there is no
            // overhang of the previous / next page into the current page viewport.
            // We assume symmetrical padding in portrait mode.
            return Math.max(defaultPageSpacingPx, 2 * getWorkspacePadding(isLayoutRtl).left);
        }
    }

    int getOverviewModeButtonBarHeight() {
        int zoneHeight = (int) (overviewModeIconZoneRatio * availableHeightPx);
        zoneHeight = Math.min(overviewModeMaxIconZoneHeightPx,
                Math.max(overviewModeMinIconZoneHeightPx, zoneHeight));
        return zoneHeight;
    }

    // The rect returned will be extended to below the system ui that covers the workspace
    Rect getHotseatRect() {
        if (isVerticalBarLayout()) {
            return new Rect(availableWidthPx - hotseatBarHeightPx, 0,
                    Integer.MAX_VALUE, availableHeightPx);
        } else {
            return new Rect(0, availableHeightPx - hotseatBarHeightPx,
                    availableWidthPx, Integer.MAX_VALUE);
        }
    }

    public static int calculateCellWidth(int width, int countX) {
        return width / countX;
    }
    public static int calculateCellHeight(int height, int countY) {
        return height / countY;
    }

    /**
     * When {@code true}, hotseat is on the bottom row when in landscape mode.
     * If {@code false}, hotseat is on the right column when in landscape mode.
     */
    boolean isVerticalBarLayout() {
        return isLandscape && transposeLayoutWithOrientation;
    }

    boolean shouldFadeAdjacentWorkspaceScreens() {
        return isVerticalBarLayout() || isLargeTablet;
    }

    private int getVisibleChildCount(ViewGroup parent) {
        int visibleChildren = 0;
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChildAt(i).getVisibility() != View.GONE) {
                visibleChildren++;
            }
        }
        return visibleChildren;
    }

    public void layout(Launcher launcher) {
        FrameLayout.LayoutParams lp;
        boolean hasVerticalBarLayout = isVerticalBarLayout();
        final boolean isLayoutRtl = Utilities.isRtl(launcher.getResources());

        // Layout the search bar space
        View searchBar = launcher.getSearchDropTargetBar();
        lp = (FrameLayout.LayoutParams) searchBar.getLayoutParams();
        if (hasVerticalBarLayout) {
            // Vertical search bar space -- The search bar is fixed in the layout to be on the left
            //                              of the screen regardless of RTL
            lp.gravity = Gravity.LEFT;
            lp.width = searchBarSpaceHeightPx;
            LinearLayout targets = (LinearLayout) searchBar.findViewById(R.id.drag_target_bar);
            targets.setOrientation(LinearLayout.VERTICAL);
            FrameLayout.LayoutParams targetsLp = (FrameLayout.LayoutParams) targets.getLayoutParams();
            targetsLp.gravity = Gravity.TOP;
            targetsLp.height = LayoutParams.WRAP_CONTENT;
        } else {
            // Horizontal search bar space
            lp.gravity = Gravity.TOP;
            lp.height = searchBarSpaceHeightPx;

            LinearLayout targets = (LinearLayout) searchBar.findViewById(R.id.drag_target_bar);
            targets.getLayoutParams().width = searchBarSpaceWidthPx;
        }
        searchBar.setLayoutParams(lp);

        // Layout the workspace
        PagedView workspace = (PagedView) launcher.findViewById(R.id.workspace);
        lp = (FrameLayout.LayoutParams) workspace.getLayoutParams();
        lp.gravity = Gravity.CENTER;
        Rect padding = getWorkspacePadding(isLayoutRtl);
        workspace.setLayoutParams(lp);
        workspace.setPadding(padding.left, padding.top, padding.right, padding.bottom);
        workspace.setPageSpacing(getWorkspacePageSpacing(isLayoutRtl));

        // Layout the hotseat
        View hotseat = launcher.findViewById(R.id.hotseat);
        lp = (FrameLayout.LayoutParams) hotseat.getLayoutParams();
        if (hasVerticalBarLayout) {
            // Vertical hotseat -- The hotseat is fixed in the layout to be on the right of the
            //                     screen regardless of RTL
            lp.gravity = Gravity.RIGHT;
            lp.width = hotseatBarHeightPx;
            lp.height = LayoutParams.MATCH_PARENT;
            hotseat.findViewById(R.id.layout).setPadding(0, 2 * edgeMarginPx, 0, 2 * edgeMarginPx);
        } else if (isTablet) {
            // Pad the hotseat with the workspace padding calculated above
            lp.gravity = Gravity.BOTTOM;
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = hotseatBarHeightPx;
            hotseat.setPadding(edgeMarginPx + padding.left, 0,
                    edgeMarginPx + padding.right,
                    2 * edgeMarginPx);
        } else {
            // For phones, layout the hotseat without any bottom margin
            // to ensure that we have space for the folders
            lp.gravity = Gravity.BOTTOM;
            lp.width = LayoutParams.MATCH_PARENT;
            lp.height = hotseatBarHeightPx;
            /*hotseat.findViewById(R.id.layout).setPadding(2 * edgeMarginPx, 0,
                    2 * edgeMarginPx, 0);*/
            hotseat.findViewById(R.id.layout).setPadding(padding.left, 0,
                    padding.right, folderIconOffsetYFromHotseat);
        }
        hotseat.setLayoutParams(lp);

        // Layout the page indicators
        View pageIndicator = launcher.findViewById(R.id.page_indicator);
        //lijun add for pageIndicator begin
        View pageIndicatorCube = launcher.findViewById(R.id.page_indicator_cube);
        //lijun add for pageIndicator end
        if (pageIndicator != null) {
            if (hasVerticalBarLayout) {
                // Hide the page indicators when we have vertical search/hotseat
                pageIndicator.setVisibility(View.GONE);
                //lijun add for pageIndicator begin
                pageIndicatorCube.setVisibility(View.GONE);
                //lijun add for pageIndicator end
            } else {
                // Put the page indicators above the hotseat
                lp = (FrameLayout.LayoutParams) pageIndicator.getLayoutParams();
                lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                /*lp.width = LayoutParams.WRAP_CONTENT;
                lp.height = LayoutParams.WRAP_CONTENT;*/
                lp.height = pageIndicatorHeightPx;
                lp.bottomMargin = hotseatBarHeightPx+exttaPageIndicatorCirclePx/2;
                pageIndicator.setLayoutParams(lp);
                //lijun add for pageIndicator begin
                lp = (FrameLayout.LayoutParams) pageIndicatorCube.getLayoutParams();
                lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
//                lp.width = pageIndicatorCubePanelWidthPx;
                lp.width = LayoutParams.WRAP_CONTENT;
                lp.height = pageIndicatorCubeCellHeightPx;
                lp.bottomMargin = (int) (hotseatBarHeightPx*0.975+(exttaPageIndicatorCubePx-pageIndicatorCubeCellHeightPx)/2);
                pageIndicatorCube.setLayoutParams(lp);
                //lijun add for pageIndicator end
            }
        }

        // Layout the Overview Mode
        ViewGroup overviewMode = launcher.getOverviewPanel();
        if (overviewMode != null) {
            int overviewButtonBarHeight = getOverviewModeButtonBarHeight();
            lp = (FrameLayout.LayoutParams) overviewMode.getLayoutParams();
            lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;

            int visibleChildCount = getVisibleChildCount(overviewMode);
            int totalItemWidth = visibleChildCount * overviewModeBarItemWidthPx;
            int maxWidth = totalItemWidth + (visibleChildCount-1) * overviewModeBarSpacerWidthPx;

            lp.width = Math.min(availableWidthPx, maxWidth);
            lp.height = overviewButtonBarHeight;
            overviewMode.setLayoutParams(lp);

            if (lp.width > totalItemWidth && visibleChildCount > 1) {
                // We have enough space. Lets add some margin too.
                int margin = (lp.width - totalItemWidth) / (visibleChildCount-1);
                View lastChild = null;

                // Set margin of all visible children except the last visible child
                for (int i = 0; i < visibleChildCount; i++) {
                    if (lastChild != null) {
                        MarginLayoutParams clp = (MarginLayoutParams) lastChild.getLayoutParams();
                        if (isLayoutRtl) {
                            clp.leftMargin = margin;
                        } else {
                            clp.rightMargin = margin;
                        }
                        lastChild.setLayoutParams(clp);
                        lastChild = null;
                    }
                    View thisChild = overviewMode.getChildAt(i);
                    if (thisChild.getVisibility() != View.GONE) {
                        lastChild = thisChild;
                    }
                }
            }
        }
    }

    private int getCurrentWidth() {
        return isLandscape
                ? Math.max(widthPx, heightPx)
                : Math.min(widthPx, heightPx);
    }

    private int getCurrentHeight() {
        return isLandscape
                ? Math.min(widthPx, heightPx)
                : Math.max(widthPx, heightPx);
    }
    //liuzuo
    public  int getFolderIconOffsetY(){
        return folderIconOffsetY;
    }
    public  int getFolderIconOffsetYFromHotseat(){
        return (hotseatBarHeightPx-folderIconOffsetYFromHotseat-hotseatIconSizePx)/2;
    }
}
