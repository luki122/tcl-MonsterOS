package cn.tcl.music.activities.live;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xiami.sdk.utils.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.model.live.SongDetailBean;

public class NetworkBatchAdapter extends BaseAdapter {

    private Context mContext;
    private List<SongDetailBean> mData = new ArrayList<SongDetailBean>();

    private List<Integer> mflags = new ArrayList<Integer>();
    private int num = 0;
    private String albumNameCustom;

    public NetworkBatchAdapter(Context context, List<SongDetailBean> data, List<Integer> flags) {
        mContext = context;
        mData = data;
        mflags = flags;
    }

    public void updateFlags(List<Integer> flags) {
        mflags = flags;
        notifyDataSetChanged();
    }

    public List<Integer> getFlags() {
        return mflags;
    }

    public void updateData(List<SongDetailBean> list) {
        mData.clear();
        mData.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        if (mData == null)
            return 0;
        else
            return mData.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mData.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    final static class BatchHolder {
        ImageView imageview;
        TextView Title;
        TextView subTitle;
        CheckBox checkBox;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final BatchHolder holder;
        if (convertView == null) {
            holder = new BatchHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.row_network_item, parent, false);
            holder.imageview = (ImageView) convertView.findViewById(R.id.network_image_view);
            holder.Title = (TextView) convertView.findViewById(R.id.network_title_view);
            holder.subTitle = (TextView) convertView.findViewById(R.id.network_subtitle_view);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.network_check);
            convertView.setTag(holder);
        } else {
            holder = (BatchHolder) convertView.getTag();
        }
        Glide.with(mContext)
                .load(ImageUtil.transferImgUrl(mData.get(position).album_logo, 330))
                .placeholder(R.drawable.default_cover_list)
                .into(holder.imageview);
        holder.Title.setText(mData.get(position).song_name);
        String albumName = mData.get(position).album_name;
        if (albumName == null && albumNameCustom != null) {
            albumName = albumNameCustom;
        }
        if (albumName == null) {
            albumName = "";
        }
        holder.subTitle.setText(mData.get(position).artist_name + " " + albumName);

        if (mflags != null && position < mflags.size() && mflags.get(position) == 1) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("mytest", "check status is " + holder.checkBox.isChecked());
                mflags.set(position, holder.checkBox.isChecked() ? 1 : 0);
            }
        });

        return convertView;
    }

    public void updateNum(boolean isAdd) {
        if (isAdd) {
            num++;
        } else {
            num--;
        }
        if (num == mflags.size()) {
            setCheckBox(true);
        } else {
            setCheckBox(false);
        }
        if (num != 0) {
            setBatchEnable(true);
        } else {
            setBatchEnable(false);
        }
    }

    private void setCheckBox(boolean b) {
//        if (!(mContext instanceof ActivityAddSong)) {
        NetworkBatchActivity activity = (NetworkBatchActivity) mContext;
        activity.setCheckAll(b);
//        }
    }

    private void setBatchEnable(boolean b) {
//        if (!(mContext instanceof ActivityAddSong)) {
        NetworkBatchActivity activity = (NetworkBatchActivity) mContext;
        activity.setBatchOperate(b);
//        }
    }

    public void setAlbumName(String mAlbumName) {
        albumNameCustom = mAlbumName;
    }

}
