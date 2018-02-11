/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import android.content.Context;

import cn.tcl.filemanager.manager.FileInfoComparator;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.utils.FileInfo;
import cn.tcl.filemanager.utils.LogUtils;

public class CategorySearchTask extends BaseAsyncTask {

    private static final int MAX_CACHE_SIZE = 10;
    private static final long EXPIRE_TIME = 60 * 1000;    //60s
    private final String mSearchName;
    private static Map<String, List<FileInfo>> searchResultCacheMap = new WeakHashMap<String, List<FileInfo>>(MAX_CACHE_SIZE);  //cache MAX_CACHE_SIZE search result
    private static Map<String, Long> searchResultExpireMap = new HashMap<String, Long>(MAX_CACHE_SIZE);     //search result expire time
    private List<FileInfo> mSearchResult;
    private final List<FileInfo> mFilesInfoList;
    private int mCategory;
    private int FILE_MODE_SEARCH = 2;//add for PR972394 by yane.wang@jrdcom.com 20150410

    public CategorySearchTask(Context context, FileInfoManager fileInfoManager, FileManagerService.OperationEventListener operationEvent,
            String searchName, List<FileInfo> filesInfoList, int category) {
        super(context, fileInfoManager, operationEvent);
        mContext = context;
        mFilesInfoList = filesInfoList;
        mSearchName = searchName;
        mCategory = category;
        mSearchResult = new ArrayList<FileInfo>(1000);
		if (isExpire()) {
            searchResultCacheMap.clear();
            searchResultExpireMap.clear();
        }
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int ret = FileManagerService.OperationEventListener.ERROR_CODE_SUCCESS;
        int total = mFilesInfoList.size();
        int progress = 0;

        String tag = mCategory + mSearchName;
        List<FileInfo> cacheList = searchResultCacheMap.get(tag);
        if (cacheList != null
                && (System.currentTimeMillis() - searchResultExpireMap.get(tag)) < EXPIRE_TIME) {
            LogUtils.d(this.getClass().getName(), "Hit cache, selection= " + tag);
            for (FileInfo file : cacheList) {
                if (file.getFile().exists()) {
                    mSearchResult.add(file);
                }
            }
            mFileInfoManager.addItemList(mSearchResult);
        } else {
        	int len =  mFilesInfoList.size();

            List<FileInfo> searchList = new ArrayList<FileInfo>();

            for (int i = 0; i <len; i++) {
				if (mCancelled) {
					cancel(true);
					onCancelled();
					return FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL;
				}
            	//add for PR972394 by yane.wang@jrdcom.com 20150410 begin
				if (mFileInfoManager.mCurMode != FILE_MODE_SEARCH) {
					return FileManagerService.OperationEventListener.ERROR_CODE_UNSUCCESS;
				}
				//add for PR972394 by yane.wang@jrdcom.com 20150410 end
                FileInfo info = mFilesInfoList.get(i);
                String name = info.getFileName();
                String path = info.getFileAbsolutePath();
                if (name.toLowerCase().contains(mSearchName.toLowerCase())) {
                    searchList.add(new FileInfo(mContext, path));
                    publishProgress(new ProgressInfo(info, progress++, total));
                }
            }

            for (FileInfo fi : searchList) {
                mSearchResult.add(fi);
            }

            if (mSearchResult != null && mSearchResult.size() > 0) {
                mFileInfoManager.addItemList(mSearchResult);
                if (searchResultCacheMap.entrySet().size() < MAX_CACHE_SIZE) {
                    searchResultCacheMap.put(tag, mSearchResult);
                    searchResultExpireMap.put(tag, System.currentTimeMillis());
                }
                mFileInfoManager
                        .updateSearchList(FileInfoComparator.SORT_BY_TYPE);
            }
        }

        return ret;
    }

	private boolean isExpire() {
		long currentTime = System.currentTimeMillis();
		long lastTime = 1;
		for (Iterator<String> it = searchResultExpireMap.keySet().iterator(); it.hasNext();) {
			String searchKey = it.next();
			if (lastTime < searchResultExpireMap.get(searchKey)) {
				lastTime = searchResultExpireMap.get(searchKey);
			}
		}

		if ((currentTime - lastTime) > EXPIRE_TIME) {
			return true;
		}
		return false;
	}

}
