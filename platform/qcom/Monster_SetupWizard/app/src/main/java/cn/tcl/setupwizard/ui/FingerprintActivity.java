/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.ui;

/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-3356295*/
import android.graphics.drawable.AnimationDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

import cn.tcl.setupwizard.R;
import cn.tcl.setupwizard.utils.CommonUtils;
import cn.tcl.setupwizard.utils.LogUtils;

/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-21,BUG-2669930*/
/* MODIFIED-END by xinlei.sheng,BUG-2669930*/

public class FingerprintActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "FingerprintActivity";
    private static final int REQUEST_CODE_FINGERPRINT = 500;
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/

    private ImageView mAnimImageView;
    /* MODIFIED-END by xinlei.sheng,BUG-3356295*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);
        findViewById(R.id.header_back).setOnClickListener(this);
        findViewById(R.id.fingerprint_btn_begin).setOnClickListener(this);
        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
        findViewById(R.id.fingerprint_btn_continue).setOnClickListener(this);
        findViewById(R.id.fingerprint_skip).setOnClickListener(this);
        findViewById(R.id.fingerprint_other).setOnClickListener(this);

        FingerprintManager fm = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE); // MODIFIED by xinlei.sheng, 2016-11-04,BUG-3356295
        if (!fm.isHardwareDetected()) {
            findViewById(R.id.fingerprint_prompt).setVisibility(View.GONE);
            findViewById(R.id.fingerprint_unsupported).setVisibility(View.VISIBLE);
            findViewById(R.id.fingerprint_btn_begin).setEnabled(false);
        }

        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-21,BUG-2669930*/
        mAnimImageView = (ImageView) findViewById(R.id.background_fingerprint);
        mAnimImageView.setImageResource(R.drawable.anim_lock);
//        Glide.with(this).load(R.drawable.gif_fingerprint)
//                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                .into(new GlideDrawableImageViewTarget(imageView, 1));
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }

    @Override
    protected void onResume() {
        setButtonVisibility(); // MODIFIED by xinlei.sheng, 2016-11-18,BUG-3356295
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AnimationDrawable drawable = (AnimationDrawable) mAnimImageView
                                .getDrawable();
                        drawable.start();
                    }
                });
            }
        }, 500);
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FINGERPRINT) {
            setButtonVisibility(); // MODIFIED by xinlei.sheng, 2016-11-18,BUG-3356295
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSetupFinished() {
        if (!this.isDestroyed()) {
            this.finish();
        }
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.header_back:
                finish();
                /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-3356295*/
                Intent simIntent = new Intent(this, UserTermsActivity.class); // MODIFIED by
                // xinlei.sheng, 2016-09-30,BUG-2669930
                /* MODIFIED-END by xinlei.sheng,BUG-3356295*/
                simIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(simIntent);
                break;
            case R.id.fingerprint_btn_begin:
            /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
            case R.id.fingerprint_other:
                Intent intent = new Intent();
                ComponentName cn = new ComponentName("com.android.settings",
                        "com.android.settings.fingerprint.FingerprintSettings");
                intent.setComponent(cn);
                startActivityForResult(intent, REQUEST_CODE_FINGERPRINT);
                break;
            case R.id.fingerprint_btn_continue:
            case R.id.fingerprint_skip:
                startActivity(new Intent(this, FinishActivity.class));
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                break;
            default:
                break;
        }
    }

    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-18,BUG-3356295*/
    private void setButtonVisibility() {
        if (CommonUtils.hasFingerprint(this)) {
            findViewById(R.id.fingerprint_btn_begin).setVisibility(View.GONE);
            findViewById(R.id.fingerprint_skip).setVisibility(View.GONE);
            findViewById(R.id.fingerprint_btn_continue).setVisibility(View.VISIBLE);
            findViewById(R.id.fingerprint_other).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.fingerprint_btn_begin).setVisibility(View.VISIBLE);
            findViewById(R.id.fingerprint_skip).setVisibility(View.VISIBLE);
            findViewById(R.id.fingerprint_btn_continue).setVisibility(View.GONE);
            findViewById(R.id.fingerprint_other).setVisibility(View.GONE);
        }
    }
    /* MODIFIED-END by xinlei.sheng,BUG-3356295*/
}
