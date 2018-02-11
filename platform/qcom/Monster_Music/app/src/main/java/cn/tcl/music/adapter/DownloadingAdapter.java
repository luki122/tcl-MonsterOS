package cn.tcl.music.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.List;

import cn.download.mie.downloader.DownloadStatus;
import cn.download.mie.downloader.DownloadTask;
import cn.tcl.music.R;
import cn.tcl.music.util.LogUtil;

public class DownloadingAdapter extends BaseAdapter {

    private static final String TAG = DownloadingAdapter.class.getSimpleName();
    private List<DownloadTask> mList;
    private Context mContext;

//    private View.OnClickListener mOnClickListener;

//    public void setOnClickLinsiner(View.OnClickListener l) {
//        this.mOnClickListener = l;
//    }

    public DownloadingAdapter(Context context, List<DownloadTask> list) {
        super();
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_downloading, null);
            viewHolder = new ViewHolder();
            viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.item_down_image);
            viewHolder.mIconImageView = (ImageView) convertView.findViewById(R.id.item_down_icon);
            viewHolder.mSongTextView = (TextView) convertView.findViewById(R.id.item_down_song_name);
            viewHolder.mProgressBar = (ProgressBar) convertView.findViewById(R.id.item_down_progressbar);
            viewHolder.mProgressTextView = (TextView) convertView.findViewById(R.id.item_down_progress);
            viewHolder.mAllTextView = (TextView) convertView.findViewById(R.id.item_down_all);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        DownloadTask task = (DownloadTask) getItem(position);

        if (task.mStatus == DownloadStatus.DOWNLOADING) {
            LogUtil.d(TAG, " task.mStatus = " + task.mStatus);
            viewHolder.mIconImageView.setImageResource(R.drawable.download_start);
            viewHolder.mProgressBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.progressbar_color));
            viewHolder.mProgressTextView.setText(String.valueOf(" KB/S"));// TODO: set downloading speed
            DecimalFormat decimalFormat = new DecimalFormat("######0.0");
            double downloadedLength = task.mFileDownloadedSize;
            double toatalLength = task.mFileTotalSize;
            viewHolder.mAllTextView.setText(String.valueOf(decimalFormat.format(downloadedLength / 1024.0 / 1024.0)) + "M/"
                    + String.valueOf(decimalFormat.format(toatalLength / 1024.0 / 1024.0) + "M"));
            viewHolder.mProgressBar.setMax((int) task.mFileTotalSize);
            viewHolder.mProgressBar.setProgress((int) task.mFileDownloadedSize);
        } else if (task.mStatus == DownloadStatus.WAITING || task.mStatus == DownloadStatus.ERROR ||
                task.mStatus == DownloadStatus.NEW || task.mStatus == DownloadStatus.STOP) {
            viewHolder.mIconImageView.setImageResource(R.drawable.download_stop);
            viewHolder.mProgressBar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.progressbar_pause));
            viewHolder.mProgressBar.setProgress(R.drawable.progressbar_pause);
            viewHolder.mProgressTextView.setText(R.string.download_pause);
            viewHolder.mProgressBar.setMax((int) task.mFileTotalSize);
            viewHolder.mProgressBar.setProgress((int) task.mFileDownloadedSize);
        }

        Glide.with(mContext)
                .load(task.album_logo)
                .placeholder(R.drawable.default_artist)
                .into(viewHolder.mImageView);
        viewHolder.mSongTextView.setText(task.song_name);
        return convertView;
    }

    private static class ViewHolder {
        private ImageView mImageView;
        private ImageView mIconImageView;
        private TextView mSongTextView;
        private ProgressBar mProgressBar;
        private TextView mProgressTextView;
        private TextView mAllTextView;
    }

}
