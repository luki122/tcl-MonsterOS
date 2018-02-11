package com.monster.paymentsecurity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.monster.paymentsecurity.adapter.WhiteListAdapter;
import com.monster.paymentsecurity.bean.WhiteListInfo;
import com.monster.paymentsecurity.db.WhiteListDao;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mst.app.MstActivity;
import mst.widget.recycleview.LinearLayoutManager;
import mst.widget.recycleview.RecyclerView;
import mst.widget.toolbar.Toolbar;

import static tmsdk.common.module.qscanner.QScanConstants.APK_TYPE_UNINSTALLED;

/**
 * Created by sandysheny on 16-11-23.
 */

public class WhiteListActivity extends MstActivity {
    private List<WhiteListInfo> mList;
    private WhiteListAdapter mListAdapter;

    private WhiteListDao mWhiteListDao;
//    private PackageChangeReceiver mPackageChangeReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_whitelist);
        mWhiteListDao = new WhiteListDao(this);

        mList = new ArrayList<>();
        mListAdapter = new WhiteListAdapter(this, mList);

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNavigationClicked(View view) {
        super.onNavigationClicked(view);
        finish();
    }

    private void initViews() {
        Toolbar toolbar = getToolbar();
        toolbar.setTitle(R.string.white_list_manager);
        Button mBtnRemove = (Button) findViewById(R.id.btn_remove);

        mBtnRemove.setOnClickListener(v -> {
            mListAdapter.removeItems();
            mBtnRemove.setEnabled(false);
        });

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mListAdapter);
        mListAdapter.setOnItemClickedListener(() -> mBtnRemove.setEnabled(mListAdapter.hasItemsWaitingRemove()));
    }

    private void initData() {
        mList = mWhiteListDao.getWhiteList();
        Iterator<WhiteListInfo> iterator = mList.iterator();
        WhiteListInfo whiteListInfo;
        File file;
        while (iterator.hasNext()) {
            whiteListInfo = iterator.next();
            if (whiteListInfo.getApkType() == APK_TYPE_UNINSTALLED) {
                file = new File(whiteListInfo.getApkPath());
                if (!file.exists()) {
                    mWhiteListDao.delete(whiteListInfo);
                    iterator.remove();
                }
            }
        }
        if (mList != null && mList.size() > 0) {
            mListAdapter.setData(mList);
        }
    }

}
