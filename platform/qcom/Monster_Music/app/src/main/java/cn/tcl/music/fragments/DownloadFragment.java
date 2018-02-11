package cn.tcl.music.fragments;

import cn.download.mie.downloader.DownloaderConfig;
import mst.app.dialog.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tcl.framework.db.EntityManager;
import com.tcl.framework.db.sqlite.Selector;
import com.tcl.framework.notification.NotificationCenter;
import com.tcl.framework.notification.TopicSubscriber;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.download.mie.base.util.DownloadManager;
import cn.download.mie.downloader.DownloadStatus;
import cn.download.mie.downloader.DownloadTask;
import cn.download.mie.downloader.IDownloadListener;
import cn.download.mie.downloader.IDownloader;
import cn.download.mie.util.DBUtils;
import cn.tcl.music.R;
import cn.tcl.music.adapter.DownloadedAdapter;
import cn.tcl.music.adapter.DownloadingAdapter;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.view.PlaylistView;

public class DownloadFragment extends NetWorkBaseFragment implements
        AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = DownloadFragment.class.getSimpleName();

    private final static int NO_SONG = 0;
    private final static int IS_EXPAND_ALL = 3;
    private final static String DOWNLOADED = "DOWNLOADED";
    private boolean isExpand;
    private int mTotalNumber;
    private int mDownLoadingNumber;
    private int mDownLoadedNumber;
    private TextView mDownloadingNumTv;
    private TextView mDownloadedNumTv;
    private RelativeLayout mDownloadingRl;
    private LinearLayout mDownloadingLl;
    private LinearLayout mDownloadedLl;
    private LinearLayout mExpandLl;
    private PlaylistView mDownloadingPv;
    private PlaylistView mDownloadedPv;
    private List<DownloadTask> mDownloadingList;
    private List<DownloadTask> mDownLoadedList;
    private DownloadingAdapter mDownloadingAdapter;
    private DownloadedAdapter mDownloadedAdapter;
    private MySubscriber mMySubscriber;
    private IDownloader mIDownloader;
    private static EntityManager<DownloadTask> mEntityManager;

    public static DownloadFragment newInstance(Bundle bundle) {
        DownloadFragment fragment = new DownloadFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private IDownloadListener listener = new IDownloadListener() {
        @Override
        public void onDownloadStatusChange(final DownloadTask item) {
            if (item.isLyric)
                return;
            if (item.mStatus == DownloadStatus.ERROR || item.mStatus == DownloadStatus.INVALID) {
                if (mIDownloader != null) {
                    mIDownloader.startMusicDownload(item.mKey);
                }
            }
            LogUtil.d("DOWNLOADER", "STATUS CHANGE" + item.mStatus);

            if (null != getActivity()) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshData();
                    }
                });
            }
        }

        @Override
        public void onDownloadProgress(DownloadTask item, long downloadSize, long totalSize,
                                       int speed, int maxSpeed, long timeCost) {
            LogUtil.d("DOWNLOADER", "progress " + downloadSize + "  " + speed);
            if (item.isLyric)
                return;
            mDownloadingAdapter.notifyDataSetChanged();
        }

        @Override
        public boolean acceptItem(DownloadTask item) {
            return true;
        }
    };

    @Override
    protected int getSubContentLayout() {
        return R.layout.fragment_download;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDownloadingList = new ArrayList<DownloadTask>();
        mDownLoadedList = new ArrayList<DownloadTask>();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void findViewByIds(View parent) {
        super.findViewByIds(parent);
        mDownloadingRl = (RelativeLayout) parent.findViewById(R.id.downloading_relative);
        mDownloadingNumTv = (TextView) parent.findViewById(R.id.downloading_num);
        mDownloadingLl = (LinearLayout) parent.findViewById(R.id.downloading_all);
        mDownloadingPv = (PlaylistView) parent.findViewById(R.id.downloading_playlistview);
        mDownloadedLl = (LinearLayout) parent.findViewById(R.id.downloaded_linear);
        mDownloadedNumTv = (TextView) parent.findViewById(R.id.downloaded_num);
        mDownloadedPv = (PlaylistView) parent.findViewById(R.id.downloaded_playlistview);
        mExpandLl = (LinearLayout) parent.findViewById(R.id.downloaded_expand);

        mDownloadingLl.setOnClickListener(this);
        mExpandLl.setOnClickListener(this);
    }

    @Override
    protected void initViews() {
        super.initViews();
        showContent();
        hideLoading();
        mDownloadingAdapter = new DownloadingAdapter(getActivity(), mDownloadingList);
        mDownloadedAdapter = new DownloadedAdapter(getActivity());
        mDownloadingPv.setAdapter(mDownloadingAdapter);
        mDownloadedPv.setAdapter(mDownloadedAdapter);

        mDownloadingPv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DownloadTask task = mDownloadingList.get(i);
                switch (task.mStatus) {
                    case DownloadStatus.NEW:
                        mIDownloader.startDownloadInNetwork(task);
                        break;
                    case DownloadStatus.WAITING:
                        mIDownloader.pauseDownload(task);
                        task.mStatus = DownloadStatus.STOP;
                        mDownloadingAdapter.notifyDataSetChanged();
                        break;
                    case DownloadStatus.DOWNLOADING:
                        mIDownloader.pauseDownload(task);
                        task.mStatus = DownloadStatus.STOP;
                        mDownloadingAdapter.notifyDataSetChanged();
                        break;
                    case DownloadStatus.STOP:
//                      if (Util.getNetworkType() == Util.NETTYPE_WIFI) {
//                      }
                        mIDownloader.startDownloadInNetwork(task);
                        task.mStatus = DownloadStatus.WAITING;
                        mDownloadingAdapter.notifyDataSetChanged();
                        break;
                    case DownloadStatus.ERROR:
                        mIDownloader.startMusicDownload(task.mKey);
                        break;
                    case DownloadStatus.INVALID:
                        mIDownloader.startMusicDownload(task.mKey);
                        break;
                    case DownloadStatus.PICTURE:
                        mIDownloader.startPictureDownload(task);
                        break;
                }
            }
        });

        mDownloadedPv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

            }
        });

        mDownloadedAdapter.setIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int position = (Integer) view.getTag();
                switch (view.getId()) {
                    case R.id.item_downed_menu:
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                        View mDownloadedDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_downloaded_song, null);
                        mBuilder.setView(mDownloadedDialogView);
                        final AlertDialog mDownloadedDialog = mBuilder.create();
                        mDownloadedDialog.show();
                        View.OnClickListener dialogListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final SongDetailBean song = new SongDetailBean();
                                DownloadTask task = mDownLoadedList.get(position);
                                song.listen_file = task.getFinalFilePath();
                                song.song_name = task.song_name;
                                song.album_logo = task.album_logo;
                                song.album_name = task.album_name;
                                song.length = task.length;
                                song.artist_name = task.artist_name;
                                song.artist_logo = task.artist_logo;
                                switch (v.getId()) {
                                }
                                mDownloadedDialog.dismiss();
                            }
                        };

                        TextView action_play_next = (TextView) mDownloadedDialogView.findViewById(R.id.action_play_next);
                        TextView action_add_to_playlist = (TextView) mDownloadedDialogView.findViewById(R.id.action_add_to_playlist);
                        TextView action_add_to_queue = (TextView) mDownloadedDialogView.findViewById(R.id.action_add_to_queue);
                        TextView action_remove = (TextView) mDownloadedDialogView.findViewById(R.id.action_remove);

                        action_play_next.setOnClickListener(dialogListener);
                        action_add_to_playlist.setOnClickListener(dialogListener);
                        action_add_to_queue.setOnClickListener(dialogListener);
                        action_remove.setOnClickListener(dialogListener);
                        break;
                }
            }
        });

        mIDownloader = DownloadManager.getInstance(getActivity()).getDownloader();
        mIDownloader.addDownloadListener(listener);
        refreshData();
    }

    /**
     * refresh data
     */
    private void refreshData() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                final List<DownloadTask> allTasks = mIDownloader.getAllTask();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDownLoadedList.clear();
                        mDownloadingList.clear();
                        for (int i = 0; i < allTasks.size(); i++) {
                            DownloadTask task = allTasks.get(i);
                            if (task.mStatus == DownloadStatus.WAITING ||
                                    task.mStatus == DownloadStatus.NEW || task.mStatus == DownloadStatus.STOP) {
                                mDownloadingList.add(task);
                            }
                        }

                        Selector selector = Selector.create().where("mStatus", "=", DownloadStatus.DOWNLOADED);
                        selector.orderBy(DownloaderConfig.DOWNLOAD_FINISH_TIME, true);
                        mDownLoadedList = mEntityManager.findAll(selector);
                        mDownloadedAdapter.setData(mDownLoadedList);
                        mDownloadingAdapter.notifyDataSetChanged();
                        mDownloadedAdapter.notifyDataSetChanged();
                        mDownLoadingNumber = mDownloadingList.size();
                        mDownLoadedNumber = mDownLoadedList.size();
                        mDownloadingNumTv.setText(getResources().getString(R.string.number_format, mDownLoadingNumber));
                        mDownloadedNumTv.setText(getResources().getString(R.string.number_format, mDownLoadedNumber));

                        if (mDownloadingList.size() == NO_SONG) {
                            mDownloadingRl.setVisibility(View.GONE);
                        } else {
                            mDownloadingRl.setVisibility(View.VISIBLE);
                        }

                        if (mDownLoadedList.size() == NO_SONG) {
                            mDownloadedLl.setVisibility(View.GONE);
                        } else {
                            mDownloadedLl.setVisibility(View.VISIBLE);
                        }
                        if (mDownLoadedList.size() <= IS_EXPAND_ALL || isExpand == true){
                            mDownloadedAdapter.setReturnCount(mDownLoadedList.size());
                            mExpandLl.setVisibility(View.GONE);
                        } else {
                            mDownloadedAdapter.setReturnCount(IS_EXPAND_ALL);
                            mExpandLl.setVisibility(View.VISIBLE);
                            isExpand = false;
                        }

                    }
                });

            }
        }).start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEntityManager = DBUtils.getDownloadTaskManager(MusicApplication.getApp(), null);
        mMySubscriber = new MySubscriber();
        NotificationCenter.defaultCenter().subscriber("DOWNLOAD_SUBSCRISBER", mMySubscriber);
    }

    @Override
    public void onDestroy() {
        if (mIDownloader != null) {
            if (listener != null) {
                mIDownloader.removeDownloadListener(listener);
            }
        }
        if (mMySubscriber != null) {
            NotificationCenter.defaultCenter().unsubscribe("DOWNLOAD_SUBSCRISBER", mMySubscriber);
            mMySubscriber = null;
        }

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.downloading_all:
                break;
            case R.id.downloaded_expand:
                mDownloadedAdapter.setReturnCount(mDownLoadedList.size());
                mExpandLl.setVisibility(View.GONE);
                isExpand = true;
                break;

        }
    }

    public boolean fileIsExists(String s) {
        try {
            File f = new File(s);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    class MySubscriber implements TopicSubscriber {
        @Override
        public void onEvent(String s, Object o) {
            DownloadTask downloadingMusicBean = (DownloadTask) o;
            if (downloadingMusicBean.action.equals(DOWNLOADED)) {
                refreshData();
            }
        }
    }

}
