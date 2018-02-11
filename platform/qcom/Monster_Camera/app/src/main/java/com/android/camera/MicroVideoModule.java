package com.android.camera;

import android.app.ProgressDialog;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.KeyEvent;

import com.android.camera.app.AppController;
import com.android.camera.app.CameraAppUI;
import com.android.camera.app.MediaSaver;
import com.android.camera.debug.Log;
import com.android.camera.decoder.MicroVideoRemixer;
import com.android.camera.decoder.Remixer;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.test.TestUtils; // MODIFIED by wenhua.tu, 2016-08-11,BUG-2710178
import com.android.camera.util.CameraUtil;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.tct.camera.R;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sichao.hu on 10/12/15.
 */
public class MicroVideoModule extends  VideoModule implements MicroVideoController{

    private static final String MICROVIDEO_MODULE_STRING_ID="MicroVideoModule";
    private Log.Tag TAG=new Log.Tag("MicroVideo");
    private MicroVideoUI mUI;
    private static final float PROGRESS_UPPER_BOUND=15000;
    private static final float PROGRESS_LOWER_BOUND=3000;
    private static final float MIN_VIDEO_DURATION_LIMIT=1000;
    private static final int PROGRESS_START_DELAY=800;
    private static final int PROGRESS_UPDATE_DELAY=100;
    private static final int VIDEO_ORIENTATION=90;
    private static final Uri CONTENT_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    private static final int MAX_DURATION_FOR_MICROVIDEO=15000;
    private int mRemainingProgress=MAX_DURATION_FOR_MICROVIDEO;

    private boolean mPaused;
    private boolean mIsMaxVideoProgress;
    private int mRemoveClickTimes;
    private boolean mVolumeKeyLongPressed;
    private boolean mIsMicroVideoSegmentAvailable=false;
    private boolean mIsProgressMaxAuto=false;

    /**
     * Construct a new video module.
     *
     * @param app
     */
    public MicroVideoModule(AppController app) {
        super(app);
    }

    @Override
    protected VideoUI getVideoUI() {
        mUI=new MicroVideoUI(mActivity,this,mActivity.getModuleLayoutRoot());
        return mUI;
    }

    @Override
    protected boolean isNeedStartRecordingOnSwitching() {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Do not handle any key if the activity is paused.
        if (mPaused) {
            return true;
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case CameraUtil.BOOM_KEY:
                if (mUI.isMircoGuideShow()) {
                    return false;
                }
                if (event.isLongPress()) {
                    mVolumeKeyLongPressed = true;
                    mAppController.setShutterPress(true);
                    onShutterButtonLongClick();
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                case KeyEvent.KEYCODE_VOLUME_UP:
                case CameraUtil.BOOM_KEY:
                    if (mUI.isMircoGuideShow()) {
                        return false;
                    }
                    if (mVolumeKeyLongPressed) {
                        mVolumeKeyLongPressed = false;
                        mAppController.setShutterPress(false);
                        onShutterButtonFocus(false); //MODIFIED by wenhua.tu, 2016-04-09,BUG-1911880
                    }
                    return true;
            }
            return false;
    }


    private List<String> mRemixVideoPath=new LinkedList<>();

    private final MediaSaver.OnMediaSavedListener mOnVideoSavedListener =
            new MediaSaver.OnMediaSavedListener() {
                @Override
                public void onMediaSaved(Uri uri) {
                    if(!mNeedAddToStore) {
                        mUI.enableMicroVideoButton();
                        String videoFileName=mCurrentVideoFilename.replace(VIDEO_TEMP_SUFFIXES,"");
                        MediaMetadataRetriever mmr=new MediaMetadataRetriever();
                        mmr.setDataSource(videoFileName);
                        final String durationStr=mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        mmr.release();
                        final float videoDuration=Float.parseFloat(durationStr);
                        if(videoDuration<MIN_VIDEO_DURATION_LIMIT) {
                            onRecordingUnreasonably();
                            removeFileInStorage(videoFileName);
                            if(mRemixVideoPath.size()==0){
                                mIsMicroVideoSegmentAvailable=false;
                                checkMicroVideoState();
                                mUI.resetProgress();
                            }
                            return;
                        }

                        if(mRemixVideoPath.size()==0){
                            mPendingOrientation=getMediaRecorderRotation();
                        }

                        mRemainingProgress-=videoDuration;

                        /*MODIFIED-BEGIN by wenhua.tu, 2016-04-09,BUG-1911880*/
                        mIsMicroVideoSegmentAvailable=true;
                        checkMicroVideoState();
                        /*MODIFIED-END by wenhua.tu,BUG-1911880*/
                        mRemixVideoPath.add(videoFileName);
                        mUI.markSegment(videoDuration);
                        mAppController.getCameraAppUI().showMicroVideoEditButtons(true);
                        Log.w(TAG, "duration is " + durationStr);
                        Log.w(TAG,"remaining duration is "+mRemainingProgress);
                    }else{
                        mCurrentVideoUri = uri;
                        mCurrentVideoUriFromMediaSaved = true;
                        onVideoSaved();
                        dismissRemixHint();
                        mAppController.getCameraAppUI().hideMicroVideoEditButtons(false);
                        mActivity.notifyNewMedia(uri);
                        mNeedAddToStore=false;
                        for(String path :mRemixVideoPath){
                            removeFileInStorage(path);
                        }
                        mRemixVideoPath.clear();
                        mIsMicroVideoSegmentAvailable=false;
                        checkMicroVideoState();
                        mActivity.onPeekThumbClicked(mCurrentVideoUri);
                    }
                }
            };

    @Override
    protected MediaSaver.OnMediaSavedListener getVideoSavedListener() {
        return mOnVideoSavedListener;
    }

    private final void segmentRemove(int index){
        if(index<mRemixVideoPath.size()){
            String path=mRemixVideoPath.remove(index);
            removeFileInStorage(path);
            if(mRemixVideoPath.size()==0){
                mActivity.onLastMediaDataUpdated(); //MODIFIED by wenhua.tu, 2016-04-09,BUG-1911880
                mAppController.getCameraAppUI().hideMicroVideoEditButtons(true);
                mPendingOrientation=0;
                mUI.hideMintimeTip();
            }
        }
    }

    @Override
    public void resume() {
        super.resume();
        mPaused = false;
//        mRemixVideoPath.clear();
        if(!checkMediaFileValidation()){
            mRemixVideoPath.clear();
            mUI.resetProgress();
            mAppController.getCameraAppUI().hideMicroVideoEditButtons(false);
            mRemainingProgress=MAX_DURATION_FOR_MICROVIDEO;
            mIsMicroVideoSegmentAvailable=false;
        }
        checkMicroVideoState();
//        mAppController.getCameraAppUI().hideMicroVideoEditButtons();
        mUI.onResume();
    }

    private boolean checkMediaFileValidation(){
        if(mRemixVideoPath.size()==0){
            return false;
        }
        for(String path:mRemixVideoPath){
            File file=new File(path);
            if(file.exists()){
                continue;
            }else{
                return false;
            }
        }
        return true;
    }

    @Override
    public void pause() {
        recoverFromRecording();
        dismissRemixHint();
        mProgressDialog=null;
        mPaused = true;

//        for(String path : mRemixVideoPath){
//            removeFileInStorage(path);
//        }
//        mRemixVideoPath.clear();
//        mRemainingProgress=MAX_DURATION_FOR_MICROVIDEO;
//        mUI.resetProgress();
        mUI.hideMintimeTip();
        mUI.hideShutterTip();
        mUI.enableMicroIcons();
        mRemoveClickTimes = 0;
        super.pause();
        if(mModeSelectionLock !=null) {
            mActivity.unlockModuleSelection(mModeSelectionLock);
            mModeSelectionLock =null;
            mActivity.getButtonManager().showSettings();
        }
    }

    private void removeFileInStorage(String path){
        File file=new File(path);

        if(file.exists()){
            mContentResolver.delete(CONTENT_URI, MediaStore.Images.ImageColumns.DATA + "=?", new String[] {path});
            file.delete();
        }
    }

    private boolean mNeedAddToStore=false;
    @Override
    protected boolean needAddToMediaSaver() {
        return true;
    }

    @Override
    public String getModuleStringIdentifier() {
        return MICROVIDEO_MODULE_STRING_ID;
    }

    @Override
    public int getModuleId() {
        return mAppController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_micro_video);
    }

    Remixer mRemixer;
    private int mPendingOrientation;
    private void initRemixer(){
        mRemixer=new MicroVideoRemixer();
        mRemixer.setRemxingProgressListener(mRemixProgressListener);
    }

    private void releaseRemixer(){
        mRemixer.releaseRemixer();
    }

    final protected int getOverrodeVideoDuration(){
        Log.w(TAG,"max duration for video recording is "+mRemainingProgress);
        return mRemainingProgress;
    }

    private final Remixer.RemixProgressListener mRemixProgressListener=new Remixer.RemixProgressListener() {
        @Override
        public void onRemixDone() {
            mNeedAddToStore=true;
            saveVideo();
            releaseRemixer();
            mRemainingProgress=MAX_DURATION_FOR_MICROVIDEO;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mUI.resetProgress();
                }
            });
        }
    };
    private void startRemix(){
        if(mRemixVideoPath.size()==0){
            return;
        }
        generateVideoFilename(mProfile.fileFormat);
        mRemixer.setDisplayOrientation(mPendingOrientation);
        mRemixer.prepareForRemixer(mCurrentVideoFilename, mRemixVideoPath);
        mRemixer.startRemix();
    }

    final Runnable updateProgressRunnable=new Runnable() {
        @Override
        public void run() {
            float sumDuration=mUI.getSumDuration();
            long currentTime=System.currentTimeMillis();
            if(mRecordingStartTime==0){
                Log.w(TAG, "initialize recording start time");
                mRecordingStartTime=currentTime;
            }
            long deltaTime=currentTime-mRecordingStartTime;
            float progress=deltaTime+sumDuration;
            if (progress < PROGRESS_UPPER_BOUND && mIsMaxVideoProgress) {
                mIsMaxVideoProgress = false;
                return;
            } else if (progress>=PROGRESS_UPPER_BOUND) {
                if (!mIsMaxVideoProgress) {
                    if (mRemixVideoPath.size() == 0) {
                        mNeedAddToStore = true;
                    }
                    mAppController.getCameraAppUI().enableModeOptions();
                    mAppController.setShutterEnabled(false);
                    boolean recordFail = onStopVideoRecording();
                    mIsMaxVideoProgress = true;
                    mRecordingStartTime=0;
                    Log.w(TAG, "video time is maximum : " + mIsMaxVideoProgress);
                    if (recordFail) {
                        onRecordingUnreasonably();
                    } else {
                        mIsProgressMaxAuto = true;
                        onRemixClicked();
                    }
                    return;
                }
            }

            if(!isRecording()) {
                mRecordingStartTime=0;
                return;
            } else {
                mUI.updateMicroVideoProgress(progress);
            }
            mHandler.postDelayed(this, PROGRESS_UPDATE_DELAY);
        }
    };

    private long mRecordingStartTime=0;
    @Override
    protected void onVideoRecordingStarted() {
        updateProgressRunnable.run();
    }

    @Override
    public void onShutterButtonFocus(final boolean pressed) {
        if(mSwitchingCamera){
            return;
        }
        if (!pressed) {
            recoverFromRecording();
        }
        if (mCameraSettings != null) {
            mFocusManager.onShutterUp(mCameraSettings.getCurrentFocusMode());
        }
    }


    @Override
    protected void stopVideoWhileAudioFocusLoss() {
        onShutterButtonFocus(false);
    }

    private void recoverFromRecording() {
        if (mCameraState==RECORDING || mCameraState == RECORDING_PENDING_START) {
            // CameraAppUI mishandles mode option enable/disable
            // for video, override that
            mAppController.getCameraAppUI().enableModeOptions();
            mAppController.setShutterEnabled(true);
            if (mCameraState==RECORDING) {

                mActivity.getButtonManager().hideSettings();
                boolean recordFail = onStopVideoRecording();
                if (recordFail) {
                    onRecordingUnreasonably();
                }else{
                    mIsMicroVideoSegmentAvailable=true;
                    checkMicroVideoState();
                    return;
                }
            }

            mUI.enableMicroVideoButton();
        }
    }

    @Override
    public void onShutterButtonLongClick() {
        if(mSwitchingCamera){
            return;
        }
        if (mCameraState==IDLE) {
            // CameraAppUI mishandles mode option enable/disable
            // for video, override that
            mAppController.setShutterEnabled(false);
            mUI.disableMicroVideoButton();
            mAppController.getCameraAppUI().disableModeOptions();
            mUI.hideMintimeTip();
            mUI.hideShutterTip();
            mRemoveClickTimes = 0;
            if(mRemixVideoPath.size()==0) {
                mAppController.getCameraAppUI().animateHidePeek();
            }

            startVideoRecording();
            if (mCameraSettings != null) {
                mFocusManager.onShutterUp(mCameraSettings.getCurrentFocusMode());
            }
        }
    }

    @Override
    protected void pendingRecordFailed() {
        onShutterButtonFocus(false);
        super.pendingRecordFailed();
    }

    @Override
    public void onShutterButtonClick() {
//        super.onShutterButtonClick();
        mUI.showShutterTip();
    }

    @Override
    public boolean onBackPressed() {
        recoverFromRecording();
        for(String path:mRemixVideoPath){
            removeFileInStorage(path);
        }
        return super.onBackPressed();
    }


    @Override
    protected boolean isVideoShutterAnimationEnssential() {
        return false;
    }

    @Override
    public void onSegmentRemoveClicked() {
        if(mRemixVideoPath.size()==0){
            return;
        }
        // Tap 2 times, the first time only change the last segment color
        // the second time remove the last segment actually
        if (mRemoveClickTimes == 0) {
            mRemoveClickTimes++;
            mUI.changeLastSegmentColor();
            return;
        }
        int removedProgress=mUI.segmentRemoveOnProgress();
        mRemainingProgress+=removedProgress;
        segmentRemove(mRemixVideoPath.size()-1);
        mRemoveClickTimes = 0;
        if(mRemixVideoPath.size()==0){
            mIsMicroVideoSegmentAvailable=false;
            checkMicroVideoState();
        }
    }

    @Override
    public void onRemixClicked() {
        Log.w(TAG, "onRemixClicked");
        // when there is only a 15s video and video is not recording yet,
        // both will cause mRemixVideoPath.size()=0 and mUI.getSumDuration()=0
        if (mRemixVideoPath.size() > 0 && mUI.getSumDuration() < PROGRESS_LOWER_BOUND && !mIsProgressMaxAuto) {
            mUI.disableRemixButton();
            mUI.showMintimeTip();
            return;
        }
        initRemixer();
        startRemix();
        // showRemixHint() except when remix and remove button clicked at the same time
        // means (run remove first and remix last)
        if (mRemixVideoPath.size() > 0 || mIsProgressMaxAuto) {
            mUI.disableMicroVideoButton();
            showRemixHint();
            if (mIsProgressMaxAuto) {
                mIsProgressMaxAuto = false;
            }
        }
        mPendingOrientation=0;
    }


    ProgressDialog mProgressDialog;
    private void showRemixHint(){
        if(mProgressDialog==null){
            mProgressDialog=new ProgressDialog(mActivity);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(mActivity.getResources().getString(R.string.micro_video_remix_hint));
        }
        mProgressDialog.show();
    }

    private void dismissRemixHint(){
        if(mProgressDialog==null){
            return;
        }
        mProgressDialog.dismiss();
    }

    private void onRecordingUnreasonably() {
        Log.w(TAG, "stop recording fail or video time is less than 1s");
        if(mRemixVideoPath.size()==0) {
            mAppController.getCameraAppUI().setModeSwitchUIVisibility(true);
            mAppController.getCameraAppUI().hideMicroVideoEditButtons(true);
        }
        mUI.showShutterTip();
        mUI.clearPendingProgress();
    }

    @Override
    protected void overrideProfileSize() {
        mProfile.videoFrameWidth = 720;
        mProfile.videoFrameHeight = 720;
    }

    @Override
    public boolean updateModeSwitchUIinModule() {
        return false;
    }

    @Override
    protected boolean shouldHoldRecorderForSecond() {
        return false;
    }

    @Override
    protected void onPreviewStarted() {
        super.onPreviewStarted();
        mUI.disableMicroIcons();
    }


    private Integer mModeSelectionLock =null;
    @Override
    protected void setCameraState(int state) {

        super.setCameraState(state);
        checkMicroVideoState();
    }

    private void checkMicroVideoState(){
        if(mIsMicroVideoSegmentAvailable){
            if(mModeSelectionLock==null) {
                mModeSelectionLock = mActivity.lockModuleSelection();
            }
//            mActivity.getButtonManager().hideSettings();
//            mActivity.getButtonManager().hideButton(ButtonManager.BUTTON_CAMERA);
        }else{
            if(mModeSelectionLock !=null){
                mActivity.unlockModuleSelection(mModeSelectionLock);
                mModeSelectionLock =null;
            }
//            mActivity.getButtonManager().showSettings();
//            mActivity.getButtonManager().show
        }

        Log.v(TAG,"is segment available "+mIsMicroVideoSegmentAvailable);
        mAppController.getCameraAppUI().applyModuleSpecs(this.getHardwareSpec(),this.getBottomBarSpec());
    }

    @Override
    public CameraAppUI.BottomBarUISpec getBottomBarSpec() {
        CameraAppUI.BottomBarUISpec bottomBarSpec = super.getBottomBarSpec();
        if (mIsMicroVideoSegmentAvailable) {
            bottomBarSpec.hideSetting = true;
            bottomBarSpec.hideCamera=true;
            bottomBarSpec.setCameraInvisible=true;
        }
        return bottomBarSpec;
    }

    @Override
    public void destroy() {
        Log.w(TAG,"destory microvideo");
        recoverFromRecording();
        for(String path:mRemixVideoPath){
            removeFileInStorage(path);
        }
        super.destroy();
    }

    @Override
    protected boolean isSendMsgEnableShutterButton() {
        return false;
    }

    /* MODIFIED-BEGIN by wenhua.tu, 2016-08-11,BUG-2710178*/
    public int getRemixVideoPathNum() {
        if (TestUtils.IS_TEST) {
            return mRemixVideoPath.size();
        }

        return 0;
    }
    /* MODIFIED-END by wenhua.tu,BUG-2710178*/
}
