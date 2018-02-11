package com.android.camera.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.android.camera.MultiToggleImageButton;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.tct.camera.R;

/**
 * Created by Sean Scott on 10/12/16.
 */
public class AudioRecordButton extends MultiToggleImageButton {

    private static final Log.Tag TAG = new Log.Tag("AudioRecordButton");

    public interface ButtonStateListener {
        void onAudioRecordOnOffSwitched();
    }

    public AudioRecordButton(Context context) {
        super(context);
    }

    public AudioRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioRecordButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initializeButton(final SettingsManager settingsManager,
                                 final ButtonStateListener listener) {
        overrideImageIds(R.array.audio_record_icons);
        overrideContentDescriptions(R.array.audio_record_descriptions);

        int index = settingsManager.getIndexOfCurrentValue(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_CAMERA_PHOTO_AUDIO_RECORD);
        setState(index >= 0 ? index : 0, false, false);

        setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                settingsManager.setValueByIndex(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_CAMERA_PHOTO_AUDIO_RECORD, state);
                if (listener != null) {
                    listener.onAudioRecordOnOffSwitched();
                }
            }
        });
    }
}