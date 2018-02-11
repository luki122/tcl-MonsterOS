/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.adapter.SafeStorageAdapter;
import cn.tcl.filemanager.adapter.SafeStorageForExternalAdapter;
import cn.tcl.filemanager.manager.MountManager;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.SafeInfo;
import cn.tcl.filemanager.utils.SafeUtils;

public class SelectStorageForExternalActivity extends FileBaseActionbarActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = SelectStorageForExternalActivity.class.getSimpleName();
    private ListView mSelectList;
    private SafeStorageForExternalAdapter adapter;
    private MountManager mMountPointManager;
    private FileManagerApplication mApplication;
    private String tag;
    private TextView mItemName;
    private TextView mPathName;
    private static final String PHONE_TAG = "phone";
    private static final String SDCARD_TAG = "sdcard";
    private static final String USBOTG_TAG = "usbotg";
    private static final int STORAGE_SELECT_CODE = 1129;
    private static final String STORAGE_SELECT_TAG = "selectTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.timerMark(TAG+" start");
        super.onCreate(savedInstanceState);
        LogUtils.timerMark(TAG+" end");
        setContentView(R.layout.select_storage);
        mToolbar.setTitle(R.string.storage);
        mToolbar.setNavigationIcon(com.mst.R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mPathName = (TextView) findViewById(R.id.storage_path_name);
        mPathName.setText(R.string.main_external_storage);
        mMountPointManager = MountManager.getInstance();
        mApplication = (FileManagerApplication) getApplicationContext();
        mSelectList = (ListView) findViewById(R.id.storage_status_list);
        adapter = new SafeStorageForExternalAdapter(SelectStorageForExternalActivity.this, mApplication, mMountPointManager);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final int category = SafeStorageAdapter.CATEGORY_STORAGE_LIST;
                final List<SafeInfo> StorageInfo = SafeUtils.getStorageExternalItem(mMountPointManager, SelectStorageForExternalActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (category != 0) {
                            adapter.setCategory(category);
                            adapter.addAll(StorageInfo);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }).start();

        mSelectList.setAdapter(adapter);
        mSelectList.setOnItemClickListener(this);
    }

    public boolean onOptionsItemSelected(MenuItem item) {//back button click
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // Click item to jump to the corresponding root directory
        mItemName = (TextView) view.findViewById(R.id.safe_storage_name);
        if (mItemName.getText().equals(getResources().getString(R.string.sd_card)) && mMountPointManager.getSDCardPath() != null) {
            tag = SDCARD_TAG;
            Intent intent = getIntent();
            intent.putExtra(STORAGE_SELECT_TAG, tag);
            setResult(STORAGE_SELECT_CODE, intent);
            this.finish();
        } else if (mItemName.getText().equals(getResources().getString(R.string.usbotg_m)) && mMountPointManager.getUsbOtgPath() != null) {
            tag = USBOTG_TAG;
            Intent intent = getIntent();
            intent.putExtra(STORAGE_SELECT_TAG, tag);
            setResult(STORAGE_SELECT_CODE, intent);
            this.finish();
        }
    }
}
