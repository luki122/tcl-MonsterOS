package com.android.camera;

import android.graphics.Rect;
import android.media.MediaRecorder;
import android.util.Base64;
import android.widget.Toast;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.options.SerializeOptions;
import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.util.ToastUtil;
import com.android.camera.util.XmpUtil;
import com.android.ex.camera2.portability.Size;
import com.tct.camera.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Sean Scott on 9/13/16.
 */
public class CylindricalPanoramaModule extends TS_PanoramaGPModule
        implements MediaRecorder.OnErrorListener {

    public static final String CYL_PANO_MODULE_STRING_ID = "Cyl_Panorama";
    private static final Log.Tag TAG = new Log.Tag(CYL_PANO_MODULE_STRING_ID);

    private static final double ANGLE_OF_VIEW_DEGREE_360_PHOTO = 67.0d;

    private static final double MAX_ANGLE = 360.0d;

    public static final String CROPPED_AREA_IMAGE_WIDTH_PIXELS = "CroppedAreaImageWidthPixels";
    public static final String CROPPED_AREA_IMAGE_HEIGHT_PIXELS = "CroppedAreaImageHeightPixels";
    public static final String CROPPED_AREA_FULL_PANO_WIDTH_PIXELS = "FullPanoWidthPixels";
    public static final String CROPPED_AREA_FULL_PANO_HEIGHT_PIXELS = "FullPanoHeightPixels";
    public static final String CROPPED_AREA_LEFT = "CroppedAreaLeftPixels";
    public static final String CROPPED_AREA_TOP = "CroppedAreaTopPixels";
    public static final String PROJECTION_TYPE = "ProjectionType";
    public static final String GOOGLE_PANO_NAMESPACE = "http://ns.google.com/photos/1.0/panorama/";

    public static final String AUDIO_DATA = "Data";
    public static final String GOOGLE_AUDIO_NAMESPACE = "http://ns.google.com/photos/1.0/audio/";

    public static final String PROJECTION_TYPE_CYL_PANO = "equirectangular";

    private SettingsManager mSettingsManager;
    private boolean mRecordAudio;

    private MediaRecorder mMediaRecorder;
    private File mTempFile;

    public static final String VOICE_PHOTO = "VoicePhoto";
    private String mVoicePhotoPath;

    public CylindricalPanoramaModule(AppController app) {
        super(app);
        mSettingsManager = app.getSettingsManager();
        mRecordAudio = Keys.isPhotoAudioRecordOn(mSettingsManager);
        if (mRecordAudio) {
            createVoicePhotoPath();
        }
    }

    @Override
    protected PhotoUI getPhotoUI() {
        if (mUI == null) {
            mUI = new CylPanoramaUI(mActivity, this, mActivity.getModuleLayoutRoot());
            initWidgetSize();
        }
        return mUI;
    }

    @Override
    public int getModuleId() {
        return mAppController.getAndroidContext().getResources()
                .getInteger(R.integer.camera_mode_360_photo);
    }

    @Override
    public String getModuleStringIdentifier() {
        return CYL_PANO_MODULE_STRING_ID;
    }

    @Override
    public boolean isSelfie() {
        return false;
    }
    @Override
    protected boolean isSelfieSupported() {
        return false;
    }

    @Override
    public boolean is360Photo() {
        return true;
    }

    @Override
    protected double getAngleOfViewDegree(float h_fov, float v_fov) {
        return ANGLE_OF_VIEW_DEGREE_360_PHOTO;
    }

    @Override
    protected double getMaxAngle() {
        return MAX_ANGLE;
    }

    @Override
    public void onAudioRecordOnOffSwitched() {
        mRecordAudio = Keys.isPhotoAudioRecordOn(mSettingsManager);
        if (mRecordAudio) {
            createVoicePhotoPath();
        }
    }

    private void createVoicePhotoPath() {
        if (mVoicePhotoPath != null && mVoicePhotoPath.startsWith(Storage.DIRECTORY)) {
            return;
        }
        mVoicePhotoPath = Storage.DIRECTORY + "/" + VOICE_PHOTO;
        File dir = new File(mVoicePhotoPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.isDirectory() || !dir.canWrite()) {
            Log.e(TAG, "createVoicePhotoPath failed");
        }
    }

    @Override
    public String getBasePath() {
        if (mRecordAudio && mVoicePhotoPath != null) {
            return mVoicePhotoPath;
        }
        return super.getBasePath();
    }

    public void startAudioRecording() {
        if (!mRecordAudio) {
            return;
        }

        createMediaRecorder();
        if (mMediaRecorder == null) {
            return;
        }

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.setOnErrorListener(this);
            mMediaRecorder.start();
        } catch (IOException e) {
            Log.e(TAG, "startAudioRecording fail " + e);
            e.printStackTrace();
            releaseMediaRecorder();
            deleteTempFile();
        }
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder == null) {
            return;
        }
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    private void createMediaRecorder() {
        createTempFile();
        if (mTempFile == null || !mTempFile.exists()) {
            Log.e(TAG, "createMediaRecorder fail.");
            return;
        }
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOutputFile(mTempFile.getPath());
    }

    private void createTempFile() {
        deleteTempFile();

        String path = getBasePath() + "/" + getAudioMp4FileName();
        mTempFile = new File(path);
        try {
            mTempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            deleteTempFile();
        }
    }

    private void deleteTempFile() {
        if (mTempFile == null) {
            return;
        }
        if (mTempFile.length() > 0) {
            mTempFile.delete();
        }
    }

    public void stopAudioRecording() {
        if (!mRecordAudio) {
            return;
        }
        try {
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.stop();
        } catch (RuntimeException e) {
            Log.e(TAG, "stopAudioRecording fail " + e);
            e.printStackTrace();
            deleteTempFile();
        }
        releaseMediaRecorder();
    }

    @Override
    public void onError(MediaRecorder mediaRecorder, int what, int extra) {
        Log.e(TAG, "MediaRecorder error. what=" + what + ". extra=" + extra);
        if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
            stopAudioRecording();
            deleteTempFile();
        }
    }

    @Override
    protected void writeMetadata(String file_path, Rect rect) {
        if (file_path == null || rect == null || rect.isEmpty()) {
            Log.e(TAG, "Abnormal file_path " + file_path + " rect " + rect);
            return;
        }

        Size dstImgSize = getDstImageSize();
        if (dstImgSize == null) {
            return;
        }

        int fullPanoHeightPixels = dstImgSize.width() / 2;
        int fullPanoWidthPixels = 2 * fullPanoHeightPixels;

        int croppedAreaImageWidthPixels = rect.width();
        int croppedAreaImageHeightPixels = rect.height();

        int croppedAreaLeftPixels = rect.left;
/* MODIFIED-BEGIN by jianying.zhang, 2016-11-18,BUG-2694254*/
//        int croppedAreaTopPixels = rect.top;
        int croppedAreaTopPixels = (fullPanoHeightPixels - croppedAreaImageHeightPixels) / 2;
        /* MODIFIED-END by jianying.zhang,BUG-2694254*/
        XMPMeta xmpMeta = XmpUtil.extractOrCreateXMPMeta(file_path);
        try {
            xmpMeta.setProperty(GOOGLE_PANO_NAMESPACE,
                    CROPPED_AREA_FULL_PANO_WIDTH_PIXELS, fullPanoWidthPixels);
            xmpMeta.setProperty(GOOGLE_PANO_NAMESPACE,
                    CROPPED_AREA_FULL_PANO_HEIGHT_PIXELS, fullPanoHeightPixels);
            xmpMeta.setProperty(GOOGLE_PANO_NAMESPACE,
                    CROPPED_AREA_IMAGE_WIDTH_PIXELS, croppedAreaImageWidthPixels);
            xmpMeta.setProperty(GOOGLE_PANO_NAMESPACE,
                    CROPPED_AREA_IMAGE_HEIGHT_PIXELS, croppedAreaImageHeightPixels);
            xmpMeta.setProperty(GOOGLE_PANO_NAMESPACE,
                    CROPPED_AREA_LEFT, croppedAreaLeftPixels);
            xmpMeta.setProperty(GOOGLE_PANO_NAMESPACE,
                    CROPPED_AREA_TOP, croppedAreaTopPixels);
            xmpMeta.setProperty(GOOGLE_PANO_NAMESPACE,
                    PROJECTION_TYPE, PROJECTION_TYPE_CYL_PANO);

            if (mTempFile != null) {
                try {
                    // Write the audio data to extended xmp.
                    // String base64String = encodeBase64File(mTempFile.getPath());
                    // xmpMeta.setPropertyBase64(GOOGLE_AUDIO_NAMESPACE,
                    //         AUDIO_DATA, base64String.getBytes());
                    byte[] audioData = getAudioData(mTempFile);
                    if (audioData != null) {
                        XMPMeta extendedXmpMeta = XmpUtil.createXMPMeta();
                        extendedXmpMeta.setProperty(GOOGLE_AUDIO_NAMESPACE, AUDIO_DATA, audioData);
                        XmpUtil.writeXMPMeta(file_path, xmpMeta, extendedXmpMeta);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                deleteTempFile();
            }

            XmpUtil.writeXMPMeta(file_path, xmpMeta);
            Log.d(TAG, "XMPMeta: " + XMPMetaFactory.serializeToString(
                    xmpMeta, new SerializeOptions()));
        } catch (XMPException e) {
            e.printStackTrace();
        }
    }

    public static String encodeBase64File(String path) throws Exception {
        File file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int)file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return Base64.encodeToString(buffer,Base64.DEFAULT);
    }

    public static void decoderBase64File(String base64Code, String path) throws Exception {
        byte[] buffer =Base64.decode(base64Code, Base64.DEFAULT);
        FileOutputStream out = new FileOutputStream(path);
        out.write(buffer);
        out.close();
    }

    private byte[] getAudioData(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        int length = (int) file.length();
        byte[] data = new byte[length];
        FileInputStream is = null;
        try {
            is = new FileInputStream(file.getPath());
            is.read(data, 0, length);
        } catch (IOException e) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }
}
