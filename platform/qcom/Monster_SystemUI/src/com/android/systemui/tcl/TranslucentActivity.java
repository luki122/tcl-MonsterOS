package com.android.systemui.tcl;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.widget.Toast;

import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.ui.SplitScreenModeEvent;


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
                //SystemServicesProxy.startActivityAsUser(TranslucentActivity.this, intent, null, userHandle);
                this.startActivityAsUser(intent, null, userHandle);
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
        handler.removeMessages(MSG_SPLIT_SCREEN);
        handler.sendMessageDelayed(handler.obtainMessage(MSG_SPLIT_SCREEN), 1000);

    }

    @Override
    protected void onStop() {
        handler.removeMessages(MSG_SPLIT_SCREEN);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //如果DOCKED_STACK上有singleTask属性的activity，在分屏列表中再次把该activity拉起来时，分屏下方会出现黑屏
    //出现黑屏时TranslucentActivity一直处于onPause状态，如果1S之后还处于该状态，则下方显示分屏选择应用界面，避免一直停留在黑屏状态
    private static final int MSG_SPLIT_SCREEN = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_SPLIT_SCREEN:
                    EventBus.getDefault().send(new SplitScreenModeEvent(false));
                    break;
                default:
                    break;
            }
        }
    };

}
