/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.setupwizard.adapter;

import android.content.Context;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-2669930*/
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.tcl.setupwizard.R;
import cn.tcl.setupwizard.ui.WifiSetActivity;
import cn.tcl.setupwizard.utils.WifiUtils;

/**
 * the wifi info list adapter
 */
public class WifiListAdapter extends RecyclerView.Adapter {
/* MODIFIED-END by xinlei.sheng,BUG-2669930*/

    public final static String TAG = "WifiListAdapter";
    /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
    public final static String PROGRESS_BAR = "ProgressBar";
    public final static String SELECT_ICON = "SelectIcon";
    private Context mContext;
    private ArrayList<CustomerWifiInfo> mWifiInfos;
    private int mSelectIndex;

    public WifiListAdapter(Context context, ArrayList<CustomerWifiInfo> wifiInfos) {
        mContext = context;
        mWifiInfos = wifiInfos;
        mSelectIndex = 0;
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }


    @Override
    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-2669930*/
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.wifi_info_item, parent, false);
        return new ItemHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ItemHolder) holder).init(position);
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-2669930*/
    public int getItemCount() {
        return mWifiInfos.size();
    }

    public int getSelectIndex() {
        return mSelectIndex;
    }

    public void setSelectIndex(int selectIndex) {
        mSelectIndex = selectIndex;
    }

    public void addData(CustomerWifiInfo customerWifiInfo) {
        mWifiInfos.add(1, customerWifiInfo);
        notifyItemInserted(1);
    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ProgressBar mProgressBar;
        public ImageView mSelectIcon;
        public TextView mWifiTitle;
        public ImageView mWifiSignal;
        private RelativeLayout mLayout;

        public ItemHolder(View itemView) {
            super(itemView);
            mLayout = (RelativeLayout) itemView.findViewById(R.id.wifi_item_layout);
            mLayout.setOnClickListener(this);
            mProgressBar = (ProgressBar) itemView
                    .findViewById(R.id.wifi_setting_progress);
            mSelectIcon = (ImageView) itemView.findViewById(R.id.wifi_select_icon);
            mWifiSignal = (ImageView) itemView.findViewById(R.id.wifi_signal);
            mWifiTitle = (TextView) itemView.findViewById(R.id.wifi_title);
        }

        public void init(int position) {
            CustomerWifiInfo wifiInfo = mWifiInfos.get(position);
            mWifiTitle.setText(wifiInfo.getSsid());
            setWIfiSignal(mWifiSignal, wifiInfo);

        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
            int state = wifiInfo.getState();
            if (state == 1) {
                mProgressBar.setVisibility(View.GONE);
                mSelectIcon.setVisibility(View.GONE);
            } else if (state == 2) {
                mProgressBar.setVisibility(View.VISIBLE);
                mSelectIcon.setVisibility(View.GONE);
                setSelectIndex(position);
            } else if (state == 3) {
                mProgressBar.setVisibility(View.GONE);
                mSelectIcon.setVisibility(View.VISIBLE);
                setSelectIndex(position);
            }
        }

        private void setWIfiSignal(ImageView signalImageView, CustomerWifiInfo wifiInfo) {
            switch (wifiInfo.getLevel()) {
                case 0:
                    if (WifiUtils.getSecurity(wifiInfo.getSecurity()) > 0) {
                        signalImageView.setImageResource(R.drawable.ic_wifi_lock_signal_1_dark);
                    } else {
                        signalImageView.setImageResource(R.drawable.ic_wifi_signal_1_dark);
                    }
                    break;
                case 1:
                    if (WifiUtils.getSecurity(wifiInfo.getSecurity()) > 0) {
                        signalImageView.setImageResource(R.drawable.ic_wifi_lock_signal_2_dark);
                    } else {
                        signalImageView.setImageResource(R.drawable.ic_wifi_signal_2_dark);
                    }
                    break;
                case 2:
                    if (WifiUtils.getSecurity(wifiInfo.getSecurity()) > 0) {
                        signalImageView.setImageResource(R.drawable.ic_wifi_lock_signal_3_dark);
                    } else {
                        signalImageView.setImageResource(R.drawable.ic_wifi_signal_3_dark);
                    }
                    break;
                case 3:
                default:
                    if (WifiUtils.getSecurity(wifiInfo.getSecurity()) > 0) {
                        signalImageView.setImageResource(R.drawable.ic_wifi_lock_signal_4_dark);
                    } else {
                        signalImageView.setImageResource(R.drawable.ic_wifi_signal_4_dark);
                    }
                    break;
            }
        }

        @Override
        public void onClick(View view) {
            /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-2669930*/
            if (getAdapterPosition() < mWifiInfos.size()) {
                ((WifiSetActivity) mContext).onItemClick(mWifiInfos.get(getAdapterPosition()));
            }
            /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
            /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
        }
    }
}
