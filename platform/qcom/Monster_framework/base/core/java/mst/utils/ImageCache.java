/*
 * Copyright (C) 2012 The Android Open Source Project
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

package mst.utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import mst.utils.icon.RecyclingBitmapDrawable;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.util.LruCache;

public class ImageCache {
    private static final String TAG = "ImageCache";
    
    private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 1024 * 20; // 20MB
    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 100; // 100MB

    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 100;
    private static final int DISK_CACHE_INDEX = 0;
    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
    private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

    private DiskLruCache mDiskLruCache;
    private LruCache<String, BitmapDrawable> mMemoryCache;
    private ImageCacheParams mCacheParams;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private HashSet<SoftReference<Bitmap>> mReusableBitmaps;
    
    protected int mImageWidth;
    protected int mImageHeight;
    public static int sCacheWidth = 300;
    public static int sCacheHeight = 300;
    public boolean mIsThumb;
    public static boolean sIsThumb;

    private ImageCache(ImageCacheParams cacheParams) {
        init(cacheParams);
    }

    public static ImageCache getInstance(FragmentManager fragmentManager, ImageCacheParams cacheParams) {

        final RetainFragment mRetainFragment = findOrCreateRetainFragment(fragmentManager);

        ImageCache imageCache = (ImageCache) mRetainFragment.getObject();

        if (imageCache == null) {
            imageCache = new ImageCache(cacheParams);
            mRetainFragment.setObject(imageCache);
        }

        return imageCache;
    }

    public static ImageCache getInstance(ImageCacheParams cacheParams){
			
			return new ImageCache(cacheParams);
    }
    
    private void init(ImageCacheParams cacheParams) {
        mCacheParams = cacheParams;

        if (mCacheParams.memoryCacheEnabled) {

            if (SdkVersionUtils.hasHoneycomb()) {
                mReusableBitmaps = new HashSet<SoftReference<Bitmap>>();
            }

            mMemoryCache = new LruCache<String, BitmapDrawable>(mCacheParams.memCacheSize) {

                @Override
                protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue,
                        BitmapDrawable newValue) {
                    if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {
                        ((RecyclingBitmapDrawable) oldValue).setIsCached(false);
                    } else {
                        if (SdkVersionUtils.hasHoneycomb()) {
                            mReusableBitmaps.add(new SoftReference<Bitmap>(oldValue.getBitmap()));
                        }
                    }
                }

                @Override
                protected int sizeOf(String key, BitmapDrawable value) {
                    final int bitmapSize = getBitmapSize(value) / 1024;
                    return bitmapSize == 0 ? 1 : bitmapSize;
                }
            };
        }

        if (cacheParams.initDiskCacheOnCreate) {
            initDiskCache(null);
        }
    }

    public void initDiskCache(String update) {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                File diskCacheDir = mCacheParams.diskCacheDir;
                if (mCacheParams.diskCacheEnabled && diskCacheDir != null) {
                    if (!diskCacheDir.exists()) {
                        diskCacheDir.mkdirs();
                    }
                    if (getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize) {
                        try {
                            mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, mCacheParams.diskCacheSize);
                            if (update != null) {
                            	String key = hashKeyForDisk(update);
								if(mDiskLruCache.get(key) != null) {
									removeDiskCache(key);
								}
							}
                        } catch (final IOException e) {
                            mCacheParams.diskCacheDir = null;
                            Log.e(TAG, "initDiskCache - " + e);
                        }
                    }
                }
            }
            mDiskCacheStarting = false;
            mDiskCacheLock.notifyAll();
        }
    }

    public void addBitmapToCache(String data, BitmapDrawable value) {
        if (data == null || value == null) {
            return;
        }

        if (mMemoryCache != null) {
            if (RecyclingBitmapDrawable.class.isInstance(value)) {
                ((RecyclingBitmapDrawable) value).setIsCached(true);
            }
            mMemoryCache.put(data, value);
        }

        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            value.getBitmap().compress(mCacheParams.compressFormat,
                                    mCacheParams.compressQuality, out);
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } catch (Exception e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    public BitmapDrawable getBitmapFromMemCache(String data) {
        BitmapDrawable memValue = null;
        if (mMemoryCache != null) {
            memValue = mMemoryCache.get(data);
        }


        return memValue;
    }
    
    public boolean hasMemKey(String key) {
		return (mMemoryCache != null && mMemoryCache.get(key) != null);
	}
    
    public void removeMemCache(String key) {
    	if (mMemoryCache != null) {
    		mMemoryCache.remove(key);
		}
	}
    
    public boolean hasDiskKey(String key) {
    	boolean hasKey = false;
    	try {
    		if (mDiskLruCache != null) {
    			hasKey = (mDiskLruCache.get(key) != null);
			}
		} catch (IOException e) {
			
		}
		return hasKey;
	}
    
    public void removeDiskCache(String key) {
    		if (mDiskLruCache != null) {
    			try {
					mDiskLruCache.remove(key);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	}
    
    public Bitmap getBitmapFromDiskCache(String data) {
        final String key = hashKeyForDisk(data);
        Bitmap bitmap = null;

        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            FileDescriptor fd = ((FileInputStream) inputStream).getFD();

                            bitmap = decodeSampledBitmapFromDescriptor(fd, sCacheWidth,
                                    sCacheHeight, sIsThumb, this);
                        }
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "getBitmapFromDiskCache - " + e);
                } catch (OutOfMemoryError e) {
                	Log.e(TAG, "OutOfMemoryError - " + e);
				} finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
            return bitmap;
        }
    }
    
    
    
    public Bitmap getBitmapFromResources(Resources resources, String data) {
		Bitmap bitmap = null;
		BitmapFactory.Options opts = new BitmapFactory.Options(); 
		//opts.inJustDecodeBounds = true;
		opts.inSampleSize = 2;
		try {
			AssetManager am = resources.getAssets();
			InputStream is = am.open(data); 
			bitmap = BitmapFactory.decodeStream(is, null, opts);
			is.close();
		} catch (Exception e) {
		}
		
		return bitmap;
	}
    
    

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth,
            int reqHeight, ImageCache cache) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        if (SdkVersionUtils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight,
            ImageCache cache) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        if (SdkVersionUtils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }
    
    public static Bitmap decodeSampledBitmapFromDescriptor(String filename, int reqWidth, int reqHeight,
            ImageCache cache) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        File file = new File(filename);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(fis!=null){
        	try {
				BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        if (SdkVersionUtils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        options.inJustDecodeBounds = false;
        Bitmap bm = null;
        try {
            if(fis!=null) bm = BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{ 
            if(fis!=null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bm;
    }
    
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
    
        if (height > reqHeight || width > reqWidth) {
    
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    
    public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth,
            int reqHeight, boolean isThumb, ImageCache cache) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        options.inSampleSize = calculateInSampleSize(options, sCacheWidth, sCacheHeight);
        if (isThumb) {
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        } else {
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inPurgeable = true;
            options.inInputShareable = true;
        }
        options.inDither = false;
        options.inJustDecodeBounds = false;


        if (SdkVersionUtils.hasHoneycomb()) {
            addInBitmapOptions(options, cache);
        }

        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);	//bug-5577 java.lang.OutOfMemoryError
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {
        options.inMutable = true;

        if (cache != null) {
            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

            if (inBitmap != null) {
                options.inBitmap = inBitmap;
            }
        }
    }
    
    protected synchronized Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        Bitmap bitmap = null;

        if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
            final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps.iterator();
            Bitmap item;
            List deList = new ArrayList();

            while (iterator.hasNext()) {
                item = iterator.next().get();

                if (null != item && item.isMutable()) {
                    // Check to see it the item can be used for inBitmap

                    if (canUseForInBitmap(item, options)) {
                        bitmap = item;

                        // Remove from reusable set so it can't be used again
                        deList.add(item);
                        break;
                    }
                } else {
                    // Remove from the set if the reference has been cleared.
                    deList.add(item);
                }
            }

            mReusableBitmaps.removeAll(deList);
        }

        return bitmap;
    }

    public void clearMemoryCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
            mMemoryCache = null;
        }
        return;
    }

    public void clearCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }

        synchronized (mDiskCacheLock) {
            mDiskCacheStarting = true;
            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try {
                    mDiskLruCache.delete();
                } catch (IOException e) {
                    Log.e(TAG, "clearCache - " + e);
                } catch (Exception e) {
					e.printStackTrace();
				}
                mDiskLruCache = null;
                initDiskCache(null);
            }
        }
    }

    public void flush() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                } catch (IOException e) {
                    Log.e(TAG, "flush - " + e);
                }
            }
        }
    }

    public void close() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "close - " + e);
                }
            }
        }
    }

    public static class ImageCacheParams {
        public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
        public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
        public File diskCacheDir;
        public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
        public int compressQuality = DEFAULT_COMPRESS_QUALITY;
        public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
        public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
        public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;

        public ImageCacheParams(Context context, String diskCacheDirectoryName) {
            diskCacheDir = getDiskCacheDir(context, diskCacheDirectoryName);
        }

        public void setMemCacheSizePercent(float percent) {
            if (percent < 0.05f || percent > 0.8f) {
                throw new IllegalArgumentException("setMemCacheSizePercent - percent must be "
                        + "between 0.05 and 0.8 (inclusive)");
            }
            memCacheSize = Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
        }
    }

    private static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {
        int width = targetOptions.outWidth / targetOptions.inSampleSize;
        int height = targetOptions.outHeight / targetOptions.inSampleSize;

        return candidate.getWidth() == width && candidate.getHeight() == height;
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        final String cachePath = Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState()
        ) ? context.getCacheDir().getPath() : getExternalCacheDir(
                context).getPath();
        Log.d(TAG, "---getDiskCacheDir---"+cachePath);
        return new File(cachePath + File.separator + uniqueName);
    }

    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    @TargetApi(12)
    public static int getBitmapSize(BitmapDrawable value) {
        Bitmap bitmap = value.getBitmap();
		if(bitmap == null)return 0;
        if (SdkVersionUtils.hasHoneycombMR1()) {
            return bitmap.getByteCount();
        }

        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    @TargetApi(9)
    public static boolean isExternalStorageRemovable() {
        if (SdkVersionUtils.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    @TargetApi(8)
    public static File getExternalCacheDir(Context context) {
        if (SdkVersionUtils.hasFroyo()) {
            return context.getExternalCacheDir();
        }

        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    @TargetApi(9)
    public static long getUsableSpace(File path) {
        if (SdkVersionUtils.hasGingerbread()) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }

    private static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
        RetainFragment mRetainFragment = (RetainFragment) fm.findFragmentByTag(TAG);

        if (mRetainFragment == null) {
            mRetainFragment = new RetainFragment();
            fm.beginTransaction().add(mRetainFragment, TAG).commitAllowingStateLoss();
        }

        return mRetainFragment;
    }

    public static class RetainFragment extends Fragment {
        private Object mObject;

        public RetainFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setRetainInstance(true);
        }

        public void setObject(Object object) {
            mObject = object;
        }

        public Object getObject() {
            return mObject;
        }
    }

}
