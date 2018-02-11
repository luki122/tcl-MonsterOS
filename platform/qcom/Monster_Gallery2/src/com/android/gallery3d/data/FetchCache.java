package com.android.gallery3d.data;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

//this file is strip from LocalMergeAlbum
public  class FetchCache {
    private MediaSet mBaseSet;
    private SoftReference<ArrayList<MediaItem>> mCacheRef;
    private int mStartPos;
    private final int mPageSize;
    private boolean mForEnumerating;

    public FetchCache(MediaSet baseSet, int pageSize) {
        mBaseSet = baseSet;
        mPageSize = pageSize;
    }

    public void invalidate() {
        mCacheRef = null;
    }
    
    public void setForEnumerating(boolean forEnumerating) {
        mForEnumerating = forEnumerating;
    }

    public MediaItem getItem(int index) {
        boolean needLoading = false;
        ArrayList<MediaItem> cache = null;
        if (mCacheRef == null
                || index < mStartPos || index >= mStartPos + mPageSize) {
            needLoading = true;
        } else {
            cache = mCacheRef.get();
            if (cache == null) {
                needLoading = true;
            }
        }

        if (needLoading) {
            if (mBaseSet == null) return null;
            if(mForEnumerating) {
                cache = mBaseSet.getMediaItemWhenEnumerating(index, mPageSize);
            } else {
                cache = mBaseSet.getMediaItem(index, mPageSize);
            }
            if (cache == null) return null;
            mCacheRef = new SoftReference<ArrayList<MediaItem>>(cache);
            mStartPos = index;
        }

        if (index < mStartPos || index >= mStartPos + cache.size()) {
            return null;
        }

        return cache.get(index - mStartPos);
    }
}
