package com.tcl.monster.fota;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tcl.monster.fota.downloadengine.DownloadEngine;
import com.tcl.monster.fota.downloadengine.DownloadTask;
import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.model.UpdatePackageInfo;
import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.FotaUtil;

import mst.app.MstActivity;

public class FotaVersionDetailActivity extends MstActivity implements View.OnClickListener {
    /**
     * TAG for Log.
     */
    public static final String TAG = "FotaVersionDetailActivity";

    /**
     * Intent extra for DownloadTask Id.
     */
    public static final String EXTRA_ID = "ID";

    /**
     * The id for current version.
     */
    public static final String CURRENT_VERSION_ID = "0";

    /**
     * Message Code for show no version detail.
     */
    private final static int SHOW_NO_VERSION_DETAIL = 1;

    /**
     * Message Code for show current version detail.
     */
    private final static int SHOW_CURRENT_VERSION_DETAIL = 2;

    /**
     * Message Code for show new version detail.
     */
    private final static int SHOW_NEW_VERSION_DETAIL = 3;

    /** ContentView */
    private View noDetailView;
    private Button btnStartUpdate;
    private ScrollView mVersionDetail;
    private LinearLayout mVersionLayout;
    private ProgressBar mProgressBar = null;

    /**
     * The DownloadTask Id for show version detail.
     */
    private String mDownloadTaskId = null;

    /**
     * The DownloadTask for show version detail.
     */
    private DownloadTask mDownloadTask = null;

    /**
     * DownloadEngine
     */
    private DownloadEngine mDownloadEngine;

    /**
     * Version Detail String.
     */
    private String mVersionDetailStr = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mProgressBar.setVisibility(View.GONE);
            FotaLog.d(TAG, "handleMessage -> msg = " + msg.what);
            switch (msg.what) {
                case SHOW_NO_VERSION_DETAIL:
                    showNoVersionDetail();
                    break;
                case SHOW_CURRENT_VERSION_DETAIL:
                    showCurrentVersionDetail();
                    break;
                case SHOW_NEW_VERSION_DETAIL:
                    showNewVersionDetail();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_fota_version_detail);
        initView();
        initData();
    }

    private void initView() {
        noDetailView = findViewById(R.id.no_detail);
        btnStartUpdate = (Button) findViewById(R.id.start_update);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mVersionDetail = (ScrollView) findViewById(R.id.version_detail);
        mVersionLayout = new LinearLayout(this);
        mVersionLayout.setOrientation(LinearLayout.VERTICAL);

        btnStartUpdate.setOnClickListener(this);
    }

    private void initData() {
        mProgressBar.setVisibility(View.VISIBLE);
        mDownloadTaskId = getIntent().getStringExtra(EXTRA_ID);
        FotaLog.d(TAG, "initData -> getStringExtra = " + mDownloadTaskId);
        if (!TextUtils.isEmpty(mDownloadTaskId)) {
            mDownloadEngine = DownloadEngine.getInstance();
            mDownloadEngine.init(this);
            if (mDownloadTaskId.equals(CURRENT_VERSION_ID)) {
                initCurrentVersionInfo();
            } else {
                initNewVersionInfo();
            }
        } else {
            mHandler.sendEmptyMessage(SHOW_NO_VERSION_DETAIL);
        }
    }

    private void initCurrentVersionInfo() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mDownloadTaskId = prefs.getString(FotaConstants.INSTALLED_DOWNLOAD_ID,
                CURRENT_VERSION_ID);
        FotaLog.d(TAG, "initCurrentVersionInfo -> mDownloadTaskId = " + mDownloadTaskId);
        mDownloadTask = mDownloadEngine.findDownloadTaskByTaskId(mDownloadTaskId);
        if (mDownloadTask != null && mDownloadTask.getUpdateInfo() != null
                && mDownloadTask.getUpdateInfo().mUpdateDesc != null &&
                !TextUtils.isEmpty(mDownloadTask.getUpdateInfo().mUpdateDesc)) {
            String downloadTaskTv = mDownloadTask.getUpdateInfo().mTv;
            String currentVersion = FotaUtil.VERSION();
            FotaLog.d(TAG, "initCurrentVersionInfo -> downloadTaskTv = " + downloadTaskTv +
                    ", currentVersion = " + currentVersion);
            int cmp = currentVersion.compareToIgnoreCase(downloadTaskTv);
            if (cmp == 0) {
                mVersionDetailStr = mDownloadTask.getUpdateInfo().mUpdateDesc;
                mHandler.sendEmptyMessage(SHOW_CURRENT_VERSION_DETAIL);
            } else {
                mHandler.sendEmptyMessage(SHOW_NO_VERSION_DETAIL);
            }
        } else {
            mHandler.sendEmptyMessage(SHOW_NO_VERSION_DETAIL);
        }
    }

    private void initNewVersionInfo() {
        mDownloadTask = mDownloadEngine.findDownloadTaskByTaskId(mDownloadTaskId);
        if (mDownloadTask != null && mDownloadTask.getUpdateInfo() != null
                && mDownloadTask.getUpdateInfo().mUpdateDesc != null &&
                !TextUtils.isEmpty(mDownloadTask.getUpdateInfo().mUpdateDesc)) {
            mVersionDetailStr = mDownloadTask.getUpdateInfo().mUpdateDesc;
            FotaLog.d(TAG, "initData -> mVersionDetailStr = " + mVersionDetailStr);
            mHandler.sendEmptyMessage(SHOW_NEW_VERSION_DETAIL);
        } else {
            mHandler.sendEmptyMessage(SHOW_NO_VERSION_DETAIL);
        }
    }

    @Override
    public void onNavigationClicked(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_update:
                Intent intent = new Intent(FotaVersionDetailActivity.this, FotaMainActivity.class);
                intent.putExtra(FotaMainActivity.EXTRA_ACTION, FotaMainActivity.ACTION_DOWNLOAD);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private void showNoVersionDetail() {
        mVersionDetail.setVisibility(View.GONE);
        noDetailView.setVisibility(View.VISIBLE);
    }

    private void showCurrentVersionDetail() {
        noDetailView.setVisibility(View.GONE);
        btnStartUpdate.setVisibility(View.GONE);
        setVersionDetailView();
    }

    private void showNewVersionDetail() {
        noDetailView.setVisibility(View.GONE);
        setVersionDetailView();
        UpdatePackageInfo.UpdateFile updateFile = mDownloadTask.getUpdateInfo().mFiles.get(0);
        btnStartUpdate.setText(getResources().getString(R.string.start_update)
                + "(" + FotaUtil.formatSize(updateFile.mFileSize) + ")");
        btnStartUpdate.setVisibility(View.VISIBLE);
    }

    private void setVersionDetailView() {
        mVersionLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(FotaVersionDetailActivity.this);
        mVersionDetailStr = mVersionDetailStr.trim();
        FotaLog.d(TAG, "setVersionDetailView -> mVersionDetailStr = " + mVersionDetailStr);
        String[] Blocks = mVersionDetailStr.split("\n");
        for(int i = 0; i < Blocks.length; i++){
            if(!Blocks[i].equals("")) {
                if (Blocks[i].startsWith("[") && Blocks[i].endsWith("]")) {
                    FotaLog.d(TAG, "setVersionDetailView -> title = " + Blocks[i]);
                    String titleStr = Blocks[i].substring(1, Blocks[i].length() - 1);
                    TextView title = (TextView)inflater
                            .inflate(R.layout.version_detail_title_item, null);
                    title.setText(titleStr);
                    mVersionLayout.addView(title);
                } else {
                    TextView content = (TextView)inflater
                            .inflate(R.layout.version_detail_content_item, null);
                    FotaLog.d(TAG, "setVersionDetailView -> content = " + Blocks[i]);
                    content.setText(Blocks[i]);
                    mVersionLayout.addView(content);
                }
            }
        }
        mVersionDetail.addView(mVersionLayout);
        mVersionDetail.setVisibility(View.VISIBLE);
    }
}