package cn.tcl.music.widget;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tcl.framework.log.NLog;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.LocalAlbumDetailActivity;
import cn.tcl.music.activities.LocalAlbumListActivity;
import cn.tcl.music.activities.LocalMusicActivity;
import cn.tcl.music.activities.MyFavouriteMusicActivity;
import cn.tcl.music.activities.PlaylistDetailActivity;
import cn.tcl.music.activities.RecentlyPlayActivity;
import cn.tcl.music.fragments.LocalAlbumDetailFragment;
import cn.tcl.music.fragments.LocalAlbumListFragment;
import cn.tcl.music.fragments.LocalArtistsFragment;
import cn.tcl.music.fragments.LocalMediaFragment;
import cn.tcl.music.fragments.MyFavouriteMusicFragment;
import cn.tcl.music.fragments.PlaylistDetailFragment;
import cn.tcl.music.fragments.RecentlyPlayFragment;
import cn.tcl.music.model.MediaInfo;

/* MODIFIED-BEGIN by Kang.Ren, 2016-11-07,BUG-3373604*/
/* MODIFIED-END by Kang.Ren,BUG-3373604*/

public class ActionModeHandler implements Callback, View.OnClickListener {

    private static final String TAG = ActionModeHandler.class.getSimpleName();

    private final int UNSPECIFIED = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    private final int LOCKED = ActivityInfo.SCREEN_ORIENTATION_LOCKED;
    private static final int LOCALMEDIA_FRAGMENT_FLAG = 0;
    private static final int ARTISTS_FRAGMENT_FLAG = 2;
    private static final int ALBUMS_FRAGMENT_FLAG = 3;
    private static final int DETAILS_FRAGMENT_FLAG = 4;
    private Activity activity = null;
    private List<Integer> selectedSongIds = new ArrayList<Integer>();
    public static ActionMode mActionMode;
    private MediaInfo mPlayListInfo;
    private MediaInfo tmpPlayListInfo;
    private Menu actionBarMenu;
    private TextView mToolbarCancelTextView;
    private TextView mToolbarSelectAllTextView;
    private boolean mIsSelectAllMode;
    private static int sFragmentFlag = 0;

    public ActionModeHandler(Activity activity) {
        this.activity = activity;
    }

    public ActionMode startActionMode(MediaInfo info) {
        if (activity != null) {
            mActionMode = this.activity.startActionMode(this);
            View customView = LayoutInflater.from(this.activity).inflate(
                    R.layout.ac_mode_cus_view, null);
//        View actionModeLayout = (View) customView.findViewById(R.id.action_mode_bar_layout);
//        actionModeLayout.getLayoutParams().width = getScreenWidth(this.activity);
//        customView.getLayoutParams().width = getScreenWidth(this.activity);
            mToolbarCancelTextView = (TextView) customView.findViewById(R.id.toolbar_cancel_tv);
            mToolbarSelectAllTextView = (TextView) customView.findViewById(R.id.toolbar_select_all_tv);
            mToolbarCancelTextView.setOnClickListener(this);
            mToolbarSelectAllTextView.setOnClickListener(this);
            mActionMode.setCustomView(customView);
            mPlayListInfo = copyMedia(info);
            tmpPlayListInfo = copyMedia(info);
            NLog.d(TAG, "startActionMode mPlayListInfo = " + mPlayListInfo + "");
            if (null != activity && activity instanceof LocalMusicActivity) {
                LocalMusicActivity localMusicActivity = (LocalMusicActivity) activity;
                localMusicActivity.setMultiMode(true);               // enter into multiMode
                localMusicActivity.getActionBar().hide();     // hide top action bar
                localMusicActivity.mPlayingButton.setVisibility(View.GONE);
                int backButtonId = R.id.action_mode_close_button;
                AppCompatImageView actionBackImageView = (AppCompatImageView) localMusicActivity.findViewById(backButtonId);
                actionBackImageView.setVisibility(View.GONE);


                localMusicActivity.setRequestedOrientation(LOCKED);   // set to PORTRAIT
                Fragment localMusicActivityCurrentFragment = localMusicActivity.getCurrentFragment();
                if (localMusicActivityCurrentFragment != null && localMusicActivityCurrentFragment instanceof LocalMediaFragment) {
                    ((LocalMediaFragment) localMusicActivityCurrentFragment).showBottomActionLayout();
                    ((LocalMediaFragment) localMusicActivityCurrentFragment).hideDownloadManager();
                    ((LocalMediaFragment) localMusicActivityCurrentFragment).noclickableplayall();
                } else if (localMusicActivityCurrentFragment instanceof LocalArtistsFragment) {
                    ((LocalArtistsFragment) localMusicActivityCurrentFragment).showBottomActionLayout();
                }
            } else if (activity instanceof LocalAlbumListActivity) { // MODIFIED by Kang.Ren, 2016-11-07,BUG-3373604
                LocalAlbumListActivity localAlbumListActivity = (LocalAlbumListActivity) activity;
                localAlbumListActivity.getActionBar().hide();     // hide top action bar

                int backButtonId = R.id.action_mode_close_button;
                AppCompatImageView actionBackImageView = (AppCompatImageView) localAlbumListActivity.findViewById(backButtonId);
                actionBackImageView.setVisibility(View.GONE);
                localAlbumListActivity.mPlayingButton.setVisibility(View.GONE);

                localAlbumListActivity.setRequestedOrientation(LOCKED);   // set to PORTRAIT
                Fragment albumListFragment = localAlbumListActivity.getFragmentManager().findFragmentByTag("LocalAlbumListActivity");    // refresh PagerFragment
                if (null != albumListFragment && albumListFragment instanceof LocalAlbumListFragment) {
                    ((LocalAlbumListFragment) albumListFragment).setMultiMode(true);
                    ((LocalAlbumListFragment) albumListFragment).showBottomActionLayout();
                    ((LocalAlbumListFragment) albumListFragment).noclickableplayall();
                    sFragmentFlag = ALBUMS_FRAGMENT_FLAG;
                }
            } else if (activity instanceof LocalAlbumDetailActivity) {
                LocalAlbumDetailActivity detailActivity = (LocalAlbumDetailActivity) activity;
                detailActivity.getActionBar().hide();     // hide top action bar

                int backButtonId = R.id.action_mode_close_button;
                AppCompatImageView actionBackImageView = (AppCompatImageView) detailActivity.findViewById(backButtonId);
                actionBackImageView.setVisibility(View.GONE);
                detailActivity.mPlayingButton.setVisibility(View.GONE);

                detailActivity.setRequestedOrientation(LOCKED);   // set to PORTRAIT
                Fragment detailFragment = detailActivity.getFragmentManager().findFragmentByTag("DetailFragment");    // refresh PagerFragment
                if (null != detailFragment && detailFragment instanceof LocalAlbumDetailFragment) {
                    ((LocalAlbumDetailFragment) detailFragment).setMultiMode(true);
                    ((LocalAlbumDetailFragment) detailFragment).showBottomActionLayout();
                    ((LocalAlbumDetailFragment) detailFragment).noclickableplayall();
                    sFragmentFlag = DETAILS_FRAGMENT_FLAG;
                }
            } else if (activity instanceof MyFavouriteMusicActivity) {
                MyFavouriteMusicActivity myFavouriteMusicActivity = (MyFavouriteMusicActivity) activity;
                myFavouriteMusicActivity.getActionBar().hide();     // hide top action bar

                int backButtonId = R.id.action_mode_close_button;
                AppCompatImageView actionBackImageView = (AppCompatImageView) myFavouriteMusicActivity.findViewById(backButtonId);
                actionBackImageView.setVisibility(View.GONE);
                myFavouriteMusicActivity.mPlayingButton.setVisibility(View.GONE);
                myFavouriteMusicActivity.setRequestedOrientation(LOCKED);   // set to PORTRAIT
                Fragment myFavouriteMusicFragment = myFavouriteMusicActivity.getFragmentManager().findFragmentByTag("MyFavouriteMusicFragment");    // refresh PagerFragment
                if (null != myFavouriteMusicFragment && myFavouriteMusicFragment instanceof MyFavouriteMusicFragment) {
                    ((MyFavouriteMusicFragment) myFavouriteMusicFragment).setMultiMode(true);
                    ((MyFavouriteMusicFragment) myFavouriteMusicFragment).showBottomActionLayout();
                    ((MyFavouriteMusicFragment) myFavouriteMusicFragment).noclickableplayall();
                    sFragmentFlag = LOCALMEDIA_FRAGMENT_FLAG;

                }
            } else if (activity instanceof PlaylistDetailActivity) {
                PlaylistDetailActivity playListDetailActivity = (PlaylistDetailActivity) activity;
                playListDetailActivity.getActionBar().hide();     // hide top action bar

                int backButtonId = R.id.action_mode_close_button;
                AppCompatImageView actionBackImageView = (AppCompatImageView) playListDetailActivity.findViewById(backButtonId);
                actionBackImageView.setVisibility(View.GONE);
                playListDetailActivity.mPlayingButton.setVisibility(View.GONE);
                playListDetailActivity.setRequestedOrientation(LOCKED);   // set to PORTRAIT
                Fragment playListDetailFragment = playListDetailActivity.getFragmentManager()
                        .findFragmentByTag(PlaylistDetailActivity.class.getSimpleName());    // refresh PagerFragment
                if (null != playListDetailFragment && playListDetailFragment instanceof PlaylistDetailFragment) {
                    ((PlaylistDetailFragment) playListDetailFragment).setMultiMode(true);
                    ((PlaylistDetailFragment) playListDetailFragment).showBottomActionLayout();
                    ((PlaylistDetailFragment) playListDetailFragment).noclickableplayall();
                    sFragmentFlag = LOCALMEDIA_FRAGMENT_FLAG;
                }
            } else if (activity instanceof RecentlyPlayActivity) {
                RecentlyPlayActivity recentlyPlayActivity = (RecentlyPlayActivity) activity;
                recentlyPlayActivity.getActionBar().hide();     // hide top action bar

                int backButtonId = R.id.action_mode_close_button;
                AppCompatImageView actionBackImageView = (AppCompatImageView) recentlyPlayActivity.findViewById(backButtonId);
                actionBackImageView.setVisibility(View.GONE);
                recentlyPlayActivity.mPlayingButton.setVisibility(View.GONE);
                recentlyPlayActivity.setRequestedOrientation(LOCKED);   // set to PORTRAIT
                Fragment recentlyFragment = recentlyPlayActivity.getFragmentManager()
                        .findFragmentByTag(RecentlyPlayFragment.class.getSimpleName());    // refresh PagerFragment
                if (null != recentlyFragment && recentlyFragment instanceof RecentlyPlayFragment) {
                    ((RecentlyPlayFragment) recentlyFragment).setMultiMode(true);
                    ((RecentlyPlayFragment) recentlyFragment).showBottomActionLayout();
                    ((RecentlyPlayFragment) recentlyFragment).noclickableplayall();
                    sFragmentFlag = LOCALMEDIA_FRAGMENT_FLAG;
                }
            }
            return mActionMode;
        } else {
            return null;
        }
    }

    private void exitActionMode() {
        if (null != selectedSongIds) {
            selectedSongIds.clear();
        }
        removeSelectAll();
        sFragmentFlag = 0;
        if (null != activity && (activity instanceof LocalMusicActivity)) {
            LocalMusicActivity localMusicActivity = (LocalMusicActivity) activity;
            localMusicActivity.setMultiMode(false); // exit MultiMode
//            localMusicActivity.showNavigationDrawerFragment();   // show left action bar
            localMusicActivity.getActionBar().show();// show top action bar
//            localMusicActivity.postManageNowPlayingFragmentVisibity(); // show bottom playing bar
            localMusicActivity.setRequestedOrientation(UNSPECIFIED);  // exit PORTRAIT
            localMusicActivity.mPlayingButton.setVisibility(View.VISIBLE);
            Fragment localMusicActivityCurrentFragment = localMusicActivity.getCurrentFragment();
            if (localMusicActivityCurrentFragment != null && localMusicActivityCurrentFragment instanceof LocalMediaFragment) {
                ((LocalMediaFragment) localMusicActivityCurrentFragment).setMultiMode(false, -1);
                ((LocalMediaFragment) localMusicActivityCurrentFragment).hideBottomActionLayout();
                ((LocalMediaFragment) localMusicActivityCurrentFragment).showDownloadManager();
                ((LocalMediaFragment) localMusicActivityCurrentFragment).clickableplayall();
            } else if (localMusicActivityCurrentFragment instanceof LocalArtistsFragment) {
                ((LocalArtistsFragment)localMusicActivityCurrentFragment).setMultiMode(false, -1);
                ((LocalArtistsFragment)localMusicActivityCurrentFragment).hideBottomActionLayout();
            }
        } else if (activity instanceof LocalAlbumListActivity) { // MODIFIED by Kang.Ren, 2016-11-07,BUG-3373604
            LocalAlbumListActivity localAlbumListActivity = (LocalAlbumListActivity)activity;
//            localMusicActivity.showNavigationDrawerFragment();   // show left action bar
            localAlbumListActivity.getActionBar().show();// show top action bar
//            localMusicActivity.postManageNowPlayingFragmentVisibity(); // show bottom playing bar
            localAlbumListActivity.setRequestedOrientation(UNSPECIFIED);  // exit PORTRAIT
            localAlbumListActivity.mPlayingButton.setVisibility(View.VISIBLE);
            Fragment albumListFragment = localAlbumListActivity.getFragmentManager().findFragmentByTag("LocalAlbumListActivity");    // refresh PagerFragment
            if(null != albumListFragment && albumListFragment instanceof LocalAlbumListFragment) {
                ((LocalAlbumListFragment) albumListFragment).setMultiMode(false);
                ((LocalAlbumListFragment) albumListFragment).hideBottomActionLayout();
                ((LocalAlbumListFragment) albumListFragment).clickableplayall();
            }
        } else if (activity instanceof LocalAlbumDetailActivity){
            LocalAlbumDetailActivity detailActivity = (LocalAlbumDetailActivity)activity;
            detailActivity.getActionBar().show();     // hide top action bar
            detailActivity.setRequestedOrientation(UNSPECIFIED);   // set to PORTRAIT
            detailActivity.mPlayingButton.setVisibility(View.VISIBLE);
            Fragment detailFragment = detailActivity.getFragmentManager().findFragmentByTag("DetailFragment");    // refresh PagerFragment
            if(null != detailFragment && detailFragment instanceof LocalAlbumDetailFragment) {
                ((LocalAlbumDetailFragment) detailFragment).setMultiMode(false);
                ((LocalAlbumDetailFragment) detailFragment).hideBottomActionLayout();
                ((LocalAlbumDetailFragment) detailFragment).clickableplayall();
            }
        } else if (activity instanceof MyFavouriteMusicActivity) {
            MyFavouriteMusicActivity myFavouriteMusicActivity = (MyFavouriteMusicActivity) activity;
            myFavouriteMusicActivity.getActionBar().show();     // hide top action bar
            myFavouriteMusicActivity.setRequestedOrientation(UNSPECIFIED);   // set to PORTRAIT
            myFavouriteMusicActivity.mPlayingButton.setVisibility(View.VISIBLE);
            myFavouriteMusicActivity.setMyFavoriteTitleEnable(true);
            Fragment myFavouriteMusicFragment = myFavouriteMusicActivity.getFragmentManager().findFragmentByTag("MyFavouriteMusicFragment");    // refresh PagerFragment
            if (null != myFavouriteMusicFragment && myFavouriteMusicFragment instanceof MyFavouriteMusicFragment) {
                ((MyFavouriteMusicFragment) myFavouriteMusicFragment).setMultiMode(false);
                ((MyFavouriteMusicFragment) myFavouriteMusicFragment).hideBottomActionLayout();
                ((MyFavouriteMusicFragment) myFavouriteMusicFragment).clickableplayall();
            }
        } else if (activity instanceof PlaylistDetailActivity) {
            PlaylistDetailActivity playListDetailActivity = (PlaylistDetailActivity) activity;
            playListDetailActivity.getActionBar().show();     // hide top action bar
            playListDetailActivity.setRequestedOrientation(UNSPECIFIED);   // set to PORTRAIT
            playListDetailActivity.mPlayingButton.setVisibility(View.VISIBLE);
            playListDetailActivity.setPlayListTitleEnable(true);
            Fragment playListDetailFragment = playListDetailActivity.getFragmentManager()
                    .findFragmentByTag(PlaylistDetailActivity.class.getSimpleName());    // refresh PagerFragment
            if (null != playListDetailFragment && playListDetailFragment instanceof PlaylistDetailFragment) {
                ((PlaylistDetailFragment) playListDetailFragment).setMultiMode(false);
                ((PlaylistDetailFragment) playListDetailFragment).hideBottomActionLayout();
                ((PlaylistDetailFragment) playListDetailFragment).clickableplayall();
            }
        } else if (activity instanceof RecentlyPlayActivity) {
            RecentlyPlayActivity recentlyPlayActivity = (RecentlyPlayActivity) activity;
            recentlyPlayActivity.getActionBar().show();     // hide top action bar
            recentlyPlayActivity.setRequestedOrientation(UNSPECIFIED);   // set to PORTRAIT
            recentlyPlayActivity.mPlayingButton.setVisibility(View.VISIBLE);
            Fragment recentlyFragment = recentlyPlayActivity.getFragmentManager()
                    .findFragmentByTag(RecentlyPlayFragment.class.getSimpleName());    // refresh PagerFragment
            if (null != recentlyFragment && recentlyFragment instanceof RecentlyPlayFragment) {
                ((RecentlyPlayFragment) recentlyFragment).setMultiMode(false);
                ((RecentlyPlayFragment) recentlyFragment).hideBottomActionLayout();
                ((RecentlyPlayFragment) recentlyFragment).clickableplayall();
            }
        }
    }

    public void setItemNum(ArrayList<Integer> songIds) {
        NLog.d(TAG, "setItemNum songIds = " + songIds);
        if (null != songIds) {
            onPrepareActionMode(mActionMode, actionBarMenu);  // refresh action bar icon
            View view = mActionMode.getCustomView();   // set selected song num
            TextView textView = (TextView) view.findViewById(R.id.toolbar_select_num_tv);
            String format = activity.getResources().getString(R.string.batch_songs_num);
            switch (sFragmentFlag) {
                case LOCALMEDIA_FRAGMENT_FLAG:
                    format = activity.getResources().getString(R.string.batch_songs_num);
                    break;
                case ARTISTS_FRAGMENT_FLAG:
                    format = activity.getResources().getString(R.string.batch_artists_num);
                    break;
                case ALBUMS_FRAGMENT_FLAG:
                    format = activity.getResources().getString(R.string.batch_albums_num);
                    break;
            }
            textView.setText(String.format(format, songIds.size()));
            selectedSongIds = songIds;
            mActionMode.invalidate();
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//        switch (item.getItemId()) {
//        case R.id.action_ok:
//            NLog.d(TAG, "onActionItemClicked action_ok isFavorite = " +AppConfig.isFavorite);
//
//            if (AppConfig.isFavorite){
//                addToFavoriteList_v2();  //addToFavoriteList();
//            }else {
//                addToCustomPlayList();
//            }
//
//            finishActionMode();
//            break;
//
//         default:
//            break;
//        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        exitActionMode();
    }

    public void finishActionMode() {
        if (null != mActionMode) {
            mActionMode.finish();
        }
    }

    private MediaInfo copyMedia(MediaInfo oldInfo) {
        if (null == oldInfo) {
            return null;
        }
        MediaInfo info = new MediaInfo();
        info.Id = oldInfo.audioId;
        info.title = oldInfo.title;
        info.description = oldInfo.description;
        return info;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        actionBarMenu = menu;
        MenuInflater inflater = mode.getMenuInflater();
//        inflater.inflate(R.menu.action_mode_click_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//        menu.setGroupVisible(R.id.action_menu_group, selectedSongIds.size() > 0);
        return true;
    }


    /**
     * 添加到收藏列表
     */
//    private void addToFavoriteList_v2(){
//        NLog.d(TAG, "addToFavoriteList ");
//        final MediaPlaylist favoritePlaylist = new MediaPlaylist(activity, MusicMediaDatabaseHelper.Playlists.FAVORITE_URI);
//        AsyncTask<AddPlaylistParameter, Void, Void> addFavoriteTask = new AsyncTask<PlaylistManager.AddPlaylistParameter, Void, Void>(){
//            @Override
//            protected Void doInBackground(
//                    PlaylistManager.AddPlaylistParameter... params) {
//                if(selectedSongIds != null){
//                    PlaylistManager.AddPlaylistParameter parameter = null;
//
//                    for(int i = 0; i < selectedSongIds.size(); i++){
//                        parameter = new PlaylistManager.AddPlaylistParameter();
//                        parameter.addType = PlaylistManager.AddTypes.MEDIA;
//                        parameter.id = selectedSongIds.get(i);
//                        favoritePlaylist.add(parameter);
//                    }
//
//                    Uri playlistUri = MusicMediaDatabaseHelper.Playlists.FAVORITE_URI;
//                    AddPlaylistParameter addPlaylistParameter = null;
//                    MediaPlaylist mediaPlaylist = new MediaPlaylist(activity, playlistUri);
//
//                    for(int i = 0; i < selectedSongIds.size(); i++){
//                        addPlaylistParameter = LibraryNavigationUtil.retrieveAddPlaylistParameter(ItemMediaType.FAVORITE, tmpPlayListInfo);
//                        addPlaylistParameter.id = selectedSongIds.get(i);
//                        mediaPlaylist.add(addPlaylistParameter);
//                    }
//                }
//
//
//                return null;
//            }
//        };
//        addFavoriteTask.execute();
//    }
//
//    /**
//     * 添加到自定义歌单
//     */
//
//    private void addToCustomPlayList(){
//        Uri playlistUri = MusicMediaDatabaseHelper.Playlists.CONTENT_URI.buildUpon().appendPath(String.valueOf(mPlayListInfo.audioId)).build();
//        NLog.d(TAG, "addToCustomPlayList playlistUri = " +playlistUri);
//
//        for(Integer songId : selectedSongIds) {      // add songs to playlist
//            tmpPlayListInfo.Id = songId;
//            AddPlaylistParameter addPlaylistParameter = LibraryNavigationUtil.retrieveAddPlaylistParameter(ItemMediaType.SINGLE_MEDIA, tmpPlayListInfo);
//            NLog.d(TAG, "addToCustomPlayList = " +addPlaylistParameter);
//            MediaPlaylist mediaPlaylist = new MediaPlaylist(activity, playlistUri);
//            mediaPlaylist.add(addPlaylistParameter);
//        }
//    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_cancel_tv:
                finishActionMode();
                break;

            case R.id.toolbar_select_all_tv:
                if (mIsSelectAllMode) {
                    removeSelectAll();
                } else {
                    selectAll();
                }
                break;
        }
    }

    private void selectAll() {
        if (activity instanceof LocalMusicActivity) {
            LocalMusicActivity localMusicActivity = (LocalMusicActivity) activity;
            Fragment localMusicActivityCurrentFragment = localMusicActivity.getCurrentFragment();
            if (localMusicActivityCurrentFragment != null && localMusicActivityCurrentFragment instanceof LocalMediaFragment) {
                ((LocalMediaFragment) localMusicActivityCurrentFragment).selectAll(true);
                mToolbarSelectAllTextView.setText(R.string.batch_cancel_select_all);
                mIsSelectAllMode = true;
            } else if(localMusicActivityCurrentFragment instanceof LocalArtistsFragment){
                ((LocalArtistsFragment)localMusicActivityCurrentFragment).selectAll(true);
                mToolbarSelectAllTextView.setText(R.string.batch_cancel_select_all);
                mIsSelectAllMode = true;
            }
        } else if(activity instanceof LocalAlbumListActivity){ // MODIFIED by Kang.Ren, 2016-11-07,BUG-3373604
            LocalAlbumListActivity localAlbumListActivity = (LocalAlbumListActivity)activity;
            Fragment albumListFragment = localAlbumListActivity.getFragmentManager().findFragmentByTag("LocalAlbumListActivity");    // refresh PagerFragment
            if(null != albumListFragment && albumListFragment instanceof LocalAlbumListFragment) {
                ((LocalAlbumListFragment)albumListFragment).selectAll(true);
                mToolbarSelectAllTextView.setText(R.string.batch_cancel_select_all);
                mIsSelectAllMode = true;
            }
        } else if (activity instanceof  LocalAlbumDetailActivity){
            LocalAlbumDetailActivity detailActivity = (LocalAlbumDetailActivity)activity;
            Fragment detailFragment = detailActivity.getFragmentManager().findFragmentByTag("DetailFragment");    // refresh PagerFragment
            if(null != detailFragment && detailFragment instanceof LocalAlbumDetailFragment) {
                ((LocalAlbumDetailFragment)detailFragment).selectAll(true);
                mToolbarSelectAllTextView.setText(R.string.batch_cancel_select_all);
                mIsSelectAllMode = true;
            }
        } else if (activity instanceof  MyFavouriteMusicActivity){
            MyFavouriteMusicActivity myFavouriteMusicActivity = (MyFavouriteMusicActivity)activity;
            Fragment myFavouriteMusicFragment = myFavouriteMusicActivity.getFragmentManager().findFragmentByTag("MyFavouriteMusicFragment");    // refresh PagerFragment
            if(null != myFavouriteMusicFragment && myFavouriteMusicFragment instanceof MyFavouriteMusicFragment) {
                ((MyFavouriteMusicFragment)myFavouriteMusicFragment).selectAll(true);
                mToolbarSelectAllTextView.setText(R.string.batch_cancel_select_all);
                mIsSelectAllMode = true;
            }
        } else if (activity instanceof  PlaylistDetailActivity){
            PlaylistDetailActivity playListDetailActivity = (PlaylistDetailActivity)activity;
            Fragment playListDetailFragment = playListDetailActivity.getFragmentManager()
                    .findFragmentByTag(PlaylistDetailActivity.class.getSimpleName());    // refresh PagerFragment
            if(null != playListDetailFragment && playListDetailFragment instanceof PlaylistDetailFragment) {
                ((PlaylistDetailFragment)playListDetailFragment).selectAll(true);
                mToolbarSelectAllTextView.setText(R.string.batch_cancel_select_all);
                mIsSelectAllMode = true;
            }
        } else if (activity instanceof RecentlyPlayActivity){
            RecentlyPlayActivity recentlyPlayActivity = (RecentlyPlayActivity)activity;
            Fragment recentlyFragment = recentlyPlayActivity.getFragmentManager()
                    .findFragmentByTag(RecentlyPlayFragment.class.getSimpleName());    // refresh PagerFragment
            if(null != recentlyFragment && recentlyFragment instanceof RecentlyPlayFragment) {
                ((RecentlyPlayFragment)recentlyFragment).selectAll(true);
                mToolbarSelectAllTextView.setText(R.string.batch_cancel_select_all);
                mIsSelectAllMode = true;
            }
        }
    }

    private void removeSelectAll() {
        if (activity instanceof LocalMusicActivity) {
            LocalMusicActivity localMusicActivity = (LocalMusicActivity) activity;
            Fragment localMusicActivityCurrentFragment = localMusicActivity.getCurrentFragment();
            if (localMusicActivityCurrentFragment != null && localMusicActivityCurrentFragment instanceof LocalMediaFragment) {
                ((LocalMediaFragment) localMusicActivityCurrentFragment).selectAll(false);
                mToolbarSelectAllTextView.setText(R.string.select_all);
                mIsSelectAllMode = false;
            } else if(localMusicActivityCurrentFragment instanceof LocalArtistsFragment){
                ((LocalArtistsFragment)localMusicActivityCurrentFragment).selectAll(false);
                mToolbarSelectAllTextView.setText(R.string.select_all);
                mIsSelectAllMode = false;
            }
        } else if(activity instanceof LocalAlbumListActivity){ // MODIFIED by Kang.Ren, 2016-11-07,BUG-3373604
            LocalAlbumListActivity localAlbumListActivity = (LocalAlbumListActivity)activity;
            Fragment albumListFragment = localAlbumListActivity.getFragmentManager().findFragmentByTag("LocalAlbumListActivity");    // refresh PagerFragment
            if(null != albumListFragment && albumListFragment instanceof LocalAlbumListFragment) {
                ((LocalAlbumListFragment)albumListFragment).selectAll(false);
                mToolbarSelectAllTextView.setText(R.string.select_all);
                mIsSelectAllMode = false;
            }
        } else if (activity instanceof  LocalAlbumDetailActivity){
            LocalAlbumDetailActivity detailActivity = (LocalAlbumDetailActivity)activity;
            Fragment detailFragment = detailActivity.getFragmentManager().findFragmentByTag("DetailFragment");    // refresh PagerFragment
            if(null != detailFragment && detailFragment instanceof LocalAlbumDetailFragment) {
                ((LocalAlbumDetailFragment)detailFragment).selectAll(false);
                mToolbarSelectAllTextView.setText(R.string.select_all);
                mIsSelectAllMode = false;
            }
        } else if (activity instanceof MyFavouriteMusicActivity){
            MyFavouriteMusicActivity myFavouriteMusicActivity = (MyFavouriteMusicActivity)activity;
            Fragment myFavouriteMusicFragment = myFavouriteMusicActivity.getFragmentManager().findFragmentByTag("MyFavouriteMusicFragment");    // refresh PagerFragment
            if(null != myFavouriteMusicFragment && myFavouriteMusicFragment instanceof MyFavouriteMusicFragment) {
                ((MyFavouriteMusicFragment)myFavouriteMusicFragment).selectAll(false);
                mToolbarSelectAllTextView.setText(R.string.select_all);
                mIsSelectAllMode = false;
            }
        } else if (activity instanceof PlaylistDetailActivity){
            PlaylistDetailActivity playListDetailActivity = (PlaylistDetailActivity)activity;
            Fragment playListDetailFragment = playListDetailActivity.getFragmentManager()
                    .findFragmentByTag(PlaylistDetailActivity.class.getSimpleName());    // refresh PagerFragment
            if(null != playListDetailFragment && playListDetailFragment instanceof PlaylistDetailFragment) {
                ((PlaylistDetailFragment)playListDetailFragment).selectAll(false);
                mToolbarSelectAllTextView.setText(R.string.select_all);
                mIsSelectAllMode = false;
            }
        } else if (activity instanceof RecentlyPlayActivity){
            RecentlyPlayActivity recentlyPlayActivity = (RecentlyPlayActivity)activity;
            Fragment recentlyFragment = recentlyPlayActivity.getFragmentManager()
                    .findFragmentByTag(RecentlyPlayFragment.class.getSimpleName());    // refresh PagerFragment
            if(null != recentlyFragment && recentlyFragment instanceof RecentlyPlayFragment) {
                ((RecentlyPlayFragment)recentlyFragment).selectAll(false);
                mToolbarSelectAllTextView.setText(R.string.select_all);
                mIsSelectAllMode = false;
            }
        }
    }
}
