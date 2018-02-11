package com.android.camera.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.permission.PermissionUtil;
import com.android.camera.permission.PermsInfo;
import com.android.camera.ui.PreferenceLayout;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.tct.camera.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by mec on 8/22/16.
 */
public class CameraSettingsFragment extends PreferenceFragment implements SwitchPreferenceSingle.onSwitchChangeListener, PreferenceLayout.onCloseClickListener {

    private static final Log.Tag TAG = new Log.Tag("SettingsFragment");
    private static final String COUNTDOWN_FIRST_ENTRY = "0";
    private AppController mController;
    private int mCameraId;
    private static boolean mSecureCamera = false;
    private boolean mFacingBack;
    private String mModuleScope;
    private SettingsManager mSettingsManager;

    private boolean mShowShutterSound = true;
    private boolean mShowAutoHDR = true;
    private boolean mShowGridLines = true;
    private boolean mShowFaceBeauty = true;
    private boolean mShowMirrorSelfie = true;
    private boolean mShowPose = true;

    private boolean mLocationEnable = false;

    private String[] mTimerValues;
    private String[] mTimerEntries;

    private ListView mListView;
    private boolean mHavePrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.camera_preference_settings);

        SwitchPreferenceSingle locationPreference = (SwitchPreferenceSingle) getPreferenceScreen().findPreference(Keys.KEY_RECORD_LOCATION);
        locationPreference.setSwitchChangeListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PreferenceLayout preferenceLayout = (PreferenceLayout) inflater.inflate(R.layout.preference_list_fragment, container, false);
        preferenceLayout.setOnCloseClickListener(this);
        mListView = (ListView) preferenceLayout.findViewById(R.id.preference_list);
        return preferenceLayout;
    }

    public void init(AppController controller) {

        mController = controller;
        mSecureCamera = mController.isSecureCamera();
        mModuleScope = mController.getModuleScope();
        mCameraId = mController.getCurrentCameraId();

        mSettingsManager = mController.getSettingsManager();
        mFacingBack = mController.getCameraProvider().getCharacteristics(mCameraId).isFacingBack();

        mLocationEnable = CameraUtil.isLocationEnable(mController.getAndroidContext());
    }

    /**
     * Sets the root of the preference hierarchy that this fragment is showing.
     * <p/>
     * because i use custom preference_list {@link ListView}, replace {@link android.R.id.list}
     * addPreferencesFromResource will call setPreferenceScreen ,make mHavePrefs true.
     * so will throw list can't finding exception in {@link PreferenceFragment#onActivityCreated(Bundle)}
     * However i can't override {@link PreferenceFragment#setPreferenceScreen(PreferenceScreen)}
     * so deal with using reflection
     *
     * @param preferenceScreen The root {@link PreferenceScreen} of the preference hierarchy.
     */
    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (preferenceScreen == null)
            return;
        Method setMethod = null;
        try {
            Class<?> cls = Class.forName("android.preference.PreferenceManager");
            setMethod = cls.getDeclaredMethod("setPreferences", PreferenceScreen.class);
            setMethod.setAccessible(true);
            mHavePrefs = (boolean) setMethod.invoke(getPreferenceManager(), preferenceScreen);
        } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

        setCameraPreference();

        if (mHavePrefs) {
            getPreferenceScreen().bind(mListView);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setCameraPreference() {
        // Load the list entries.
        loadListEntries();

        // Make sure to disable settings for cameras that don't exist on this
        // device.
        setVisibilities();

        setPreferenceEnable();

        fillEntriesAndSummaries();
    }

    private void loadListEntries() {
        mTimerValues = mSettingsManager.getStringPossibleValues(Keys.KEY_COUNTDOWN_DURATION);
        mTimerEntries = new String[mTimerValues.length];
        for (int i = 0; i < mTimerValues.length; i++) {
            if (i == 0 && mTimerValues[i].equals(COUNTDOWN_FIRST_ENTRY)) {
                mTimerEntries[i] = getActivity().getResources().getString(R.string.pref_camera_timer_entry_0);
            } else {
                mTimerEntries[i] = mTimerValues[i] + getActivity().getResources().getString(R.string.pref_camera_timer_unit_suffix);
            }
            Log.d(TAG, "loadListEntries mTimerEntries[i] " + mTimerEntries[i] + " mTimerValues[i] " + mTimerValues[i]);
        }
    }

    /**
     * Depending on camera availability on the device, this removes settings
     * for cameras the device doesn't have.
     */
    private void setVisibilities() {
        mShowShutterSound = !SettingsUtil.isCameraSoundForced();
        if (!mShowShutterSound) {
            getPreferenceScreen().removePreference(findPreference(Keys.KEY_SOUND));
        }

        mShowAutoHDR = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_AUTO_HDR, false);
        if (!mShowAutoHDR) {
            getPreferenceScreen().removePreference(findPreference(Keys.KEY_CAMERA_HDR));
        }

        mShowGridLines = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_GRID_LINE, false);
        if (!mShowGridLines) {
            getPreferenceScreen().removePreference(findPreference(Keys.KEY_CAMERA_GRID_LINES));
        }

        mShowFaceBeauty = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_PHOTO_FACEBEAUTY_SUPPORT, false);
        if (!mShowFaceBeauty) {
            getPreferenceScreen().removePreference(findPreference(Keys.KEY_CAMERA_FACEBEAUTY));
        }

        mShowMirrorSelfie = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_MIRROR_SELFIE, false);
        if (!mShowMirrorSelfie) {
            getPreferenceScreen().removePreference(findPreference(Keys.KEY_CAMERA_MIRROR_SELFIE));
        }

        mShowPose = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_POSE, false);
        if (!mShowPose) {
            getPreferenceScreen().removePreference(findPreference(Keys.KEY_CAMERA_POSE));
        }
    }

    private void setPreferenceEnable() {
        if (!mShowShutterSound) {
            // When sound forced, the Shutter sound ManagedSwitchPreference will not be shown,
            // so set the settings only.
            if (!mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL, Keys.KEY_SOUND)) {
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL, Keys.KEY_SOUND, true);
            }
        }

        if (!mLocationEnable) {
            setRememberLocation(false);
        }

    }

    private void fillEntriesAndSummaries() {
        Preference pref = getPreferenceScreen().findPreference(Keys.KEY_COUNTDOWN_DURATION);
        if (pref == null) {
            return;
        }
        ListPreference listPreference = (ListPreference) pref;
        setEntriesForSelection(mTimerValues, mTimerEntries, listPreference);
    }

    private void setEntriesForSelection(String[] qualities,
                                        String[] titles, ListPreference preference) {
        preference.setEntries(titles);
        preference.setEntryValues(qualities);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListView = null;
    }

    @Override
    public void onSwitchChanged(SwitchPreferenceSingle preference) {
        switch (preference.getKey()) {
            case Keys.KEY_RECORD_LOCATION:
                boolean locationPermission = PermissionUtil.isPermissionGranted(getActivity(), PermsInfo.PERMS_ACCESS_COARSE_LOCATION)
                        && PermissionUtil.isPermissionGranted(getActivity(), PermsInfo.PERMS_ACCESS_FINE_LOCATION);
                if (!locationPermission) {
                    if (!mSecureCamera) {
                    } else {
//                        PermissionUtil.showSnackBar(getActivity(), getView(),
//                                R.string.location_snack_setting,
//                                R.string.grant_access_settings);
                    }
                    setRememberLocation(false);
                } else {
                    onGpsSaving();
                }
            default:
                break;
        }
    }

    private void setRememberLocation(boolean remember) {
        SwitchPreferenceSingle sps = (SwitchPreferenceSingle)
                getPreferenceScreen().findPreference(Keys.KEY_RECORD_LOCATION);
        sps.setChecked(remember);
        if (mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL, Keys.KEY_RECORD_LOCATION) != remember) {
            mSettingsManager.set(SettingsManager.SCOPE_GLOBAL, Keys.KEY_RECORD_LOCATION, remember);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void onGpsSaving() {
        boolean isGpsTaggingOn = CameraUtil.gotoGpsSetting(getActivity(), mSettingsManager, R.drawable.gps_white);
        if (isGpsTaggingOn) {
            setRememberLocation(false);
        }
    }

    public boolean onBackPressed() {
        return mController.getCameraAppUI().dismissCameraSettingFragment();
    }

    @Override
    public void onCloseClick() {
        mController.getCameraAppUI().dismissCameraSettingFragment();
    }
}