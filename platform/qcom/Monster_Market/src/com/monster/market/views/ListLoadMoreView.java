package com.monster.market.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import mst.widget.FoldProgressBar;
import android.widget.TextView;

import com.monster.market.R;

/**
 * Created by xiaobin on 16-9-18.
 */
public class ListLoadMoreView extends LinearLayout {

    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_ERROR = 2;
    public static final int STATUS_FINISH = 3;

    private int status = STATUS_NORMAL;
    private boolean isLoading = false;

    private FoldProgressBar listview_foot_progress;
    private TextView listview_foot_more;

    public ListLoadMoreView(Context context) {
        super(context);
        initViews();
    }

    public ListLoadMoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ListLoadMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    public ListLoadMoreView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initViews();
    }

    private void initViews() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_list_loadmore, this);
        listview_foot_progress = (FoldProgressBar) view.findViewById(R.id.listview_foot_progress);
        listview_foot_more = (TextView) view.findViewById(R.id.listview_foot_more);
    }

    public void showNormal() {
        status = STATUS_NORMAL;
        isLoading = false;

        listview_foot_progress.setVisibility(View.GONE);
        listview_foot_more.setVisibility(View.VISIBLE);
        listview_foot_more.setText("加载更多");
    }

    public void showNormalProgress() {
        status = STATUS_NORMAL;
        isLoading = false;

        listview_foot_progress.setVisibility(View.VISIBLE);
        listview_foot_more.setVisibility(View.GONE);
    }

    public void showProgress() {
        status = STATUS_NORMAL;
        isLoading = true;

        listview_foot_progress.setVisibility(View.VISIBLE);
        listview_foot_more.setVisibility(View.GONE);
    }

    public void showError() {
        status = STATUS_ERROR;
        isLoading = false;

        listview_foot_progress.setVisibility(View.GONE);
        listview_foot_more.setVisibility(View.VISIBLE);
        listview_foot_more.setText("加载失败,点击重试");
    }

    public void showFinish() {
        status = STATUS_FINISH;
        isLoading = false;

        listview_foot_progress.setVisibility(View.GONE);
        listview_foot_more.setVisibility(View.VISIBLE);
        listview_foot_more.setText("已经全部加载完啦");
    }

    public boolean isLoading() {
        return isLoading;
    }

    public int getStatus() {
        return status;
    }
}
