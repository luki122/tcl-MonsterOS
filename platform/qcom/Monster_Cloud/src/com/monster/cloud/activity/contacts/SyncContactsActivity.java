package com.monster.cloud.activity.contacts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.cloud.R;
import com.monster.cloud.utils.SPUtil;
import com.monster.cloud.utils.SyncTimeUtil;
import com.tencent.qqpim.sdk.accesslayer.StatisticsFactory;
import com.tencent.qqpim.sdk.accesslayer.def.CommonMsgCode;
import com.tencent.qqpim.sdk.accesslayer.def.ISyncDef;
import com.tencent.qqpim.sdk.accesslayer.def.PMessage;
import com.tencent.qqpim.sdk.accesslayer.interfaces.IGetRecordNumObserver;
import com.tencent.qqpim.sdk.accesslayer.interfaces.basic.ISyncProcessorObsv;
import com.tencent.qqpim.sdk.accesslayer.interfaces.statistics.IStatisticsUtil;
import com.tencent.qqpim.sdk.apps.GetRecordNumProcessor;
import com.tencent.qqpim.sdk.defines.DataSyncResult;
import com.tencent.qqpim.sdk.defines.ISyncMsgDef;
import com.tencent.tclsdk.sync.SyncContact;
import com.tencent.tclsdk.utils.GetCountUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import WUPSYNC.RESULT_TYPE;
import mst.app.MstActivity;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.ProgressDialog;

/**
 * Created by zouxu on 16-10-18.
 */
public class SyncContactsActivity extends MstActivity implements ISyncProcessorObsv{

    private TextView local_contacts_count;
    private TextView cloud_contacts_count;
    private TextView last_sync_time;
    private Switch auto_sync_switch;
    private RelativeLayout sync_layout;
    private RelativeLayout auto_sync_switch_layout;

    private String openid = "";
    private String access_token = "";
    private int sync_type = 0;

    private IStatisticsUtil mIStatisticsUtil = null;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.sync_contact);
        getToolbar().setTitle(R.string.contacts);
        mIStatisticsUtil = StatisticsFactory.getStatisticsUtil();
        getIntentData();
        initView();
        doSyncContacts(sync_type);
    }

    private void getIntentData() {
        Intent i = getIntent();
        if (i != null) {
            openid = i.getStringExtra("openid");
            access_token = i.getStringExtra("access_token");
            sync_type = i.getIntExtra("sync_type", 0);
        }

    }

    private void initView() {
        local_contacts_count = (TextView) findViewById(R.id.local_count);
        cloud_contacts_count = (TextView) findViewById(R.id.cloud_count);
        last_sync_time = (TextView) findViewById(R.id.last_sync_time);
        auto_sync_switch = (Switch) findViewById(R.id.auto_sync_switch);
        sync_layout = (RelativeLayout) findViewById(R.id.sync_layout);
        auto_sync_switch_layout = (RelativeLayout) findViewById(R.id.auto_sync_switch_layout);
//        last_sync_time.setText(SPUtil.getContactsLastSyncTimeInfo(this));
        if(SyncTimeUtil.getContactSyncTime(SyncContactsActivity.this)>0){
            last_sync_time.setText(SyncTimeUtil.setTime(SyncTimeUtil.getContactSyncTime(SyncContactsActivity.this),this));
        } else {
            last_sync_time.setText(R.string.never_sync);
        }

        sync_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(SPUtil.isFirstSyncContact(SyncContactsActivity.this)){
//                    Intent i = new Intent();
//                    i.putExtra("should_goto_sync_contact",false);
//                    i.setClass(SyncContactsActivity.this,ContactsChooseSyncTypeActivity.class);
//                    startActivityForResult(i,1);
//
//                } else {
                    doSyncContacts(1);
//                }
            }
        });

        auto_sync_switch_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auto_sync_switch.setChecked(!auto_sync_switch.isChecked());
            }
        });

        auto_sync_switch.setChecked(SyncTimeUtil.getContactSyncLabel(this));

        auto_sync_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                SPUtil.setContactsAutoSync(SyncContactsActivity.this,b);
                SyncTimeUtil.setContactSyncLabel(SyncContactsActivity.this,b);
            }
        });

        getContactNum();


    }

    public void doSyncContacts(int type) {


        if(type == 0){
            return;
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(R.string.contacts_sync);
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

        switch (type) {
            case 1:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new SyncContact(SyncContactsActivity.this, SyncContactsActivity.this).merge();
                    }
                }).start();
                break;
            case 2:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new SyncContact(SyncContactsActivity.this, SyncContactsActivity.this).sync();
                    }
                }).start();

                break;
            case 3:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new SyncContact(SyncContactsActivity.this, SyncContactsActivity.this).backup();
                    }
                }).start();
                break;
        }
    }


    @Override
    public void onNavigationClicked(View view) {
        finish();
    }


//    @Override
//    public void getRecordNumFinished(Message msg) {
////        handler.sendMessage(msg);
//    }


    @Override
    public void onSyncStateChanged(PMessage msg) {
        Message message = mSyncHandler.obtainMessage();
        message.what = 1;
        message.obj = msg;
        mSyncHandler.sendMessage(message);
    }

    @Override
    public void onLoginkeyExpired() {

    }

    private Handler mSyncHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                uiProgressChanged((PMessage) msg.obj);
            }
        }
    };


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
//                if (null != mTv_Progress) {
//                    Log.e("progress", msg.arg1 + "%");
//                    mTv_Progress.setText(msg.arg1 + "%");
//                }

                int progress = msg.arg1;
                if(mProgressDialog !=null){
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
                mProgressDialog.dismiss();
                syncAllFinished(msg);
                break;
            default:
                break;
        }
    }


    private void showSyncSuccessDialog(){

        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String time = dateFormat.format( now );
        String sync_time = getString(R.string.contacts_sync_time);

        new AlertDialog.Builder(this).setTitle(R.string.contacts_sync).setPositiveButton(com.mst.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                String sync_info= SPUtil.getContactsLastSyncTimeInfo(SyncContactsActivity.this);
                last_sync_time.setText(SyncTimeUtil.setTime(SyncTimeUtil.getContactSyncTime(SyncContactsActivity.this),SyncContactsActivity.this));

            }
        }).setMessage(String.format(sync_time,time)).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                last_sync_time.setText(SyncTimeUtil.setTime(SyncTimeUtil.getContactSyncTime(SyncContactsActivity.this),SyncContactsActivity.this));
            }
        }).show();
//        SPUtil.saveContactsSyncTime(this,time);

        SyncTimeUtil.updateContactSyncTime(this,System.currentTimeMillis());

        SPUtil.setFirstSync(this,false);
    }


    private void syncAllFinished(PMessage msg) {
        // 清理标识，恢复屏幕不长亮。
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
                    getContactNum();
                    break;
                case ISyncDef.SYNC_ERR_TYPE_RELOGIN:
                    //需要重新登录
                    Toast.makeText(this, "需要重新登录", Toast.LENGTH_SHORT).show();
                    break;
                case ISyncDef.SYNC_ERR_TYPE_CLIENT_ERR:
                    //客户端错误
                    Toast.makeText(this, "客户端错误", Toast.LENGTH_SHORT).show();
                    break;
                case ISyncDef.SYNC_ERR_TYPE_SERVER_ERR:
                    //网络错误
                    Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show();
                    break;
                case ISyncDef.SYNC_ERR_TYPE_USER_CANCEL:
                    //用户取消
                    Toast.makeText(this, "用户取消", Toast.LENGTH_SHORT).show();
                    break;
                case ISyncDef.SYNC_ERR_TYPE_FAIL_CONFLICT:
                    //由于其他软件的同步模块正在使用导致的错误
                    Toast.makeText(this, "由于其他软件的同步模块正在使用导致的错误", Toast.LENGTH_SHORT).show();
                    break;
                case ISyncDef.SYNC_ERR_TYPE_TIME_OUT:
                    //网络超时错误
                    Toast.makeText(this, "网络超时错误", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            cloud_contacts_count.setText(""+cloudContactNum);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null){
            sync_type = data.getIntExtra("sync_type",0);
            doSyncContacts(sync_type);
        }
    }

    private int cloudContactNum=0;

    public void getContactNum(){
        IStatisticsUtil mIStatisticsUtil = StatisticsFactory.getStatisticsUtil();
        new Thread(new Runnable() {
            @Override
            public void run() {
//                GetRecordNumProcessor p = new GetRecordNumProcessor(SyncContactsActivity.this);
//                p.getRecordNumOfContact();
                cloudContactNum = GetCountUtils.getRecordNumOfContact();
                handler.sendEmptyMessage(0);
            }
        }).start();

        int localContactNum = mIStatisticsUtil.getLocalContactNum(this);
        local_contacts_count.setText("" + localContactNum);

    }


}
