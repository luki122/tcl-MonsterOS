package com.android.camera;

import android.view.View;

import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.ui.ModuleLayoutWrapper;
import com.android.camera.ui.Rotatable;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.widget.AudioRecordButton;
import com.android.external.plantform.ExtBuild;
import com.tct.camera.R;

/**
 * Created by Sean Scott on 10/12/16.
 */
public class CylPanoramaUI extends TS_PanoramaGPUI
        implements AudioRecordButton.ButtonStateListener {

    private static final Log.Tag TAG = new Log.Tag("CylPanoUI");

    private boolean mAudioRecordSupported;
    private AudioRecordButton mAudioRecordButton;

    private final CameraActivity mActivity;
    private final TS_PanoramaController mController;
    private final View mRootView;

    public CylPanoramaUI(CameraActivity activity, TS_PanoramaController controller, View parent) {
        super(activity, controller, parent);
        mActivity = activity;
        mController = controller;
        mRootView = parent;

        mAudioRecordSupported = CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_SUPPORT_PHOTO_AUDIO_RECORD, true);
        if (mAudioRecordSupported) {
            initAudioRecordButton();
        }
    }

    private void initAudioRecordButton() {
        ModuleLayoutWrapper moduleRoot =
                (ModuleLayoutWrapper) mRootView.findViewById(R.id.module_layout);
        mActivity.getLayoutInflater().inflate(R.layout.audio_record_button, moduleRoot, true);
        mAudioRecordButton = (AudioRecordButton) mRootView.findViewById(R.id.audio_record_button);
        mAudioRecordButton.initializeButton(mActivity.getSettingsManager(), this);
        mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mAudioRecordButton, true));
    }

    @Override
    public void onAllViewRemoved(AppController controller) {
        super.onAllViewRemoved(controller);
        if (mAudioRecordSupported && mAudioRecordButton != null) {
            controller.removeRotatableFromListenerPool(mAudioRecordButton.hashCode());
        }
    }

    @Override
    public void onAudioRecordOnOffSwitched() {
        mController.onAudioRecordOnOffSwitched();
    }

    @Override
    public void transitionToMode(int mode) {
        super.transitionToMode(mode);

        if (mAudioRecordButton == null) {
            return;
        }

        if (mode == TS_PanoramaController.PANO_MODE_CAPTURE) {
            mAudioRecordButton.setVisibility(View.INVISIBLE);
        } else {
            mAudioRecordButton.setVisibility(View.VISIBLE);
        }
    }
}