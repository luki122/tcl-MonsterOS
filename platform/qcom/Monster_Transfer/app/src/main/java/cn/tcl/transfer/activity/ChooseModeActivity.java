/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import cn.tcl.transfer.R;
import cn.tcl.transfer.fasttransfer.QQNoteActivity;
import mst.app.MstActivity;

public class ChooseModeActivity extends MstActivity {
    private View mTransfer;
    private View mQQHelper;
    private View mCloudService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_choose_mode);
        mTransfer = findViewById(R.id.one_touch_move);
        mQQHelper = findViewById(R.id.qq_helper);
        mCloudService = findViewById(R.id.cloud);
        mTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(ChooseModeActivity.this,NewNoteActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mQQHelper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(ChooseModeActivity.this,QQNoteActivity.class);
                startActivity(intent);
            }
        });
        mCloudService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.monster.cloud.main");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onNavigationClicked(View view) {
        this.finish();
    }
}
