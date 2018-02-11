/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import android.content.Context;

//import FavoriteManager;
import cn.tcl.filemanager.manager.FileInfoManager;
import cn.tcl.filemanager.service.FileManagerService.OperationEventListener;
//import FavoriteHelper;


public abstract class FavoriteFileOperationTask extends BaseAsyncTask {

    public FavoriteFileOperationTask(Context context, FileInfoManager fileInfoManager,
            OperationEventListener listener) {
        super(context, fileInfoManager, listener);
        // TODO Auto-generated constructor stub
    }

//    protected FavoriteHelper mFavoriteHelper;
//    protected SQLiteDatabase db;
//
//    public FavoriteFileOperationTask(FileInfoManager fileInfoManager,
//            OperationEventListener operationEvent, Context context) {
//        super(context, fileInfoManager, operationEvent);
//        if (context == null) {
//            throw new IllegalArgumentException();
//        } else {
//            mFavoriteHelper = new FavoriteHelper(context);
//        }
//    }
//
//    protected void addItem(HashMap<File, FileInfo> fileInfoMap, File file, File addFile) {
//        if (fileInfoMap.containsKey(file)) {
//            FileInfo fileInfo = new FileInfo(mContext, addFile);
//            mFileInfoManager.addItem(fileInfo);
//        }
//    }
//
//    static class AddFavoriteFileTask extends FavoriteFileOperationTask {
//
//        private final List<FileInfo> mSrcList;
//
//        public AddFavoriteFileTask(FileInfoManager fileInfoManager,
//                OperationEventListener operationEvent, Context context, List<FileInfo> src) {
//            super(fileInfoManager, operationEvent, context);
//            mSrcList = src;
//        }
//
//        @Override
//        protected Integer doInBackground(Void... params) {
//            if (mSrcList.isEmpty()) {
//                return OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS;
//            }
//            return insertFavorite();
//        }
//
//        private Integer insertFavorite() {
//            int res = OperationEventListener.ERROR_CODE_SUCCESS;
//            int total = mSrcList.size();
//            int progress = 0;
//			if (total > 100) {
//				publishProgress(new ProgressInfo("", 0, total));
//			}
//
//            try {
//                for (FileInfo fileInfo : mSrcList) {
//                    if (isCancelled()) {
//                        return OperationEventListener.ERROR_CODE_USER_CANCEL;
//                    }
//                    String path = fileInfo.getFileAbsolutePath();
//                    db = mFavoriteHelper.getWritableDatabase();
//                    ContentValues values = new ContentValues();
//                    values.put(FavoriteHelper.FILE_PATH, path);
//                    db.insert(FavoriteHelper.TABLE_NAME, null, values);
//                    if (!FavoriteManager.favoriteArray.contains(path)) {
//                        FavoriteManager.favoriteArray.add(path);
//                    }
//                    fileInfo.setFavorite(true);//add for PR976798 by yane.wang@jrdcom.com 20150414
//                    if (db != null)
//                        db.close();
//					if (total > 100) {
//						publishProgress(new ProgressInfo(fileInfo, ++progress, total));
//					}
//                    res = OperationEventListener.ERROR_CODE_SUCCESS;
//                }
//            } catch (Exception e) {
//            	e.printStackTrace();
//				if (total > 100) {
//					publishProgress(new ProgressInfo(OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS, true));
//				}
//                res = OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS;
//            }
//            return res;
//        }
//    }
//
//    static class CancelFavoriteFileTask extends FavoriteFileOperationTask {
//
//        private final List<FileInfo> mSrcList;
//
//        public CancelFavoriteFileTask(FileInfoManager fileInfoManager,
//                OperationEventListener operationEvent, Context context, List<FileInfo> src) {
//            super(fileInfoManager, operationEvent, context);
//            mSrcList = src;
//        }
//
//        @Override
//        protected Integer doInBackground(Void... params) {
//            if (mSrcList.isEmpty()) {
//                return OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS;
//            }
//            return delFavorite();
//        }
//
//        private Integer delFavorite() {
//            int total = mSrcList.size();
//            int progress = 0;
//            int res = OperationEventListener.ERROR_CODE_SUCCESS;
//			if (total > 100) {
//				publishProgress(new ProgressInfo("", 0, total));
//			}
//
//            try {
//                for (FileInfo fileInfo : mSrcList) {
//                    if (isCancelled()) {
//                        return OperationEventListener.ERROR_CODE_USER_CANCEL;
//                    }
//                    String path = fileInfo.getFileAbsolutePath();
//                    db = mFavoriteHelper.getWritableDatabase();
//					db.delete(FavoriteHelper.TABLE_NAME,
//							FavoriteHelper.FILE_PATH + "=?",
//							new String[] { path });
//                    if (FavoriteManager.favoriteArray.contains(path)) {
//                        FavoriteManager.favoriteArray.remove(path);
//                    }
//                    fileInfo.setFavorite(false);//add for PR976798 by yane.wang@jrdcom.com 20150414
//                    mFileInfoManager.removeItem(fileInfo);//add for PR915696 by yane.wang@jrdcom.com 20150128
//                    db.close();
//					if (total > 100) {
//						publishProgress(new ProgressInfo(new FileInfo(mContext,
//								path), progress++, total));
//					}
//                    res = OperationEventListener.ERROR_CODE_SUCCESS;
//                }
//            } catch (Exception e) {
//				if (total > 100) {
//					publishProgress(new ProgressInfo(OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS, true));
//				}
//                res = OperationEventListener.ERROR_CODE_FAVORITE_UNSUCESS;
//            }
//            return res;
//        }
//    }

}
