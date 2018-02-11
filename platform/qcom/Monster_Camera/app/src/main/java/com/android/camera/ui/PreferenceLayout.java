package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.tct.camera.R;

/**
 * Created by mec on 9/13/16.
 */
public class PreferenceLayout extends LinearLayout implements View.OnClickListener {

    private ListView mListView;
    private ImageView mSettingClose;

    public interface onCloseClickListener {
        void onCloseClick();
    }

    private onCloseClickListener mOnCloseClickListener;

    public PreferenceLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreferenceLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PreferenceLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mListView = (ListView) findViewById(R.id.preference_list);
        mSettingClose = (ImageView) findViewById(R.id.setting_close);
        mSettingClose.setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setListViewHeight();
    }

    private void setListViewHeight() {
        ListAdapter listAdapter = mListView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, mListView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight() + listItem.getPaddingBottom() + listItem.getPaddingTop();
        }

        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = totalHeight + (mListView.getDividerHeight() * (listAdapter.getCount() - 1) + mListView.getPaddingTop() + mListView.getPaddingBottom());
        mListView.setLayoutParams(params);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_close:
                if (mOnCloseClickListener != null) {
                    mOnCloseClickListener.onCloseClick();
                }
                break;
        }
    }

    public void setOnCloseClickListener(onCloseClickListener l) {
        mOnCloseClickListener = l;
    }
}
