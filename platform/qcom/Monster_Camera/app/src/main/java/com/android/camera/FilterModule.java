/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.android.camera.app.AppController;
import com.android.camera.app.CameraAppUI;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.widget.TopMenus;
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.CameraDeviceInfo;
import com.android.ex.camera2.portability.Size;
/* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
import com.android.gl_component.BaseGLRenderer;
import com.android.gl_component.GLAnimationProxy;
import com.android.gl_component.NormalGLComposer;
import com.android.gl_component.GLProxy;
import com.tct.camera.R;
/* MODIFIED-END by jianying.zhang,BUG-3255060*/

/* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
/* MODIFIED-END by sichao.hu,BUG-2821981*/

/**
 * Created by sichao.hu on 7/21/16.
 */
public class FilterModule extends BaseGLModule {

    private Log.Tag TAG = new Log.Tag("FilterMode"); // MODIFIED by jianying.zhang, 2016-11-08,BUG-3255060

    /**
     * 状态变换规则：
     * 1、第一次初始化 ： 0
     * 2、返回预览界面 ： 1
     * 3、九宫格加载完成 ： 2
     * 4、加载过程 (加载九宫格选择界面或者加载预览界面) : 3
     */
    /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
    protected static final int FILTER_STATE_UNITIALIZED = 0;
    protected static final int FILTER_STATE_IDLE_ENLARGED = 1;
    protected static final int FILTER_STATE_IDLE_SHRINKED = 2;
    protected static final int FILTER_STATE_ANIMATING = 3;

    private static final int MSG_ACTIVE_FILTER = 1;
    private static final int MSG_INACTIVE_FILTER = 2;
    private static final int MSG_FILTER_STATE_IDLE_SHRINKED = 3;
    private static final int MSG_FILTER_STATE_IDLE_ENLARGED = 4;
    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
    protected static final int INDEX_NONE_FILTER = CameraAgent.INDEX_NONE_FILTER;

    private Object mFilterFocusPauseKey = new Object();
    protected int mFilterState;
    protected int mChosenFilterIndex = INDEX_NONE_FILTER;
    /* MODIFIED-END by jianying.zhang,BUG-3255060*/

    /* MODIFIED-END by sichao.hu,BUG-2821981*/
    public FilterModule(AppController app) {
        super(app);
        setFilterState(FILTER_STATE_UNITIALIZED);
    }

    PointF mCoords;

    @Override
    protected boolean needFaceDetection() {
        if (mFilterState == FILTER_STATE_IDLE_ENLARGED) {
            return true;
        } else {
            return false;
        }
    }

    public void onSingleTapUp(View view, int x, int y) {
        Log.w(TAG, String.format("x is %d , y is %d", x, y));
        /* MODIFIED-END by Sichao Hu,BUG-2989818*/
        /* MODIFIED-END by Sichao Hu,BUG-2989818*/
        if(mPaused){
            /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
            super.onSingleTapUp(view,x, y);
            return;
        }
        if (mFilterState == FILTER_STATE_IDLE_ENLARGED) {
            super.onSingleTapUp(view, x, y);
        } else if (mFilterState == FILTER_STATE_ANIMATING || mFilterState == FILTER_STATE_UNITIALIZED) {
            return;
        } else {
            if (!getPhotoUI().isContainsSingleTapUpPoint(x, y)) {
                return;
            }
            RectF previewRect = mActivity.getCameraAppUI().getPreviewArea();
            if (previewRect.contains(x, y)) {
                mChosenFilterIndex = calculateCurrentIndex(x, y);
                Log.d(TAG, "mChosenFilterIndex : " + mChosenFilterIndex);
                chooseSingleFilterWindow(mChosenFilterIndex);
                /* MODIFIED-END by jianying.zhang,BUG-3255060*/
            /* MODIFIED-END by Sichao Hu,BUG-2989818*/
            }
        }
    }

    protected void resumeFocusFrame() {
        getPhotoUI().resumeFocusFrame(mFilterFocusPauseKey.hashCode());
    }

    @Override
    protected int getFilterId() {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
        return mChosenFilterIndex == INDEX_NONE_FILTER ? INDEX_NONE_FILTER : mChosenFilterIndex;
    }

    private int calculateCurrentIndex(int x, int y) {
        if (mCameraDevice == null || mSurfaceSpec == null) {
            return INDEX_NONE_FILTER;
        }
        RectF rectF = mActivity.getCameraAppUI().getPreviewArea();
        float newYValue = (float) y;
        if (!rectF.isEmpty() && rectF.top > 0) {
            Log.d(TAG, "top" + rectF.top);
            newYValue = y - rectF.top;
        }
        Size previewSize = mCameraSettings.getCurrentPreviewSize();
        Log.d(TAG, "previewSize width : " + previewSize.width() + " height : " + previewSize.height());
        int longSideInPreview = Math.max(previewSize.width(), previewSize.height());
        int shortSideInPreview = previewSize.width() + previewSize.height() - longSideInPreview;
        int shortSideInSurface = Math.min(mSurfaceSpec.x, mSurfaceSpec.y);
        int normalizedLongSideInPreview = longSideInPreview * shortSideInSurface / shortSideInPreview;
        int specWidth = shortSideInSurface;
        int specHeight = normalizedLongSideInPreview;
        float normalizedXCoord = 2.0f * (float) x / specWidth;//normalized to 0 - 2.0
        float normalizedYCoord = 2.0f * newYValue / specHeight;
        Log.w(TAG, String.format("coordinate in normalized coordinator :x =%f, y=%f"
                , normalizedXCoord, normalizedYCoord));
        return getChoosenIndex(normalizedXCoord, normalizedYCoord);
    }

    private int getChoosenIndex(float normalizedXCoord, float normalizedYCoord) {
        PointF coords = new PointF(normalizedXCoord, normalizedYCoord);
        int chosenIndex = INDEX_NONE_FILTER;
        if (coords == null) {
            chosenIndex = INDEX_NONE_FILTER;
        } else {
            float singleWindowWidth = (2.0f - BaseGLRenderer.SEPARATOR_WIDTH * 2) / 3;
            int xStep = (int) (coords.x / (singleWindowWidth + BaseGLRenderer.SEPARATOR_WIDTH));
            int yStep = (int) (coords.y / (singleWindowWidth + BaseGLRenderer.SEPARATOR_WIDTH));
            chosenIndex = xStep + yStep * 3;
            Log.w(TAG, String.format("coord is %fx%f, window spec is %fx%f, xStep is %d " +
                            ", yStep is %d , chosenIndex is %d", coords.x, coords.y, singleWindowWidth,
                    singleWindowWidth, xStep, yStep, chosenIndex));
        }
        return chosenIndex;
        /* MODIFIED-END by jianying.zhang,BUG-3255060*/
    }


    @Override
    public boolean onBackPressed() {
        if (mFilterState == FILTER_STATE_IDLE_SHRINKED) {

            /* MODIFIED-BEGIN by Sichao Hu, 2016-09-23,BUG-2989818*/
            /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
            chooseSingleFilterWindow(-1);//keep last coordinate
            return true;
        } else {
            return super.onBackPressed();
        }
    }

    protected GLAnimationProxy getAnimateProxy() {
        return (NormalGLComposer) mGLComponent;
    }

    private void chooseSingleFilterWindow(int chosenFilterIndex) {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
        if (mPaused) {
            return;
        }
        /* MODIFIED-END by jianying.zhang,BUG-3467717*/
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                hideBottomBarInFilterMode(false);
            }
        });
        if (getAnimateProxy() != null) {
            /* MODIFIED-BEGIN by sichao.hu, 2016-09-12,BUG-2895116*/
            setFilterState(FILTER_STATE_ANIMATING);
            pauseFocusFrame();
            updatePreviewAreaInFilter(false);
            getAnimateProxy().startEnlargeAnimation(chosenFilterIndex, new GLAnimationProxy.AnimationProgressListener() {
                @Override
                public void onAnimationDone(int chosenIndex) {
                    resumeFocusFrame();
                    mChosenFilterIndex = chosenIndex;
                    saveSelectedFilterIndex();
                    setFilterState(FILTER_STATE_IDLE_ENLARGED);
                    if (mChosenFilterIndex == INDEX_NONE_FILTER) {
                    /* MODIFIED-END by jianying.zhang,BUG-3255060*/
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mAppController.getCameraAppUI().quitFilter();
                            }
                        });
                        mFilterhandler.sendEmptyMessage(MSG_INACTIVE_FILTER);
                    } else {
                        mFilterhandler.sendEmptyMessage(MSG_ACTIVE_FILTER);
                    }
                }
            });
        }
    }

    private void quitToFilterChoosingWindow() {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
        if (mPaused) {
            return;
        }
        /* MODIFIED-END by jianying.zhang,BUG-3467717*/
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                hideBottomBarInFilterMode(true);
            }
        });
        setFilterState(FILTER_STATE_ANIMATING);
        pauseFocusFrame();
        mChosenFilterIndex = INDEX_NONE_FILTER; // MODIFIED by jianying.zhang, 2016-11-08,BUG-3255060
        updatePreviewAreaInFilter(true);
        getAnimateProxy().startShrinkAnimation(new GLAnimationProxy.AnimationProgressListener() {
            @Override
            public void onAnimationDone(int chosenIndex) {//Not in the same thread against main thread
                setFilterState(FILTER_STATE_IDLE_SHRINKED);
            }
        });
    }

    @Override
    public void onShutterButtonClick() {
        Log.d(TAG, "onShutterButtonClick mFilterState : " + mFilterState); // MODIFIED by jianying.zhang, 2016-11-08,BUG-3255060
        if (mFilterState == FILTER_STATE_IDLE_ENLARGED) {
            super.onShutterButtonClick();
        }
    }

    protected void pauseFocusFrame() {
        getPhotoUI().pauseFocusFrame(mFilterFocusPauseKey.hashCode());
    }

    protected GLProxy buildGLProxy() {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
        NormalGLComposer normalGLComposer = new NormalGLComposer(mAppController.getAndroidContext());
        return normalGLComposer;
    }

    protected void onFirstFrameArrive() {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
        if (mPaused) {
            return;
        }
        /* MODIFIED-END by jianying.zhang,BUG-3467717*/
        if (getAnimateProxy() != null) {
                    /* MODIFIED-BEGIN by sichao.hu, 2016-09-12,BUG-2895116*/
            Log.w(TAG, "onFirstFrameArrive");
            mChosenFilterIndex = getSelectedFilterIndex(mActivity.getCameraScope());
            Log.d(TAG, "onFirstFrameArrive mChosenFilterIndex : "
                    + mChosenFilterIndex);
            if (mChosenFilterIndex != INDEX_NONE_FILTER) {
                getAnimateProxy().switchToSingleWindowImmediately(mChosenFilterIndex, new GLAnimationProxy.AnimationProgressListener() {

                    @Override
                    public void onAnimationDone(int chosenIndex) {
                        mChosenFilterIndex = chosenIndex;
                        setFilterState(FILTER_STATE_IDLE_ENLARGED);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                hideBottomBarInFilterMode(false);
                            }
                        });
                        resumeFocusFrame();
                        mFilterhandler.sendEmptyMessage(MSG_ACTIVE_FILTER);
                        /* MODIFIED-END by jianying.zhang,BUG-3255060*/
                    }
                });
                return;
            }
            quitToFilterChoosingWindow();
        }
    }

    protected void setFilterState(int state) {
        mFilterState = state;
        Log.d(TAG,"setFilterState : " + mFilterState);
        switch (state) {
            case FILTER_STATE_IDLE_SHRINKED:
                mFilterhandler.sendEmptyMessage(MSG_FILTER_STATE_IDLE_SHRINKED);
                break;
            case FILTER_STATE_IDLE_ENLARGED:
                mFilterhandler.sendEmptyMessage(MSG_FILTER_STATE_IDLE_ENLARGED);
                break;
        }
    }

    private Handler mFilterhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ACTIVE_FILTER: {
                    mAppController.getCameraAppUI()
                            .setFiltersButtonActivated(TopMenus.BUTTON_FILTER, TopMenus.ACTIVE_FILTER);
                    break;
                }
                case MSG_INACTIVE_FILTER: {
                    mAppController.getCameraAppUI()
                            .setFiltersButtonActivated(TopMenus.BUTTON_FILTER, TopMenus.INACTIVE_FILTER);
                    break;
                }
                case MSG_FILTER_STATE_IDLE_SHRINKED:
                    setFilterSelecterUIAction();
                    break;
                case MSG_FILTER_STATE_IDLE_ENLARGED:
                    setFilterPreviewUIAction();
                    break;
            }
        }
    };

    private void setFilterSelecterUIAction() {
        mAppController.getCameraAppUI().setSwipeEnabled(false);
        mAppController.getCameraAppUI().setSwitchBtnEnabled(false);
        mActivity.getCameraAppUI().lockZoom();
        stopFaceDetection();
        if (Keys.areGridLinesOn(mAppController.getSettingsManager()) &&
                isGridLinesEnabled()) {
            mAppController.getCameraAppUI().hideGridLines();
        }
        getPhotoUI().hideFacebeauty();
        /* MODIFIED-BEGIN by jianying.zhang, 2016-10-22,BUG-3201367*/
/* MODIFIED-BEGIN by feifei.xu, 2016-11-03,BUG-3312848*/
//        if(mAppController.getCameraAppUI().needClosePoseFragment()){
//            mAppController.getCameraAppUI().closePoseFragment();
//        }
        mAppController.getCameraAppUI().hidePoseFragment();
        /* MODIFIED-END by feifei.xu,BUG-3312848*/
        /* MODIFIED-END by jianying.zhang,BUG-3201367*/
    }

    private void setFilterPreviewUIAction() {
        mAppController.getCameraAppUI().setSwipeEnabled(true);
        mAppController.getCameraAppUI().setSwitchBtnEnabled(true);
        mActivity.getCameraAppUI().unLockZoom();
        startFaceDetection();
        mAppController.getCameraAppUI().showPoseFragment(); // MODIFIED by feifei.xu, 2016-11-03,BUG-3312848
        if (Keys.areGridLinesOn(mAppController.getSettingsManager()) &&
                isGridLinesEnabled()) {
            mAppController.getCameraAppUI().showGridLines();
        }
        if (isFacebeautyEnabled()) {
            getPhotoUI().showFacebeauty();
        }
    }

    @Override
    protected boolean showFilter() {
        return true;
    }

    @Override
    public void onShutterButtonLongClick() {
    }

    @Override
    public void onFilterClicked() {
        super.onFilterClicked();
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
        if (mPaused) {
            return;
        }
        /* MODIFIED-END by jianying.zhang,BUG-3467717*/
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
        Log.d(TAG, "onFilterClicked mFilterState : " + mFilterState);
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3330109*/
        if (mFilterState == FILTER_STATE_IDLE_ENLARGED) {
        /* MODIFIED-END by jianying.zhang,BUG-3255060*/
            resetZoomValue();
            quitToFilterChoosingWindow();
            hideBottomBarInFilterMode(true);
        }
    }

    private void resetZoomValue() {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
        if (mZoomValue > 1.0f) {
            mZoomValue = 1.0f;
            setCameraParameters(UPDATE_PARAM_ZOOM);
        }
        /* MODIFIED-END by jianying.zhang,BUG-3255060*/
    }

    /* MODIFIED-END by jianying.zhang,BUG-3330109*/
    private void hideBottomBarInFilterMode(boolean needHideBottomBar) {
        if (needHideBottomBar) {
            clearAspectRatioViewer(false); // MODIFIED by jianying.zhang, 2016-11-01,BUG-3278074
            getPhotoUI().setAspectRatioVisible(false);
            mAppController.getCameraAppUI().hideModeOptions();
            mAppController.getCameraAppUI().
                    getTopMenus().setTopModeOptionVisibility(false);
            mAppController.getCameraAppUI().hideBottomBar();
        } else {
            mAppController.getCameraAppUI().showModeOptions();
            if (aspectRatioVisible()) {
                getPhotoUI().setAspectRatioVisible(true);
            }
            mAppController.getCameraAppUI().
                    getTopMenus().setTopModeOptionVisibility(true);
            mAppController.getCameraAppUI().showBottomBar();
        }
    }

    @Override
    protected void updateParametersPictureSize() {
        super.updateParametersPictureSize();
        initMatrix();
    }

    protected void initMatrix() { // MODIFIED by jianying.zhang, 2016-11-08,BUG-3255060
        int cameraId = mActivity.getCameraProvider().getCurrentCameraId();
        if (cameraId >= 0) {
            CameraDeviceInfo.Characteristics info = mActivity.getCameraProvider().getCharacteristics(cameraId);
            mTempMatrix = info.getPreviewTransform(mOrientation, mActivity.getCameraAppUI().getCaptureLayoutHelper().getFullscreenRect(),
                    mActivity.getCameraAppUI().getCaptureLayoutHelper().getPreviewRect());
        } else {
            Log.w(TAG, "Unable to find current camera... defaulting to identity matrix");
            mTempMatrix = new Matrix();
        }
    }

    private Matrix mTempMatrix;

    protected void updatePreviewAreaInFilter(final boolean isSelecting) { // MODIFIED by jianying.zhang, 2016-11-08,BUG-3255060
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CameraAppUI appUI = mActivity.getCameraAppUI();
                CaptureLayoutHelper captureLayoutHelper = appUI.getCaptureLayoutHelper();
                Matrix matrix = new Matrix();

                /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
                if (isSelecting) {
                    if (!(captureLayoutHelper.isFullscreen()
                            || captureLayoutHelper.getPreviewRect().top != 0)) {
                        float marginTop = (captureLayoutHelper.getFullscreenRect().height()
                                - captureLayoutHelper.getPreviewRect().height()) / 2;
                        matrix.set(mTempMatrix);
                        matrix.postTranslate(0, marginTop);
                        float aspectRatio = TextureViewHelper.MATCH_SCREEN;
                        RectF previewRect = captureLayoutHelper.getPreviewRect();
                        if (previewRect.width() != 0 && previewRect.height() != 0) {
                            aspectRatio = previewRect.width() / previewRect.height();
                        }

                        mActivity.getCameraAppUI().updatePreviewTransformFullscreen(matrix, aspectRatio);
                        /* MODIFIED-END by jianying.zhang,BUG-3255060*/
                    }
                } else {
                    mActivity.getCameraAppUI().updatePreviewTransform(mTempMatrix);
                }
            }
        });
    }

    /* MODIFIED-BEGIN by Sichao Hu, 2016-09-23,BUG-2989818*/
    @Override
    protected boolean isOptimizeCapture() {
        return mChosenFilterIndex == INDEX_NONE_FILTER; // MODIFIED by jianying.zhang, 2016-11-08,BUG-3255060
    }

    @Override
    protected void onPreviewStarted() {
        super.onPreviewStarted();
        activeFilterButton();
    }

    protected boolean isCountDownShow() {
        return true;
    }

    protected void activeFilterButton() {
        if (getFilterId() == INDEX_NONE_FILTER) { // MODIFIED by jianying.zhang, 2016-11-08,BUG-3255060
            mFilterhandler.sendEmptyMessage(MSG_INACTIVE_FILTER);
        } else {
            mFilterhandler.sendEmptyMessage(MSG_ACTIVE_FILTER);
        }
    }

    @Override
    public boolean isFacebeautyEnabled() {
        if (mFilterState != FILTER_STATE_IDLE_ENLARGED) {
            return false;
        }
        return super.isFacebeautyEnabled();
    }

    /* MODIFIED-BEGIN by xuan.zhou, 2016-11-03,BUG-3311864*/
    @Override
    public boolean isFilterSelectorScreen() {
        return (mFilterState == FILTER_STATE_IDLE_SHRINKED);
    }
    /* MODIFIED-END by xuan.zhou,BUG-3311864*/


    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
    protected void saveSelectedFilterIndex() {
        mActivity.getSettingsManager().setChosenFilterIndex(mActivity.getCameraScope(),
                Keys.KEY_FILTER_MODULE_SELECTED, mChosenFilterIndex);
    }

    protected int getSelectedFilterIndex(String scope) {
        return mActivity.getSettingsManager().getChosenFilterIndex(scope,
                Keys.KEY_FILTER_MODULE_SELECTED, INDEX_NONE_FILTER);
    }

    @Override
    protected void switchCamera() {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
        if (mPaused) {
            return;
        }
        /* MODIFIED-END by jianying.zhang,BUG-3467717*/
        String scope = mAppController.getCameraScopeByID(Integer.toString(mPendingSwitchCameraId));
        mChosenFilterIndex = getSelectedFilterIndex(scope);
        Log.d(TAG, "switchCamera mChosenFilterIndex : " + mChosenFilterIndex);
        int filterModeId = mAppController.getAndroidContext()
                .getResources().getInteger(R.integer.camera_mode_filter);
        int photoModeId = mAppController.getAndroidContext()
                .getResources().getInteger(R.integer.camera_mode_photo);
        validateFilterSelected(filterModeId, photoModeId);
    }

    protected void validateFilterSelected(int filterIndex, int normalIndex) {
        if (mChosenFilterIndex != INDEX_NONE_FILTER) {
            mAppController.getCameraAppUI().onFilterModuleSelected(filterIndex);
        } else {
            mAppController.getCameraAppUI().onModeSelected(normalIndex);
        }
    }
    /* MODIFIED-END by jianying.zhang,BUG-3255060*/
}

