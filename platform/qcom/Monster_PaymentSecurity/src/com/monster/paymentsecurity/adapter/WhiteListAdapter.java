package com.monster.paymentsecurity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.monster.paymentsecurity.R;
import com.monster.paymentsecurity.bean.WhiteListInfo;
import com.monster.paymentsecurity.db.WhiteListDao;
import com.monster.paymentsecurity.util.PackageUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mst.widget.recycleview.RecyclerView;

import static tmsdk.common.module.qscanner.QScanConstants.APK_TYPE_UNINSTALLED;

/**
 * Created by sandysheny on 16-11-23.
 */

public class WhiteListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int NORMAL_ITEM = 0;
    private static final int GROUP_ITEM = 1;

    private Context mContext;
    private List<WhiteListInfo> mDataList;
    private LayoutInflater mLayoutInflater;
    private WhiteListDao mWhiteListDao;

    private List<WhiteListInfo> mSelectedList = new ArrayList<>();
    private OnItemClickedListener mItemClickedListener;

    public WhiteListAdapter(Context context, List<WhiteListInfo> list) {
        mContext = context;
        setData(list);
        mLayoutInflater = LayoutInflater.from(context);
        mWhiteListDao = new WhiteListDao(context);
    }

    public synchronized void setData(List<WhiteListInfo> list) {
        mDataList = list;
        notifyDataSetChanged();
    }

    public void removeItems() {
        int position = 0;
        WhiteListInfo item;
        Iterator<WhiteListInfo> iterator = mDataList.iterator();
        while (iterator.hasNext()) {
            item = iterator.next();
            if (item.isChecked()) {
                item.setEnabled(false);
                mWhiteListDao.update(item);
                iterator.remove();
                notifyItemRemoved(position);
                continue;
            }
            position += 1;
        }
        notifyItemChanged(0);
    }

    public boolean hasItemsWaitingRemove() {
        return mSelectedList.size() > 0;
    }

    public interface OnItemClickedListener {
        void onItemClicked();
    }

    public void setOnItemClickedListener(OnItemClickedListener listener) {
        mItemClickedListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == NORMAL_ITEM) {
            return new NormalItemHolder(mLayoutInflater.inflate(R.layout.item_whitelist, viewGroup, false));
        } else {
            return new GroupItemHolder(mLayoutInflater.inflate(R.layout.item_whitelist_group, viewGroup, false));
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        WhiteListInfo item = mDataList.get(i);
        if (null == item)
            return;

        if (viewHolder instanceof GroupItemHolder) {
            bindGroupItem((GroupItemHolder) viewHolder);
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
        return NORMAL_ITEM;
    }


    private void bindGroupItem(GroupItemHolder holder) {
        holder.category.setText(mContext.getString(R.string.whitelist_group, getItemCount() - 1));
    }

    private void bindNormalItem(WhiteListInfo item, NormalItemHolder holder) {
        holder.mCheckBox.setChecked(item.isChecked());
        if (item.getApkType() == APK_TYPE_UNINSTALLED) {
            holder.mImageView.setBackground(PackageUtils.getApkIcon(mContext, item.getApkPath()));
            holder.mTextView.setText(item.getName() + "(安装包)");
        } else {
            holder.mImageView.setBackground(PackageUtils.getAppIcon(mContext, item.getPackageName()));
            holder.mTextView.setText(item.getName());
        }
    }

    private void clickItem(int position, boolean isChecked) {
        if (position >= 0 && position < mDataList.size()) {
            WhiteListInfo info = mDataList.get(position);
            info.setChecked(isChecked);
            if (isChecked) {
                mSelectedList.add(info);
            } else {
                mSelectedList.remove(info);
            }
        }
    }

    private class NormalItemHolder extends RecyclerView.ViewHolder {
        View mListItem;
        TextView mTextView;
        ImageView mImageView;
        CheckBox mCheckBox;

        NormalItemHolder(View itemView) {
            super(itemView);
            mListItem = itemView.findViewById(R.id.list_item);
            mTextView = (TextView) itemView.findViewById(R.id.text);
            mImageView = (ImageView) itemView.findViewById(R.id.icon);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.checkbox);
            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    clickItem(getAdapterPosition(), isChecked);
                    if (mItemClickedListener != null) {
                        mItemClickedListener.onItemClicked();
                    }
                }
            });


        }
    }

    private class GroupItemHolder extends RecyclerView.ViewHolder {
        TextView category;

        GroupItemHolder(View itemView) {
            super(itemView);
            category = (TextView) itemView.findViewById(R.id.group_text);
        }
    }

}
