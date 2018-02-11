package com.android.gallery3d.data;

/*
 * This file is added by qianfeng.shen@tcl.com, it merges LocalMergeAlbum's function and ComboAlbum's function.
 */

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;
import com.android.gallery3d.util.Future;

import com.android.gallery3d.util.LogUtil;

public class SortedComboAlbum extends MediaSet implements ContentListener {
    @SuppressWarnings("unused")
    private static final String TAG = "SortedComboAlbum";
    private final MediaSet[] mSets;
    private String mName;
    
    private static final int PAGE_SIZE = 64;
    // mIndex maps global position to the position of each underlying media sets.
    private TreeMap<Integer, int[]> mIndex = new TreeMap<Integer, int[]>();
    private FetchCache[] mFetcher;
    
    private TreeMap<Integer, int[]> mEnumerateIndex = new TreeMap<Integer, int[]>();
    private FetchCache[] mEnumerateFetcher;

    private int mSupportedOperation;
    private final Comparator<MediaItem> mComparator =  DataManager.sDateTakenComparator;

    public SortedComboAlbum(Path path, MediaSet[] mediaSets, String name) {
        //super(path, nextVersionNumber());
        super(path, INVALID_DATA_VERSION);
        mSets = mediaSets;
        for (MediaSet set : mSets) {
            set.addContentListener(this);
        }
        mName = name;
    }

    /*
    private void updateData(TreeMap<Integer, int[]> treeMap, FetchCache[] fetcher, boolean forEnumerating) {
        int supported = mSets.length == 0 ? 0 : MediaItem.SUPPORT_ALL;
        fetcher = new FetchCache[mSets.length];
        for (int i = 0, n = mSets.length; i < n; ++i) {
            fetcher[i] = new FetchCache(mSets[i], PAGE_SIZE);
            fetcher[i].setForEnumerating(forEnumerating);
            supported &= mSets[i].getSupportedOperations();
        }
        mSupportedOperation = supported;
        treeMap.clear();
        treeMap.put(0, new int[mSets.length]);
    }

    private void invalidateCache(TreeMap<Integer, int[]> treeMap, FetchCache[] fetcher) {
        for (int i = 0, n = mSets.length; i < n; i++) {
            fetcher[i].invalidate();
        }
        treeMap.clear();
        treeMap.put(0, new int[mSets.length]);
    }
    */
    
    private void updateData() {
        //updateData(mIndex, mFetcher, false);
        int supported = mSets.length == 0 ? 0 : MediaItem.SUPPORT_ALL;
        mFetcher = new FetchCache[mSets.length];
        for (int i = 0, n = mSets.length; i < n; ++i) {
            synchronized(this) {
                mFetcher[i] = new FetchCache(mSets[i], PAGE_SIZE);
                mFetcher[i].setForEnumerating(false);
            }
            supported &= mSets[i].getSupportedOperations();
        }
        mSupportedOperation = supported;
        mIndex.clear();
        mIndex.put(0, new int[mSets.length]);
    }
    
    private void invalidateCache() {
        //invalidateCache(mIndex, mFetcher);
        for (int i = 0, n = mSets.length; i < n; i++) {
            if (mFetcher[i] != null)
                 mFetcher[i].invalidate();
        }
        mIndex.clear();
        mIndex.put(0, new int[mSets.length]);
    }
    
    private void updateDataForEnumerating() {
        //updateData(mEnumerateIndex, mEnumerateFetcher, true);
        int supported = mSets.length == 0 ? 0 : MediaItem.SUPPORT_ALL;
        mEnumerateFetcher = new FetchCache[mSets.length];
        for (int i = 0, n = mSets.length; i < n; ++i) {
            synchronized(this) {
                mEnumerateFetcher[i] = new FetchCache(mSets[i], PAGE_SIZE);
               mEnumerateFetcher[i].setForEnumerating(true);
            }
            supported &= mSets[i].getSupportedOperations();
        }
        mSupportedOperation = supported;
        mEnumerateIndex.clear();
        mEnumerateIndex.put(0, new int[mSets.length]);
    }
    
    private void invalidateCacheForEnumerating() {
        //invalidateCache(mEnumerateIndex, mEnumerateFetcher);
        for (int i = 0, n = mSets.length; i < n; i++) {
            if(mEnumerateFetcher[i] != null)
                mEnumerateFetcher[i].invalidate();
        }
        mEnumerateIndex.clear();
        mEnumerateIndex.put(0, new int[mSets.length]);
    }
    // TCL ShenQianfeng Begin on 2016.11.11
    /*
    private void updateData() {
        int supported = mSets.length == 0 ? 0 : MediaItem.SUPPORT_ALL;
        mFetcher = new FetchCache[mSets.length];
        for (int i = 0, n = mSets.length; i < n; ++i) {
            mFetcher[i] = new FetchCache(mSets[i], PAGE_SIZE);
            supported &= mSets[i].getSupportedOperations();
        }
        mSupportedOperation = supported;
        mIndex.clear();
        mIndex.put(0, new int[mSets.length]);
    }

    private void invalidateCache() {
        for (int i = 0, n = mSets.length; i < n; i++) {
            mFetcher[i].invalidate();
        }
        mIndex.clear();
        mIndex.put(0, new int[mSets.length]);
    }
    */
    // TCL ShenQianfeng End on 2016.11.11
    
    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        // First find the nearest mark position <= start.
        //modify by liaoanhua begin
        ArrayList<MediaItem> result = new ArrayList<MediaItem>();
        SortedMap<Integer, int[]> head = mIndex.headMap(start + 1);
        if (head == null || head.isEmpty()) {
            Log.e(TAG, "sortedComboAlbum head is null");
            return result;
        }
        int markPos  = head.lastKey();
        int[] subPos = head.get(markPos).clone();

        //modify by liaoanhua end
        if (mSets.length == 0) return result;
        MediaItem[] slot = new MediaItem[mSets.length];

        int size = mSets.length;
        //LogUtil.d(TAG, " start:" + start + " count:" + count);
        // TODO: java.lang.NullPointerException:
        // Attempt to invoke virtual method
        // 'com.android.gallery3d.data.MediaItem
        // com.android.gallery3d.data.SortedComboAlbum$FetchCache.getItem(int)'
        // on a null object reference
        // fill all slots
        for (int i = 0; i < size; i++) {
            if (mFetcher[i] == null) continue;
            slot[i] = mFetcher[i].getItem(subPos[i]);
        }

        for (int i = markPos; i < start + count; i++) {
            int k = -1; // k points to the best slot up to now.
            for (int j = 0; j < size; j++) {
                if (slot[j] != null) {
                    if (k == -1 || mComparator.compare(slot[j], slot[k]) < 0) {
                        k = j;
                    }
                }
            }
            // If we don't have anything, all streams are exhausted.
            if (k == -1)
                break;

            // Pick the best slot and refill it.
            subPos[k]++;
            if (i >= start) {
                result.add(slot[k]);
            }
            if (mFetcher[k] != null)
                 slot[k] = mFetcher[k].getItem(subPos[k]);
            // Periodically leave a mark in the index, so we can come back later.

            if ((i + 1) % PAGE_SIZE == 0) {
                 mIndex.put(i + 1, subPos.clone());
            }
        }
        return result;
    }

    @Override
    public ArrayList<MediaItem> getMediaItemWhenEnumerating(int start, int count) {
        // First find the nearest mark position <= start.
        //modify by liaoanhua begin
        ArrayList<MediaItem> result = new ArrayList<MediaItem>();
        SortedMap<Integer, int[]> head = mEnumerateIndex.headMap(start + 1);
        if (head == null || head.isEmpty()) {
            Log.e(TAG, "sortedComboAlbum head is null");
            return result;
        }
        int markPos  = head.lastKey();
        int[] subPos = head.get(markPos).clone();

        //modify by liaoanhua end
        if (mSets.length == 0) return result;
        MediaItem[] slot = new MediaItem[mSets.length];

        int size = mSets.length;
        //LogUtil.d(TAG, " start:" + start + " count:" + count);
        // TODO: java.lang.NullPointerException:
        // Attempt to invoke virtual method
        // 'com.android.gallery3d.data.MediaItem
        // com.android.gallery3d.data.SortedComboAlbum$FetchCache.getItem(int)'
        // on a null object reference
        // fill all slots
        for (int i = 0; i < size; i++) {
            if (mEnumerateFetcher[i] == null) continue;
            slot[i] = mEnumerateFetcher[i].getItem(subPos[i]);
        }

        for (int i = markPos; i < start + count; i++) {
            int k = -1; // k points to the best slot up to now.
            for (int j = 0; j < size; j++) {
                if (slot[j] != null) {
                    if (k == -1 || mComparator.compare(slot[j], slot[k]) < 0) {
                        k = j;
                    }
                }
            }
            // If we don't have anything, all streams are exhausted.
            if (k == -1)
                break;

            // Pick the best slot and refill it.
            subPos[k]++;
            if (i >= start) {
                result.add(slot[k]);
            }
            if (mEnumerateFetcher[k] != null)
                slot[k] = mEnumerateFetcher[k].getItem(subPos[k]);
            // Periodically leave a mark in the index, so we can come back later.

            if ((i + 1) % PAGE_SIZE == 0) {
                mEnumerateIndex.put(i + 1, subPos.clone());
            }
        }
        return result;
    }

    @Override
    public int getMediaItemCount() {
        int count = 0;
        for (MediaSet set : mSets) {
            count += set.getMediaItemCount();
        }
        return count;
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }

    @Override
    public String getName() {
        return mName;
    }

    public void useNameOfChild(int i) {
        if (i < mSets.length) mName = mSets[i].getName();
    }

    @Override
    public long reload() {
        boolean changed = false;
        for (int i = 0, n = mSets.length; i < n; ++i) {
            long version = mSets[i].reload();
            if (version > mDataVersion) changed = true;
        }
        if (changed) {
            mDataVersion = nextVersionNumber();
            updateData();
            invalidateCache();
        }
        return mDataVersion;
    }

    @Override
    public void onContentDirty() {
        notifyContentChanged();
    }

    @Override
    public Future<Integer> requestSync(SyncListener listener) {
        return requestSyncOnMultipleSets(mSets, listener);
    }
    
    //TCL ShenQianfeng Begin on 2016.07.05
    @Override
    public boolean isCameraRoll() {
        return false;
    }
    
    @Override
    protected void reloadForEnumerating() {
        updateDataForEnumerating();
        invalidateCacheForEnumerating();
        for (MediaSet set : mSets) {
            set.reloadForEnumerating();
        }
    };

    @Override
    protected int enumerateMediaItems(ItemConsumer consumer, int startIndex) {
        
        reloadForEnumerating();
        
        int total = getMediaItemCount();
        int start = 0;
        while (start < total) {
            int count = Math.min(MEDIAITEM_BATCH_FETCH_COUNT, total - start);
            ArrayList<MediaItem> items = getMediaItemWhenEnumerating(start, count);
            for (int i = 0, n = items.size(); i < n; i++) {
                MediaItem item = items.get(i);
                consumer.consume(startIndex + start + i, item);
            }
            start += count;
        }
        return total;
    }

    @Override
    public int getSubMediaSetCount() {
        return mSets.length;
    }

    @Override
    public MediaSet getSubMediaSet(int index) {
        return mSets[index];
    }

    //TCL ShenQianfeng End on 2016.07.05
}
