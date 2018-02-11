package cn.tcl.music.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.SearchActivity;
import cn.tcl.music.adapter.SearchArtistResultAdapter;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.model.live.LiveMusicSinger;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveMusicSearchSingerByKeywordTask;
import cn.tcl.music.util.Connectivity;

public class SearchArtistResultFragment extends Fragment implements ILoadData {

    private static final int ONLINE_ARTIST_SIZE = 5;
    private static final String TAG = SearchArtistResultFragment.class.getSimpleName();

    private View mRootView;
    private SearchActivity mContext;
    private TextView mEmptyTextView;
    private RecyclerView mSearchMediaRecyclerView;
    private SearchArtistResultAdapter mSearchArtistResultAdapter;
    private LiveMusicSearchSingerByKeywordTask mLiveMusicSearchSingerByKeywordTask;

    protected boolean mHaveMoreData = true;
    private ArrayList<Object> mOnlineArtistList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContext = (SearchActivity) getActivity();
        mRootView = inflater.inflate(R.layout.fragment_search_media, null);
        mEmptyTextView = (TextView) mRootView.findViewById(R.id.empty_content);
        mSearchMediaRecyclerView = (RecyclerView) mRootView.findViewById(R.id.search_media_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mSearchMediaRecyclerView.setLayoutManager(linearLayoutManager);
        if (mSearchArtistResultAdapter == null) {
            mSearchArtistResultAdapter = new SearchArtistResultAdapter(mContext);
        }
        mSearchMediaRecyclerView.setAdapter(mSearchArtistResultAdapter);
        return mRootView;
    }

    public void refreshSearchResult() {
        ArrayList<Object> localArtistList = new ArrayList<>();
        localArtistList.addAll(mContext.getmLocalSearchResultList());
        if (localArtistList.size() == 0 && mOnlineArtistList.size() == 0) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.INVISIBLE);
        }
        mSearchArtistResultAdapter.setLocalArtistData(localArtistList);
        mSearchArtistResultAdapter.notifyDataSetChanged();
    }

    public void searchOnlineArtist(String searchKey, int currentPage) {
        if (MusicApplication.isNetWorkCanUsed()) {
            if (mLiveMusicSearchSingerByKeywordTask != null && mLiveMusicSearchSingerByKeywordTask.getStatus() == AsyncTask.Status.RUNNING) {
                mLiveMusicSearchSingerByKeywordTask.cancel(true);
            }
            mLiveMusicSearchSingerByKeywordTask = new LiveMusicSearchSingerByKeywordTask(getActivity(), this, searchKey, String.valueOf(currentPage));
            mLiveMusicSearchSingerByKeywordTask.executeMultiTask();
        } else {
            mOnlineArtistList.clear();
            mSearchArtistResultAdapter.setOnlineArtistData(mOnlineArtistList);
            mSearchArtistResultAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoadSuccess(int dataType, List datas) {
        if (datas != null && datas.size() > 0) {
            LiveMusicSinger data = (LiveMusicSinger) datas.get(0);
            if (data != null) {
                mHaveMoreData = data.more;
                int searchArtistSize = data.artists.size() > ONLINE_ARTIST_SIZE ? ONLINE_ARTIST_SIZE : data.artists.size();

                mOnlineArtistList.clear();
                for (int i = 0; i < searchArtistSize; i++) {
                    mOnlineArtistList.add(data.artists.get(i));
                    Log.d(TAG, "mOnlineArtistList : " + mOnlineArtistList.get(i).toString() + "\n");
                }
                if (mOnlineArtistList.size() == 0) {
                    mEmptyTextView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyTextView.setVisibility(View.INVISIBLE);
                }
                mSearchArtistResultAdapter.setOnlineArtistData(mOnlineArtistList);
                mSearchArtistResultAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoadFail(int dataType, String message) {

    }
}
