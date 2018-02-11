package cn.tcl.music.view.mixvibes;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class AlertDialogActivity extends Activity {

    TextView mMsgView;
    final int FINISH_MSG = 1;
    boolean isRoamDialog = false;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case FINISH_MSG:
                finish();
                break;
            }
        }
    };

    OnClickListener mCancelListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            handler.sendEmptyMessage(FINISH_MSG);
        }
    };

    OnClickListener mOkListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(AlertDialogActivity.this);
            sharedPrefs.edit().putBoolean("stream_via_wifi_only", false).commit();
            handler.sendEmptyMessage(FINISH_MSG);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(com.mixvibes.mvlib.R.layout.alert_dialog);

        Button okBtn = (Button) findViewById(com.mixvibes.mvlib.R.id.dialog_ok);
        Button cancelBtn = (Button) findViewById(com.mixvibes.mvlib.R.id.dialog_cancel);

        okBtn.setOnClickListener(mOkListener);
        cancelBtn.setOnClickListener(mCancelListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

}