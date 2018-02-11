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

package com.android.gallery3d.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.gallery3d.common.BlobCache;
import com.android.gallery3d.common.BlobCache.LookupRequest;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BytesBufferPool.BytesBuffer;
import com.android.gallery3d.util.CacheManager;
import com.android.gallery3d.util.GalleryUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class ImageCacheService {
    @SuppressWarnings("unused")
    private static final String TAG = "ImageCacheService";

    private static final String IMAGE_CACHE_FILE = "imgcache";
    private static final int IMAGE_CACHE_MAX_ENTRIES = 5000;
    private static final int IMAGE_CACHE_MAX_BYTES = 200 * 1024 * 1024;
    private static final int IMAGE_CACHE_VERSION = 7;
    //ShenQianfeng Sync TCT on 2016.08.18 Begin
    private static final int RAM_CACHE_SIZE_KB = (int) (Runtime.getRuntime().maxMemory() / (4 * 1024));
    //ShenQianfeng Sync TCT on 2016.08.18 End
    private BlobCache mCache;
    private LruCache<Long, Bitmap> mMomentsRamCache;
    private LruCache<Long, Bitmap> mAlbumRamCache;

    public ImageCacheService(Context context) {
        mCache = CacheManager.getCache(context, IMAGE_CACHE_FILE,
                IMAGE_CACHE_MAX_ENTRIES, IMAGE_CACHE_MAX_BYTES,
                IMAGE_CACHE_VERSION);

        if (mMomentsRamCache == null) {
            mMomentsRamCache = new LruCache<Long, Bitmap>(RAM_CACHE_SIZE_KB) {
                @Override
                protected int sizeOf(Long key, Bitmap value) {
                    return (value.getByteCount() / 1024);
                }

                @Override
                protected void entryRemoved(boolean evicted, Long key,
                        Bitmap oldValue, Bitmap newValue) {
                    super.entryRemoved(evicted, key, oldValue, newValue);
                    if (oldValue != null && (oldValue instanceof Bitmap && !oldValue.isRecycled())) {
                        oldValue.recycle();
                    }
                }
            };
        }

        if (mAlbumRamCache == null) {
            mAlbumRamCache = new LruCache<Long, Bitmap>(RAM_CACHE_SIZE_KB) {
                @Override
                protected int sizeOf(Long key, Bitmap value) {
                    return (value.getByteCount() / 1024);
                }

                @Override
                protected void entryRemoved(boolean evicted, Long key,
                        Bitmap oldValue, Bitmap newValue) {
                    super.entryRemoved(evicted, key, oldValue, newValue);
                    if (oldValue != null && (oldValue instanceof Bitmap && !oldValue.isRecycled())) {
                        oldValue.recycle();
                    }
                }
            };
        }
    }

    public void cleanMomentsRamCache() {
        mMomentsRamCache.evictAll();
    }

    public void cleanMomentsRamCacheExceptKeys(ArrayList<Long> keys) {
        Map<Long, Bitmap> map = mMomentsRamCache.snapshot();
        Set<Long> allKeys = map.keySet();
        for (Long key : allKeys) {
            if (keys.contains(key)) {
                continue;
            }
            removeBitmapFromMomentsRamCache(key);
        }
    }

    public synchronized Bitmap getBitmapFromMomentsRamCache(Long key) {
        Bitmap bmp = null;
        if (mMomentsRamCache != null) {
            bmp = mMomentsRamCache.get(key);
        }
        return bmp;
    }

    public synchronized void addBitmap2MomentsRamCache(Long key, Bitmap bmp) {
        if (mMomentsRamCache != null) {
            Bitmap bitmap = getBitmapFromMomentsRamCache(key);
            if (bitmap == null) {
                mMomentsRamCache.put(key, bmp);
            } else {
                if (bitmap.isRecycled()) {
                    mMomentsRamCache.remove(key);
                    mMomentsRamCache.put(key, bmp);
                }
            }
        }
    }

    public synchronized boolean isMomentsRamCacheExist(Long key) {
        if (mMomentsRamCache != null) {
            Bitmap bmp = getBitmapFromMomentsRamCache(key);
            if (bmp != null && !bmp.isRecycled()) {
                return true;
            }
        }
        return false;
    }

    public synchronized void removeBitmapFromMomentsRamCache(Long key) {
        if (mMomentsRamCache != null) {
            Bitmap bmp = getBitmapFromMomentsRamCache(key);
            if (bmp != null) {
                mMomentsRamCache.remove(key);
            }
        }
    }

    public void cleanAlbumRamCache() {
        mAlbumRamCache.evictAll();
    }

    public void cleanAlbumRamCacheExceptKeys(ArrayList<Long> keys) {
        Map<Long, Bitmap> map = mAlbumRamCache.snapshot();
        Set<Long> allKeys = map.keySet();
        for (Long key : allKeys) {
            if (keys.contains(key)) {
                continue;
            }
            removeBitmapFromAlbumRamCache(key);
        }
    }

    public synchronized Bitmap getBitmapFromAlbumRamCache(Long key) {
        Bitmap bmp = null;
        if (mAlbumRamCache != null) {
            bmp = mAlbumRamCache.get(key);
        }
        return bmp;
    }

    public synchronized void addBitmap2AlbumRamCache(Long key, Bitmap bmp) {
        if (mAlbumRamCache != null) {
            Bitmap bitmap = getBitmapFromAlbumRamCache(key);
            if (bitmap == null) {
                mAlbumRamCache.put(key, bmp);
            } else {
                if (bitmap.isRecycled()) {
                    mAlbumRamCache.remove(key);
                    mAlbumRamCache.put(key, bmp);
                }
            }
        }
    }

    public synchronized boolean isAlbumRamCacheExist(Long key) {
        if (mAlbumRamCache != null) {
            Bitmap bmp = getBitmapFromAlbumRamCache(key);
            if (bmp != null && !bmp.isRecycled()) {
                return true;
            }
        }
        return false;
    }

    public synchronized void removeBitmapFromAlbumRamCache(Long key) {
        if (mAlbumRamCache != null) {
            Bitmap bmp = getBitmapFromAlbumRamCache(key);
            if (bmp != null) {
                mAlbumRamCache.remove(key);
            }
        }
    }

    /* MODIFIED-BEGIN by dongliang.feng, 2016-03-22,BUG-1850791 */
    public synchronized void removeBitmapRamCache(Long key) {
        removeBitmapFromMomentsRamCache(key);
        removeBitmapFromAlbumRamCache(key);
    }
    /* MODIFIED-END by dongliang.feng,BUG-1850791 */

    /**
     * Gets the cached image data for the given <code>path</code>,
     *  <code>timeModified</code> and <code>type</code>.
     *
     * The image data will be stored in <code>buffer.data</code>, started from
     * <code>buffer.offset</code> for <code>buffer.length</code> bytes. If the
     * buffer.data is not big enough, a new byte array will be allocated and returned.
     *
     * @return true if the image data is found; false if not found.
     */
    public boolean getImageData(Path path, long timeModified, int type, BytesBuffer buffer) {
        byte[] key = makeKey(path, timeModified, type);
        long cacheKey = Utils.crc64Long(key);
        try {
            LookupRequest request = new LookupRequest();
            request.key = cacheKey;
            request.buffer = buffer.data;
            synchronized (mCache) {
                if (!mCache.lookup(request)) return false;
            }
            if (isSameKey(key, request.buffer)) {
                buffer.data = request.buffer;
                buffer.offset = key.length;
                buffer.length = request.length - buffer.offset;
                return true;
            }
        } catch (IOException ex) {
            // ignore.
        }
        return false;
    }

    public void putImageData(Path path, long timeModified, int type, byte[] value) {
        byte[] key = makeKey(path, timeModified, type);
        long cacheKey = Utils.crc64Long(key);
        ByteBuffer buffer = ByteBuffer.allocate(key.length + value.length);
        buffer.put(key);
        buffer.put(value);
        synchronized (mCache) {
            try {
                mCache.insert(cacheKey, buffer.array());
            } catch (IOException ex) {
                // ignore.
            }
        }
    }

    public void clearImageData(Path path, long timeModified, int type) {
        byte[] key = makeKey(path, timeModified, type);
        long cacheKey = Utils.crc64Long(key);
        synchronized (mCache) {
            try {
                mCache.clearEntry(cacheKey);
            } catch (IOException ex) {
                // ignore.
            }
        }
    }

    private static byte[] makeKey(Path path, long timeModified, int type) {
        return GalleryUtils.getBytes(path.toString() + "+" + timeModified + "+" + type);
    }

    private static boolean isSameKey(byte[] key, byte[] buffer) {
        int n = key.length;
        if (buffer.length < n) {
            return false;
        }
        for (int i = 0; i < n; ++i) {
            if (key[i] != buffer[i]) {
                return false;
            }
        }
        return true;
    }
}
