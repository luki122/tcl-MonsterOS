package cn.tcl.music.fragments.live;

import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.network.ILoadData;
import cn.tcl.music.util.ToastUtil;

public abstract class MyABaseFragment extends Fragment implements ILoadData {
    public static final int STATE_NONE = 0;
    public static final int STATE_REFRESH = 1;
    public static final int STATE_LOADMORE = 2;
    public static final int STATE_NOMORE = 3;
    public static final int STATE_PRESSNONE = 4;// 正在下拉但还没有到刷新的状态
    public static int mState = STATE_NONE;
    protected ViewGroup rootView;// 根视图
    private Handler mHandler = new Handler();
    private Toast mToast;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (inflateContentView() > 0) {
            rootView = (ViewGroup) inflater.inflate(inflateContentView(), null);
            rootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            _layoutInit(inflater, savedInstanceState);
            layoutInit(inflater, savedInstanceState);
            return rootView;
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    abstract protected int inflateContentView();

    /**
     * 子类重写这个方法，初始化视图
     *
     * @param inflater
     * @param savedInstanceSate
     */
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {

    }

    /**
     * 根视图
     *
     * @return
     */
    protected ViewGroup getRootView() {
        return rootView;
    }

    /**
     * 用于某些Fragment需要修改主题的情况下 实现
     */
    /**
     * A*Fragment重写这个方法
     *
     * @param inflater
     * @param savedInstanceSate
     */
    protected void _layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {

    }

    @Override
    public void onLoadSuccess(int dataType, List datas) {

    }

    @Override
    public void onLoadFail(int dataType, String message) {

    }

    protected ActionBar getActionBar() {
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getActionBar();
        return actionBar;
    }

    protected void setActionBarTitle(int resId) {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(resId);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(0xDE354047));
    }

    public void showNetworkInvalidToast() {
        if (getActivity() != null) {
            ToastUtil.showToast(getActivity(), getString(R.string.tip_network_error));
        }

    }

    private Runnable r = new Runnable() {
        public void run() {
            mToast.cancel();
        }
    };

    protected void cancelTask(AsyncTask task) {
        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }
}