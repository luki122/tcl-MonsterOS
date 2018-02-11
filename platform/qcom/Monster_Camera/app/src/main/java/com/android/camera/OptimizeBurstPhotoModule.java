package com.android.camera;

import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;

import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.exif.ExifInterface;
import com.android.camera.settings.Keys;
import com.android.ex.camera2.portability.CameraAgent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sichao.hu on 1/18/16.
 */
public class OptimizeBurstPhotoModule extends NormalPhotoModule {

    private static final String OPT_BURST_PHOTO_MODULE_STRING_ID="OptBurstModule";
    private Log.Tag TAG=new Log.Tag(OPT_BURST_PHOTO_MODULE_STRING_ID);

    private static final int BURST_UPDATE_INTERVAL=80;
    private static final int BURST_UPDATE_DELAY=0;
    private static final int BURST_ACTION_INIT =0;

    private static final int BURST_ACTION_DISPLAY_DONE =1;
    private static final int BURST_ACTION_SNAP_DONE =1<<1;
    private static final int BURST_ACTION_FINISH = BURST_ACTION_DISPLAY_DONE | BURST_ACTION_SNAP_DONE;
    private int mBurstState=BURST_ACTION_INIT;

    private OptimizeLongshotPictureCallback mOptimizedLongshotCallback;


    private List<TaggedRunnable> mSavingQueue=new LinkedList<>();

    private HandlerThread mSaveQueueExecutorThread;


    private Handler mSaveQueueExecutorHandler;


    private HandlerThread mSoundPlayerThread;
    protected Handler mSoundPlayerHandler;
    private Object mSoundPlayerLock=new Object();

    public OptimizeBurstPhotoModule(AppController app) {
        super(app);

    }

    private final Runnable mSoundPlayRunnable=new Runnable() {
        @Override
        public void run() {
            synchronized (mSoundPlayerLock) {
                if (mSoundPlayer != null) {
                    mSoundPlayer.play();
                }
            }
            Log.w(TAG,"mCameraState is "+mCameraState+" burstState is "+mBurstState);
            if(mCameraState!=SNAPSHOT_LONGSHOT_PENDING_START
                    &&mCameraState!=SNAPSHOT_LONGSHOT){
                return;
            }
            if((mBurstState&BURST_ACTION_DISPLAY_DONE)!=0){
                return;
            }
            mSoundPlayerHandler.postDelayed(this,BURST_UPDATE_INTERVAL);
        }
    };

    private final Runnable mBurstUpdateRunnable =new Runnable() {
        @Override
        public void run() {
            mReceivedBurstNum++;
            Log.e(TAG,"PreAllocBurst updateBurstNum "+mReceivedBurstNum);
            Log.v(TAG,"update burst count ");
            getPhotoUI().updateBurstCount(mReceivedBurstNum, BURST_MAX);

            checkBurstSaveQueue(mReceivedBurstNum);

            if(mReceivedBurstNum>=BURST_MAX
                    ||(mCameraState!=SNAPSHOT_LONGSHOT_PENDING_START
                    &&mCameraState!=SNAPSHOT_LONGSHOT)){
                if(mCameraState!=IDLE) {
                    if(mSavingQueue.size()<mReceivedBurstNum){
                        showSavingHint(mReceivedBurstNum);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!isInBurstshot()) {
                                    getPhotoUI().updateBurstCount(0, BURST_MAX);
                                }
                            }
                        },BURST_STOP_DELAY);

                    }
                }
                onBurstStateUpdate(BURST_ACTION_DISPLAY_DONE);
                return;
            }
            mHandler.postDelayed(this, BURST_UPDATE_INTERVAL);
        }
    };


    @Override
    protected void showSavingHint(int count) {
        //dummy , don't show saving hint
        return ;
    }

    private void checkBurstSaveQueue(int currentDisplayNum){
        final int jobNumToCheck=(mSavingQueue.size()<=currentDisplayNum?mSavingQueue.size():currentDisplayNum);

        Log.v(TAG, "currentDisplayNum is  " + currentDisplayNum + " current saveQueueSize is " + mSavingQueue.size(), new Throwable());


        for(int i=0;i<jobNumToCheck;i++){
            final TaggedRunnable runnable=mSavingQueue.get(i);
            if(!runnable.isJobFinished()) {
                runnable.tagJob();
                mSaveQueueExecutorHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        runnable.run();
                    }
                });
            }
        }
    }

    private void onBurstStateUpdate(int action){
        Log.w(TAG, "onBurstStateUpdate ,current Burst state is " + mBurstState + ",Action is " + action, new Throwable());
        mBurstState|=action;
        if(mBurstState==BURST_ACTION_DISPLAY_DONE){//BURST_ACTION_SNAP_DONE  not triggered yet , if snap shot process rather fast ,the onPictureTaken would get over before the
            if(mSavingQueue.size()>mReceivedBurstNum){
                onBurstStateUpdate(BURST_ACTION_SNAP_DONE);
                return;
            }
        }
        if(mBurstState== BURST_ACTION_FINISH){
            mBurstState=BURST_ACTION_INIT;
            checkBurstSaveQueue(mReceivedBurstNum);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopBurst();
                }
            }, BURST_STOP_DELAY);
        }

    }

    @Override
    protected boolean takeOptimizedBurstShot(Location loc) {
        mSavingQueue.clear();
        mBurstState=BURST_ACTION_INIT;
        mOptimizedLongshotCallback = new OptimizeLongshotPictureCallback(loc);
        synchronized (mSoundPlayerLock) {
            mSoundPlayer = new SoundPlay(Keys.isShutterSoundOn(mAppController.getSettingsManager()));
            mSoundPlayer.load();
        }
        setCameraState(SNAPSHOT_LONGSHOT_PENDING_START);
        mCameraDevice.startPreAllocBurstShot();
        mCameraDevice.applySettings(mCameraSettings);
        mCameraDevice.takePicture(mHandler, new OptBurstShutterCallback(), null, null, mOptimizedLongshotCallback);
        return true;
    }

    @Override
    protected void abortOptimizedBurstShot() {
        mCameraDevice.stopPreAllocBurstShot();
        mCameraDevice.applySettings(mCameraSettings);
    }

    @Override
    public void resume() {
        mSaveQueueExecutorThread=new HandlerThread(OPT_BURST_PHOTO_MODULE_STRING_ID+"_saveExecutorThraed");
        mSaveQueueExecutorThread.start();
        mSaveQueueExecutorHandler=new Handler(mSaveQueueExecutorThread.getLooper());

        mSoundPlayerThread=new HandlerThread(OPT_BURST_PHOTO_MODULE_STRING_ID+"_soundPlayerThread");
        mSoundPlayerThread.start();;
        mSoundPlayerHandler =new Handler(mSoundPlayerThread.getLooper());
        super.resume();
    }

    @Override
    public void pause() {
        if(isInBurstshot()) {
            mHandler.removeCallbacks(mBurstUpdateRunnable);
            mSoundPlayerHandler.removeCallbacks(mSoundPlayRunnable);
            checkBurstSaveQueue(mReceivedBurstNum);
        }
        super.pause();
        if(mSaveQueueExecutorThread!=null) {
            mSaveQueueExecutorThread.quitSafely();
            mSaveQueueExecutorHandler = null;
            mSaveQueueExecutorThread = null;
        }

        if(mSoundPlayerThread!=null) {
            mSoundPlayerThread.quitSafely();
            mSoundPlayerThread = null;
            mSoundPlayerHandler = null;
        }
    }

    @Override
    protected void stopBurst() {
        Log.w(TAG,"stop burst ",new Throwable());
        super.stopBurst();
    }

    @Override
    protected int getBurstShotMediaSaveAction() {
        return super.getBurstShotMediaSaveAction()|AppController.NOTIFY_NEW_MEDIA_ACTION_OPTIMIZECAPTURE;
    }

    protected class OptimizeLongshotPictureCallback extends LongshotPictureCallback{

        public OptimizeLongshotPictureCallback(Location loc) {
            super(loc);
        }

        private int mOptimizedLongshotCount=0;


        private int mDebugCount=0;
        @Override
        public void onPictureTaken(final byte[] originalJpegData, final CameraAgent.CameraProxy camera) {
            if (mPaused) {
                return;
            }
            mOptimizedLongshotCount++;
            Log.w(TAG, "burst shot count  is " + mOptimizedLongshotCount+" mReceived num is "+mReceivedBurstNum+" current state is "+mCameraState);
            if(mCameraState==IDLE){
                return;
            }

            if(mCameraState!= SNAPSHOT_LONGSHOT_PENDING_START &&mCameraState!=SNAPSHOT_LONGSHOT&&mCameraState!=SNAPSHOT_LONGSHOT_PENDING_STOP){
                Log.w(TAG, "stop burst in picture taken");
                stopBurst();
                return;
            }
            // Do not take the picture if there is not enough storage.
            if (mActivity.getStorageSpaceBytes() <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
                Log.i(TAG, "Not enough space or storage not ready. remaining="
                        + mActivity.getStorageSpaceBytes());
                mVolumeButtonClickedFlag = false;
                stopBurst();
                return;
            }

            if(mCameraState==SNAPSHOT_LONGSHOT_PENDING_STOP&&mSavingQueue.size()==0){
            //under this case , burst shot never started , we need manually stop burst display and notify the action
                onBurstStateUpdate(BURST_ACTION_DISPLAY_DONE);
            }else if(mCameraState== SNAPSHOT_LONGSHOT_PENDING_START){
                if(mHandler !=null) {
                    mHandler.postDelayed(mBurstUpdateRunnable,BURST_UPDATE_DELAY);
                    mSoundPlayerHandler.post(mSoundPlayRunnable);
                }
                mAppController.getCameraAppUI().setModeStripViewVisibility(false);
                setCaptureView(true);
                setCameraState(SNAPSHOT_LONGSHOT);
            }


//            mReceivedBurstNum++;
//            getPhotoUI().updateBurstCount(mReceivedBurstNum);


            final long pictureTakenTime= System.currentTimeMillis();
            /* MODIFIED-BEGIN by yuanxing.tan, 2016-04-20,BUG-1972920*/
            mNamedImages.nameNewImage(pictureTakenTime);
            final NamedImages.NamedEntity name = mNamedImages.getNextNameEntity();
            /* MODIFIED-END by yuanxing.tan,BUG-1972920*/
            TaggedRunnable saveRunnable=new TaggedRunnable() {
                @Override
                public synchronized void run() {
                    mDebugCount++;
                    Log.w(TAG, "run tagged runnable :" + mDebugCount);
                    final ExifInterface exif = Exif.getExif(originalJpegData);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateThumbnail(exif);
                        }
                    });
/* MODIFIED-BEGIN by yuanxing.tan, 2016-04-20,BUG-1972920*/
//                    mNamedImages.nameNewImage(pictureTakenTime);
                    updateExifAndSave(name, exif,originalJpegData,camera);
                    /* MODIFIED-END by yuanxing.tan,BUG-1972920*/
                }
            };

            mSavingQueue.add(saveRunnable);


            Log.w(TAG,"saving queue size is "+mSavingQueue.size()+" longShot count is "+mOptimizedLongshotCount);
            if(mSavingQueue.size()>=BURST_MAX||(mCameraState==SNAPSHOT_LONGSHOT_PENDING_STOP&&mSavingQueue.size()>=mReceivedBurstNum)){
                if((mBurstState&BURST_ACTION_SNAP_DONE)==0) {
                    OptimizeBurstPhotoModule.this.onBurstStateUpdate(BURST_ACTION_SNAP_DONE);
                }
            }
        }
    }

    @Override
    protected void unloadSoundPlayer() {
        synchronized (mSoundPlayerLock) {
            if (mSoundPlayer != null) {
                mSoundPlayer.unLoad();
                mSoundPlayer = null;
            }
        }
    }

    private final class OptBurstShutterCallback implements CameraAgent.CameraShutterCallback {
        @Override
        public void onShutter(CameraAgent.CameraProxy camera) {
            Log.v(TAG, "burst shot callback");
        }
    }


    private abstract class TaggedRunnable implements   Runnable{

        protected boolean mIsJobInQueue =false;

        public boolean isJobFinished(){
            return mIsJobInQueue;
        }

        public void tagJob(){
            mIsJobInQueue=true;
        }

        @Override
        public abstract void run() ;
    }
}
