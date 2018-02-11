package com.android.systemui.tcl;

import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.widget.Toast;

import com.android.systemui.recents.misc.SystemServicesProxy;


/**
 * @author liuzhicang
 *         用于分屏时对back事件处理
 */

public class TranslucentActivity extends BaseActivity {
    private boolean onPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                Intent intent = bundle.getParcelable("intent");
                UserHandle userHandle = bundle.getParcelable("userhandle");
                SystemServicesProxy.startActivityAsUser(TranslucentActivity.this, intent, null, userHandle);
            }
        } catch (Exception e) {
            Toast.makeText(this, "don't installed", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (onPaused) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPaused = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
