package com.monster.paymentsecurity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.monster.paymentsecurity.R;
import com.monster.paymentsecurity.bean.PayAppInfo;
import com.monster.paymentsecurity.util.PackageUtils;

import java.util.Collections;
import java.util.List;

import mst.widget.recycleview.RecyclerView;

/**
 * Created by sandysheny on 16-11-23.
 */

public class PayListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int NORMAL_ITEM = 0;
    private static final int GROUP_ITEM = 1;

    private Context mContext;
    private List<PayAppInfo> mDataList;
    private LayoutInflater mLayoutInflater;

    private int mProtectingCount;
    private int mUnProtectCount;

    private onStateChangedListener mOnStateChangedListener;

    public PayListAdapter(Context context, List<PayAppInfo> list) {
        mContext = context;
        setData(list);
        mLayoutInflater = LayoutInflater.from(context);
    }

    public synchronized void setData(List<PayAppInfo> list) {
        mDataList = list;
        Collections.sort(mDataList);
        mProtectingCount = 0;
        mUnProtectCount = 0;
        for (PayAppInfo item : list) {
            if (item.isNeedDetect()) {
                mProtectingCount++;
            } else {
                mUnProtectCount++;
            }
        }
        notifyDataSetChanged();
    }

    public void setOnStateChangedListener(onStateChangedListener listener) {
        mOnStateChangedListener = listener;
    }

    public PayAppInfo getItem(int position) {
        if (position >= 0 && position < getItemCount()) {
            return mDataList.get(position);
        }
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == NORMAL_ITEM) {
            return new NormalItemHolder(mLayoutInflater.inflate(R.layout.item_paylist, viewGroup, false));
        } else {
            return new GroupItemHolder(mLayoutInflater.inflate(R.layout.item_paylist_group, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        PayAppInfo item = mDataList.get(i);

        if (null == item)
            return;

        if (viewHolder instanceof GroupItemHolder) {
            bindGroupItem(item, (GroupItemHolder) viewHolder);
        } else {
            NormalItemHolder holder = (NormalItemHolder) viewHolder;
            bindNormalItem(item, holder);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //第一个要显示分类
        if (position == 0) {
            return GROUP_ITEM;
        }

        boolean isNeedDetect = mDataList.get(position).isNeedDetect();
        int prevIndex = position - 1;
        boolean isDifferent = !(mDataList.get(prevIndex).isNeedDetect() == isNeedDetect);

        return isDifferent ? GROUP_ITEM : NORMAL_ITEM;
    }

    private void bindNormalItem(PayAppInfo item, NormalItemHolder holder) {
        holder.mTextView.setText(item.getName());
        holder.mSwitch.setChecked(item.isNeedDetect());
        holder.mImageView.setBackground(PackageUtils.getAppIcon(mContext, item.getPackageName()));
    }

    private void bindGroupItem(PayAppInfo item, GroupItemHolder holder) {
        bindNormalItem(item, holder);
        String protectingStr = mContext.getString(R.string.applist_protecting, mProtectingCount);
        String unProtectStr = mContext.getString(R.string.applist_unprotect, mUnProtectCount);
        holder.category.setText(item.isNeedDetect() ? protectingStr : unProtectStr);

    }

    private void switchItem(int position, boolean isChecked) {
        if (position >= 0 && position < mDataList.size()) {
            PayAppInfo info = mDataList.get(position);
            info.setNeedDetect(isChecked);
            if (mOnStateChangedListener != null) {
                mOnStateChangedListener.onStateChanged(position, isChecked);
            }
        }
    }

    private class NormalItemHolder extends RecyclerView.ViewHolder {
        View mListItem;
        TextView mTextView;
        ImageView mImageView;
        Switch mSwitch;

        NormalItemHolder(View itemView) {
            super(itemView);
            mListItem = itemView.findViewById(R.id.list_item);
            mTextView = (TextView) itemView.findViewById(R.id.text);
            mImageView = (ImageView) itemView.findViewById(R.id.icon);
            mSwitch = (Switch) itemView.findViewById(R.id.enable);
            mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                switchItem(getAdapterPosition(), isChecked);
            });
        }
    }


    private class GroupItemHolder extends NormalItemHolder {
        TextView category;

        GroupItemHolder(View itemView) {
            super(itemView);
            category = (TextView) itemView.findViewById(R.id.group_text);
        }
    }


    public interface onStateChangedListener {
        void onStateChanged(int position, boolean enable);
    }
}
