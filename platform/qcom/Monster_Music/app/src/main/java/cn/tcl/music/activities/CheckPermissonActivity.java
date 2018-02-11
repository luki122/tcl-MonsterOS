package cn.tcl.music.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cn.tcl.music.R;
import cn.tcl.music.util.PermissionsUtil;
import mst.app.MstActivity;

public class CheckPermissonActivity extends MstActivity implements View.OnClickListener {

    public static int CHECK_PERMISSION_REQUEST = 1;
    public static int RESULT_EXIT = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_permission);
        initView();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_exit:
                setResult(CheckPermissonActivity.RESULT_EXIT);
                finish();
                break;
            case R.id.btn_settings:
                PermissionsUtil.gotoSettings(this);
                break;
        }
    }

    void initView() {
        TextView btn_exit = (TextView) findViewById(R.id.btn_exit);
        TextView btn_settings = (TextView) findViewById(R.id.btn_settings);
        btn_exit.setOnClickListener(this);
        btn_settings.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

}
