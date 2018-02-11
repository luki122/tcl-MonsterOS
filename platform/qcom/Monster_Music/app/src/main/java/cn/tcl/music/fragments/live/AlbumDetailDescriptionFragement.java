package cn.tcl.music.fragments.live;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import cn.tcl.music.R;
import cn.tcl.music.activities.live.AlbumDetailActivity;
import cn.tcl.music.util.SystemUtility;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.view.EmptyLayoutV2;

/**
 * @author zengtao.kuang
 * @Description: TAB精品详情
 * @date 2015/11/9 18:46
 * @copyright TCL-MIE
 */
public class AlbumDetailDescriptionFragement extends Fragment implements View.OnClickListener{


    public static AlbumDetailDescriptionFragement newInstance(){
        return new AlbumDetailDescriptionFragement();
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
        AlbumDetailActivity singerDetailActivity = (AlbumDetailActivity)getActivity();
        String description = singerDetailActivity.getDescription();
        if(description==null){
            singerDetailActivity.loadAlbumDetailData();
        }else {
            updateAlbumDetailDescription(description);
        }
    }

    public void updateAlbumDetailDescription(String description){
        if(description==null){
            return;
        }
        if(!isAdded()){
            return;
        }
        if(descriptionTV==null){
            return;
        }

        descriptionTV.setText(description);
    }

    public void updateStatus(){
        if(!isAdded()){
            return;
        }
        AlbumDetailActivity associatedActivity = (AlbumDetailActivity)getActivity();
        if(associatedActivity==null){
            return;
        }
        int loadAlbumDetailStatus = associatedActivity.getLoadAlbumDetailStatus();
        if(loadAlbumDetailStatus==3){
            emptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_ERROR);
        }else if(loadAlbumDetailStatus==2){
            emptyLayoutV2.setErrorType(EmptyLayoutV2.HIDE_LAYOUT);
        }else if(loadAlbumDetailStatus==1){
            emptyLayoutV2.setErrorType(EmptyLayoutV2.NETWORK_LOADING);
        }
    }

    @Override
    public void onClick(View v) {
        if(!isAdded()){
            return;
        }
        int id = v.getId();
        if(id== R.id.empty_view_container){
            if (SystemUtility.getNetworkType() == SystemUtility.NetWorkType.none) {
                ToastUtil.showToast(v.getContext(), R.string.network_error_prompt);
                return;
            }
            AlbumDetailActivity associatedActivity = (AlbumDetailActivity)getActivity();
            if(associatedActivity!=null){
                associatedActivity.loadAlbumDetailData();
                updateStatus();
            }
        }
    }
}
