/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import java.util.ArrayList;

import cn.tcl.filemanager.FileManagerApplication;
import cn.tcl.filemanager.R;
import cn.tcl.filemanager.dialog.PasswordDialog;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.SafeUtils;

@TargetApi(Build.VERSION_CODES.M)
public class FileEncryptWelcomeActivity extends FileBaseActionbarActivity implements View.OnClickListener {

    private static final String TAG = "FileEncryptWelcomeActivity";

    private Button mEncryptNotUseBtn;
    private Button mEncryptUseBtn;
    private Button mBtnOk;
    private ViewPager mViewPager;
    private ArrayList<View> mViewList;

    public static final String STATUS_KEY = "DATA";
    public static final int MORE_STATUS = 1;
    public static final int CATEGORY_STATUS = 2;
    private PasswordDialog mPasswordDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setActionbarTitle(R.string.category_safe);
//        getActionBar().hide();
        setContentView(R.layout.encrypt_welcome);

        initViewList();

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(0);

    }

    /**
     * init view object
     */
    private void initViewList() {
        mViewList = new ArrayList<>();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View welcomeSecurity = layoutInflater.inflate(R.layout.welcome_security, null);
        View welcomeDepth = layoutInflater.inflate(R.layout.welcome_depth, null);
        View welcomeConvenient = layoutInflater.inflate(R.layout.welcome_convenient, null);
        View welcomeAdd = layoutInflater.inflate(R.layout.welcome_add, null);

        mViewList.add(welcomeSecurity);
        mViewList.add(welcomeDepth);
        if (getIntent().getIntExtra(STATUS_KEY, CATEGORY_STATUS) == MORE_STATUS) {
            mViewList.add(welcomeConvenient);
        } else {
            mViewList.add(welcomeAdd);
        }

        mEncryptNotUseBtn = (Button) welcomeConvenient.findViewById(R.id.encrypt_not_use_btn);
        mEncryptUseBtn = (Button) welcomeConvenient.findViewById(R.id.encrypt_use_btn);
        mBtnOk = (Button) welcomeAdd.findViewById(R.id.btn_ok);

        mEncryptNotUseBtn.setOnClickListener(this);
        mEncryptUseBtn.setOnClickListener(this);
        mBtnOk.setOnClickListener(this);
    }

    PagerAdapter pagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            container.removeView(mViewList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewList.get(position), 0);
            return mViewList.get(position);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case android.R.id.home:
            case R.id.encrypt_not_use_btn:
                this.finish();
                break;
            case R.id.btn_ok:
                //todo Encrypt i know
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.encrypt_use_btn:
                AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
                infoDialog.setTitle(R.string.welcome_dialog_title);
                if (isSystemLock()) {
                    //TODO Verify identity
                    infoDialog.setMessage(R.string.welcome_dialog_info);
                    infoDialog.setPositiveButton(R.string.welcome_dialog_verify_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent verificationIntent = new Intent();
                            verificationIntent.setAction("com.tct.securitycenter.FingerprintVerify");
                            startActivityForResult(verificationIntent, 100);
                        }
                    });
                } else {
                    // TODO set system pwd
                    infoDialog.setMessage(R.string.welcome_dialog_verify_pwd);
                    infoDialog.setPositiveButton(R.string.safe_set, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            ComponentName cn = new ComponentName("com.android.settings",
                                    "com.android.settings.fingerprint.FingerprintSettings");
                            intent.setComponent(cn);
                            startActivity(intent);
                        }
                    });
                    infoDialog.setNeutralButton(R.string.cancel, null);
                }
                infoDialog.show();

                LogUtils.i(TAG, "start use encrypt");
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                //todo Encrypt
                setResult(RESULT_OK);
            }
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPasswordDialog != null && mPasswordDialog.fingerPrintDialog != null && mPasswordDialog.fingerPrintDialog.isShowing()) {
            mPasswordDialog.fingerPrintDialog.dismiss();
            mPasswordDialog.stopFingerprint();
        }
    }

}
