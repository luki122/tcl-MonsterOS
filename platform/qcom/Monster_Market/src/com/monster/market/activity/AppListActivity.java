package com.monster.market.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.monster.market.R;
import com.monster.market.fragment.AppListFragment;

import mst.widget.toolbar.Toolbar;

/**
 * Created by xiaobin on 16-8-3.
 */
public class AppListActivity extends BaseActivity {

    public static final String OPEN_TYPE = "open_type";
    public static final String TYPE_NAME = "type_name";
    public static final String TYPE_SUB_ID = "type_sub_id";
    public static final int TYPE_NEW = 1;
    public static final int TYPE_CATEGORY = 2;
    public static final int TYPE_TOPIC = 3;
    public static final int TYPE_AWARD = 4; // 设计奖

    private int openType;
    private String typeName;
    private int subId;

    private AppListFragment appListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_app_list);

        getIntentData();
        initViews();
        initData();
    }

    @Override
    public void initViews() {
        mToolbar = getToolbar();

        mToolbar.setTitle(typeName);
    }

    @Override
    public void initData() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        if (openType == TYPE_NEW) {
            appListFragment = AppListFragment.newInstance(AppListFragment.TYPE_NEW, 0);
        } else if (openType == TYPE_CATEGORY) {
            appListFragment = AppListFragment.newInstance(AppListFragment.TYPE_CATEGORY, subId);
            appListFragment.setCategoryName(typeName);
        } else if (openType == TYPE_TOPIC) {
            appListFragment = AppListFragment.newInstance(AppListFragment.TYPE_TOPIC, subId);
        } else if (openType == TYPE_AWARD) {
            appListFragment = AppListFragment.newInstance(AppListFragment.TYPE_AWARD, 0);
        }

        ft.add(R.id.container, appListFragment);
        ft.commit();
    }

    @Override
    public void onNavigationClicked(View view) {
//        super.onNavigationClicked(view);
        finish();
    }

    private void getIntentData() {
        openType = getIntent().getIntExtra(OPEN_TYPE, TYPE_NEW);
        typeName = getIntent().getStringExtra(TYPE_NAME);
        subId = getIntent().getIntExtra(TYPE_SUB_ID, -1);

        if (openType == TYPE_NEW) {
            typeName = getString(R.string.tab_new);
        } else if (openType == TYPE_TOPIC) {
            typeName = getString(R.string.tab_topic_detail);
        } else if (openType == TYPE_AWARD) {
            typeName = getString(R.string.tab_award);
        }
    }

}
