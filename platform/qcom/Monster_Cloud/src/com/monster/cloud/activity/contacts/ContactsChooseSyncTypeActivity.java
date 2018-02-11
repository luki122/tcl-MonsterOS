package com.monster.cloud.activity.contacts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.cloud.R;
import com.monster.cloud.utils.SystemUtil;
import com.tencent.qqpim.sdk.accesslayer.StatisticsFactory;
import com.tencent.qqpim.sdk.accesslayer.def.CommonMsgCode;
import com.tencent.qqpim.sdk.accesslayer.interfaces.IGetRecordNumObserver;
import com.tencent.qqpim.sdk.accesslayer.interfaces.statistics.IStatisticsUtil;
import com.tencent.qqpim.sdk.apps.GetRecordNumProcessor;

import WUPSYNC.RESULT_TYPE;
import mst.app.MstActivity;
import mst.app.dialog.AlertDialog;

/**
 * Created by zouxu on 16-10-19.
 */
public class ContactsChooseSyncTypeActivity extends MstActivity implements View.OnClickListener,IGetRecordNumObserver {


    private TextView local_contacts_count;
    private TextView cloud_contacts_count;

    private RelativeLayout merge_layout;
    private RadioButton radio_bt_merge;
    private TextView local_cloud_merge_count;

    private RelativeLayout base_local_layout;
    private RadioButton radio_bt_base_local;
    private TextView base_local_count;

    private RelativeLayout base_cloud_layout;
    private RadioButton radio_bt_base_cloud;
    private TextView base_cloud_count;

    private RelativeLayout next_layout ;

    private IStatisticsUtil mIStatisticsUtil = null;
    private boolean should_goto_sync_contact = true;

    private int localContactNum;
    private int cloudContactNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemUtil.setStatusBarColor(this,R.color.background_fafafa);

        setMstContentView(R.layout.choose_contacts_sync_type_activity);
        getToolbar().setTitle(R.string.choose_contects_sync_type_title);
        mIStatisticsUtil = StatisticsFactory.getStatisticsUtil();
        getIntentData();
        initView();

    }

    public void getIntentData(){
        Intent i = getIntent();
        if(i !=null){
            should_goto_sync_contact = i.getBooleanExtra("should_goto_sync_contact",true);
        }
    }



    private void initView() {
        cloud_contacts_count = (TextView)findViewById(R.id.cloud_count);
        local_contacts_count = (TextView)findViewById(R.id.local_count);

        merge_layout = (RelativeLayout)findViewById(R.id.merge_layout);
        radio_bt_merge = (RadioButton)findViewById(R.id.radio_bt_merge);
        local_cloud_merge_count = (TextView)findViewById(R.id.local_cloud_merge_count);

        base_local_layout = (RelativeLayout)findViewById(R.id.base_local_layout);
        radio_bt_base_local = (RadioButton)findViewById(R.id.radio_bt_base_local);
        base_local_count = (TextView)findViewById(R.id.base_local_count);

        base_cloud_layout = (RelativeLayout)findViewById(R.id.base_cloud_layout);
        radio_bt_base_cloud = (RadioButton)findViewById(R.id.radio_bt_base_cloud);
        base_cloud_count = (TextView)findViewById(R.id.base_cloud_count);

        next_layout = (RelativeLayout)findViewById(R.id.next_layout);

        merge_layout.setOnClickListener(this);
        base_local_layout.setOnClickListener(this);
        base_cloud_layout.setOnClickListener(this);
        next_layout.setOnClickListener(this);

        radio_bt_merge.setChecked(true);
        radio_bt_base_local.setChecked(false);
        radio_bt_base_cloud.setChecked(false);

        radio_bt_merge.setVisibility(View.VISIBLE);
        radio_bt_base_local.setVisibility(View.INVISIBLE);
        radio_bt_base_cloud.setVisibility(View.INVISIBLE);

//        radio_bt_merge.setClickable(false);
//        radio_bt_base_local.setClickable(false);
//        radio_bt_base_cloud.setClickable(false);
//
//        radio_bt_merge.setFocusable(false);
//        radio_bt_base_local.setFocusable(false);
//        radio_bt_base_cloud.setFocusable(false);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                 Log.i("zouxu","thread mIStatisticsUtil.getLocalContactNum="+mIStatisticsUtil.getLocalContactNum(ContactsChooseSyncTypeActivity.this));
//            }
//        }).start();

        showNetNum();
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.merge_layout:
                radio_bt_merge.setChecked(true);
                radio_bt_base_local.setChecked(false);
                radio_bt_base_cloud.setChecked(false);

                radio_bt_merge.setVisibility(View.VISIBLE);
                radio_bt_base_local.setVisibility(View.INVISIBLE);
                radio_bt_base_cloud.setVisibility(View.INVISIBLE);

                break;
            case R.id.base_local_layout:
                if(!radio_bt_base_local.isChecked()){
                    showBaseLocalDialog();
                }
                break;
            case R.id.base_cloud_layout:
                if(!radio_bt_base_cloud.isChecked()){
                    showBaseCloudDialog();
                }
                break;
            case R.id.next_layout:
                CLickNext();
                break;
        }

    }


    private void showBaseLocalDialog(){
        String baseLocal = getString(R.string.delete_cloud_contacts_warming);
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setPositiveButton(com.mst.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        radio_bt_merge.setChecked(false);
                        radio_bt_base_local.setChecked(true);
                        radio_bt_base_cloud.setChecked(false);

                        radio_bt_merge.setVisibility(View.INVISIBLE);
                        radio_bt_base_local.setVisibility(View.VISIBLE);
                        radio_bt_base_cloud.setVisibility(View.INVISIBLE);

                    }
                }).setNegativeButton(com.mst.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setMessage(String.format(baseLocal,cloudContactNum)).create();
        alertDialog.show();
    }

    private void showBaseCloudDialog(){
        String baseCloud = getString(R.string.delete_local_contacts_warming);
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setPositiveButton(com.mst.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        radio_bt_merge.setChecked(false);
                        radio_bt_base_local.setChecked(false);
                        radio_bt_base_cloud.setChecked(true);

                        radio_bt_merge.setVisibility(View.INVISIBLE);
                        radio_bt_base_local.setVisibility(View.INVISIBLE);
                        radio_bt_base_cloud.setVisibility(View.VISIBLE);

                    }
                }).setNegativeButton(com.mst.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setMessage(String.format(baseCloud,localContactNum)).create();
        alertDialog.show();
    }

    private void showNetNum(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                GetRecordNumProcessor p = new GetRecordNumProcessor(ContactsChooseSyncTypeActivity.this);
                p.getRecordNumOfContact();
                Log.i("zouxu","showNetNum！！");
            }
        }).start();
    }



    public void CLickNext(){


        int type = 1;
        if(radio_bt_base_local.isChecked()){
            type = 2;
        } else if(radio_bt_base_cloud.isChecked()){
            type =3;
        }
        Intent i = new Intent();
        i.putExtra("sync_type",type);

        if(should_goto_sync_contact){
            i.setClass(this,SyncContactsActivity.class);
            startActivity(i);
        } else {
            setResult(RESULT_OK,i);
        }

        finish();

    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }


    @Override
    public void getRecordNumFinished(Message message) {
        handler.sendMessage(message);
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){

            if (msg.what == MSG_ID_GET_RECORD_NUM){
                switch(msg.arg1){
                    case RESULT_TYPE._RESULT_SUCC:{
                        cloudContactNum = GetRecordNumProcessor.getServerContactNum();
                        cloud_contacts_count.setText(""+cloudContactNum);
                        updateContactCount();
                    }
                    break;
                    case CommonMsgCode.RET_NETWORK_ERR:
                        //网络错误
                        Toast.makeText(ContactsChooseSyncTypeActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
                        break;
                    case CommonMsgCode.RET_PARAMETER_ERR:
                        //参数错误
                        Toast.makeText(ContactsChooseSyncTypeActivity.this,"参数错误",Toast.LENGTH_SHORT).show();
                        break;
                    case RESULT_TYPE._RESULT_LOGINKEY_EXPIRED:
                        //登录态失效
                        Toast.makeText(ContactsChooseSyncTypeActivity.this,"登录态失效",Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        }
    };

    public void updateContactCount(){

        localContactNum = mIStatisticsUtil.getLocalContactNum(this);
        local_contacts_count.setText("" + localContactNum);


        String contacts_local_cloud_merge_count = getString(R.string.contacts_local_cloud_merge_count);
        local_cloud_merge_count.setText(String.format(contacts_local_cloud_merge_count,localContactNum,cloudContactNum));

        String contacts_base_local_count = getString(R.string.contacts_base_local_count);
        base_local_count.setText(String.format(contacts_base_local_count,localContactNum));

        String contacts_base_cloud_count = getString(R.string.contacts_base_cloud_count);
        base_cloud_count.setText(String.format(contacts_base_cloud_count,cloudContactNum));

    }

}
