package com.android.camera;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.location.Location;
import android.net.Uri;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;

import com.android.camera.data.LocalMediaData;
import com.android.camera.exif.ExifInterface;
import com.android.camera.exif.ExifTag;
import com.android.camera.settings.Keys;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.ExternalExifInterface;
import com.android.camera.util.NV21Convertor;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.tct.camera.R;

public class ContinueShot {
    private static final String TAG = "ContinueShotRoutine";
    private static final int MAX_CAPTURE_NUM = 10;
    private static ContinueShot CSHOT= null;

    private boolean destroyed = false;
    private CameraActivity mActivity;
    private ContentResolver mContentResolver;
    private PhotoModule mPhotoModule;
    private Camera mCameraDevice;
    private Handler mUiHandler;
    private Handler mSaveHandler = null;
    private HandlerThread mSaveHT = null;
    private ProgressDialog mProgressDialog;
    private ActivityManager mActivityManager;
    private ActivityManager.MemoryInfo mMemInfo;

    private JpegInfo lastJpegInfo;
    private Bitmap mPreviewThumb = null;
    private int rotation;
    private int jpegOrientation;
    private Size size;
    private Parameters param;
    private int originalJpegQuality;
    private CameraCapabilities.FlashMode mFlashMode;
    private Location loc;

    private onContinueShotFinishListener mListener;
    private ContinueShotPictureCallback mCB;
    private int mMaxCaptureNum = MAX_CAPTURE_NUM;
    private int mDisplayCaptureNum = 0;
    private int mSaveCaptureNum = 0;
    private int mTakenNum = 0;
    private int interval = 120;
    private boolean isJpegReady = false;
    private boolean mSoundEnable = true;
    private boolean isStop = false;
    private boolean isProcessing = true;
    private SoundPlay mSoundPlay = null;

    private ConditionVariable mReady = new ConditionVariable(true);

    private NV21Convertor mNV21Convertor;

    private ContinueShot(PhotoModule module) {
        mActivity = module.mActivity;
        mNV21Convertor=new NV21Convertor(mActivity.getApplicationContext());
        mContentResolver = mActivity.getContentResolver();
        mPhotoModule = module;
        mCameraDevice = module.mCameraDevice.getCamera();
        param = mCameraDevice.getParameters();
        originalJpegQuality = param.getJpegQuality();
        mSoundEnable = Keys.isShutterSoundOn(mActivity.getSettingsManager());
        jpegOrientation = mPhotoModule.getJpegRotation(false);
        mFlashMode = mPhotoModule.mCameraSettings.getCurrentFlashMode();
        loc = mActivity.getLocationManager().getCurrentLocation();
        CameraUtil.setGpsParameters(mPhotoModule.mCameraSettings, loc);
        mPhotoModule.mCameraDevice.applySettings(mPhotoModule.mCameraSettings);

        mUiHandler = new Handler(module.mHandler.getLooper(), new UiHandlerCB());
        mSaveHT = new HandlerThread("ContinueShotSave", Thread.MIN_PRIORITY);
        mSaveHT.start();
        mSaveHandler = new Handler(mSaveHT.getLooper(), new SaveHandlerCB());
        mActivityManager = (ActivityManager) mActivity.getSystemService(mActivity.ACTIVITY_SERVICE);
        mMemInfo = new ActivityManager.MemoryInfo();
        mSoundPlay = new SoundPlay(mSoundEnable);
    }

    public synchronized static ContinueShot create(PhotoModule module) {
        destroy();
        CSHOT = new ContinueShot(module);
        return CSHOT;
    }

    public synchronized static void destroy() {
        if (CSHOT != null && !CSHOT.destroyed) {
            CSHOT.destroyed = true;
            CSHOT.mSaveHT.quit();
        }
        CSHOT = null;
    }

    public interface onContinueShotFinishListener {
        public void onFinish();
    }

    public void prepare() {
        isJpegReady = false;
        rotation = mCameraDevice.getParameters().getInt("rotation");
        size = mCameraDevice.getParameters().getPreviewSize();
        mCameraDevice.setPreviewCallback(new PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] arg0, Camera arg1) {
                if (!isJpegReady) {
                    Bitmap bmp = compressToThumb(arg0, size.width, size.height);
                    Bitmap thumb = null;
                    if ((jpegOrientation % 360) != 0) {
                        Matrix matrix = new Matrix();
                        matrix.setRotate(jpegOrientation);
                        thumb = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                        bmp.recycle();
                        bmp = null;
                        mPreviewThumb = thumb;
                    } else {
                        mPreviewThumb = bmp;
                    }
                } else {
                    mCameraDevice.setPreviewCallback(null);
                }
            }
        });
    }

    private void setflashMode() {
        if(mFlashMode == null)
            return;

        if (mFlashMode == CameraCapabilities.FlashMode.AUTO) {
            param.setFlashMode(Parameters.FLASH_MODE_OFF);
        }else if (mFlashMode == CameraCapabilities.FlashMode.ON) {
            param.setFlashMode(Parameters.FLASH_MODE_TORCH);
        }else if (mFlashMode == CameraCapabilities.FlashMode.OFF) {
            param.setFlashMode(Parameters.FLASH_MODE_OFF);
        }
    }

    private Bitmap compressToThumb(byte[] yuv, int rw, int rh) {
        Bitmap bmp=mNV21Convertor.convertNV21ToBitmap(yuv,rw,rh);
        return bmp;
    }

    public void takePicture() {
        takePicture(null);
    }

    public void takePicture(onContinueShotFinishListener l) {
        Log.d(TAG, "takePicture====");
        isJpegReady = false;
        mReady.close();
        mListener = l;
        mCB = new ContinueShotPictureCallback();
        start();

        param.set("snapshot-burst-num", MAX_CAPTURE_NUM);
        param.setJpegQuality(85);
        param.setRotation(jpegOrientation);
        setflashMode();
        mCameraDevice.enableShutterSound(false);
        mCameraDevice.setParameters(param);
        calcInterval(param.getPictureSize());
        mCameraDevice.takePicture(null, null, null, mCB);
    }


    public void start() {
        new Thread(new Runnable() {
            long time = 0L;
            public void run() {
                isStop = false;
                mSoundPlay.load();
                Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);

                //First JPEG CB HERE
                mReady.block(500);
                Log.d(TAG, "mReady====");
                while (!isStop && mDisplayCaptureNum < mMaxCaptureNum) {
                    time = System.currentTimeMillis() + interval;
                    mDisplayCaptureNum++;
                    mSoundPlay.play();
                    Log.d(TAG, "sendEmptyMessage:" + mDisplayCaptureNum);
                    mUiHandler.sendEmptyMessage(mDisplayCaptureNum); //UI Display
                    mSaveHandler.sendEmptyMessage(mDisplayCaptureNum);
                    long slptm = time - System.currentTimeMillis();
                    if (slptm > 0) {
                        SystemClock.sleep(slptm);
                    }
                }
                mSoundPlay.unLoad();

                if (isStop || mDisplayCaptureNum == mMaxCaptureNum) {
                    Log.d(TAG, "sendEmptyMessage stop:" + mDisplayCaptureNum);
                    mSaveHandler.sendEmptyMessage(mDisplayCaptureNum);
                }
            }
        }).start();
    }

    public synchronized void stop() {
        Log.d(TAG, "Stop bustShot");
        isStop = true;
    }

    public synchronized void close() {
        Log.d(TAG, "close====");
        if (!isProcessing) {
            return;
        }
        isProcessing = false;
        mUiHandler.sendEmptyMessage(0); //hide BurstCount
        dismissSavingHint();
        param.set("snapshot-burst-num", 1);
        param.setJpegQuality(originalJpegQuality);
        param.setFlashMode(Parameters.FLASH_MODE_OFF);
        mCameraDevice.enableShutterSound(mSoundEnable);
        mCameraDevice.setParameters(param);
        destroy();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onFinish();
                    realseAfterFinish();
                }
            }
        });
    }

    public synchronized boolean canclePicture() {
        Method pictureMethod = null;
        try {
            pictureMethod = Class.forName("android.hardware.Camera").getMethod("cancelPicture", (Class[])null);
            pictureMethod.invoke(mCameraDevice, (Object[])null);
            Log.d(TAG, "canclePicture succeed");
            return true;
        } catch (Exception e) {
            Log.d(TAG, "canclePicture failed" );
            e.printStackTrace();
            return false;
        }
    }

    public void realseAfterFinish() {
        if (!isProcessing) {
            if (loc != null) {
                loc = null;
            }
            if (mCB != null && mCB.mJpegQueue != null) {
                mCB.mJpegQueue.clear();
                mCB.mJpegQueue = null;
            }
            if (mCB != null && mCB.mThumbQueue != null) {
                mCB.mThumbQueue.clear();
                mCB.mThumbQueue = null;
            }
            mCB = null;
        }
    }

    private void calcInterval(Size size) {
        interval = 80;
        int pixel = size.width * size.height;
        if (pixel >= 3264 * 2448) {
            interval = 80;
        } else if (pixel >= 2592 * 1944) {
            interval = 80;
        } else if (pixel >= 1600 * 1200) {
            interval = 80;
        } else if (pixel >= 1024 * 768) {
            interval = 80;
        } else {
            interval = 80;
        }
    }

    // **************CLASS-SaveHandlerCB*******************/
    private class SaveHandlerCB implements Handler.Callback {
        int num = 0;
        int lastNum = 0;
        int retry = 0;
        @Override
        public boolean handleMessage(Message arg0) {

            num = arg0.what;
            if(lastNum == num){
                Log.d(TAG, "mDisplayCapture num = " + lastNum + "mPictureTaken num  = " + mTakenNum +
                        "savePicture num = " + mSaveCaptureNum);

                while (num > mTakenNum) {
                    retry++;
                    if (retry == 1) {
                        mUiHandler.sendEmptyMessage(num);//show saving hint
                    }
                    if (retry > 5) {
                        break;
                    }
                    Log.d(TAG, "wait ... Picture taken  num = " + (mTakenNum + 1));
                    SystemClock.sleep(100);
                }

                canclePicture();

                while (num > mSaveCaptureNum) {
                    Log.d(TAG, "wait ... saving picture num = " + (mSaveCaptureNum + 1));
                    SystemClock.sleep(100);
                }
                close();
                return true;
            }

            lastNum = num;

            Log.d(TAG, "send message savePicture num = " + num);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    savePicture(num);
                }

            }).start();
            return true;
        }
    }

    public int savePicture(int count) {

        Log.d(TAG, "savePicture node1  num = "+count);

        int tempCount = count;
        JpegInfo ji = null;
        try {
            ji = mCB.mJpegQueue.poll(500, TimeUnit.MILLISECONDS);
            //ji = mCB.mJpegQueue.take();
        } catch (Exception e) {
        }

        if (ji == null) {
            ji = lastJpegInfo;
        } else {
            lastJpegInfo = ji;
        }

        if (ji == null) {
            return -1;
        }

        byte[] tempdata = ji.jpegData;
        Location location = ji.location;
        long date= ji.captureStartTime;
        String title = "Snapshot_" + CameraUtil.createJpegName(date) ;
        ExifInterface exif = Exif.getExif(tempdata);
        int orientation = Exif.getOrientation(exif);
        int width, height;
        String jpegPath;
        final String mimeType = "image/jpeg";

        final Map<String,Object> externalBundle=new HashMap<>();
        externalBundle.put(ExternalExifInterface.BURST_SHOT_ID, mCB.hashCode());
        externalBundle.put(ExternalExifInterface.BURST_SHOT_INDEX, tempCount);
        if(externalBundle!=null){
            String externalJson=CameraUtil.serializeToJson(externalBundle);
            ExifTag externalTag=exif.buildTag(ExifInterface.TAG_USER_COMMENT, externalJson);
            exif.setTag(externalTag);
        }
        if ( (rotation + orientation) % 180 == 0) {
            width = size.width;
            height = size.height;
        } else {
            width = size.height;
            height = size.width;
        }
        jpegPath = Storage.generateFilepath(title);
        Storage.writeFile(jpegPath,tempdata, exif);

        Log.d(TAG, "savePicture node2  num = " + tempCount);

        ContentValues values = Storage.getContentValuesForData(title, date, location, orientation, tempdata.length, jpegPath, width,
                        height, mimeType);
        Uri uri = null;
        try {
            uri = mContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (Throwable th)  {
        }

        Log.d(TAG, "savePicture node2  num = " + tempCount);

        if (uri != null) {
            Log.d(TAG, "notifyNewMedia  num = " + tempCount);
            // This is not used now, ignore the secure uri here,
            // if (mActivity.isSecureCamera()) {
            //    mActivity.getSecureUri().add(uri);
            // }
            CameraUtil.broadcastNewPicture(mActivity.getAndroidContext(), uri);
            LocalMediaData.PhotoData.fromContentUri(mActivity.getContentResolver(), uri);
            mActivity.getCameraAppUI().updatePeekThumbUri(uri);
        }

        mSaveCaptureNum ++;
        Log.d(TAG, "savePicture node3  num = " + tempCount);
        if (mSaveCaptureNum >= mMaxCaptureNum) {
            Log.d(TAG, "All picture saved and close snapShot = " + mSaveCaptureNum);
            //close();
        }

        return mSaveCaptureNum;
    }

    private void showSavingHint(int count){
        if(count==0){
            return;
        }
        if(mProgressDialog==null){
            mProgressDialog=new ProgressDialog(mActivity);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(String.format(mActivity.getAndroidContext().getResources().getString(R.string.burst_saving_hint), count));
        mProgressDialog.show();
    }

    private void dismissSavingHint(){
        if(mProgressDialog!=null&&mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }

    // **************CLASS-UiHandlerCB*******************/
    private class UiHandlerCB implements Handler.Callback {
        int num = 0;
        int lastNum = 0;
        @Override
        public boolean handleMessage(Message arg0) {

            num = arg0.what;
            if (lastNum == num) {
                showSavingHint(num);
                return true;
            }
            lastNum = num;
            mPhotoModule.getPhotoUI().updateBurstCount(num);
            if (num == 0) {
                return true;
            }
            if (mCB != null) {
                Bitmap bmp = mCB.mThumbQueue.get(num, null);
                Log.d(TAG, "UiHandlerCB bmp:" + bmp + "  num:" + num);
                if (bmp == null && mCB.mThumbQueue.size() > 0) {
                    bmp = mCB.mThumbQueue.get(mCB.mThumbQueue.size(), null);
                }
                if (bmp == null) {
                    //use preview thumb
                    bmp = mPreviewThumb;
                    Log.d(TAG, "UiHandlerCB mPreviewThumb:" + bmp + "  num:" + num);
                }
                if (bmp != null) {
                    mActivity.getCameraAppUI().updatePeekThumbBitmapWithAnimation(bmp);
                }
            }
            Log.d(TAG, "UiHandlerCB updateBurstCount = " + num);
            return true;
        }
    }

    // **************CLASS-SoundPlay*******************/
    private class SoundPlay {
        private android.media.SoundPool soundPool = null;
        private int soundID = 0;
        private int streamID = 0;
        private boolean soundEnable;

        private SoundPlay(boolean s) {
            soundEnable = s;
        }

        private void load() {
            if (!soundEnable) {
                return;
            }
            soundPool = new android.media.SoundPool(10,
                    SoundClips.getAudioTypeForSoundPool(), 0);
            soundID = soundPool.load(mActivity, com.tct.camera.R.raw.continuous_shot, 1);
        }

        private void play() {
            if (!soundEnable) {
                return;
            }
            if (streamID != 0) {
                soundPool.stop(streamID);
            }
            streamID = soundPool.play(soundID, 0.5f, 0.5f, 1, 0, 1.5f);
        }

        private void unLoad() {
            if (soundEnable && soundPool != null) {
                soundPool.unload(soundID);
                soundPool.release();
                soundPool = null;
            }
        }
    }

    // **************CLASS-JpegInfo*******************/
    private final class JpegInfo {
        byte[] jpegData;
        long captureStartTime;
        Location location;

        public JpegInfo(byte[] data, long time) {
            this(data,time, null);
        }

        public JpegInfo(byte[] data, long time, Location l) {
            jpegData = data;
            captureStartTime = time;
            location = l;
        }
    }

    // **************CLASS-ContinueShotPictureCallback*******************/
    class ContinueShotPictureCallback implements Camera.PictureCallback {
        ArrayBlockingQueue<JpegInfo> mJpegQueue;
        SparseArray<Bitmap> mThumbQueue;

        public ContinueShotPictureCallback() {
            mJpegQueue = new ArrayBlockingQueue<JpegInfo>(MAX_CAPTURE_NUM + 1);
            mThumbQueue = new SparseArray<Bitmap>(MAX_CAPTURE_NUM + 1);
        }

        @Override
        public void onPictureTaken(final byte[] jpegData, Camera camera) {
            mTakenNum++;
            if (mTakenNum == 1) {
                mReady.open();
            }
            if (mTakenNum > MAX_CAPTURE_NUM) {
                //CanclePicture
                return;
            }
            JpegInfo ji = new JpegInfo(jpegData, System.currentTimeMillis(),loc);
            try {
                mJpegQueue.put(ji);
            } catch (Exception e) {
            }
            mThumbQueue.put(mTakenNum, Exif.getExif(jpegData).getThumbnailBitmap());
            isJpegReady = true;
            Log.d(TAG, "PictureCallback num = " + mTakenNum);
        }
    }
}
