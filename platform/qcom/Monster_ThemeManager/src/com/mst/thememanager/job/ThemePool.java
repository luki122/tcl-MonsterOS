package com.mst.thememanager.job;



import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.job.SparseArrayThemePool.Node;

import android.graphics.Point;
import android.util.Pools.Pool;
import android.util.Pools.SynchronizedPool;

public class ThemePool {

    private static final int CAPACITY_BYTES = 20971520;

    
    private static final int POOL_NULL = Theme.THEME_NULL;
    private static final int POOL_INDEX_THEME_PKG = Theme.THEME_PKG;
    private static final int POOL_INDEX_FONTS = Theme.FONTS;
    private static final int POOL_INDEX_WALLPAPER = Theme.WALLPAPER;
    private static final int POOL_INDEX_RINGTONG = Theme.RINGTONG;


    private int mCapacityBytes;
    private SparseArrayThemePool [] mPools;
    private Pool<Node> mSharedNodePool = new SynchronizedPool<Node>(128);

    private ThemePool(int capacityBytes) {
        mPools = new SparseArrayThemePool[3];
        mPools[POOL_INDEX_THEME_PKG] = new SparseArrayThemePool(capacityBytes / 3, mSharedNodePool);
        mPools[POOL_INDEX_FONTS] = new SparseArrayThemePool(capacityBytes / 3, mSharedNodePool);
        mPools[POOL_INDEX_WALLPAPER] = new SparseArrayThemePool(capacityBytes / 3, mSharedNodePool);
        mPools[POOL_INDEX_RINGTONG] = new SparseArrayThemePool(capacityBytes / 3, mSharedNodePool);
        mCapacityBytes = capacityBytes;
    }

    private static ThemePool sInstance = new ThemePool(CAPACITY_BYTES);

    public static ThemePool getInstance() {
        return sInstance;
    }

    private SparseArrayThemePool getPoolFortThemeType(int themeType) {
        int index = getPoolIndexForThemeType(themeType);
        if (index == POOL_NULL) {
            return null;
        } else {
            return mPools[index];
        }
    }

    private int getPoolIndexForThemeType(int themeType) {
        if (themeType == Theme.THEME_NULL) {
            return Theme.THEME_NULL;
        }
        return themeType;
    }

    /**
     * @return Capacity of the pool in bytes.
     */
    public synchronized int getCapacity() {
        return mCapacityBytes;
    }

    /**
     * @return Approximate total size in bytes of the themes stored in the pool.
     */
    public int getSize() {
        // Note that this only returns an approximate size, since multiple threads
        // might be getting and putting themes from the pool and we lock at the
        // sub-pool level to avoid unnecessary blocking.
        int total = 0;
        for (SparseArrayThemePool p : mPools) {
            total += p.getSize();
        }
        return total;
    }

    /**
     * @return theme from the pool with the desired height/width or null if none available.
     */
    public Theme get(int themeId,int themeType) {
        SparseArrayThemePool pool = getPoolFortThemeType(themeType);
        if (pool == null) {
            return null;
        } else {
            return pool.get(themeId);
        }
    }

    /**
     * Adds the given theme to the pool.
     * @return Whether the theme was added to the pool.
     */
    public boolean put(Theme theme) {
        if (theme == null) {
            return false;
        }
        SparseArrayThemePool pool = getPoolFortThemeType(theme.type);
        if (pool == null) {
            return false;
        } else {
            return pool.put(theme);
        }
    }

    /**
     * Empty the pool, recycling all the themes currently in it.
     */
    public void clear() {
        for (SparseArrayThemePool p : mPools) {
            p.clear();
        }
    }
}
