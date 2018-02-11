package cn.tcl.music.fragments.live;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.doreso.sdk.utils.Logger;
import com.tcl.framework.log.NLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.tcl.music.R;
import cn.tcl.music.util.ActivityHelper;
import cn.tcl.music.view.striptab.SlidingTabLayout;
import mst.widget.FragmentPagerAdapter;
import mst.widget.ViewPager;

public abstract class ATabTitlePagerFragment<T extends ATabTitlePagerFragment.TabTitlePagerBean> extends
        MyABaseFragment implements ViewPager.OnPageChangeListener {
    protected View rootView;
    static final String TAG = ATabTitlePagerFragment.class.getSimpleName();

    SlidingTabLayout slidingTabs;

    protected ViewPager viewPager;
    public MyViewPagerAdapter mViewPagerAdapter;

    protected ArrayList<T> mChanneList;
    private Map<String, Fragment> fragments;
    protected int selectedIndex = 0;
    private long lastChanneTime;

    private String lastPageName;


    abstract protected ArrayList<T> getPageTitleBeans();

    abstract protected String setFragmentTitle();

    abstract protected Fragment newFragment(T bean);

    protected void replaceSelfInActivity() {
    }

    @Override
    protected int inflateContentView() {
        return R.layout.ui_tabtitle_pager_v2;
    }


    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);
        rootView = getRootView();
        initViewPage();
        setTab(savedInstanceSate);
    }

    private void initViewPage() {
        viewPager = (ViewPager) rootView.findViewById(R.id.pager);
        slidingTabs = (SlidingTabLayout) rootView.findViewById(R.id.slidingTabs);

    }

    @SuppressWarnings("unchecked")
    protected void setTab(final Bundle savedInstanceSate) {
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (getActivity() == null)
                    return;
                if (savedInstanceSate == null) {
                    mChanneList = getPageTitleBeans();
                    String lastReadType = ActivityHelper.getInstance().getShareData("pagerLastPosition" + setFragmentTitle(), null);
                    selectedIndex = 0;
                    if (getArguments() != null) {
                        lastReadType = null;

                        selectedIndex = getArguments().getInt("index", 0);
                    }
                    if (!TextUtils.isEmpty(lastReadType)) {
                        for (int i = 0; i < mChanneList.size(); i++) {
                            TabTitlePagerBean bean = mChanneList.get(i);
                            if (lastReadType.equals(bean.getType())) {
                                if (recoveryLastPositonEnable()) {
                                    selectedIndex = i;
                                }
                                break;
                            }
                        }
                    }
                } else {
                    mChanneList = (ArrayList<T>) savedInstanceSate.getSerializable("channes");
                    selectedIndex = savedInstanceSate.getInt("selectedIndex");
                }
                fragments = new HashMap<>();
                NLog.e(TAG, "mChanneList = " + mChanneList);
                if (mChanneList == null)
                    return;
                if (mChanneList.size() == 0) {

                } else {
                    for (int i = 0; i < mChanneList.size(); i++) {
                        Fragment fragment = getActivity().getFragmentManager()
                                .findFragmentByTag(mChanneList.get(i).getTitle() + setFragmentTitle());
                        if (fragment != null)
                            fragments.put(mChanneList.get(i).getTitle() + setFragmentTitle(), fragment);
                    }

                    mViewPagerAdapter = new MyViewPagerAdapter(getMyFragmentManager());
                    viewPager.setOffscreenPageLimit(3);
                    viewPager.setAdapter(mViewPagerAdapter);
                    if (selectedIndex >= mViewPagerAdapter.getCount())
                        selectedIndex = 0;
                    viewPager.setCurrentItem(selectedIndex);
                    slidingTabs.setCustomTabView(R.layout.comm_lay_tab_indicator, android.R.id.text1);
                    slidingTabs.setSelectedIndicatorColors(getSelectedIndicatorColors(R.color.comm_tab_selected_strip));
                    slidingTabs.setDistributeEvenly(isDistributeEvenly());
                    slidingTabs.setViewPager(viewPager);
                    slidingTabs.setOnPageChangeListener(ATabTitlePagerFragment.this);
                    slidingTabs.setCurrent(selectedIndex);
                    lastChanneTime = System.currentTimeMillis();
                }
                if (mChanneList != null && TextUtils.isEmpty(lastPageName) && selectedIndex < mChanneList.size()) {
                    lastPageName = mChanneList.get(selectedIndex).getTitle();
                }
            }

        }, delayInitTabs());
    }

    public FragmentManager getMyFragmentManager() {
        return getActivity().getFragmentManager();
    }

    protected boolean recoveryLastPositonEnable() {
        return true;
    }

    protected int getSelectedIndicatorColors(int color) {
        return getResources().getColor(color);
    }

    protected boolean isDistributeEvenly() {
        return mChanneList.size() <= 5;
    }

    protected int delayInitTabs() {
        return 270;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        selectedIndex = viewPager.getCurrentItem();
        outState.putSerializable("channes", mChanneList);
        outState.putInt("selectedIndex", selectedIndex);
    }

    class MyViewPagerAdapter extends FragmentPagerAdapter {

        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = fragments.get(makeFragmentName(position));
            if (fragment == null) {
                fragment = newFragment(mChanneList.get(position));

                fragments.put(makeFragmentName(position), fragment);
            }

            return fragment;
        }


        @Override
        public int getCount() {
            return mChanneList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mChanneList.get(position).getTitle();
        }


        protected String makeFragmentName(int position) {
            return mChanneList.get(position).getTitle() + setFragmentTitle();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mChanneList != null && TextUtils.isEmpty(lastPageName) && selectedIndex < mChanneList.size()) {
            lastPageName = mChanneList.get(selectedIndex).getTitle();
        }

        if (ActivityHelper.getInstance().getBooleanShareData("ChanneSortHasChanged", false) ||
                ActivityHelper.getInstance().getBooleanShareData("offlineChanneChanged", false)) {

            ActivityHelper.getInstance().putBooleanShareData("ChanneSortHasChanged", false);
            ActivityHelper.getInstance().putBooleanShareData("offlineChanneChanged", false);
            replaceSelfInActivity();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!TextUtils.isEmpty(lastPageName)) {
            lastPageName = null;
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int position) {
        NLog.d(TAG, "onPageSelected  position = " + position);
        lastPageName = mChanneList.get(position).getTitle();
        lastChanneTime = System.currentTimeMillis();
        selectedIndex = position;
        ActivityHelper.getInstance().putShareData("pagerLastPosition" + setFragmentTitle(),
                mChanneList.get(viewPager.getCurrentItem()).getType());
    }

    Handler mHandler = new Handler() {

    };

    public ViewPager getViewPager() {
        return viewPager;
    }

    public SlidingTabLayout getSlidingTabLayout() {
        return slidingTabs;
    }

    public FragmentPagerAdapter getViewPagerAdapter() {
        return mViewPagerAdapter;
    }

    public Fragment getCurrentFragment() {
        if (mViewPagerAdapter.getCount() < selectedIndex)
            return null;

        Fragment fragment = fragments.get(mViewPagerAdapter.makeFragmentName(selectedIndex));
        Logger.d(TAG, String.format("getCurrentFragment, position = %d, title = %s", selectedIndex, mViewPagerAdapter.makeFragmentName(selectedIndex)));
        return fragment;
    }

    public Fragment getFragment(String title) {
        if (fragments == null)
            return null;

        return fragments.get(title);
    }

    public Map<String, Fragment> getFragments() {
        return fragments;
    }

    public static class TabTitlePagerBean implements Serializable {

        private static final long serialVersionUID = 3680682035685685311L;

        private String type;

        private String title;

        public TabTitlePagerBean() {
        }

        public TabTitlePagerBean(String type, String title) {
            this.type = type;
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return "TabTitlePagerBean{" +
                    "type='" + type + '\'' +
                    ", title='" + title + '\'' +
                    '}';
        }
    }
}
