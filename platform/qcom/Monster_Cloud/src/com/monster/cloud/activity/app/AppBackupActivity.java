package com.monster.cloud.activity.app;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.cloud.CloudApplication;
import com.monster.cloud.R;
import com.monster.cloud.activity.BaseActivity;
import com.monster.cloud.utils.LogUtil;
import com.monster.cloud.utils.SyncTimeUtil;
import com.tencent.qqpim.softbox.SoftBoxProtocolModel;

import mst.app.dialog.ProgressDialog;
import mst.widget.toolbar.Toolbar;

/**
 * Created by xiaobin on 16-11-2.
 */
public class AppBackupActivity extends BaseActivity implements View.OnClickListener{

    private RelativeLayout auto_sync_switch_layout;
    private Switch auto_sync_switch;
    private TextView last_sync_time;
    private TextView netSituationHint;

    private RelativeLayout sync_to_cloud_layout;
    private RelativeLayout download_to_local_layout;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_app_backup);

        initViews();
        initData();

        getDownloadingCount();
    }

    @Override
    public void initViews() {
        mToolbar = getToolbar();
        mToolbar.setTitle(getString(R.string.app_list));
        mToolbar.inflateMenu(R.menu.toolbar_action_app_download);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getTitle().equals("app_download_manager")) {
                    Intent managerIntent = new Intent("com.monster.market.downloadmanager");
                    startActivity(managerIntent);
                }
                return false;
            }
        });

        auto_sync_switch_layout = (RelativeLayout) findViewById(R.id.auto_sync_switch_layout);
        auto_sync_switch = (Switch) findViewById(R.id.auto_sync_switch);
        last_sync_time = (TextView) findViewById(R.id.last_sync_time);
        sync_to_cloud_layout = (RelativeLayout) findViewById(R.id.sync_to_cloud_layout);
        download_to_local_layout = (RelativeLayout) findViewById(R.id.download_to_local_layout);

        long lastSyncTime = SyncTimeUtil.getAppListSyncTime(this);
        if (lastSyncTime == 0) {
            last_sync_time.setText(getResources().getString(R.string.never_backup));
        } else {
            last_sync_time.setText(getResources().getString(R.string.last_backup) +
                    SyncTimeUtil.setTime(lastSyncTime, this));
        }

        sync_to_cloud_layout.setOnClickListener(this);
        download_to_local_layout.setOnClickListener(this);

        auto_sync_switch_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auto_sync_switch.setChecked(!auto_sync_switch.isChecked());
            }
        });

        auto_sync_switch.setChecked(SyncTimeUtil.getAppListSyncLabel(this));

        auto_sync_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SyncTimeUtil.setAppListSyncLabel(AppBackupActivity.this,b);
            }
        });

        netSituationHint = (TextView) findViewById(R.id.net_situation);
        if (SyncTimeUtil.getSyncWhenWifiLabel(this)) {
            netSituationHint.setText(getString(R.string.sync_auto));
        } else {
            netSituationHint.setText(getString(R.string.backup_all_net_situation));
        }
    }

    @Override
    public void initData() {

    }

    @Override
    public void networkNotAvailable() {
        //TODO
    }

    @Override
    public void onNavigationClicked(View view) {
        super.onNavigationClicked(view);

        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sync_to_cloud_layout:
                new AppBackupTask().execute();
                break;
            case R.id.download_to_local_layout:
                Intent i = new Intent(AppBackupActivity.this, AppRecoveryActivity.class);
                startActivity(i);
                break;
        }
    }

    private void showProgressDialog(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.app_being_backed_up_app_list));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

    }
    private void hideProgressDialog(){
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private class AppBackupTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int result = SoftBoxProtocolModel.backupSoft(getApplication());
            return result;
        }

        @Override
        protected void onPostExecute(Integer o) {
            hideProgressDialog();
            int result = o;
            if (result == SoftBoxProtocolModel.RESULT_SUCCESS) {
                Toast.makeText(getApplicationContext(), "备份软件成功", Toast.LENGTH_SHORT).show();

                SyncTimeUtil.updateListSyncTime(getApplicationContext(), System.currentTimeMillis());

                long lastSyncTime = SyncTimeUtil.getAppListSyncTime(AppBackupActivity.this);
                if (lastSyncTime == 0) {
                    last_sync_time.setText(getResources().getString(R.string.never_backup));
                } else {
                    last_sync_time.setText(getResources().getString(R.string.last_backup) +
                            SyncTimeUtil.setTime(lastSyncTime, AppBackupActivity.this));
                }

            } else if (result == SoftBoxProtocolModel.RESULT_FAIL) {
                Toast.makeText(getApplicationContext(), "备份软件失败", Toast.LENGTH_SHORT).show();
            } else if (result == SoftBoxProtocolModel.RESULT_LOGINKEY_EXPIRE) {
                Toast.makeText(getApplicationContext(), "登录态过期", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 获取应用商店正在下载数量
     * @return
     */
    private int getDownloadingCount() {
        int count = 0;
        int countMore = 0;
        Uri uri = Uri.parse("content://com.monster.market.provider/download_list_info");

        if (CloudApplication.getInstance() != null) {
            ContentResolver contentResolver = CloudApplication.getInstance().getContentResolver();
            if (contentResolver == null) {
                return count;
            }

            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                    countMore = cursor.getInt(1);
                    LogUtil.i(TAG, "getDownloadingCount count: " + count + ", countMore: " + countMore);
                }
            } else {
                LogUtil.i(TAG, "cursor null or size is 0");
            }
        }
        return count;
    }

}
