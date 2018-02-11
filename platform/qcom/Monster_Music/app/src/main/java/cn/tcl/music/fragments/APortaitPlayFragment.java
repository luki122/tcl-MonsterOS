package cn.tcl.music.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;


/**
 * Created by xiangxiangliu on 2015/11/25.
 */
public abstract class APortaitPlayFragment extends MyABaseFragment

{

    public static final String TAG = APortaitPlayFragment.class.getSimpleName();

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);
        initCommonView();
    }

    private void initCommonView(){

    }


}
