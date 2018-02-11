package cn.tcl.music.adapter.live;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.model.live.LiveMusicRadio;
import cn.tcl.music.model.live.RadioBean;

import cn.tcl.music.view.OnDetailItemClickListener;


public class RadioListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<RadioBean> mRadioBeen;
    private OnDetailItemClickListener mOnDetailItemClickListener;

    public RadioListAdapter(Context context, List<LiveMusicRadio> list) {
        mContext = context;
        mRadioBeen = list.get(0).radios;
        mInflater = LayoutInflater.from(context);
    }

    public void setOnDetailItemClickListener(OnDetailItemClickListener onDetailItemClickListener) {
        mOnDetailItemClickListener = onDetailItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_online_radio_list, null);
        RadioListViewHolder radioListViewHolder = new RadioListViewHolder(view);
        return radioListViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (getItemCount() == 0) {
            return;
        }
        RadioListViewHolder radioListViewHolder = (RadioListViewHolder) holder;
        radioListViewHolder.radioListNameTv.setText(mRadioBeen.get(position).radio_name);
        radioListViewHolder.hotRadioTv.setText(mRadioBeen.get(position).play_count);
        if (!MusicApplication.getApp().isDataSaver()) {
            Glide.with(mContext)
                    .load(ImageUtil.transferImgUrl(mRadioBeen.get(position).radio_logo, 330)).asBitmap().centerCrop()
                    .placeholder(R.drawable.default_cover_home)
                    .into(radioListViewHolder.coverImg);
        } else {
            Glide.with(mContext).load("")
                    .placeholder(R.drawable.default_cover_home)
                    .into(radioListViewHolder.coverImg);
        }
        View.OnClickListener onClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (mOnDetailItemClickListener != null) {
                    mOnDetailItemClickListener.onClick(v, mRadioBeen, position);
                }
            }
        };
        radioListViewHolder.itemRadio.setOnClickListener(onClickListener);

    }

    @Override
    public int getItemCount() {
        return mRadioBeen.size();
    }

    /**
     * item view
     */
    static class RadioListViewHolder extends RecyclerView.ViewHolder {
        public RelativeLayout itemRadio;
        public ImageView coverImg;
        public TextView radioListNameTv;
        public TextView hotRadioTv;

        public RadioListViewHolder(View itemView) {
            super(itemView);
            itemRadio = (RelativeLayout) itemView.findViewById(R.id.rl_item_online_radio);
            coverImg = (ImageView) itemView.findViewById(R.id.online_radio_cover);
            radioListNameTv = (TextView) itemView.findViewById(R.id.online_radio_item_name);
            hotRadioTv = (TextView) itemView.findViewById(R.id.online_radio_hot_number);
        }
    }
}
