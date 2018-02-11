package com.monster.market.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

import com.monster.market.R;
import com.monster.market.adapter.FragmentAdapter;
import com.monster.market.fragment.CategoryFragment;

import java.util.ArrayList;
import java.util.List;

import mst.widget.ViewPager;
import mst.widget.tab.TabLayout;
import mst.widget.toolbar.Toolbar;

/**
 * Created by xiaobin on 16-8-11.
 */
public class CategoryActivity extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FragmentAdapter adapter;

    private List<Fragment> fragmentList;
    private CategoryFragment appCategoryFragment;
    private CategoryFragment gameCategoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_category);

        initViews();
        initData();
    }

    @Override
    public void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        tabLayout = (TabLayout) this.findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        mToolbar = getToolbar();
        mToolbar.setTitle(getString(R.string.tab_category));
    }

    @Override
    public void initData() {

        appCategoryFragment = CategoryFragment.newInstance(CategoryFragment.TYPE_APP);
        gameCategoryFragment = CategoryFragment.newInstance(CategoryFragment.TYPE_GAME);
        fragmentList = new ArrayList<Fragment>();
        fragmentList.add(appCategoryFragment);
        fragmentList.add(gameCategoryFragment);
        String[] titles = {getString(R.string.app_category), getString(R.string.game_category)};
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
