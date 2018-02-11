package cn.tcl.music.fragments;

import android.os.AsyncTask;
import android.view.View;
import android.view.ViewStub;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.view.EmptyLayoutV2;

/**
 * 2015-11-02
 * fragment base class
 * 默认显示progressBar
 */
public abstract class NetWorkBaseFragment extends BaseFragment implements ILoadData {
    private View mLoadFailView;
    private View mSubContentView;
    private ViewStub mContentStub;
    private EmptyLayoutV2 mEmptyLayout;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_network_base;
    }

    @Override
    protected void findViewByIds(View parent) {
        super.findViewByIds(parent);
        mContentStub = (ViewStub)parent.findViewById(R.id.stub_content);
        mEmptyLayout = (EmptyLayoutV2)parent.findViewById(R.id.empty_layout);

        mContentStub.setLayoutResource(getSubContentLayout());
        mSubContentView = mContentStub.inflate();
        mSubContentView.setVisibility(View.INVISIBLE);

        mEmptyLayout.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doReloadData();
            }
        });
    }

    @Override
    protected void initViews() {
        super.initViews();
    }

    protected View getSubContentView(){
        return mSubContentView;
    }

    protected abstract int getSubContentLayout();

    @Override
    public void onLoadSuccess(int dataType, List datas) {

    }

    @Override
    public void onLoadFail(int dataType, String message) {

    }

    protected void showContent(){
        if(mSubContentView != null){
            mSubContentView.setVisibility(View.VISIBLE);
        }
    }

    protected void hideContent(){
        if(mSubContentView != null){
            mSubContentView.setVisibility(View.INVISIBLE);
        }
    }

    protected void showLoading(){
        if(mEmptyLayout != null){
            mEmptyLayout.setErrorType(EmptyLayoutV2.NETWORK_LOADING);
        }
    }

    protected void hideLoading(){
        if(mEmptyLayout != null){
            mEmptyLayout.setErrorType(EmptyLayoutV2.HIDE_LAYOUT);
        }
    }

    protected void showFail(){
        if(isRemoving()){
            return;
        }

        if(mEmptyLayout != null){
            mEmptyLayout.setErrorType(EmptyLayoutV2.NETWORK_ERROR);
        }
        mSubContentView.setVisibility(View.GONE);
    }

    protected void showNoData(){
        if(mEmptyLayout != null){
            mEmptyLayout.setErrorType(EmptyLayoutV2.NODATA);
        }
    }

    protected void doReloadData(){

    }


    protected void cancelTask(AsyncTask task){
        if (task != null){
            task.cancel(true);
            task = null;
        }
    }
}
