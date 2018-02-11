package com.monster.paymentsecurity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.monster.paymentsecurity.bean.PayAppInfo;
import com.monster.paymentsecurity.util.PackageUtils;
import com.monster.paymentsecurity.R;

import java.util.List;

import mst.widget.recycleview.RecyclerView;

/**
 * Created by sandysheny on 16-11-28.
 */

public class PayListCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<PayAppInfo> mDataList;
    private LayoutInflater mLayoutInflater;

    public PayListCardAdapter(Context context, List<PayAppInfo> list) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        setData(list);
    }

    public synchronized void setData(List<PayAppInfo> list) {
        if (list != mDataList) {
            mDataList = list;
            notifyDataSetChanged();
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ItemHolder(mLayoutInflater.inflate(R.layout.item_paylist_card, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        PayAppInfo item = mDataList.get(i);

        if (null == item)
            return;

        ItemHolder holder = (ItemHolder) viewHolder;
        holder.mImageView.setBackground(PackageUtils.getAppIcon(mContext,item.getPackageName()));
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }


    private class ItemHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        ItemHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.icon);
        }
    }

}
