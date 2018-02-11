package cn.tcl.music.view.mixvibes;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import cn.tcl.music.util.Connectivity;


public class ChargeDialogActivity extends Activity {

    TextView mMsgView;
    final int FINISH_MSG = 1;
    boolean isRoamDialog = false;
    private CheckBox checkBox;

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
            PreferenceManager.getDefaultSharedPreferences(ChargeDialogActivity.this).edit().putBoolean("donot_show_chargeDialog_again", checkBox.isChecked()).commit();
            Connectivity.setMobileDataEnabled(ChargeDialogActivity.this,true);
            handler.sendEmptyMessage(FINISH_MSG);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_LEFT_ICON);
        this.setContentView(com.mixvibes.mvlib.R.layout.charge_alert_dialog);

        //this.setTitle(R.string.extraCharge_warning_title);
        Button okBtn = (Button) findViewById(com.mixvibes.mvlib.R.id.dialog_ok);
        Button cancelBtn = (Button) findViewById(com.mixvibes.mvlib.R.id.dialog_cancel);

        okBtn.setOnClickListener(mOkListener);
        cancelBtn.setOnClickListener(mCancelListener);
        checkBox = (CheckBox) findViewById(com.mixvibes.mvlib.R.id.checkbox_show_again);

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public void onAttachedToWindow() {
        // TODO Auto-generated method stub
        super.onAttachedToWindow();
    }

}