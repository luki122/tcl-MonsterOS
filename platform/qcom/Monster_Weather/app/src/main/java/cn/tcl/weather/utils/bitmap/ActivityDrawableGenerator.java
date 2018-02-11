package cn.tcl.weather.utils.bitmap;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;


import com.leon.tools.view.AndroidUtils;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cn.tcl.weather.utils.ThreadHandler;
/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-11-16.
 * manage drawable resource
 */
class ActivityDrawableGenerator implements IDrawableGenerator<AbsBmpLoadItem> {
    private final static String TAG = "ActivityDrawableGenerator";

    private final Application mApp;
    private ThreadHandler mThreadHandler = new ThreadHandler(TAG);
    private ResBmpGenerator mResFileDrawableGenerator = new ResBmpGenerator();

    public ActivityDrawableGenerator(Application app) {
        mApp = app;
    }

    @Override
    public void init() {
        mThreadHandler.init();
        mResFileDrawableGenerator.init();
    }

    @Override
    public void recycle() {
        mResFileDrawableGenerator.recycle();
        mThreadHandler.recycle();
    }

    @Override
    public void onTrimMemory(int level) {
        mResFileDrawableGenerator.onTrimMemory(level);
    }

    @Override
    public void regiestLoadItem(AbsBmpLoadItem item) {
        if (item instanceof AbsBmpLoadItem.AbsResBmpLoadItem) {
            mResFileDrawableGenerator.regiestLoadItem((AbsBmpLoadItem.ResFileViewBgLoadItem) item);
        }
    }

    @Override
    public void unregiestLoadItem(AbsBmpLoadItem item) {
        if (item instanceof AbsBmpLoadItem.AbsResBmpLoadItem) {
            mResFileDrawableGenerator.unregiestLoadItem((AbsBmpLoadItem.AbsResBmpLoadItem) item);
        }
    }


    /*
     * manage res bmp drawable resource
     */
    private class ResBmpGenerator implements IDrawableGenerator<AbsBmpLoadItem.AbsResBmpLoadItem> {

        private SparseArray<BmpDrawableNode> mNodeItems = new SparseArray<>(24);
        private ReentrantReadWriteLock.ReadLock mReadLock;
        private ReentrantReadWriteLock.WriteLock mWriteLock;


        public ResBmpGenerator() {
            ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
            mReadLock = lock.readLock();
            mWriteLock = lock.writeLock();
        }

        @Override
        public void regiestLoadItem(AbsBmpLoadItem.AbsResBmpLoadItem item) {
            if (item.isChanged()) {
                int lastId = item.getLastResId();
                BmpDrawableNode items = getBmpDrawableNode(lastId, false);
                if (null != items) {
                    mWriteLock.lock();
                    items.unregiestLoadItem(item);
                    mWriteLock.unlock();
                }
            }

            int id = item.getResId();
            BmpDrawableNode items = getBmpDrawableNode(id, true);
            if (null != items) {
                mWriteLock.lock();
                items.regiestLoadItem(item);
                mWriteLock.unlock();
            }
        }

        @Override
        public void unregiestLoadItem(AbsBmpLoadItem.AbsResBmpLoadItem item) {
            int id = item.getResId();
            BmpDrawableNode items = getBmpDrawableNode(id, true);
            if (null != items) {
                mWriteLock.lock();
                items.unregiestLoadItem(item);
                mWriteLock.unlock();
            }
        }


        /*get node items*/
        private BmpDrawableNode getBmpDrawableNode(int resId, boolean isGenerate) {
            if (0 == resId)
                return null;
            mReadLock.lock();
            BmpDrawableNode item = mNodeItems.get(resId);
            mReadLock.unlock();
            if (isGenerate && null == item) {
                mWriteLock.lock();
                item = mNodeItems.get(resId);
                if (null == item) {
                    item = new BmpDrawableNode(resId);
                    mNodeItems.put(resId, item);
                }
                mWriteLock.unlock();
            }
            return item;
        }

        @Override
        public void init() {

        }

        @Override
        public void recycle() {

        }

        @Override
        public void onTrimMemory(int level) {
            if (level == Application.TRIM_MEMORY_RUNNING_LOW) {// when memory is low, we should clear some bitmap
                mWriteLock.lock();
                for (int i = mNodeItems.size() - 1; i >= 0; i--) {
                    mNodeItems.valueAt(i).recycleBitmap(false);
                }
                mWriteLock.unlock();
            } else if (level == Application.TRIM_MEMORY_UI_HIDDEN) {
                mWriteLock.lock();
                for (int i = mNodeItems.size() - 1; i >= 0; i--) {
                    mNodeItems.valueAt(i).recycleBitmap(true);
                }
                mWriteLock.unlock();
            }
        }


        private class BmpDrawableNode implements Runnable {

            private Drawable mDrawable;
            private Bitmap mBitmap;

            private LinkedList<AbsBmpLoadItem.AbsResBmpLoadItem> mItems = new LinkedList<>();
            private final int mResId;

            BmpDrawableNode(int resId) {
                mResId = resId;
            }

            void unregiestLoadItem(AbsBmpLoadItem.AbsResBmpLoadItem item) {
                if (mItems.remove(item))
                    item.reset();
            }

            void regiestLoadItem(AbsBmpLoadItem.AbsResBmpLoadItem item) {
                if (!mItems.contains(item)) {
                    mItems.add(item);
                }
                if (null == mBitmap) {
                    mThreadHandler.remove(this);
                    mThreadHandler.post(this);
                } else {
                    item.setCurrentDrawable(mDrawable);
                }
            }

            private boolean recycleBitmap(boolean isForce) {
                if (isForce || mItems.isEmpty()) {
                    if (null != mBitmap) {
                        LinkedList<AbsBmpLoadItem.AbsResBmpLoadItem> items;
                        items = (LinkedList<AbsBmpLoadItem.AbsResBmpLoadItem>) mItems.clone();
                        for (AbsBmpLoadItem.AbsResBmpLoadItem item : items) {
                            item.reset();
                        }
                        mBitmap.recycle();
                    }
                    mBitmap = null;
                    mDrawable = null;
                    return true;
                }
                return false;
            }

            @Override
            public void run() {
                if (null == mBitmap) {
                    mBitmap = AndroidUtils.decodeSampledBitmapFromResource(mApp, mResId, 0, 0);
                    mDrawable = new BitmapDrawable(mBitmap);
                    LinkedList<AbsBmpLoadItem.AbsResBmpLoadItem> items;
                    items = (LinkedList<AbsBmpLoadItem.AbsResBmpLoadItem>) mItems.clone();
                    for (AbsBmpLoadItem.AbsResBmpLoadItem item : items) {
                        item.setCurrentDrawable(mDrawable, false);
                    }
                }
            }
        }
    }
}
