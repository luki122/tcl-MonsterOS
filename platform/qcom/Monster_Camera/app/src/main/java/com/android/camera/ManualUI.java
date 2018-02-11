package com.android.camera;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.android.camera.app.AppController;
import com.android.camera.app.CameraAppUI;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.ui.ManualGroup;
import com.android.camera.ui.ManualGroupWrapper;
import com.android.camera.ui.ManualItem;
import com.android.camera.ui.ModuleLayoutWrapper;
import com.android.camera.ui.Rotatable;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomUtil;
import com.android.camera.widget.FloatingActionsMenu;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.CameraSettings;
import com.android.ex.camera2.portability.Size;
import com.android.external.plantform.ExtBuild; // MODIFIED by peixin, 2016-04-26,BUG-1999452
import com.tct.camera.R;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManualUI extends PhotoUI implements ManualItem.ManualStateChangeListener,FloatingActionsMenu.ManualMenuExpandChangeListener
        ,HelpTipsManager.NextButtonListener{
    private static final String TAG = "ManualUI";
    public static final String SETTING_AUTO = "auto";
    public static final String SETTING_PROGRESS = "progress";
    public static final String SETTING_INDEX = "index";
    private ManualItem mItemISO;
    private ManualItem mItemS;
    private ManualItem mItemWb;
    private ManualItem mItemF;
    private FloatingActionsMenu mManualMenu;

    private int mMinFocusPos = 0;
    private int mMaxFocusPos = 0;
    private String mCurFocusState;

    private ArrayList<String> mExposureTimeDouble;
    private ArrayList<String> mExposureTimeTitles;
    private String mCurExposureTimeState;

    private ArrayList<Integer> mISOValues;
    private String mCurIsoState;

    private ArrayList<String> mWBValues;
    private ArrayList<String> mWBTitles;
    private String mCurWBState;

    private ManualGroup mManualGroup;
    private CameraAppUI mCameraAppUI;
    private HelpTipsManager mHelpTipsManager;
    private  boolean mIsFirstUseManual = false;

    public ManualUI(CameraActivity activity, PhotoController controller, View parent,
                    ManualModeCallBackListener l) {
        super(activity, controller, parent);
        ModuleLayoutWrapper moduleRoot = (ModuleLayoutWrapper) parent.findViewById(R.id.module_layout);
        mActivity.getLayoutInflater().inflate(R.layout.manual_items_layout,
                moduleRoot, true);
        mManualModeCallBackListener = l;
        mManualGroup = (ManualGroup) parent.findViewById(R.id.manual_items_layout);
        ManualGroupWrapper wrapper = (ManualGroupWrapper) parent.findViewById(R.id.manual_group_wrapper);
        mCameraAppUI = activity.getCameraAppUI();
        mManualGroup.setCaptureLayoutHelper(mCameraAppUI.getCaptureLayoutHelper());
        wrapper.setCaptureLayoutHelper(mCameraAppUI.getCaptureLayoutHelper());
        mManualGroup.setVisibility(View.GONE);
        mItemISO = (ManualItem) parent.findViewById(R.id.item_iso);
        mItemS = (ManualItem) parent.findViewById(R.id.item_s);
        mItemWb = (ManualItem) parent.findViewById(R.id.item_wb);
        mItemF = (ManualItem) parent.findViewById(R.id.item_f);
        mManualMenu = (FloatingActionsMenu) parent.findViewById(R.id.multiple_actions);

        boolean bFrontCameraFacing = (Keys.isCameraBackFacing(mActivity.getSettingsManager(), SettingsManager.SCOPE_GLOBAL));
        String pictureSizeKey = bFrontCameraFacing ? Keys.KEY_PICTURE_SIZE_FRONT
                : Keys.KEY_PICTURE_SIZE_BACK;

        String defaultPicSize = SettingsUtil.getDefaultPictureSize(bFrontCameraFacing);
        String pictureSize = mActivity.getSettingsManager().getString(SettingsManager.SCOPE_GLOBAL,
                pictureSizeKey, defaultPicSize);

        Size size = SettingsUtil.sizeFromString(pictureSize);

        if(size != null && (size.width() == size.height())){
            Log.i(TAG, "Tony size is equal ratio= " + size.toString());
            int marginBottom = (int)mActivity.getResources().getDimension(R.dimen.menu_overlay_margin_horizontal);
            int marginStart = (int)mActivity.getResources().getDimension(R.dimen.multiple_actions_margin_start);
            int marginEnd = (int)mActivity.getResources().getDimension(R.dimen.multiple_actions_margin_end);
            int marginTop = (int)mActivity.getResources().getDimension(R.dimen.multiple_actions_margin_top);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mManualMenu.getLayoutParams();
            params.setMargins(marginStart, marginTop, marginEnd, marginBottom);
            mManualMenu.setLayoutParams(params);
        }

        mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mItemISO, true));
        mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mItemS, true));
        mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mItemWb, true));
        mActivity.addRotatableToListenerPool(new Rotatable.RotateEntity(mItemF, true));
        mActivity.addLockableToListenerPool(mManualMenu);
        activity.getCameraAppUI().addManualModeListener(mManualGroup);
    }

    @Override
    public void onAllViewRemoved(AppController controller) {
        super.onAllViewRemoved(controller);
        controller.removeRotatableFromListenerPool(mItemISO.hashCode());
        controller.removeRotatableFromListenerPool(mItemS.hashCode());
        controller.removeRotatableFromListenerPool(mItemWb.hashCode());
        controller.removeRotatableFromListenerPool(mItemF.hashCode());
        controller.removeLockableFromListenerPool(mManualMenu);
        mCameraAppUI.removeManualModeListener(mManualGroup);
    }

    public void collapseManualMenu() {
        mManualMenu.collapse();
    }
    @Override
    public void onCameraOpened(CameraCapabilities capabilities, CameraSettings settings) {
        super.onCameraOpened(capabilities, settings);
        mManualGroup.setVisibility(View.VISIBLE);
        initManualSettings(capabilities);
        initManualSubViews();
    }

    private void initManualSubViews() {
        mItemISO.initType(this, mISOValues, mCurIsoState);
        mItemS.initType(this, ManualItem.MANUAL_SETTING_EXPOSURE, mExposureTimeDouble, mExposureTimeTitles, mCurExposureTimeState);
        mItemWb.initType(this, ManualItem.MANUAL_SETTING_WHITE_BALANCE, mWBValues, mWBTitles, mCurWBState);
        mItemF.initType(this, mMinFocusPos, mMaxFocusPos, mCurFocusState);
    }

    public boolean isManualMode(String key) {
        SettingsManager settingsManager = mActivity.getSettingsManager();
        String curState = settingsManager.getString(mActivity.getModuleScope(), key);
        if (curState == null) {
            return false;
        }
        try {
            boolean auto = (boolean) CameraUtil.parseJSON(curState, SETTING_AUTO);
            return !auto;
        } catch (JSONException e) {
        }
        return false;
    }

    private void initManualSettings(CameraCapabilities capabilities) {
        CameraCapabilities.Stringifier stringifier = capabilities.getStringifier();

        int minIso = capabilities.getMinISO();
        int maxIso = capabilities.getMaxIso();
        SettingsManager settingsManager = mActivity.getSettingsManager();
        mCurIsoState = settingsManager.getString(mActivity.getModuleScope(), Keys.KEY_MANUAL_ISO_STATE);
        /* MODIFIED-BEGIN by peixin, 2016-04-26,BUG-1999452*/
        int[] isoValues;
        isoValues = mActivity.getResources().getIntArray(R.array.camera_iso_values);
        if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
            isoValues = mActivity.getResources().getIntArray(R.array.camera_iso_values_mtk);
        }
        /* MODIFIED-END by peixin,BUG-1999452*/
        mISOValues = new ArrayList<>();
        for (int i = 0; i < isoValues.length; i++) {
            if (isoValues[i] >= minIso && isoValues[i] <= maxIso) {
                mISOValues.add(isoValues[i]);
            }
        }

        String minExposureTime = capabilities.getMinExposureTime();
        String maxExposureTime = capabilities.getMaxExposureTime();
        String[] exposureTimeDouble = mActivity.getResources().getStringArray(R.array.exposure_time_double);
        String[] exposureTimeString = mActivity.getResources().getStringArray(R.array.exposure_time_string);

        /* MODIFIED-BEGIN by peixin, 2016-04-28,BUG-2003610*/
        String[] exposureTimeDouble_mtk = mActivity.getResources().getStringArray(R.array.exposure_time_double_mtk);
        String[] exposureTimeString_mtk = mActivity.getResources().getStringArray(R.array.exposure_time_string_mtk);

        mExposureTimeDouble = new ArrayList<>();
        mExposureTimeTitles = new ArrayList<>();
        if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
            for (int i = 0; i < exposureTimeDouble_mtk.length; i++) {
                if (Double.parseDouble(exposureTimeDouble_mtk[i]) <= Double.parseDouble(maxExposureTime) &&
                        Double.parseDouble(exposureTimeDouble_mtk[i]) >= Double.parseDouble(minExposureTime)) {
                    mExposureTimeDouble.add(exposureTimeDouble_mtk[i]);
                    mExposureTimeTitles.add(exposureTimeString_mtk[i]);
                }
            }
        } else {
            for (int i = 0; i < exposureTimeDouble.length; i++) {
                if (Double.parseDouble(exposureTimeDouble[i]) <= Double.parseDouble(maxExposureTime) &&
                        Double.parseDouble(exposureTimeDouble[i]) >= Double.parseDouble(minExposureTime)) {
                    mExposureTimeDouble.add(exposureTimeDouble[i]);
                    mExposureTimeTitles.add(exposureTimeString[i]);
                }
                /* MODIFIED-END by peixin,BUG-2003610*/
            }
        }


        mCurExposureTimeState = settingsManager.getString(mActivity.getModuleScope(), Keys.KEY_CUR_EXPOSURE_TIME_STATE);

        String[] wbValues = mActivity.getResources().getStringArray(R.array.white_balance_values);
        String[] wbStrings = mActivity.getResources().getStringArray(R.array.white_balance_strings);
        mWBValues = new ArrayList<>();
        mWBTitles = new ArrayList<>();
        for (int i = 0; i < wbValues.length; i++) {
            if (capabilities.supports(stringifier.whiteBalanceFromString(wbValues[i]))) {
                mWBValues.add(wbValues[i]);
                mWBTitles.add(wbStrings[i]);
            }
        }
        mCurWBState = settingsManager.getString(mActivity.getModuleScope(), Keys.KEY_CUR_WHITE_BALANCE_STATE);

        mMinFocusPos = capabilities.getMinFocusScale();
        mMaxFocusPos = capabilities.getMaxFocusScale();
        mCurFocusState = settingsManager.getString(mActivity.getModuleScope(), Keys.KEY_CUR_FOCUS_STATE);
    }

    public void onManualSettingChanged(int settingType, int progressValue, int index, boolean auto) {
        Log.d(TAG, "onManualSettingChanged progressValue: " + progressValue + ",  settingType" + settingType + ", " + index + ", " + auto);
        SettingsManager settingsManager = mActivity.getSettingsManager();
        if (mManualModeCallBackListener == null) {
            return;
        }
        Map<String,Object> curState = new HashMap<>();
        curState.put("auto",auto);
        curState.put("progress", progressValue);
        curState.put("index", index);
        String jsonString = CameraUtil.serializeToJson(curState);
        switch (settingType) {
            case ManualItem.MANUAL_SETTING_EXPOSURE:
                settingsManager.set(mActivity.getModuleScope(), Keys.KEY_CUR_EXPOSURE_TIME_STATE, jsonString);
                if(mExposureTimeDouble.size()==0){
                    break;
                }
                mManualModeCallBackListener.updateExposureTime(auto, mExposureTimeDouble.get(index));
                break;
            case ManualItem.MANUAL_SETTING_ISO:
                settingsManager.set(mActivity.getModuleScope(), Keys.KEY_MANUAL_ISO_STATE, jsonString);
                if(mISOValues.size()==0){
                    break;
                }
                mManualModeCallBackListener.updateISOValue(auto, mISOValues.get(index));
                break;
            case ManualItem.MANUAL_SETTING_FOCUS_POS:
                settingsManager.set(mActivity.getModuleScope(), Keys.KEY_CUR_FOCUS_STATE, jsonString);
                mManualModeCallBackListener.updateManualFocusValue(auto, index);
                break;
            case ManualItem.MANUAL_SETTING_WHITE_BALANCE:
                settingsManager.set(mActivity.getModuleScope(), Keys.KEY_CUR_WHITE_BALANCE_STATE, jsonString);
                mManualModeCallBackListener.updateWBValue(auto, mWBValues.get(index));
                break;
        }
    }

    public void onVisibilityChanged(int settingType) {
        switch (settingType) {
            case ManualItem.MANUAL_SETTING_EXPOSURE:
                mItemISO.resetView();
                mItemWb.resetView();
                mItemF.resetView();
                break;
            case ManualItem.MANUAL_SETTING_ISO:
                mItemS.resetView();
                mItemWb.resetView();
                mItemF.resetView();
                break;
            case ManualItem.MANUAL_SETTING_FOCUS_POS:
                mItemISO.resetView();
                mItemS.resetView();
                mItemWb.resetView();
                break;
            case ManualItem.MANUAL_SETTING_WHITE_BALANCE:
                mItemISO.resetView();
                mItemS.resetView();
                mItemF.resetView();
                break;
        }

        if(mIsFirstUseManual && mHelpTipsManager != null){
            mHelpTipsManager.notifyEventFinshed();
            mIsFirstUseManual = false;
            mManualMenu.mAddButton.setEnabled(true);
            mManualMenu.setMenuExpandChangeListener(null, false);
        }
    }

    public void initManualUIForTutorial() {
        mIsFirstUseManual = !mActivity.getSettingsManager().getBoolean(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_MANUAL_FINISHED,false);
        mManualMenu.setMenuExpandChangeListener(this, mIsFirstUseManual);
        mHelpTipsManager = mActivity.getHelpTipsManager();
        if(mHelpTipsManager != null){
            mHelpTipsManager.setManualUpdateUIListener(this);
            if(mIsFirstUseManual){
                mManualMenu.toggleForTutorial();
            }
        }
    }

    @Override
    public void onUpdateUIChangedFromTutorial() {
        mIsFirstUseManual = false;
        mManualMenu.setMenuExpandChangeListener(null, false);
    }

    public interface ManualModeCallBackListener {
        void updateISOValue(boolean auto, int isoValue);

        void updateManualFocusValue(boolean auto, int focusPos);

        void updateWBValue(boolean auto, String wbValue);

        void updateExposureTime(boolean auto, String ecValue);
    }

    private ManualModeCallBackListener mManualModeCallBackListener;

    @Override
    public void onManualMenuExpandChanged(boolean expand) {
        if(!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_HELP_TIP_TUTORIAL,false)){
            return;
        }

        if (mIsFirstUseManual) {
            if (mHelpTipsManager != null && expand) {
                mHelpTipsManager.createAndShowHelpTip(HelpTipsManager.MANUAL_GROUP, true);
            }
        }
    }

    @Override
    public void onManualMenuClick() {
        if(!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_HELP_TIP_TUTORIAL,false)){
            return;
        }
        if (mIsFirstUseManual) {
            mIsFirstUseManual = false;
            if (mHelpTipsManager != null && mHelpTipsManager.isHelpTipShowExist()) {
                mHelpTipsManager.notifyEventFinshed();
            }
        }
    }
}
