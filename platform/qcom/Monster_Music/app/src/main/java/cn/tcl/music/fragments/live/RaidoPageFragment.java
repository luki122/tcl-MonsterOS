package cn.tcl.music.fragments.live;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tcl.framework.log.NLog;

import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.activities.FragmentArgs;
import cn.tcl.music.activities.live.FragmentContainerActivityV2;
import cn.tcl.music.model.live.RadioCategoryBean;

public class RaidoPageFragment extends ATabTitlePagerFragment<ATabTitlePagerFragment.TabTitlePagerBean> {
    static final String TAG = RaidoPageFragment.class.getSimpleName();
    ArrayList<RadioCategoryBean> pageTitleeans;

    public static void launch(Activity from, ArrayList<RadioCategoryBean> categoryBeans) {
        FragmentArgs args = new FragmentArgs();
        args.add("pageTitleeans", categoryBeans);
        FragmentContainerActivityV2.launch(from, RaidoPageFragment.class, args);
    }


    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        if (savedInstanceSate == null) {
            pageTitleeans = (ArrayList<RadioCategoryBean>) getArguments().getSerializable("pageTitleeans");
        } else {
            pageTitleeans = (ArrayList<RadioCategoryBean>) savedInstanceSate.getSerializable("pageTitleeans");
        }
        NLog.d(TAG, "pageTitleeans = " + pageTitleeans);
        if (pageTitleeans == null) {
            pageTitleeans = new ArrayList<>();
        }
        //设置actionbar
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDefaultDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        LayoutInflater inflater1 = LayoutInflater.from(getActivity());
        View customView = inflater1.inflate(R.layout.second_view_header, new LinearLayout(getActivity()), false);
        actionBar.setCustomView(customView);
        TextView tx = (TextView) customView.findViewById(R.id.second_header_view);
        tx.setText(getActivity().getResources().getString(R.string.radio));

        super.layoutInit(inflater, savedInstanceSate);
        initView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("pageTitleeans", pageTitleeans);
    }

    private void initView() {
        //setActionBarTitle(R.string.radio);
    }

    @Override
    protected ArrayList getPageTitleBeans() {
        ArrayList<TabTitlePagerBean> beans = new ArrayList<>();
        if (pageTitleeans != null && pageTitleeans.size() > 0) {
            for (RadioCategoryBean bean : pageTitleeans) {
                TabTitlePagerBean tabTitlePagerBean = new TabTitlePagerBean(bean.category_id, bean.category_name);
                beans.add(tabTitlePagerBean);
            }
        }
        return beans;
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
        int index = getViewPager().getCurrentItem();
        if (index == 0) {
            //MobclickAgent.onEvent(getActivity(), MobConfig.RADIO_TAB_RECOMMEND_BROWSE);
        } else if (index == 1) {
            //MobclickAgent.onEvent(getActivity(), MobConfig.RADIO_TAB_ORIGINAL_BROWSE);
        } else if (index == 2) {
            //MobclickAgent.onEvent(getActivity(), MobConfig.RADIO_TAB_SCENE_BROWSE);
        } else if (index == 3) {
            // MobclickAgent.onEvent(getActivity(), MobConfig.RADIO_TAB_STYLE_BROWSE);
        } else {
            //MobclickAgent.onEvent(getActivity(), MobConfig.RADIO_TAB_MOOD_BROWSE);
        }
    }

    @Override
    protected String setFragmentTitle() {
        return TAG;
    }

    @Override
    protected Fragment newFragment(TabTitlePagerBean bean) {
        return RadioListFragment.newInstance(bean);
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

}
