package cn.tcl.music.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tcl.framework.log.NLog;

import cn.tcl.music.util.ToastUtil;

public class BaseFragment extends Fragment {
    private static final String TAG = BaseFragment.class.getSimpleName();
    private View mContentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mContentView != null) {
            ViewGroup parent = (ViewGroup) mContentView.getParent();
            if (parent != null)
                parent.removeView(mContentView);

            return mContentView;
        }

        mContentView = inflater.inflate(getLayoutResId(), container, false);
        findViewByIds(mContentView);
        return mContentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    /**
     *
     * get layout resId
     */
    protected int getLayoutResId(){
        return 0;
    }

    /**
     * find view by ids
     */
    protected void findViewByIds(View parent){

    }

    /**
     * init views
     */
    protected void initViews(){

    }

    /**
     * get the string
     */
    protected String getTheString(int id){
        if(isAdded()){
            return getString(id);
        }
        return "";
    }
}
