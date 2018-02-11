package cn.tcl.music.view;

import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import cn.tcl.music.R;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.PreferenceUtil;

public class DataSaverDialog {
    public static final String TAG = DataSaverDialog.class.getSimpleName();
    private static Context mContext;
    private static DataSaverDialog instance = null;
    private static AlertDialog mAlertDialog;

    private DataSaverDialog(Context context) {
        mContext = context;
    }

    public static DataSaverDialog getInstance(Context context) {

        instance = new DataSaverDialog(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_data_saver, null);
        CheckBox checkToNeverShow = (CheckBox) dialogView.findViewById(R.id.checkbox_show_again);
        checkToNeverShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    PreferenceUtil.saveValue(mContext, PreferenceUtil.NODE_DATA_SAVER_CHOICE, PreferenceUtil.KEY_DATA_SAVER_CHOICE,
                            CommonConstants.NEVER_SHOW_AGAIN);
                } else {
                    PreferenceUtil.saveValue(mContext, PreferenceUtil.NODE_DATA_SAVER_CHOICE, PreferenceUtil.KEY_DATA_SAVER_CHOICE,
                            CommonConstants.SHOW_AGAIN);
                }
            }
        });
        builder.setTitle(R.string.warning_dialog_title);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.setCanceledOnTouchOutside(false);

        return instance;
    }

    public boolean isChooseNeverShowAgain() {
        return PreferenceUtil.getValue(mContext, PreferenceUtil.NODE_DATA_SAVER_CHOICE, PreferenceUtil
                .KEY_DATA_SAVER_CHOICE, CommonConstants.SHOW_AGAIN) == CommonConstants.NEVER_SHOW_AGAIN;
    }


    public boolean showWrapper() {
        if (!isChooseNeverShowAgain()) {
            mAlertDialog.show();
            return true;
        } else {
            LogUtil.e(TAG, "Conditions is not met,Dialog won't show");
            return false;
        }
    }

}
