package com.monster.market.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.monster.market.R;

import java.util.List;

/**
 * Created by xiaobin on 16-8-22.
 */
public class SearchHistoryAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inflater;

    private List<String> history;

    public SearchHistoryAdapter(Context context, List<String> history) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.history = history;
    }

    @Override
    public int getCount() {
        return history == null ? 0 : history.size();
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
            holder.tv_text = (TextView) view.findViewById(R.id.tv_text);

            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        holder.tv_text.setText(history.get(i));

        return view;
    }

    static class Holder {
        TextView tv_text;
    }

}
