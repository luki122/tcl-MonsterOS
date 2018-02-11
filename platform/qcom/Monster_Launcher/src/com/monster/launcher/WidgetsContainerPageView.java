package com.monster.launcher;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.monster.launcher.model.WidgetsModel;
import com.monster.launcher.widget.PendingAddShortcutInfo;
import com.monster.launcher.widget.PendingAddWidgetInfo;
import com.monster.launcher.widget.WidgetCell;
import com.monster.launcher.widget.WidgetHostViewLoader;
import com.monster.launcher.widget.WidgetImageView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lj on 16-6-30.
 * The widgets list view container.
 */
public class WidgetsContainerPageView extends PagedView implements View.OnLongClickListener, View.OnClickListener, DragSource ,DropTarget ,DragController.DragListener {
    private static final String TAG = "WidgetsContainerPageView";
    private static final boolean DEBUG = false;

    Launcher mLauncher;
    private DragController mDragController;

    private int rowSize = 4;

    ArrayList<Object> mWidgetsInfos;
    private LayoutInflater mLayoutInflater;
    private WidgetPreviewLoader mWidgetPreviewLoader;
    private IconCache mIconCache;
    private PackageManager mPackageManager;

    private float curScrollXRate;

    ImageView leftIndicator,rightIndicator;
    public void setIndicator(ImageView left, ImageView right){
        leftIndicator = left;
        rightIndicator = right;
    }

    public WidgetsContainerPageView(Context context) {
        this(context, null);
    }

    public WidgetsContainerPageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WidgetsContainerPageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLauncher = (Launcher) context;
        mDragController = mLauncher.getDragController();

        final Resources res = getResources();

        mWidgetsInfos = new ArrayList<Object>();
        mLayoutInflater = LayoutInflater.from(context);
        mIconCache = (LauncherAppState.getInstance()).getIconCache();
        rowSize = res.getInteger(R.integer.config_widegtscontainerpageview_cells_count);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int count = getChildCount();
        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            InvariantDeviceProfile idp = LauncherAppState.getInstance().getInvariantDeviceProfile();
            int childWidthMode;
            int childHeightMode;
            childWidthMode = MeasureSpec.EXACTLY;
            childHeightMode = MeasureSpec.EXACTLY;
            heightSize = idp.portraitProfile.widgetsContainerBarHeightPx;
            final int childWidthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(widthSize, childWidthMode);
            final int childHeightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(heightSize, childHeightMode);
            for (int i = 0; i < count; i++) {
                getChildAt(i).measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }else{
            for (int i = 0; i < count; i++) {
                getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
            }
        }
        mViewport.set(0, 0, widthSize, heightSize);
        // 给每一个子view给予相同的空间

        /** 滚动到目标坐标 */
//        scrollTo(mCurScreen * widthSize, 0);
        this.setMeasuredDimension(widthSize,heightSize);
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mPageIndicator = null;
    }

    @Override
    protected void overScroll(float amount) {
        boolean shouldOverScroll = (amount <= 0 && mIsRtl) ||
                (amount >= 0 &&  !mIsRtl);

        if(shouldOverScroll) {
            dampedOverScroll(amount);
        }
    }
    @Override
    protected void getEdgeVerticalPostion(int[] pos) {
        View child = getChildAt(getPageCount() - 1);
        pos[0] = child.getTop();
        pos[1] = child.getBottom();
    }

    public void scrollToTop() {
//        snapToPage(0);
    }
    final public void setSearchBarBounds(Rect bounds) {
        //here need consider
    }

    /**
     * Initialize the widget data model.
     */
    public void addWidgets(WidgetsModel model) {
        mWidgetsInfos.clear();
        final int size = model.getPackageSize();
        for(int i = 0 ; i < size ; i ++){
            List<Object> infoList = model.getSortedWidgets(i);
            mWidgetsInfos.addAll(infoList);
        }
//        sortWidgetBySystemAndThirdpart();
        updateViews();
    }

    /**
     * 对widgets排序，系统widget放前面，第三方放后面
     */
    private void sortWidgetBySystemAndThirdpart() {
        ArrayList<Object> tempThirdpartWidgets = new ArrayList<Object>();
        if (mWidgetsInfos != null && mWidgetsInfos.size() > 0) {
            boolean isSystemApp = true;
            for (Iterator it = mWidgetsInfos.iterator(); it.hasNext(); ) {
                Object info = it.next();
                if (info instanceof LauncherAppWidgetProviderInfo) {
                    isSystemApp = Utilities.isSystemApp(((LauncherAppWidgetProviderInfo) info).provider.getPackageName(),getContext());
                } else if (info instanceof ResolveInfo) {
                    isSystemApp = Utilities.isSystemApp(((ResolveInfo) info).activityInfo.packageName,getContext());
                }
                if (!isSystemApp) {
                    tempThirdpartWidgets.add(info);
                    it.remove();
                }
            }
            for (Object info2 : tempThirdpartWidgets) {
                mWidgetsInfos.add(info2);
            }
        }
    }

    public void updateViews(){
        if(mWidgetsInfos == null || mWidgetsInfos.size() <= 0)return;
        final int infoSize = mWidgetsInfos.size();
        final int cellLayoutSize = infoSize/rowSize + ((infoSize%rowSize>0)?1:0);
        removeAllViews();
        for(int i = 0 ; i < cellLayoutSize ; i ++){
            LinearLayout widgetCellLayout = (LinearLayout)mLayoutInflater.inflate(R.layout.widgets_container_pageview_celllayout,this,false);
            finish:
            for(int j = 0 ; j < rowSize ; j ++){
                final int index = i*rowSize + j;

                if(index >= infoSize)break finish;
                Object info = mWidgetsInfos.get(index);
                WidgetCell widget = (WidgetCell) mLayoutInflater.inflate(
                        R.layout.widget_cell_forpageview, widgetCellLayout, false);
                if (info instanceof LauncherAppWidgetProviderInfo) {
                    LauncherAppWidgetProviderInfo infoTemp = (LauncherAppWidgetProviderInfo) info;
                    PendingAddWidgetInfo pawi = new PendingAddWidgetInfo(mLauncher, infoTemp, null);
                    widget.setTag(pawi);
                    widget.applyFromAppWidgetProviderInfo(infoTemp, getWidgetPreviewLoader());
                }else if (info instanceof ResolveInfo) {
                    ResolveInfo infoTemp = (ResolveInfo) info;
                    PendingAddShortcutInfo pasi = new PendingAddShortcutInfo(infoTemp.activityInfo);
                    widget.setTag(pasi);
                    widget.applyFromResolveInfo(mLauncher.getPackageManager(), infoTemp, getWidgetPreviewLoader());
                }
                widget.ensurePreview();
                widget.setVisibility(View.VISIBLE);
                widget.setOnClickListener(this);
                widget.setOnLongClickListener(this);
                widgetCellLayout.addView(widget);
            }
            addView(widgetCellLayout);
        }
    }
    private WidgetPreviewLoader getWidgetPreviewLoader() {
        if (mWidgetPreviewLoader == null) {
            mWidgetPreviewLoader = LauncherAppState.getInstance().getWidgetCache();
        }
        return mWidgetPreviewLoader;
    }
    @Override
    public boolean supportsFlingToDelete() {
        return false;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return true;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return true;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        return 0;
    }

    @Override
    public void onFlingToDeleteCompleted() {
//        mLauncher.exitSpringLoadedDragModeDelayed(true,
//                Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
        mLauncher.unlockScreenOrientation(false);
    }

    @Override
    public void onDropCompleted(View target, DropTarget.DragObject d, boolean isFlingToDelete, boolean success) {
        if (isFlingToDelete || !success || (target != mLauncher.getWorkspace() &&
                !(target instanceof DeleteDropTarget) && !(target instanceof Folder))) {
            // Exit spring loaded mode if we have not successfully dropped or have not handled the
            // drop in Workspace
//            mLauncher.exitSpringLoadedDragModeDelayed(true,
//                    Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
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
//            if (showOutOfSpaceMessage) {
//                mLauncher.showOutOfSpaceMessage(false);
//            }
            d.deferDragViewCleanupPostAnimation = false;
        }
    }

    @Override
    public void onClick(View v) {
        if (!mLauncher.isWidgetsViewVisible()
                || mLauncher.getWorkspace().isSwitchingState()
                || !(v instanceof WidgetCell)) return;

        if (v.getTag() instanceof ItemInfo) {
            ItemInfo info = (ItemInfo) v.getTag();
            mLauncher.getWorkspace().onWidgetCellClick(info,(WidgetCell)v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (!v.isInTouchMode()) return false;
        // When we have exited all apps or are in transition, disregard long clicks
        if (!mLauncher.isWidgetsViewVisible() ||
                mLauncher.getWorkspace().isSwitchingState() || !(v instanceof WidgetCell)) return false;
        // Return if global dragging is not enabled
        Log.d(TAG, String.format("onLonglick dragging enabled?.", v));
        if (!mLauncher.isDraggingEnabled()) return false;

        boolean status = beginDragging(v);
        if (status && v.getTag() instanceof PendingAddWidgetInfo) {
            WidgetHostViewLoader hostLoader = new WidgetHostViewLoader(mLauncher, v);
            boolean preloadStatus = hostLoader.preloadWidget();
            if (DEBUG) {
                Log.d(TAG, String.format("widgets pageview preloading widget [status=%s]", preloadStatus));
            }
            mLauncher.getDragController().addDragListener(hostLoader);
        }
        return status;
    }

    private boolean beginDragging(View v) {
        mDragController.addDragListener(this);
        CellLayout.CellInfo longClickCellInfo = null;
        if (v.getTag() instanceof ItemInfo) {
            ItemInfo info = (ItemInfo) v.getTag();
            longClickCellInfo = new CellLayout.CellInfo(v, info);
        }
//        mLauncher.getWorkspace().startDragOutOfWorkspace(longClickCellInfo);
//        mLauncher.getWorkspace().getmPageIndicatorManager().showCubeIndicator();

        if (v instanceof WidgetCell) {
            if (!beginDraggingWidget((WidgetCell) v)) {
                return false;
            }
        } else {
            Log.e(TAG, "Unexpected dragging view: " + v);
        }

        // We don't enter spring-loaded mode if the drag has been cancelled
        if (mLauncher.getDragController().isDragging()) {
            // Go into spring loaded mode (must happen before we startDrag())
            mLauncher.enterSpringLoadedDragMode();
        }

        return true;
    }

    private boolean beginDraggingWidget(WidgetCell v) {
        // Get the widget preview as the drag representation
        WidgetImageView image = (WidgetImageView) v.findViewById(R.id.widget_preview);
        PendingAddItemInfo createItemInfo = (PendingAddItemInfo) v.getTag();

        // If the ImageView doesn't have a drawable yet, the widget preview hasn't been loaded and
        // we abort the drag.
        if (image.getBitmap() == null) {
            return false;
        }

        // Compose the drag image
        Bitmap preview;
        float scale = 1f;
        final Rect bounds = image.getBitmapBounds();

        if (createItemInfo instanceof PendingAddWidgetInfo) {
            // This can happen in some weird cases involving multi-touch. We can't start dragging
            // the widget if this is null, so we break out.

            PendingAddWidgetInfo createWidgetInfo = (PendingAddWidgetInfo) createItemInfo;
            int[] size = mLauncher.getWorkspace().estimateItemSize(createWidgetInfo, true);

            Bitmap icon = image.getBitmap();
            float minScale = 1.25f;
            int maxWidth = Math.min((int) (icon.getWidth() * minScale), size[0]);

            int[] previewSizeBeforeScale = new int[1];
            preview = getWidgetPreviewLoader().generateWidgetPageViewPreview(mLauncher,
                    createWidgetInfo.info, maxWidth, null, previewSizeBeforeScale);

            if (previewSizeBeforeScale[0] < icon.getWidth()) {
                // The icon has extra padding around it.
                int padding = (icon.getWidth() - previewSizeBeforeScale[0]) / 2;
                if (icon.getWidth() > image.getWidth()) {
                    padding = padding * image.getWidth() / icon.getWidth();
                }

                bounds.left += padding;
                bounds.right -= padding;
            }
            scale = bounds.width() / (float) preview.getWidth();
        } else {
            PendingAddShortcutInfo createShortcutInfo = (PendingAddShortcutInfo) v.getTag();
            Drawable icon = mIconCache.getFullResIcon(createShortcutInfo.activityInfo);
            preview = Utilities.createIconBitmap(icon, mLauncher);
            createItemInfo.spanX = createItemInfo.spanY = 1;
            scale = ((float) mLauncher.getDeviceProfile().iconSizePx) / preview.getWidth();
        }

        // Don't clip alpha values for the drag outline if we're using the default widget preview
        boolean clipAlpha = !(createItemInfo instanceof PendingAddWidgetInfo &&
                (((PendingAddWidgetInfo) createItemInfo).previewImage == 0));

        // Start the drag
        mLauncher.lockScreenOrientation();
        mLauncher.getWorkspace().onDragStartedWithItem(createItemInfo, preview, clipAlpha);
        mDragController.startDrag(image, preview, this, createItemInfo,
                bounds, DragController.DRAG_ACTION_COPY, scale);

        preview.recycle();
        return true;
    }

    @Override
    public boolean isDropEnabled() {
        return true;
    }

    @Override
    public void onDrop(DragObject dragObject) {

    }

    @Override
    public void onDragEnter(DragObject dragObject) {

    }

    @Override
    public void onDragOver(DragObject dragObject) {

    }

    @Override
    public void onDragExit(DragObject dragObject) {

    }

    @Override
    public void onFlingToDelete(DragObject dragObject, PointF vec) {

    }

    @Override
    public boolean acceptDrop(DragObject dragObject) {
        return false;
    }

    @Override
    public void prepareAccessibilityDrop() {

    }

    @Override
    public void getHitRectRelativeToDragLayer(Rect outRect) {
        if(mLauncher!=null){
            mLauncher.getDragLayer().getDescendantRectRelativeToSelf(this, outRect);
        }
    }

    @Override
    public void getLocationInDragLayer(int[] loc) {

    }

    @Override
    protected void snapToPage(int whichPage, int delta, int duration, boolean immediate, TimeInterpolator interpolator) {
        super.snapToPage(whichPage, delta, duration, immediate, interpolator);
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        if (mDragController != null && !mDragController.isContainDropTarget(this)) {
            mDragController.addDropTarget(this);
        }
    }

    @Override
    public void onDragEnd() {

        if (mDragController != null && mDragController.isContainDropTarget(this)) {
            mDragController.removeDragListener(this);
            mDragController.removeDropTarget(this);
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        if (getWidth() > 0) {
            curScrollXRate = x / (float) getWidth();
            if (curScrollXRate < 0) {
                leftIndicator.setAlpha(0.0f);
            } else if (curScrollXRate < 1.0) {
                leftIndicator.setAlpha(1.0f * curScrollXRate);
            } else {
                leftIndicator.setAlpha(1.0f);
            }

            if (curScrollXRate > getPageCount() - 1) {
                rightIndicator.setAlpha(0.0f);
            } else if (curScrollXRate > getPageCount() - 2) {
                rightIndicator.setAlpha(1.0f * (getPageCount() - 1 - curScrollXRate));
            } else {
                rightIndicator.setAlpha(1.0f);
            }
        }
    }

    public void updateWidgetsPageIndicator(){
        boolean isBlacktext = LauncherAppState.getInstance().getWindowGlobalVaule().isBlackText();
        if(isBlacktext){
            leftIndicator.setImageResource(R.drawable.ic_widgets_left_indicator_black);
            rightIndicator.setImageResource(R.drawable.ic_widgets_right_indicator_black);
        }else{
            leftIndicator.setImageResource(R.drawable.ic_widgets_left_indicator);
            rightIndicator.setImageResource(R.drawable.ic_widgets_right_indicator);
        }
    }
}
