package cn.tcl.music.adapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tcl.framework.log.NLog;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import cn.tcl.music.R;
import cn.tcl.music.adapter.holders.ClickableViewHolder;
import cn.tcl.music.adapter.holders.QueueViewHolder;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.service.MusicPlayBackService;
import cn.tcl.music.util.ViewHolderBindingUtil;
import cn.tcl.music.view.image.ImageFetcher;

public class QueueAdapter extends RecyclerViewCursorAdapter<ClickableViewHolder> {

    private static final String TAG = QueueAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private ImageFetcher mImageFetcher;
    private boolean mElementsAreFloating = false;
    private int mCurrentToPosition = -1;
    private int mOriginalFromPosition = -1;

    boolean mSimpleAdapter = false;
//    private boolean mPlayState = false;
//    private MyBroadcastReceive myBroadcast = new MyBroadcastReceive();

    public static class DismissedInfo {
        public DismissedInfo(int position, long itemId) {
            this.position = position;
            this.itemId = itemId;
        }

        public int position;
        public long itemId;
    }

    private Set<DismissedInfo> dismissedInfos = new TreeSet<DismissedInfo>(new Comparator<DismissedInfo>() {

        @Override
        public int compare(DismissedInfo lhs, DismissedInfo rhs) {
            return lhs.position - rhs.position;
        }

    });

    public QueueAdapter(Context context, Cursor c, ImageFetcher imageFetcher) {
        super(context, c, new int[]{R.id.media_content, R.id.media_header});
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageFetcher = imageFetcher;
        //setHasStableIds(true);
        //解决播放列表界面重影的bug
        setHasStableIds(false);
        //register broadcast
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("cn.tcl.music.sendstate");
//        mContext.registerReceiver(myBroadcast, intentFilter);
    }

    public void setElementsAreFloating(boolean areElementsFloating, int fromPosition) {
        mElementsAreFloating = areElementsFloating;

        mOriginalFromPosition = fromPosition;
        mCurrentToPosition = fromPosition;
        if (!mElementsAreFloating) {
            mCurrentToPosition = -1;
            mOriginalFromPosition = -1;
        }
    }

    public void setSimpleAdapter(boolean simpleAdapter) {
        mSimpleAdapter = simpleAdapter;
    }

    public void addDismissedInfos(int position, long itemId) {
        int offsetPosition = getPositionForContent(position);

        DismissedInfo info = new DismissedInfo(offsetPosition, itemId);

        dismissedInfos.add(info);
    }

    public Set<DismissedInfo> getDismissedInfos() {
        return dismissedInfos;
    }

    public void clearDismissedInfos() {
        dismissedInfos.clear();
    }

    public int getNumDismissedSongs() {
        return dismissedInfos.size();
    }


    public void updateFloatingElements(int fromPosition, int toPosition) {
        if (toPosition == mCurrentToPosition) {
            return;
        }
        mCurrentToPosition = toPosition;

        notifyDataSetChanged();
    }

    @Override
    public void onBindCursorToViewHolder(ClickableViewHolder viewHolder,
                                         int position) {
        NLog.d(TAG, "onBindCursorToViewHolder position = " + position);
        QueueViewHolder qvh = (QueueViewHolder) viewHolder;
        ViewHolderBindingUtil.bindSong(mContext, qvh, mCursor, mImageFetcher);
        MediaInfo currentMedia = MusicPlayBackService.getCurrentMediaInfo();
        long audioId = mCursor.getLong(mCursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.AUDIO_ID));
        boolean isTrackPlaying = (currentMedia != null && currentMedia.audioId == audioId);
        int bpm = mCursor.getInt(mCursor.getColumnIndex(MusicMediaDatabaseHelper.Media.MediaColumns.BPM));
//        int randPlayOrder = mCursor.getInt(mCursor.getColumnIndex(MusicMediaDatabaseHelper.Queue.QueueColumns.RANDOM_PLAY_ORDER));
//        int playOrder = mCursor.getInt(mCursor.getColumnIndex(MusicMediaDatabaseHelper.Queue.QueueColumns.PLAY_ORDER));
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("randPlayOrder--->");
//        stringBuilder.append(randPlayOrder);
//        stringBuilder.append("----");
//        stringBuilder.append("playOrder-->");
//        stringBuilder.append(playOrder);
//        LogUtil.d(TAG, stringBuilder.toString());
        if (bpm > 0) {
            qvh.mediaSubtitleBisTextView.setText("");
        } else {
            qvh.mediaSubtitleBisTextView.setText("");
        }

        if (isTrackPlaying) {
            qvh.mediaPlayView.setImageResource(R.drawable.ic_isplaying);
            qvh.mediaPlayView.setVisibility(View.VISIBLE);
        } else {
            qvh.mediaPlayView.setImageResource(R.drawable.ic_isplaying);
            qvh.mediaPlayView.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType,
                                                  RecyclerView.LayoutManager currentRecyclerLayoutManager) {
        switch (viewType) {
            case R.id.media_content: {
                NLog.d(TAG, "onCreateViewHolder viewType = media_content");
                ViewGroup rowContainer = (ViewGroup) mInflater.inflate(R.layout.row_queue_item, parent, false);
                final QueueViewHolder qvh = new QueueViewHolder(rowContainer, this);
                if (mSimpleAdapter) {
                    qvh.contextMenuImageButton.setVisibility(View.INVISIBLE);
                }

                return qvh;
            }

        }
        return null;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() - dismissedInfos.size();
    }

    @Override
    public int getPositionForContent(int position) {
        int cursorPosition = position;

        if (mElementsAreFloating) {
            if (position == mCurrentToPosition) {
                cursorPosition = mOriginalFromPosition;
            } else if (position > mCurrentToPosition && position < mOriginalFromPosition) {
                cursorPosition = position - 1;
            } else if (position < mCurrentToPosition && position > mOriginalFromPosition) {
                cursorPosition = position + 1;
            } else if (position == mOriginalFromPosition) {
                if (mCurrentToPosition > mOriginalFromPosition)
                    cursorPosition = position + 1;
                else if (mCurrentToPosition < mOriginalFromPosition)
                    cursorPosition = position - 1;
            }
        }

        for (DismissedInfo dismissedInfo : dismissedInfos) {
            if (cursorPosition >= dismissedInfo.position)
                cursorPosition += 1;
        }
        return cursorPosition;
    }

//    private class MyBroadcastReceive extends BroadcastReceiver {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if ("cn.tcl.music.sendstate".equals(action)) {
//                int state = intent.getIntExtra("state", 0);
//                mPlayState = state == 1 ? true : false;
//                notifyDataSetChanged();
//            }
//
//        }
//
//    }

}
