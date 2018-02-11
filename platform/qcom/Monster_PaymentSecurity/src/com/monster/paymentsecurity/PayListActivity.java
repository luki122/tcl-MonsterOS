package com.monster.paymentsecurity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.monster.paymentsecurity.adapter.PayListAdapter;
import com.monster.paymentsecurity.bean.PayAppInfo;
import com.monster.paymentsecurity.constant.Constant;
import com.monster.paymentsecurity.db.PayListDao;

import java.util.ArrayList;
import java.util.List;

import mst.app.MstActivity;
import mst.widget.recycleview.LinearLayoutManager;
import mst.widget.recycleview.RecyclerView;
import mst.widget.toolbar.Toolbar;

/**
 * Created by sandysheny on 16-11-23.
 */

public class PayListActivity extends MstActivity {
    private List<PayAppInfo> mList;
    private PayListAdapter mListAdapter;

    private PayListDao mPayListDao;

    private PackageChangeReceiver mPackageChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_paylist);
        mPayListDao = new PayListDao(this);

        mList = new ArrayList<>();
        mListAdapter = new PayListAdapter(this, mList);

        initViews();
        initData();

        registerPackageChangeReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterPackageChangeReceiver();
    }

    @Override
    public void onNavigationClicked(View view) {
        super.onNavigationClicked(view);
        finish();
    }

    private void initViews() {
        Toolbar toolbar = getToolbar();
        toolbar.setTitle(R.string.pay_list_manager);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mListAdapter);
        mListAdapter.setOnStateChangedListener(new PayListAdapter.onStateChangedListener() {
            @Override
            public void onStateChanged(int position, boolean enable) {
                PayAppInfo payAppInfo = mListAdapter.getItem(position);
                if (payAppInfo != null) {
                    mPayListDao.insert(payAppInfo);
                    Intent intent = new Intent(Constant.ACTION_APP_CHANGE);
                    sendBroadcast(intent);
                }
            }
        });
    }

    private void initData() {
        mList = mPayListDao.getPayList();
        if (mList != null && mList.size() > 0) {
            mListAdapter.setData(mList);
        }
    }

    private void registerPackageChangeReceiver() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constant.ACTION_APP_CHANGE);
        mPackageChangeReceiver = new PackageChangeReceiver();
        registerReceiver(mPackageChangeReceiver, mIntentFilter);
    }


    private void unregisterPackageChangeReceiver() {
        if (mPackageChangeReceiver != null) {
            unregisterReceiver(mPackageChangeReceiver);
            mPackageChangeReceiver = null;
        }
    }

    private class PackageChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constant.ACTION_APP_CHANGE.equals(action)) {
                Log.d("Sandysheny", "receive app change broadcast");
                initData();
            }

        }
    }

}
