package com.monster.market.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

import com.monster.market.R;
import com.monster.market.adapter.FragmentAdapter;
import com.monster.market.fragment.AppListFragment;

import java.util.ArrayList;
import java.util.List;

import mst.widget.ViewPager;
import mst.widget.tab.TabLayout;

/**
 * Created by xiaobin on 16-8-5.
 */
public class AppRankingActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FragmentAdapter adapter;

    private List<Fragment> fragmentList;
    private AppListFragment appRankingFragment;
    private AppListFragment gameRankingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_app_ranking);

        initViews();
        initData();
    }

    @Override
    public void initViews() {
        tabLayout = (TabLayout) this.findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        mToolbar = getToolbar();
        mToolbar.setTitle(getString(R.string.tab_ranking));
    }

    @Override
    public void initData() {
        appRankingFragment = AppListFragment.newInstance(AppListFragment.TYPE_RANK, AppListFragment.RANK_TYPE_APP);
        gameRankingFragment = AppListFragment.newInstance(AppListFragment.TYPE_RANK, AppListFragment.RANK_TYPE_GAME);
        fragmentList = new ArrayList<Fragment>();
        fragmentList.add(appRankingFragment);
        fragmentList.add(gameRankingFragment);
        String[] titles = {getString(R.string.app_ranking), getString(R.string.game_ranking)};
        adapter = new FragmentAdapter(getFragmentManager(), fragmentList, titles);
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabsFromPagerAdapter(adapter);
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }

}
