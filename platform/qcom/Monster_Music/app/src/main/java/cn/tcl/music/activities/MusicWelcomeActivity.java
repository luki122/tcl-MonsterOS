package cn.tcl.music.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.PermissionsUtil;
import cn.tcl.music.util.PreferenceUtil;
import mst.app.MstActivity;

public class MusicWelcomeActivity extends MstActivity implements View.OnClickListener {

    public static final String TAG = MusicWelcomeActivity.class.getSimpleName();
    public static final int PERMISSION_REQUEST_CODE = 5;
    private Handler mHandler;
    private boolean mCheckFlag;
    private boolean mIsAgreed;
    private boolean mShouldCheckPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        if (mIsAgreed && !PermissionsUtil.shouldRequestPermissions(this)) {
            Intent i = new Intent(MusicWelcomeActivity.this, MusicMainActivity.class);
            startActivity(i);
            finish();
        } else {
            setContentView(R.layout.activity_welcome);
            Button startMusic = (Button) findViewById(R.id.start_music_view);
            startMusic.setOnClickListener(this);
        }
    }

    private void initData() {
        mHandler = new Handler();
        mShouldCheckPermission = true;
        mIsAgreed = PreferenceUtil.getValue(this, PreferenceUtil.NODE_IS_AGREED, PreferenceUtil.KEY_IS_AGREED, false);
    }

    @TargetApi(23)
    @Override
    protected void onStart() {
        super.onStart();
        if (mShouldCheckPermission) {
            if (PermissionsUtil.shouldCheckPermissions() && PermissionsUtil.shouldRequestPermissions(this)) {
                mCheckFlag = true;
                if (mIsAgreed) {
                    final String[] permissions = PermissionsUtil.getRequestPermissions(this);
                    mShouldCheckPermission = false;
                    requestPermissions(permissions, PERMISSION_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CheckPermissonActivity.CHECK_PERMISSION_REQUEST) {
            if (resultCode == CheckPermissonActivity.RESULT_EXIT) {
                finish();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (PermissionsUtil.shouldCheckPermissions()) {
                    if (!PermissionsUtil.shouldRequestPermissions(this)) {
                        Intent i = new Intent(MusicWelcomeActivity.this, MusicMainActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        finish();
                    }
                } else {
                    //surprise
                }

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i]);
            }
        }
        int requestPermissionSize = deniedPermissions.size();
        if (requestPermissionSize > 0) {
            PermissionsUtil.gotoExplainActivity(this, CheckPermissonActivity.class);
        } else {
            Intent i = new Intent(MusicWelcomeActivity.this, MusicMainActivity.class);
            startActivity(i);
            finish();
        }
        mShouldCheckPermission = true;
    }

    @TargetApi(23)
    @Override
    public void onClick(View v) {
        saveAgreed(true);
        if (mCheckFlag) {
            String[] permissions = PermissionsUtil.getRequestPermissions(this);
            LogUtil.d(TAG, "" + PermissionsUtil.shouldExplainPermissions(this, permissions));
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        } else {
            Intent i = new Intent(MusicWelcomeActivity.this, MusicMainActivity.class);
            i.putExtra("firstLaunch", true);
            startActivity(i);
            finish();
        }

    }

    public void saveAgreed(boolean isAgreed) {
        PreferenceUtil.saveValue(this, PreferenceUtil.NODE_IS_AGREED, PreferenceUtil.KEY_IS_AGREED, isAgreed);
    }
}
