package com.monster.market.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.monster.market.R;
import com.monster.market.bean.SearchKeyInfo;

import java.util.List;

/**
 * Created by xiaobin on 16-8-22.
 */
public class SearchKeyAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inflater;

    private List<SearchKeyInfo> searchKeyInfoList;

    public SearchKeyAdapter(Context context, List<SearchKeyInfo> searchKeyInfoList) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.searchKeyInfoList = searchKeyInfoList;
    }

    @Override
    public int getCount() {
        return searchKeyInfoList == null ? 0 : searchKeyInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.item_search_key, null);

            holder = new Holder();
            holder.iv_history = (ImageView) view.findViewById(R.id.iv_history);
            holder.tv_text = (TextView) view.findViewById(R.id.tv_text);

            holder.iv_history.setVisibility(View.GONE);

            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        holder.tv_text.setText(searchKeyInfoList.get(i).getKey());

        return view;
    }

    static class Holder {
        ImageView iv_history;
        TextView tv_text;
    }

}
