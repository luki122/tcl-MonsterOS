package cn.tcl.music.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.download.mie.downloader.DownloadTask;
import cn.tcl.music.R;

import static com.tcl.framework.util.R.mContext;

public class DownloadedAdapter extends AbstractAdapter<DownloadTask> {

    private View.OnClickListener mIconOnClickListener;
    private List<DownloadTask> mList;
    private int mReturnCount;

    public int getReturnCount() {
        return mReturnCount;
    }

    public void setReturnCount(int returnCount) {
        mReturnCount = returnCount;
        notifyDataSetChanged();
    }

    public void setIconOnClickListener(View.OnClickListener l) {
        this.mIconOnClickListener = l;
    }

    public DownloadedAdapter(Context context) {
        super(context);
    }

    @Override
    public int getCount() {
        mList = getData();
        if (mList == null) {
            return 0;
        } else {
            return getReturnCount();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_downloaded, null);
            viewHolder = new ViewHolder();
            viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.item_downed_image);
            viewHolder.mIconImageView = (ImageView) convertView.findViewById(R.id.item_downed_menu);
            viewHolder.mSongTextView = (TextView) convertView.findViewById(R.id.item_downed_song_name);
            viewHolder.mSingerTextView = (TextView) convertView.findViewById(R.id.item_downed_singer_name);
            viewHolder.mAlbumTextView = (TextView) convertView.findViewById(R.id.item_downed_album_name);
            viewHolder.mIconImageView.setOnClickListener(mIconOnClickListener);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mIconImageView.setTag(position);
        DownloadTask task = getItem(position);
        if (task == null) {
            return convertView;
        }
        Glide.with(mContext)
                .load(task.album_logo)
                .placeholder(R.drawable.default_artist)
                .into(viewHolder.mImageView);
        viewHolder.mSongTextView.setText(task.song_name);
        viewHolder.mSingerTextView.setText(task.artist_name);
        viewHolder.mAlbumTextView.setText(task.album_name);
        return convertView;
    }

    private static class ViewHolder {
        private ImageView mImageView;
        private ImageView mIconImageView;
        private TextView mSongTextView;
        private TextView mSingerTextView;
        private TextView mAlbumTextView;
    }

}
