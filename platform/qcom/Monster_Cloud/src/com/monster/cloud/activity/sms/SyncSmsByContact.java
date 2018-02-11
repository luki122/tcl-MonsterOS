package com.monster.cloud.activity.sms;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.monster.cloud.R;
import com.monster.cloud.adpater.SmsByContactAdapter;
import com.monster.cloud.bean.SmsByContact;
import com.monster.cloud.utils.SystemUtil;
import com.tencent.tclsdk.obj.SMSConversationSummary;
import com.tencent.tclsdk.utils.GetCountUtils;

import java.util.ArrayList;
import java.util.List;

import mst.app.MstActivity;
import mst.app.dialog.ProgressDialog;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;

/**
 * Created by zouxu on 16-11-25.
 */
public class SyncSmsByContact extends MstActivity implements CallBack {

    private int type = 0;//0 备份 1 恢复

    private ListView contact_list;
    private RelativeLayout sync_now;
    private TextView text_sync_now;
    private SMSConversationSummary mSummary;
    private SmsByContactAdapter mAdatper;
    private ProgressDialog getDataDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemUtil.setStatusBarColor(this,R.color.white);

        setMstContentView(R.layout.sync_sms_by_contact);
        getIntentData();
        initView();
        getData();
        showActionMode(true);
        setActionModeListener(listener);
        updateSelect();
        getActionMode().setNagativeText(getString(R.string.cancel));
        setClickEnable(false);
    }

    private void getIntentData() {
        Intent i = getIntent();
        if (i != null) {
            type = i.getIntExtra("type", 0);
        }

    }


    private void initView() {
        contact_list = (ListView) findViewById(R.id.contact_list);
        sync_now = (RelativeLayout) findViewById(R.id.sync_now);
        text_sync_now = (TextView) findViewById(R.id.text_sync_now);

        if (type == 0) {
            text_sync_now.setText(R.string.sync_to_cloud);
        } else {
            text_sync_now.setText(R.string.download_to_local);
        }

        sync_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickSyncNow();
            }
        });

        mAdatper = new SmsByContactAdapter(this, this);
        contact_list.setAdapter(mAdatper);
//        contact_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                mAdatper.click(i);
//                updateSelect();
//            }
//        });
    }

    private void ClickSyncNow() {

        List<SmsByContact> mDataList = mAdatper.getDataList();
        ArrayList<String> phone_num_list = new ArrayList<String>();
        int count=0;
        int contact_count=0;
        for(int i=0;i<mDataList.size();i++){
            if(mDataList.get(i).is_select){
                phone_num_list.add(mDataList.get(i).sms.number);
                count +=mDataList.get(i).sms.num;
                contact_count++;
            }
        }

        Intent i = new Intent();
        i.putExtra("is_sync_by_time", false);
        i.putStringArrayListExtra("phone_num_list",phone_num_list);
        i.putExtra("type", type);
        i.putExtra("count", count);
        i.putExtra("contact_count", contact_count);
        setResult(RESULT_OK, i);
        finish();
    }

    private void setClickEnable(boolean is_enable){

        if(is_enable){
            sync_now.setAlpha(1.0f);
            sync_now.setClickable(true);
        } else {
            sync_now.setAlpha(0.3f);
            sync_now.setClickable(false);
        }
    }

    private boolean is_local = false;

    private void getData() {
        getDataDialog = new ProgressDialog(this);
        getDataDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        getDataDialog.setMessage(getString(R.string.sync_ing));
        getDataDialog.show();

        if (type == 1) {
            is_local = false;
        } else {
            is_local = true;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                mSummary = GetCountUtils.getRecordOfSMSOrderByConversation(SyncSmsByContact.this, is_local);
                mHandler.sendEmptyMessage(0);
            }
        }).start();

    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i("zouxu", "mSummary.result = " + mSummary.result);
            getDataDialog.dismiss();
            if (mSummary.result == 0) {//表示获取数据成功
                List<SmsByContact> mList = new ArrayList<SmsByContact>();
                if(mSummary.SMSSummaryList!=null) {
                    for (int i = 0; i < mSummary.SMSSummaryList.size(); i++) {
                        WUPSYNC.SMSSummary sms = (WUPSYNC.SMSSummary) mSummary.SMSSummaryList.get(i);
                        SmsByContact data = new SmsByContact();
                        data.sms = sms;
                        mList.add(data);
                    }
                }
                mAdatper.updateData(mList);
            }
        }
    };

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }

    private ActionModeListener listener = new ActionModeListener() {
        @Override
        public void onActionItemClicked(ActionMode.Item item) {

            int id = item.getItemId();

            switch (id) {
                case ActionMode.POSITIVE_BUTTON://全选
                    if (isSelectAll) {
                        mAdatper.selectAll(false);
                    } else {
                        mAdatper.selectAll(true);
                    }
                    updateSelect();
                    break;
                case ActionMode.NAGATIVE_BUTTON://取消
                    finish();
                    break;
            }

        }

        @Override
        public void onActionModeShow(ActionMode actionMode) {

        }

        @Override
        public void onActionModeDismiss(ActionMode actionMode) {

        }
    };

    private boolean isSelectAll = false;

    private void updateSelect() {
        List<SmsByContact> mDataList = mAdatper.getDataList();
        int select_count = 0;
        for (int i = 0; i < mDataList.size(); i++) {
            SmsByContact data = mDataList.get(i);
            if (data.is_select) {
                select_count++;
            }
        }

        if(select_count>0){
            setClickEnable(true);
        } else {
            setClickEnable(false);
        }

        getActionMode().setTitle(String.format(getString(R.string.str_have_select), select_count));

        if (select_count == 0 || select_count < mDataList.size()) {
            isSelectAll = false;
            getActionMode().setPositiveText(getString(R.string.select_all));
        } else {
            isSelectAll = true;
            getActionMode().setPositiveText(getString(R.string.str_select_all_no));
        }

    }

    @Override
    public void callBack() {
        updateSelect();
    }
}
