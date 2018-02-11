package cn.tcl.music.activities.live;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.download.mie.base.util.DownloadManager;
import cn.download.mie.downloader.IDownloader;
import cn.tcl.music.R;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.LiveMusicPlayTask;
import cn.tcl.music.util.live.OnlineUtil;
import mst.app.MstActivity;

public class NetworkBatchActivity extends MstActivity implements View.OnClickListener {
    private static final String TAG = "NetworkBatchActivity";
    private Button buttonOperate;
    private RadioButton mSelectAll;
    private TextView cancleSelect;
    private Button DeleteButton;
    private View customView;
    private ListView mListView;
    private NetworkBatchAdapter mAdapter;
    private TextView network_text_select_all;
    private String mAlbumName;
    private List<Integer> mflags = new ArrayList<Integer>() {
        @Override
        public Integer set(int index, Integer object) {
            if (get(index) == object) {
                return super.set(index, object);
            }
            mAdapter.updateNum((int) object == 1);
            return super.set(index, object);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle mBundle = this.getIntent().getExtras();
        String title = mBundle.getString("name");
        mAlbumName = mBundle.getString("album_name", null);
        setContentView(R.layout.network_batch_fragment);
        drawActionbar();
        initView();
        setTitle(title);
    }

    private void setTitle(String title) {
        TextView tx = (TextView) customView.findViewById(R.id.second_header_view);
        tx.setText(title);
    }

    private void initView() {
        buttonOperate = (Button) findViewById(R.id.network_button_operate);
        buttonOperate.setOnClickListener(this);
        buttonOperate.setEnabled(false);


        mSelectAll = (RadioButton) findViewById(R.id.network_select_all);
        mSelectAll.setClickable(false);

        cancleSelect = (TextView) findViewById(R.id.network_cancle_select_all);
        cancleSelect.setOnClickListener(this);

        DeleteButton = (Button) findViewById(R.id.network_button_delete);
        DeleteButton.setOnClickListener(this);
        DeleteButton.setEnabled(false);

        mListView = (ListView) findViewById(R.id.network_list);
        mflags.clear();
        if (OnlineUtil.getSongDetailData() != null) {
            for (int i = 0; i < OnlineUtil.getSongDetailData().size(); i++) {
                mflags.add(0);
            }
        }
        mAdapter = new NetworkBatchAdapter(this, OnlineUtil.getSongDetailData(), mflags);
        mAdapter.setAlbumName(mAlbumName);
        mListView.setAdapter(mAdapter);

        network_text_select_all = (TextView) findViewById(R.id.network_text_select_all);
        network_text_select_all.setOnClickListener(this);
    }

    private void drawActionbar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDefaultDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        LayoutInflater inflater = LayoutInflater.from(this);
        customView = inflater.inflate(R.layout.second_view_header, new LinearLayout(this), false);
        actionBar.setCustomView(customView);
    }

    private boolean isAddToPlayListAvailable = true;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.network_cancle_select_all:
//                if(mSelectAll.isChecked()) {
//                    mSelectAll.setChecked(false);
//                    for(int i=0;i<MusicUtils.getSongDetailData().size();i++){
//                        mflags.set(i, 0);
//                    }
//                    mAdapter.updateFlags(mflags);
//                }  else {
//                    for(int i=0;i<MusicUtils.getSongDetailData().size();i++){
//                        mflags.set(i, 0);
//                    }
//                    mAdapter.updateFlags(mflags);
//                }
                this.finish();
                break;
            case R.id.network_button_delete:
                if (isAddToPlayListAvailable) {
                    isAddToPlayListAvailable = false;
                    List<Integer> checks = mAdapter.getFlags();
                    LiveMusicPlayTask liveMusicPlayTask = new LiveMusicPlayTask(this);
                    List<SongDetailBean> Queulist = new ArrayList<SongDetailBean>();
                    for (int i = 0; i < OnlineUtil.getSongDetailData().size(); i++) {
                        if (checks.get(i) == 1) {
                            SongDetailBean song = OnlineUtil.getSongDetailData().get(i);
                            if (song != null) {
                                if (Queulist == null) Queulist = new ArrayList<SongDetailBean>();
                                Queulist.add(song);
                            }
                        }
                    }
                    liveMusicPlayTask.add2Queue(Queulist);
                    this.finish();
                }

                break;
            case R.id.network_button_operate:
                List<Integer> datas = mAdapter.getFlags();
                List<SongDetailBean> songList = new ArrayList<SongDetailBean>();
                for (int i = 0; i < OnlineUtil.getSongDetailData().size(); i++) {
                    if (datas.get(i) == 1) {
                        songList.add(OnlineUtil.getSongDetailData().get(i));
                    }
                }
                IDownloader batchDownloader = DownloadManager.getInstance(this).getDownloader();
                batchDownloader.startBatchMusicDownload(songList);
                break;
            case R.id.network_text_select_all:
                List<SongDetailBean> mSongDetailBeans = OnlineUtil.getSongDetailData();
                if (mSongDetailBeans != null) {
                    if (!mSelectAll.isChecked()) {
                        mSelectAll.setChecked(true);
                        for (int i = 0; i < mSongDetailBeans.size(); i++) {
                            mflags.set(i, 1);
                        }
                        mAdapter.updateFlags(mflags);
                        network_text_select_all.setText(this.getString(R.string.cancel));
                    } else {
                        mSelectAll.setChecked(false);
                        for (int i = 0; i < mSongDetailBeans.size(); i++) {
                            mflags.set(i, 0);
                        }
                        mAdapter.updateFlags(mflags);
                        network_text_select_all.setText(this.getString(R.string.select_all));

                    }
                }
                break;
        }
    }

    public void setCheckAll(boolean checkAll) {
        mSelectAll.setChecked(checkAll);
        network_text_select_all.setText(checkAll ? this.getString(R.string.cancel) : this.getString(R.string.select_all));
    }

    public void setBatchOperate(boolean shouldBatch) {
        buttonOperate.setEnabled(shouldBatch);
        DeleteButton.setEnabled(shouldBatch);
    }
}
