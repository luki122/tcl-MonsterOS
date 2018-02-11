package cn.tcl.music.fragments.live;

import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import cn.tcl.music.R;
import cn.tcl.music.activities.live.SingerDetailActivity;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.model.live.ArtistBean;
import cn.tcl.music.util.Connectivity;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.view.EmptyLayoutV2;

/**
 * @author zengtao.kuang
 * @Description: TAB歌手详情
 * @date 2015/11/9 18:46
 * @copyright TCL-MIE
 */
public class SingerDetailFragement extends Fragment implements View.OnClickListener{


    public static SingerDetailFragement newInstance(){
        return new SingerDetailFragement();
    }

    private TextView descriptionTV;
    private EmptyLayoutV2 emptyLayoutV2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_singer_detail, container, false);
        descriptionTV = (TextView)rootView.findViewById(R.id.description);
        emptyLayoutV2 = (EmptyLayoutV2)rootView.findViewById(R.id.empty_view_container);
        emptyLayoutV2.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SingerDetailActivity associatedActivity = (SingerDetailActivity)getActivity();
        ArtistBean artistBean = associatedActivity.getArtistBean();
        if(artistBean==null){
            associatedActivity.loadSingerDetailData();
        }else {
            updateSingerDetail(artistBean);
        }
    }

    /**
     * 更新展示状态
     */
    public void updateStatus(int totalSongCount, int realSongCount){
        if(!isAdded()){
            return;
        }
        SingerDetailActivity associatedActivity = (SingerDetailActivity)getActivity();
        if(associatedActivity==null){
            return;
        }
        int loadSingerDetailStatus = associatedActivity.getLoadSingerDetailStatus();
        if(loadSingerDetailStatus==3){
            emptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_ERROR);
        }else if(loadSingerDetailStatus==2){
            if(totalSongCount > 0 && realSongCount == 0){
                emptyLayoutV2.setErrorType(EmptyLayoutV2.NO_VALID_SONG);
                return;
            }

            emptyLayoutV2.setErrorType(EmptyLayoutV2.HIDE_LAYOUT);
        }else if(loadSingerDetailStatus==1){
            emptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_LOADING);
        }
    }

    public void updateSingerDetail(ArtistBean artistBean){
        if(!isAdded()){
            return;
        }
        if(artistBean==null){
            return;
        }
        if(artistBean.description==null){
            return;
        }

        descriptionTV.setText(artistBean.description);
        if(TextUtils.isEmpty(artistBean.description)){
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
        if(id== R.id.empty_view_container){
            if (MusicApplication.isNetWorkCanUsed()) {
                SingerDetailActivity associatedActivity = (SingerDetailActivity)getActivity();
                if(associatedActivity!=null){
                    associatedActivity.loadSingerDetailData();
                    updateStatus(0, 0);
                }
            } else {
                ToastUtil.showToast(v.getContext(), R.string.network_error_prompt);
            }
        }
    }
}
