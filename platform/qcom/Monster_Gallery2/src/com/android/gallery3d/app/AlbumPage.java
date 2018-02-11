/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.gallery3d.app;

import mst.widget.ActionMode;
import mst.widget.ActionMode.Item;
import mst.widget.ActionModeListener;
import mst.widget.toolbar.Toolbar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.gallery3d.R;
import com.android.gallery3d.app.SystemUIManager.SystemUIFlagChangeListener;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.DateGroupInfos;
import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.MyEnumerateListener;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.filtershow.crop.CropActivity;
import com.android.gallery3d.filtershow.crop.CropExtras;
import com.android.gallery3d.glrenderer.CustomStringTexture;
import com.android.gallery3d.glrenderer.FadeTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.ui.ActionModeHandler;
//import com.android.gallery3d.ui.ActionModeHandler.ActionModeListener;
import com.android.gallery3d.ui.AlbumSlotRenderer;
import com.android.gallery3d.ui.DetailsHelper;
import com.android.gallery3d.ui.DetailsHelper.CloseListener;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.GifTexture;
import com.android.gallery3d.ui.MstToolBarListener;
import com.android.gallery3d.ui.PhotoFallbackEffect;
import com.android.gallery3d.ui.PhotoView;
import com.android.gallery3d.ui.RelativePosition;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SlotView;
import com.android.gallery3d.ui.SlotView.SelectionStatusGetter;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.util.Future;
import com.android.gallery3d.util.FutureListener;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import com.android.gallery3d.util.LogUtil;
import com.android.gallery3d.ui.GLRootView;

public class AlbumPage extends ActivityState implements
        GalleryActionBar.ClusterRunner, SelectionManager.SelectionListener,
        /*MediaSet.SyncListener, */GalleryActionBar.OnAlbumModeSelectedListener,
        MyEnumerateListener, SelectionStatusGetter, ActionModeListener, MstToolBarListener, 
        PhotoView.AlbumPageSlotPositionProvider/*, SystemUIFlagChangeListener*/ {
    @SuppressWarnings("unused")
    private static final String TAG = "AlbumPage";

    public static final String KEY_MEDIA_PATH = "media-path";
    public static final String KEY_PARENT_MEDIA_PATH = "parent-media-path";
    public static final String KEY_SET_CENTER = "set-center";
    public static final String KEY_AUTO_SELECT_ALL = "auto-select-all";
    public static final String KEY_SHOW_CLUSTER_MENU = "cluster-menu";
    public static final String KEY_EMPTY_ALBUM = "empty-album";
    public static final String KEY_RESUME_ANIMATION = "resume_animation";

    private static final int REQUEST_SLIDESHOW = 1;
    public static final int REQUEST_PHOTO = 2;
    private static final int REQUEST_DO_ANIMATION = 3;

    private static final int BIT_LOADING_RELOAD = 1;
    // TCL ShenQianfeng Begin on 2016.11.09
    // Annotated Below:
    //private static final int BIT_LOADING_SYNC = 2;
    // TCL ShenQianfeng End on 2016.11.09
    private static final float USER_DISTANCE_METER = 0.3f;

    private boolean mIsActive = false;
    private AlbumSlotRenderer mAlbumView;
    private Path mMediaSetPath;
    private String mParentMediaSetString;
    private SlotView mSlotView;

    private AlbumDataLoader mAlbumDataAdapter;

    protected SelectionManager mSelectionManager;

    private boolean mGetContent;
    private boolean mShowClusterMenu;

    private ActionModeHandler mActionModeHandler;
    private int mFocusIndex = 0;
    private DetailsHelper mDetailsHelper;
    private MyDetailsSource mDetailsSource;
    private MediaSet mMediaSet;
    private boolean mShowDetails;
    private float mUserDistance; // in pixel
    // TCL ShenQianfeng Begin on 2016.11.09
    // Annotated Below:
    //private Future<Integer> mSyncTask = null;
    // TCL ShenQianfeng End on 2016.11.09
    
    private boolean mLaunchedFromPhotoPage;
    private boolean mInCameraApp;
    private boolean mInCameraAndWantQuitOnPause;

    private int mLoadingBits = 0;
    private boolean mInitialSynced = false;
    private int mSyncResult;
    private boolean mLoadingFailed;
    private RelativePosition mOpenCenter = new RelativePosition();

    private Handler mHandler;
    private static final int MSG_PICK_PHOTO = 0;

    // TCL ShenQianfeng Begin on 2016.07.18
    private static final int MSG_LOAD_WHOLE_BITMAP_THUMBNAIL_DONE = 1;
    //private static final int MSG_ZOOM_IN_END = 2;
    //private static final int MSG_ZOOM_OUT_END = 3;
    
    private static final int MSG_REBUILD_TOOL_BAR = 4;
    private boolean mEmbeddedRebuilt;

    private int mSingleTapIndex = -1;
    // TCL ShenQianfeng End on 2016.07.18

    // TCL ShenQianfeng Begin on 2016.07.08
    //private SelectDeletePopupWindow mSelectDeletePopupWindow;
    // TCL ShenQianfeng End on 2016.07.08

    // TCL ShenQianfeng Begin on 2016.07.18
    //private ZoomAnimation mZoomAnimation; // may be zoom in or zoom out
    //private PhotoZoomManager mZoomManager;
    // TCL ShenQianfeng End on 2016.07.18
    
    // TCL ShenQianfeng Begin on 2016.09.08
    private PhotoPage mEmbeddedPhotoPage;
    // private Rect mTempRect = new Rect();
    // TCL ShenQianfeng End on 2016.09.08
    

    private PhotoFallbackEffect mResumeEffect;
    private PhotoFallbackEffect.PositionProvider mPositionProvider = new PhotoFallbackEffect.PositionProvider() {

        @Override
        public Rect getPosition(int index) {
            Rect rect = mSlotView.getSlotRect(index);
            Rect bounds = mSlotView.bounds();
            rect.offset(bounds.left - mSlotView.getScrollX(), bounds.top
                    - mSlotView.getScrollY());
            return rect;
        }

        @Override
        public int getItemIndex(Path path) {
            int start = mSlotView.getVisibleStart();
            int end = mSlotView.getVisibleEnd();
            for (int i = start; i < end; ++i) {
                MediaItem item = mAlbumDataAdapter.get(i);
                if (item != null && item.getPath() == path)
                    return i;
            }
            return -1;
        }
    };

    @Override
    protected int getBackgroundColorId() {
        // TCL ShenQianfeng Begin on 2016.06.30
        // Original:
        // return R.color.album_background;
        // Modify To:
        return R.color.mst_default_background;
        // TCL ShenQianfeng End on 2016.06.30
    }

    private final GLView mRootPane = new GLView() {
        private final float mMatrix[] = new float[16];

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

            int slotViewTop = mActivity.getGalleryActionBar().getHeight();
            int slotViewBottom = bottom - top;
            int slotViewRight = right - left;

            if (mShowDetails) {
                mDetailsHelper.layout(left, slotViewTop, right, bottom);
            } else {
                mAlbumView.setHighlightItemPath(null);
            }
            // Set the mSlotView as a reference point to the open animation
            mOpenCenter.setReferencePosition(0, slotViewTop);
            mSlotView.layout(0, slotViewTop, slotViewRight, slotViewBottom);
            
            // TCL ShenQianfeng Begin on 2016.09.08
            if(mEmbeddedPhotoPage != null) {
                mEmbeddedPhotoPage.layoutEmbedded(left, top, right, bottom);
            }
            // TCL ShenQianfeng End on 2016.09.08
            
            GalleryUtils.setViewPointMatrix(mMatrix, (right - left) / 2, (bottom - top) / 2, -mUserDistance);
        }

        @Override
        protected void render(GLCanvas canvas) {
            canvas.save(GLCanvas.SAVE_FLAG_MATRIX);
            canvas.multiplyMatrix(mMatrix, 0);
            super.render(canvas);
            // TCL ShenQianfeng Begin on 2016.07.19
            /*
             * if (mResumeEffect != null) { boolean more =
             * mResumeEffect.draw(canvas); if (!more) { mResumeEffect = null;
             * mAlbumView.setSlotFilter(null); } // We want to render one more
             * time even when no more effect // required. So that the animated
             * thumbnails could be draw // with declarations in super.render().
             * invalidate(); }
             */
            // TCL ShenQianfeng End on 2016.07.19
            canvas.restore();
        }
        
        @Override
        public void onWindowsInsetsChanged(Rect newWindowInsets) {
            if(mActionModeHandler != null) {
                mActionModeHandler.updateBottomBarPaddingBottomWhenInsetsChanged(newWindowInsets);
            }
            for(int i = 0; i < getComponentCount(); i++) {
                GLView view = getComponent(i);
                view.onWindowsInsetsChanged(newWindowInsets);
            }
        }
    };
    
    // TCL ShenQianfeng Begin on 2016.09.08
    public GLView getRootPane() {
        return mRootPane;
    }
    // TCL ShenQianfeng End on 2016.09.08
    
    // This are the transitions we want:
    //
    // +--------+ +------------+ +-------+ +----------+
    // | Camera |---------->| Fullscreen |--->| Album |--->| AlbumSet |
    // | View | thumbnail | Photo | up | Page | up | Page |
    // +--------+ +------------+ +-------+ +----------+
    // ^ | | ^ |
    // | | | | | close
    // +----------back--------+ +----back----+ +--back-> app
    //
    @Override
    protected void onBackPressed() {
        //LogUtil.d(TAG, "AlbumPage::onBackPressed");
        // TCL ShenQianfeng Begin on 2016.09.18
        if(mEmbeddedPhotoPage != null && mEmbeddedPhotoPage.isEmbedded() && mEmbeddedPhotoPage.isShowingEmbedded()) {
            mEmbeddedPhotoPage.onBackPressed();
            return;
        }
        // TCL ShenQianfeng End on 2016.09.18
        
        if (mShowDetails) {
            hideDetails();
        } else if (mSelectionManager.inSelectionMode()) {
            mSelectionManager.leaveSelectionMode();
        } else {
            if (mLaunchedFromPhotoPage) {
                mActivity.getTransitionStore().putIfNotPresent(
                        PhotoPage.KEY_ALBUMPAGE_TRANSITION,
                        PhotoPage.MSG_ALBUMPAGE_RESUMED);
            }
            // TODO: fix this regression
            // mAlbumView.savePositions(PositionRepository.getInstance(mActivity));
            if (mInCameraApp) {
                super.onBackPressed();
            } else {
                onUpPressed();
            }
        }
    }

    private void onUpPressed() {
        if (mInCameraApp) {
            GalleryUtils.startGalleryActivity(mActivity);
        } else if (mActivity.getStateManager().getStateCount() > 1) {
            super.onBackPressed();
        } else if (mParentMediaSetString != null) {
            Bundle data = new Bundle(getData());
            data.putString(AlbumSetPage.KEY_MEDIA_PATH, mParentMediaSetString);
            mActivity.getStateManager().switchState(this, AlbumSetPage.class,
                    data);
        }
        // TCL ShenQianfeng Begin on 2016.07.20
        else {
            super.onBackPressed();
        }
        // TCL ShenQianfeng End on 2016.07.20
    }

    // TCL ShenQianfeng Begin on 2016.06.27

    private Size getScreenSize() {
        WindowManager windowManager = mActivity.getWindowManager();
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        return new Size(width, height);
    }

    @Override
    protected void onConfigurationChanged(Configuration config) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(config);
        mSlotView.onConfigurationChanged(config);
        Size screenSize = getScreenSize();
        mRootPane.layout(0, 0, screenSize.getWidth(), screenSize.getHeight());
        if(mEmbeddedPhotoPage != null && mEmbeddedPhotoPage.isActive()) {
            mEmbeddedPhotoPage.onConfigurationChanged(config);
        } else {
            SystemUIManager systemUIManager = mActivity.getSystemUIManager();
            boolean lightsOut = false;
            boolean occupyNavigationBar = mActivity.isPortrait();
            int navigationBarColor = Color.WHITE;
            systemUIManager.setFlag(lightsOut, occupyNavigationBar, navigationBarColor);
        }
        if(null != mActionModeHandler) {
            mActionModeHandler.onConfigurationChanged(config);
        }
    }

    // TCL ShenQianfeng End on 2016.06.27

    private void onDown(int index) {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Original:
        // mAlbumView.setPressedIndex(index);
        // Modify To:
        if(mSelectionManager != null && ! mSelectionManager.inSelectionMode()) {
            mAlbumView.setPressedIndex(index);
        }
    }

    private void onUp(boolean followedByLongPress) {
        if (followedByLongPress) {
            // Avoid showing press-up animations for long-press.
            mAlbumView.setPressedIndex(-1);
        } else {
            mAlbumView.setPressedUp();
        }
    }

    private void onSingleTapUp(int slotIndex) {
        if (!mIsActive)
            return;

        if (mSelectionManager.inSelectionMode()) {
            MediaItem item = mAlbumDataAdapter.get(slotIndex);
            if (item == null)
                return; // Item not ready yet, ignore the click
            // TCL BaiYuan Begin on 2016.11.02
            // Original:
            /*
            mSelectionManager.toggle(item.getPath());
            */
            // Modify To:
            mSelectionManager.toggle(item);
            // TCL BaiYuan End on 2016.11.02
            mSlotView.invalidate();
        } else {
            // Render transition in pressed state
            mAlbumView.setPressedIndex(slotIndex);
            // TCL ShenQianfeng Begin on 2016.09.26
            // Annotated Below:
            // mAlbumView.setPressedUp();
            // TCL ShenQianfeng End on 2016.09.26
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_PHOTO, slotIndex, 0), FadeTexture.DURATION);
        }
    }

    private void pickPhoto(int slotIndex) {
        if(mEmbeddedPhotoPage != null && 
                mEmbeddedPhotoPage.isEmbedded() && 
                mEmbeddedPhotoPage.isZoomAnimating()) {
            return;
        }
        pickPhoto(slotIndex, false);
    }

    // TCL ShenQianfeng Begin on 2016.07.18
    public class LoadWholeBitmapTask implements Job<Bitmap> {

        private MediaItem mMediaItem;

        public LoadWholeBitmapTask(MediaItem item) {
            mMediaItem = item;
        }

        @Override
        public Bitmap run(JobContext jc) {
            if (mMediaItem == null)
                return null;
            return mMediaItem.requestImage(MediaItem.TYPE_THUMBNAIL).run(jc);
        }

    }

    private void getWholeBitmapToZoom(final MediaItem item, final int slotIndex) {
        if (item == null) return;
        LoadWholeBitmapTask task = new LoadWholeBitmapTask(item);
        ThreadPool threadPool = mActivity.getThreadPool();
        threadPool.submitImmediately(task, new FutureListener<Bitmap>() {
                    @Override
                    public void onFutureDone(Future<Bitmap> future) {
                        Bitmap bitmap = future.get();
                        if (null == bitmap) {
                            return;
                        }
                        mHandler.obtainMessage(MSG_LOAD_WHOLE_BITMAP_THUMBNAIL_DONE, slotIndex, 0, bitmap).sendToTarget();
                    }
                });
    }
    // TCL ShenQianfeng End on 2016.07.18

    private void pickPhoto(int slotIndex, boolean startInFilmstrip) {
        if (!mIsActive)
            return;

        if (!startInFilmstrip) {
            // Launch photos in lights out mode
            // mActivity.getGLRoot().setLightsOutMode(true);
        }

        MediaItem item = mAlbumDataAdapter.get(slotIndex);
        if (item == null)
            return; // Item not ready yet, ignore the click
        if (mGetContent) {
            onGetContent(item);
        } else if (mLaunchedFromPhotoPage) {
            TransitionStore transitions = mActivity.getTransitionStore();
            transitions.put(PhotoPage.KEY_ALBUMPAGE_TRANSITION, PhotoPage.MSG_ALBUMPAGE_PICKED);
            transitions.put(PhotoPage.KEY_INDEX_HINT, slotIndex);
            onBackPressed();
        } else {
            // Get into the PhotoPage.
            // mAlbumView.savePositions(PositionRepository.getInstance(mActivity));
            /*
             * Bundle data = new Bundle(); data.putInt(PhotoPage.KEY_INDEX_HINT,
             * slotIndex); data.putParcelable(PhotoPage.KEY_OPEN_ANIMATION_RECT,
             * mSlotView.getSlotRect(slotIndex, mRootPane));
             * data.putString(PhotoPage.KEY_MEDIA_SET_PATH,
             * mMediaSetPath.toString());
             * data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH,
             * item.getPath().toString());
             * data.putInt(PhotoPage.KEY_ALBUMPAGE_TRANSITION,
             * PhotoPage.MSG_ALBUMPAGE_STARTED);
             * data.putBoolean(PhotoPage.KEY_START_IN_FILMSTRIP,
             * startInFilmstrip); data.putBoolean(PhotoPage.KEY_IN_CAMERA_ROLL,
             * mMediaSet.isCameraRoll()); if (startInFilmstrip) {
             * mActivity.getStateManager().switchState(this,
             * FilmstripPage.class, data); } else {
             * mActivity.getStateManager().startStateForResult
             * (SinglePhotoPage.class, REQUEST_PHOTO, data); }
             */
            /*
            if (mZoomAnimation != null && mZoomAnimation.isActive()) {
                return;
            }
            */
            getWholeBitmapToZoom(item, slotIndex);
        }
    }

    private void onGetContent(final MediaItem item) {
        DataManager dm = mActivity.getDataManager();
        Activity activity = mActivity;
        if (mData.getString(GalleryActivity.EXTRA_CROP) != null) {
            Uri uri = dm.getContentUri(item.getPath());
            Intent intent = new Intent(CropActivity.CROP_ACTION, uri).addFlags(
                    Intent.FLAG_ACTIVITY_FORWARD_RESULT).putExtras(getData());
            if (mData.getParcelable(MediaStore.EXTRA_OUTPUT) == null) {
                intent.putExtra(CropExtras.KEY_RETURN_DATA, true);
            }
            activity.startActivity(intent);
            activity.finish();
        } else {
            Intent intent = new Intent(null, item.getContentUri())
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
        }
    }

    public void onLongTap(int slotIndex) {
        if (mGetContent)
            return;
        MediaItem item = mAlbumDataAdapter.get(slotIndex);
        if (item == null)
            return;
        // TCL BaiYuan Begin on 2016.11.02
        // Original:
        /*
        mSelectionManager.setAutoLeaveSelectionMode(true);
        mSelectionManager.toggle(item.getPath());
        */
        // Modify To:
        mSelectionManager.toggle(item);
        // TCL BaiYuan End on 2016.11.02
        mSlotView.invalidate();
    }

    @Override
    public void doCluster(int clusterType) {
        String basePath = mMediaSet.getPath().toString();
        String newPath = FilterUtils.newClusterPath(basePath, clusterType);
        Bundle data = new Bundle(getData());
        data.putString(AlbumSetPage.KEY_MEDIA_PATH, newPath);
        if (mShowClusterMenu) {
            Context context = mActivity.getAndroidContext();
            data.putString(AlbumSetPage.KEY_SET_TITLE, mMediaSet.getName());
            data.putString(AlbumSetPage.KEY_SET_SUBTITLE, GalleryActionBar
                    .getClusterByTypeString(context, clusterType));
        }

        // mAlbumView.savePositions(PositionRepository.getInstance(mActivity));
        mActivity.getStateManager().startStateForResult(AlbumSetPage.class,
                REQUEST_DO_ANIMATION, data);
    }
    
    public class AlbumPageSynchronizedHandler extends SynchronizedHandler {
        public AlbumPageSynchronizedHandler(GLRoot root) {
            super(root);
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
            case MSG_PICK_PHOTO: {
                pickPhoto(message.arg1);
                break;
            }
            case MSG_LOAD_WHOLE_BITMAP_THUMBNAIL_DONE: {
                int slotIndex = mSingleTapIndex;
                Bitmap bitmap = (Bitmap) message.obj;
                if (slotIndex == -1) {
                    return;
                }
                MediaItem item = mAlbumDataAdapter.get(slotIndex);
                /*
                Bundle data = new Bundle();
                data.putInt(PhotoPage.KEY_INDEX_HINT, slotIndex);
                data.putParcelable(PhotoPage.KEY_OPEN_ANIMATION_RECT, mSlotView.getSlotRect(slotIndex, mRootPane));
                data.putString(PhotoPage.KEY_MEDIA_SET_PATH, mMediaSetPath.toString());
                data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH, item.getPath().toString());
                data.putInt(PhotoPage.KEY_ALBUMPAGE_TRANSITION, PhotoPage.MSG_ALBUMPAGE_STARTED);
                boolean startInFilmstrip = false;// ShenQianfeng add on 2016.07.19
                data.putBoolean(PhotoPage.KEY_START_IN_FILMSTRIP, startInFilmstrip);
                data.putBoolean(PhotoPage.KEY_IN_CAMERA_ROLL, mMediaSet.isCameraRoll());

                data.putParcelable(PhotoPage.KEY_ENTERING_BITMAP, bitmap);
                data.putInt(PhotoPage.KEY_ENTERING_BITMAP_ROTATION, item.getRotation());
                data.putInt(PhotoPage.KEY_ENTERING_BITMAP_INDEX, slotIndex);
                //mEmbeddedPhotoPage.createEmbedded(AlbumPage.this, data);
                 */
                
                Path path = item .getPath();
                PhotoPage.EnteringBitmapInfo enteringBitmapInfo = new PhotoPage.EnteringBitmapInfo();
                enteringBitmapInfo.bitmap = bitmap;
                enteringBitmapInfo.index = slotIndex;
                enteringBitmapInfo.totalCount = mAlbumDataAdapter.size();
                enteringBitmapInfo.rotation = item.getRotation();
                enteringBitmapInfo.isImage = item.getMediaType() == MediaObject.MEDIA_TYPE_IMAGE;
                enteringBitmapInfo.isGif = GifTexture.isGif(item.getFilePath());
                mEmbeddedPhotoPage.setEnteringBitmapInfo(enteringBitmapInfo);
                
                //mEmbeddedPhotoPage.setEnteringBitmapInfo(bitmap, slotIndex, mAlbumDataAdapter.size(),  item.getRotation());
                mEmbeddedPhotoPage.setSlotRect(mSlotView.getSlotRect(slotIndex, mRootPane));
                mEmbeddedPhotoPage.showEmbedded(true);
                mEmbeddedPhotoPage.goToIndex(path, slotIndex);
                mEmbeddedPhotoPage.buildZoomInAnimation();
                mEmbeddedPhotoPage.initControlStatusWhenZoomingInStart();
                break;
            }
            /*
            case MSG_ZOOM_IN_END: {
                int slotIndex = mSingleTapIndex;
                Bitmap bitmap = mZoomManager.getBitmap();
                if (slotIndex == -1) {
                    return;
                }
                MediaItem item = mAlbumDataAdapter.get(slotIndex);
                Bundle data = new Bundle();
                data.putInt(PhotoPage.KEY_INDEX_HINT, slotIndex);
                data.putParcelable(PhotoPage.KEY_OPEN_ANIMATION_RECT, mSlotView.getSlotRect(slotIndex, mRootPane));
                data.putString(PhotoPage.KEY_MEDIA_SET_PATH, mMediaSetPath.toString());
                data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH, item .getPath().toString());
                data.putInt(PhotoPage.KEY_ALBUMPAGE_TRANSITION, PhotoPage.MSG_ALBUMPAGE_STARTED);
                boolean startInFilmstrip = false;// ShenQianfeng add on 2016.07.19
                data.putBoolean(PhotoPage.KEY_START_IN_FILMSTRIP, startInFilmstrip);
                data.putBoolean(PhotoPage.KEY_IN_CAMERA_ROLL, mMediaSet.isCameraRoll());

                data.putParcelable(PhotoPage.KEY_ENTERING_BITMAP, bitmap);
                data.putInt(PhotoPage.KEY_ENTERING_BITMAP_ROTATION, item.getRotation());
                data.putInt(PhotoPage.KEY_ENTERING_BITMAP_INDEX, slotIndex);

                if (startInFilmstrip) {
                    mActivity.getStateManager().switchState(AlbumPage.this, FilmstripPage.class, data);
                } else {
                    mActivity.getStateManager().startStateForResult(PhotoPage.class, REQUEST_PHOTO, data);
                }
                break;
            }
            */
            // TCL ShenQianfeng Begin on 2016.09.18
            case MSG_REBUILD_TOOL_BAR: {
                if(! mEmbeddedRebuilt) {
                    //LogUtil.d("ZoomOut", "MSG_REBUILD_TOOL_BAR =======handleMessage");
                    rebuildToolBar();
                    GalleryActionBar actionBar = mActivity.getGalleryActionBar();
                    actionBar.setToolbarChildViewsAlpha(actionBar.getChildViewAlpha());
                    showToolBar(); //without animation
                    mEmbeddedRebuilt = true;
                }
                break;
            }
            // TCL ShenQianfeng End on 2016.09.18
            default:
                throw new AssertionError(message.what);
            }
        }
    }
    
    // TCL ShenQianfeng Begin on 2016.09.18
    public void notifyToRebuildToolbar() {
        mHandler.obtainMessage(MSG_REBUILD_TOOL_BAR).sendToTarget();
    }
    
    public void resetRebuildFlag() {
        mEmbeddedRebuilt = false;
    }

    public boolean getRebuildFlag() {
        return mEmbeddedRebuilt;
    }
    // TCL ShenQianfeng End on 2016.09.18

    @Override
    protected void onCreate(Bundle data, Bundle restoreState) {
        super.onCreate(data, restoreState);
        mUserDistance = GalleryUtils.meterToPixel(USER_DISTANCE_METER);
        initializeViews();
        initializeData(data);
        mGetContent = data.getBoolean(GalleryActivity.KEY_GET_CONTENT, false);
        mShowClusterMenu = data.getBoolean(KEY_SHOW_CLUSTER_MENU, false);
        mDetailsSource = new MyDetailsSource();
        //Context context = mActivity.getAndroidContext();

        if (data.getBoolean(KEY_AUTO_SELECT_ALL)) {
            mSelectionManager.selectAll();
        }
        mLaunchedFromPhotoPage = mActivity.getStateManager().hasStateClass(FilmstripPage.class);
        mInCameraApp = data.getBoolean(PhotoPage.KEY_APP_BRIDGE, false);
        mHandler = new AlbumPageSynchronizedHandler(mActivity.getGLRoot());
        
        // TCL ShenQianfeng Begin on 2016.08.11
        rebuildToolBar();
        // TCL ShenQianfeng End on 2016.08.11
        
        // TCL ShenQianfeng Begin on 2016.09.09
        createEmbeddedPhotoPage();
        // TCL ShenQianfeng End on 2016.09.09
    }
    
    private void createEmbeddedPhotoPage() {
        Bundle embeddedData = new Bundle();
        //embeddedData.putInt(PhotoPage.KEY_INDEX_HINT, slotIndex);
        //embeddedData.putParcelable(PhotoPage.KEY_OPEN_ANIMATION_RECT, mSlotView.getSlotRect(slotIndex, mRootPane));
        embeddedData.putString(PhotoPage.KEY_MEDIA_SET_PATH, mMediaSetPath.toString());
        //embeddedData.putString(PhotoPage.KEY_MEDIA_ITEM_PATH, item .getPath().toString());
        //embeddedData.putInt(PhotoPage.KEY_ALBUMPAGE_TRANSITION, PhotoPage.MSG_ALBUMPAGE_STARTED);
        //boolean startInFilmstrip = false;// ShenQianfeng add on 2016.07.19
        //embeddedData.putBoolean(PhotoPage.KEY_START_IN_FILMSTRIP, startInFilmstrip);
        embeddedData.putBoolean(PhotoPage.KEY_IN_CAMERA_ROLL, mMediaSet.isCameraRoll());
        /*
        embeddedData.putParcelable(PhotoPage.KEY_ENTERING_BITMAP, bitmap);
        embeddedData.putInt(PhotoPage.KEY_ENTERING_BITMAP_ROTATION, item.getRotation());
        embeddedData.putInt(PhotoPage.KEY_ENTERING_BITMAP_INDEX, slotIndex);
        */
        mEmbeddedPhotoPage = new PhotoPage();
        mEmbeddedPhotoPage.createEmbedded(this, embeddedData);
        mEmbeddedPhotoPage.setAlbumPageSlotPositionProvider(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
        SystemUIManager systemUIManager = mActivity.getSystemUIManager();
        systemUIManager.registerSystemUIFlagChangeListener(this);
        */
        if(mEmbeddedPhotoPage != null && mEmbeddedPhotoPage.isShowingEmbedded()) {
            mEmbeddedPhotoPage.modifySystemUIWhenResume();
        } else {
            SystemUIManager systemUIManager = mActivity.getSystemUIManager();
            boolean isPortrait = mActivity.isPortrait();
            systemUIManager.setFlag(false/*lightsOut*/, isPortrait ? true : false /*occupyNavigationBar*/, Color.WHITE);
        }
        mIsActive = true;
        mResumeEffect = mActivity.getTransitionStore().get(KEY_RESUME_ANIMATION);
        if (mResumeEffect != null) {
            mAlbumView.setSlotFilter(mResumeEffect);
            mResumeEffect.setPositionProvider(mPositionProvider);
            mResumeEffect.start();
        }
        setContentPane(mRootPane);
        
        // TCL ShenQianfeng Begin on 2016.11.15
        // Annotated Below:
        /*
        boolean enableHomeButton = (mActivity.getStateManager().getStateCount() > 1) | mParentMediaSetString != null;
        GalleryActionBar actionBar = mActivity.getGalleryActionBar();
        actionBar.setDisplayOptions(enableHomeButton, true);
        */
        // TCL ShenQianfeng End on 2016.11.15

        /*
        SystemUIManager systemUIManager = mActivity.getSystemUIManager();
        boolean lightsOut = false;
        boolean occupyNavigationBar = mActivity.isPortrait();
        int navigationBarColor = Color.WHITE;
        systemUIManager.setFlag(lightsOut, occupyNavigationBar, navigationBarColor);
        */
        
        // TCL ShenQianfeng Begin on 2016.07.04
        // Shenqianfeng annotated
        /*
         * if (!mGetContent) {
         * actionBar.enableAlbumModeMenu(GalleryActionBar.ALBUM_GRID_MODE_SELECTED
         * , this); }
         */
        // TCL ShenQianfeng End on 2016.07.04

        // Set the reload bit here to prevent it exit this page in
        // clearLoadingBit().
        setLoadingBit(BIT_LOADING_RELOAD);
        mLoadingFailed = false;
        mAlbumDataAdapter.resume();
        mAlbumView.resume();
        mAlbumView.setPressedIndex(-1);
        mActionModeHandler.resume();
        
        // TCL ShenQianfeng Begin on 2016.11.09
        // Annotated Below:
        /*
        if (!mInitialSynced) {
            setLoadingBit(BIT_LOADING_SYNC);
            mSyncTask = mMediaSet.requestSync(this);
        }
        */
        // TCL ShenQianfeng End on 2016.11.09

        mInCameraAndWantQuitOnPause = mInCameraApp;

        // TCL ShenQianfeng Begin on 2016.09.09
        if(mEmbeddedPhotoPage != null /*&& mEmbeddedPhotoPage.isShowingEmbedded()*/) {
            mEmbeddedPhotoPage.resumeEmbedded(false);
        }
        // TCL ShenQianfeng End on 2016.09.09
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsActive = false;
        // TCL BaiYuan Begin on 2016.11.14
        // Annotated Below:
        /*
        if (mSelectionManager.inSelectionMode()) {
            mSelectionManager.leaveSelectionMode();
        }
        */
        // TCL BaiYuan End on 2016.11.14

        // TCL ShenQianfeng Begin on 2016.11.17
        /*
        SystemUIManager systemUIManager = mActivity.getSystemUIManager();
        systemUIManager.unregisterSystemUIFlagChangeListener(this);
        */
        // TCL ShenQianfeng End on 2016.11.17

        mAlbumView.setSlotFilter(null);
        mActionModeHandler.pause();
        mAlbumDataAdapter.pause();
        mAlbumView.pause();
        DetailsHelper.pause();
        if (!mGetContent) {
            mActivity.getGalleryActionBar().disableAlbumModeMenu(true);
        }
        
        // TCL ShenQianfeng Begin on 2016.11.09
        // Annotated Below:
        /*
        if (mSyncTask != null) {
            mSyncTask.cancel();
            mSyncTask = null;
            clearLoadingBit(BIT_LOADING_SYNC);
        }
        */
        // TCL ShenQianfeng End on 2016.11.09
        // TCL ShenQianfeng Begin on 2016.09.09
        if(mEmbeddedPhotoPage != null) {
            mEmbeddedPhotoPage.pauseEmbedded();
        }
        // TCL ShenQianfeng End on 2016.09.09
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAlbumDataAdapter != null) {
            mAlbumDataAdapter.destroy();
            mAlbumDataAdapter.setLoadingListener(null);
        }
        mActionModeHandler.destroy();
    }

    private void initializeViews() {
        mSelectionManager = new SelectionManager(mActivity, false);
        mSelectionManager.setSelectionListener(this);
        Config.AlbumPage config = Config.AlbumPage.get(mActivity);
        mSlotView = new SlotView(mActivity, config.slotViewSpec);
        
        // TCL ShenQianfeng Begin on 2016.08.11
        mSlotView.setSelectionStatusGetter(this);
        //mSlotView.setOverscrollEffect(SlotView.OVERSCROLL_SYSTEM);
        // TCL ShenQianfeng End on 2016.08.11

        mAlbumView = new AlbumSlotRenderer(mActivity, mSlotView, mSelectionManager, config.placeholderColor);
        
        // TCL ShenQianfeng Begin on 2016.08.11
        CustomStringTexture.initialize(mActivity);
        // TCL ShenQianfeng End on 2016.08.11
        
        mSlotView.setSlotRenderer(mAlbumView);
        mRootPane.addComponent(mSlotView);
        mSlotView.setListener(new SlotView.SimpleListener() {
            @Override
            public void onDown(int index) {
                AlbumPage.this.onDown(index);
            }

            @Override
            public void onUp(boolean followedByLongPress) {
                AlbumPage.this.onUp(followedByLongPress);
            }

            @Override
            public void onSingleTapUp(int slotIndex) {
                // TCL ShenQianfeng Begin on 2016.07.19
                mSingleTapIndex = slotIndex;
                // TCL ShenQianfeng End on 2016.07.19
                AlbumPage.this.onSingleTapUp(slotIndex);
            }

            @Override
            public void onLongTap(int slotIndex) {
                AlbumPage.this.onLongTap(slotIndex);
            }
        });
        mActionModeHandler = new ActionModeHandler(mActivity, mSelectionManager);
        // TCL ShenQianfeng Begin on 2016.08.11
        // Original:
        /*
         mActionModeHandler.setActionModeListener(new ActionModeListener() {
            @Override
            public boolean onActionItemClicked(MenuItem item) {
                return onItemSelected(item);
            }
        });
         */
        // Modify To:
        mActionModeHandler.setActionModeListener(this);
        // TCL ShenQianfeng End on 2016.08.11
        
        // TCL ShenQianfeng Begin on 2216.07.22
        //mActionModeHandler.setOnClickListener(this);
        // TCL ShenQianfeng End on 2216.07.22
    }

    private void initializeData(Bundle data) {
        mMediaSetPath = Path.fromString(data.getString(KEY_MEDIA_PATH));
        mParentMediaSetString = data.getString(KEY_PARENT_MEDIA_PATH);
        mMediaSet = mActivity.getDataManager().getMediaSet(mMediaSetPath);
        // TCL ShenQianfeng Begin on 2016.06.24
        /*
         * if(mMediaSet instanceof SortedComboAlbum) { SortedComboAlbum album =
         * (SortedComboAlbum)mMediaSet; album.formDateGroupNumMap(); }
         */
        // TCL ShenQianfeng End on 2016.06.24

        if (mMediaSet == null) {
            Utils.fail("MediaSet is null. Path = %s", mMediaSetPath);
        }
        mSelectionManager.setSourceMediaSet(mMediaSet);
        mAlbumDataAdapter = new AlbumDataLoader(mActivity, mMediaSet);
        mAlbumDataAdapter.setLoadingListener(new MyLoadingListener());
        mAlbumDataAdapter.setEnumeratListener(this);
        mAlbumView.setModel(mAlbumDataAdapter);
    }

    private void showDetails() {
        mShowDetails = true;
        if (mDetailsHelper == null) {
            mDetailsHelper = new DetailsHelper(mActivity, mRootPane,
                    mDetailsSource);
            mDetailsHelper.setCloseListener(new CloseListener() {
                @Override
                public void onClose() {
                    hideDetails();
                }
            });
        }
        mDetailsHelper.show();
    }

    private void hideDetails() {
        mShowDetails = false;
        mDetailsHelper.hide();
        mAlbumView.setHighlightItemPath(null);
        mSlotView.invalidate();
    }

    // TCL ShenQianfeng Begin on 2016.06.30
    // Original:
    /*
     * @Override protected boolean onCreateActionBar(Menu menu) {
     * GalleryActionBar actionBar = mActivity.getGalleryActionBar();
     * MenuInflater inflator = getSupportMenuInflater(); if (mGetContent) {
     * inflator.inflate(R.menu.pickup, menu); int typeBits =
     * mData.getInt(GalleryActivity.KEY_TYPE_BITS, DataManager.INCLUDE_IMAGE);
     * actionBar.setTitle(GalleryUtils.getSelectionModePrompt(typeBits)); } else
     * { inflator.inflate(R.menu.album, menu);
     * actionBar.setTitle(mMediaSet.getName());
     * 
     * FilterUtils.setupMenuItems(actionBar, mMediaSetPath, true);
     * 
     * menu.findItem(R.id.action_group_by).setVisible(mShowClusterMenu);
     * 
     * menu.findItem(R.id.action_camera).setVisible(
     * MediaSetUtils.isCameraSource(mMediaSetPath) &&
     * GalleryUtils.isCameraAvailable(mActivity));
     * 
     * } actionBar.setSubtitle(null); return true; }
     */
    // Modify To:
    
    

    @Override
    protected boolean onCreateActionBar(Menu menu) {
        GalleryActionBar actionBar = mActivity.getGalleryActionBar();
        MenuInflater inflator = getSupportMenuInflater();
        if (mGetContent) {
            inflator.inflate(R.menu.pickup, menu);
            int typeBits = mData.getInt(GalleryActivity.KEY_TYPE_BITS,
                    DataManager.INCLUDE_IMAGE);
            actionBar.setTitle(GalleryUtils.getSelectionModePrompt(typeBits));
        } else {
            // inflator.inflate(R.menu.album, menu);
            inflator.inflate(R.menu.mst_album_page_normal, menu);
            
            /*
            String title = mActivity.getString(R.string.mst_gallery);
            SpannableString s = new SpannableString(title);
            s.setSpan(new TypefaceSpan("/system/fonts/SourceHanSansCN-Light"), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //actionBar.setTitle(s);
            actionBar.setTitle(s);
            */
            Resources resource = mActivity.getResources();
            int titleId = resource.getIdentifier("action_bar_title", "id", "android");
            TextView titleTextView = (TextView) mActivity.findViewById(titleId);
            if(null != titleTextView) {
                titleTextView.setText(R.string.mst_gallery);
                Typeface tf = Typeface.createFromFile("/system/fonts/Roboto-Bold.ttf"); //Roboto-Bold.ttf ///system/fonts/SourceHanSansCN-Light.ttf"
                titleTextView.setTypeface(tf);
            }
            // TCL ShenQianfeng End on 2016.07.04
        }
        actionBar.setSubtitle(null);
        return true;
    }
    
    private boolean isShowingEmbeddedPhotoPage() {
        return mEmbeddedPhotoPage != null && mEmbeddedPhotoPage.isShowingEmbedded();
    }

    @Override
    public void onMstToolbarNavigationClicked(View view) {
        if(isShowingEmbeddedPhotoPage()) {
            mEmbeddedPhotoPage.onMstToolbarNavigationClicked(view);
        }
    }

    @Override
    public boolean onMstToolbarMenuItemClicked(MenuItem item) {
        if(isShowingEmbeddedPhotoPage()) {
            return mEmbeddedPhotoPage.onMstToolbarMenuItemClicked(item);
        }
        int itemId = item.getItemId();
        if(itemId == R.id.action_enteralbum) {
            
        }
        // TCL BaiYuan Begin on 2016.11.15
        else if(R.id.action_cancel == itemId){
            mActivity.getStateManager().finishState(this);
        }
        // TCL BaiYuan End on 2016.11.15
        return true;
    }

    @Override
    protected boolean rebuildToolBar() {
        GalleryActionBar actionBar = mActivity.getGalleryActionBar();
        //MenuInflater inflator = getSupportMenuInflater();
        if (mGetContent) {
            //inflator.inflate(R.menu.pickup, menu);
            int typeBits = mData.getInt(GalleryActivity.KEY_TYPE_BITS, DataManager.INCLUDE_IMAGE);
            actionBar.setTitle(GalleryUtils.getSelectionModePrompt(typeBits));
            Menu menu = mActivity.getOptionMenu();
            menu.clear();
            mActivity.inflateToolbarMenu(R.menu.pickup);
        } else {
            Menu menu = mActivity.getOptionMenu();
            menu.clear();
            mActivity.inflateToolbarMenu(R.menu.mst_album_page_normal);
            actionBar.setTitle(R.string.mst_gallery);
            // TCL ShenQianfeng End on 2016.07.04
        }
        Toolbar toolBar = actionBar.getToolbar();
        toolBar.setNavigationIcon(null);
        
        /*
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)toolBar.getLayoutParams();
        lp.topMargin = mActivity.getStatusBarHeight();
        toolBar.setLayoutParams(lp);
        */
        /*
        toolBar.setPadding(toolBar.getPaddingLeft(), mActivity.getStatusBarHeight(), toolBar.getPaddingRight(), toolBar.getPaddingBottom());
        
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)toolBar.getLayoutParams();
        lp.height = mActivity.getStatusBarHeight() + 144;
        toolBar.setLayoutParams(lp);
        */

        mActivity.setupActionModeWithDecor(toolBar);
        actionBar.setSubtitle(null);
        TextView indicatorTextView = (TextView) toolBar.findViewById(R.id.mst_photo_page_indicator);
        if(indicatorTextView != null) {
            toolBar.removeView(indicatorTextView);
        }
        return true;
    }
    
    public void showToolBar() {
        Toolbar toolbar = mActivity.getToolbar();
        toolbar.setTranslationY(0);
    }

    // TCL ShenQianfeng End on 2016.06.30
    private void prepareAnimationBackToFilmstrip(int slotIndex) {
        if (mAlbumDataAdapter == null || !mAlbumDataAdapter.isActive(slotIndex))
            return;
        MediaItem item = mAlbumDataAdapter.get(slotIndex);
        if (item == null)
            return;
        TransitionStore transitions = mActivity.getTransitionStore();
        transitions.put(PhotoPage.KEY_INDEX_HINT, slotIndex);
        transitions.put(PhotoPage.KEY_OPEN_ANIMATION_RECT,
                mSlotView.getSlotRect(slotIndex, mRootPane));
    }

    private void switchToFilmstrip() {
        if (mAlbumDataAdapter.size() < 1)
            return;
        int targetPhoto = mSlotView.getVisibleStart();
        prepareAnimationBackToFilmstrip(targetPhoto);
        if (mLaunchedFromPhotoPage) {
            onBackPressed();
        } else {
            pickPhoto(targetPhoto, true);
        }
    }

    @Override
    protected boolean onItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home: {
            onUpPressed();
            return true;
        }
        case R.id.action_cancel:
            mActivity.getStateManager().finishState(this);
            return true;
        case R.id.action_select:
            mSelectionManager.setAutoLeaveSelectionMode(false);
            mSelectionManager.enterSelectionMode();
            return true;
        case R.id.action_group_by: {
            mActivity.getGalleryActionBar().showClusterDialog(this);
            return true;
        }
        case R.id.action_slideshow: {
            mInCameraAndWantQuitOnPause = false;
            Bundle data = new Bundle();
            data.putString(SlideshowPage.KEY_SET_PATH, mMediaSetPath.toString());
            data.putBoolean(SlideshowPage.KEY_REPEAT, true);
            mActivity.getStateManager().startStateForResult(
                    SlideshowPage.class, REQUEST_SLIDESHOW, data);
            return true;
        }
        case R.id.action_details: {
            if (mShowDetails) {
                hideDetails();
            } else {
                showDetails();
            }
            return true;
        }
        case R.id.action_camera: {
            GalleryUtils.startCameraActivity(mActivity);
            return true;
        }
        default:
            return false;
        }
    }

    @Override
    protected void onStateResult(int request, int result, Intent data) {
        switch (request) {
        case REQUEST_SLIDESHOW: {
            // data could be null, if there is no images in the album
            if (data == null)
                return;
            mFocusIndex = data.getIntExtra(SlideshowPage.KEY_PHOTO_INDEX, 0);
            mSlotView.setCenterIndex(mFocusIndex);
            break;
        }
        case REQUEST_PHOTO: {
            if (data == null)
                return;
            // TCL ShenQianfeng Begin on 2016.08.13
            Menu menu = mActivity.getOptionMenu();
            menu.clear();
            rebuildToolBar();
            // TCL ShenQianfeng End on 2016.08.13
            mFocusIndex = data.getIntExtra(PhotoPage.KEY_RETURN_INDEX_HINT, 0);
            mSlotView.makeSlotVisible(mFocusIndex);
            
            break;
        }
        case REQUEST_DO_ANIMATION: {
            mSlotView.startRisingAnimation();
            break;
        }
        }
    }

    @Override
    public void onSelectionModeChange(int mode) {
        switch (mode) {
        case SelectionManager.ENTER_SELECTION_MODE: {
            mActivity.setActionModeListener(AlbumPage.this);
            mActionModeHandler.startActionMode();
            //View popupParent = mActivity.findViewById(R.id.gallery_root);
            // TCL ShenQianfeng Begin on 2016.07.08
            /*
            if (mSelectDeletePopupWindow == null) {
                mSelectDeletePopupWindow = new SelectDeletePopupWindow(
                        popupParent);
            }
            mSelectDeletePopupWindow.setOnSelectDeleteClickedListener(this);
            mSelectDeletePopupWindow.show();
            */
            // TCL ShenQianfeng End on 2016.07.08
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            mSlotView.getLayout().initLayoutParameters(-1);
            mSlotView.invalidate();
            break;
        }
        case SelectionManager.LEAVE_SELECTION_MODE: {
            // TCL ShenQianfeng Begin on 2016.07.08
            /*
            if (mSelectDeletePopupWindow != null) {
                mSelectDeletePopupWindow.dismiss();
            }
            */
            // TCL ShenQianfeng End on 2016.07.08
            mActionModeHandler.finishActionMode();
            mSlotView.getLayout().initLayoutParameters(-1);
            mSlotView.invalidate();
            break;
        }
        case SelectionManager.SELECT_ALL_MODE: {
            // TCL ShenQianfeng Begin on 2016.07.07
            // Annotated Below:
            // mActionModeHandler.updateSupportedOperation();
            // TCL ShenQianfeng End on 2016.07.07
            mSlotView.invalidate();
            break;
        }
        }
    }

    @Override
    public void onSelectionChange(Path path, boolean selected) {

        // TCL ShenQianfeng Begin on 2016.08.13
        // Original:
        /*
        int count = mSelectionManager.getSelectedCount();
        String format = mActivity.getResources().getQuantityString(
                R.plurals.number_of_items_selected, count);
        mActionModeHandler.setTitle(String.format(format, count));
        mActionModeHandler.updateSupportedOperation(path, selected);
        */
        // Modify To:
        updateActionModeSelectedCount();
        int count = mSelectionManager.getSelectedCount();
        boolean deleteButtonEnabled = count != 0;
        mActionModeHandler.setDeleteButtonEnable(deleteButtonEnabled);
        // TCL ShenQianfeng End on 2016.08.13
        // TCL BaiYuan Begin on 2016.11.11
        updateActionMode();
        // TCL BaiYuan End on 2016.11.11
    }
    
    public void updateActionModeSelectedCount() {
        int count = mSelectionManager.getSelectedCount();
        // TCL BaiYuan Begin on 2016.11.03
        /*
        if(count == mSelectionManager.getTotalCount()){
            String text = mActivity.getString(R.string.deselect_all);
            mActivity.getActionMode().setPositiveText(text);
        }
        */
        // TCL BaiYuan End on 2016.11.03
        mActionModeHandler.updateSelectedCount(count);
    }
    
    // TCL ShenQianfeng Begin on 2016.11.09
    // Annotated Below:
    /*
    @Override
    public void onSyncDone(final MediaSet mediaSet, final int resultCode) {
        Log.d(TAG, "onSyncDone: " + Utils.maskDebugInfo(mediaSet.getName())
                + " result=" + resultCode);
        ((Activity) mActivity).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GLRoot root = mActivity.getGLRoot();
                root.lockRenderThread();
                mSyncResult = resultCode;
                try {
                    if (resultCode == MediaSet.SYNC_RESULT_SUCCESS) {
                        mInitialSynced = true;
                    }
                    clearLoadingBit(BIT_LOADING_SYNC);
                    showSyncErrorIfNecessary(mLoadingFailed);
                } finally {
                    root.unlockRenderThread();
                }
            }
        });
    }
    */
    // TCL ShenQianfeng End on 2016.11.09
    
    // Show sync error toast when all the following conditions are met:
    // (1) both loading and sync are done,
    // (2) sync result is error,
    // (3) the page is still active, and
    // (4) no photo is shown or loading fails.
    private void showSyncErrorIfNecessary(boolean loadingFailed) {
        if ((mLoadingBits == 0) && (mSyncResult == MediaSet.SYNC_RESULT_ERROR)
                && mIsActive
                && (loadingFailed || (mAlbumDataAdapter.size() == 0))) {
            Toast.makeText(mActivity, R.string.sync_album_error,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setLoadingBit(int loadTaskBit) {
        mLoadingBits |= loadTaskBit;
    }

    private void clearLoadingBit(int loadTaskBit) {
        mLoadingBits &= ~loadTaskBit;
        // TCL ShenQianfeng Begin on 2216.07.23
        // Annotated Below:
        /*
         * if (mLoadingBits == 0 && mIsActive) { if (mAlbumDataAdapter.size() ==
         * 0) { Intent result = new Intent(); result.putExtra(KEY_EMPTY_ALBUM,
         * true); setStateResult(Activity.RESULT_OK, result);
         * mActivity.getStateManager().finishState(this); } }
         */
        // TCL ShenQianfeng End on 2216.07.23
    }

    private class MyLoadingListener implements LoadingListener {
        @Override
        public void onLoadingStarted() {
            setLoadingBit(BIT_LOADING_RELOAD);
            mLoadingFailed = false;
        }

        @Override
        public void onLoadingFinished(boolean loadingFailed) {
            clearLoadingBit(BIT_LOADING_RELOAD);
            mLoadingFailed = loadingFailed;
            showSyncErrorIfNecessary(loadingFailed);
            // TCL BaiYuan Begin on 2016.11.15
//            mSlotView.showEmptyPage(false);
            // TCL BaiYuan Begin on 2016.11.15
        }

        // TCL BaiYuan Begin on 2016.11.14
        // Original:
        /*
        // TCL ShenQianfeng Begin on 2016.08.10
        @Override
        public void onNotifyEmpty() {
            //TODO: show no photos view
            mSlotView.clearDateGroupInfos();
            // TCL BaiYuan Begin on 2016.11.14
            mSlotView.showEmptyPage(true);
            // TCL BaiYuan End on 2016.11.14
            mRootPane.invalidate();
        }
        // TCL ShenQianfeng End on 2016.08.10
        */
        @Override
        public void onNotifyEmpty(boolean isEmpty) {
            if(isEmpty){
                mSlotView.clearDateGroupInfos();
            }
            mSlotView.showEmptyPage(isEmpty);
        }
        // TCL BaiYuan Begin on 2016.11.14
    }

    private class MyDetailsSource implements DetailsHelper.DetailsSource {
        private int mIndex;

        @Override
        public int size() {
            return mAlbumDataAdapter.size();
        }

        @Override
        public int setIndex() {
            Path id = mSelectionManager.getSelected(false).get(0);
            mIndex = mAlbumDataAdapter.findItem(id);
            return mIndex;
        }

        @Override
        public MediaDetails getDetails() {
            // this relies on setIndex() being called beforehand
            MediaObject item = mAlbumDataAdapter.get(mIndex);
            if (item != null) {
                mAlbumView.setHighlightItemPath(item.getPath());
                return item.getDetails();
            } else {
                return null;
            }
        }
    }

    @Override
    public void onAlbumModeSelected(int mode) {
        if (mode == GalleryActionBar.ALBUM_FILMSTRIP_MODE_SELECTED) {
            switchToFilmstrip();
        }
    }

    @Override
    public void onEnumerate(int index, boolean finished, DateGroupInfos info) {
        //LogUtil.d(TAG, "AlbumPage --> onEnumerate index:" + index);
        mSlotView.setDateGroupInfos(info);
    }
    // TCL ShenQianfeng End on 2216.07.22

    // TCL ShenQianfeng Begin on 2016.08.11
    @Override
    public boolean isInSelectionMode() {
        return mSelectionManager != null && mSelectionManager.inSelectionMode();
    }
    // TCL ShenQianfeng End on 2016.08.11

    @Override
    public void select(int slotIndex) {
        if(mSelectionManager == null) return;
        if(slotIndex < 0 || slotIndex >= mAlbumDataAdapter.size()) return;
        MediaItem item = mAlbumDataAdapter.get(slotIndex);
        if (item == null)
            return; // Item not ready yet, ignore the click
        Path path = item.getPath();
        if( ! mSelectionManager.isItemSelected(path)) {
            // TCL BaiYuan Begin on 2016.11.02
            // Original:
            /*
            mSelectionManager.toggle(item.getPath());
            */
            // Modify To:
            mSelectionManager.toggle(item);
            // TCL BaiYuan End on 2016.11.02
            mSlotView.invalidate();
        }
    }

    @Override
    public void unselect(int slotIndex) {
        if(mSelectionManager == null) return;
        if(slotIndex < 0 || slotIndex >= mAlbumDataAdapter.size()) return;
        MediaItem item = mAlbumDataAdapter.get(slotIndex);
        if (item == null)
            return; // Item not ready yet, ignore the click
        Path path = item.getPath();
        if(mSelectionManager.isItemSelected(path)) {
             // TCL BaiYuan Begin on 2016.11.02
             // Original:
             /*
            mSelectionManager.toggle(item.getPath());
            */
            // Modify To:
            mSelectionManager.toggle(item);
            // TCL BaiYuan End on 2016.11.02
            mSlotView.invalidate();
        }
    }

    @Override
    public boolean isItemSelected(int slotIndex) {
        if(mSelectionManager == null) return false;
        if(slotIndex < 0 || slotIndex >= mAlbumDataAdapter.size()) return false;
        MediaItem item = mAlbumDataAdapter.get(slotIndex);
        if (item == null)
            return false; // Item not ready yet, ignore the click
        Path path = item.getPath();
        return mSelectionManager.isItemSelected(path);
    }

    // TCL ShenQianfeng Begin on 2016.08.11
    @Override
    public void onActionItemClicked(Item paramItem) {
        int id = paramItem.getItemId();
        if(id == ActionMode.POSITIVE_BUTTON) {
            ActionMode actionMode = mActivity.getActionMode();
            String text = "";
            if(mSelectionManager.inSelectAllMode()) {
                mSelectionManager.deSelectAll();
                text = mActivity.getString(R.string.select_all);
                mActionModeHandler.setDeleteButtonEnable(false);
            } else {
                mSelectionManager.selectAll();
                mActionModeHandler.setDeleteButtonEnable(true);
                text = mActivity.getString(R.string.deselect_all);
            }
            actionMode.setPositiveText(text);
            updateActionModeSelectedCount();
            mSlotView.invalidate();
        } else if(id == ActionMode.NAGATIVE_BUTTON) {
            mSelectionManager.leaveSelectionMode();
        }
    }

    // TCL BaiYuan Begin on 2016.11.11
    public void updateActionMode() {
        ActionMode actionMode = mActivity.getActionMode();
        String text = "";
        if(mSelectionManager.getSelectedCount() == mSelectionManager.getTotalCount()) {
            text = mActivity.getString(R.string.deselect_all);
        } else {
            text = mActivity.getString(R.string.select_all);
        }
        actionMode.setPositiveText(text);
    }
    // TCL BaiYuan End on 2016.11.11

    @Override
    public void onActionModeShow(ActionMode paramActionMode) {
        
    }

    @Override
    public void onActionModeDismiss(ActionMode paramActionMode) {

    }
    
    @Override
    public Rect getSlotRect(int slotIndex) {
        /*
        int currentUnitCount = mSlotView.getCurrentUnitCount();
        int mode = mSlotView.getColumnDisplayMode(currentUnitCount);
        mSlotView.getSlotRect(slotIndex, currentUnitCount, mTempRect, mode);
        LogUtil.d(TAG, "getSlotRect slotIndex:" + slotIndex + 
                " currentUnitCount:" + currentUnitCount + 
                " mode:" + mode + 
                " mTempRect:" + mTempRect);
        return mTempRect;
        */
        Rect rect = mSlotView.getSlotRect(slotIndex, mRootPane);
        return rect;
    }
    // TCL ShenQianfeng End on 2016.08.11

    /*
    @Override
    public void onNavigationBarHidden() {
        LogUtil.d(TAG, "AlbumPage::onNavigationBarHidden");
    }

    @Override
    public void onNavigationBarShown() {
        LogUtil.d(TAG, "AlbumPage::onNavigationBarShown");
    }
    */
    
}
