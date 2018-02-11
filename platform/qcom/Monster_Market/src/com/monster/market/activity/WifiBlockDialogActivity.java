package com.monster.market.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.monster.market.R;
import com.monster.market.download.AppDownloadService;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.SettingUtil;

import mst.app.dialog.AlertDialog;

/**
 * Created by xiaobin on 16-10-10.
 */
public class WifiBlockDialogActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i(TAG, "WifiBlockDialogActivity onCreate()");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_prompt);
        builder.setMessage(R.string.dialog_wifi_disconnect_download_tip);
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent networkChange = new Intent(WifiBlockDialogActivity.this, AppDownloadService.class);
                networkChange.putExtra(AppDownloadService.DOWNLOAD_OPERATION,
                        AppDownloadService.OPERATION_NETWORK_MOBILE_CONTINUE);
                startService(networkChange);

                SettingUtil.setWifiBlockAlertOperation(WifiBlockDialogActivity.this, true);
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        SettingUtil.setWifiBlockAlert(this, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG, "WifiBlockDialogActivity onDestroy()");
    }

    @Override
    public void initViews() {

    }

    @Override
    public void initData() {

    }


}
