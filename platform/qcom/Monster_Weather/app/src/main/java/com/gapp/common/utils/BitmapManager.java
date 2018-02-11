package com.gapp.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.gapp.common.obj.IManager;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-15.
 * $desc
 */
public class BitmapManager implements IManager {


    private SparseArray<BmpNode> mBitmapChaches = new SparseArray<>();

    public Context mContext;

    public BitmapManager(Context context) {
        mContext = context;
    }

    @Override
    public void init() {

    }

    @Override
    public void recycle() {
        for (int i = mBitmapChaches.size() - 1; i >= 0; i--) {
            mBitmapChaches.valueAt(i).clear();
        }
        mBitmapChaches.clear();
    }


    public Bitmap generateBitmap(int resId) {
        BmpNode node = mBitmapChaches.get(resId);
        if (null == node) {
            node = new BmpNode(resId);
            mBitmapChaches.put(resId, node);
        }
        return node.addBitmap();

    }

    public boolean recycleBitmap(int resId) {
        BmpNode node = mBitmapChaches.get(resId);
        if (null != node)
            return node.removeBitmap();
        return true;
    }

    @Override
    public void onTrimMemory(int level) {

    }


    private class BmpNode {
        Bitmap mBmp;
        int mCounts;
        int mResId;

        BmpNode(int resId) {
            mResId = resId;
        }

        Bitmap addBitmap() {
            if (null == mBmp) {
                mBmp = AnimationUtils.decodeSampledBitmapFromResource(mContext, mResId, 0, 0);
                mCounts = 0;
            }
            mCounts++;
            return mBmp;
        }


        boolean removeBitmap() {
            mCounts--;
            if (null != mBmp) {
                if (mCounts <= 0) {
                    Bitmap oBmp = mBmp;
                    mBmp = null;
                    oBmp.recycle();
                    return true;
                }
                return false;
            }
            return true;
        }

        void clear() {
            mCounts = 0;
            if (null != mBmp) {
                mBmp.recycle();
                mBmp = null;
            }
        }
    }
}
