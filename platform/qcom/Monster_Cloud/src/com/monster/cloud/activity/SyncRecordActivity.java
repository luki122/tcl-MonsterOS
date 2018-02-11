package com.monster.cloud.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.cloud.R;
import com.monster.cloud.utils.LoginUtil;
import com.monster.cloud.utils.SyncTimeUtil;
import com.tencent.qqpim.sdk.accesslayer.StatisticsFactory;
import com.tencent.qqpim.sdk.accesslayer.def.ISyncDef;
import com.tencent.qqpim.sdk.accesslayer.def.PMessage;
import com.tencent.qqpim.sdk.accesslayer.interfaces.basic.ISyncProcessorObsv;
import com.tencent.qqpim.sdk.accesslayer.interfaces.statistics.IStatisticsUtil;
import com.tencent.qqpim.sdk.defines.DataSyncResult;
import com.tencent.qqpim.sdk.defines.ISyncMsgDef;
import com.tencent.tclsdk.sync.SyncCallLog;
import com.tencent.tclsdk.utils.GetCountUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import QQMPS.P;
import mst.app.MstActivity;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;

/**
 * Created by yubai on 16-10-31.
 */
public class SyncRecordActivity extends MstActivity implements ISyncProcessorObsv {

    private static final int TYPE_BACKUP = 0;
    private static final int TYPE_RECOVERY = 1;

    private mst.widget.toolbar.Toolbar toolbar;
    private Switch autoSwitch;
    private RelativeLayout backUpBtn, recoveryBtn;

    private ProgressDialog progressDialog;
    private TextView lastSyncTime;
    private TextView localCount, cloudCount;

    // update progress
    private int progress;
    // recovery or backup
    private int syncType;

    private int cloudCalllogNum = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.sync_sms);
        toolbar = getToolbar();
        toolbar.setBackgroundColor(Color.parseColor("#05000000"));
        toolbar.setTitle(R.string.call_log);

        autoSwitch = (Switch) findViewById(R.id.auto_sync_switch);
        if (SyncTimeUtil.getRecordSyncLabel(this)) {
            //auto synchronize on
            autoSwitch.setChecked(true);
        } else {
            autoSwitch.setChecked(false);
        }
        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (autoSwitch.isChecked()) {
                    //TODO off
                    SyncTimeUtil.setRecordSyncLabel(SyncRecordActivity.this, true);
                } else {
                    // TODO on
                    SyncTimeUtil.setRecordSyncLabel(SyncRecordActivity.this, false);
                }
            }
        });


        backUpBtn = (RelativeLayout) findViewById(R.id.sync_to_cloud_layout);
        backUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginUtil.getLoginLabel(SyncRecordActivity.this)) {
                    syncType = TYPE_BACKUP;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new SyncCallLog(SyncRecordActivity.this, SyncRecordActivity.this).sync();
                        }
                    }).start();
                } else {
                    //TODO Login first
                }
            }
        });

        recoveryBtn = (RelativeLayout) findViewById(R.id.download_to_local_layout);
        recoveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginUtil.getLoginLabel(SyncRecordActivity.this)) {
                    syncType = TYPE_RECOVERY;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new SyncCallLog(SyncRecordActivity.this, SyncRecordActivity.this).backup();
                        }
                    }).start();
                }
            }
        });

        lastSyncTime = (TextView) findViewById(R.id.last_sync_time);
        if(SyncTimeUtil.getRecordSyncTime(this) > 0){
            lastSyncTime.setText(SyncTimeUtil.setTime(SyncTimeUtil.getRecordSyncTime(this), this));
        } else {
            lastSyncTime.setText(R.string.never_backup);
        }

        localCount = (TextView) findViewById(R.id.local_count);
        cloudCount = (TextView) findViewById(R.id.cloud_count);
        getRecordCount();
    }


    @Override
    public void onSyncStateChanged(PMessage pMessage) {
        Message msg = syncHandler.obtainMessage();
        msg.what = 0;
        msg.obj = pMessage;
        syncHandler.sendMessage(msg);
    }

    @Override
    public void onLoginkeyExpired() {
        // DO NOTHING
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }

    private Handler syncHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    uiProgressChanged((PMessage) msg.obj);
                    break;
                case 1:
                    cloudCount.setText(cloudCalllogNum + "");
                    break;
                default:
                    break;
            }
        }
    };

    private void uiProgressChanged(PMessage msg) {
        switch (msg.msgId) {
            case ISyncMsgDef.ESTATE_SYNC_ALL_BEGIN:
                //同步开始（全部任务）
                initProgressDialog();
                break;
            case ISyncMsgDef.ESTATE_SYNC_SCAN_BEGIN:
                //数据库扫描开始
                break;
            case ISyncMsgDef.ESTATE_SYNC_SCAN_FINISHED:
                //数据库扫描结束
                break;
            case ISyncMsgDef.ESTATE_SYNC_PROGRESS_CHANGED:
                //同步进度变化
                progress = msg.arg1;
                if(progressDialog != null){
                    progressDialog.setProgress(progress);
                }
                break;
            case ISyncMsgDef.ESTATE_SYNC_DATA_REARRANGEMENT_BEGIN:
                //数据同步完成，数据整理开始
                break;
            case ISyncMsgDef.ESTATE_SYNC_DATA_REARRANGEMENT_FINISHED:
                //数据同步完成，数据整理完成
                break;
            case ISyncMsgDef.ESTATE_SYNC_ALL_FINISHED:
                //同步结束（全部）
                syncAllfinished(msg);
                break;
            default:
                break;
        }
    }

    private void syncAllfinished(PMessage msg) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        List<DataSyncResult> resultList = null;

        if (null != msg.obj1) {
            resultList = (List<DataSyncResult>) msg.obj1;
        }

        if (null == resultList) {
            return;
        }

        DataSyncResult result = null;

        for (int i = 0; i < resultList.size(); ++i) {
            result = resultList.get(i);
            if (null == result) {
                return;
            }

            switch (result.getResult()) {
                case ISyncDef.SYNC_ERR_TYPE_SUCCEED:
                    initFinishedDialog();
                    getRecordCount();
                    progressDialog.dismiss();
                    break;
                case ISyncDef.SYNC_ERR_TYPE_RELOGIN:
                    //需要重新登录
                    break;
                case ISyncDef.SYNC_ERR_TYPE_CLIENT_ERR:
                    //客户端错误
                    break;
                case ISyncDef.SYNC_ERR_TYPE_SERVER_ERR:
                    //网络错误
                    break;
                case ISyncDef.SYNC_ERR_TYPE_USER_CANCEL:
                    //用户取消
                    break;
                case ISyncDef.SYNC_ERR_TYPE_FAIL_CONFLICT:
                    //由于其他软件的同步模块正在使用导致的错误
                    break;
                case ISyncDef.SYNC_ERR_TYPE_TIME_OUT:
                    //网络超时错误
                    break;
                default:
                    break;
            }
        }
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if (syncType == TYPE_BACKUP) {
            progressDialog.setTitle(getString(R.string.call_log_backup));
        } else if (syncType == TYPE_RECOVERY) {
            progressDialog.setTitle(getString(R.string.call_log_recovery));
        }
        progressDialog.setMessage(getString(R.string.sync_ing));
        progressDialog.setMax(100);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });
        progressDialog.show();
    }

    private void initFinishedDialog() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String time = dateFormat.format(now);
        String syncTime = getString(R.string.sms_sync_time);

        if (syncType == TYPE_BACKUP) {
            SyncTimeUtil.updateRecordSyncTime(this, System.currentTimeMillis());
        } else if (syncType == TYPE_RECOVERY) {
            syncTime = getString(R.string.sms_restore_time);
        }

        new AlertDialog.Builder(this)
                .setTitle(syncType == TYPE_BACKUP ? R.string.call_log_backup : R.string.call_log_recovery)
                .setPositiveButton(com.mst.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        lastSyncTime
                                .setText(SyncTimeUtil.setTime(SyncTimeUtil.getRecordSyncTime(SyncRecordActivity.this), SyncRecordActivity.this));
                    }
                }).setMessage(String.format(syncTime, time)).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        lastSyncTime
                                .setText(SyncTimeUtil.setTime(SyncTimeUtil.getRecordSyncTime(SyncRecordActivity.this), SyncRecordActivity.this));
                    }
                }).show();
    }

    public void getRecordCount(){
        IStatisticsUtil mIStatisticsUtil = StatisticsFactory.getStatisticsUtil();
        int localRecordNum = mIStatisticsUtil.getLocalCalllogNum(this);
        localCount.setText("" + localRecordNum);

        new Thread(new Runnable() {
            @Override
            public void run() {
                cloudCalllogNum = GetCountUtils.getRecordNumOfCalllog();
                syncHandler.sendEmptyMessage(1);
            }
        }).start();


    }
}
