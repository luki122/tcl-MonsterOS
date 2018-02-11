/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-21,BUG-2669930*/
import android.widget.ImageView;

/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
/* MODIFIED-END by xinlei.sheng,BUG-2669930*/

import java.security.PublicKey;

import cn.tcl.setupwizard.R;

public class UserTermsActivity extends BaseActivity implements View.OnClickListener{

    public static final String ACTION_LICENSE = "cn.tcl.setupwizard.action.license";
    public static final String ACTION_PRIVACY = "cn.tcl.setupwizard.action.privacy";
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_terms);
        findViewById(R.id.header_back).setOnClickListener(this);
        findViewById(R.id.user_terms_license).setOnClickListener(this);
        findViewById(R.id.user_terms_privacy).setOnClickListener(this);
        findViewById(R.id.user_terms_btn_continue).setOnClickListener(this);

        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-21,BUG-2669930*/
        ImageView imageView = (ImageView) findViewById(R.id.background_user_terms);
        Glide.with(this).load(R.drawable.gif_user_terms)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new GlideDrawableImageViewTarget(imageView, 1));
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }

    @Override
    /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
    public void onSetupFinished() {
        if (!this.isDestroyed()) {
            this.finish();
        }
    }

    @Override
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.header_back:
                finish();
                Intent simIntent = new Intent(this, OtherServiceActivity.class); // MODIFIED by xinlei.sheng, 2016-09-30,BUG-2669930
                simIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(simIntent);
                /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
                break;
            case R.id.user_terms_license:
                Intent licenseIntent = new Intent(this, TermsContentActivity.class);
                licenseIntent.setAction(ACTION_LICENSE);
                startActivity(licenseIntent);
                break;
            case R.id.user_terms_privacy:
                Intent privacyIntent = new Intent(this, TermsContentActivity.class);
                privacyIntent.setAction(ACTION_PRIVACY);
                startActivity(privacyIntent);
                /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                break;
            case R.id.user_terms_btn_continue:
                startActivity(new Intent(this, FingerprintActivity.class)); // MODIFIED by xinlei.sheng, 2016-08-22,BUG-2669930
                break;
            default:
                break;
        }
    }
}
