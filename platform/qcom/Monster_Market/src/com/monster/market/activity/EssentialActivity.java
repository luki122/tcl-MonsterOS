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
 * Created by xiaobin on 16-9-5.
 */
public class EssentialActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FragmentAdapter adapter;

    private List<Fragment> fragmentList;
    private AppListFragment appEssentialFragment;
    private AppListFragment gameEssentialFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_essential);

        initViews();
        initData();
    }

    @Override
    public void initViews() {
        tabLayout = (TabLayout) this.findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        mToolbar = getToolbar();
        mToolbar.setTitle(getString(R.string.tab_essential));

    }

    @Override
    public void initData() {
        appEssentialFragment = AppListFragment.newInstance(AppListFragment.TYPE_ESSENTIAL, AppListFragment.ESSENTIAL_TYPE_APP);
        gameEssentialFragment = AppListFragment.newInstance(AppListFragment.TYPE_ESSENTIAL, AppListFragment.ESSENTIAL_TYPE_GAME);
        fragmentList = new ArrayList<Fragment>();
        fragmentList.add(appEssentialFragment);
        fragmentList.add(gameEssentialFragment);
        String[] titles = {getString(R.string.app_essential), getString(R.string.game_essential)};
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