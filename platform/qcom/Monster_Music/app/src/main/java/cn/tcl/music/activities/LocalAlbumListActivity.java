package cn.tcl.music.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import cn.tcl.music.R;
import cn.tcl.music.adapter.AlbumListAdapter;
import cn.tcl.music.adapter.LocalFolderAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.fragments.LocalAlbumListFragment;
import cn.tcl.music.widget.ActionModeHandler;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.toolbar.Toolbar;

public class LocalAlbumListActivity extends BaseMusicActivity implements AlbumListAdapter.IonSlidingViewClickListener,
        LocalFolderAdapter.IonSlidingViewClickListener {

    private final static String TAG = LocalAlbumListActivity.class.getSimpleName();
    private LocalAlbumListFragment mAlbumFragment;
    private ActionModeHandler mActionModeHandler;
    private boolean mIsMultiMode = false;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_album_list);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlbumFragment = new LocalAlbumListFragment();
        mAlbumFragment.setArguments(getIntent().getExtras());
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String title = bundle.getString(CommonConstants.BUNDLE_KEY_ARTIST);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_container, mAlbumFragment, TAG).commit();
        initToolBar(title);
        mActionModeHandler = new ActionModeHandler(this);
    }

    private void initToolBar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.album_toolbar);
        toolbar.inflateMenu(R.menu.memu_other_music);
        toolbar.setTitle(title);
        toolbar.setTitleTextAppearance(LocalAlbumListActivity.this,R.style.ToolbarTitle);
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
                        mAlbumFragment.leaveMultiChoose();
                        break;
                    case ActionMode.POSITIVE_BUTTON:
                        if (mAlbumFragment.isSelectAll()) {
                            getActionMode().setPositiveText(getResources().getString(R.string.select_all));
                            mAlbumFragment.selectAll(false);
                        } else {
                            getActionMode().setPositiveText(getResources().getString(R.string.cancel_select_all));
                            mAlbumFragment.selectAll(true);
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

    public LocalAlbumListFragment.OnMediaFragmentSelectedListener OnAlbumFragmentSelectedListener = new LocalAlbumListFragment.OnMediaFragmentSelectedListener() {
        @Override
        public void onAudioSelectdNum(ArrayList<Integer> songIds) {
            if (mActionModeHandler == null) {
                mActionModeHandler = new ActionModeHandler(LocalAlbumListActivity.this);
            }
            mActionModeHandler.setItemNum(songIds);
        }
    };

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.other_setting:
                    Intent intent = new Intent(LocalAlbumListActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCurrentMusicMetaChanged() {

    }

    @Override
    public void onItemClick(View view, int position) {
        if (null != mAlbumFragment) {
            mAlbumFragment.clickItem(position);
        }
    }

    @Override
    public void onDeleteBtnClick(View view, int position) {
        Log.d(TAG,"onDeleteBtnClick and position is " + position);
        if (null != mAlbumFragment) {
            mAlbumFragment.deleteItem(position);
        }
    }

    public void goToMultiChoose() {
        mAlbumFragment.mBatchOperateLinearLayout.setVisibility(View.VISIBLE);
        showActionMode(true);
        getActionMode().setPositiveText(getResources().getString(R.string.select_all));
    }

    @Override
    public void onBackPressed() {
        if (getActionMode().isShowing()) {
            mAlbumFragment.leaveMultiChoose();
            return;
        }
        super.onBackPressed();
    }

    public void setSelectedNumber(int selectedNum) {
        String format = getResources().getString(R.string.batch_albums_num);
        getActionMode().setTitle(String.format(format, selectedNum));
    }

    public void setMultiMode(boolean isMultiMode) {
        this.mIsMultiMode = isMultiMode;
    }
}
