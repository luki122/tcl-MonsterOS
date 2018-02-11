package com.android.camera.settings;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.app.CameraApp;
import com.android.camera.debug.Log;
import com.android.camera.ui.SelectTextViewLayout;
import com.tct.camera.R;

/**
 * Created by mec on 8/19/16.
 */
public class SelectPreference extends ListPreference implements SelectTextViewLayout.onTextViewChangeListener {

    private static final Log.Tag TAG = new Log.Tag("SelectPreference");
    private CameraActivity mActivity;
    private SettingsManager mSettingsManager;

    public SelectPreference(Context context) {
        this(context, null);
    }

    public SelectPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        if (context instanceof CameraActivity)
            mActivity = (CameraActivity) context;
    }

    public SelectPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView selectPreferenceTitle = (TextView) view.findViewById(R.id.select_preference_title);
        SelectTextViewLayout selectorLayout = (SelectTextViewLayout) view.findViewById(R.id.select_preference);
        selectPreferenceTitle.setText(getTitle());
        selectorLayout.setIndex(findIndexOfValue(getPersistedString(getValue())));
        selectorLayout.setEntries(getEntries());
        selectorLayout.setTextViewChangeListener(this);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        return LayoutInflater.from(getContext()).inflate(R.layout.select_pference,
                parent, false);
    }

    private CameraApp getCameraApp() {
        Context context = getContext();
        if (context instanceof Activity) {
            Application application = ((Activity) context).getApplication();
            if (application instanceof CameraApp) {
                return (CameraApp) application;
            }
        }
        return null;
    }

    @Override
    public void onSelectTextViewChanged(int index) {
        setValueIndex(index);
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        mActivity = null;
    }

    @Override
    protected boolean persistString(String value) {
        CameraApp cameraApp = getCameraApp();
        if (cameraApp == null) {
            return false;
        }
        mSettingsManager = cameraApp.getSettingsManager();
        if (mSettingsManager != null) {
            mSettingsManager.set(mActivity.getCameraScope(), getKey(), value);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        CameraApp cameraApp = getCameraApp();
        if (cameraApp == null) {
            return defaultReturnValue;
        }
        mSettingsManager = cameraApp.getSettingsManager();
        if (mSettingsManager != null) {
            return mSettingsManager.getString(mActivity.getCameraScope(), getKey());
        } else {
            return defaultReturnValue;
        }
    }
}