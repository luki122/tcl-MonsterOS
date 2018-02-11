/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.tcl.setupwizard.R;
import cn.tcl.setupwizard.utils.LogUtils;


public class LanguageAdapter extends BaseAdapter {
    private static final String TAG = "LanguageAdapter";
    private Context mContext;
    private String[] mLanguages;
    private int mSelectIndex;

    public LanguageAdapter(Context context, String[] languages, int selectIndex) {
        mContext = context;
        mLanguages = languages;
        mSelectIndex = selectIndex;
    }

    @Override
    public int getCount() {
        return mLanguages.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.language_info_item, null);
            holder.mSelectIcon = (ImageView) convertView.findViewById(R.id.language_select_icon);
            holder.mLanguageName = (TextView) convertView.findViewById(R.id.language_name);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        try {
            holder.mLanguageName.setText(mLanguages[position]);
            if (position == mSelectIndex) {
                holder.mSelectIcon.setVisibility(View.VISIBLE);
                holder.mLanguageName.setTextColor(ContextCompat.getColor(mContext, R.color.title_black));
            } else {
                holder.mSelectIcon.setVisibility(View.GONE);
                holder.mLanguageName.setTextColor(ContextCompat.getColor(mContext, R.color.title_dark_gray));
            }
        } catch (IndexOutOfBoundsException e) {
            LogUtils.i(TAG, "getView: " + e.toString());
        }
        return convertView;
    }

    public void setSelectIndex(int selectIndex) {
        mSelectIndex = selectIndex;
    }

    public int getSelectIndex() {
        return mSelectIndex;
    }

    static class Holder {
        public ImageView mSelectIcon;
        public TextView mLanguageName;
    }
}
