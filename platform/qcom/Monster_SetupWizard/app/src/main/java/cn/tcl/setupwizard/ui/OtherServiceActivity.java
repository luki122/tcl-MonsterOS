/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.ui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-09-21,BUG-2669930*/
import android.widget.ImageView;
import android.widget.Switch;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
/* MODIFIED-END by xinlei.sheng,BUG-2669930*/

import cn.tcl.setupwizard.R;
import cn.tcl.setupwizard.utils.CommonUtils;
import cn.tcl.setupwizard.utils.LogUtils;

public class OtherServiceActivity extends BaseActivity implements View.OnClickListener {
/* MODIFIED-END by xinlei.sheng,BUG-2669930*/

    private static final String TAG = "OtherServiceActivity";
    private Switch mLocation, mExperience;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_service);
        mLocation = (Switch) findViewById(R.id.other_service_location);
        mExperience = (Switch) findViewById(R.id.other_service_experience);
        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
        if (!CommonUtils.getLocationEnabled(this)) {
            CommonUtils.setLocationEnabled(this, true);
        }
        mLocation.setChecked(true);
        mExperience.setChecked(true);
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
        mLocation.setOnClickListener(this);
        mExperience.setOnClickListener(this);
        findViewById(R.id.header_back).setOnClickListener(this);
        findViewById(R.id.other_service_btn_continue).setOnClickListener(this);

        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-21,BUG-2669930*/
        ImageView imageView = (ImageView) findViewById(R.id.background_other_service);
        Glide.with(this).load(R.drawable.gif_other_service)
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
                Intent simIntent = new Intent(this, SimSetActivity.class); // MODIFIED by xinlei.sheng, 2016-09-30,BUG-2669930
                simIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(simIntent);
                break;
            case R.id.other_service_location:
                /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
                if (!CommonUtils.getLocationEnabled(this)) {
                    CommonUtils.setLocationEnabled(this, true);
                    mLocation.setChecked(true);
                } else {
                    CommonUtils.setLocationEnabled(this, false);
                    mLocation.setChecked(false);
                    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                }
                break;
            case R.id.other_service_experience:
                break;
            case R.id.other_service_btn_continue:
                startActivity(new Intent(this, UserTermsActivity.class)); // MODIFIED by xinlei.sheng, 2016-08-22,BUG-2669930
                break;
            default:
                break;
        }
    }
}
