package cn.tcl.music.fragments.live;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.activities.live.FragmentContainerActivityV2;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.view.EmptyLayout;

public class SingerPageFragment extends ATabTitlePagerFragment<ATabTitlePagerFragment.TabTitlePagerBean> {
    static final String TAG = SingerPageFragment.class.getSimpleName();
    private View layout_filter;

    public static void launch(Activity from) {
        FragmentContainerActivityV2.launch(from, SingerPageFragment.class);
    }

    @Override
    protected int inflateContentView() {
        return R.layout.fragment_singer_list;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);
        initView();
    }

    private int firstFilterConditionIndex = 1;

    private void initView() {
        setHasOptionsMenu(true);
        //设置actionbar
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDefaultDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View customView = inflater.inflate(R.layout.second_view_header, new LinearLayout(getActivity()), false);
        actionBar.setCustomView(customView);
        TextView tx = (TextView) customView.findViewById(R.id.second_header_view);
        tx.setText(getActivity().getResources().getString(R.string.singer));

        layout_filter = rootView.findViewById(R.id.layout_filter);

        //第1行过滤条件
        TextView tv_male = (TextView) (layout_filter.findViewById(R.id.tv_male));
        tv_male.setSelected(true);
        updateTextViewState(R.id.tv_male, 1);
        updateTextViewState(R.id.tv_female, 2);
        updateTextViewState(R.id.tv_group, 3);
    }

    @Override
    protected int getSelectedIndicatorColors(int color) {
        return super.getSelectedIndicatorColors(R.color.singer_list_tab_bg_dark);
    }

    /**
     * @param textViewId
     * @param index      该TextView在该行过滤条件的索引
     */
    private void updateTextViewState(int textViewId, final int index) {
        TextView textView = (TextView) (layout_filter.findViewById(textViewId));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //数据还未加载完成 不允许点击条目  解决当男歌手数据还正在加载中  点击 女歌手  待男歌手数据加载完成后 显示在
                //女歌手数据的列表中的bug add by xiangxiang.liu 2015/11/27 14:18
                if (currentFragment != null && currentFragment.mErrorLayout != null) {
                    if (currentFragment.mErrorLayout.getErrorState() == EmptyLayout.NETWORK_LOADING) {
                        LogUtil.e(TAG, "还没加载完成 return");
                        return;
                    }
                }
                //重置第1行过滤条件的字体颜色
                resetSingerGenderTextViewColor();
                firstFilterConditionIndex = index;
                v.setSelected(!v.isSelected());
                SingerListFragment currentFragment = (SingerListFragment) mViewPagerAdapter.getItem(selectedIndex);
                if (currentFragment != null) {
                    currentFragment.refreshData(getFilterConditionType());
                }
            }
        });
    }

    SingerListFragment currentFragment;

    private TabTitlePagerBean lastTabTitlePagerBean;

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
        //友盟统计
        if (lastTabTitlePagerBean != null) {
            //MobclickAgent.onPageEnd(lastTabTitlePagerBean.getTitle());
        }
        TabTitlePagerBean currentTabTitlePagerBean = mChanneList.get(position);
        // MobclickAgent.onPageStart(currentTabTitlePagerBean.getTitle());
        lastTabTitlePagerBean = currentTabTitlePagerBean;
        //当过滤条件改变时  重新拉取数据
        currentFragment = (SingerListFragment) mViewPagerAdapter.getItem(selectedIndex);
        String currentFilterType = getFilterConditionType();
        LogUtil.d(TAG, "currentFragment = " + currentFragment + ", view page filter type = " + currentFilterType + ", singer fragment filter type = " + currentFragment.getLastFilterType());
        if (!currentFilterType.equals(currentFragment.getLastFilterType())) {
            //过滤type不同 重新调用接口
            currentFragment.refreshData(currentFilterType);
        }
    }

    public static final String DEFAULT_FILTER_TYPE = FITER_TYPE.M.name(); //默认加载男歌手数据

    public enum FITER_TYPE {
        M,    //男歌手
        F,    //女歌手
        B     //组合
    }

    private String getFilterConditionType() {
        String filterType = DEFAULT_FILTER_TYPE;
        //男艺人
        if (firstFilterConditionIndex == 1) {
            filterType = FITER_TYPE.M.name();
        }
        //女艺人
        else if (firstFilterConditionIndex == 2) {
            filterType = FITER_TYPE.F.name();
        }
        //组合
        else if (firstFilterConditionIndex == 3) {
            filterType = FITER_TYPE.B.name();
        }
        return filterType;
    }


    private void resetSingerGenderTextViewColor() {
        TextView tv_male = (TextView) (layout_filter.findViewById(R.id.tv_male));
        tv_male.setSelected(false);
        TextView tv_female = (TextView) (layout_filter.findViewById(R.id.tv_female));
        tv_female.setSelected(false);
        TextView tv_group = (TextView) (layout_filter.findViewById(R.id.tv_group));
        tv_group.setSelected(false);
    }


    @Override
    protected ArrayList getPageTitleBeans() {
        ArrayList<TabTitlePagerBean> beans = new ArrayList<>();
        //去掉全部
      /*  beans.add(new TabTitlePagerBean("0", getString(R.string.song_style_all)));*/
        beans.add(new TabTitlePagerBean("1", getString(R.string.song_style_chinese) + "   "));
        beans.add(new TabTitlePagerBean("2", getString(R.string.song_style_european) + "   "));
        beans.add(new TabTitlePagerBean("3", getString(R.string.song_style_japan)));
        beans.add(new TabTitlePagerBean("4", getString(R.string.song_style_korean)));
        //beans.add(new TabTitlePagerBean("5", getString(R.string.song_style_musician)));
        return beans;
    }


    @Override
    protected String setFragmentTitle() {
        return TAG;
    }

    @Override
    protected Fragment newFragment(TabTitlePagerBean bean) {
        return SingerListFragment.newInstance(bean);
    }

    void switchRepeat() {
        ObjectAnimator heightOa = null;
        if (layout_filter.getHeight() == 0) {
            heightOa = ObjectAnimator.ofPropertyValuesHolder(layout_filter, PropertyValuesHolder.ofInt("height",
                    getResources().getDimensionPixelSize(R.dimen.height_filter)));
        } else {
            heightOa = ObjectAnimator.ofPropertyValuesHolder(layout_filter, PropertyValuesHolder.ofInt("height", layout_filter.getHeight(), 0));
        }
        heightOa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (Integer) animation.getAnimatedValue();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout_filter.getLayoutParams();
                // 第一次
                params.height = height;
                layout_filter.setLayoutParams(params);
            }
        });

        heightOa.setDuration(400);
        heightOa.start();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

}