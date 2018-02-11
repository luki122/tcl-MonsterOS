package com.android.camera;

import com.android.camera.app.AppController;
import com.tct.camera.R;

/**
 * Created by sichao.hu on 9/22/15.
 */
public class VideoCaptureIntentModule extends  VideoModule {

    private static final String VIDEO_CAPTURE_MODULE_STRING_ID = "VideoCaptureModule";

    /**
     * Construct a new video module.
     *
     * @param app
     */
    public VideoCaptureIntentModule(AppController app) {
        super(app);
    }

    @Override
    protected boolean isNeedStartRecordingOnSwitching() {
        return false;
    }

    @Override
    public int getModuleId() {
        return mAppController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_video_capture);
    }

    @Override
    public String getModuleStringIdentifier() {
        return VIDEO_CAPTURE_MODULE_STRING_ID;
    }
}
