/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.format.DateFormat;

import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.ContentListener;
import com.android.gallery3d.data.DateGroupInfos;
import com.android.gallery3d.data.FetchCache;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.SortedComboAlbum;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.data.MyEnumerateListener;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.SynchronizedHandler;
import com.android.gallery3d.util.LogUtil;

public class AlbumDataLoader implements MyEnumerateListener {
    @SuppressWarnings("unused")
    private static final String TAG = "AlbumDataLoader";
    private static final int DATA_CACHE_SIZE = 1000;

    private static final int MSG_LOAD_START = 1;
    private static final int MSG_LOAD_FINISH = 2;
    private static final int MSG_RUN_OBJECT = 3;

    // TCL ShenQianfeng Begin on 2016.08.10
    private static final int MSG_NO_PHOTOS = 1002;
    // TCL ShenQianfeng End on 2016.08.10

    private static final int MIN_LOAD_COUNT = 32;
    private static final int MAX_LOAD_COUNT = 64;

    private final MediaItem[] mData;
    private final long[] mItemVersion;
    private final long[] mSetVersion;

    public static interface DataListener {
        public void onContentChanged(int index);
        public void onSizeChanged(int size);
    }

    private int mActiveStart = 0;
    private int mActiveEnd = 0;

    private int mContentStart = 0;
    private int mContentEnd = 0;

    private final MediaSet mSource;
    private long mSourceVersion = MediaObject.INVALID_DATA_VERSION;

    private final Handler mMainHandler;
    private int mSize = 0;

    private DataListener mDataListener;
    private MySourceListener mSourceListener = new MySourceListener();
    private LoadingListener mLoadingListener;
    //modify by liaoah begin
    //private boolean mThreadRunBackgroud = false;
    //modify end
    private ReloadTask mReloadTask = null;
    // the data version on which last loading failed
    private long mFailedVersion = MediaObject.INVALID_DATA_VERSION;

    // TCL ShenQianfeng Begin on 2016.07.25
    private DateGroupInfos mDateGroupNumMap = new DateGroupInfos();
    private EnumerateThread mEnumerateThread = null;
    private MyEnumerateListener mEnumerateListener;

    // TCL ShenQianfeng End on 2016.07.25
    //private static boolean mFirstReloadStartUp = false;
    //public static final Object mSyncLock = new Object();
    //public static final Object mSyncObject = new Object();
    public AlbumDataLoader(AbstractGalleryActivity context, MediaSet mediaSet) {
        mSource = mediaSet;

        mData = new MediaItem[DATA_CACHE_SIZE];
        mItemVersion = new long[DATA_CACHE_SIZE];
        mSetVersion = new long[DATA_CACHE_SIZE];
        Arrays.fill(mItemVersion, MediaObject.INVALID_DATA_VERSION);
        Arrays.fill(mSetVersion, MediaObject.INVALID_DATA_VERSION);

        mMainHandler = new SynchronizedHandler(context.getGLRoot()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                case MSG_RUN_OBJECT:
                    ((Runnable) message.obj).run();
                    return;
                case MSG_LOAD_START:
                    if (mLoadingListener != null)
                        mLoadingListener.onLoadingStarted();
                    return;
                case MSG_LOAD_FINISH:
                    if (mLoadingListener != null) {
                        boolean loadingFailed = (mFailedVersion != MediaObject.INVALID_DATA_VERSION);
                        mLoadingListener.onLoadingFinished(loadingFailed);
                    }
                    return;
                // TCL ShenQianfeng Begin on 2016.09.26
                case MSG_NO_PHOTOS:
                    if (mLoadingListener != null) {
                        // TCL BaiYuan Begin on 2016.11.14
                        // Original:
                        /*
                        mLoadingListener.onNotifyEmpty();
                        */
                        // Modify To:
                        boolean isEmpty = false;
                        if (null == message.obj) {
                            isEmpty = true;
                        }else{
                            int count = ((Integer) message.obj).intValue();
                            isEmpty = (count == 0);
                            if(isEmpty && null != mEnumerateThread) {
                                mEnumerateThread.notifyDirty();
                            }
                        }
                        mLoadingListener.onNotifyEmpty(isEmpty);
                        // TCL BaiYuan End on 2016.11.14
                    }
                    return;
                // TCL ShenQianfeng End on 2016.09.26
                }
            }
        };
    }

    public void setEnumeratListener(MyEnumerateListener listener) {
        mEnumerateListener = listener;
    }

    public class EnumerateThread extends Thread {
        private static final String TAG = "EnumerateThread";
        private final MyEnumerateListener mListener;
        private boolean mDirty = true;
        private boolean mActive = true;

        public EnumerateThread(MyEnumerateListener listener) {
            mListener = listener;
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            //LogUtil.d(TAG, "EnumerateThread :: run");
            boolean enumerateComplete = false;
            while (mActive) {
                synchronized (this) {
                    if (mActive && !mDirty && enumerateComplete) {
                        //LogUtil.d(TAG, "EnumerateThread :: pause");
                        Utils.waitWithoutInterrupt(this);
                        //LogUtil.d(TAG, "EnumerateThread :: resume");
                        continue;
                    }
                    mDirty = false;
                }
                //long time = System.currentTimeMillis();
                formDateGroupNumMap(mListener);
                //LogUtil.i(TAG, "EnumerateThread run formDateGroupNumMap time:" + (System.currentTimeMillis() - time));
                enumerateComplete = true;
            }
        }

        public synchronized void notifyDirty() {
            //LogUtil.i2(TAG, "EnumerateThread :: notifyDirty");
            mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            //LogUtil.d(TAG, "EnumerateThread :: terminate");
            mActive = false;
            notifyAll();
        }

        // TCL ShenQianfeng Begin on 2016.07.25
        public void formDateGroupNumMap(final MyEnumerateListener enumerateListener) {
            //LogUtil.d(TAG, "formDateGroupNumMap ---- ");
            long time = System.currentTimeMillis();
            final int total = mSource.getMediaItemCount();
            synchronized (mDateGroupNumMap) {
                mDateGroupNumMap.clear();
            }
            // TCL BaiYuan Begin on 2016.11.14
            // Original:
            /*
            if(total == 0) {
                mMainHandler.sendEmptyMessage(MSG_NO_PHOTOS);
                return;
            }
            */
            // Modify To:
            if(total == 0) {
                return;
            }
            // TCL BaiYuan Begin on 2016.11.14
            mSource.enumerateMediaItems(new ItemConsumer() {
                @Override
                public void consume(int index, MediaItem item) {
                    long dateInMs = item.getDateInMs();
                    String dateText = String.valueOf(DateFormat.format("yyyy/MMdd", dateInMs));
                    synchronized (mDateGroupNumMap) {
                        mDateGroupNumMap.put(dateText, index);
                        boolean finished = total - 1 == index;
                        /*
                         LogUtil.d(TAG, "enumerateMediaItems -------index :" + index + 
                         " mContentStart:" + mContentStart +
                         " mContentEnd:" + mContentEnd + 
                         " mActiveStart:" + mActiveStart + 
                         " mActiveEnd:" + mActiveEnd ); 
                        */
                        if ((mActiveStart == 0) || (index >= mActiveEnd) || finished) {
                            DateGroupInfos clone = (DateGroupInfos) mDateGroupNumMap.clone();
                            enumerateListener.onEnumerate(index, finished, clone);
                        }
                        if(index !=0 &&  ((index + 1) % 50) == 0) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                LogUtil.d(TAG, "formDateGroupNumMap enumerateMediaItems InterruptedException ");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            //LogUtil.d(TAG, "formDateGroupNumMap consume time:" + (System.currentTimeMillis() - time));
        }
    }
    // TCL ShenQianfeng End on 2016.07.25
    
    /*
    public void destroy() {
        if (mEnumerateThread != null) {
             mEnumerateThread.terminate();
             mEnumerateThread = null;
        }
        if (mReloadTask != null) {
            mReloadTask.terminate();
            mReloadTask = null;
        }
        mSource.removeContentListener(mSourceListener);
    }
    */
    
    public void resume() {
        //LogUtil.d(TAG, "AlbumDataLoader::resume -- ");
        mSource.addContentListener(mSourceListener);
        if (mReloadTask == null) {
            mReloadTask = new ReloadTask();
            mReloadTask.start();
        }
        if (mEnumerateThread == null) {
            mEnumerateThread = new EnumerateThread(this);
            mEnumerateThread.start();
        }
        if(mReloadTask != null) {
            mReloadTask.notifyDirty();
        }
        if(mEnumerateThread != null) {
            mEnumerateThread.notifyDirty();
        }
    }

    public void pause() {
        //LogUtil.d(TAG, "AlbumDataLoader::pause -- ");
        // TCL ShenQianfeng Begin on 2016.07.26
        /*
        if (mEnumerateThread != null) {
            mEnumerateThread.terminate();
            mEnumerateThread = null;
       }
        // TCL ShenQianfeng End on 2016.07.26
       if (mReloadTask != null) {
           mReloadTask.terminate();
           mReloadTask = null;
       }
       */
       mSource.removeContentListener(mSourceListener);
    }
    
    public void destroy() {
        if (mEnumerateThread != null) {
            mEnumerateThread.terminate();
            mEnumerateThread = null;
       }
       if (mReloadTask != null) {
           mReloadTask.terminate();
           mReloadTask = null;
       }
    }

    public MediaItem get(int index) {
        if (!isActive(index)) {
            return mSource.getMediaItem(index, 1).get(0);
        }
        return mData[index % mData.length];
    }

    public int getActiveStart() {
        return mActiveStart;
    }

    public boolean isActive(int index) {
        return index >= mActiveStart && index < mActiveEnd;
    }

    public int size() {
        return mSize;
    }

    // Returns the index of the MediaItem with the given path or
    // -1 if the path is not cached
    public int findItem(Path id) {
        for (int i = mContentStart; i < mContentEnd; i++) {
            MediaItem item = mData[i % DATA_CACHE_SIZE];
            if (item != null && id == item.getPath()) {
                return i;
            }
        }
        return -1;
    }

    private void clearSlot(int slotIndex) {
        mData[slotIndex] = null;
        mItemVersion[slotIndex] = MediaObject.INVALID_DATA_VERSION;
        mSetVersion[slotIndex] = MediaObject.INVALID_DATA_VERSION;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart == mContentStart && contentEnd == mContentEnd)
            return;
        int end = mContentEnd;
        int start = mContentStart;

        // We need change the content window before calling reloadData(...)
        synchronized (this) {
            mContentStart = contentStart;
            mContentEnd = contentEnd;
        }
        long[] itemVersion = mItemVersion;
        long[] setVersion = mSetVersion;
        if (contentStart >= end || start >= contentEnd) {
            for (int i = start, n = end; i < n; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
        } else {
            for (int i = start; i < contentStart; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
            for (int i = contentEnd, n = end; i < n; ++i) {
                clearSlot(i % DATA_CACHE_SIZE);
            }
        }
        if (mReloadTask != null)
            mReloadTask.notifyDirty();
    }

    public void setActiveWindow(int start, int end) {
        if (start == mActiveStart && end == mActiveEnd)
            return;

        // LogUtil.d(TAG, "setActiveWindow start:" + start + " end:" + end);

        Utils.assertTrue(start <= end && end - start <= mData.length && end <= mSize);

        int length = mData.length;
        mActiveStart = start;
        mActiveEnd = end;

        // If no data is visible, keep the cache content
        if (start == end) return;

        int contentStart = Utils.clamp((start + end) / 2 - length / 2, 0, Math.max(0, mSize - length));
        int contentEnd = Math.min(contentStart + length, mSize);
        if (mContentStart > start || mContentEnd < end || Math.abs(contentStart - mContentStart) > MIN_LOAD_COUNT) {
            setContentWindow(contentStart, contentEnd);
        }
    }

    private class MySourceListener implements ContentListener {
        @Override
        public void onContentDirty() {
            if (mReloadTask != null)
                mReloadTask.notifyDirty();
            // TCL ShenQianfeng Begin on 2016.07.26
            if (mEnumerateThread != null)
                mEnumerateThread.notifyDirty();
            // TCL ShenQianfeng End on 2016.07.26
        }
    }

    public void setDataListener(DataListener listener) {
        mDataListener = listener;
    }

    public void setLoadingListener(LoadingListener listener) {
        mLoadingListener = listener;
    }

    // TCL ShenQianfeng End on 2016.06.18

    private <T> T executeAndWait(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<T>(callable);
        mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_RUN_OBJECT,
                task));
        try {
            return task.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static class UpdateInfo {
        public long version;
        public int reloadStart;
        public int reloadCount;

        public int size;
        public ArrayList<MediaItem> items;
    }

    private class GetUpdateInfo implements Callable<UpdateInfo> {
        private final long mVersion;

        public GetUpdateInfo(long version) {
            mVersion = version;
        }

        @Override
        public UpdateInfo call() throws Exception {
            if (mFailedVersion == mVersion) {
                // previous loading failed, return null to pause loading
                return null;
            }
            // LogUtil.d(TAG, "ReloadTask::GetUpdateInfo run ---");
            UpdateInfo info = new UpdateInfo();
            long version = mVersion;
            info.version = mSourceVersion;
            info.size = mSize;
            long setVersion[] = mSetVersion;
            for (int i = mContentStart, n = mContentEnd; i < n; ++i) {
                int index = i % DATA_CACHE_SIZE;
                if (setVersion[index] != version) {
                    info.reloadStart = i;
                    info.reloadCount = Math.min(MAX_LOAD_COUNT, n - i);
                    return info;
                }
            }
            return mSourceVersion == mVersion ? null : info;
        }
    }

    private class UpdateContent implements Callable<Void> {

        private UpdateInfo mUpdateInfo;

        public UpdateContent(UpdateInfo info) {
            mUpdateInfo = info;
        }

        @Override
        public Void call() throws Exception {
            // LogUtil.d(TAG, "ReloadTask::UpdateContent run ---");
            UpdateInfo info = mUpdateInfo;
            mSourceVersion = info.version;
            if (mSize != info.size) {
                mSize = info.size;
                if (mDataListener != null)
                    mDataListener.onSizeChanged(mSize);
                if (mContentEnd > mSize)
                    mContentEnd = mSize;
                if (mActiveEnd > mSize)
                    mActiveEnd = mSize;
            }

            ArrayList<MediaItem> items = info.items;

            mFailedVersion = MediaObject.INVALID_DATA_VERSION;
            if ((items == null) || items.isEmpty()) {
                if (info.reloadCount > 0) {
                    mFailedVersion = info.version;
                    Log.d(TAG, "loading failed: " + mFailedVersion);
                }
                return null;
            }
            int start = Math.max(info.reloadStart, mContentStart);
            int end = Math.min(info.reloadStart + items.size(), mContentEnd);

            for (int i = start; i < end; ++i) {
                int index = i % DATA_CACHE_SIZE;
                mSetVersion[index] = info.version;
                MediaItem updateItem = items.get(i - info.reloadStart);
                long itemVersion = updateItem.getDataVersion();
                if (mItemVersion[index] != itemVersion) {
                    mItemVersion[index] = itemVersion;
                    mData[index] = updateItem;
                    if (mDataListener != null && i >= mActiveStart
                            && i < mActiveEnd) {
                        mDataListener.onContentChanged(i);
                    }
                }
            }
            return null;
        }
    }

    /*
     * The thread model of ReloadTask * [Reload Task] [Main Thread] | |
     * getUpdateInfo() --> | (synchronous call) (wait) <---- getUpdateInfo() | |
     * Load Data | | | updateContent() --> | (synchronous call) (wait)
     * updateContent() | | | |
     */
    private class ReloadTask extends Thread {

        private volatile boolean mActive = true;
        private volatile boolean mDirty = true;
        private boolean mIsLoading = false;

        private void updateLoading(boolean loading) {
            if (mIsLoading == loading)
                return;
            mIsLoading = loading;
            mMainHandler.sendEmptyMessage(loading ? MSG_LOAD_START : MSG_LOAD_FINISH);
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            boolean updateComplete = false;
            while (mActive) {
                synchronized (this) {
                    if (mActive && !mDirty && updateComplete) {
                        updateLoading(false);
                        //LogUtil.d(TAG, "reload pause");
                        Utils.waitWithoutInterrupt(this);
                        //LogUtil.d(TAG, "reload resume");
                        continue;
                    }
                    mDirty = false;
                }
                updateLoading(true);
                //modify by liaoanhua begin
                //LogUtil.e(TAG, "ReloadTask::reload begin ---");
                long version = mSource.reload();
                //LogUtil.e(TAG, "ReloadTask::reload end ---");
                 //modify by liaoanhua end
                UpdateInfo  info = executeAndWait(new GetUpdateInfo(version));
                updateComplete = info == null;
	            if (updateComplete) 
	                 continue;
                if (info.version != version) {
                    info.size = mSource.getMediaItemCount();
                    info.version = version;
                    // TCL BaiYuan Begin on 2016.11.14
                    // Original:
                    /*
                    if (info.size == 0) {
                        mMainHandler.sendEmptyMessage(MSG_NO_PHOTOS);
                        return;
                    }
                    */
                    // Modify To:
                    mMainHandler.removeMessages(MSG_NO_PHOTOS);
                    mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_NO_PHOTOS, Integer.valueOf(info.size)));
                    // TCL BaiYuan End on 2016.11.14
                }
                if (info.reloadCount > 0) {
                    info.items = mSource.getMediaItem(info.reloadStart,
                            info.reloadCount);
                }
                executeAndWait(new UpdateContent(info));
            }
            updateLoading(false);
        }

        public synchronized void notifyDirty() {
            //LogUtil.e(TAG, "ReloadTask::notifyDirty ");
            mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            //LogUtil.e(TAG, "ReloadTask::terminate ");
            mActive = false;
            notifyAll();
        }
    }

    @Override
    public void onEnumerate(int index, boolean finished, DateGroupInfos info) {
        mEnumerateListener.onEnumerate(index, finished, info);
    }
}
