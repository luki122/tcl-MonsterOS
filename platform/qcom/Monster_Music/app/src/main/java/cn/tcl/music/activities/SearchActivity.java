package cn.tcl.music.activities;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MstSearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.tcl.music.R;
import cn.tcl.music.adapter.SearchHistoryAdapter;
import cn.tcl.music.adapter.live.AutoTipsAdapter;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.database.DBUtil;
import cn.tcl.music.database.MusicMediaDatabaseHelper;
import cn.tcl.music.fragments.SearchAlbumResultFragment;
import cn.tcl.music.fragments.SearchArtistResultFragment;
import cn.tcl.music.fragments.SearchSongResultFragment;
import cn.tcl.music.model.AlbumInfo;
import cn.tcl.music.model.MediaInfo;
import cn.tcl.music.model.live.LiveMusicAutoTipsBean;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveMusicAutoTipsBannerTask;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.MusicUtil;
import cn.tcl.music.util.PreferenceUtil;
import cn.tcl.music.view.CrossViewPager;
import mst.widget.FragmentPagerAdapter;

public class SearchActivity extends BaseMusicActivity implements View.OnClickListener, AdapterView.OnItemClickListener, CrossViewPager.OnPageChangeListener, ILoadData {
    private static final String TAG = SearchActivity.class.getSimpleName();
    private static final int SONG_TAB = 0;
    private static final int ARTIST_TAB = 1;
    private static final int ALBUM_TAB = 2;
    private static final int CLICK_SHOWKEYBOARD_TIME = 100;
    private static final int INITVIEW_SHOWKEYBOARD_TIME = 500;
    private static final float LOW_TRANSPARENCY = 0.3f;
    private static final float HIGH_TRANSPARENCY = 0.86f;
    //the follow tip is provided by Xiami Api,need hard code
    public static final String AUTO_TIPS_SONG = "歌曲";
    public static final String AUTO_TIPS_ARTIST = "艺人";
    public static final String AUTO_TIPS_ALBUM = "专辑";

    public static final String[] KEY_SEARCH_HISTORY = new String[]{
            "KEY_SEARCH_HISTORY_INDEX1",
            "KEY_SEARCH_HISTORY_INDEX2",
            "KEY_SEARCH_HISTORY_INDEX3",
            "KEY_SEARCH_HISTORY_INDEX4",
            "KEY_SEARCH_HISTORY_INDEX5"
    };

    private String mSearchString;
    private int mCurrentPosition = 0;
    private int mSearchHistoryCount = 0;
    private int mSearchHistoryUpdate = 0;

    private AutoTipsAdapter mAutoTipsAdapter;
    private SearchHistoryAdapter mSearchHistoryAdapter;
    private List<Fragment> mFragmentList = new ArrayList<Fragment>();
    private ArrayList<String> mSearchHistoryList = new ArrayList<String>();
    private ArrayList<Object> mLocalSearchResultList = new ArrayList<Object>();

    private ImageView mSearchBackImageView;
    private MstSearchView mSearchView;
    private TextView mSearchTextView;

    private TextView mSongTextView;
    private TextView mArtistTextView;
    private TextView mAlbumTextView;
    private ListView mSearchListView;
    private ListView mAutoTipsListView;
    protected CrossViewPager mViewPager;
    private View mSearchPagerTab;
    private LoadSearchAsync mLoadSearchAsync;
    private LiveMusicAutoTipsBannerTask mLiveMusicAutoTipsBannerTask;
    private SearchHeadClick mSearchHeadClick = new SearchHeadClick();

    private FragmentPagerAdapter mFragmentPagerAdapter = new FragmentPagerAdapter(getFragmentManager()) {
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
        setContentView(R.layout.activity_search);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        setListener();
        mSearchHistoryAdapter = new SearchHistoryAdapter(this, mSearchHistoryList);
        mSearchListView.setAdapter(mSearchHistoryAdapter);
        mAutoTipsAdapter = new AutoTipsAdapter(this);
        mAutoTipsListView.setAdapter(mAutoTipsAdapter);
    }

    @Override
    public void onCurrentMusicMetaChanged() {
        if (mFragmentList != null && !mFragmentList.isEmpty()) {
            ((SearchSongResultFragment) mFragmentList.get(0)).notifyCurrentSongChanged();
        }
    }

    private void initData() {
        mSearchHistoryCount = PreferenceUtil.getValue(this, PreferenceUtil.NODE_SEARCH_HISTORY, PreferenceUtil.KEY_SEARCH_HISTORY_COUNT, 0);
        if (mSearchHistoryCount == 0) {
            LogUtil.d(TAG, "KEY_SEARCH_HISTORY_COUNT");
            PreferenceUtil.saveValue(this, PreferenceUtil.NODE_SEARCH_HISTORY, PreferenceUtil.KEY_SEARCH_HISTORY_COUNT, 0);
            PreferenceUtil.saveValue(this, PreferenceUtil.NODE_SEARCH_HISTORY, PreferenceUtil.KEY_SEARCH_HISTORY_UPDATE, 0);
            mSearchListView.setVisibility(View.INVISIBLE);
        } else {
            mSearchHistoryUpdate = PreferenceUtil.getValue(this, PreferenceUtil.NODE_SEARCH_HISTORY, PreferenceUtil.KEY_SEARCH_HISTORY_UPDATE, -1);
        }
        int searchHistoryUpdate = mSearchHistoryUpdate - 1;
        for (int index = 0; index < mSearchHistoryCount; index++) {
            mSearchHistoryList.add(PreferenceUtil.getValue(this, PreferenceUtil.NODE_SEARCH_HISTORY, SearchActivity.KEY_SEARCH_HISTORY[searchHistoryUpdate % KEY_SEARCH_HISTORY.length], ""));
            searchHistoryUpdate--;
        }
    }

    private void setListener() {
        mSearchBackImageView.setOnClickListener(this);
        mSongTextView.setOnClickListener(mSearchHeadClick);
        mArtistTextView.setOnClickListener(mSearchHeadClick);
        mAlbumTextView.setOnClickListener(mSearchHeadClick);
        mSearchTextView.setOnClickListener(this);
        mSearchListView.setOnItemClickListener(this);
        mViewPager.setOnPageChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.search_back) {
            hideKeyboardWindow();
            finish();
        } else if (viewId == mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null)) {
            mSearchTextView.setCursorVisible(true);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showSearchResult();
        mSearchString = mSearchHistoryList.get(position);
        mSearchTextView.setText(mSearchString);
        hideKeyboardWindow();
        executeLoadSearchAsync(mSearchString);
    }

    private void executeAutoTipsBannerTask(String keyWord) {
        if (mLiveMusicAutoTipsBannerTask != null && mLiveMusicAutoTipsBannerTask.getStatus() == AsyncTask.Status.RUNNING) {
            mLiveMusicAutoTipsBannerTask.cancel(true);
        }
        mLiveMusicAutoTipsBannerTask = new LiveMusicAutoTipsBannerTask(this, this, keyWord);
        mLiveMusicAutoTipsBannerTask.executeMultiTask();
    }

    class AutoTipsBeanComparator implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            LiveMusicAutoTipsBean autoTipsBeanO1 = (LiveMusicAutoTipsBean) o1;
            LiveMusicAutoTipsBean autoTipsBeanO2 = (LiveMusicAutoTipsBean) o2;
            switch (autoTipsBeanO1.getType()) {
                case AUTO_TIPS_SONG:
                    return -1;
                case AUTO_TIPS_ARTIST:
                    switch (autoTipsBeanO2.getType()) {
                        case AUTO_TIPS_SONG:
                            return 1;
                        case AUTO_TIPS_ARTIST:
                            return 0;
                        case AUTO_TIPS_ALBUM:
                            return -1;
                    }
                case AUTO_TIPS_ALBUM:
                    return 1;
                default:
                    return -1;
            }
        }
    }

    @Override
    public void onLoadSuccess(int dataType, List datas) {
        LogUtil.d(TAG, "onLoadSuccess");
        if (datas != null && datas.size() > 0) {
            AutoTipsBeanComparator autoTipsBeanComparator = new AutoTipsBeanComparator();
            Collections.sort(datas, autoTipsBeanComparator);
            mAutoTipsAdapter.setmAutoTipsList(datas);
            mAutoTipsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoadFail(int dataType, String message) {
    }

    private void initView() {
        mSearchBackImageView = (ImageView) findViewById(R.id.search_back);
        mSearchView = (MstSearchView) findViewById(R.id.search_view);
        mSearchTextView = (TextView) mSearchView.findViewById(mSearchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null));
        mSearchTextView.setHint(R.string.search_online_hint);
        mSearchView.needHintIcon(false);
        if (mSearchView.isIconified()) {
            mSearchView.setIconified(false);
            if (mSearchView.isIconified()) {
                mSearchView.setIconified(false);
            }
        }
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                LogUtil.d(TAG, "onFocusChange b = " + b + " mSearchTextView.isCursorVisible() = " + mSearchTextView.isCursorVisible());
                if (!b) {
                    if (!mSearchTextView.isCursorVisible()) {
                        finish();
                    } else {
                        mSearchTextView.setCursorVisible(false);
                    }
                }
            }
        });
        mSearchView.setOnQueryTextListener(new MstSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                LogUtil.i(TAG, "onQueryTextSubmit: s = " + s);
                mSearchString = s.trim();
                if (!TextUtils.isEmpty(mSearchString)) {
                    if (!mSearchHistoryList.contains(mSearchString)) {
                        if (mSearchHistoryCount < KEY_SEARCH_HISTORY.length) {
                            PreferenceUtil.saveValue(SearchActivity.this, PreferenceUtil.NODE_SEARCH_HISTORY,
                                    PreferenceUtil.KEY_SEARCH_HISTORY_COUNT, ++mSearchHistoryCount);
                        }
                        PreferenceUtil.saveValue(SearchActivity.this, PreferenceUtil.NODE_SEARCH_HISTORY,
                                SearchActivity.KEY_SEARCH_HISTORY[mSearchHistoryUpdate % KEY_SEARCH_HISTORY.length], mSearchString);
                        PreferenceUtil.saveValue(SearchActivity.this, PreferenceUtil.NODE_SEARCH_HISTORY,
                                PreferenceUtil.KEY_SEARCH_HISTORY_UPDATE, ++mSearchHistoryUpdate);
                        mSearchHistoryList.add(mSearchString);
                    }
                    if (View.GONE != mSearchListView.getVisibility()) {
                        showSearchResult();
                    }
                    executeLoadSearchAsync(mSearchString);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                LogUtil.i(TAG, "onQueryTextChange: newtext = " + newText);
                if (TextUtils.isEmpty(newText.trim())) {
                    if (mSearchListView.getVisibility() != View.GONE) {
                        mSearchListView.setVisibility(View.VISIBLE);
                    } else if (mViewPager.getVisibility() != View.VISIBLE) {
                        mViewPager.setVisibility(View.VISIBLE);
                        mSearchPagerTab.setVisibility(View.VISIBLE);
                    }
                    mAutoTipsListView.setVisibility(View.INVISIBLE);
                } else {
                    if (mSearchListView.getVisibility() != View.GONE) {
                        mSearchListView.setVisibility(View.INVISIBLE);
                    }
                    if (mViewPager.getVisibility() != View.INVISIBLE) {
                        mViewPager.setVisibility(View.INVISIBLE);
                        mSearchPagerTab.setVisibility(View.INVISIBLE);
                    }
                    mAutoTipsListView.setVisibility(View.VISIBLE);
                    if (MusicApplication.isNetWorkCanUsed()) {
                        executeAutoTipsBannerTask(newText.toString().trim());
                    }
                }
                return false;
            }
        });

        mSongTextView = (TextView) findViewById(R.id.heard_song);
        mArtistTextView = (TextView) findViewById(R.id.heard_artist);
        mAlbumTextView = (TextView) findViewById(R.id.heard_album);
        mSearchListView = (ListView) findViewById(R.id.search_list_view);
        mAutoTipsListView = (ListView) findViewById(R.id.auto_tips_list_view);
        mViewPager = (CrossViewPager) findViewById(R.id.view_pager);
        mViewPager.setScrollDisable(true);
        mViewPager.setPageMargin(3);
        mViewPager.setOffscreenPageLimit(3);
        mSearchPagerTab = findViewById(R.id.search_pager_fragment);
        showKeyboardWindow(INITVIEW_SHOWKEYBOARD_TIME);
    }

    private void showKeyboardWindow(int time) {
        mSearchTextView.setCursorVisible(true);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mSearchTextView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mSearchTextView, 0);
            }
        }, time);
    }

    private void showSearchResult() {
        SearchSongResultFragment searchSongResultFragment = new SearchSongResultFragment();
        mFragmentList.add(searchSongResultFragment);
        SearchArtistResultFragment searchArtistResultFragment = new SearchArtistResultFragment();
        mFragmentList.add(searchArtistResultFragment);
        SearchAlbumResultFragment searchAlbumResultFragment = new SearchAlbumResultFragment();
        mFragmentList.add(searchAlbumResultFragment);
        mViewPager.setAdapter(mFragmentPagerAdapter);
    }

    private void hideKeyboardWindow() {
        mSearchTextView.setCursorVisible(false);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(mSearchTextView.getWindowToken(), 0);
        }
    }

    public void clearSearchHistory() {
        mSearchHistoryCount = 0;
        mSearchHistoryUpdate = 0;
        PreferenceUtil.saveValue(this, PreferenceUtil.NODE_SEARCH_HISTORY, PreferenceUtil.KEY_SEARCH_HISTORY_COUNT, 0);
        PreferenceUtil.saveValue(this, PreferenceUtil.NODE_SEARCH_HISTORY, PreferenceUtil.KEY_SEARCH_HISTORY_UPDATE, 0);
        mSearchListView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mLoadSearchAsync && mLoadSearchAsync.getStatus() == AsyncTask.Status.RUNNING) {
            mLoadSearchAsync.cancel(true);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        LogUtil.d(TAG, "onPageScrolled");
    }

    @Override
    public void onPageSelected(int position) {
        LogUtil.d(TAG, "PagerFragment onPageSelected position = " + position);
        mCurrentPosition = position;
        mSongTextView.setAlpha(LOW_TRANSPARENCY);
        mArtistTextView.setAlpha(LOW_TRANSPARENCY);
        mAlbumTextView.setAlpha(LOW_TRANSPARENCY);
        if (position == SONG_TAB) {
            mSongTextView.setAlpha(HIGH_TRANSPARENCY);
        } else if (position == ARTIST_TAB) {
            mArtistTextView.setAlpha(HIGH_TRANSPARENCY);
        } else if (position == ALBUM_TAB) {
            mAlbumTextView.setAlpha(HIGH_TRANSPARENCY);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        LogUtil.d(TAG, "onPageScrollStateChanged");
    }

    private void executeLoadSearchAsync(String searchString) {
        LogUtil.d(TAG, "datatest :  searchString = " + searchString);
        mSearchListView.setVisibility(View.GONE);
        if (View.INVISIBLE != mAutoTipsListView.getVisibility()) {
            mAutoTipsListView.setVisibility(View.INVISIBLE);
        }
        if (View.VISIBLE != mViewPager.getVisibility()) {
            mViewPager.setVisibility(View.VISIBLE);
            mSearchPagerTab.setVisibility(View.VISIBLE);
        }
        if (null == mLoadSearchAsync) {
            mLoadSearchAsync = new LoadSearchAsync(this);
            mLoadSearchAsync.execute(new String[]{searchString});
        } else {
            if (mLoadSearchAsync.getStatus() == AsyncTask.Status.RUNNING) {
                mLoadSearchAsync.cancel(true);
            }
            mLoadSearchAsync = new LoadSearchAsync(this);
            mLoadSearchAsync.execute(new String[]{searchString});
        }

    }

    private class LoadSearchAsync extends AsyncTask<String, Void, ArrayList<Object>> {
        private Context mContext;

        public LoadSearchAsync(Context context) {
            mContext = context;
        }

        @Override
        protected ArrayList<Object> doInBackground(String... params) {
            String searchKey = params[0];
            mLocalSearchResultList.clear();
            LogUtil.d(TAG, "LoadSearchAsync doInBackground and searchKey is " + searchKey);
            switch (mCurrentPosition) {
                case SONG_TAB:
                    //search local Song
                    ArrayList<Long> selectMediaIds = new ArrayList<Long>();

                    String localSongByTitleSelection = MusicMediaDatabaseHelper.Media.MediaColumns.TITLE + " like '%" + searchKey + "%'";
                    final Cursor localSongByTitleCursor = mContext.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED,
                            DBUtil.MEDIA_FOLDER_COLUMNS, localSongByTitleSelection, null, MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY);
                    if (localSongByTitleCursor != null) {
                        while (localSongByTitleCursor.moveToNext()) {
                            MediaInfo mediaInfo = MusicUtil.getMediaInfoFromCursor(localSongByTitleCursor);
                            mLocalSearchResultList.add(mediaInfo);
                            selectMediaIds.add(mediaInfo.audioId);
                        }
                        localSongByTitleCursor.close();
                    }

                    String localSongByArtistSelection = MusicMediaDatabaseHelper.Media.MediaColumns.ARTIST + " like " + "'%" + searchKey + "%'";
                    final Cursor localSongByArtistCursor = mContext.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED,
                            DBUtil.MEDIA_FOLDER_COLUMNS, localSongByArtistSelection, null, MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY);
                    if (localSongByArtistCursor != null) {
                        while (localSongByArtistCursor.moveToNext()) {
                            MediaInfo mediaInfo = MusicUtil.getMediaInfoFromCursor(localSongByArtistCursor);
                            if (!selectMediaIds.contains(mediaInfo.audioId)) {
                                mLocalSearchResultList.add(mediaInfo);
                                selectMediaIds.add(mediaInfo.audioId);
                            }
                        }
                        localSongByArtistCursor.close();
                    }

                    String localSongByAlbumSelection = MusicMediaDatabaseHelper.Media.MediaColumns.ALBUM + " like " + "'%" + searchKey + "%'";
                    final Cursor localSongByAlbumCursor = mContext.getContentResolver().query(MusicMediaDatabaseHelper.Media.CONTENT_URI_NOT_IGNORED,
                            DBUtil.MEDIA_FOLDER_COLUMNS, localSongByAlbumSelection, null, MusicMediaDatabaseHelper.Media.MediaColumns.TITLE_KEY);
                    if (null != localSongByAlbumCursor) {
                        while (localSongByAlbumCursor.moveToNext()) {
                            MediaInfo mediaInfo = MusicUtil.getMediaInfoFromCursor(localSongByAlbumCursor);
                            if (!selectMediaIds.contains(mediaInfo.audioId)) {
                                mLocalSearchResultList.add(mediaInfo);
                                selectMediaIds.add(mediaInfo.audioId);
                            }
                        }
                        localSongByAlbumCursor.close();
                    }
                    break;
                case ARTIST_TAB:
                    //search local Artist
                    String localArtistSelection = MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST + " like '%" + searchKey + "%'";
                    final Cursor localArtistCursor = mContext.getContentResolver().query(MusicMediaDatabaseHelper.Artists.CONTENT_URI,
                            null, localArtistSelection, null, MusicMediaDatabaseHelper.Artists.ArtistsColumns.ARTIST_KEY);
                    if (null != localArtistCursor) {
                        while (localArtistCursor.moveToNext()) {
                            mLocalSearchResultList.add(MusicUtil.getArtistInfoFromCursor(localArtistCursor));
                        }
                        localArtistCursor.close();
                    }
                    break;
                case ALBUM_TAB:
                    //search local Album
                    ArrayList<String> searchMediaIds = new ArrayList<String>();

                    String localAlbumByAlbumSelection = MusicMediaDatabaseHelper.Albums.AlbumColumns.ALBUM + " like '%" + searchKey + "%'";
                    final Cursor localAlbumByAlbumCursor = mContext.getContentResolver().query(MusicMediaDatabaseHelper.Albums.CONTENT_URI,
                            null, localAlbumByAlbumSelection, null, MusicMediaDatabaseHelper.Albums.AlbumColumns.ALBUM_KEY);
                    if (null != localAlbumByAlbumCursor) {
                        while (localAlbumByAlbumCursor.moveToNext()) {
                            AlbumInfo albumInfo = MusicUtil.getAlbumInfoFromCursor(localAlbumByAlbumCursor);
                            mLocalSearchResultList.add(albumInfo);
                            searchMediaIds.add(albumInfo.albumId);
                        }
                        localAlbumByAlbumCursor.close();
                    }

                    String localAlbumByArtistSelection = MusicMediaDatabaseHelper.Albums.AlbumColumns.ARTIST + " like " + "'%" + searchKey + "%'";
                    final Cursor localAlbumByArtistCursor = mContext.getContentResolver().query(MusicMediaDatabaseHelper.Albums.CONTENT_URI,
                            null, localAlbumByArtistSelection, null, MusicMediaDatabaseHelper.Albums.AlbumColumns.ALBUM_KEY);
                    if (null != localAlbumByArtistCursor) {
                        while (localAlbumByArtistCursor.moveToNext()) {
                            AlbumInfo albumInfo = MusicUtil.getAlbumInfoFromCursor(localAlbumByArtistCursor);
                            if (!searchMediaIds.contains(albumInfo.albumId)) {
                                mLocalSearchResultList.add(albumInfo);
                                searchMediaIds.add(albumInfo.albumId);
                            }
                        }
                        localAlbumByArtistCursor.close();
                    }
                    break;
                default:
                    break;
            }
            return mLocalSearchResultList;
        }

        @Override
        protected void onPostExecute(ArrayList<Object> localSearchResultList) {
            super.onPostExecute(localSearchResultList);
            LogUtil.i(TAG, "onPostExecute: mCurrentPosition " + mCurrentPosition + " and search result count is " + localSearchResultList.size());
            switch (mCurrentPosition) {
                case SONG_TAB:
                    SearchSongResultFragment searchSongResultFragment = (SearchSongResultFragment) mFragmentList.get(mCurrentPosition);
                    searchSongResultFragment.refreshSearchResult();
                    searchSongResultFragment.searchOnlineSong(mSearchString, CommonConstants.ONLINE_SEARCH_PAGE);
                    break;
                case ARTIST_TAB:
                    SearchArtistResultFragment searchArtistResultFragment = (SearchArtistResultFragment) mFragmentList.get(mCurrentPosition);
                    searchArtistResultFragment.refreshSearchResult();
                    searchArtistResultFragment.searchOnlineArtist(mSearchString, CommonConstants.ONLINE_SEARCH_PAGE);
                    break;
                case ALBUM_TAB:
                    SearchAlbumResultFragment searchAlbumResultFragment = (SearchAlbumResultFragment) mFragmentList.get(mCurrentPosition);
                    searchAlbumResultFragment.refreshSearchResult();
                    searchAlbumResultFragment.searchOnlineAlbum(mSearchString, CommonConstants.ONLINE_SEARCH_PAGE);
                    break;
                default:
                    break;
            }
        }
    }

    private class SearchHeadClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            LogUtil.d(TAG, "SearchHeadClick --- onClick");
            mSongTextView.setAlpha(LOW_TRANSPARENCY);
            mArtistTextView.setAlpha(LOW_TRANSPARENCY);
            mAlbumTextView.setAlpha(LOW_TRANSPARENCY);
            int id = v.getId();
            switch (id) {
                case R.id.heard_song:
                    mCurrentPosition = SONG_TAB;
                    mSongTextView.setAlpha(HIGH_TRANSPARENCY);
                    mViewPager.setCurrentItem(SONG_TAB, false);
                    break;
                case R.id.heard_artist:
                    mCurrentPosition = ARTIST_TAB;
                    mArtistTextView.setAlpha(HIGH_TRANSPARENCY);
                    mViewPager.setCurrentItem(ARTIST_TAB, false);
                    break;
                case R.id.heard_album:
                    mCurrentPosition = ALBUM_TAB;
                    mAlbumTextView.setAlpha(HIGH_TRANSPARENCY);
                    mViewPager.setCurrentItem(ALBUM_TAB, false);
                    break;
            }
            executeLoadSearchAsync(mSearchString);
        }
    }

    public ArrayList<Object> getmLocalSearchResultList() {
        return mLocalSearchResultList;
    }
}