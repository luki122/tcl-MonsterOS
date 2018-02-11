package com.android.camera;

import com.android.ex.camera2.portability.Size;

/**
 * Created by Sean Scott on 8/27/16.
 */
public interface TS_PanoramaController extends PhotoController {

    // Panorama state.
    int PANO_STATE_PREVIEW_STOPPED = 100;  //预览状态
    int PANO_STATE_IDLE = 101;  //初始化、关闭状态
    int PANO_STATE_RECORDING = 102; //拍摄过程状态
    int PANO_STATE_SAVING = 103; //saving状态
    int PANO_STATE_RECORDING_PENDING_STOP = 104; // shutter when recording and pending to stop.

    // Panorama UI mode.
    int PANO_MODE_CLEAR = 1000;
    int PANO_MODE_PREVIEW_LTR = 1001;
    int PANO_MODE_PREVIEW_RTL = 1002;
    int PANO_MODE_CAPTURE = 1003;
    int PANO_MODE_SELFIE_PREVIEW = 1004;
    int PANO_MODE_SELFIE_CAPTURE = 1005;

    boolean isRoundCornerPreview();

    boolean isSelfie();

    boolean is360Photo();

    boolean isDirectionLTR();
    void onDirectionInverted(boolean isLTR);

    Size getStillImageSize();

    void onAudioRecordOnOffSwitched();
}