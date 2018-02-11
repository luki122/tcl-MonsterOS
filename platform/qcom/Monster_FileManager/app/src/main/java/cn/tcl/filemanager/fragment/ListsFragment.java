/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.fragment;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import cn.tcl.filemanager.R;
import cn.tcl.filemanager.activity.FileBrowserActivity;
import cn.tcl.filemanager.adapter.DownLoadListFileInfoAdapter;
import cn.tcl.filemanager.adapter.ListFileInfoAdapter;
import cn.tcl.filemanager.manager.CategoryManager;
import cn.tcl.filemanager.utils.LogUtils;

import cn.tcl.filemanager.activity.FileBaseActivity.deleteFileInfo;

import mst.widget.MstListView;


import static android.content.Context.DOWNLOAD_SERVICE;

public class ListsFragment extends FileBrowserFragment implements AbsListView.OnScrollListener {

    private ListView mListView;
    private FileBrowserActivity.HideInputMethodListener mHideInputMethodListener;
    private boolean isStartScroll = false;
    private DownLoadCompleteReceiver mDownLoadCompleteReceiver;
    private deleteFileInfo mDelFileInfo;

    public ListsFragment() {
    }

    public ListsFragment(deleteFileInfo  delFileInfo) {
        this.mDelFileInfo = delFileInfo;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LogUtils.getAppInfo(mActivity);
        mListView = (MstListView) (mAbsListView = (AbsListView) view.findViewById(R.id.list_view));
        mNoSearchView = (LinearLayout) view.findViewById(R.id.list_no_search_result);
        mNoFolderView = (LinearLayout) view.findViewById(R.id.list_no_folder);
        noSearchText = (TextView) view.findViewById(R.id.list_no_result_text);
        mNo_messageView = (TextView) view.findViewById(R.id.list_no_folder_text);
        mListView.setOnScrollListener(this);
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
        return R.layout.fragment_list;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("niky", "ListsFragment -> onActivityCreated");
        if (CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_DOWNLOAD) {
            mAdapter = new DownLoadListFileInfoAdapter(mActivity, mApplication.mFileInfoManager, mListView, mDelFileInfo);
            mDownLoadCompleteReceiver = new DownLoadCompleteReceiver();
            setDownloadManagerInfo();
            mActivity.registerReceiver(mDownLoadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } else {
            Log.e("niky", "ListsFragment -> onActivityCreated not download");
            mAdapter = new ListFileInfoAdapter(mActivity, mApplication.mFileInfoManager, mListView, mDelFileInfo);
            ((ListFileInfoAdapter) mAdapter).setAbsUpdateEncryptFilesCount(mAbsUpdateEncryptFilesCount);
        }
        Log.d("MODE", "this is enter mode ---222--");
        mListView.setAdapter(mAdapter);
        mListView.setSelection(0);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
//        mListView.setOnScrollListener(this);
//        mListView.setOnTouchListener(this);
        updateActionMode(mAbsListViewFragmentListener.getFileActionMode());
    }

    private void setDownloadManagerInfo() {
        registerContentObservers(CategoryManager.CATEGORY_DOWNLOAD);
    }

    /**
     * download complete receiver
     */
    private class DownLoadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                LogUtils.i(this.getClass().getName(), "download complete!!!");
                registerContentObservers(CategoryManager.CATEGORY_DOWNLOAD);
            }
        }
    }

    @Override
    protected void restoreListPosition() {
        //mListView.setAdapter(mAdapter);
        if (mIsBack && !mPosStack.empty()) {
            Pos lastPos = mPosStack.pop();
            mListView.setSelectionFromTop(lastPos.index, lastPos.top);
        }
    }

    @Override
    protected void switchToEditView(int position, int top) {
        super.switchToEditView(position, top);
        mListView.setSelectionFromTop(position, top);
        switchToEditView();
    }

    @Override
    public void onBackPressed() {
        Log.i(this.getClass().getName(), "onBackPressed");
        super.onBackPressed();
    }

    @Override
    public void onBackPress() {
        super.onBackPress();
        Log.i(this.getClass().getName(), "onBackPress");
    }


    @Override
    public void updateActionMode(int mode) {
        super.updateActionMode(mode);
        if (mAbsListViewFragmentListener != null) {
            mAbsListViewFragmentListener.setFileActionMode(mode);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mDownLoadCompleteReceiver &&
                CategoryManager.mCurrentCagegory == CategoryManager.CATEGORY_DOWNLOAD) {
            mActivity.unregisterReceiver(mDownLoadCompleteReceiver);
        }
    }

    @Override
    public void setViewPostion(int position) {
        final int pos = position;
        mListView.post(new Runnable() {
            @Override
            public void run() {
                mListView.requestFocus();
                mListView.setItemChecked(pos, true);
                mListView.setSelection(pos);
            }
        });
    }

}
