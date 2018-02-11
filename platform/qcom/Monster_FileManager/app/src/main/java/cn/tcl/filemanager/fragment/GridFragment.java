/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.activity.FileBrowserActivity;
import cn.tcl.filemanager.adapter.GridFileInfoAdapter;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.utils.LogUtils;
import cn.tcl.filemanager.utils.PicturesGestureListener;

public class GridFragment extends FileBrowserFragment implements AbsListView.OnScrollListener, View.OnTouchListener {

    private GridView mGridView;
    private FileBrowserActivity.HideInputMethodListener mHideInputMethodListener;
    private boolean isStartScroll = false;
    private static final int LAND_SCREEN_NUM = 4;
    private static final int PORT_SCREEN_NUM = 3;
    private GestureDetector mDetector;
    private PicturesGestureListener mPicturesGestureListener;


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LogUtils.getAppInfo(mActivity);
        mGridView = (GridView) (mAbsListView = (AbsListView) view.findViewById(R.id.grid_view));
        mNoSearchView = (LinearLayout) view.findViewById(R.id.grid_no_search_result);
        mNoFolderView = (LinearLayout) view.findViewById(R.id.grid_no_folder);
        noSearchText = (TextView) view.findViewById(R.id.grid_no_result_text);
        mNo_messageView = (TextView) view.findViewById(R.id.grid_no_folder_text);
        mGridView.setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState > AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            isStartScroll = true;
        } else {
            isStartScroll = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (isStartScroll && mHideInputMethodListener != null) {
            mHideInputMethodListener.hideInputMethod();
            isStartScroll = false;
        }
    }

    @Override
    public void setHideInputMethod(FileBrowserActivity.HideInputMethodListener hideInputMethodListener) {
        mHideInputMethodListener = hideInputMethodListener;
    }

    @Override
    int getContentLayoutId() {
        return R.layout.fragment_grid;
    }

    public void setGridViewNumColumns() {
        if (mGridView == null) {
            return;
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mGridView.setNumColumns(LAND_SCREEN_NUM);
            return;
        }
        mGridView.setNumColumns(PORT_SCREEN_NUM);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new GridFileInfoAdapter(mActivity, mApplication.mFileInfoManager, mGridView);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);
//        mGridView.setOnScrollListener(this);
//        mGridView.setOnTouchListener(this);
        updateActionMode(mAbsListViewFragmentListener.getFileActionMode());

        if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_PICTURES) {
            mGridView.setOnTouchListener(this);
            mPicturesGestureListener = new PicturesGestureListener(mActivity, mAdapter, mGridView);
            mDetector = new GestureDetector(getActivity(), mPicturesGestureListener);
        }
    }

    public void updateActionMode(int mode) {
        super.updateActionMode(mode);
        /*MODIFIED-BEGIN by haifeng.tang, 2016-04-09,BUG-1913721*/
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.setFileActionMode(mode);
        }
        /*MODIFIED-END by haifeng.tang,BUG-1913721*/

    }

    @Override
    public void setViewPostion(int position) {
        mGridView.setSelection(position);
    }

    @Override
    protected void restoreListPosition() {
//        mGridView.setAdapter(mAdapter);
        if (mIsBack && !mPosStack.empty()) {
            Pos lastPos = mPosStack.pop();
            mGridView.setSelection(lastPos.index);
        }
    }

    @Override
    protected void restoreFirstPosition() {
        mGridView.smoothScrollToPosition(0);
    }

    protected void switchToEditView(int position, int top) {
        super.switchToEditView(position, top);
        mGridView.setSelection(position);
        switchToEditView();
    }

    public void onConfiguarationChanged() {
        setGridViewNumColumns();
    }

    // ADD START FOR PR1052271 BY HONGBIN.CHEN 20150724
    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) setGridViewNumColumns();
        super.onHiddenChanged(hidden);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && null != mPicturesGestureListener) {
            mPicturesGestureListener.up();
            updateEditBarByThread();
        }
        return mDetector.onTouchEvent(event);
    }
}
