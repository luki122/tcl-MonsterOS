package cn.tcl.music.fragments.live;

import android.content.Context;
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
import android.widget.Toast;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.live.SingerDetailActivity;
import cn.tcl.music.adapter.live.LiveArtistAlbumsAdapter;
import cn.tcl.music.model.live.AlbumBean;
import cn.tcl.music.util.SystemUtility;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.view.EmptyLayoutV2;
import cn.tcl.music.view.FooterView;
import cn.tcl.music.view.OnDetailItemClickListener;

/**
 * @author zengtao.kuang
 * @Description:
 * @date 2015/11/9 16:08
 * @copyright TCL-MIE
 */
public class SingerAlbumsFragment extends Fragment implements View.OnClickListener,PopupMenu.OnMenuItemClickListener,OnDetailItemClickListener {

    public static final String TAG = "SingerAlbumsFragment";
    LiveArtistAlbumsAdapter liveArtistAlbumsAdapter;
    RecyclerView recyclerView;
    EmptyLayoutV2 emptyLayoutV2;

    public static SingerAlbumsFragment newInstance(){
        return new SingerAlbumsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_singer_album,container,false);
        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerview);
        emptyLayoutV2 = (EmptyLayoutV2)rootView.findViewById(R.id.empty_view_container);
        emptyLayoutV2.setOnClickListener(this);
        liveArtistAlbumsAdapter = new LiveArtistAlbumsAdapter(rootView.getContext());
        liveArtistAlbumsAdapter.setOnDetailItemClickListener(this);
        recyclerView.setAdapter(liveArtistAlbumsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                LiveArtistAlbumsAdapter adapter = (LiveArtistAlbumsAdapter)recyclerView.getAdapter();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
                    SingerDetailActivity singerDetailActivity = (SingerDetailActivity)getActivity();
                    if(singerDetailActivity.isArtistAlbumMore()){
                        adapter.setIsMore(true);
                        adapter.setState(FooterView.STATE_LOADING);
                        adapter.notifyItemChanged(lastVisibleItem, true);
                        singerDetailActivity.loadSingerAlbumData();
                    } else {
                        adapter.setIsMore(false);
                        adapter.setState(FooterView.STATE_NOMOREDATA);
                        adapter.notifyItemChanged(lastVisibleItem, true);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();

            }

        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SingerDetailActivity associatedActivity = (SingerDetailActivity)getActivity();
        List<AlbumBean> albumBeanList = associatedActivity.getAlbumBeanList();
        if(albumBeanList==null){
            associatedActivity.loadSingerAlbumData();
        }else {
            updateArtistAlbums(albumBeanList);
        }
    }

    /**
     * 更新展示状态
     */
    public void updateStatus(int totalSongCount, int realSongCount){
        if(!isAdded()){
            return;
        }
        if(liveArtistAlbumsAdapter ==null){
            return;
        }
        SingerDetailActivity associatedActivity = (SingerDetailActivity)getActivity();
        if(associatedActivity==null){
            return;
        }
        int loadSingerAlbumStatus = associatedActivity.getLoadSingerAlbumStatus();
        if(loadSingerAlbumStatus==3){
            if(associatedActivity.getArtistAlbumPage() ==1){
                emptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_ERROR);
            }
            liveArtistAlbumsAdapter.setState(FooterView.STATE_FAILED);
        }else if(loadSingerAlbumStatus==2){
            if(totalSongCount > 0 && realSongCount == 0){
                emptyLayoutV2.setErrorType(EmptyLayoutV2.NO_VALID_SONG);
                return;
            }

            emptyLayoutV2.setErrorType(EmptyLayoutV2.HIDE_LAYOUT);
            if (associatedActivity.isArtistAlbumMore()) {
                liveArtistAlbumsAdapter.setIsMore(true);
                liveArtistAlbumsAdapter.setState(FooterView.STATE_NORMAL);
            } else {
                liveArtistAlbumsAdapter.setIsMore(false);
                liveArtistAlbumsAdapter.setState(FooterView.STATE_NOMOREDATA);
            }
        }else if(loadSingerAlbumStatus==1){
            if(associatedActivity.getArtistAlbumPage()==1){
                emptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_LOADING);
            }
            liveArtistAlbumsAdapter.setState(FooterView.STATE_LOADING);

        }
    }

    public void updateArtistAlbums(List<AlbumBean> albumBeanList){
        if(!isAdded()){
            return;
        }
        if(liveArtistAlbumsAdapter ==null){
            return;
        }
        SingerDetailActivity associatedActivity = (SingerDetailActivity)getActivity();
        if(associatedActivity==null){
            return;
        }
        liveArtistAlbumsAdapter.setIsMore(associatedActivity.isArtistAlbumMore());
        liveArtistAlbumsAdapter.addDataList(albumBeanList);
        liveArtistAlbumsAdapter.notifyDataSetChanged();
        if(albumBeanList.size()==0){
            emptyLayoutV2.setErrorType(EmptyLayoutV2.NODATA_ENABLE_CLICK);
        }else{
            emptyLayoutV2.setErrorType(EmptyLayoutV2.HIDE_LAYOUT);
        }
    }

    @Override
    public void onClick(View v) {
        if(!isAdded()){
            return;
        }
        int id = v.getId();
        Context context = getContext();
        if(id== R.id.item_detail){
        }else if(id== R.id.empty_view_container){
            if (SystemUtility.getNetworkType() == SystemUtility.NetWorkType.none) {
                ToastUtil.showToast(v.getContext(), R.string.network_error_prompt);
                return;
            }
            SingerDetailActivity associatedActivity = (SingerDetailActivity)getActivity();
            if(associatedActivity!=null){
                associatedActivity.loadSingerAlbumData();
                updateStatus(0, 0);
            }
        }

    }

    @Override
    public void onClick(View v,Object object,int position) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

}
