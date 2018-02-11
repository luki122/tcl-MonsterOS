package cn.tcl.music.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.adapter.LocalMediaAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.fragments.LocalAlbumDetailFragment;
import cn.tcl.music.widget.ActionModeHandler;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.toolbar.Toolbar;

public class LocalAlbumDetailActivity extends BaseMusicActivity implements LocalMediaAdapter.IonSlidingViewClickListener {

    private static final String TAG = LocalAlbumDetailActivity.class.getSimpleName();
    private LocalAlbumDetailFragment mLocalAlbumDetailFragment;

    private ImageView mDetailImageView;
    private TextView mTitleTextView;
    private TextView mAlbumtimeText;
    private TextView mEmptyTextView;
    private ImageView mArtworkImageView;
    private RelativeLayout mAlbumInformationRelativeLayout;
    private FrameLayout mSongsListFrameLayout;
    private String mTitle = "";
    private String mArtistName = "";
    private String artwork = "";
    private ActionModeHandler mActionModeHandler;

    public LocalAlbumDetailFragment.OnMediaFragmentSelectedListener OnMediaFragmentSelectedListener = new LocalAlbumDetailFragment.OnMediaFragmentSelectedListener() {
        @Override
        public void onAudioSelectdNum(ArrayList<Integer> songIds) {
            if (mActionModeHandler == null) {
                mActionModeHandler = new ActionModeHandler(LocalAlbumDetailActivity.this);
            }
            mActionModeHandler.setItemNum(songIds);
        }
    };

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_album_detail);
    }

    @Override
    protected Activity getMainActivity() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        mTitle = bundle.getString(CommonConstants.BUNDLE_KEY_ALBUM_NAME);
        mArtistName = bundle.getString(CommonConstants.BUNDLE_KEY_ARTIST);
        artwork = bundle.getString(CommonConstants.BUNDLE_KEY_ARTWORK);
        initView(mArtistName);
        initToolBar(mTitle);
        mActionModeHandler = new ActionModeHandler(this);
    }

    private void initView(String title) {
        mArtworkImageView = (ImageView) findViewById(R.id.artwork_image_view);
        mDetailImageView = (ImageView) findViewById(R.id.detail_play_all_image);
        mTitleTextView = (TextView) findViewById(R.id.detail_title_tv);
        mAlbumtimeText = (TextView) findViewById(R.id.detail_album_time);
        mEmptyTextView = (TextView) findViewById(R.id.album_empty_tv);
        mAlbumInformationRelativeLayout = (RelativeLayout) findViewById(R.id.detail_rl1);
        mSongsListFrameLayout = (FrameLayout) findViewById(R.id.detail_up_external_container);
        Glide.with(this)
                .load(artwork)
                .placeholder(R.drawable.empty_album)
                .into(mArtworkImageView);

        mLocalAlbumDetailFragment = new LocalAlbumDetailFragment();
        String tag = "DetailFragment";
        mLocalAlbumDetailFragment.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().replace(R.id.detail_up_external_container,
                mLocalAlbumDetailFragment, tag).commit();
        if (TextUtils.isEmpty(title) || title.equals("<unknown>")) {
            mTitleTextView.setText(getResources().getString(R.string.unknown));
        } else {
            mTitleTextView.setText(title);
        }
    }

    private void initToolBar(String toobarName) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        toolbar.inflateMenu(R.menu.memu_other_music);
        toolbar.setTitle(toobarName);
        toolbar.setTitleTextAppearance(LocalAlbumDetailActivity.this,R.style.ToolbarTitle);
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        setActionModeListener(new ActionModeListener() {
            @Override
            public void onActionItemClicked(ActionMode.Item item) {
                switch (item.getItemId()) {
                    case ActionMode.NAGATIVE_BUTTON:
                        mLocalAlbumDetailFragment.leaveMultiChoose();
                        break;
                    case ActionMode.POSITIVE_BUTTON:
                        if (mLocalAlbumDetailFragment.isSelectAll()) {
                            getActionMode().setPositiveText(getResources().getString(R.string.select_all));
                            mLocalAlbumDetailFragment.selectAll(false);
                        } else {
                            getActionMode().setPositiveText(getResources().getString(R.string.cancel_select_all));
                            mLocalAlbumDetailFragment.selectAll(true);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onActionModeShow(ActionMode actionMode) {
            }

            @Override
            public void onActionModeDismiss(ActionMode actionMode) {
            }
        });
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.other_setting:
                    Intent intent = new Intent(LocalAlbumDetailActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCurrentMusicMetaChanged() {
        mLocalAlbumDetailFragment.onCurrentMetaChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onItemClick(View view, int position) {
        mLocalAlbumDetailFragment.clickItem(position);
    }

    @Override
    public void onDeleteBtnClick(View view, int position) {
        mLocalAlbumDetailFragment.deleteItem(position);
    }

    public void showEmpty() {
        mEmptyTextView.setVisibility(View.VISIBLE);
        mAlbumInformationRelativeLayout.setVisibility(View.GONE);
        mSongsListFrameLayout.setVisibility(View.GONE);
    }

    public void hideEmpty() {
        mEmptyTextView.setVisibility(View.GONE);
        mAlbumInformationRelativeLayout.setVisibility(View.VISIBLE);
        mSongsListFrameLayout.setVisibility(View.VISIBLE);
    }

    public void goToMultiChoose() {
        mLocalAlbumDetailFragment.mBatchOperateLinearLayout.setVisibility(View.VISIBLE);
        mLocalAlbumDetailFragment.noclickableplayall();
        showActionMode(true);
        getActionMode().setPositiveText(getResources().getString(R.string.select_all));
    }

    @Override
    public void onBackPressed() {
        if (getActionMode().isShowing()) {
            mLocalAlbumDetailFragment.leaveMultiChoose();
            return;
        }
        super.onBackPressed();
    }

    public void setSelectedNumber(int selectedNum) {
        String format = getResources().getString(R.string.batch_songs_num);
        getActionMode().setTitle(String.format(format, selectedNum));
    }

}