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
import cn.tcl.music.adapter.SearchAlbumResultAdapter;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.model.live.ArtistAlbumsDataBean;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.network.LiveMusicSearchAlbumByKeywordTask;
import cn.tcl.music.util.Connectivity;

public class SearchAlbumResultFragment extends Fragment implements ILoadData {

    private static final int ONLINE_ALBUM_SIZE = 3;
    public static final String BUNDLE_TITLE = "title";
    private static final String TAG = SearchAlbumResultFragment.class.getSimpleName();

    protected boolean mHaveMoreData = true;

    private View mRootView;
    private SearchActivity mContext;
    private TextView mEmptyTextView;
    private RecyclerView mSearchMediaRecyclerView;
    private SearchAlbumResultAdapter mSearchAlbumResultAdapter;
    private ArrayList<Object> mOnlineAlbumList = new ArrayList<>();
    private LiveMusicSearchAlbumByKeywordTask mLiveMusicSearchAlbumByKeywordTask;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContext = (SearchActivity) getActivity();
        mRootView = inflater.inflate(R.layout.fragment_search_media, null);
        mEmptyTextView = (TextView) mRootView.findViewById(R.id.empty_content);
        mSearchMediaRecyclerView = (RecyclerView) mRootView.findViewById(R.id.search_media_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mSearchMediaRecyclerView.setLayoutManager(linearLayoutManager);
        if (mSearchAlbumResultAdapter == null) {
            mSearchAlbumResultAdapter = new SearchAlbumResultAdapter(mContext);
        }
        mSearchMediaRecyclerView.setAdapter(mSearchAlbumResultAdapter);
        return mRootView;
    }

    public void refreshSearchResult() {
        ArrayList<Object> localAlbumList = new ArrayList<>();
        localAlbumList.addAll(mContext.getmLocalSearchResultList());
        if (localAlbumList.size() == 0 && mOnlineAlbumList.size() == 0) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            mEmptyTextView.setVisibility(View.INVISIBLE);
        }
        mSearchAlbumResultAdapter.setLocalAlbumData(localAlbumList);
        mSearchAlbumResultAdapter.notifyDataSetChanged();
    }

    public void searchOnlineAlbum(String searchKey, int currentPage) {
        if (MusicApplication.isNetWorkCanUsed()) {
            if (mLiveMusicSearchAlbumByKeywordTask != null && mLiveMusicSearchAlbumByKeywordTask.getStatus() == AsyncTask.Status.RUNNING) {
                mLiveMusicSearchAlbumByKeywordTask.cancel(true);
            }
            mLiveMusicSearchAlbumByKeywordTask = new LiveMusicSearchAlbumByKeywordTask(getActivity(), this, searchKey, String.valueOf(currentPage));
            mLiveMusicSearchAlbumByKeywordTask.executeMultiTask();
        } else {
            mOnlineAlbumList.clear();
            mSearchAlbumResultAdapter.setOnlineAlbumData(mOnlineAlbumList);
            mSearchAlbumResultAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoadSuccess(int dataType, List datas) {
        Log.d(TAG, "onLoadSuccess");
        if (datas != null && datas.size() > 0) {
            ArtistAlbumsDataBean data = (ArtistAlbumsDataBean) datas.get(0);
            if (data != null) {
                mHaveMoreData = data.more;
                int searchAlbumSize = data.albums.size() > ONLINE_ALBUM_SIZE ? ONLINE_ALBUM_SIZE : data.albums.size();
                mOnlineAlbumList.clear();
                for (int i = 0; i < searchAlbumSize; i++) {
                    mOnlineAlbumList.add(data.albums.get(i));
                    Log.d(TAG, "mOnlineAlbumList : " + mOnlineAlbumList.get(i).toString() + "\n");
                }
                if (mOnlineAlbumList.size() == 0) {
                    mEmptyTextView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyTextView.setVisibility(View.INVISIBLE);
                }
                mSearchAlbumResultAdapter.setOnlineAlbumData(mOnlineAlbumList);
                mSearchAlbumResultAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoadFail(int dataType, String message) {

    }
}
