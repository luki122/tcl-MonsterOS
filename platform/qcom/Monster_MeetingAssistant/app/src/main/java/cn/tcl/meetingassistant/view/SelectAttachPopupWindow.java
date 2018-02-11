/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import cn.tcl.meetingassistant.R;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * the select popup menu
 */
public class SelectAttachPopupWindow extends PopupWindow {

    private View mRootView;
    private ListView mListView;
    private View mView;
    private String[] mList;

    public SelectAttachPopupWindow(Context context, final OnPopItemClickLister listener, String[] arrayString,
                                   View rootView) {
        super(context);
        mRootView = rootView;
        mView = ((LayoutInflater) (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))).inflate(R.layout.layout_pop, null);
        setContentView(mView);
        mList = arrayString;
        mListView = (ListView) mView.findViewById(R.id.list_view_in_pop);
        PopAdapter myAdapter = new PopAdapter(context, mList);
        mListView.setAdapter(myAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listener.click(mList[i], i,view);
            }
        });
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
    }

    public void show() {
        this.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
    }

    static class ViewHolder {
        public TextView title;
    }

    public class PopAdapter extends BaseAdapter {
        private LayoutInflater mInflater = null;
        private String[] titles;

        private PopAdapter(Context context, String[] titles) {
            this.mInflater = LayoutInflater.from(context);
            this.titles = titles;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public String getItem(int position) {
            return titles[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.pop_item_layout, null);
                holder.title = (TextView) convertView.findViewById(R.id.item_in_pop);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.title.setText(getItem(position));

            return convertView;
        }
    }

    public interface OnPopItemClickLister {
        void click(String string, int position, View view);
    }
}