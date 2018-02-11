package cn.tcl.music.view;

import android.content.Context;
import android.content.DialogInterface;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.util.Connectivity;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.PreferenceUtil;
import mst.app.dialog.AlertDialog;

public class MobileNetworkDialog {
    private static final String TAG = MobileNetworkDialog.class.getSimpleName();
    private static Context mContext;
    private static MobileNetworkDialog mInstance;
    private static AlertDialog mAlertDialog;

    private MobileNetworkDialog(Context context) {
        mContext = context;
    }

    public static MobileNetworkDialog getInstance(Context context, boolean isPlay) {
        mInstance = new MobileNetworkDialog(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        builder.setTitle(R.string.network_warning_dialog);
        if (isPlay) {
            builder.setMessage(context.getString(R.string.network_play_warning_message));
        } else {
            builder.setMessage(context.getString(R.string.network_download_warning_message));
        }
        builder.setPositiveButton(R.string.confirmation, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PreferenceUtil.saveValue(mContext, PreferenceUtil.NODE_NETWORK_SWITCH,
                        PreferenceUtil.KEY_NETWORK_SWITCH, CommonConstants.OPEN);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancle_select, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.setCanceledOnTouchOutside(false);
        return mInstance;
    }

    private boolean isMobileNetWorkOpen() {
        return CommonConstants.OPEN == PreferenceUtil.getValue(mContext, PreferenceUtil.NODE_NETWORK_SWITCH,
                PreferenceUtil.KEY_NETWORK_SWITCH, CommonConstants.NO_OPEN);
    }

    private boolean shouldShow() {
        if (Connectivity.isConnectedMobile(mContext) && !isMobileNetWorkOpen()) {
            return true;
        }
        return false;
    }

    public boolean showWrapper() {
        if (shouldShow()) {
            mAlertDialog.show();
            return true;
        } else {
            LogUtil.e(TAG, "Conditions is not met,Dialog won't show");
            return false;
        }
    }
}
