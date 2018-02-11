package cn.tcl.music.fragments.live;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.live.NetworkBatchActivity;
import cn.tcl.music.activities.live.ScenesDetailActivity;
import cn.tcl.music.adapter.live.DetailSongAdapter;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.LiveMusicPlayTask;
import cn.tcl.music.util.MusicUtil;
import cn.tcl.music.util.SystemUtility;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.view.AuditionAlertDialog;
import cn.tcl.music.view.EmptyLayoutV2;
import cn.tcl.music.view.OnDetailItemClickListener;

//import com.umeng.analytics.MobclickAgent;

/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/11/12 21:02
 * @copyright TCL-MIE
 */
public class ScenesDetailSongFragment extends Fragment implements View.OnClickListener, OnDetailItemClickListener{
    private static final String TAG = CollectDetailSongFragment.class.getSimpleName();
    private DetailSongAdapter mDetailSongAdapter;
    private RecyclerView mRecyclerView;
    private EmptyLayoutV2 mEmptyLayoutV2;
    private boolean mFirstClick = true;

    private TextView mSongCount;

    public static ScenesDetailSongFragment newInstance(){
        return new ScenesDetailSongFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.fragment_singer_song, container, false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerview);
        mEmptyLayoutV2 = (EmptyLayoutV2)rootView.findViewById(R.id.empty_view_container);
        mSongCount = (TextView) rootView.findViewById(R.id.detail_total_num_tv);
        mEmptyLayoutV2.setOnClickListener(this);
        mDetailSongAdapter = new DetailSongAdapter(rootView.getContext());
        mDetailSongAdapter.setOnDetailItemClickListener(this);

        mRecyclerView.setAdapter(mDetailSongAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            private int totalDy = 0;
            private boolean t = false;
            private int tmpDy = 0;
            private int lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                DetailSongAdapter adapter = (DetailSongAdapter)mRecyclerView.getAdapter();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager)mRecyclerView.getLayoutManager();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                //batchOperateLayout.setTranslationY(totalDy);
                return;
            }

        });

        return rootView;
    }

    //[BUGFIX]-ADD by yanjia.li, 2016-06-18,BUG-2197064 begin
    @Override
    public void onDestroy() {
        if(mDetailSongAdapter != null){
            mDetailSongAdapter.unRegisterListener();
        }
        super.onDestroy();
    }
    //[BUGFIX]-ADD by yanjia.li, 2016-06-18,BUG-2197064 end

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ScenesDetailActivity associatedActivity = (ScenesDetailActivity)getActivity();
        List<SongDetailBean> songDetailBeanList = associatedActivity.getSongDetailBeanList();
        if(songDetailBeanList==null){
            associatedActivity.loadRadioDetailData();
        }else {
            updateCollectDetailSong(songDetailBeanList);
        }

    }

    public void updateCollectDetailSong(List<SongDetailBean> songDetailBeanList){
        if(!isAdded()){
            return;
        }
        if(mDetailSongAdapter ==null){
            return;
        }
        ScenesDetailActivity associatedActivity = (ScenesDetailActivity)getActivity();
        if(associatedActivity==null){
            return;
        }
        mDetailSongAdapter.setIsMore(associatedActivity.isRadioDetailSongMore());
        mDetailSongAdapter.addDataList(songDetailBeanList);
        mDetailSongAdapter.notifyDataSetChanged();
        if(songDetailBeanList.size()==0){
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.NODATA_ENABLE_CLICK);
        }else{
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.HIDE_LAYOUT);
        }

    }

    public void updateStatus(int totalSongCount, int realSongCount){
        if(!isAdded()){
            return;
        }
        if(mDetailSongAdapter ==null){
            return;
        }
        ScenesDetailActivity associatedActivity = (ScenesDetailActivity)getActivity();
        if(associatedActivity==null){
            return;
        }
        int loadCollectDetailStatus = associatedActivity.getLoadRadioDetailStatus();
        if(loadCollectDetailStatus==3){
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_ERROR);
        }else if(loadCollectDetailStatus==2){
            if(totalSongCount > 0 && realSongCount == 0){
                mEmptyLayoutV2.setErrorType(EmptyLayoutV2.NO_VALID_SONG);
                return;
            }
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.HIDE_LAYOUT);
        }else if(loadCollectDetailStatus==1){
            mEmptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_LOADING);
        }
    }

    public void updateSongCount(int count){
        if(!isAdded()){
            return;
        }
        if(mDetailSongAdapter ==null){
            return;
        }
        ScenesDetailActivity associatedActivity = (ScenesDetailActivity)getActivity();
        if(associatedActivity==null){
            return;
        }
        mSongCount.setText(getActivity().getResources().getQuantityString(R.plurals.number_of_folder_detail_songs, count, count));

    }


    @Override
    public void onClick(View v) {
        if(!isAdded()){
            return;
        }
        int id = v.getId();
        Context context = getContext();
        if(id== R.id.empty_view_container){
            if (SystemUtility.getNetworkType() == SystemUtility.NetWorkType.none) {
                ToastUtil.showToast(v.getContext(), R.string.network_error_prompt);
                return;
            }
            ScenesDetailActivity associatedActivity = (ScenesDetailActivity)getActivity();
            if(associatedActivity!=null){
                associatedActivity.loadRadioDetailData();
                updateStatus(0, 0);
            }
        }
    }

    @Override
    public void onClick(View v, Object object, int position) {

    }
}
