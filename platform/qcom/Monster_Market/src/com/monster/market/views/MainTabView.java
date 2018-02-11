package com.monster.market.views;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.monster.market.R;
import com.monster.market.activity.AppListActivity;
import com.monster.market.activity.AppRankingActivity;
import com.monster.market.activity.CategoryActivity;
import com.monster.market.activity.EssentialActivity;
import com.monster.market.activity.TopicActivity;

/**
 * Created by xiaobin on 16-7-28.
 */
public class MainTabView extends LinearLayout implements View.OnClickListener {

    private MainTabItemView mtiv_new;
    private MainTabItemView mtiv_topic;
    private MainTabItemView mtiv_ranking;
    private MainTabItemView mtiv_category;
    private View bottom_line;

    private int height;
    private int bgHeight;
    private int maintabMaxHeight;
    private int maintabMinHeight;

    private int tempHeight;

    public MainTabView(Context context) {
        super(context);
        initView();
    }

    public MainTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MainTabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.view_main_tab, this);

        mtiv_new = (MainTabItemView) view.findViewById(R.id.mtiv_new);
        mtiv_topic = (MainTabItemView) view.findViewById(R.id.mtiv_topic);
        mtiv_ranking = (MainTabItemView) view.findViewById(R.id.mtiv_ranking);
        mtiv_category = (MainTabItemView) view.findViewById(R.id.mtiv_category);
        bottom_line = view.findViewById(R.id.bottom_line);

        mtiv_new.setOnClickListener(this);
        mtiv_topic.setOnClickListener(this);
        mtiv_ranking.setOnClickListener(this);
        mtiv_category.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mtiv_new:
//                Intent newIntent = new Intent(getContext(), AppListActivity.class);
//                newIntent.putExtra(AppListActivity.OPEN_TYPE, AppListActivity.TYPE_NEW);
//                getContext().startActivity(newIntent);
                Intent newIntent = new Intent(getContext(), EssentialActivity.class);
                getContext().startActivity(newIntent);
                break;
            case R.id.mtiv_ranking:
                Intent rankingIntent = new Intent(getContext(),
                        AppRankingActivity.class);
                getContext().startActivity(rankingIntent);
                break;
            case R.id.mtiv_topic:
//                Intent topicIntent = new Intent(getContext(),
//                        TopicActivity.class);
//                getContext().startActivity(topicIntent);
                Intent awardIntent = new Intent(getContext(), AppListActivity.class);
                awardIntent.putExtra(AppListActivity.OPEN_TYPE, AppListActivity.TYPE_AWARD);
                getContext().startActivity(awardIntent);
                break;
            case R.id.mtiv_category:
                Intent categoryIntent = new Intent(getContext(),
                        CategoryActivity.class);
                getContext().startActivity(categoryIntent);
                break;
        }
    }

    public void showBottomLine(boolean show) {
        if (bottom_line != null) {
            if (show) {
                bottom_line.setVisibility(View.VISIBLE);
            } else {
                bottom_line.setVisibility(View.GONE);
            }
        }
    }

}
