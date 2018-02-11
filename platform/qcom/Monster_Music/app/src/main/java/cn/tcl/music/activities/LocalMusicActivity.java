package cn.tcl.music.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.adapter.LocalArtistAdapter;
import cn.tcl.music.adapter.LocalFolderAdapter;
import cn.tcl.music.adapter.LocalMediaAdapter;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.fragments.LocalArtistsFragment;
import cn.tcl.music.fragments.LocalFolderFragment;
import cn.tcl.music.fragments.LocalMediaFragment;
import cn.tcl.music.fragments.LocalScenesFragment;
import cn.tcl.music.view.CrossViewPager;
import cn.tcl.music.widget.ActionModeHandler;
import mst.widget.ActionMode;
import mst.widget.ActionModeListener;
import mst.widget.FragmentPagerAdapter;
import mst.widget.toolbar.Toolbar;

public class LocalMusicActivity extends BaseMusicActivity implements View.OnClickListener,
        CrossViewPager.OnPageChangeListener, LocalMediaAdapter.IonSlidingViewClickListener, LocalFolderAdapter.IonSlidingViewClickListener,
        LocalArtistAdapter.IonSlidingViewClickListener {
    private static final String TAG = LocalMusicActivity.class.getSimpleName();
    private static final int LOCAL_MEDIA_FRAGMENT = 0;
    private static final int LOCAL_SENSE_FRAGMENT = 1;
    private static final int LOCAL_ARTIST_FRAGMENT = 2;
    private static final int LOCAL_FOLDER_FRAGMENT = 3;

    protected CrossViewPager mViewPager;

    private TextView mSongTextView;
    private TextView mArtistTextView;
    private TextView mScenesTextView;
    private TextView mFoldersTextView;
    private int mCurrentPosition = 0;
    private List<Fragment> mFragmentList = new ArrayList<Fragment>();
    private ActionModeHandler mActionModeHandler;
    private boolean mSelectAll;

    private FragmentPagerAdapter mPagerAdapter = new FragmentPagerAdapter(getFragmentManager()) {
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }
    };

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_local_media);
    }

    @Override
    protected Activity getMainActivity() {
        return this;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initToolBar();
        mActionModeHandler = new ActionModeHandler(this);
    }

    private void initView() {
        mViewPager = (CrossViewPager) findViewById(R.id.view_pager);
        mViewPager.setScrollDisable(true);
        Fragment mediaFragment = new LocalMediaFragment();
        mFragmentList.add(mediaFragment);
        Fragment scenesFragment = new LocalScenesFragment();
        mFragmentList.add(scenesFragment);
        Fragment artistFragment = new LocalArtistsFragment();
        mFragmentList.add(artistFragment);
        Fragment folderFragment = new LocalFolderFragment();
        mFragmentList.add(folderFragment);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setPageMargin(4);
        mViewPager.setOffscreenPageLimit(4);

        mSongTextView = (TextView) findViewById(R.id.heard_song);
        mSongTextView.setOnClickListener(this);
        mArtistTextView = (TextView) findViewById(R.id.heard_singer);
        mArtistTextView.setOnClickListener(this);
        mScenesTextView = (TextView) findViewById(R.id.heard_scenes);
        mScenesTextView.setOnClickListener(this);
        mFoldersTextView = (TextView) findViewById(R.id.heard_folder);
        mFoldersTextView.setOnClickListener(this);
        mViewPager.setOnPageChangeListener(this);
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.local_music_toolbar);
        toolbar.inflateMenu(R.menu.menu_local_music);
        toolbar.setTitle(getResources().getString(R.string.local));
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTitle);
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
                        if (mCurrentPosition == LOCAL_ARTIST_FRAGMENT) {
                            ((LocalArtistsFragment) getCurrentFragment()).leaveMultiChoose();
                        } else if (mCurrentPosition == LOCAL_MEDIA_FRAGMENT) {
                            ((LocalMediaFragment) getCurrentFragment()).leaveMultiChoose();
                        }
                        break;
                    case ActionMode.POSITIVE_BUTTON:
                        if (mCurrentPosition == LOCAL_ARTIST_FRAGMENT) {
                            if (((LocalArtistsFragment) getCurrentFragment()).isSelectAll()) {
                                getActionMode().setPositiveText(getResources().getString(R.string.select_all));
                                ((LocalArtistsFragment) getCurrentFragment()).selectAll(false);
                            } else {
                                getActionMode().setPositiveText(getResources().getString(R.string.cancel_select_all));
                                ((LocalArtistsFragment) getCurrentFragment()).selectAll(true);
                            }
                        } else if (mCurrentPosition == LOCAL_MEDIA_FRAGMENT) {
                            if (((LocalMediaFragment) getCurrentFragment()).isSelectAll()) {
                                getActionMode().setPositiveText(getResources().getString(R.string.select_all));
                                ((LocalMediaFragment) getCurrentFragment()).selectAll(false);
                            } else {
                                getActionMode().setPositiveText(getResources().getString(R.string.cancel_select_all));
                                ((LocalMediaFragment) getCurrentFragment()).selectAll(true);
                            }
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onCurrentMusicMetaChanged() {
        try {
            ((LocalMediaFragment) mPagerAdapter.getItem(0)).onCurrentMetaChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_search:
                    Intent searchIntent = new Intent(LocalMusicActivity.this, SearchActivity.class);
                    startActivity(searchIntent);
                    break;
                case R.id.action_setting:
                    Intent intent = new Intent(LocalMusicActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

    @Override
    public void onClick(View view) {
        if (mIsMultiMode) {
            return;
        }
        mSongTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
        mScenesTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
        mArtistTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
        mFoldersTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);

        int id = view.getId();
        switch (id) {
            case R.id.heard_song:
                mSongTextView.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
                mViewPager.setCurrentItem(LOCAL_MEDIA_FRAGMENT, false);
                break;
            case R.id.heard_scenes:
                mScenesTextView.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
                mViewPager.setCurrentItem(LOCAL_SENSE_FRAGMENT, false);
                break;
            case R.id.heard_singer:
                mArtistTextView.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
                mViewPager.setCurrentItem(LOCAL_ARTIST_FRAGMENT, false);
                break;
            case R.id.heard_folder:
                mFoldersTextView.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
                mViewPager.setCurrentItem(LOCAL_FOLDER_FRAGMENT, false);
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPosition = position;
        mSongTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
        mScenesTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
        mArtistTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);
        mFoldersTextView.setAlpha(CommonConstants.VIEW_USER_INTERFACE_DISABLE_ALPHA);

        if (position == LOCAL_MEDIA_FRAGMENT) {
            mSongTextView.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
            LocalMediaFragment fragment = (LocalMediaFragment) getCurrentFragment();
            fragment.refreshData();
        } else if (position == LOCAL_SENSE_FRAGMENT) {
            mScenesTextView.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
        } else if (position == LOCAL_ARTIST_FRAGMENT) {
            mArtistTextView.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
            LocalArtistsFragment fragment = (LocalArtistsFragment) getCurrentFragment();
            fragment.refreshData();
        } else if (position == LOCAL_FOLDER_FRAGMENT) {
            mFoldersTextView.setAlpha(CommonConstants.VIEW_LOCAL_SELECTER_TITLE_ALPHA);
            LocalFolderFragment fragment = (LocalFolderFragment) getCurrentFragment();
            fragment.refreshData();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public Fragment getCurrentFragment() {
        return mPagerAdapter.getItem(mCurrentPosition);
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG, "onItemClick and current fragment index is " + mCurrentPosition + " and position is " + position);
        if (mCurrentPosition == LOCAL_MEDIA_FRAGMENT) {
            LocalMediaFragment fragment = (LocalMediaFragment) getCurrentFragment();
            if (null != fragment) {
                fragment.clickItem(position);
            } else {
                Log.d(TAG, "fragment is null");
            }
        } else if (mCurrentPosition == LOCAL_SENSE_FRAGMENT) {
//            LocalScenesFragment fragment = (LocalScenesFragment) pagerFragment.getCurrentFragment();
        } else if (mCurrentPosition == LOCAL_ARTIST_FRAGMENT) {
            LocalArtistsFragment fragment = (LocalArtistsFragment) getCurrentFragment();
            if (null != fragment) {
                fragment.clickItem(position);
            } else {
                Log.d(TAG, "artist fragment is null");
            }
        } else if (mCurrentPosition == LOCAL_FOLDER_FRAGMENT) {
            LocalFolderFragment fragment = (LocalFolderFragment) getCurrentFragment();
            if (null != fragment) {
                fragment.clickItem(position);
            } else {
                Log.d(TAG, "folder fragment is null");
            }
        }
    }

    @Override
    public void onDeleteBtnClick(View view, int position) {
        Log.d(TAG, "onDeleteBtnClick and current fragment index is " + mCurrentPosition + " and position is " + position);
        Fragment localMusicActivityCurrentFragment = this.getCurrentFragment();
        if (localMusicActivityCurrentFragment != null &&
                localMusicActivityCurrentFragment instanceof LocalMediaFragment) {
            ((LocalMediaFragment) localMusicActivityCurrentFragment).deleteItem(position);
        } else if (localMusicActivityCurrentFragment instanceof LocalArtistsFragment) {
            ((LocalArtistsFragment) localMusicActivityCurrentFragment).deleteItem(position);
        } else if (localMusicActivityCurrentFragment instanceof LocalFolderFragment) {
            ((LocalFolderFragment) localMusicActivityCurrentFragment).deleteItem(position);
        }
    }

    protected boolean mIsMultiMode = false;

    public void setMultiMode(boolean isMultiMode) {
        this.mIsMultiMode = isMultiMode;
    }

    public LocalMediaFragment.OnMediaFragmentSelectedListener OnMediaFragmentSelectedListener = new LocalMediaFragment.OnMediaFragmentSelectedListener() {
        @Override
        public void onAudioSelectdNum(ArrayList<Integer> songIds) {
            if (mActionModeHandler == null) {
                mActionModeHandler = new ActionModeHandler(LocalMusicActivity.this);
            }
            mActionModeHandler.setItemNum(songIds);
        }
    };

    @Override
    public void onBackPressed() {
        if (getActionMode().isShowing()) {
            if (mCurrentPosition == LOCAL_ARTIST_FRAGMENT) {
                ((LocalArtistsFragment) getCurrentFragment()).leaveMultiChoose();
            } else if (mCurrentPosition == LOCAL_MEDIA_FRAGMENT) {
                ((LocalMediaFragment) getCurrentFragment()).leaveMultiChoose();
            }
            return;
        }
        super.onBackPressed();
    }

    public void setSelectedNumber(int selectedNum) {
        String format = getResources().getString(R.string.batch_songs_num);
        if (mCurrentPosition == LOCAL_ARTIST_FRAGMENT) {
            format = getResources().getString(R.string.batch_artists_num);
        }
        getActionMode().setTitle(String.format(format, selectedNum));
    }
}

