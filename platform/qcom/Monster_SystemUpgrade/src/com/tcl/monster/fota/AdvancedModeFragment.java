
package com.tcl.monster.fota;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import mst.preference.Preference;
import mst.preference.PreferenceFragment;
import mst.preference.PreferenceScreen;
import android.telephony.TelephonyManager;

import com.tcl.monster.fota.service.LogUploadService;
import com.tcl.monster.fota.utils.FotaUtil;


public class AdvancedModeFragment extends PreferenceFragment {

    private static final String NAME_PREFERENCE_ADVANCED_MODE = "advancedmode";
    SharedPreferences mPrefs;

    Preference mInformation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getActivity().getSharedPreferences(NAME_PREFERENCE_ADVANCED_MODE,
                Context.MODE_PRIVATE);

        addPreferencesFromResource(R.xml.advancedmode_setting);
        mInformation = findPreference("key_information");
        mInformation.setSummary(buildInformation());

    }

    private String buildInformation() {
        StringBuilder sb = new StringBuilder();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        float stroke = getResources().getDimension(R.dimen.stroke_width);
        int index = getResources().getInteger(R.integer.rect_index_fix);
        float density = getResources().getDisplayMetrics().density;
        int dpi = getResources().getDisplayMetrics().densityDpi;

        sb.append("cu :").append(REF()).append("\n");
        sb.append("current version :").append(VERSION()).append("\n");
        sb.append("imei :").append(IMEI(getActivity())).append("\n");
        sb.append("device rooted :").append(FotaUtil.isDeviceRooted()).append("\n");
        sb.append("update file path:").append(FotaUtil.updateZip().getAbsolutePath()).append("\n");
        sb.append("width*height:").append(width).append("*").append(height)
        .append("\n");
        sb.append("density:").append(density).append(" , dpi:").append(dpi).append("\n");
        sb.append("stroke:").append(stroke).append(" , ");
        sb.append("rectIndex:").append(index).append("\n");
        return sb.toString();
    }

    public static String REF() {
        return FotaUtil.REF();
    }

    private String VERSION() {
        return FotaUtil.VERSION();
    }

    private String IMEI(Context context) {
        TelephonyManager telephonyManager = ((TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE));
        String imei = telephonyManager.getDeviceId();
        return imei;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference.getKey().equals("test_crash")) {
            String str = "";
            str.charAt(3);
        }

        if (preference.getKey().equals("test_uploadlog")) {
            Intent i = new Intent(getActivity(), LogUploadService.class);
            i.setAction(LogUploadService.ACTION_UPLOAD_LOG);
            getActivity().startService(i);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}