package cn.tcl.music.fragments;

import android.app.ActionBar;
import android.app.Fragment;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.DownloadManagerActivity;
import cn.tcl.music.adapter.MusicFragmentPagerAdapter;
import cn.tcl.music.view.striptab.CustomViewPager;
import mst.widget.tab.TabLayout;

/**
 * Created by dongdong.huang on 2015/11/10.
 * viewpager页面加载类
 */
public abstract class PagerNetWorkFragment extends NetWorkBaseFragment{
    public CustomViewPager mPager;
    public TabLayout mTabLayout; // MODIFIED by beibei.yang, 2016-05-09,BUG-2019225
    public ImageView xiamiIcon ;
    private MusicFragmentPagerAdapter mPagerAdapter;
    @Override
    protected int getSubContentLayout() {
        return R.layout.pager_network;
    }

    @Override
    protected void findViewByIds(View parent) {
        super.findViewByIds(parent);
        mPager = (CustomViewPager) parent.findViewById(R.id.pager);
        mTabLayout = (TabLayout)parent.findViewById(R.id.tabs);
        xiamiIcon=(ImageView)parent.findViewById(R.id.xiami_icon_bottom);
        ActionBar actionBar = ((DownloadManagerActivity)getActivity()).getActionBar();
        if(actionBar==null){
            return ;

        }
        actionBar.setTitle(getTitle());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
    }
    //[BUGFIX]-Add-BEGIN by Peng.Tian,Defect 1930076,2016/05/22
    @Override
    public void onResume() {
        super.onResume();
/* MODIFIED-BEGIN by beibei.yang, 2016-05-09,BUG-2019225*/
//        if(mTabLayout != null) {
//            mTabLayout.removeAllTabs();
//            initTabs(mTabLayout);
//        }
/* MODIFIED-END by beibei.yang,BUG-2019225*/
    }
    //[BUGFIX]-Add-END by Peng.Tian
    @Override
    protected void initViews() {
        super.initViews();
        doLoadData();
    }

    @Override
    protected void showContent() {
        super.showContent();
        mPagerAdapter = new MusicFragmentPagerAdapter(getActivity().getFragmentManager(), getFragments());
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mPager);
        mTabLayout.removeAllTabs();
        initTabs(mTabLayout);
    }


    /**
     * 加载网络数据
     */
    public abstract void doLoadData();

    /**
     *
     * 设置显示fragment
     */
    public abstract List<Fragment> getFragments();

    /**
     *
     * 设置显示tabs
     */
    public abstract void initTabs(TabLayout tabLayout);

    /**
     * 设置标题
     */
    protected String getTitle(){
        return "";
    }

}
