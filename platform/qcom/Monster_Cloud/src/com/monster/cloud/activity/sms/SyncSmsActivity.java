package com.monster.cloud.activity.sms;

import android.app.AppOpsManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
//import android.provider.Telephony;
import android.provider.Telephony;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.monster.cloud.R;
import com.monster.cloud.utils.SPUtil;
import com.monster.cloud.utils.SyncTimeUtil;
import com.tencent.qqpim.sdk.accesslayer.StatisticsFactory;
import com.tencent.qqpim.sdk.accesslayer.def.ISyncDef;
import com.tencent.qqpim.sdk.accesslayer.def.PMessage;
import com.tencent.qqpim.sdk.accesslayer.interfaces.basic.ISyncProcessorObsv;
import com.tencent.qqpim.sdk.accesslayer.interfaces.statistics.IStatisticsUtil;
import com.tencent.qqpim.sdk.defines.DataSyncResult;
import com.tencent.qqpim.sdk.defines.ISyncMsgDef;
import com.tencent.qqpim.sdk.object.sms.SmsTimeType;
import com.tencent.tclsdk.sync.SyncSMS;
import com.tencent.tclsdk.utils.GetCountUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import mst.app.MstActivity;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;

/**
 * Created by zouxu on 16-10-27.
 */
public class SyncSmsActivity extends MstActivity implements ISyncProcessorObsv {

    private TextView local_count;
    private TextView cloud_count;
    private TextView last_sync_time;
    private Switch auto_sync_switch;
    private RelativeLayout auto_sync_switch_layout;
    private RelativeLayout sync_to_cloud_layout;
    private RelativeLayout download_to_local_layout;

    private ProgressDialog mProgressDialog;
    private int type;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.sync_sms);
        getToolbar().setTitle(R.string.sms);
        initView();
        getSMSCount();
    }

    private void initView() {
        local_count = (TextView) findViewById(R.id.local_count);
        cloud_count = (TextView) findViewById(R.id.cloud_count);
        last_sync_time = (TextView) findViewById(R.id.last_sync_time);
        auto_sync_switch = (Switch) findViewById(R.id.auto_sync_switch);
        auto_sync_switch_layout = (RelativeLayout) findViewById(R.id.auto_sync_switch_layout);
//        last_sync_time.setText(SPUtil.getSMSLastSyncTimeInfo(this));
        if(SyncTimeUtil.getSmsSyncTime(this)>0){
            last_sync_time.setText(SyncTimeUtil.setTime(SyncTimeUtil.getSmsSyncTime(this),this));
        } else {
            last_sync_time.setText(R.string.never_backup);
        }
        sync_to_cloud_layout = (RelativeLayout) findViewById(R.id.sync_to_cloud_layout);
        download_to_local_layout = (RelativeLayout) findViewById(R.id.download_to_local_layout);
        sync_to_cloud_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(SyncSmsActivity.this, SmsChooseSyncTypeActivity.class);
                i.putExtra("type", 0);
                startActivityForResult(i, 1);
            }
        });
        download_to_local_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(SyncSmsActivity.this, SmsChooseSyncTypeActivity.class);
                i.putExtra("type", 1);
                startActivityForResult(i, 1);
            }
        });

        auto_sync_switch_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auto_sync_switch.setChecked(!auto_sync_switch.isChecked());
            }
        });

        auto_sync_switch.setChecked(SyncTimeUtil.getSmsSyncLabel(this));

        auto_sync_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                SPUtil.setSMSAutoSync(SyncSmsActivity.this, b);
                SyncTimeUtil.setSmsSyncLabel(SyncSmsActivity.this,b);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            type = data.getIntExtra("type", 0);//0 备份 1 恢复

            if(type == 1){//恢复的时候要设置为默认短信
                setDefaultMsmApp();
            }

            boolean is_sync_by_time = data.getBooleanExtra("is_sync_by_time", false);
            if (is_sync_by_time) {
                int time_type = data.getIntExtra("time_type", 1);//1 一个月 2 三个月 3 半年 4 一年 5全部
                SmsSyncByTime(type, time_type);
            }
        }
    }

    public void SmsSyncByTime(final int type, int time_type) {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if(type ==0){
            mProgressDialog.setTitle(R.string.sms_sync);
        } else {
            mProgressDialog.setTitle(R.string.sms_restore);
        }
        mProgressDialog.setMessage(getString(R.string.sync_ing));
        mProgressDialog.setMax(100);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if(keyCode == KeyEvent.KEYCODE_BACK){
                    return true;
                }
                return false;
            }
        });
        mProgressDialog.show();


        switch (time_type) {
            case 1:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(type ==0){
                            new SyncSMS(SyncSmsActivity.this, SyncSmsActivity.this, SmsTimeType.TIME_ONE_MONTH).sync();
                        } else {
                            new SyncSMS(SyncSmsActivity.this, SyncSmsActivity.this, SmsTimeType.TIME_ONE_MONTH).backup();
                        }
                    }
                }).start();

                break;
            case 2:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(type ==0){
                            new SyncSMS(SyncSmsActivity.this, SyncSmsActivity.this, SmsTimeType.TIME_THREE_MONTH).sync();
                        } else {
                            new SyncSMS(SyncSmsActivity.this, SyncSmsActivity.this, SmsTimeType.TIME_THREE_MONTH).backup();
                        }
                    }
                }).start();

                break;
            case 3:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(type ==0){
                            new SyncSMS(SyncSmsActivity.this, SyncSmsActivity.this, SmsTimeType.TIME_SIX_MONTH).sync();
                        } else {
                            new SyncSMS(SyncSmsActivity.this, SyncSmsActivity.this, SmsTimeType.TIME_SIX_MONTH).backup();
                        }
                    }
                }).start();

                break;
            case 4:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(type ==0){
                            new SyncSMS(SyncSmsActivity.this, SyncSmsActivity.this, SmsTimeType.TIME_ONE_YEAR).sync();

                        } else {
                            new SyncSMS(SyncSmsActivity.this, SyncSmsActivity.this, SmsTimeType.TIME_ONE_YEAR).backup();
                        }
                    }
                }).start();

                break;
            case 5:
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if(type == 0){
                            new SyncSMS(SyncSmsActivity.this, SyncSmsActivity.this, SmsTimeType.TIME_ALL).sync();
                        } else {
                            new SyncSMS(SyncSmsActivity.this, SyncSmsActivity.this, SmsTimeType.TIME_ALL).backup();
                        }

                    }
                }).start();
                break;
        }

    }

    @Override
    public void onSyncStateChanged(PMessage msg) {
        // 通知UI
        Message message = mSyncHandler.obtainMessage();
        message.what = 1;
        message.obj = msg;
        mSyncHandler.sendMessage(message);

    }


    private Handler mSyncHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                uiProgressChanged((PMessage) msg.obj);
            } else if(msg.what == 2){
                cloud_count.setText(""+cloudSMSNum);
            }
        }
    };

    /**
     * UI进度条变化
     *
     * @param msg
     */
    private void uiProgressChanged(PMessage msg) {
        switch (msg.msgId) {
            case ISyncMsgDef.ESTATE_SYNC_ALL_BEGIN:
                //同步开始（全部任务）
                break;
            case ISyncMsgDef.ESTATE_SYNC_SCAN_BEGIN:
                //数据库扫描开始
                break;
            case ISyncMsgDef.ESTATE_SYNC_SCAN_FINISHED:
                //数据库扫描结束
                break;
            case ISyncMsgDef.ESTATE_SYNC_PROGRESS_CHANGED:
                //同步进度变化
                int progress = msg.arg1;
                if(mProgressDialog!=null){
                    mProgressDialog.setProgress(progress);
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
                syncAllFinished(msg);
                break;
            default:
                break;
        }
    }

    private void syncAllFinished(PMessage msg) {
        // 清理标识，恢复屏幕不长亮。
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mProgressDialog.dismiss();

        List<DataSyncResult> resultList = null;
        if (null != msg.obj1) {
            resultList = (List<DataSyncResult>) msg.obj1;
        }
        if (null == resultList) {
            return;
        }
        int size = resultList.size();
        DataSyncResult result = null;

        for (int i = 0; i < size; i++) {
            result = resultList.get(i);
            if (null == result) {
                return;
            }
            switch (result.getResult()) {
                case ISyncDef.SYNC_ERR_TYPE_SUCCEED:
//                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
                    showSyncSuccessDialog();
                    getSMSCount();
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


    private void showSyncSuccessDialog(){

        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String time = dateFormat.format( now );
        String sync_time = getString(R.string.sms_sync_time);
        if(type ==1){
            sync_time = getString(R.string.sms_restore_time);
        } else {
//            SPUtil.saveSMSSyncTime(this,time);//只显示备份的　恢复不显示
            SyncTimeUtil.updateSmsSyncTime(this,System.currentTimeMillis());
        }

        new AlertDialog.Builder(this).setTitle(R.string.contacts_sync).setPositiveButton(com.mst.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                String sync_info= SPUtil.getSMSLastSyncTimeInfo(SyncSmsActivity.this);
                last_sync_time.setText(SyncTimeUtil.setTime(SyncTimeUtil.getSmsSyncTime(SyncSmsActivity.this),SyncSmsActivity.this));

            }
        }).setMessage(String.format(sync_time,time)).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                last_sync_time.setText(SyncTimeUtil.setTime(SyncTimeUtil.getSmsSyncTime(SyncSmsActivity.this),SyncSmsActivity.this));
            }
        }).show();

    }


    @Override
    public void onLoginkeyExpired() {

    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }

    public void setDefaultMsmApp(){
//        startActivity(getRequestDefaultSmsAppActivity());
        //SmsApplication.setDefaultApplication(value, getContext());
        try {
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService("appops");
            appOpsManager.setMode(15, android.os.Process.myUid(), getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Intent getRequestDefaultSmsAppActivity() {
        final Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, "com.monster.cloud");
        return intent;
    }

    private int cloudSMSNum = 0;

    public void getSMSCount(){
        IStatisticsUtil mIStatisticsUtil = StatisticsFactory.getStatisticsUtil();
        int localSMSNum = mIStatisticsUtil.getLocalSmsNum(this);
        local_count.setText("" + localSMSNum);

        new Thread(new Runnable() {
            @Override
            public void run() {
                cloudSMSNum = GetCountUtils.getRecordNumOfSMS();
                mSyncHandler.sendEmptyMessage(2);
            }
        }).start();


    }


}
