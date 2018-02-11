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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Looper;
import android.os.Parcelable;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.monster.launcher.DropTarget.DragObject;
import com.monster.launcher.compat.UserHandleCompat;
import com.monster.launcher.unread.MonsterUnreadLoader;
import com.monster.launcher.util.Thunk;

import java.util.ArrayList;

/**
 * An icon that can appear on in the workspace representing an {@link UserFolder}.
 */
public class FolderIcon extends FrameLayout implements FolderInfo.FolderListener,IChangeColors.IItemColorChange {
    private static final String TAG = "FolderIcon";
    @Thunk
    Launcher mLauncher;
    @Thunk Folder mFolder;
    private FolderInfo mInfo;
    @Thunk static boolean sStaticValuesDirty = true;

    private CheckLongPressHelper mLongPressHelper;
    private StylusEventHelper mStylusEventHelper;

    // The number of icons to display in the
    //M:liuzuo change the foldericon 3>>4 begin
    static int mFolderImportRadius;
    private float scale;
    public static  int NUM_ITEMS_IN_PREVIEW = 3;
    private int mItemInPreviewSize;
    private int paddingX, paddingY, middlePaddingX, middlePaddingY;
    private FolderIconMode folderIconMode = FolderIconMode.Four;
    private int paddingNum;
    private int mOffsetXFolderBackground;
    private int mOffsetYFolderBackground;
    private int mOffsetYtemp;
    @Override
    public void changeColors(int[] colors) {
        this.mFolderName.setTextColor(colors[0]);
        mFolder.mFolderName.setHintTextColor(colors[0]);
        mFolder.mFolderName.setTextColor(colors[2]);
        mFolder.mContent.getFolderAddIcon().setTextColor(colors[2]);
        mFolder.setFolderBackground();
        Log.d("liuzuo98","changeColors="+Integer.toHexString(colors[0]));
    }

    enum FolderIconMode {One,Four,Nine}
    //M:liuzuo change the foldericon 3>>4 end
    private static final int CONSUMPTION_ANIMATION_DURATION = 100;
    private static final int DROP_IN_ANIMATION_DURATION = 400;
    private static final int INITIAL_ITEM_ANIMATION_DURATION = 350;
    private static final int FINAL_ITEM_ANIMATION_DURATION = 200;

    // The degree to which the inner ring grows when accepting drop
    private static final float INNER_RING_GROWTH_FACTOR = 0.25f;//liuzuo 0.15f>>0.2f

    // The degree to which the outer ring is scaled in its natural state
    private static final float OUTER_RING_GROWTH_FACTOR = 0.3f;

    // The amount of vertical spread between items in the stack [0...1]
    private static final float PERSPECTIVE_SHIFT_FACTOR = 0.18f;

    // Flag as to whether or not to draw an outer ring. Currently none is designed.
    public static final boolean HAS_OUTER_RING = true;

    // Flag whether the folder should open itself when an item is dragged over is enabled.
    public static final boolean SPRING_LOADING_ENABLED = true;

    // The degree to which the item in the back of the stack is scaled [0...1]
    // (0 means it's not scaled at all, 1 means it's scaled to nothing)
    private static final float PERSPECTIVE_SCALE_FACTOR = 0.35f;

    // Delay when drag enters until the folder opens, in miliseconds.
    private static final int ON_OPEN_DELAY = 800;

    public static Drawable sSharedFolderLeaveBehind = null;

    @Thunk ImageView mPreviewBackground;
    @Thunk BubbleTextView mFolderName;

    FolderRingAnimator mFolderRingAnimator = null;

    // These variables are all associated with the drawing of the preview; they are stored
    // as member variables for shared usage and to avoid computation on each frame
    private int mIntrinsicIconSize;
    private float mBaselineIconScale;
    private int mBaselineIconSize;
    private int mAvailableSpaceInPreview;
    private int mTotalWidth = -1;
    private int mPreviewOffsetX;
    private int mPreviewOffsetY;
    private float mMaxPerspectiveShift;
    boolean mAnimating = false;
    private Rect mOldBounds = new Rect();

    private float mSlop;

    private PreviewItemDrawingParams mParams = new PreviewItemDrawingParams(0, 0, 0, 0);
    @Thunk PreviewItemDrawingParams mAnimParams = new PreviewItemDrawingParams(0, 0, 0, 0);
    @Thunk ArrayList<ShortcutInfo> mHiddenItems = new ArrayList<ShortcutInfo>();

    private Alarm mOpenAlarm = new Alarm();
    @Thunk ItemInfo mDragInfo;

    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FolderIcon(Context context) {
        super(context);
        init();
    }

    private void init() {
        mLongPressHelper = new CheckLongPressHelper(this);
        mStylusEventHelper = new StylusEventHelper(this);
        setAccessibilityDelegate(LauncherAppState.getInstance().getAccessibilityDelegate());
        mOffsetXFolderBackground=getResources().getDimensionPixelOffset(R.dimen.folder_icon_background_offsetX);
        mOffsetYFolderBackground=getResources().getDimensionPixelOffset(R.dimen.folder_icon_background_offsetY);
    }

    public boolean isDropEnabled() {
        final ViewGroup cellLayoutChildren = (ViewGroup) getParent();
        final ViewGroup cellLayout = (ViewGroup) cellLayoutChildren.getParent();
        final Workspace workspace = (Workspace) cellLayout.getParent();
        return !workspace.workspaceInModalState();
    }

    static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
            FolderInfo folderInfo, IconCache iconCache) {
        @SuppressWarnings("all") // suppress dead code warning
        final boolean error = INITIAL_ITEM_ANIMATION_DURATION >= DROP_IN_ANIMATION_DURATION;
        if (error) {
            throw new IllegalStateException("DROP_IN_ANIMATION_DURATION must be greater than " +
                    "INITIAL_ITEM_ANIMATION_DURATION, as sequencing of adding first two items " +
                    "is dependent on this");
        }

        DeviceProfile grid = launcher.getDeviceProfile();

        FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);
        icon.setClipToPadding(false);
        icon.mFolderName = (BubbleTextView) icon.findViewById(R.id.folder_icon_name);
        //liuzuo begin
        mFolderImportRadius =  launcher.getResources().getInteger(R.integer.folder_import_icon_radius);
        if("".equals(folderInfo.title)){
            String folderName=launcher.getResources().getString(R.string.folder_hint_text);
            icon.mFolderName.setText(folderName);
            folderInfo.title=folderName;
        }else {
            //icon.mFolderName.setText(folderInfo.title);
            //liuzuo end

            //add by xiangzx
            ContentResolver contentResolver = launcher.getContentResolver();
            String currentLanguage = launcher.getResources().getConfiguration().locale.getLanguage();
            if(!LauncherAppState.getLauncherProvider().fieldExists(currentLanguage)){
                currentLanguage = LauncherSettings.AppCategory.EN;
            }
            Cursor categoryCursor = contentResolver.query(LauncherSettings.AppCategory.APP_CATEGORY_URI,
                    new String[]{currentLanguage},
                    LauncherSettings.AppCategory.CATEGORY_NAME + "=?", new String[]{folderInfo.title.toString()}, null);
            if (categoryCursor.getCount() == 1) {
                categoryCursor.moveToFirst();
                if (categoryCursor.getString(categoryCursor.getColumnIndex(currentLanguage)) != null) {
                    icon.mFolderName.setText(categoryCursor.getString(categoryCursor.getColumnIndex(currentLanguage)));
                } else {
                    String folderName = launcher.getResources().getString(R.string.folder_hint_text);
                    icon.mFolderName.setText(folderName);
                    folderInfo.title = folderName;
                }
            }else{
                icon.mFolderName.setText(folderInfo.title);
            }
            categoryCursor.close();
        }

        icon.mFolderName.setCompoundDrawablePadding(0);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) icon.mFolderName.getLayoutParams();
        lp.topMargin = grid.iconSizePx + grid.iconDrawablePaddingPx;

        // Offset the preview background to center this view accordingly
        icon.mPreviewBackground = (ImageView) icon.findViewById(R.id.preview_background);
        lp = (FrameLayout.LayoutParams) icon.mPreviewBackground.getLayoutParams();
        //liuzuo begin
        if(lp.width<grid.folderIconSizePx){
            lp.topMargin=0;
            Log.d("liuzuo82","lp.width<grid.folderIconSizePx"+grid.folderBackgroundOffset);
        }else {
            lp.topMargin = grid.folderBackgroundOffset;
        }
        //liuzuo end
        lp.width = grid.folderIconSizePx;
        lp.height = grid.folderIconSizePx;

        icon.setTag(folderInfo);
        icon.setOnClickListener(launcher);
        icon.mInfo = folderInfo;
        icon.mLauncher = launcher;
        icon.setContentDescription(String.format(launcher.getString(R.string.folder_name_format),
                folderInfo.title));
        icon.mFolderName.setTextColor(LauncherAppState.getInstance().getWindowGlobalVaule().getTextColor());
        Folder folder = Folder.fromXml(launcher);
        folder.setDragController(launcher.getDragController());
        folder.setFolderIcon(icon);
        folder.bind(folderInfo, icon.mFolderName.getText().toString());
        icon.mFolder = folder;

        icon.mFolderRingAnimator = new FolderRingAnimator(launcher, icon);
        folderInfo.addListener(icon);

        icon.setOnFocusChangeListener(launcher.mFocusHandler);

        return icon;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        sStaticValuesDirty = true;
        return super.onSaveInstanceState();
    }

    public static class FolderRingAnimator {
        public int mCellX;
        public int mCellY;
        @Thunk CellLayout mCellLayout;
        public float mOuterRingSize;
        public float mInnerRingSize;
        public FolderIcon mFolderIcon = null;
        public static Drawable sSharedOuterRingDrawable = null;
        public static Drawable sSharedInnerRingDrawable = null;
        public static int sPreviewSize = -1;
        public static int sPreviewPadding = -1;

        private ValueAnimator mAcceptAnimator;
        private ValueAnimator mNeutralAnimator;

        public FolderRingAnimator(Launcher launcher, FolderIcon folderIcon) {
            mFolderIcon = folderIcon;
            Resources res = launcher.getResources();

            // We need to reload the static values when configuration changes in case they are
            // different in another configuration
            if (sStaticValuesDirty) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    throw new RuntimeException("FolderRingAnimator loading drawables on non-UI thread "
                            + Thread.currentThread());
                }

                DeviceProfile grid = launcher.getDeviceProfile();
                sPreviewSize = grid.folderIconSizePx;
                sPreviewPadding = res.getDimensionPixelSize(R.dimen.folder_preview_padding);
                sSharedOuterRingDrawable = res.getDrawable(R.drawable.portal_ring_outer);
                sSharedInnerRingDrawable = res.getDrawable(R.drawable.portal_ring_inner);//liuzuo portal_ring_inner_nolip>>portal_ring_inner
                sSharedFolderLeaveBehind = res.getDrawable(R.drawable.portal_ring_rest);
                sStaticValuesDirty = false;
            }
        }

        public void animateToAcceptState() {
            if (mNeutralAnimator != null) {
                mNeutralAnimator.cancel();
            }
            mAcceptAnimator = LauncherAnimUtils.ofFloat(mCellLayout, 0f, 1f);
            mAcceptAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);

            final int previewSize = sPreviewSize;
            mAcceptAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + percent * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    mInnerRingSize = (1 + percent * INNER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            });
            mAcceptAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mFolderIcon != null) {
                        mFolderIcon.mPreviewBackground.setVisibility(INVISIBLE);
                    }
                }
            });
            mAcceptAnimator.start();
        }

        public void animateToNaturalState() {
            if (mAcceptAnimator != null) {
                mAcceptAnimator.cancel();
            }
            mNeutralAnimator = LauncherAnimUtils.ofFloat(mCellLayout, 0f, 1f);
            mNeutralAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);

            final int previewSize = sPreviewSize;
            mNeutralAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float percent = (Float) animation.getAnimatedValue();
                    mOuterRingSize = (1 + (1 - percent) * OUTER_RING_GROWTH_FACTOR) * previewSize;
                    mInnerRingSize = (1 + (1 - percent) * INNER_RING_GROWTH_FACTOR) * previewSize;
                    if (mCellLayout != null) {
                        mCellLayout.invalidate();
                    }
                }
            });
            mNeutralAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mCellLayout != null) {
                        mCellLayout.hideFolderAccept(FolderRingAnimator.this);
                    }
                    if (mFolderIcon != null) {
                        mFolderIcon.mPreviewBackground.setVisibility(VISIBLE);
                    }
                }
            });
            mNeutralAnimator.start();
        }

        // Location is expressed in window coordinates
        public void getCell(int[] loc) {
            loc[0] = mCellX;
            loc[1] = mCellY;
        }

        // Location is expressed in window coordinates
        public void setCell(int x, int y) {
            mCellX = x;
            mCellY = y;
        }

        public void setCellLayout(CellLayout layout) {
            mCellLayout = layout;
        }

        public float getOuterRingSize() {
            return mOuterRingSize;
        }

        public float getInnerRingSize() {
            return mInnerRingSize;
        }
    }

    public Folder getFolder() {
        return mFolder;
    }

    FolderInfo getFolderInfo() {
        return mInfo;
    }

    private boolean willAcceptItem(ItemInfo item) {
        final int itemType = item.itemType;
        return ((itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) &&
                !mFolder.isFull() && item != mInfo && !mInfo.opened);
    }

    public boolean acceptDrop(Object dragInfo) {
        final ItemInfo item = (ItemInfo) dragInfo;
        return !mFolder.isDestroyed() && willAcceptItem(item);
    }

    public void addItem(ShortcutInfo item) {
        mInfo.add(item);
    }

    public void onDragEnter(Object dragInfo) {
        if (mFolder.isDestroyed() || !willAcceptItem((ItemInfo) dragInfo)) return;
        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) getLayoutParams();
        CellLayout layout = (CellLayout) getParent().getParent();
        mFolderRingAnimator.setCell(lp.cellX, lp.cellY);
        mFolderRingAnimator.setCellLayout(layout);
        mFolderRingAnimator.animateToAcceptState();
        layout.showFolderAccept(mFolderRingAnimator);
        mOpenAlarm.setOnAlarmListener(mOnOpenListener);
        if (SPRING_LOADING_ENABLED &&
                ((dragInfo instanceof AppInfo) || (dragInfo instanceof ShortcutInfo))) {
            // TODO: we currently don't support spring-loading for PendingAddShortcutInfos even
            // though widget-style shortcuts can be added to folders. The issue is that we need
            // to deal with configuration activities which are currently handled in
            // Workspace#onDropExternal.
            mOpenAlarm.setAlarm(ON_OPEN_DELAY);
        }
        mDragInfo = (ItemInfo) dragInfo;
    }

    public void onDragOver(Object dragInfo) {
    }

    OnAlarmListener mOnOpenListener = new OnAlarmListener() {
        public void onAlarm(Alarm alarm) {
            ShortcutInfo item;
            if (mDragInfo instanceof AppInfo) {
                // Came from all apps -- make a copy.
                item = ((AppInfo) mDragInfo).makeShortcut();
                item.spanX = 1;
                item.spanY = 1;
            } else {
                // ShortcutInfo
                item = (ShortcutInfo) mDragInfo;
            }
            mFolder.beginExternalDrag(item);
            mLauncher.openFolder(FolderIcon.this);
            mLauncher.hideStausBar();//liuzuo add
        }
    };

    public void performCreateAnimation(final ShortcutInfo destInfo, final View destView,
            final ShortcutInfo srcInfo, final DragView srcView, Rect dstRect,
            float scaleRelativeToDragLayer, Runnable postAnimationRunnable) {

        // These correspond two the drawable and view that the icon was dropped _onto_
        Drawable animateDrawable = getTopDrawable((TextView) destView);
        computePreviewDrawingParams(animateDrawable.getIntrinsicWidth(),
                destView.getMeasuredWidth());

        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        animateFirstItem(animateDrawable, INITIAL_ITEM_ANIMATION_DURATION, false, null);
        addItem(destInfo);

        // This will animate the dragView (srcView) into the new folder
        onDrop(srcInfo, srcView, dstRect, scaleRelativeToDragLayer, 1, postAnimationRunnable, null);
    }

    public void performDestroyAnimation(final View finalView, Runnable onCompleteRunnable) {
        Drawable animateDrawable = getTopDrawable((TextView) finalView);
        //liuzuo add begin
        int width ;
        if(mTotalWidth>0){
            width = mTotalWidth;
        }else {
            width = finalView.getMeasuredWidth();
        }
        //liuzuo add end
        computePreviewDrawingParams(animateDrawable.getIntrinsicWidth(),
                width);
        // This will animate the first item from it's position as an icon into its
        // position as the first item in the preview
        animateFirstItem(animateDrawable, FINAL_ITEM_ANIMATION_DURATION, true,
                onCompleteRunnable);
    }

    public void onDragExit(Object dragInfo) {
        onDragExit();
    }

    public void onDragExit() {
        mFolderRingAnimator.animateToNaturalState();
        mOpenAlarm.cancelAlarm();
    }

    private void onDrop(final ShortcutInfo item, DragView animateView, Rect finalRect,
            float scaleRelativeToDragLayer, int index, Runnable postAnimationRunnable,
            DragObject d) {
        item.cellX = -1;
        item.cellY = -1;

        // Typically, the animateView corresponds to the DragView; however, if this is being done
        // after a configuration activity (ie. for a Shortcut being dragged from AllApps) we
        // will not have a view to animate
        if (animateView != null) {
            DragLayer dragLayer = mLauncher.getDragLayer();
            Rect from = new Rect();
            dragLayer.getViewRectRelativeToSelf(animateView, from);
            Rect to = finalRect;
            if (to == null) {
                to = new Rect();
                Workspace workspace = mLauncher.getWorkspace();
                // Set cellLayout and this to it's final state to compute final animation locations
                workspace.setFinalTransitionTransform((CellLayout) getParent().getParent());
                float scaleX = getScaleX();
                float scaleY = getScaleY();
                setScaleX(1.0f);
                setScaleY(1.0f);
                scaleRelativeToDragLayer = dragLayer.getDescendantRectRelativeToSelf(this, to);
                // Finished computing final animation locations, restore current state
                setScaleX(scaleX);
                setScaleY(scaleY);
                workspace.resetTransitionTransform((CellLayout) getParent().getParent());
            }

            int[] center = new int[2];
            float scale = getLocalCenterForIndex(index, center);
            center[0] = (int) Math.round(scaleRelativeToDragLayer * center[0]);
            center[1] = (int) Math.round(scaleRelativeToDragLayer * center[1]);

            to.offset(center[0] - animateView.getMeasuredWidth() / 2,
                      center[1] - animateView.getMeasuredHeight() / 2);
            Log.d("liuzuo98", "   center[0]="+ center[0]+"  center[1]="+center[1]);
            float finalAlpha = index < NUM_ITEMS_IN_PREVIEW ? 0.4f : 0f;

            float finalScale = scale * scaleRelativeToDragLayer;
            dragLayer.animateView(animateView, from, to, finalAlpha,
                    1, 1, finalScale, finalScale, DROP_IN_ANIMATION_DURATION,
                    new AccelerateInterpolator(2f), new AccelerateDecelerateInterpolator(),
                    postAnimationRunnable, DragLayer.ANIMATION_END_DISAPPEAR, null);
            addItem(item);
            mHiddenItems.add(item);
            mFolder.hideItem(item);
            postDelayed(new Runnable() {
                public void run() {
                    mHiddenItems.remove(item);
                    mFolder.showItem(item);
                    mLauncher.getHotseat().onExitHotseat(null,CellLayout.MODE_ON_DROP);
                    invalidate();
                }
            }, DROP_IN_ANIMATION_DURATION);
        } else {
            addItem(item);
        }
    }

    public void onDrop(DragObject d) {
        ShortcutInfo item;
        if (d.dragInfo instanceof AppInfo) {
            // Came from all apps -- make a copy
            item = ((AppInfo) d.dragInfo).makeShortcut();
        } else {
            item = (ShortcutInfo) d.dragInfo;
        }
        mFolder.notifyDrop();
        onDrop(item, d.dragView, null, 1.0f, mInfo.contents.size(), d.postAnimationRunnable, d);
    }

    private void computePreviewDrawingParams(int drawableSize, int totalSize) {
        if (mIntrinsicIconSize != drawableSize || mTotalWidth != totalSize||mOffsetYtemp!=getItemDrawingParamOffsetY()) {
            DeviceProfile grid = mLauncher.getDeviceProfile();

            mIntrinsicIconSize = drawableSize;
            mTotalWidth = totalSize;
            //M:liuzuo change the foldericon begin
            if (folderIconMode != FolderIconMode.One) {
                final int previewSize = (int) (drawableSize - mOffsetXFolderBackground * 2);//FolderRingAnimator.sPreviewSize;
                final int previewPadding = FolderRingAnimator.sPreviewPadding;
                mAvailableSpaceInPreview = previewSize;
                final int previewIconItemPadding = getResources().getDimensionPixelSize(R.dimen.folder_preview_IconItemPadding);
                int preIconItemSize = 0;
                if (folderIconMode == FolderIconMode.Four) {
                    paddingNum = 3;
                    //单个icon的大小
                    preIconItemSize = mItemInPreviewSize = (previewSize - (paddingNum - 2) * previewIconItemPadding - 2 * previewPadding) / (paddingNum - 1);

                } else if (folderIconMode == FolderIconMode.Nine) {
                    paddingNum = 4;
                    preIconItemSize = mItemInPreviewSize = (previewSize - (paddingNum - 2) * previewIconItemPadding - 2 * previewPadding) / (paddingNum - 1);

                }
                scale = ((float) preIconItemSize) / drawableSize;
                int extraLine = ((int) ((mIntrinsicIconSize - mAvailableSpaceInPreview) * scale)) / 2;
                middlePaddingX = middlePaddingY = previewIconItemPadding;
                paddingX = (int) (mTotalWidth - drawableSize) / 2 + mOffsetXFolderBackground + previewPadding;
                Log.d("liuzuo82", "preIconItemSize=" +preIconItemSize + " drawableSize=" + drawableSize+  " mIntrinsicIconSize" + mIntrinsicIconSize + " scale=" + scale);
                mOffsetYtemp=getItemDrawingParamOffsetY();
                paddingY = previewPadding + mOffsetYFolderBackground+mOffsetYtemp;
                NUM_ITEMS_IN_PREVIEW = getRow() * getRow();
            } else {
                //M:liuzuo change the foldericon end
                final int previewSize = mPreviewBackground.getLayoutParams().height;
                final int previewPadding = FolderRingAnimator.sPreviewPadding;
                mAvailableSpaceInPreview = (previewSize - 2 * previewPadding);
                // cos(45) = 0.707  + ~= 0.1) = 0.8f

                int adjustedAvailableSpace = (int) ((mAvailableSpaceInPreview / 2) * (1 + 0.8f));

                int unscaledHeight = (int) (mIntrinsicIconSize * (1 + PERSPECTIVE_SHIFT_FACTOR));

                mBaselineIconScale = (1.0f * adjustedAvailableSpace / unscaledHeight);

                mBaselineIconSize = (int) (mIntrinsicIconSize * mBaselineIconScale);
                mMaxPerspectiveShift = mBaselineIconSize * PERSPECTIVE_SHIFT_FACTOR;

                mPreviewOffsetX = (mTotalWidth - mAvailableSpaceInPreview) / 2;
                mPreviewOffsetY = previewPadding + grid.folderBackgroundOffset;
            }
        }
    }

    private void computePreviewDrawingParams(Drawable d) {
        computePreviewDrawingParams(d.getIntrinsicWidth(), getMeasuredWidth());
    }

    class PreviewItemDrawingParams {
        PreviewItemDrawingParams(float transX, float transY, float scale, int overlayAlpha) {
            this.transX = transX;
            this.transY = transY;
            this.scale = scale;
            this.overlayAlpha = overlayAlpha;
        }
        float transX;
        float transY;
        float scale;
        int overlayAlpha;
        Drawable drawable;
    }

    private float getLocalCenterForIndex(int index, int[] center) {
        mParams = computePreviewItemDrawingParams(Math.min(NUM_ITEMS_IN_PREVIEW, index), mParams);

        mParams.transX += mPreviewOffsetX;
        mParams.transY += mPreviewOffsetY;
        float offsetX = mParams.transX + (mParams.scale * mIntrinsicIconSize) / 2;
        float offsetY = mParams.transY + (mParams.scale * mIntrinsicIconSize) / 2;

        center[0] = (int) Math.round(offsetX);
        center[1] = (int) Math.round(offsetY);
        return mParams.scale;
    }

    private PreviewItemDrawingParams computePreviewItemDrawingParams(int index,
                                                                     PreviewItemDrawingParams params) {
        //M:liuzuo change the foldericon begin
        if (folderIconMode != FolderIconMode.One) {
            float transY = 0;
            float transX = 0;
            final int overlayAlpha = 0;
            if(index<NUM_ITEMS_IN_PREVIEW) {
                transX = paddingX + index % (getRow()) * (mItemInPreviewSize + middlePaddingX) ;
                transY = paddingY + index / (getRow()) * (mItemInPreviewSize + middlePaddingY) ;
            }else {
                transX = paddingX + mItemInPreviewSize / 2;
                transY = paddingY + mItemInPreviewSize / 2;
            }
            if (params == null) {
                params = new PreviewItemDrawingParams(transX, transY, scale,
                        overlayAlpha);
            } else {
                params.transX = transX;
                params.transY = transY;
                params.scale = scale;
                params.overlayAlpha = overlayAlpha;
            }
            return params;
        } else {
            //M:liuzuo change the foldericon end
            index = NUM_ITEMS_IN_PREVIEW - index - 1;
            float r = (index * 1.0f) / (NUM_ITEMS_IN_PREVIEW - 1);
            float scale = (1 - PERSPECTIVE_SCALE_FACTOR * (1 - r));

            float offset = (1 - r) * mMaxPerspectiveShift;
            float scaledSize = scale * mBaselineIconSize;
            float scaleOffsetCorrection = (1 - scale) * mBaselineIconSize;

            // We want to imagine our coordinates from the bottom left, growing up and to the
            // right. This is natural for the x-axis, but for the y-axis, we have to invert things.
            float transY = mAvailableSpaceInPreview - (offset + scaledSize + scaleOffsetCorrection) + getPaddingTop();
            float transX = (mAvailableSpaceInPreview - scaledSize) / 2;
            float totalScale = mBaselineIconScale * scale;
            final int overlayAlpha = (int) (80 * (1 - r));

            if (params == null) {
                params = new PreviewItemDrawingParams(transX, transY, totalScale, overlayAlpha);
            } else {
                params.transX = transX;
                params.transY = transY;
                params.scale = totalScale;
                params.overlayAlpha = overlayAlpha;
            }
            return params;
        }
    }
    private void drawPreviewItem(Canvas canvas, PreviewItemDrawingParams params) {
        canvas.save();
        canvas.translate(params.transX + mPreviewOffsetX, params.transY + mPreviewOffsetY);
        canvas.scale(params.scale, params.scale);
        Drawable d = params.drawable;

        if (d != null) {
            mOldBounds.set(d.getBounds());
            d.setBounds(0, 0, mIntrinsicIconSize, mIntrinsicIconSize);
            if (d instanceof FastBitmapDrawable) {
                FastBitmapDrawable fd = (FastBitmapDrawable) d;
                int oldBrightness = fd.getBrightness();
                fd.setBrightness(params.overlayAlpha);
                d.draw(canvas);
                fd.setBrightness(oldBrightness);
            } else {
                d.setColorFilter(Color.argb(params.overlayAlpha, 255, 255, 255),
                        PorterDuff.Mode.SRC_ATOP);
                d.draw(canvas);
                d.clearColorFilter();
            }
            d.setBounds(mOldBounds);
        }
        canvas.restore();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mFolder == null) return;
        if (mFolder.getItemCount() == 0 && !mAnimating) return;
        //M:liuzuo begin
        mFolder.mItemsInvalidated=true;
        //M:liuzuo end
        ArrayList<View> items = mFolder.getItemsInReadingOrder();
        Drawable d;
        TextView v;

        // Update our drawing parameters if necessary
        if (mAnimating) {
            computePreviewDrawingParams(mAnimParams.drawable);
        } else {
            v = (TextView) items.get(0);
            d = getTopDrawable(v);
            computePreviewDrawingParams(d);
        }

        int nItemsInPreview = Math.min(items.size(), NUM_ITEMS_IN_PREVIEW);
        if (!mAnimating) {
            for (int i = nItemsInPreview - 1; i >= 0; i--) {
                v = (TextView) items.get(i);
                if (!mHiddenItems.contains(v.getTag())&&!(v.getTag() instanceof FolderPagedView.FolderAddInfo)) {
                    d = getTopDrawable(v);
                    mParams = computePreviewItemDrawingParams(i, mParams);
                    mParams.drawable = d;
                    drawPreviewItem(canvas, mParams);
                }
            }
        } else {
            drawPreviewItem(canvas, mAnimParams);
        }
        boolean hadDrawImport = drawFolderImportIfNeed(canvas, this);//liuzuo
        //lijun add for unread
        if(!hadDrawImport){
            MonsterUnreadLoader.drawUnreadEventIfNeed(canvas, this,mLauncher);
        }
    }
   Paint mPaint;
   Paint mPaintNum;
    private boolean drawFolderImportIfNeed(Canvas canvas, FolderIcon folderIcon) {
        if(mFolder.mCheckedViews!=null&&mFolder.mCheckedViews.size()>0){
            int pointX = getScrollX() + (mTotalWidth-mAvailableSpaceInPreview)/2 + mAvailableSpaceInPreview;
            int offsetY= mFolderImportRadius < getItemDrawingParamOffsetY()+mOffsetYFolderBackground ? getItemDrawingParamOffsetY()+mOffsetYFolderBackground : mFolderImportRadius ;
            int pointY = getScrollY() + offsetY;
            if(mPaint==null) {
                mPaint = new Paint();
                mPaint.setColor(getResources().getColor(R.color.icon_check_background));
                mPaint.setStrokeWidth(3);
                mPaint.setAntiAlias(true);
            }
           Rect r= new Rect();
            if(mPaintNum==null){
                mPaintNum = new Paint();
                mPaintNum.setColor(getResources().getColor(R.color.icon_check_circle));
                mPaintNum.setStrokeWidth(3);
                mPaintNum.setAntiAlias(true);
                mPaintNum.setTextSize(getResources().getDimension(R.dimen.folder_import_icon_number_size));
            }
            Log.d("liuzuo81","drawCircle iconCenterX="+pointX+"  iconCenterY= "+pointY);
            String number = String.valueOf(mFolder.mCheckedViews.size());
            mPaintNum.getTextBounds(number,0,number.length(),r);
            int offsetX=1;
            if(mFolder.mCheckedViews.size()%10==1){
                offsetX=4;
            }
            canvas.drawCircle(pointX, pointY, mFolderImportRadius, mPaint);
            canvas.drawText(number,pointX-r.width()/2-offsetX,pointY+r.height()/2,mPaintNum);
            return true;
        }
        return false;
    }

    private Drawable getTopDrawable(TextView v) {
        Drawable d = v.getCompoundDrawables()[1];
        return (d instanceof PreloadIconDrawable) ? ((PreloadIconDrawable) d).mIcon : d;
    }

    private void animateFirstItem(final Drawable d, int duration, final boolean reverse,
            final Runnable onCompleteRunnable) {
        final PreviewItemDrawingParams finalParams = computePreviewItemDrawingParams(0, null);
        float iconSize = mLauncher.getDeviceProfile().iconSizePx;
        final float scale0 = iconSize / d.getIntrinsicWidth() ;
        final float transX0 = (mTotalWidth-iconSize ) / 2;
        final float transY0 = (mAvailableSpaceInPreview - iconSize) / 2+mOffsetYFolderBackground + getPaddingTop();
        mAnimParams.drawable = d;

        ValueAnimator va = LauncherAnimUtils.ofFloat(this, 0f, 1.0f);
        va.addUpdateListener(new AnimatorUpdateListener(){
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (Float) animation.getAnimatedValue();
                if (reverse) {
                    progress = 1 - progress;
                    mPreviewBackground.setAlpha(progress);
                }

                mAnimParams.transX = transX0 + progress * (finalParams.transX - transX0);
                mAnimParams.transY = transY0 + progress * (finalParams.transY - transY0);
                mAnimParams.scale = scale0 + progress * (finalParams.scale - scale0);
                invalidate();
            }
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                if (onCompleteRunnable != null) {
                    onCompleteRunnable.run();
                }
            }
        });
        va.setDuration(duration);
        va.start();
    }

    public void setTextVisible(boolean visible) {
        if (visible) {
            mFolderName.setVisibility(VISIBLE);
        } else {
            mFolderName.setVisibility(INVISIBLE);
        }
    }

    public boolean getTextVisible() {
        return mFolderName.getVisibility() == VISIBLE;
    }

    public void onItemsChanged() {
        invalidate();
        requestLayout();
    }

    public void onAdd(ShortcutInfo item) {
        //lijun add start for unread
        final ComponentName componentName = item.intent.getComponent();
        updateFolderUnreadNum(componentName, item.unreadNum,item.user.getUser());
        //lijun add end
        invalidate();
        requestLayout();
    }

    public void onRemove(ShortcutInfo item) {
        //lijun add start for unread
        final ComponentName componentName = item.intent.getComponent();
        updateFolderUnreadNum(componentName, item.unreadNum,item.user.getUser());
        //lijun add end
        invalidate();
        requestLayout();
    }

    public void onTitleChanged(CharSequence title) {
        mFolderName.setText(title);
        setContentDescription(String.format(getContext().getString(R.string.folder_name_format),
                title));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Call the superclass onTouchEvent first, because sometimes it changes the state to
        // isPressed() on an ACTION_UP
        boolean result = super.onTouchEvent(event);

        // Check for a stylus button press, if it occurs cancel any long press checks.
        if (mStylusEventHelper.checkAndPerformStylusEvent(event)) {
            mLongPressHelper.cancelLongPress();
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLongPressHelper.postCheckForLongPress();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mLongPressHelper.cancelLongPress();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!Utilities.pointInView(this, event.getX(), event.getY(), mSlop)) {
                    mLongPressHelper.cancelLongPress();
                }
                break;
        }
        return result;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        if(mFolder.getContent()!=null&&mFolder.getContent() instanceof IChangeLauncherColor){
            mLauncher.addChangeLauncherColorCallback((IChangeLauncherColor)(mFolder.getContent()));
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mFolder.getContent()!=null&&mFolder.getContent() instanceof IChangeLauncherColor){
            mLauncher.removeChangeLauncherColorCallback((IChangeLauncherColor)(mFolder.getContent()));
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        mLongPressHelper.cancelLongPress();
    }
    //M:liuzuo get the row of the folderIconMode begin
    public int getRow() {
        int row = (mAvailableSpaceInPreview - paddingNum * getResources().getDimensionPixelSize(R.dimen.folder_preview_IconItemPadding))/mItemInPreviewSize ;
        return row;
    }
    //M:liuzuo get the row of the folderIconMode end

    //M:liuzuo add the folderImportMode begin
    @Override
    public void onAddInfo(ArrayList<ShortcutInfo> items) {
        invalidate();
        requestLayout();
    }

    @Override
    public void onRemoveInfo(ArrayList<ShortcutInfo> items) {

    }

    @Override
    public void clearInfo() {
        invalidate();
    }
    public void removeInfo() {
        invalidate();
    }

    //M:liuzuo add the folderImportMode end


    /**
     * lijun add for unread
     * Update the unread message number of the shortcut with the given value.
     *
     * @param unreadNum the number of the unread message.
     */
    public void setFolderUnreadNum(int unreadNum) {
        if (Log.DEBUG_UNREAD) {
            Log.d(TAG, "setFolderUnreadNum: unreadNum = " + unreadNum + ", mInfo = " + mInfo
                    + ", this = " + this);
        }

        if (unreadNum <= 0) {
            mInfo.unreadNum = 0;
        } else {
            mInfo.unreadNum = unreadNum;
        }
    }

    /**
     * lijun add for unread
     * Update unread number of the folder, the number is the total unread number
     * of all shortcuts in folder, duplicate shortcut will be only count once.
     */
    public void updateFolderUnreadNum() {
        final ArrayList<ShortcutInfo> contents = mInfo.contents;
        final int contentsCount = contents.size();
        int unreadNumTotal = 0;
        final ArrayList<ComponentName> components = new ArrayList<ComponentName>();
        ShortcutInfo shortcutInfo = null;
        ComponentName componentName = null;
        int unreadNum = 0;
        for (int i = 0; i < contentsCount; i++) {
            shortcutInfo = contents.get(i);
            componentName = shortcutInfo.intent.getComponent();
            UserHandle user = null;
            if(shortcutInfo.user!=null){
                user = shortcutInfo.user.getUser();
            }else {
                user = UserHandleCompat.myUserHandle().getUser();
            }
            unreadNum = MonsterUnreadLoader.getUnreadNumberOfComponent(componentName,user);
            if (unreadNum > 0) {
                shortcutInfo.unreadNum = unreadNum;
                int j = 0;
                for (j = 0; j < components.size(); j++) {
                    if (componentName != null && componentName.equals(components.get(j))) {
                        break;
                    }
                }
                if (Log.DEBUG_UNREAD) {
                    Log.d(TAG, "updateFolderUnreadNum: unreadNumTotal = " + unreadNumTotal
                            + ", j = " + j + ", components.size() = " + components.size());
                }
                if (j >= components.size()) {
                    components.add(componentName);
                    unreadNumTotal += unreadNum;
                }
            }
        }
        if (Log.DEBUG_UNREAD) {
            Log.d(TAG, "updateFolderUnreadNum 1 end: unreadNumTotal = " + unreadNumTotal);
        }
        setFolderUnreadNum(unreadNumTotal);
    }

    /**
     * lijun add for unread
     * Update the unread message of the shortcut with the given information.
     *
     * @param unreadNum the number of the unread message.
     */
    public void updateFolderUnreadNum(ComponentName component, int unreadNum, UserHandle user) {
        final ArrayList<ShortcutInfo> contents = mInfo.contents;
        final int contentsCount = contents.size();
        int unreadNumTotal = 0;
        ShortcutInfo appInfo = null;
        ComponentName name = null;
        final ArrayList<ComponentName> components = new ArrayList<ComponentName>();
        for (int i = 0; i < contentsCount; i++) {
            appInfo = contents.get(i);
            name = appInfo.intent.getComponent();
            final UserHandleCompat us = appInfo.user;
            boolean isCurrentUser = true;
            if (user != null && us != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                isCurrentUser = us.getUser().equals(user);
            }
            if (name != null && name.equals(component) && isCurrentUser) {
                appInfo.unreadNum = unreadNum;
            }
            if (appInfo.unreadNum > 0) {
                int j = 0;
                for (j = 0; j < components.size(); j++) {
                    if (name != null && name.equals(components.get(j))) {
                        break;
                    }
                }
                if (Log.DEBUG_UNREAD) {
                    Log.d(TAG, "updateFolderUnreadNum: unreadNumTotal = " + unreadNumTotal
                            + ", j = " + j + ", components.size() = " + components.size());
                }
                if (j >= components.size()) {
                    components.add(name);
                    unreadNumTotal += appInfo.unreadNum;
                }
            }
        }
        if (Log.DEBUG_UNREAD) {
            Log.d(TAG, "updateFolderUnreadNum 2 end: unreadNumTotal = " + unreadNumTotal);
        }
        setFolderUnreadNum(unreadNumTotal);
    }
    //liuzuo begin

    private int getItemDrawingParamOffsetY() {
        DeviceProfile profile = mLauncher.getDeviceProfile();
        if(mInfo.container==LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
            return profile.getFolderIconOffsetYFromHotseat();
        } else {
            return profile.getFolderIconOffsetY();
        }
    }

    //liuzuo end
}
