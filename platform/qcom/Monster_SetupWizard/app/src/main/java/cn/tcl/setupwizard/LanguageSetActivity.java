/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.setupwizard;

import android.content.Intent;
import android.content.res.Resources;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
import android.nfc.Tag;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cn.tcl.setupwizard.adapter.LanguageAdapter;
import cn.tcl.setupwizard.ui.BaseActivity;
import cn.tcl.setupwizard.ui.SimSetActivity;
import cn.tcl.setupwizard.utils.CommonUtils;
import cn.tcl.setupwizard.utils.LanguageUtils;
import cn.tcl.setupwizard.utils.LogUtils;
import cn.tcl.setupwizard.utils.VersionUtils;

public class LanguageSetActivity extends BaseActivity
        implements View.OnClickListener, AdapterView.OnItemClickListener {

    public final static String TAG = "LanguageSetActivity";
    // String array to store the languages list
    private String[] mLanguages;
    // Current language of the device.
    private String mCurrentLanguage;
    private int mSelectIndex = 0;
    private Button mBtnContinue;
    private TextView mProgressBar;
    private ListView mListView;
    private LanguageAdapter mAdapter;
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    private Handler mHandler = new Handler();

    private Runnable mLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            initLanguage();
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
        LogUtils.i(TAG, "oncreate start: " + System.currentTimeMillis());
        super.onCreate(savedInstanceState);
        String versionNumber = VersionUtils.getVersionNumber(this);
        if (!TextUtils.isEmpty(versionNumber)) {
            LogUtils.i(TAG, "current version is: " + versionNumber);
        }
        setContentView(R.layout.activity_language_set);
        initView();
        initLanguage();
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                mHandler.post(mLoadingRunnable);
            }
        });
        LogUtils.i(TAG, "oncreate end: " + System.currentTimeMillis());
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.i(TAG, "onresume end: " + System.currentTimeMillis());
    }

    @Override
    public void onSetupFinished() {
        if (!this.isDestroyed()) {
            this.finish();
        }
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }

    @Override
    public void onClick(View view) {
        if (view == mBtnContinue) {
            Intent intent = new Intent(this, SimSetActivity.class);
            startActivity(intent);
        }
    }

    /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mAdapter.getSelectIndex() != position) {
            mSelectIndex = position;
            mAdapter.setSelectIndex(mSelectIndex);
            mAdapter.notifyDataSetChanged();
            setLanguages();
        }
    }

    private void initView() {
        mProgressBar = (TextView) findViewById(R.id.language_progress);
        mProgressBar.setVisibility(View.VISIBLE);
        mBtnContinue = (Button) findViewById(R.id.language_btn_continue);
        mBtnContinue.setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.language_list);
        mListView.setOnItemClickListener(this);
    }

    private void initLanguage() {
        mLanguages = LanguageUtils.getLanguages(this);
        mCurrentLanguage = LanguageUtils.getCurrentLanguage();

        for (int i = 0; i < mLanguages.length; i++) {
            if (TextUtils.equals(mCurrentLanguage, mLanguages[i])) {
                mSelectIndex = i;
            }
        }
        mAdapter = new LanguageAdapter(this, mLanguages, mSelectIndex);
        mListView.setAdapter(mAdapter);
        mListView.setSelection(mSelectIndex);
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }

    private void setLanguages(){
        // set the language
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(280);
                } catch (InterruptedException e) {

                }
                /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
                if (mSelectIndex >= 0 && mSelectIndex < mLanguages.length) {
                    LogUtils.e("LanguageSetActivity","select language is "+mLanguages[mSelectIndex]);
                    LanguageUtils.setLanguage(mLanguages[mSelectIndex]);
                    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                }
            }
        }).start();
    }
}
