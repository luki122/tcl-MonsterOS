/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import java.util.List;

import android.content.Context;

import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.utils.FileInfo;

public class ShowListBeforeSearch extends BaseAsyncTask {

    private final List<FileInfo> mFilesInfoList;

    public ShowListBeforeSearch(Context context, FileInfoManager fileInfoManager, FileManagerService.OperationEventListener listener,
            List<FileInfo> filesInfoList) {
        super(context, fileInfoManager, listener);
        mFilesInfoList = filesInfoList;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        mFileInfoManager.addItemList(mFilesInfoList);
        return FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
    }

}
