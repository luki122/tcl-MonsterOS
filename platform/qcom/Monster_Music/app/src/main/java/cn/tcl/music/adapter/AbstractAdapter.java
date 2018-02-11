package cn.tcl.music.adapter;

/**
 * Created by hongjie.xiang on 2015/11/9.
 */

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.List;


public abstract class AbstractAdapter<T> extends BaseAdapter {

    protected Context mContext;
    protected List<T> mData;

    public AbstractAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<T> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void setOnlyData(List<T> data) {
        mData = data;
    }


    public List<T> getData() {
        return mData;
    }

    @Override
    public int getCount() {
        List<T> data = getData();
        return data == null ? 0 : data.size();
    }

    @Override
    public T getItem(int position) {
        List<T> data = getData();
        if (null == data) {
            return null;
        }
        if (position < 0 || position >= data.size()) {
            return null;
        }
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }

}

