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

import mst.widget.toolbar.Toolbar;
import android.annotation.TargetApi;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.app.Activity;
//import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.android.gallery3d.R;
import com.android.gallery3d.app.SystemUIManager.SystemUIFlagChangeListener;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.data.ComboAlbum;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.FilterDeleteSet;
import com.android.gallery3d.data.FilterSource;
import com.android.gallery3d.data.MediaDetails;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaObject.PanoramaSupportCallback;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.data.SecureAlbum;
import com.android.gallery3d.data.SecureSource;
import com.android.gallery3d.data.SnailAlbum;
import com.android.gallery3d.data.SnailItem;
import com.android.gallery3d.data.SnailSource;
import com.android.gallery3d.filtershow.FilterShowActivity;
import com.android.gallery3d.filtershow.crop.CropActivity;
import com.android.gallery3d.picasasource.PicasaSource;
import com.android.gallery3d.ui.DetailsHelper;
import com.android.gallery3d.ui.DetailsHelper.CloseListener;
import com.android.gallery3d.ui.DetailsHelper.DetailsSource;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.ui.GLView;
import com.android.gallery3d.ui.MenuExecutor;
import com.android.gallery3d.ui.PhotoView;
import com.android.gallery3d.ui.PhotoView.AlbumPageSlotPositionProvider;
import com.android.gallery3d.ui.SelectionManager;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.ui.ZoomAnimationListener;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.LogUtil;
import com.android.gallery3d.util.UsageStatistics;
// TCL ShenQianfeng Begin on 2016.09.08
// Original:
//public abstract class PhotoPage extends ....
// Modify To:
//public  class PhotoPage extends ....
// TCL ShenQianfeng End on 2016.09.08
public class PhotoPage extends ActivityState implements
        PhotoView.Listener, AppBridge.Server, ShareActionProvider.OnShareTargetSelectedListener,
        PhotoPageBottomControls.Delegate, GalleryActionBar.OnAlbumModeSelectedListener , ZoomAnimationListener, 
        SystemUIFlagChangeListener {
    private static final String TAG = "PhotoPage";

    private static final int MSG_HIDE_BARS = 1;
    private static final int MSG_ON_FULL_SCREEN_CHANGED = 4;
    private static final int MSG_UPDATE_ACTION_BAR = 5;
    private static final int MSG_UNFREEZE_GLROOT = 6;
    private static final int MSG_WANT_BARS = 7;
    private static final int MSG_REFRESH_BOTTOM_CONTROLS = 8;
    private static final int MSG_ON_CAMERA_CENTER = 9;
    private static final int MSG_ON_PICTURE_CENTER = 10;
    private static final int MSG_REFRESH_IMAGE = 11;
    private static final int MSG_UPDATE_PHOTO_UI = 12;
    private static final int MSG_UPDATE_DEFERRED = 14;
    private static final int MSG_UPDATE_SHARE_URI = 15;
    private static final int MSG_UPDATE_PANORAMA_UI = 16;
    
    // TCL ShenQianfeng Begin on 2016.09.14
    private static final int MSG_REBUILD_TOOLBAR = 100;
    private static final int MSG_SET_TOOLBAR_CHILD_VIEWS_ALPHA = 101;
    private static final int MSG_SET_BOTTOM_CONTROLS_ALPHA = 102;
    private static final int MSG_ON_ZOOM_IN_START = 103;
    private static final int MSG_ON_ZOOM_IN_END = 104;
    private static final int MSG_ON_ZOOM_OUT_START = 105;
    private static final int MSG_ON_ZOOM_OUT_END = 106;
    private static final int MSG_INIT_BOTTOM_CONTROLS_STATUS = 107;
    private static final int MSG_REBUILD_TOOLBAR_MENU = 108;
    private static final int MSG_MODIFY_SYSTEM_UI_WHEN_RESUME = 109;
    
    private static final int MSG_EMBEDDED_BACK_WITHOUT_ANIM = 110;
    
    // TCL ShenQianfeng End on 2016.09.14
    
    private static final int HIDE_BARS_TIMEOUT = 3500;
    private static final int UNFREEZE_GLROOT_TIMEOUT = 250;

    private static final int REQUEST_SLIDESHOW = 1;
    private static final int REQUEST_CROP = 2;
    private static final int REQUEST_CROP_PICASA = 3;
    private static final int REQUEST_EDIT = 4;
    private static final int REQUEST_PLAY_VIDEO = 5;
    private static final int REQUEST_TRIM = 6;

    public static final String KEY_MEDIA_SET_PATH = "media-set-path";
    public static final String KEY_MEDIA_ITEM_PATH = "media-item-path";
    public static final String KEY_INDEX_HINT = "index-hint";
    public static final String KEY_OPEN_ANIMATION_RECT = "open-animation-rect";
    public static final String KEY_APP_BRIDGE = "app-bridge";
    public static final String KEY_TREAT_BACK_AS_UP = "treat-back-as-up";
    public static final String KEY_START_IN_FILMSTRIP = "start-in-filmstrip";
    public static final String KEY_RETURN_INDEX_HINT = "return-index-hint";
    public static final String KEY_SHOW_WHEN_LOCKED = "show_when_locked";
    public static final String KEY_IN_CAMERA_ROLL = "in_camera_roll";
    public static final String KEY_READONLY = "read-only";
    
    // TCL ShenQianfeng Begin on 2016.07.19
    public static final String KEY_ENTERING_BITMAP = "entering-bitmap";
    public static final String KEY_ENTERING_BITMAP_ROTATION = "entering-bitmap-rotation";
    public static final String KEY_ENTERING_BITMAP_INDEX = "entering-bitmap-index";
    
    public ActivityState mParentActivityState;
    // TCL ShenQianfeng End on 2016.07.19

    public static final String KEY_ALBUMPAGE_TRANSITION = "albumpage-transition";
    public static final int MSG_ALBUMPAGE_NONE = 0;
    public static final int MSG_ALBUMPAGE_STARTED = 1;
    public static final int MSG_ALBUMPAGE_RESUMED = 2;
    public static final int MSG_ALBUMPAGE_PICKED = 4;

    public static final String ACTION_NEXTGEN_EDIT = "action_nextgen_edit";
    public static final String ACTION_SIMPLE_EDIT = "action_simple_edit";

    private GalleryApp mApplication;
    private SelectionManager mSelectionManager;

    private PhotoView mPhotoView;
    private PhotoPage.Model mModel;
    private DetailsHelper mDetailsHelper;
    private boolean mShowDetails;

    // mMediaSet could be null if there is no KEY_MEDIA_SET_PATH supplied.
    // E.g., viewing a photo in gmail attachment
    private FilterDeleteSet mMediaSet;

    // The mediaset used by camera launched from secure lock screen.
    private SecureAlbum mSecureAlbum;
    
    // TCL ShenQianfeng Begin on 2016.09.12
    // Original:
    // private int mCurrentIndex = 0;
    // Modify To:
    private int mCurrentIndex = -1;
    // TCL ShenQianfeng End on 2016.09.12

    private Handler mHandler;
    // TCL ShenQianfeng Begin on 2016.07.06
    // Original:
    //private boolean mShowBars = true;
    // Modify To:
    // We do not want bars to be shown when enter
    private boolean mShowBars = true;
    private TextView mIndicatorTextView;
    private Toolbar.LayoutParams mIndicatorTextViewLayoutParams = new Toolbar.LayoutParams(Gravity.CENTER);
    // TCL ShenQianfeng End on 2016.07.06
    private volatile boolean mActionBarAllowed = true;
    private GalleryActionBar mActionBar;
    private boolean mIsMenuVisible;
    private boolean mHaveImageEditor;
    private PhotoPageBottomControls mBottomControls;
    private MediaItem mCurrentPhoto = null;
    private MenuExecutor mMenuExecutor;
    private boolean mIsActive;
    
    // TCL ShenQianfeng Begin on 2016.09.08
    /**
     * @param mEmbedded indicates this PhotoPage is embedded in a AlbumPage or some other ActivityState(s).
     */
    private boolean mEmbedded;
    private boolean mEmbeddedRebuilt;
    private Drawable mNavigationIcon;
    private Menu mPhotoPageMenu;
    // TCL ShenQianfeng End on 2016.09.08
    
    // TCL ShenQianfeng Begin on 2016.10.20
    public static final int TOOLBAR_ANIM_DURATION = 250;
    // TCL ShenQianfeng End on 2016.10.20
    
    //TCL ShenQianfeng Begin on 2016.07.06
    //Annotated Below:
    //private boolean mShowSpinner;
    //TCL ShenQianfeng End on 2016.07.06

    private String mSetPathString;
    // This is the original mSetPathString before adding the camera preview item.
    private boolean mReadOnlyView = false;
    private String mOriginalSetPathString;
    private AppBridge mAppBridge;
    private SnailItem mScreenNailItem;
    private SnailAlbum mScreenNailSet;
    private OrientationManager mOrientationManager;
    private boolean mTreatBackAsUp;
    private boolean mStartInFilmstrip;
    private boolean mHasCameraScreennailOrPlaceholder = false;
    private boolean mRecenterCameraOnResume = true;

    // These are only valid after the panorama callback
    private boolean mIsPanorama;
    private boolean mIsPanorama360;

    private long mCameraSwitchCutoff = 0;
    private boolean mSkipUpdateCurrentPhoto = false;
    private static final long CAMERA_SWITCH_CUTOFF_THRESHOLD_MS = 300;

    private static final long DEFERRED_UPDATE_MS = 250;
    private boolean mDeferredUpdateWaiting = false;
    private long mDeferUpdateUntil = Long.MAX_VALUE;

    // The item that is deleted (but it can still be undeleted before commiting)
    private Path mDeletePath;
    private boolean mDeleteIsFocus;  // whether the deleted item was in focus

    private Uri[] mNfcPushUris = new Uri[1];

    private final MyMenuVisibilityListener mMenuVisibilityListener =
            new MyMenuVisibilityListener();

    private int mLastSystemUiVis = 0;
    
    // TCL BaiYuan Begin on 2016.10.19
    private static final int TOOLBAR_MARGIN_IN_DP = 20;
    // TCL BaiYuan End on 2016.10.19
    
    // TCL ShenQianfeng Begin on 2016.10.28
    // TCL BaiYuan Begin on 2016.11.09
    /*
    private boolean mIsHidingToolBar;
    */ 
    // TCL BaiYuan End on 2016.11.09
    private HideAnimationListener mHideAnimationListener;
    
    public class HideAnimationListener implements AnimationListener {
        
        private Toolbar mToolbar;
        
        public HideAnimationListener(Toolbar toolbar) {
            mToolbar = toolbar;
        }
        @Override
        public void onAnimationStart(Animation animation) {
            // TCL BaiYuan Begin on 2016.11.09
            /*
            mIsHidingToolBar = true;
            */
            // TCL BaiYuan End on 2016.11.09
        }
        @Override
        public void onAnimationEnd(Animation animation) { 
            mToolbar.setVisibility(View.GONE);
            // TCL BaiYuan Begin on 2016.11.09
            /*
            mIsHidingToolBar = false;
            */
            // TCL BaiYuan End on 2016.11.09
        }
        @Override
        public void onAnimationRepeat(Animation animation) { }
    }
    
    // TCL ShenQianfeng End on 2016.10.28


    private final PanoramaSupportCallback mUpdatePanoramaMenuItemsCallback = new PanoramaSupportCallback() {
        @Override
        public void panoramaInfoAvailable(MediaObject mediaObject, boolean isPanorama,
                boolean isPanorama360) {
            if (mediaObject == mCurrentPhoto) {
                mHandler.obtainMessage(MSG_UPDATE_PANORAMA_UI, isPanorama360 ? 1 : 0, 0,
                        mediaObject).sendToTarget();
            }
        }
    };

    private final PanoramaSupportCallback mRefreshBottomControlsCallback = new PanoramaSupportCallback() {
        @Override
        public void panoramaInfoAvailable(MediaObject mediaObject, boolean isPanorama, boolean isPanorama360) {
            if (mediaObject == mCurrentPhoto) {
                //LogUtil.i2(TAG, "mRefreshBottomControlsCallback panoramaInfoAvailable send MSG_REFRESH_BOTTOM_CONTROLS");
                mHandler.obtainMessage(MSG_REFRESH_BOTTOM_CONTROLS, isPanorama ? 1 : 0, isPanorama360 ? 1 : 0, mediaObject).sendToTarget();
            }
        }
    };

    private final PanoramaSupportCallback mUpdateShareURICallback = new PanoramaSupportCallback() {
        @Override
        public void panoramaInfoAvailable(MediaObject mediaObject, boolean isPanorama,
                boolean isPanorama360) {
            if (mediaObject == mCurrentPhoto) {
                mHandler.obtainMessage(MSG_UPDATE_SHARE_URI, isPanorama360 ? 1 : 0, 0, mediaObject).sendToTarget();
            }
        }
    };

    public static interface Model extends PhotoView.Model {
        public void resume();
        public void pause();
        public boolean isEmpty();
        public void setCurrentPhoto(Path path, int indexHint);
        //TCL ShenQianfeng Begin on 2016.07.05
        public int getTotalCount();
        public MediaSet getSource();
        //TCL ShenQianfeng End on 2016.07.05
    }

    private class MyMenuVisibilityListener implements OnMenuVisibilityListener {
        @Override
        public void onMenuVisibilityChanged(boolean isVisible) {
            mIsMenuVisible = isVisible;
            refreshHidingMessage();
        }
    }

    @Override
    protected int getBackgroundColorId() {
        // TCL ShenQianfeng Begin on 2016.08.04
        // Original:
        //return R.color.photo_background;
        // Modify To:
        if(mShowBars) {
            return R.color.photo_white_background;
        } else {
            return R.color.photo_background;
        }
        // TCL ShenQianfeng End on 2016.08.04
        
    }

    // TCL ShenQianfeng Begin on 2016.09.08
    // Original:
     private GLView mRootPane;
    // Modify To:
    // we move it into onCreate, 
    // because there is no need to new mRootPane if this PhotoPage is embedded.
    /*
    private final GLView mRootPane = new GLView() {
        @Override
        protected void onLayout(
                boolean changed, int left, int top, int right, int bottom) {
            mPhotoView.layout(0, 0, right - left, bottom - top);
            if (mShowDetails) {
                mDetailsHelper.layout(left, mActionBar.getHeight(), right, bottom);
            }
        }
    };
    */

    // TCL ShenQianfeng End on 2016.09.08

    public class PhotoPageSynchronizedHandler extends SynchronizedHandler {

        public PhotoPageSynchronizedHandler(GLRoot root) {
            super(root);
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                // TCL ShenQianfeng Begin on 2016.10.09
                // Annotated Below:
                /*
                case MSG_HIDE_BARS: {
                    hideBars();
                    break;
                }
                */
                // TCL ShenQianfeng End on 2016.10.09
                case MSG_REFRESH_BOTTOM_CONTROLS: {
                    if (mCurrentPhoto == message.obj && mBottomControls != null) {
                        mIsPanorama = message.arg1 == 1;
                        mIsPanorama360 = message.arg2 == 1;
                        //LogUtil.d(TAG, "handleMessage MSG_REFRESH_BOTTOM_CONTROLS call mBottomControls.refresh ");
                        mBottomControls.refresh();
                    }
                    break;
                }
                case MSG_ON_FULL_SCREEN_CHANGED: {
                    if (mAppBridge != null) {
                        mAppBridge.onFullScreenChanged(message.arg1 == 1);
                    }
                    break;
                }
                case MSG_UPDATE_ACTION_BAR: {
                    updateBars();
                    break;
                }
                case MSG_WANT_BARS: {
                    wantBars();
                    break;
                }
                case MSG_UNFREEZE_GLROOT: {
                    mActivity.getGLRoot().unfreeze();
                    break;
                }
                case MSG_UPDATE_DEFERRED: {
                    long nextUpdate = mDeferUpdateUntil - SystemClock.uptimeMillis();
                    if (nextUpdate <= 0) {
                        mDeferredUpdateWaiting = false;
                        updateUIForCurrentPhoto();
                    } else {
                        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DEFERRED, nextUpdate);
                    }
                    break;
                }
                case MSG_ON_CAMERA_CENTER: {
                    mSkipUpdateCurrentPhoto = false;
                    boolean stayedOnCamera = false;
                    if (!mPhotoView.getFilmMode()) {
                        stayedOnCamera = true;
                    } else if (SystemClock.uptimeMillis() < mCameraSwitchCutoff &&
                            mMediaSet.getMediaItemCount() > 1) {
                        mPhotoView.switchToImage(1);
                    } else {
                        if (mAppBridge != null) mPhotoView.setFilmMode(false);
                        stayedOnCamera = true;
                    }
    
                    if (stayedOnCamera) {
                        if (mAppBridge == null && mMediaSet.getTotalMediaItemCount() > 1) {
                            launchCamera();
                            /* We got here by swiping from photo 1 to the
                               placeholder, so make it be the thing that
                               is in focus when the user presses back from
                               the camera app */
                            mPhotoView.switchToImage(1);
                        } else {
                            updateBars();
                            updateCurrentPhoto(mModel.getMediaItem(0));
                        }
                    }
                    break;
                }
                case MSG_ON_PICTURE_CENTER: {
                    if (!mPhotoView.getFilmMode() && mCurrentPhoto != null
                            && (mCurrentPhoto.getSupportedOperations() & MediaObject.SUPPORT_ACTION) != 0) {
                        mPhotoView.setFilmMode(true);
                    }
                    break;
                }
                case MSG_REFRESH_IMAGE: {
                    final MediaItem photo = mCurrentPhoto;
                    mCurrentPhoto = null;
                    updateCurrentPhoto(photo);
                    break;
                }
                case MSG_UPDATE_PHOTO_UI: {
                    updateUIForCurrentPhoto();
                    break;
                }
                case MSG_UPDATE_SHARE_URI: {
                    if (mCurrentPhoto == message.obj) {
                        boolean isPanorama360 = message.arg1 != 0;
                        Uri contentUri = mCurrentPhoto.getContentUri();
                        Intent panoramaIntent = null;
                        if (isPanorama360) {
                            panoramaIntent = createSharePanoramaIntent(contentUri);
                        }
                        Intent shareIntent = createShareIntent(mCurrentPhoto);
                        mActionBar.setShareIntents(panoramaIntent, shareIntent, PhotoPage.this);
                        setNfcBeamPushUri(contentUri);
                    }
                    break;
                }
                case MSG_UPDATE_PANORAMA_UI: {
                    if (mCurrentPhoto == message.obj) {
                        boolean isPanorama360 = message.arg1 != 0;
                        updatePanoramaUI(isPanorama360);
                    }
                    break;
                }
                // TCL ShenQianfeng Begin on 2016.09.14
                case MSG_REBUILD_TOOLBAR: {
                    if(! mEmbeddedRebuilt) {
                        long time = System.currentTimeMillis();
                        rebuildToolBar();
                        //LogUtil.d(TAG, "======= rebuildToolBar:  " + (System.currentTimeMillis() - time));
                        time = System.currentTimeMillis();
                        PhotoDataAdapter pda = (PhotoDataAdapter)mModel;
                        updatePhotoIndexIndicator(pda.getEnteringIndex() + 1, pda.getTotalCount());
                        //LogUtil.d(TAG, "=======  updatePhotoIndexIndicator:  " + (System.currentTimeMillis() - time));
                        
                        time = System.currentTimeMillis();
                        mActionBar.setToolbarChildViewsAlpha(mActionBar.getChildViewAlpha());
                        //LogUtil.d(TAG, "=======  setToolbarChildViewsAlpha:  " + (System.currentTimeMillis() - time));
                        mEmbeddedRebuilt = true;
                    }
                    break;
                }
                case MSG_SET_TOOLBAR_CHILD_VIEWS_ALPHA: {
                    float alphaProgress = ((Float)message.obj).floatValue();
                    mActionBar.setToolbarChildViewsAlpha(alphaProgress);
                    break;
                }
                case MSG_SET_BOTTOM_CONTROLS_ALPHA: {
                    if(mBottomControls != null) {
                        float alphaProgress = ((Float)message.obj).floatValue();
                        boolean fadeIn = message.arg1 == 1;
                        mBottomControls.setAlpha(alphaProgress, fadeIn);
                        //LogUtil.d(TAG, "fadeIn:" + fadeIn +  " alphaProgress:" + alphaProgress) ;
                    }
                    break;
                }
                case MSG_ON_ZOOM_IN_START:
                    mBottomControls.updatePadding(mActivity.getWindowInsets());
                    break;
                case MSG_ON_ZOOM_IN_END: {
                    //LogUtil.d(TAG, "PhotoPage::PhotoPageSynchronizedHandler::MSG_ON_ZOOM_IN_END ");
                    updateBackgroundColor();
                    resumeEmbedded(true);
                    boolean isPortrait = mActivity.isPortrait();
                    SystemUIManager systemUIManager = mActivity.getSystemUIManager();
                    boolean occupyNavigationBar = isPortrait ? true : false;
                    systemUIManager.setFlag(false/*lightsOut*/, occupyNavigationBar, Color.TRANSPARENT);
                    //mBottomControls.updatePadding(isPortrait);
                    //mBottomControls.updatePadding(mActivity.getWindowInsets());
                    // TCL BaiYuan Begin on 2016.11.22
                    sendMessageToRebuildMenu();
                    // TCL BaiYuan End on 2016.11.22
                    break;
                }
                case MSG_ON_ZOOM_OUT_START: {
                    //LogUtil.d(TAG, "PhotoPage::PhotoPageSynchronizedHandler::MSG_ON_ZOOM_OUT_START ");
                    SystemUIManager systemUIManager = mActivity.getSystemUIManager();
                    boolean isPortrait = mActivity.isPortrait();
                    boolean occupyNavigationBar = isPortrait ? true : false;
                    systemUIManager.setFlag(false, occupyNavigationBar, Color.WHITE);
                    break;
                }
                case MSG_ON_ZOOM_OUT_END: {
                    pauseEmbedded();
                    showEmbedded(false);
                    mPhotoView.clearEnteringBitmapInfo();
                    mCurrentIndex = -1;
                    break;
                }
                case MSG_EMBEDDED_BACK_WITHOUT_ANIM: {
                    pauseEmbedded();
                    showEmbedded(false);
                    mPhotoView.clearEnteringBitmapInfo();
                    mCurrentIndex = -1;
                    
                    break;
                }
                case MSG_REBUILD_TOOLBAR_MENU: {
                    // TCL BaiYuan Begin on 2016.11.22
                    // Original:
                    /*
                    rebuildToolBarMenu();
                    */
                    // Modify To:
                    rebuildToolBarMenu(null);
                    // TCL BaiYuan Begin on 2016.11.22
                    break;
                }
                case MSG_MODIFY_SYSTEM_UI_WHEN_RESUME:
                    //hideBars();
                    break;
                // TCL ShenQianfeng End on 2016.09.14
                default: throw new AssertionError(message.what);
            }
        }
    }

    PhotoDataAdapter.DataListener mDataListener = new PhotoDataAdapter.DataListener() {
        @Override
        public void onPhotoChanged(int index, Path item) {
            
            // TCL ShenQianfeng Begin on 2016.09.12
            if(-1 == index) {
                return;
            }
            // TCL ShenQianfeng End on 2016.09.12
            
            int oldIndex = mCurrentIndex;
            mCurrentIndex = index;
            
            /*
            LogUtil.d(TAG, "PhotoPage::onPhotoChanged index:" + index + " item:" + item + 
                    " mHasCameraScreennailOrPlaceholder:" + mHasCameraScreennailOrPlaceholder + 
                    " oldIndex:" + oldIndex);
            */
            
            if (mHasCameraScreennailOrPlaceholder) {
                if (mCurrentIndex > 0) {
                    mSkipUpdateCurrentPhoto = false;
                }

                if (oldIndex == 0 && mCurrentIndex > 0 && !mPhotoView.getFilmMode()) {
                    //LogUtil.d(TAG, "PhotoPage::onPhotoChanged 111111");
                    mPhotoView.setFilmMode(true);
                    // TCL ShenQianfeng Begin on 2016.09.12
                    // Annotated Below:
                    /*
                    if (mAppBridge != null) {
                        UsageStatistics.onEvent("CameraToFilmstrip", UsageStatistics.TRANSITION_SWIPE, null);
                    }
                    */
                    // TCL ShenQianfeng End on 2016.09.12
                } else if (oldIndex == 2 && mCurrentIndex == 1) {
                    //LogUtil.d(TAG, "PhotoPage::onPhotoChanged 222222");
                    mCameraSwitchCutoff = SystemClock.uptimeMillis() + CAMERA_SWITCH_CUTOFF_THRESHOLD_MS;
                    mPhotoView.stopScrolling();
                } else if (oldIndex >= 1 && mCurrentIndex == 0) {
                    //LogUtil.d(TAG, "PhotoPage::onPhotoChanged 333333");
                    mPhotoView.setWantPictureCenterCallbacks(true);
                    mSkipUpdateCurrentPhoto = true;
                }
            }
            if (!mSkipUpdateCurrentPhoto) {
                if (item != null) {
                    //LogUtil.d(TAG, "PhotoPage::onPhotoChanged 444444");
                    MediaItem photo = mModel.getMediaItem(0);
                    if (photo != null) updateCurrentPhoto(photo);
                }
                updateBars();
            }
            // Reset the timeout for the bars after a swipe
            refreshHidingMessage();
            
            // TCL ShenQianfeng Begin on 2016.07.06
            updatePhotoIndexIndicator();
            // TCL ShenQianfeng End on 2016.07.06
        }

        @Override
        public void onLoadingFinished(boolean loadingFailed) {
            if (!mModel.isEmpty()) {
                MediaItem photo = mModel.getMediaItem(0);
                if (photo != null) updateCurrentPhoto(photo);
            } else if (mIsActive) {
                // We only want to finish the PhotoPage if there is no
                // deletion that the user can undo.
                if (mMediaSet.getNumberOfDeletions() == 0) {
                    mActivity.getStateManager().finishState(
                            PhotoPage.this);
                }
            }
        }

        @Override
        public void onLoadingStarted() {
        }
        
        // TCL BaiYuan Begin on 2016.11.14
        // Original:
        /*
        // TCL ShenQianfeng Begin on 2016.08.10
        @Override
        public void onNotifyEmpty() {
                onBackPressed();
        }
        // TCL ShenQianfeng End on 2016.08.10
        */
        // Modify To:
        @Override
        public void onNotifyEmpty(boolean isEmpty) {
            onBackPressed();
        }
        // TCL BaiYuan Begin on 2016.11.14
    };
    
    public class PhotoPageRootPane extends GLView {
        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            mPhotoView.layout(0, 0, right - left, bottom - top);
            if (mShowDetails) {
                mDetailsHelper.layout(left, mActionBar.getHeight(), right, bottom);
            }
        }
        
        @Override
        public void onWindowsInsetsChanged(Rect newWindowInsets) {
            for(int i = 0; i < getComponentCount(); i++) {
                GLView view = getComponent(i);
                view.onWindowsInsetsChanged(newWindowInsets);
            }
        }
    }

    @Override
    public void onCreate(Bundle data, Bundle restoreState) {
        super.onCreate(data, restoreState);
        // TCL ShenQianfeng Begin on 2016.09.08
        mRootPane = new PhotoPageRootPane();
        // TCL ShenQianfeng End on 2016.09.08
        mActionBar = mActivity.getGalleryActionBar();
        mSelectionManager = new SelectionManager(mActivity, false);
        mMenuExecutor = new MenuExecutor(mActivity, mSelectionManager);
        // TCL ShenQianfeng Begin on 2016.08.13
        createIndicatorTextView();
        loadNavigationIcon();
        rebuildToolBar();
        // TCL BaiYuan Begin on 2016.11.22
        // Original:
        /*
        rebuildToolBarMenu();
        */
        // Modify
        rebuildToolBarMenu(null);
        // TCL BaiYuan Begin on 2016.11.22
        SystemUIManager systemUIManager = mActivity.getSystemUIManager();
        RelativeLayout galleryRoot = (RelativeLayout) ((Activity) mActivity).findViewById(mAppBridge != null ? R.id.content : R.id.gallery_root);
        if (galleryRoot != null) {
          if (mSecureAlbum == null) {
              //SystemUIManager systemUIManager = mActivity.getSystemUIManager();
              int navigationBarHeight = systemUIManager.hasVirtualKeys() ? systemUIManager.getNavigationBarHeight() : 0;
              mBottomControls = new PhotoPageBottomControls(this, mActivity, galleryRoot, navigationBarHeight);
              mBottomControls.updatePadding(mActivity.getWindowInsets());
          }
        }
        
        // TCL ShenQianfeng End on 2016.08.13
        mPhotoView = new PhotoView(mActivity);
        mPhotoView.setListener(this);
        mRootPane.addComponent(mPhotoView);
        mApplication = (GalleryApp) ((Activity) mActivity).getApplication();
        mOrientationManager = mActivity.getOrientationManager();
        mActivity.getGLRoot().setOrientationSource(mOrientationManager);
        // TCL ShenQianfeng Begin on 2016.10.24
        //mActivity.getGLRoot().setLightsOutMode(false);
        
        /*
        boolean lightsOut = false;
        boolean occupyNavigationBar = mActivity.isPortrait();
        int navigationBarColor = Color.TRANSPARENT;
        systemUIManager.setFlag(lightsOut, occupyNavigationBar, navigationBarColor);
        */
        
        // TCL ShenQianfeng End on 2016.10.24
        // ShenQianfeng move the function body into PhotoPageSynchronizedHandler
        mHandler = new PhotoPageSynchronizedHandler(mActivity.getGLRoot());
        mSetPathString = data.getString(KEY_MEDIA_SET_PATH);
        mReadOnlyView = data.getBoolean(KEY_READONLY);
        mOriginalSetPathString = mSetPathString;
        setupNfcBeamPush();
        String itemPathString = data.getString(KEY_MEDIA_ITEM_PATH);
        Path itemPath = itemPathString != null ? Path.fromString(data.getString(KEY_MEDIA_ITEM_PATH)) : null;
        mTreatBackAsUp = data.getBoolean(KEY_TREAT_BACK_AS_UP, false);
        mStartInFilmstrip = data.getBoolean(KEY_START_IN_FILMSTRIP, false);
        boolean inCameraRoll = data.getBoolean(KEY_IN_CAMERA_ROLL, false);
        mCurrentIndex = data.getInt(KEY_INDEX_HINT, 0);
        if (mSetPathString != null) {
            
            // TCL ShenQianfeng Begin on 2016.07.06
            // Annotated Below:
            // mShowSpinner = true;
            // TCL ShenQianfeng End on 2016.07.06
            
            mAppBridge = (AppBridge) data.getParcelable(KEY_APP_BRIDGE);
            if (mAppBridge != null) {
                mShowBars = false;
                mHasCameraScreennailOrPlaceholder = true;
                mAppBridge.setServer(this);

                // Get the ScreenNail from AppBridge and register it.
                int id = SnailSource.newId();
                Path screenNailSetPath = SnailSource.getSetPath(id);
                Path screenNailItemPath = SnailSource.getItemPath(id);
                mScreenNailSet = (SnailAlbum) mActivity.getDataManager().getMediaObject(screenNailSetPath);
                mScreenNailItem = (SnailItem) mActivity.getDataManager().getMediaObject(screenNailItemPath);
                mScreenNailItem.setScreenNail(mAppBridge.attachScreenNail());
                if (data.getBoolean(KEY_SHOW_WHEN_LOCKED, false)) {
                    // Set the flag to be on top of the lock screen.
                    mFlags |= FLAG_SHOW_WHEN_LOCKED;
                }

                // Don't display "empty album" action item for capture intents.
                if (!mSetPathString.equals("/local/all/0")) {
                    // Check if the path is a secure album.
                    if (SecureSource.isSecurePath(mSetPathString)) {
                        mSecureAlbum = (SecureAlbum) mActivity.getDataManager()
                                .getMediaSet(mSetPathString);
                        // TCL ShenQianfeng Begin on 2016.07.06
                        // Annotated Below:
                        // mShowSpinner = false;
                        // TCL ShenQianfeng End on 2016.07.06
                        
                    }
                    mSetPathString = "/filter/empty/{"+mSetPathString+"}";
                }

                // Combine the original MediaSet with the one for ScreenNail
                // from AppBridge.
                mSetPathString = "/combo/item/{" + screenNailSetPath +
                        "," + mSetPathString + "}";

                // Start from the screen nail.
                itemPath = screenNailItemPath;
            } else if (inCameraRoll && GalleryUtils.isCameraAvailable(mActivity)) {
                mSetPathString = "/combo/item/{" + FilterSource.FILTER_CAMERA_SHORTCUT +
                        "," + mSetPathString + "}";
                mCurrentIndex++;
                mHasCameraScreennailOrPlaceholder = true;
            }

            MediaSet originalSet = mActivity.getDataManager()
                    .getMediaSet(mSetPathString);
            if (mHasCameraScreennailOrPlaceholder && originalSet instanceof ComboAlbum) {
                // Use the name of the camera album rather than the default
                // ComboAlbum behavior
                ((ComboAlbum) originalSet).useNameOfChild(1);
            }
            mSelectionManager.setSourceMediaSet(originalSet);
            mSetPathString = "/filter/delete/{" + mSetPathString + "}";
            mMediaSet = (FilterDeleteSet) mActivity.getDataManager()
                    .getMediaSet(mSetPathString);
            if (mMediaSet == null) {
                Log.w(TAG, "failed to restore " + mSetPathString);
            }
            if (itemPath == null) {
                int mediaItemCount = mMediaSet.getMediaItemCount();
                if (mediaItemCount > 0) {
                    if (mCurrentIndex >= mediaItemCount) mCurrentIndex = 0;
                    itemPath = mMediaSet.getMediaItem(mCurrentIndex, 1)
                        .get(0).getPath();
                } else {
                    // Bail out, PhotoPage can't load on an empty album
                    return;
                }
            }
            PhotoDataAdapter pda = new PhotoDataAdapter(
                    mActivity, mPhotoView, mMediaSet, itemPath, mCurrentIndex,
                    mAppBridge == null ? -1 : 0,
                    mAppBridge == null ? false : mAppBridge.isPanorama(),
                    mAppBridge == null ? false : mAppBridge.isStaticCamera());
            mModel = pda;
            mPhotoView.setModel(mModel);
            //ShenQianfeng move mDataListener function body to PhotoPage's data member
            pda.setDataListener(mDataListener);
        } else {
            // Get default media set by the URI
            MediaItem mediaItem = (MediaItem)mActivity.getDataManager().getMediaObject(itemPath);
            mModel = new SinglePhotoDataAdapter(mActivity, mPhotoView, mediaItem);
            mPhotoView.setModel(mModel);
            updateCurrentPhoto(mediaItem);
            // TCL ShenQianfeng Begin on 2016.07.06
            // Annotated Below:
            // mShowSpinner = false;
            // TCL ShenQianfeng End on 2016.07.06
        }
        mPhotoView.setFilmMode(mStartInFilmstrip && mMediaSet.getMediaItemCount() > 1);
        // TCL ShenQianfeng Begin on 2016.11.01
        // Annotated Below:
        /*  
          RelativeLayout galleryRoot = (RelativeLayout) ((Activity) mActivity).findViewById(mAppBridge != null ? R.id.content : R.id.gallery_root);
          if (galleryRoot != null) {
            if (mSecureAlbum == null) {
                //SystemUIManager systemUIManager = mActivity.getSystemUIManager();
                int navigationBarHeight = systemUIManager.hasVirtualKeys() ? systemUIManager.getNavigationBarHeight() : 0;
                mBottomControls = new PhotoPageBottomControls(this, mActivity, galleryRoot, navigationBarHeight);
                mBottomControls.updatePadding(mActivity.isPortrait());
            }
          }
         */
        // TCL ShenQianfeng End on 2016.11.01
    }
    
    

    @Override
    public void onMstToolbarNavigationClicked(View view) {
        onBackPressed();
    }



    @Override
    public boolean onMstToolbarMenuItemClicked(MenuItem item) {
        int itemId = item.getItemId();
        switch(itemId) {
            case R.id.action_share: {
                Intent shareIntent = createShareIntent(mCurrentPhoto);
                mActivity.startActivity(shareIntent);
                return true;
            }
            case R.id.action_setas: {
                if (mModel == null) return true;
                refreshHidingMessage();
                MediaItem current = mModel.getMediaItem(0);

                // This is a shield for monkey when it clicks the action bar
                // menu when transitioning from filmstrip to camera
                if (current instanceof SnailItem) return true;
                // TODO: We should check the current photo against the MediaItem
                // that the menu was initially created for. We need to fix this
                // after PhotoPage being refactored.
                if (current == null) {
                    // item is not ready, ignore
                    return true;
                }
                int currentIndex = mModel.getCurrentIndex();
                Path path = current.getPath();

                DataManager manager = mActivity.getDataManager();
                int action = item.getItemId();
                mSelectionManager.deSelectAll();
                 // TCL BaiYuan Begin on 2016.11.02
                 // Original:
                 /*
                mSelectionManager.toggle(path);
                */
                // Modify To:
                mSelectionManager.toggle(current);
                // TCL BaiYuan End on 2016.11.02
                mMenuExecutor.onMenuClicked(item, null, mConfirmDialogListener);
                return true;
            }
            case R.id.action_detail: {
                if (mShowDetails) {
                    hideDetails();
                } else {
                    showDetails();
                }
                return true;
            }
        }
        return false;
    }
    
    private void loadNavigationIcon() {
        mNavigationIcon = mActivity.getDrawable(com.mst.R.drawable.ic_toolbar_back);
    }
 
    private void createIndicatorTextView() {
        if(mIndicatorTextView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            mIndicatorTextView = (TextView)inflater.inflate(R.layout.mst_photo_page_indicator_textview, null);
        }
    }


    @Override
    protected boolean rebuildToolBar() {
        //long time = System.currentTimeMillis();
        Toolbar toolBar = mActivity.getToolbar();
        toolBar.setNavigationIcon(mNavigationIcon);
        toolBar.setTitle("");
        //LogUtil.d(TAG, " rebuildToolbar 00000 ---> :" + (System.currentTimeMillis() - time));
        mIndicatorTextViewLayoutParams.rightMargin = mActivity.getResources().getDisplayMetrics().densityDpi * TOOLBAR_MARGIN_IN_DP / 160;
        if(mIndicatorTextView.getParent() == null) {
            toolBar.addView(mIndicatorTextView, mIndicatorTextViewLayoutParams);
        }
        //indicatorTextView.setText("");
        return super.rebuildToolBar();
    }
    
    
    // TCL BaiYuan Begin on 2016.11.22
    // Original:
    /*
    private void rebuildToolBarMenu() {
        Toolbar toolbar = mActivity.getToolbar();
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.mst_photopage_actionitem);
        // TCL BaiYuan Begin on 2016.11.22
        if (mCurrentPhoto != null) {
            int type = mCurrentPhoto.getMediaType();
            if (MediaObject.MEDIA_TYPE_VIDEO == type) {
                Menu menu = mActivity.getToolbar().getMenu();
                menu.removeItem(R.id.action_setas);
            }
        }
    }
    */

    private void rebuildToolBarMenu(MediaItem photo) {
        int prevMediaType = MediaObject.MEDIA_TYPE_UNKNOWN;
        int currentMediaType = MediaObject.MEDIA_TYPE_UNKNOWN;
        if(mCurrentPhoto != null) {
            prevMediaType = mCurrentPhoto.getMediaType();
        }
        if(photo != null) {
            currentMediaType = photo.getMediaType();
        }
        if((prevMediaType == MediaObject.MEDIA_TYPE_UNKNOWN && currentMediaType == MediaObject.MEDIA_TYPE_UNKNOWN) || prevMediaType != currentMediaType) {
            Menu menu = mActivity.getToolbar().getMenu();
            if (MediaObject.MEDIA_TYPE_VIDEO == currentMediaType) {
                menu.removeItem(R.id.action_setas);
            }else{
                Toolbar toolbar = mActivity.getToolbar();
                toolbar.getMenu().clear();
                toolbar.inflateMenu(R.menu.mst_photopage_actionitem);
            }
        }
    }
    // TCL BaiYuan End on 2016.11.22
    
    // TCL ShenQianfeng Begin on 2016.08.15
    private void removeIndicatorView() {
        Toolbar toolBar = mActivity.getToolbar();
        TextView indicatorTextView = (TextView)toolBar.findViewById(R.id.mst_photo_page_indicator);
        if(null != indicatorTextView) {
            toolBar.removeView(indicatorTextView);
        }
    }
    
    @Override
    protected void onConfigurationChanged(Configuration config) {
        //boolean hasNavigationBarHeight = mActivity.isPortrait(config);
        //mBottomControls.updatePadding(hasNavigationBarHeight);
        SystemUIManager systemUIManager = mActivity.getSystemUIManager();
        boolean isPortrait = mActivity.isPortrait();
        boolean occupyNavigationBar = isPortrait ? true : false;
        boolean lightsOut = ! mShowBars;
        int color = isPortrait ? Color.TRANSPARENT : (mShowBars ? Color.WHITE : Color.BLACK);
        systemUIManager.setFlag(lightsOut/*lightsOut*/, 
                occupyNavigationBar /*occupyNavigationBar*/, 
                color);
    }
    // TCL ShenQianfeng End on 2016.08.15

    @Override
    public void onPictureCenter(boolean isCamera) {
        isCamera = isCamera || (mHasCameraScreennailOrPlaceholder && mAppBridge == null);
        mPhotoView.setWantPictureCenterCallbacks(false);
        mHandler.removeMessages(MSG_ON_CAMERA_CENTER);
        mHandler.removeMessages(MSG_ON_PICTURE_CENTER);
        mHandler.sendEmptyMessage(isCamera ? MSG_ON_CAMERA_CENTER : MSG_ON_PICTURE_CENTER);
    }

    @Override
    public boolean canDisplayBottomControls() {
        // TCL ShenQianfeng Begin on 2016.07.07
        // Original:
        // return mIsActive && !mPhotoView.canUndo();
        // Modify To:
        boolean canDisplay = /*mIsActive &&*/ !mPhotoView.canUndo() && mShowBars;
        if(isEmbedded()) {
            canDisplay &= isShowingEmbedded();
        }
        /*
        LogUtil.i2(TAG, "canDisplayBottomControls canDisplay:" + canDisplay + " mIsActive:" + mIsActive + 
                " !mPhotoView.canUndo():" + (!mPhotoView.canUndo()) + 
                " mShowBars:" + mShowBars); */ 
        return canDisplay;
        // TCL ShenQianfeng End on 2016.07.07
    }
    
    @Override
    public boolean canDisplayBottomControlWhenZoomingIn(int control) {
        /*
        if (mCurrentPhoto == null) {
            LogUtil.d(TAG, "canDisplayBottomControlWhenZoomingIn mCurrentPhoto == null return true");
            return true;
        }
        */
        PhotoDataAdapter pda = (PhotoDataAdapter)mModel;
        EnteringBitmapInfo info = pda.getEnteringBitmapInfo();
        if(info == null) {
            //LogUtil.d(TAG, "canDisplayBottomControlWhenZoomingIn info == null control:" + control + " return false");
            return false;
        }
        switch(control) {
        case R.id.photo_page_bottom_control_edit:
            //LogUtil.d(TAG, " ! info.isGif :" +  ! info.isGif  + " info.isImage:" + info.isImage + " mCurrentIndex == info.index:" + (mCurrentIndex == info.index));
            //LogUtil.d(TAG, " ! info.isGif && info.isImage && mCurrentIndex == info.index : " +  (! info.isGif && info.isImage && mCurrentIndex == info.index));
            return ! info.isGif && info.isImage && mCurrentIndex == info.index;
        case R.id.photo_page_bottom_control_delete:
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean canDisplayBottomControl(int control) {
        // TCL ShenQianfeng Begin on 2016.07.07
        // Original:
        /*
        if (mCurrentPhoto == null) {
            LogUtil.d(TAG, "canDisplayBottomControl mCurrentPhoto == null return false");
            return false;
        }
        switch(control) {
        case R.id.photopage_bottom_control_edit:
            return mHaveImageEditor && mShowBars && !mReadOnlyView
                    && !mPhotoView.getFilmMode()
                    && (mCurrentPhoto.getSupportedOperations() & MediaItem.SUPPORT_EDIT) != 0
                    && mCurrentPhoto.getMediaType() == MediaObject.MEDIA_TYPE_IMAGE;
        case R.id.photopage_bottom_control_panorama:
            return mIsPanorama;
        case R.id.photopage_bottom_control_tiny_planet:
            return mHaveImageEditor && mShowBars
                    && mIsPanorama360 && !mPhotoView.getFilmMode();
        default:
            return false;
        }
        */
        // Modify To:
        if (mCurrentPhoto == null) {
            if(mEmbedded) {
                /*
                LogUtil.d(TAG, "canDisplayBottomControl mCurrentPhoto == null && mEmbedded return "
                        + "canDisplayBottomControl mCurrentPhoto == null return false");
                        */
                return canDisplayBottomControlWhenZoomingIn(control);
            } 
            return false;
        }
        switch(control) {
        case R.id.photo_page_bottom_control_edit:
            /*
            LogUtil.d(TAG, "11111 mHaveImageEditor: " + mHaveImageEditor
                    + " mShowBars: " + mShowBars + 
                    " mCurrentPhoto.getSupportedOperations() & MediaItem.SUPPORT_EDIT:" + (mCurrentPhoto.getSupportedOperations() & MediaItem.SUPPORT_EDIT) 
                    + " mCurrentPhoto.getMediaType() == MediaObject.MEDIA_TYPE_IMAGE:" + (mCurrentPhoto.getMediaType() == MediaObject.MEDIA_TYPE_IMAGE));
            */
            return /*mHaveImageEditor && mShowBars && */  !mReadOnlyView
                    && !mPhotoView.getFilmMode()
                    && (mCurrentPhoto.getSupportedOperations() & MediaItem.SUPPORT_EDIT) != 0
                    && mCurrentPhoto.getMediaType() == MediaObject.MEDIA_TYPE_IMAGE;
        case R.id.photo_page_bottom_control_delete:
            /*
            LogUtil.d(TAG, "2222   !mPhotoView.getFilmMode(): " + ( !mPhotoView.getFilmMode())
                    + " mShowBars: " + mShowBars + 
                    "  !mReadOnlyView:" +  !mReadOnlyView
                    + " mCurrentPhoto.getSupportedOperations() & MediaItem.SUPPORT_DELETE:" + (mCurrentPhoto.getSupportedOperations() & MediaItem.SUPPORT_DELETE));
                    */
            return  !mPhotoView.getFilmMode() /*&& mShowBars*/ && !mReadOnlyView &&
                   (mCurrentPhoto.getSupportedOperations() & MediaItem.SUPPORT_DELETE) != 0;
        default:
            return false;
        }
        // TCL ShenQianfeng End on 2016.07.07
        
    }

    @Override
    public void onBottomControlClicked(int control) {
        // TCL ShenQianfeng Begin on 2016.07.07
        // Original:
        /*
         switch(control) {
            case R.id.photopage_bottom_control_edit:
                launchPhotoEditor();
                return;
            case R.id.photopage_bottom_control_panorama:
                mActivity.getPanoramaViewHelper()
                        .showPanorama(mCurrentPhoto.getContentUri());
                return;
            case R.id.photopage_bottom_control_tiny_planet:
                launchTinyPlanet();
                return;
            default:
                return;
        }
         */
        // Modify To:
        switch(control) {
        case R.id.photo_page_bottom_control_edit:
            launchPhotoEditor();
            return;
        case R.id.photo_page_bottom_control_delete:
            deleteMediaItem();
            return;
        default:
            return;
        }
        // TCL ShenQianfeng End on 2016.07.07
        
    }
    
    
    // TCL ShenQianfeng Begin on 2016.07.07
    private void deleteMediaItem() {
        if (mModel == null) return;
        refreshHidingMessage();
        MediaItem current = mModel.getMediaItem(0);
        // This is a shield for monkey when it clicks the action bar
        // menu when transitioning from filmstrip to camera
        if (current instanceof SnailItem) return;
        // TODO: We should check the current photo against the MediaItem
        // that the menu was initially created for. We need to fix this
        // after PhotoPage being refactored.
        if (current == null) {
            // item is not ready, ignore
            return;
        }

        // TCL BaiYuan Begin on 2016.11.15
        // Original:
        /*
        int currentIndex = mModel.getCurrentIndex();
        Path path = current.getPath();
        String confirmMsg = mActivity.getResources().getQuantityString(R.plurals.delete_selection, 1);
        */
        // TCL BaiYuan Begin on 2016.11.15
        mSelectionManager.deSelectAll();
         // TCL BaiYuan Begin on 2016.11.02
         // Original:
         /*
        mSelectionManager.toggle(path);
        */
        // Modify To:
        mSelectionManager.toggle(current);
        String confirmMsg = mActivity.getResources().getQuantityString(R.plurals.delete_selection, 1) ;
        int type = mSelectionManager.getType();
        if (MediaObject.MEDIA_TYPE_VIDEO == type) {
            confirmMsg = mActivity.getResources().getString(R.string.delete_photo_selection, mActivity.getResources().getString(R.string.delete_video_item, ""));
        }else if(MediaObject.MEDIA_TYPE_IMAGE == type){
            confirmMsg = mActivity.getResources().getString(R.string.delete_photo_selection, mActivity.getResources().getString(R.string.delete_picture_item, ""));
        }
        // TCL BaiYuan End on 2016.11.02
        mMenuExecutor.onMenuClicked(R.id.action_delete, confirmMsg, mConfirmDialogListener);
    }
    // TCL ShenQianfeng End on 2016.07.07
    

    @TargetApi(ApiHelper.VERSION_CODES.JELLY_BEAN)
    private void setupNfcBeamPush() {
        if (!ApiHelper.HAS_SET_BEAM_PUSH_URIS) return;

        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(mActivity);
        if (adapter != null) {
            adapter.setBeamPushUris(null, mActivity);
            adapter.setBeamPushUrisCallback(new CreateBeamUrisCallback() {
                @Override
                public Uri[] createBeamUris(NfcEvent event) {
                    return mNfcPushUris;
                }
            }, mActivity);
        }
    }

    private void setNfcBeamPushUri(Uri uri) {
        mNfcPushUris[0] = uri;
    }

    private static Intent createShareIntent(MediaObject mediaObject) {
        int type = mediaObject.getMediaType();
        return new Intent(Intent.ACTION_SEND)
                .setType(MenuExecutor.getMimeType(type))
                .putExtra(Intent.EXTRA_STREAM, mediaObject.getContentUri())
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    private static Intent createSharePanoramaIntent(Uri contentUri) {
        return new Intent(Intent.ACTION_SEND)
                .setType(GalleryUtils.MIME_TYPE_PANORAMA360)
                .putExtra(Intent.EXTRA_STREAM, contentUri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    private void overrideTransitionToEditor() {
        ((Activity) mActivity).overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    private void launchTinyPlanet() {
        // Deep link into tiny planet
        MediaItem current = mModel.getMediaItem(0);
        Intent intent = new Intent(FilterShowActivity.TINY_PLANET_ACTION);
        intent.setClass(mActivity, FilterShowActivity.class);
        intent.setDataAndType(current.getContentUri(), current.getMimeType())
            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(FilterShowActivity.LAUNCH_FULLSCREEN,
                mActivity.isFullscreen());
        mActivity.startActivityForResult(intent, REQUEST_EDIT);
        overrideTransitionToEditor();
    }

    private void launchCamera() {
        mRecenterCameraOnResume = false;
        GalleryUtils.startCameraActivity(mActivity);
    }

    private void launchPhotoEditor() {
        MediaItem current = mModel.getMediaItem(0);
        if (current == null || (current.getSupportedOperations()
                & MediaObject.SUPPORT_EDIT) == 0) {
            return;
        }

        Intent intent = new Intent(ACTION_NEXTGEN_EDIT);

        intent.setDataAndType(current.getContentUri(), current.getMimeType())
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (mActivity.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() == 0) {
            intent.setAction(Intent.ACTION_EDIT);
        }
        intent.putExtra(FilterShowActivity.LAUNCH_FULLSCREEN,
                mActivity.isFullscreen());
        // TCL ShenQianfeng Begin on 2016.09.01
        // Original:
        /*
        ((Activity) mActivity).startActivityForResult(Intent.createChooser(intent, null), REQUEST_EDIT); */
        // Modify To:
        ((Activity) mActivity).startActivityForResult(intent, REQUEST_EDIT);
        // TCL ShenQianfeng End on 2016.09.01

        overrideTransitionToEditor();
    }

    private void launchSimpleEditor() {
        MediaItem current = mModel.getMediaItem(0);
        if (current == null || (current.getSupportedOperations()
                & MediaObject.SUPPORT_EDIT) == 0) {
            return;
        }

        Intent intent = new Intent(ACTION_SIMPLE_EDIT);

        intent.setDataAndType(current.getContentUri(), current.getMimeType())
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (mActivity.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() == 0) {
            intent.setAction(Intent.ACTION_EDIT);
        }
        intent.putExtra(FilterShowActivity.LAUNCH_FULLSCREEN,
                mActivity.isFullscreen());
        ((Activity) mActivity).startActivityForResult(Intent.createChooser(intent, null),
                REQUEST_EDIT);
        overrideTransitionToEditor();
    }

    private void requestDeferredUpdate() {
        mDeferUpdateUntil = SystemClock.uptimeMillis() + DEFERRED_UPDATE_MS;
        if (!mDeferredUpdateWaiting) {
            mDeferredUpdateWaiting = true;
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DEFERRED, DEFERRED_UPDATE_MS);
        }
    }

    private void updateUIForCurrentPhoto() {
        if (mCurrentPhoto == null) return;
        //LogUtil.d(TAG, "updateUIForCurrentPhoto");
        // If by swiping or deletion the user ends up on an action item
        // and zoomed in, zoom out so that the context of the action is
        // more clear
        if ((mCurrentPhoto.getSupportedOperations() & MediaObject.SUPPORT_ACTION) != 0
                && !mPhotoView.getFilmMode()) {
            mPhotoView.setWantPictureCenterCallbacks(true);
        }

        // TCL ShenQianfeng Begin on 2016.07.06
        // Annotated Below:
        // updateMenuOperations();
        // TCL ShenQianfeng End on 2016.07.06
        
        refreshBottomControlsWhenReady(false);
        if (mShowDetails) {
            mDetailsHelper.reloadDetails();
        }
        if ((mSecureAlbum == null)
                && (mCurrentPhoto.getSupportedOperations() & MediaItem.SUPPORT_SHARE) != 0) {
            mCurrentPhoto.getPanoramaSupport(mUpdateShareURICallback);
        }
    }

    private void updateCurrentPhoto(MediaItem photo) {
        //LogUtil.d(TAG, "updateCurrentPhoto 111");
        if (mCurrentPhoto == photo) {
            //LogUtil.d(TAG, "updateCurrentPhoto mCurrentPhoto == photo return ..... ");
            return;
        }
        // TCL BaiYuan Begin on 2916.11.15
        rebuildToolBarMenu(photo);
        // TCL BaiYuan End on 2916.11.15
        //LogUtil.d(TAG, "updateCurrentPhoto 222");
        mCurrentPhoto = photo;
        if (mPhotoView.getFilmMode()) {
            requestDeferredUpdate();
        } else {
            updateUIForCurrentPhoto();
        }
    }

    // TCL ShenQianfeng Begin on 2016.07.06
    // Annotated Below:
    /* 
    private void updateMenuOperations() {
        Menu menu = mActionBar.getMenu();

        // it could be null if onCreateActionBar has not been called yet
        if (menu == null) return;

        MenuItem item = menu.findItem(R.id.action_slideshow);
        if (item != null) {
            item.setVisible((mSecureAlbum == null) && canDoSlideShow());
        }
        if (mCurrentPhoto == null) return;

        int supportedOperations = mCurrentPhoto.getSupportedOperations();
        if (mReadOnlyView) {
            supportedOperations ^= MediaObject.SUPPORT_EDIT;
        }
        if (mSecureAlbum != null) {
            supportedOperations &= MediaObject.SUPPORT_DELETE;
        } else {
            mCurrentPhoto.getPanoramaSupport(mUpdatePanoramaMenuItemsCallback);
            if (!mHaveImageEditor) {
                supportedOperations &= ~MediaObject.SUPPORT_EDIT;
            }
        }
        MenuExecutor.updateMenuOperation(menu, supportedOperations);
    }
    */
    // TCL ShenQianfeng End on 2016.07.06

    private boolean canDoSlideShow() {
        if (mMediaSet == null || mCurrentPhoto == null) {
            return false;
        }
        if (mCurrentPhoto.getMediaType() != MediaObject.MEDIA_TYPE_IMAGE) {
            return false;
        }
        return true;
    }

    //////////////////////////////////////////////////////////////////////////
    //  Action Bar show/hide management
    //////////////////////////////////////////////////////////////////////////
    
    // TCL ShenQianfeng Begin on 2016.08.04
    public void updateBackgroundColor() {
        int color = mActivity.getResources().getColor(getBackgroundColorId());
        updateBackgroundColor(color);
    }
    
    public void updateBackgroundColor(int animatedColor) {
        if(mEmbedded) {
            mPhotoView.setBackgroundColor(GalleryUtils.intColorToFloatARGBArray(animatedColor));
            mPhotoView.invalidate();
        } else {
            mRootPane.setBackgroundColor(GalleryUtils.intColorToFloatARGBArray(animatedColor));
            mRootPane.invalidate();
        }
        //mActivity.getGLRoot().requestRenderForced();
    }

    // TCL ShenQianfeng End on 2016.08.04
    private void showBars() {
        if (mShowBars){ 
            return;
        }
        //LogUtil.d(TAG, "showBars" );
        mShowBars = true;
        mOrientationManager.unlockOrientation();
        //mActivity.getGLRoot().setLightsOutMode(false);
        
        SystemUIManager systemUIManager = mActivity.getSystemUIManager();
        boolean lightsOut = false;
        boolean isPortrait = mActivity.isPortrait();
        boolean occupyNavigationBar = isPortrait;
        int color = isPortrait ? Color.TRANSPARENT : Color.WHITE;
        systemUIManager.setFlag(lightsOut, occupyNavigationBar, color);
        doShowAnimation();//mActionBar.show();
        //refreshHidingMessage();
        refreshBottomControlsWhenReady(true);
        // TCL ShenQianfeng Begin on 2016.08.04
        updateBackgroundColor();
        // TCL ShenQianfeng End on 2016.08.04
        
        //LogUtil.d(TAG, " photopage::showBars--- >  vis: " +((GLRootView)mActivity.getGLRoot()).getSystemUiVisibility() );
    }

    private void hideBars() {
        if (!mShowBars) return;
        //LogUtil.d(TAG, "hideBars" );
        mShowBars = false;
        //mActivity.getGLRoot().setLightsOutMode(true);
        
        SystemUIManager systemUIManager = mActivity.getSystemUIManager();
        boolean lightsOut = true;
        boolean isPortrait = mActivity.isPortrait();
        boolean occupyNavigationBar = isPortrait;
        int color = isPortrait ? Color.TRANSPARENT : Color.BLACK;
        systemUIManager.setFlag(lightsOut, occupyNavigationBar, color);
        
        doHideAnimation();//mActionBar.hide();
        // TCL ShenQianfeng Begin on 2016.10.09
        // Annotated Below:
        // mHandler.removeMessages(MSG_HIDE_BARS);
        // TCL ShenQianfeng End on 2016.10.09
        refreshBottomControlsWhenReady(true);
        // TCL ShenQianfeng Begin on 2016.08.04
        updateBackgroundColor();
        // TCL ShenQianfeng End on 2016.08.04
        
        //LogUtil.d(TAG, " photopage::hideBars--- >  vis: " +((GLRootView)mActivity.getGLRoot()).getSystemUiVisibility() );
    }

    public void doShowAnimation() {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Original:
        // if (mActionBar != null) mActionBar.show();
        // Modify To:
        //LogUtil.d(TAG, "doShowAnimation" );
        final Toolbar toolbar = mActionBar.getToolbar();
        if (toolbar == null) return;
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.float_down_in);
        toolbar.setVisibility(View.VISIBLE);
        toolbar.startAnimation(animation);
        // TCL ShenQianfeng End on 2016.08.11
    }
    
    public void doHideAnimation() {
        // TCL ShenQianfeng Begin on 2016.08.11
        // Original:
        // if (mActionBar != null) mActionBar.hide();
        // Modify To:
        //LogUtil.d(TAG, "doHideAnimation" );
        final Toolbar toolbar = mActionBar.getToolbar();
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.float_up_out);
        toolbar.startAnimation(animation);
        if(mHideAnimationListener == null) {
            mHideAnimationListener = new HideAnimationListener(toolbar);
        }
        animation.setAnimationListener(mHideAnimationListener);
        // TCL ShenQianfeng End on 2016.08.11
    }
    
    private void refreshHidingMessage() {
        // TCL ShenQianfeng Begin on 2016.10.09
        // Annotated Below:
        /*
        mHandler.removeMessages(MSG_HIDE_BARS);
        if (!mIsMenuVisible && !mPhotoView.getFilmMode()) {
            mHandler.sendEmptyMessageDelayed(MSG_HIDE_BARS, HIDE_BARS_TIMEOUT);
        }
        */
        // TCL ShenQianfeng End on 2016.10.09
    }

    private boolean canShowBars() {
        // TCL ShenQianfeng Begin on 2016.09.12
        if(hasNoPhotoIndex()) return false;
        // TCL ShenQianfeng End on 2016.09.12
        // No bars if we are showing camera preview.
        if (mAppBridge != null && mCurrentIndex == 0 && !mPhotoView.getFilmMode()) return false;
        // No bars if it's not allowed.
        if (!mActionBarAllowed) return false;
        Configuration config = mActivity.getResources().getConfiguration();
        if (config.touchscreen == Configuration.TOUCHSCREEN_NOTOUCH) {
            return false;
        }
        return true;
    }

    private void wantBars() {
        if (canShowBars()) {
            //LogUtil.d(TAG, "wantBars wantBars");
            showBars();
        }
    }

    // TCL ShenQianfeng Begin on 2016.10.20

    // TCL ShenQianfeng End on 2016.10.20
    private void toggleBars() {
        if (mShowBars) {
            hideBars();
        } else {
            if (canShowBars()) {
                //LogUtil.d(TAG, "toggleBars canShowBars will call toggleBars");
                showBars();
            }
        }
    }

    private void updateBars() {
        if (!canShowBars()) {
            //LogUtil.i(TAG, "updateBars will call hideBars");
            hideBars();
        }
    }
    
    public boolean isZoomAnimating() {
        if( ! mEmbedded) return false;
        return mPhotoView.isZoomAnimating();
    }

    @Override
    protected void onBackPressed() {
        // TCL ShenQianfeng Begin on 2016.07.15
        /*
        GLRootView glRootView = (GLRootView)mActivity.getGLRoot();
        glRootView.lockRenderThread();
        PhotoFallbackEffect effect = mPhotoView.buildFallbackEffect(mPhotoView, glRootView.getCanvas());
        mActivity.getTransitionStore().put(AlbumPage.KEY_RESUME_ANIMATION, effect);
        glRootView.unlockRenderThread();
        */
        // TCL ShenQianfeng End on 2016.07.15
        //LogUtil.i2(TAG, "PhotoPage::onBackPressed");
        // TCL ShenQianfeng Begin on 2016.09.18
        if(mEmbedded) {
            // TCLBaiYuan Begin 0n 2016.11.09
            // Original:
            /*
            if(mIsHidingToolBar) {
            */
            // Modify To:
            if(View.VISIBLE != mActionBar.getToolbar().getVisibility()){
            // TCLBaiYuan End 0n 2016.11.09
                return;
            }
            if(mPhotoView.getFilmMode()) {
                mPhotoView.setFilmMode(false);
                return;
            }
            // TCL BaiYuan Begin on 2016.11.09
            // Original:
            /*
            if(mPhotoView.isZoomAnimating()) return; 
            */
            // Modify To:
            if (!checkAnimationForContinue()) {
                return;
            }
            // TCL BaiYuan End on 2016.11.09
            doZoomOutAnimation();
            return;
        }  
        // TCL ShenQianfeng End on 2016.09.18
        
        showBars();
        //LogUtil.d(TAG, "onBackPressed showBars");
        if (mShowDetails) {
            hideDetails();
        } else if (mAppBridge == null || !switchWithCaptureAnimation(-1)) {
            // We are leaving this page. Set the result now.
            setResult();
            if (mStartInFilmstrip && !mPhotoView.getFilmMode()) {
                mPhotoView.setFilmMode(true);
            } else if (mTreatBackAsUp) {
                onUpPressed();
            } else {
                // TCL ShenQianfeng Begin on 2016.08.04
                removeIndicatorView();
                mPhotoView.clearEnteringBitmapInfo();
                //mActivity.showStatusBar(true);
                // TCL ShenQianfeng End on 2016.08.04
                super.onBackPressed();
                
                
            }
        }
    }

    private void onUpPressed() {
        if ((mStartInFilmstrip || mAppBridge != null)
                && !mPhotoView.getFilmMode()) {
            mPhotoView.setFilmMode(true);
            return;
        }

        if (mActivity.getStateManager().getStateCount() > 1) {
            setResult();
            super.onBackPressed();
            return;
        }

        if (mOriginalSetPathString == null) return;

        if (mAppBridge == null) {
            // We're in view mode so set up the stacks on our own.
            Bundle data = new Bundle(getData());
            data.putString(AlbumPage.KEY_MEDIA_PATH, mOriginalSetPathString);
            data.putString(AlbumPage.KEY_PARENT_MEDIA_PATH,
                    mActivity.getDataManager().getTopSetPath(
                            DataManager.INCLUDE_ALL));
            mActivity.getStateManager().switchState(this, AlbumPage.class, data);
        } else {
            GalleryUtils.startGalleryActivity(mActivity);
        }
    }

    private void setResult() {
        Intent result = null;
        result = new Intent();
        result.putExtra(KEY_RETURN_INDEX_HINT, mCurrentIndex);
        setStateResult(Activity.RESULT_OK, result);
    }

    //////////////////////////////////////////////////////////////////////////
    //  AppBridge.Server interface
    //////////////////////////////////////////////////////////////////////////

    @Override
    public void setCameraRelativeFrame(Rect frame) {
        mPhotoView.setCameraRelativeFrame(frame);
    }

    @Override
    public boolean switchWithCaptureAnimation(int offset) {
        return mPhotoView.switchWithCaptureAnimation(offset);
    }

    @Override
    public void setSwipingEnabled(boolean enabled) {
        mPhotoView.setSwipingEnabled(enabled);
    }

    @Override
    public void notifyScreenNailChanged() {
        mScreenNailItem.setScreenNail(mAppBridge.attachScreenNail());
        mScreenNailSet.notifyChange();
    }

    @Override
    public void addSecureAlbumItem(boolean isVideo, int id) {
        mSecureAlbum.addMediaItem(isVideo, id);
    }

    // TCL ShenQianfeng Begin on 2016.07.06
    // Original:
    /*
    @Override
    protected boolean onCreateActionBar(Menu menu) {
        mActionBar.createActionBarMenu(R.menu.photo, menu);
        mHaveImageEditor = GalleryUtils.isEditorAvailable(mActivity, "image/*");
        updateMenuOperations();
        mActionBar.setTitle(mMediaSet != null ? mMediaSet.getName() : "");
        return true;
    }
    */
    // Modify To:
    @Override
    protected boolean onCreateActionBar(Menu menu) {
        /*
        mActionBar.createActionBarMenu(R.menu.photo_page_share, menu);
        mHaveImageEditor = GalleryUtils.isEditorAvailable(mActivity, "image/*");
        //mActionBar.setTitle(mMediaSet != null ? mMediaSet.getName() : "");
         */
        return true;
    }
    // TCL ShenQianfeng End on 2016.07.06

    private MenuExecutor.ProgressListener mConfirmDialogListener = new MenuExecutor.ProgressListener() {
        
        @Override
        public void onProgressUpdate(int index) {}

        @Override
        public void onProgressComplete(int result, int action) {
            // TCL ShenQianfeng Begin on 2016.11.01
            if(action == R.id.action_delete) {
                
                // TCL ShenQianfeng Begin on 2016.11.22
                if(mModel != null && mModel instanceof PhotoDataAdapter) {
                    PhotoDataAdapter pda = (PhotoDataAdapter)mModel;
                    if(pda.getCurrentIndex() <= pda.getEnteringIndex()) {
                        pda.recycleEnteringBitmap();
                        pda.setEnteringBitmapInfo(null);
                    }
                }
                // TCL ShenQianfeng End on 2016.11.22
                
                if(mModel instanceof SinglePhotoDataAdapter) {
                    onBackPressed();
                }
            }
            // TCL ShenQianfeng End on 2016.11.01
        }

        @Override
        public void onConfirmDialogShown() {
            // TCL ShenQianfeng Begin on 2016.10.09
            // Annotated Below:
            // mHandler.removeMessages(MSG_HIDE_BARS);
            // TCL ShenQianfeng End on 2016.10.09
            
        }

        @Override
        public void onConfirmDialogDismissed(boolean confirmed) {
            refreshHidingMessage();
        }

        @Override
        public void onProgressStart() {}

    };

    private void switchToGrid() {
        if (mActivity.getStateManager().hasStateClass(AlbumPage.class)) {
            onUpPressed();
        } else {
            if (mOriginalSetPathString == null) return;
            Bundle data = new Bundle(getData());
            data.putString(AlbumPage.KEY_MEDIA_PATH, mOriginalSetPathString);
            data.putString(AlbumPage.KEY_PARENT_MEDIA_PATH,
                    mActivity.getDataManager().getTopSetPath(
                            DataManager.INCLUDE_ALL));

            // We only show cluster menu in the first AlbumPage in stack
            // TODO: Enable this when running from the camera app
            boolean inAlbum = mActivity.getStateManager().hasStateClass(AlbumPage.class);
            data.putBoolean(AlbumPage.KEY_SHOW_CLUSTER_MENU, !inAlbum
                    && mAppBridge == null);

            data.putBoolean(PhotoPage.KEY_APP_BRIDGE, mAppBridge != null);

            // Account for live preview being first item
            mActivity.getTransitionStore().put(KEY_RETURN_INDEX_HINT,
                    mAppBridge != null ? mCurrentIndex - 1 : mCurrentIndex);

            if (mHasCameraScreennailOrPlaceholder && mAppBridge != null) {
                mActivity.getStateManager().startState(AlbumPage.class, data);
            } else {
                mActivity.getStateManager().switchState(this, AlbumPage.class, data);
            }
        }
    }

    @Override
    protected boolean onItemSelected(MenuItem item) {
        if (mModel == null) return true;
        refreshHidingMessage();
        MediaItem current = mModel.getMediaItem(0);

        // This is a shield for monkey when it clicks the action bar
        // menu when transitioning from filmstrip to camera
        if (current instanceof SnailItem) return true;
        // TODO: We should check the current photo against the MediaItem
        // that the menu was initially created for. We need to fix this
        // after PhotoPage being refactored.
        if (current == null) {
            // item is not ready, ignore
            return true;
        }
        int currentIndex = mModel.getCurrentIndex();
        Path path = current.getPath();

        DataManager manager = mActivity.getDataManager();
        int action = item.getItemId();
        String confirmMsg = null;
        switch (action) {
            // TCL ShenQianfeng Begin on 2016.07.06
            case R.id.action_share: {
                if(null == mCurrentPhoto) return false;
                Intent shareIntent = createShareIntent(mCurrentPhoto);
                mActivity.startActivity(shareIntent);
                return true;
            }
            // TCL ShenQianfeng End on 2016.07.06
            case android.R.id.home: {
                onUpPressed();
                return true;
            }
            case R.id.action_slideshow: {
                Bundle data = new Bundle();
                data.putString(SlideshowPage.KEY_SET_PATH, mMediaSet.getPath().toString());
                data.putString(SlideshowPage.KEY_ITEM_PATH, path.toString());
                data.putInt(SlideshowPage.KEY_PHOTO_INDEX, currentIndex);
                data.putBoolean(SlideshowPage.KEY_REPEAT, true);
                mActivity.getStateManager().startStateForResult(
                        SlideshowPage.class, REQUEST_SLIDESHOW, data);
                return true;
            }
            case R.id.action_crop: {
                Activity activity = mActivity;
                Intent intent = new Intent(CropActivity.CROP_ACTION);
                intent.setClass(activity, CropActivity.class);
                intent.setDataAndType(manager.getContentUri(path), current.getMimeType())
                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                activity.startActivityForResult(intent, PicasaSource.isPicasaImage(current)
                        ? REQUEST_CROP_PICASA
                        : REQUEST_CROP);
                return true;
            }
            case R.id.action_trim: {
                Intent intent = new Intent(mActivity, TrimVideo.class);
                intent.setData(manager.getContentUri(path));
                // We need the file path to wrap this into a RandomAccessFile.
                intent.putExtra(KEY_MEDIA_ITEM_PATH, current.getFilePath());
                mActivity.startActivityForResult(intent, REQUEST_TRIM);
                return true;
            }
            case R.id.action_mute: {
                MuteVideo muteVideo = new MuteVideo(current.getFilePath(),
                        manager.getContentUri(path), mActivity);
                muteVideo.muteInBackground();
                return true;
            }
            case R.id.action_edit: {
                launchPhotoEditor();
                return true;
            }
            case R.id.action_simple_edit: {
                launchSimpleEditor();
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
            case R.id.print: {
                mActivity.printSelectedImage(manager.getContentUri(path));
                return true;
            }
            case R.id.action_delete:
                confirmMsg = mActivity.getResources().getQuantityString(
                        R.plurals.delete_selection, 1);
            case R.id.action_setas:
            case R.id.action_rotate_ccw:
            case R.id.action_rotate_cw:
            case R.id.action_show_on_map:
                mSelectionManager.deSelectAll();
                // TCL BaiYuan Begin on 2016.11.02
                // Original:
                /*
                mSelectionManager.toggle(path);
                */
                 // Modify To:
                mSelectionManager.toggle(current);
                 // TCL BaiYuan End on 2016.11.02
                mMenuExecutor.onMenuClicked(item, confirmMsg, mConfirmDialogListener);
                return true;
            default :
                return false;
        }
    }

    private void hideDetails() {
        mShowDetails = false;
        mDetailsHelper.hide();
    }

    private void showDetails() {
        mShowDetails = true;
        if (mDetailsHelper == null) {
            mDetailsHelper = new DetailsHelper(mActivity, mRootPane, new MyDetailsSource());
            mDetailsHelper.setCloseListener(new CloseListener() {
                @Override
                public void onClose() {
                    hideDetails();
                }
            });
        }
        mDetailsHelper.show();
    }

    ////////////////////////////////////////////////////////////////////////////
    //  Callbacks from PhotoView
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void onSingleTapUp(int x, int y) {
        
        // TCL ShenQianfeng Begin on 2016.10.31
        // Annotated Below:
        if(mEmbedded) {
            if( mPhotoView.isZoomAnimating() || 
                    mPhotoView.getVisibility() != GLView.VISIBLE) {
                return;
            }
        }
        // TCL ShenQianfeng End on 2016.10.31
        
        if (mAppBridge != null) {
            if (mAppBridge.onSingleTapUp(x, y)) return;
        }

        MediaItem item = mModel.getMediaItem(0);
        if (item == null || item == mScreenNailItem) {
            // item is not ready or it is camera preview, ignore
            return;
        }

        int supported = item.getSupportedOperations();
        boolean playVideo = ((supported & MediaItem.SUPPORT_PLAY) != 0);
        boolean unlock = ((supported & MediaItem.SUPPORT_UNLOCK) != 0);
        boolean goBack = ((supported & MediaItem.SUPPORT_BACK) != 0);
        boolean launchCamera = ((supported & MediaItem.SUPPORT_CAMERA_SHORTCUT) != 0);

        if (playVideo) {
            // determine if the point is at center (1/6) of the photo view.
            // (The position of the "play" icon is at center (1/6) of the photo)
            int w = mPhotoView.getWidth();
            int h = mPhotoView.getHeight();
            playVideo = (Math.abs(x - w / 2) * 12 <= w)
                && (Math.abs(y - h / 2) * 12 <= h);
        }

        if (playVideo) {
            if (mSecureAlbum == null) {
                if (mPhotoView.tileViewHasValidScreenNailTexture()) {
                    playVideo(mActivity, item.getPlayUri(), item.getName());
                }
            } else {
                mActivity.getStateManager().finishState(this);
            }
        } else if (goBack) {
            onBackPressed();
        } else if (unlock) {
            Intent intent = new Intent(mActivity, GalleryActivity.class);
            intent.putExtra(GalleryActivity.KEY_DISMISS_KEYGUARD, true);
            mActivity.startActivity(intent);
        } else if (launchCamera) {
            launchCamera();
        } else {
            // TCL BaiYuan Begin on 2016.11.09
            if (!checkAnimationForContinue()) {
                return;
            }
            // TCL BaiYuan End on 2016.11.09
            toggleBars();
        }
    }

    @Override
    public void onActionBarAllowed(boolean allowed) {
        mActionBarAllowed = allowed;
        mHandler.sendEmptyMessage(MSG_UPDATE_ACTION_BAR);
    }

    @Override
    public void onActionBarWanted() {
        mHandler.sendEmptyMessage(MSG_WANT_BARS);
    }

    @Override
    public void onFullScreenChanged(boolean full) {
        Message m = mHandler.obtainMessage(
                MSG_ON_FULL_SCREEN_CHANGED, full ? 1 : 0, 0);
        m.sendToTarget();
    }

    // How we do delete/undo:
    //
    // When the user choose to delete a media item, we just tell the
    // FilterDeleteSet to hide that item. If the user choose to undo it, we
    // again tell FilterDeleteSet not to hide it. If the user choose to commit
    // the deletion, we then actually delete the media item.
    @Override
    public void onDeleteImage(Path path, int offset) {
        onCommitDeleteImage();  // commit the previous deletion
        mDeletePath = path;
        mDeleteIsFocus = (offset == 0);
        mMediaSet.addDeletion(path, mCurrentIndex + offset);
    }

    @Override
    public void onUndoDeleteImage() {
        if (mDeletePath == null) return;
        // If the deletion was done on the focused item, we want the model to
        // focus on it when it is undeleted.
        if (mDeleteIsFocus) mModel.setFocusHintPath(mDeletePath);
        mMediaSet.removeDeletion(mDeletePath);
        mDeletePath = null;
    }

    @Override
    public void onCommitDeleteImage() {
        if (mDeletePath == null) return;
        mMenuExecutor.startSingleItemAction(R.id.action_delete, mDeletePath);
        mDeletePath = null;
    }

    public void playVideo(Activity activity, Uri uri, String title) {
        try {
            // TCL BaiYuan Begin on 2016.11.21
            // Original:
            /*
            Intent intent = new Intent(Intent.ACTION_VIEW)
            */
            // Modify To:
            Intent intent = new Intent(mActivity, MovieActivity.class)
            // TCL BaiYuan Begin on 2016.11.21
                    .setDataAndType(uri, "video/*")
                    .putExtra(Intent.EXTRA_TITLE, title)
                    .putExtra(MovieActivity.KEY_TREAT_UP_AS_BACK, true);
            activity.startActivityForResult(intent, REQUEST_PLAY_VIDEO);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, activity.getString(R.string.video_err),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setCurrentPhotoByIntent(Intent intent) {
        if (intent == null) return;
        Path path = mApplication.getDataManager()
                .findPathByUri(intent.getData(), intent.getType());
        if (path != null) {
            Path albumPath = mApplication.getDataManager().getDefaultSetOf(path);
            if (albumPath == null) {
                return;
            }
            if (!albumPath.equalsIgnoreCase(mOriginalSetPathString)) {
                // If the edited image is stored in a different album, we need
                // to start a new activity state to show the new image
                Bundle data = new Bundle(getData());
                data.putString(KEY_MEDIA_SET_PATH, albumPath.toString());
                data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH, path.toString());
                mActivity.getStateManager().startState(SinglePhotoPage.class, data);
                return;
            }
            mModel.setCurrentPhoto(path, mCurrentIndex);
        }
    }

    @Override
    protected void onStateResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            // This is a reset, not a canceled
            return;
        }
        mRecenterCameraOnResume = false;
        switch (requestCode) {
            case REQUEST_EDIT:
                setCurrentPhotoByIntent(data);
                break;
            case REQUEST_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    setCurrentPhotoByIntent(data);
                }
                break;
            case REQUEST_CROP_PICASA: {
                if (resultCode == Activity.RESULT_OK) {
                    Context context = mActivity.getAndroidContext();
                    String message = context.getString(R.string.crop_saved,
                            context.getString(R.string.folder_edited_online_photos));
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_SLIDESHOW: {
                if (data == null) break;
                String path = data.getStringExtra(SlideshowPage.KEY_ITEM_PATH);
                int index = data.getIntExtra(SlideshowPage.KEY_PHOTO_INDEX, 0);
                if (path != null) {
                    mModel.setCurrentPhoto(Path.fromString(path), index);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsActive = false;
        //LogUtil.d(TAG, "PhotoPage::onPause -------- ");
        /*
        SystemUIManager systemUIManager = mActivity.getSystemUIManager();
        systemUIManager.unregisterSystemUIFlagChangeListener(this);
        */
        
        //LogUtil.d(TAG, "PhotoPage::onPause will call mActivity.getGLRoot().unfreeze()");
        mActivity.getGLRoot().unfreeze();
        mHandler.removeMessages(MSG_UNFREEZE_GLROOT);

        DetailsHelper.pause();
        // Hide the detail dialog on exit
        if (mShowDetails) hideDetails();
        if (mModel != null) {
            mModel.pause();
        }
        mPhotoView.pause();
        // TCL ShenQianfeng Begin on 2016.10.09
        // Annotated Below:
        // mHandler.removeMessages(MSG_HIDE_BARS);
        // TCL ShenQianfeng End on 2016.10.09
        //mHandler.removeMessages(MSG_REFRESH_BOTTOM_CONTROLS);
        //refreshBottomControlsWhenReady(false);
        //mActionBar.removeOnMenuVisibilityListener(mMenuVisibilityListener);
        
        // TCL ShenQianfeng Begin on 2016.07.06
        // Annotated Below:
        /*
        if (mShowSpinner) {
            mActionBar.disableAlbumModeMenu(true);
        }
        */
        // TCL ShenQianfeng End on 2016.07.06
        
        onCommitDeleteImage();
        mMenuExecutor.pause();
        if (mMediaSet != null) mMediaSet.clearDeletion();
    }

    @Override
    public void onCurrentImageUpdated() {
        //LogUtil.d(TAG, "PhotoPage::onCurrentImageUpdated will call mActivity.getGLRoot().unfreeze()");
        mActivity.getGLRoot().unfreeze();
    }

    @Override
    public void onFilmModeChanged(boolean enabled) {
        refreshBottomControlsWhenReady(false);
        
        // TCL ShenQianfeng Begin on 2016.07.06
        // Annotated Below:
        /*
         if (mShowSpinner) {
            if (enabled) {
                mActionBar.enableAlbumModeMenu(
                        GalleryActionBar.ALBUM_FILMSTRIP_MODE_SELECTED, this);
            } else {
                mActionBar.disableAlbumModeMenu(true);
            }
        }
         */
        // TCL ShenQianfeng End on 2016.07.06
        
        if (enabled) {
            // TCL ShenQianfeng Begin on 2016.10.09
            // Annotated Below:
            // mHandler.removeMessages(MSG_HIDE_BARS);
            // TCL ShenQianfeng End on 2016.10.09
            UsageStatistics.onContentViewChanged(UsageStatistics.COMPONENT_GALLERY, "FilmstripPage");
        } else {
            refreshHidingMessage();
            if (mAppBridge == null || mCurrentIndex > 0) {
                UsageStatistics.onContentViewChanged(UsageStatistics.COMPONENT_GALLERY, "SinglePhotoPage");
            } else {
                UsageStatistics.onContentViewChanged(UsageStatistics.COMPONENT_CAMERA, "Unknown"); // TODO
            }
        }
    }

    private void transitionFromAlbumPageIfNeeded() {
        TransitionStore transitions = mActivity.getTransitionStore();

        int albumPageTransition = transitions.get(
                KEY_ALBUMPAGE_TRANSITION, MSG_ALBUMPAGE_NONE);

        if (albumPageTransition == MSG_ALBUMPAGE_NONE && mAppBridge != null
                && mRecenterCameraOnResume) {
            // Generally, resuming the PhotoPage when in Camera should
            // reset to the capture mode to allow quick photo taking
            mCurrentIndex = 0;
            mPhotoView.resetToFirstPicture();
        } else {
            int resumeIndex = transitions.get(KEY_INDEX_HINT, -1);
            if (resumeIndex >= 0) {
                if (mHasCameraScreennailOrPlaceholder) {
                    // Account for preview/placeholder being the first item
                    resumeIndex++;
                }
                if (resumeIndex < mMediaSet.getMediaItemCount()) {
                    mCurrentIndex = resumeIndex;
                    mModel.moveTo(mCurrentIndex);
                }
            }
        }

        if (albumPageTransition == MSG_ALBUMPAGE_RESUMED) {
            mPhotoView.setFilmMode(mStartInFilmstrip || mAppBridge != null);
        } else if (albumPageTransition == MSG_ALBUMPAGE_PICKED) {
            mPhotoView.setFilmMode(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //LogUtil.d(TAG, "PhotoPage::onResume -------- ");
        if (mModel == null) {
            mActivity.getStateManager().finishState(this);
            return;
        }
        /*
        SystemUIManager systemUIManager = mActivity.getSystemUIManager();
        systemUIManager.registerSystemUIFlagChangeListener(this);
        */
        modifySystemUIWhenResume();
        // TCL ShenQianfeng Begin on 2016.09.12
        // Annotated Below:
        // transitionFromAlbumPageIfNeeded();
        // TCL ShenQianfeng End on 2016.09.12
        
        mActivity.getGLRoot().freeze();
        mIsActive = true;
        setContentPane(mRootPane);

        mModel.resume();
        mPhotoView.resume();
        updateBackgroundColor();
        // TCL ShenQianfeng Begin on 2016.07.06
        // Original:
        /* mActionBar.setDisplayOptions(((mSecureAlbum == null) && (mSetPathString != null)), false); */
        // Modify To:
        //mActionBar.setDisplayOptions(false, false);
        // TCL ShenQianfeng End on 2016.07.06
        
        // TCL ShenQianfeng Begin on 2016.08.04
        // Annotated Below:
        // mActionBar.addOnMenuVisibilityListener(mMenuVisibilityListener);
        // TCL ShenQianfeng End on 2016.08.04
        
        //refreshBottomControlsWhenReady(false);

        //TCL ShenQianfeng Begin on 2016.07.06
        //Annotated Below:
        /*
        if (mShowSpinner && mPhotoView.getFilmMode()) {
            mActionBar.enableAlbumModeMenu(
                    GalleryActionBar.ALBUM_FILMSTRIP_MODE_SELECTED, this);
        }
        */
        //TCL ShenQianfeng End on 2016.07.06
        
        // TCL ShenQianfeng Begin on 2016.10.28
        // Annotated Below:
        /*
        if (!mShowBars) {
            doHideAnimation();//mActionBar.hide();
            //mActivity.getGLRoot().setLightsOutMode(true);
        }
        */
        // TCL ShenQianfeng End on 2016.10.28
        
        // TCL ShenQianfeng Begin on 2016.10.29
        // Annotated Below:
        /*
        boolean haveImageEditor = GalleryUtils.isEditorAvailable(mActivity, "image/*");
        if (haveImageEditor != mHaveImageEditor) {
            mHaveImageEditor = haveImageEditor;
            // TCL ShenQianfeng Begin on 2016.07.06
            // Annotated Below:
            // updateMenuOperations();
            // TCL ShenQianfeng End on 2016.07.06
        }
        */
        // TCL ShenQianfeng End on 2016.10.29
        mRecenterCameraOnResume = true;
        //mHandler.sendEmptyMessageDelayed(MSG_UNFREEZE_GLROOT, UNFREEZE_GLROOT_TIMEOUT);
        mActivity.getGLRoot().unfreeze();
    }

    @Override
    protected void onDestroy() {
        if (mAppBridge != null) {
            mAppBridge.setServer(null);
            mScreenNailItem.setScreenNail(null);
            mAppBridge.detachScreenNail();
            mAppBridge = null;
            mScreenNailSet = null;
            mScreenNailItem = null;
        }
        mActivity.getGLRoot().setOrientationSource(null);
        if (mBottomControls != null) mBottomControls.cleanup();

        // Remove all pending messages.
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private class MyDetailsSource implements DetailsSource {

        @Override
        public MediaDetails getDetails() {
            return mModel.getMediaItem(0).getDetails();
        }

        @Override
        public int size() {
            return mMediaSet != null ? mMediaSet.getMediaItemCount() : 1;
        }

        @Override
        public int setIndex() {
            return mModel.getCurrentIndex();
        }
    }

    @Override
    public void onAlbumModeSelected(int mode) {
        if (mode == GalleryActionBar.ALBUM_GRID_MODE_SELECTED) {
            switchToGrid();
        }
    }

    @Override
    public void refreshBottomControlsWhenReady(boolean shouldAnimate) {
        if (mBottomControls == null) {
            //LogUtil.d(TAG, "refreshBottomControlsWhenReady null return ...");
            return;
        }
        // TCL ShenQianfeng Begin on 2016.10.24
        // Original:
        /*
        MediaObject currentPhoto = mCurrentPhoto;
        if (currentPhoto == null) {
            LogUtil.i(TAG, "refreshBottomControlsWhenReady currentPhoto == null send MSG_REFRESH_BOTTOM_CONTROLS");
            // TCL ShenQianfeng Begin on 2016.08.04
            mHandler.removeMessages(MSG_REFRESH_BOTTOM_CONTROLS);
            // TCL ShenQianfeng End on 2016.08.04
            mHandler.obtainMessage(MSG_REFRESH_BOTTOM_CONTROLS, 0, 0, currentPhoto).sendToTarget();
        } else {
            
            // TCL ShenQianfeng Begin on 2016.10.18
            // Original:
            // currentPhoto.getPanoramaSupport(mRefreshBottomControlsCallback);
            // Modify To:
            LogUtil.i(TAG, "refreshBottomControlsWhenReady currentPhoto != null send MSG_REFRESH_BOTTOM_CONTROLS");
            mHandler.removeMessages(MSG_REFRESH_BOTTOM_CONTROLS);
            mHandler.obtainMessage(MSG_REFRESH_BOTTOM_CONTROLS, 0, 0, mCurrentPhoto).sendToTarget();
            // TCL ShenQianfeng End on 2016.10.18
        }
        */
        // Modify To:
        if(shouldAnimate) {
            mBottomControls.refresh();
        } else {
            mBottomControls.refreshImmediately();
        }
        // TCL ShenQianfeng End on 2016.10.24
    }

    private void updatePanoramaUI(boolean isPanorama360) {
        Menu menu = mActionBar.getMenu();
        // it could be null if onCreateActionBar has not been called yet
        if (menu == null) {
            return;
        }

        MenuExecutor.updateMenuForPanorama(menu, isPanorama360, isPanorama360);

        if (isPanorama360) {
            MenuItem item = menu.findItem(R.id.action_share);
            if (item != null) {
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                item.setTitle(mActivity.getResources().getString(R.string.share_as_photo));
            }
        } else if ((mCurrentPhoto.getSupportedOperations() & MediaObject.SUPPORT_SHARE) != 0) {
            MenuItem item = menu.findItem(R.id.action_share);
            if (item != null) {
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                item.setTitle(mActivity.getResources().getString(R.string.share));
            }
        }
    }

    @Override
    public void onUndoBarVisibilityChanged(boolean visible) {
        //refreshBottomControlsWhenReady();
    }

    @Override
    public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
        final long timestampMillis = mCurrentPhoto.getDateInMs();
        final String mediaType = getMediaTypeString(mCurrentPhoto);
        UsageStatistics.onEvent(UsageStatistics.COMPONENT_GALLERY,
                UsageStatistics.ACTION_SHARE,
                mediaType,
                timestampMillis > 0 ? System.currentTimeMillis() - timestampMillis : -1);
        return false;
    }

    private static String getMediaTypeString(MediaItem item) {
        if (item.getMediaType() == MediaObject.MEDIA_TYPE_VIDEO) {
            return "Video";
        } else if (item.getMediaType() == MediaObject.MEDIA_TYPE_IMAGE) {
            return "Photo";
        } else {
            return "Unknown:" + item.getMediaType();
        }
    }
    
    // TCL ShenQianfeng Begin on 2016.07.06
    
    public void updatePhotoIndexIndicator(int index, int total) {
        String format = "%d/%d";
        String indicatorNum = String.format(format, index, total);
        //TextView indicatorTextView = (TextView)mActivity.getToolbar().findViewById(R.id.mst_photo_page_indicator);
        if(mIndicatorTextView != null) {
            mIndicatorTextView.setText(indicatorNum);
        }
    }
    
    private void updatePhotoIndexIndicator() {
        int index = mModel.getCurrentIndex() + 1;
        int total = mModel.getTotalCount();
        updatePhotoIndexIndicator(index, total);
        /*
        String format = "%d/%d";
        String indicatorNum = String.format(format, index, total);
        //mActionBar.updatePhotoPageNumIndicator(indicatorNum, mIndicatorClickListener);
        //Toolbar toolBar = mActivity.getToolbar();
        //toolBar.addView();
        //mActivity.updateActionModeTitle(indicatorNum);
        
        TextView indicatorTextView = (TextView)mActivity.getToolbar().findViewById(R.id.mst_photo_page_indicator);
        if(indicatorTextView != null) {
            indicatorTextView.setText(indicatorNum);
        }
        */
    }
    
    /*
    private View.OnClickListener mIndicatorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if(id == R.id.photopage_back_button) {
                GLRoot root = mActivity.getGLRoot();
                root.lockRenderThread();
                onUpPressed();
                root.unlockRenderThread();
            } else if(id == R.id.photopage_share_button) {
                if(null == mCurrentPhoto) return;
                Intent shareIntent = createShareIntent(mCurrentPhoto);
                mActivity.startActivity(shareIntent);
            }
        }
    };
    */
    
    public void setSlotRect(Rect slotRect) {
        mPhotoView.setSlotRect(slotRect);
    }
    
    /**
     * if we start PhotoPage embeddedly, we do not want mRootPane of this PhotoPage.
     * we use mRootPane of parent ActivityState instead.
     */
    public void createEmbedded(ActivityState parent, Bundle data) {
        initialize(parent.mActivity, data);
        mEmbedded = true;
        setParentActivityState(parent);
        mActionBar = mActivity.getGalleryActionBar();
        createIndicatorTextView();
        loadNavigationIcon();
        mSelectionManager = new SelectionManager(mActivity, false);
        mMenuExecutor = new MenuExecutor(mActivity, mSelectionManager);
        // TCL ShenQianfeng Begin on 2016.08.13
        //rebuildToolBar();
        // TCL ShenQianfeng End on 2016.08.13
        GLView parentRootPane = parent.getRootPane();// we use AlbumPage parent's mRootPane here
        mPhotoView = new PhotoView(mActivity);
        // TCL ShenQianfeng Begin on 2016.09.26
        // mPhotoView.setBackgroundRenderNotifier(this);
        // TCL ShenQianfeng End on 2016.09.26
        
        mPhotoView.setListener(this);
        mPhotoView.setZoomAnimationListener(this);
        parentRootPane.addComponent(mPhotoView);
        parentRootPane.requestLayout();
        
        mApplication = (GalleryApp) ((Activity) mActivity).getApplication();
        mOrientationManager = mActivity.getOrientationManager();
        mActivity.getGLRoot().setOrientationSource(mOrientationManager);
        
        // ShenQianfeng move the function body into PhotoPageSynchronizedHandler
        mHandler = new PhotoPageSynchronizedHandler(mActivity.getGLRoot());
        mSetPathString = data.getString(KEY_MEDIA_SET_PATH);
        mReadOnlyView = data.getBoolean(KEY_READONLY);
        mOriginalSetPathString = mSetPathString;
        // setupNfcBeamPush();
        String itemPathString = data.getString(KEY_MEDIA_ITEM_PATH);
        Path itemPath = itemPathString != null ? Path.fromString(data.getString(KEY_MEDIA_ITEM_PATH)) : null;
        mTreatBackAsUp = data.getBoolean(KEY_TREAT_BACK_AS_UP, false);
        mStartInFilmstrip = data.getBoolean(KEY_START_IN_FILMSTRIP, false);
        boolean inCameraRoll = data.getBoolean(KEY_IN_CAMERA_ROLL, false);
        mCurrentIndex = data.getInt(KEY_INDEX_HINT, -1); //if just created, the index must be -1
        if (mSetPathString != null) {
            
        // TCL ShenQianfeng Begin on 2016.07.06
        // Annotated Below:
        // mShowSpinner = true;
        // TCL ShenQianfeng End on 2016.07.06
            
            mAppBridge = (AppBridge) data.getParcelable(KEY_APP_BRIDGE);
            if (mAppBridge != null) {
                mShowBars = false;
                mHasCameraScreennailOrPlaceholder = true;
                mAppBridge.setServer(this);

                // Get the ScreenNail from AppBridge and register it.
                int id = SnailSource.newId();
                Path screenNailSetPath = SnailSource.getSetPath(id);
                Path screenNailItemPath = SnailSource.getItemPath(id);
                mScreenNailSet = (SnailAlbum) mActivity.getDataManager()
                        .getMediaObject(screenNailSetPath);
                mScreenNailItem = (SnailItem) mActivity.getDataManager()
                        .getMediaObject(screenNailItemPath);
                mScreenNailItem.setScreenNail(mAppBridge.attachScreenNail());

                if (data.getBoolean(KEY_SHOW_WHEN_LOCKED, false)) {
                    // Set the flag to be on top of the lock screen.
                    mFlags |= FLAG_SHOW_WHEN_LOCKED;
                }

                // Don't display "empty album" action item for capture intents.
                if (!mSetPathString.equals("/local/all/0")) {
                    // Check if the path is a secure album.
                    if (SecureSource.isSecurePath(mSetPathString)) {
                        mSecureAlbum = (SecureAlbum) mActivity.getDataManager().getMediaSet(mSetPathString);
                        // TCL ShenQianfeng Begin on 2016.07.06
                        // Annotated Below:
                        // mShowSpinner = false;
                        // TCL ShenQianfeng End on 2016.07.06
                    }
                    mSetPathString = "/filter/empty/{"+mSetPathString+"}";
                }
                // Combine the original MediaSet with the one for ScreenNail
                // from AppBridge.
                mSetPathString = "/combo/item/{" + screenNailSetPath + "," + mSetPathString + "}";
                // Start from the screen nail.
                itemPath = screenNailItemPath;
            } else if (inCameraRoll && GalleryUtils.isCameraAvailable(mActivity)) {
                mSetPathString = "/combo/item/{" + FilterSource.FILTER_CAMERA_SHORTCUT + "," + mSetPathString + "}";
                mCurrentIndex++;
                mHasCameraScreennailOrPlaceholder = true;
            }

            MediaSet originalSet = mActivity.getDataManager().getMediaSet(mSetPathString);
            if (mHasCameraScreennailOrPlaceholder && originalSet instanceof ComboAlbum) {
                // Use the name of the camera album rather than the default
                // ComboAlbum behavior
                ((ComboAlbum) originalSet).useNameOfChild(1);
            }
            mSelectionManager.setSourceMediaSet(originalSet);
            mSetPathString = "/filter/delete/{" + mSetPathString + "}";
            mMediaSet = (FilterDeleteSet) mActivity.getDataManager().getMediaSet(mSetPathString); 
            // mSetPathString --> /filter/delete/{/combo/item/{/local/all/-1739773001,/local/all/1028075469}}
            if (mMediaSet == null) {
                Log.w(TAG, "failed to restore " + mSetPathString);
            }
            if (itemPath == null && mCurrentIndex != -1) { // mCurrentIndex != -1 added by ShenQianfeng
                int mediaItemCount = mMediaSet.getMediaItemCount();
                if (mediaItemCount > 0) {
                    if (mCurrentIndex >= mediaItemCount) mCurrentIndex = 0;
                    mMediaSet.reload();//ShenQianfeng add this line 
                    itemPath = mMediaSet.getMediaItem(mCurrentIndex, 1).get(0).getPath();
                } else {
                    // Bail out, PhotoPage can't load on an empty album
                    return;
                }
            }
            PhotoDataAdapter pda = new PhotoDataAdapter(
                    mActivity, mPhotoView, mMediaSet, itemPath, mCurrentIndex,
                    mAppBridge == null ? -1 : 0,
                    mAppBridge == null ? false : mAppBridge.isPanorama(),
                    mAppBridge == null ? false : mAppBridge.isStaticCamera());
            mModel = pda;
            pda.setIsEmbedded(true);
            mPhotoView.setModel(mModel);
            //ShenQianfeng move mDataListener function body to PhotoPage's data member
            pda.setDataListener(mDataListener);
        } else {
            // Get default media set by the URI
            MediaItem mediaItem = (MediaItem) mActivity.getDataManager().getMediaObject(itemPath);
            mModel = new SinglePhotoDataAdapter(mActivity, mPhotoView, mediaItem);
            mPhotoView.setModel(mModel);
            updateCurrentPhoto(mediaItem);
            // TCL ShenQianfeng Begin on 2016.07.06
            // Annotated Below:
            // mShowSpinner = false;
            // TCL ShenQianfeng End on 2016.07.06
            
        }
        mPhotoView.setFilmMode(mStartInFilmstrip && mMediaSet.getMediaItemCount() > 1);
        RelativeLayout galleryRoot = (RelativeLayout) ((Activity) mActivity).findViewById(mAppBridge != null ? R.id.content : R.id.gallery_root);
        if (galleryRoot != null) {
            if (mSecureAlbum == null) {
                SystemUIManager systemUIManager = mActivity.getSystemUIManager(); 
                int navigationBarHeight = systemUIManager.hasVirtualKeys() ? systemUIManager.getNavigationBarHeight() : 0;
                mBottomControls = new PhotoPageBottomControls(this, mActivity, galleryRoot, navigationBarHeight);
                mBottomControls.updatePadding(mActivity.getWindowInsets());
            }
        }
        //we don't show PhotoView when created
        showEmbedded(false);
    }
    
    // TCL ShenQianfeng Begin on 2016.09.09
    public PhotoDataAdapter getModel() {
        return (PhotoDataAdapter)mModel;
    }
    
    public void goToIndex(Path path, int index) {
        mCurrentIndex = index;
        PhotoDataAdapter model = getModel();
        model.setEmbeddedItem(path, index);
    }
    
    // TCL ShenQianfeng Begin on 2016.10.09
    // Annotated Below:
    /*
    public void removeHideBarMsg() {
        mHandler.removeMessages(MSG_HIDE_BARS);
    }
    */
    // TCL ShenQianfeng End on 2016.10.09
    
    public void buildZoomInAnimation() {
        mPhotoView.buildZoomInAnimation();
    }
    //modify by liaoah
    public void buildZoomOutAnimation() {
        mPhotoView.buildZoomOutAnimation();
    }
    //end modify
    
    public void doZoomOutAnimation() {
       //modify by liaoah
        buildZoomOutAnimation();
        invalidate();
         //end modify
    }
    // TCL ShenQianfeng End on 2016.09.09
    
    private boolean hasNoPhotoIndex() {
        boolean hasNoPhotoIndex = (-1 == mCurrentIndex); 
        return hasNoPhotoIndex;
    }
    
    public PhotoPageBottomControls getBottomControls() {
        return mBottomControls;
    }
    
    public void modifySystemUIWhenResume() {
        SystemUIManager systemUIManager = mActivity.getSystemUIManager();
        if(mShowBars) {
            boolean lightsOut = false;
            boolean occupyNavigationBar = mActivity.isPortrait();
            systemUIManager.setFlag(lightsOut, occupyNavigationBar, Color.TRANSPARENT);
        } else {
            boolean lightsOut = true;
            boolean occupyNavigationBar = mActivity.isPortrait();
            systemUIManager.setFlag(lightsOut, occupyNavigationBar, Color.TRANSPARENT);
        }
    }
    
    public void resumeEmbedded(boolean force) {
        //LogUtil.d(TAG, "PhotoPage::resumeEmbedded -------- ");
        if( ! mEmbedded) {
            LogUtil.e(TAG, "PhotoPage::resumeEmbedded return because mEmbedded:" + mEmbedded);
            return;
        }
        if(-1 == mCurrentIndex) {
            LogUtil.e(TAG, "PhotoPage::resumeEmbedded return because -1 == mCurrentIndex");
            return;
        }
        /*
        SystemUIManager systemUIManager = mActivity.getSystemUIManager();
        systemUIManager.registerSystemUIFlagChangeListener(this);
        */
        
        //if( ! force &&  ! isShowingEmbedded()) return;
        //transitionFromAlbumPageIfNeeded();
        updateBackgroundColor();
        mActivity.getGLRoot().freeze();
        mIsActive = true;
        //setContentPane(mRootPane);
        mModel.resume();
        mPhotoView.resume();
        //refreshBottomControlsWhenReady(false);
        // TCL ShenQianfeng Begin on 2016.10.28
        // Annotated Below:
        /*
        if (!mShowBars) {
            doHideAnimation();//mActionBar.hide();
            mActivity.getGLRoot().setLightsOutMode(true);
        }
        */
        
        //modifySystemUIWhenResume();

        // TCL ShenQianfeng End on 2016.10.28
        
        // TCL ShenQianfeng Begin on 2016.10.29
        // Annotated Below:
        /*
        boolean haveImageEditor = GalleryUtils.isEditorAvailable(mActivity, "image/*");
        if (haveImageEditor != mHaveImageEditor) {
            mHaveImageEditor = haveImageEditor;
        }
        */
        // TCL ShenQianfeng End on 2016.10.29
        mRecenterCameraOnResume = true;
        
        // TCL ShenQianfeng Begin on 2016.11.15
        // Original:
        //mHandler.sendEmptyMessageDelayed(MSG_UNFREEZE_GLROOT, UNFREEZE_GLROOT_TIMEOUT);
        // Modify To:
        mActivity.getGLRoot().unfreeze();
        // TCL ShenQianfeng End on 2016.11.15
        modifySystemUIWhenResume();
        //mHandler.sendEmptyMessage(MSG_MODIFY_SYSTEM_UI_WHEN_RESUME);
        
        LogUtil.d(TAG, " PhotoPage::resumeEmbedded- -- >  vis: " +((GLRootView)mActivity.getGLRoot()).getSystemUiVisibility() );
    }
    
    public void pauseEmbedded() {
        if( ! mEmbedded) return;
        //LogUtil.d(TAG, "PhotoPage::pauseEmbedded -------- ");
        /*
        SystemUIManager systemUIManager = mActivity.getSystemUIManager();
        systemUIManager.unregisterSystemUIFlagChangeListener(this);
        */
        //if( ! isShowingEmbedded()) return;
        mIsActive = false;
        //LogUtil.d(TAG, "PhotoPage::pauseEmbedded will call mActivity.getGLRoot().unfreeze()");
        mActivity.getGLRoot().unfreeze();
        mHandler.removeMessages(MSG_UNFREEZE_GLROOT);
        DetailsHelper.pause();
        // Hide the detail dialog on exit
        if (mShowDetails) hideDetails();
        if (mModel != null) {
            mModel.pause();
        }
        mPhotoView.pause();
        // TCL ShenQianfeng Begin on 2016.10.09
        // Annotated Below:
        // mHandler.removeMessages(MSG_HIDE_BARS);
        // TCL ShenQianfeng End on 2016.10.09
        //mHandler.removeMessages(MSG_REFRESH_BOTTOM_CONTROLS);
        
        //mActionBar.removeOnMenuVisibilityListener(mMenuVisibilityListener);
        // TCL ShenQianfeng Begin on 2016.07.06
        // Annotated Below:
        /*
        if (mShowSpinner) {
            mActionBar.disableAlbumModeMenu(true);
        }
        */
        // TCL ShenQianfeng End on 2016.07.06
        //refreshBottomControlsWhenReady(false);
        onCommitDeleteImage();
        mMenuExecutor.pause();
        if (mMediaSet != null) mMediaSet.clearDeletion();
    }

    // TCL ShenQianfeng Begin on 2016.09.18
    public boolean isEmbedded() {
        return mEmbedded;
    }
    // TCL ShenQianfeng End on 2016.09.18
    
    public boolean isActive() {
        return mIsActive;
    }
    
    public void layoutEmbedded(int left, int top, int right, int bottom) {
        //mRootPane is null if this PhotoPage is embedded in AlbumPage or some other ActivityStates,
        //in this situation, this PhotoPage is created through createEmbedded
        //mRootPane is not null if this PhotoPage is created through onCreate
        if(mRootPane != null) {
            mRootPane.layout(left, top, right, bottom);
        }
        
        if(mPhotoView != null) {
            mPhotoView.layout(left, top, right, bottom);
        }
        if (mShowDetails) {
            mDetailsHelper.layout(left, mActionBar.getHeight(), right, bottom);
        }
    }
    
    // TCL ShenQianfeng Begin on 2016.07.19
    
    public void setParentActivityState(ActivityState parent) {
        mParentActivityState = parent;
    }
    
    public static class EnteringBitmapInfo {
        public Bitmap bitmap;
        public int index;
        public int rotation;
        public int totalCount;
        public boolean isImage;
        public boolean isGif;
        
        public void recycle() {
            if(null != bitmap) {
                if( ! bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                bitmap = null;
            }
            index = -1;
            totalCount = 0;
        }
    }
    
    public void setEnteringBitmapInfo(EnteringBitmapInfo enteringBitmapInfo) {
        ((PhotoDataAdapter)mModel).setEnteringBitmapInfo(enteringBitmapInfo);
    }
    
    /*
    public void setEnteringBitmapInfos(Bitmap bitmap, int index, int totalCount, int rotation) {
        ((PhotoDataAdapter)mModel).setEnteringBitmapInfo(bitmap, index, totalCount, rotation);
    }
    */
    
    public void showEmbedded(boolean show) {
        if( ! mEmbedded) return;
        int visibility = show ? GLView.VISIBLE : GLView.INVISIBLE;
        mPhotoView.setVisibility(visibility);
        mPhotoView.invalidate();
    }
    
    public boolean isShowingEmbedded() {
        return mPhotoView.getVisibility() == GLView.VISIBLE;
    }
    
    public void initControlStatusWhenZoomingInStart() {
        if(mBottomControls == null) {
            return;
        }
        mBottomControls.initControlStatusWhenZoomingInStart();
    }
    
    private void sendMessageToRebuildMenu() {
        mHandler.removeMessages(MSG_REBUILD_TOOLBAR_MENU);
        mHandler.obtainMessage(MSG_REBUILD_TOOLBAR_MENU).sendToTarget();
    }
    
    private void sendMessageToSetToolbarChildViewsAlpha(float alphaProgress) {
        mHandler.removeMessages(MSG_SET_TOOLBAR_CHILD_VIEWS_ALPHA);
        mHandler.obtainMessage(MSG_SET_TOOLBAR_CHILD_VIEWS_ALPHA, Float.valueOf(alphaProgress)).sendToTarget();
    }
    
    private void sendMessageToRebuildToolbar() {
        mHandler.removeMessages(MSG_REBUILD_TOOLBAR);
        mHandler.obtainMessage(MSG_REBUILD_TOOLBAR).sendToTarget();
    }
    
    private void sendMessageToSetBottomControlAlpha(boolean fadeIn, float alpha) {
        int fadeInValue = fadeIn ? 1 : 0;
        mHandler.removeMessages(MSG_SET_BOTTOM_CONTROLS_ALPHA);
        mHandler.obtainMessage(MSG_SET_BOTTOM_CONTROLS_ALPHA, fadeInValue, 0, Float.valueOf(alpha)).sendToTarget();
    }
    
    private void sendMessageToNotifyZoomInStart() {
        mHandler.removeMessages(MSG_ON_ZOOM_IN_START);
        mHandler.obtainMessage(MSG_ON_ZOOM_IN_START).sendToTarget();
    }
    
    private void sendMessageToNotifyZoomOutStart() {
        mHandler.removeMessages(MSG_ON_ZOOM_OUT_START);
        mHandler.obtainMessage(MSG_ON_ZOOM_OUT_START).sendToTarget();
    }
    
    @Override
    public void onZoomStart(int animType) {
        if(animType == ZoomAnimation.ANIM_TYPE_ZOOM_IN) {
            mEmbeddedRebuilt = false;
            mShowBars = true;
            sendMessageToNotifyZoomInStart();
        } else if(animType == ZoomAnimation.ANIM_TYPE_ZOOM_OUT) {
            if(mParentActivityState != null && mParentActivityState instanceof AlbumPage) {
                AlbumPage albumPage = (AlbumPage)mParentActivityState;
                albumPage.resetRebuildFlag();
            }
            sendMessageToNotifyZoomOutStart();
        }
    }
    
    @Override
    public void onZoomProgress(int animType, float progress) {
        long time = System.currentTimeMillis();
        if(animType == ZoomAnimation.ANIM_TYPE_ZOOM_IN) {
            if(progress <= 0.5f) {
                float alpha = -2 * progress + 1;
                sendMessageToSetToolbarChildViewsAlpha(alpha);
            } else if(progress > 0.5f) {
                if( ! mEmbeddedRebuilt) {
                    sendMessageToRebuildToolbar();
                    mPhotoView.invalidate();
                }
                if(mEmbeddedRebuilt) {
                    float alpha = 2 * progress - 1;
                    sendMessageToSetToolbarChildViewsAlpha(alpha);
                }
            }
            float bottomControlAlpha = Math.min(1.204819f * progress, 1);
            sendMessageToSetBottomControlAlpha(true ,bottomControlAlpha);
        } else if(animType == ZoomAnimation.ANIM_TYPE_ZOOM_OUT) {
            if(progress <= 0.5f) {
                float alpha = -2 * progress + 1;
                sendMessageToSetToolbarChildViewsAlpha(alpha);
            } else if(progress > 0.5f) {
                if(mParentActivityState != null && mParentActivityState instanceof AlbumPage) {
                    AlbumPage albumPage = (AlbumPage)mParentActivityState;
                    albumPage.notifyToRebuildToolbar();
                    if(albumPage.getRebuildFlag()) {
                        float alpha = 2 * progress - 1;
                        //LogUtil.d("ZoomOut", "ANIM_TYPE_ZOOM_OUT progress:" + progress + " alphaProgress:" + alphaProgress) ;
                        sendMessageToSetToolbarChildViewsAlpha(alpha);
                    }
                }
            }
            sendMessageToSetBottomControlAlpha(false, 1 - progress);
        }
        //LogUtil.d(TAG, " onZoomProgress: " + (System.currentTimeMillis() - time) + " anim type:" + animType);
    }

    @Override
    public void onZoomEnd(int animType) {
        if(animType == ZoomAnimation.ANIM_TYPE_ZOOM_IN) {
            mHandler.sendEmptyMessage(MSG_ON_ZOOM_IN_END);
        } else if(animType == ZoomAnimation.ANIM_TYPE_ZOOM_OUT) {
            mHandler.sendEmptyMessage(MSG_ON_ZOOM_OUT_END);
        }
    }
    
    public void invalidate() {
        mPhotoView.invalidate();
    }
    
    public void setAlbumPageSlotPositionProvider(AlbumPageSlotPositionProvider provider) {
        mPhotoView.setAlbumPageSlotPositionProvider(provider);
    }
    // TCL ShenQianfeng End on 2016.07.06
    
    // TCL BaiYuan Begin on 2016.11.09
    private boolean checkAnimationForContinue(){
        boolean couldContinue = true;
        Toolbar toolbar = mActionBar.getToolbar();
        if (null != toolbar.getAnimation()) {
            couldContinue = false;
            return couldContinue;
        }
        couldContinue = !mPhotoView.isPreparedZoomAnimation();
        return couldContinue;
    }
    // TCL BaiYuan End on 2016.11.09

    @Override
    public void onNavigationBarHidden() {

    }

    @Override
    public void onNavigationBarShown() {

    }
    
    // TCL ShenQianfeng Begin on 2016.11.17
    @Override
    public void onWindowsInsetsChanged(Rect newWindowInsets) {
        if(mEmbedded && ! isShowingEmbedded()) return;
        if(null != mBottomControls) {
            mBottomControls.updatePadding(newWindowInsets);
        }
    }
    // TCL ShenQianfeng End on 2016.11.17
}
