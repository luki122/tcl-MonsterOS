/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.transfer.IReceiveCallback;
import cn.tcl.transfer.R;
import cn.tcl.transfer.adapters.ReceiverAdapter;
import cn.tcl.transfer.receiver.IReceiveInfo;
import cn.tcl.transfer.receiver.ReceiveBackupDataService;
import cn.tcl.transfer.util.ReceiverItem;

public class ReceiverProcessActivity extends AppCompatActivity {
    private ListView mReceiveListView;
    private TextView mStatusText;
    private Button mBtn_bottom;
    private ReceiverAdapter mAdapter;
    private List<ReceiverItem> mList;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_process);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mReceiveListView = (ListView)findViewById(R.id.receive_list);
        mStatusText = (TextView)findViewById(R.id.status);
        mBtn_bottom= (Button)findViewById(R.id.bottom_btn);
        mList = mokeList();
        mAdapter= new ReceiverAdapter(this,mList);
        mReceiveListView.setAdapter(mAdapter);
        mStatusText.setText(String.format(getResources().getString(R.string.text_receive_status),10));
        mBtn_bottom.setText(R.string.text_cancel);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                moveTaskToBack(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getLeftTime() {
        long leftsize = 0;
        long speed = 0;
        if (speed != 0) {
            long time = leftsize/speed;
            return String.valueOf(time);
        } else {
            return null;
        }
    }

    /*add for test start*/
    private List<ReceiverItem> mokeList() {
        List<ReceiverItem> list = new ArrayList<>();
        for (int i=0;i<6;i++) {
            ReceiverItem item = new ReceiverItem();
            item.setType(ReceiverItem.TYPE_SYSTEM+i);
            item.setProgress(i*10);
            item.setSize(1024*1024*i);
            list.add(item);
        }
        return list;
    }

    /*add for test end*/
}
