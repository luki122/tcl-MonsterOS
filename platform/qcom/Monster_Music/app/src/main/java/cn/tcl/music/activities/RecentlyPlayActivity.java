package cn.tcl.music.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.adapter.LocalMediaAdapter;
import cn.tcl.music.fragments.RecentlyPlayFragment;
import cn.tcl.music.widget.ActionModeHandler;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.toolbar.Toolbar;

public class RecentlyPlayActivity extends BaseMusicActivity implements LocalMediaAdapter.IonSlidingViewClickListener {
    private final static String TAG = RecentlyPlayActivity.class.getSimpleName();

    private RecentlyPlayFragment mRecentlyPlayFragment;
    private ActionModeHandler mActionModeHandler;

    public RecentlyPlayFragment.OnMediaFragmentSelectedListener mOnMediaFragmentSelectedListener =
            new RecentlyPlayFragment.OnMediaFragmentSelectedListener() {
                @Override
                public void onAudioSelectdNum(ArrayList<Integer> songIds) {
                    if (mActionModeHandler == null) {
                        mActionModeHandler = new ActionModeHandler(RecentlyPlayActivity.this);
                    }
                    mActionModeHandler.setItemNum(songIds);
                }
            };


    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_recently_play);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecentlyPlayFragment = new RecentlyPlayFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.recent_play_container, mRecentlyPlayFragment, RecentlyPlayFragment.class.getSimpleName()).commit();
        initToolBar();
    }

    @Override
    public void onCurrentMusicMetaChanged() {
        if (mRecentlyPlayFragment != null) {
            mRecentlyPlayFragment.onCurrentMetaChanged();
        }
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.recent_play_toolbar);
        toolbar.inflateMenu(R.menu.memu_other_music);
        toolbar.setTitle(getResources().getString(R.string.recent_play));
        toolbar.setTitleTextAppearance(RecentlyPlayActivity.this,R.style.ToolbarTitle);
        toolbar.setOnMenuItemClickListener(mOnMenuItemClick);
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
                        mRecentlyPlayFragment.leaveMultiChoose();
                        break;
                    case ActionMode.POSITIVE_BUTTON:
                        if (mRecentlyPlayFragment.isSelectAll()) {
                            getActionMode().setPositiveText(getResources().getString(R.string.select_all));
                            mRecentlyPlayFragment.selectAll(false);
                        } else {
                            getActionMode().setPositiveText(getResources().getString(R.string.cancel_select_all));
                            mRecentlyPlayFragment.selectAll(true);
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

    private Toolbar.OnMenuItemClickListener mOnMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.other_setting:
                    Intent intent = new Intent(RecentlyPlayActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

    public void onItemClick(View view, int position) {
        if (mRecentlyPlayFragment != null) {
            mRecentlyPlayFragment.clickItem(position);
        }
    }

    public void onDeleteBtnClick(View view, int position) {
        if (mRecentlyPlayFragment != null) {
            mRecentlyPlayFragment.deleteItem(position);
        }
    }

    public void goToMultiChoose() {
        mRecentlyPlayFragment.mBatchOperateLinearLayout.setVisibility(View.VISIBLE);
        mRecentlyPlayFragment.noclickableplayall();
        showActionMode(true);
        getActionMode().setPositiveText(getResources().getString(R.string.select_all));
    }

    @Override
    public void onBackPressed() {
        if (getActionMode().isShowing()) {
            mRecentlyPlayFragment.leaveMultiChoose();
            return;
        }
        super.onBackPressed();
    }

    public void setSelectedNumber(int selectedNum) {
        String format = getResources().getString(R.string.batch_songs_num);
        getActionMode().setTitle(String.format(format, selectedNum));
    }

}
