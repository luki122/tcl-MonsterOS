package cn.tcl.music.common.cache;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.util.LruCache;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.Utils;
import cn.tcl.music.view.image.ImageResizer;

/**
 * This class handles disk and memory caching of bitmaps in conjunction with the
 * {@link ImageWorker} class and its subclasses. Use
 * {@link ImageCache#getInstance(FragmentManager, ImageCacheParams)} to get an instance of this
 * class, although usually a cache should be added directly to an {@link ImageWorker} by calling
 * {@link ImageWorker#addImageCache(FragmentManager, ImageCacheParams)}.
 */
public class ImageCache {
    private static final String TAG = "ImageCache";

    // Default memory cache size in kilobytes
    private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5; // 5MB

    // Default disk cache size in bytes
    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

    // Compression settings when writing images to disk cache
    private static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;
    private static final int DISK_CACHE_INDEX = 0;

    // Constants to easily toggle various caches
    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
    private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

    private DiskLruCache mDiskLruCache;
    private LruCache<String, Bitmap> mMemoryCache;
    private ImageCacheParams mCacheParams;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;

    private Set<SoftReference<Bitmap>> mReusableBitmaps;

    /**
     * Create a new ImageCache object using the specified parameters. This should not be
     * called directly by other classes, instead use
     * {@link ImageCache#getInstance(FragmentManager, ImageCacheParams)} to fetch an ImageCache
     * instance.
     *
     * @param cacheParams The cache parameters to use to initialize the cache
     */
    public ImageCache(ImageCacheParams cacheParams) {
        init(cacheParams);
    }

    /**
     * Initialize the cache, providing all parameters.
     *
     * @param cacheParams The cache parameters to initialize the cache
     */
    private void init(ImageCacheParams cacheParams) {
        mCacheParams = cacheParams;

        // Set up memory cache
        if (mCacheParams.memoryCacheEnabled) {
            mReusableBitmaps =
                    Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
            //}

            mMemoryCache = new LruCache<String, Bitmap>(mCacheParams.memCacheSize) {

                /**
                 * Measure item size in kilobytes rather than units which is more practical
                 * for a bitmap cache
                 */
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    final int bitmapSize = getBitmapSize(value) / 1024;
                    return bitmapSize == 0 ? 1 : bitmapSize;
                }
            };
        }

        // By default the disk cache is not initialized here as it should be initialized
        // on a separate thread due to disk access.
        if (cacheParams.initDiskCacheOnCreate) {
            // Set up disk cache
            initDiskCache();
        }
    }

    /**
     * Initializes the disk cache.  Note that this includes disk access so this should not be
     * executed on the main/UI thread. By default an ImageCache does not initialize the disk
     * cache when it is created, instead you should call initDiskCache() to initialize it on a
     * background thread.
     */
    public void initDiskCache() {
        // Set up disk cache
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                File diskCacheDir = mCacheParams.diskCacheDir;
                if (mCacheParams.diskCacheEnabled && diskCacheDir != null) {
                    if (!diskCacheDir.exists()) {
                        diskCacheDir.mkdirs();
                    }
                    if (getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize) {
                        try {
                            mDiskLruCache = DiskLruCache.open(
                                    diskCacheDir, 1, 1, mCacheParams.diskCacheSize);
                        } catch (final IOException e) {
                            mCacheParams.diskCacheDir = null;
                            LogUtil.e(TAG, "initDiskCache - " + e);
                        }
                    }
                }
            }
            mDiskCacheStarting = false;
            mDiskCacheLock.notifyAll();
        }
    }

    /**
     * Adds a bitmap to both memory and disk cache.
     * @param data Unique identifier for the bitmap to store
     * @param value The bitmap drawable to store
     */
    public void addBitmapToCache(String data, Bitmap bitmap) {
        if (data == null || bitmap == null) {
            return;
        }

        // Add to memory cache
        if (mMemoryCache != null) {
            mMemoryCache.put(data, bitmap);
        }

        synchronized (mDiskCacheLock) {
            // Add to disk cache
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            bitmap.compress(
                                    mCacheParams.compressFormat, mCacheParams.compressQuality, out);
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (final IOException e) {
                    LogUtil.e(TAG, "addBitmapToCache - " + e);
                } catch (Exception e) {
                    LogUtil.e(TAG, "addBitmapToCache - " + e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {}
                }
            }
        }
    }

    /**
     * Get from memory cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap drawable if found in cache, null otherwise
     */
    public Bitmap getBitmapFromMemCache(String data) {
        Bitmap memValue = null;

        if (mMemoryCache != null) {
            memValue = mMemoryCache.get(data);
        }

        return memValue;
    }

    /**
     * Get from disk cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromDiskCache(String data) {
        final String key = hashKeyForDisk(data);
        Bitmap bitmap = null;

        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {

                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            FileDescriptor fd = ((FileInputStream) inputStream).getFD();

                            // Decode bitmap, but we don't want to sample so give
                            // MAX_VALUE as the target dimensions
                            bitmap = ImageResizer.decodeSampledBitmapFromDescriptor(
                                    fd, Integer.MAX_VALUE, Integer.MAX_VALUE, this);
                        }
                    }
                } catch (final IOException e) {
                    LogUtil.e(TAG, "getBitmapFromDiskCache - " + e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {}
                }
            }
            return bitmap;
        }
    }

    /**
     * @param options - BitmapFactory.Options with out* options populated
     * @return Bitmap that case be used for inBitmap
     */
    public Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {

        Bitmap bitmap = null;

        if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
            synchronized (mReusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps.iterator();
                Bitmap item;

                while (iterator.hasNext()) {
                    item = iterator.next().get();

                    if (null != item && item.isMutable()) {
                        // Check to see it the item can be used for inBitmap
                        if (canUseForInBitmap(item, options)) {
                            bitmap = item;

                            // Remove from reusable set so it can't be used again
                            iterator.remove();
                            break;
                        }
                    } else {
                        // Remove from the set if the reference has been cleared.
                        iterator.remove();
                    }
                }
            }
        }

        return bitmap;
    }

    /**
     * Clears both the memory and disk cache associated with this ImageCache object. Note that
     * this includes disk access so this should not be executed on the main/UI thread.
     */
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
                    LogUtil.e(TAG, "clearCache - " + e);
                }
                mDiskLruCache = null;
                initDiskCache();
            }
        }
    }

    /**
     * Flushes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void flush() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                } catch (IOException e) {
                    LogUtil.e(TAG, "flush - " + e);
                }
            }
        }
    }

    /**
     * Closes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void close() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                    }
                } catch (IOException e) {
                    LogUtil.e(TAG, "close - " + e);
                }
            }
        }
    }

    /**
     * A holder class that contains cache parameters.
     */
    public static class ImageCacheParams {
        public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
        public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
        public File diskCacheDir;
        public Bitmap.CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
        public int compressQuality = DEFAULT_COMPRESS_QUALITY;
        public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
        public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
        public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;

        /**
         * Create a set of image cache parameters that can be provided to
         * {@link ImageCache#getInstance(FragmentManager, ImageCacheParams)} or
         * {@link ImageWorker#addImageCache(FragmentManager, ImageCacheParams)}.
         * @param context A context to use.
         * @param diskCacheDirectoryName A unique subdirectory name that will be appended to the
         *                               application cache directory. Usually "cache" or "images"
         *                               is sufficient.
         */
        public ImageCacheParams(Context context, String diskCacheDirectoryName) {
            diskCacheDir = getDiskCacheDir(context, diskCacheDirectoryName);
        }

        /**
         * Sets the memory cache size based on a percentage of the max available VM memory.
         * Eg. setting percent to 0.2 would set the memory cache to one fifth of the available
         * memory. Throws {@link IllegalArgumentException} if percent is < 0.01 or > .8.
         * memCacheSize is stored in kilobytes instead of bytes as this will eventually be passed
         * to construct a LruCache which takes an int in its constructor.
         *
         * This value should be chosen carefully based on a number of factors
         * Refer to the corresponding Android Training class for more discussion:
         * http://developer.android.com/training/displaying-bitmaps/
         *
         * @param percent Percent of available app memory to use to size memory cache
         */
        public void setMemCacheSizePercent(float percent) {
            if (percent < 0.01f || percent > 0.8f) {
                throw new IllegalArgumentException("setMemCacheSizePercent - percent must be "
                        + "between 0.01 and 0.8 (inclusive)");
            }
            memCacheSize = Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
        }
    }

    /**
     * @param candidate - Bitmap to check
     * @param targetOptions - Options that have the out* value populated
     * @return true if <code>candidate</code> can be used for inBitmap re-use with
     *      <code>targetOptions</code>
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean canUseForInBitmap(
            Bitmap candidate, BitmapFactory.Options targetOptions) {

        if (!Utils.hasKitKat()) {
            // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
            return candidate.getWidth() == targetOptions.outWidth
                    && candidate.getHeight() == targetOptions.outHeight
                    && targetOptions.inSampleSize == 1;
        }

        // From Android 4.4 (KitKat) onward we can re-use if the byte size of the new bitmap
        // is smaller than the reusable bitmap candidate allocation byte count.
        int width = targetOptions.outWidth / targetOptions.inSampleSize;
        int height = targetOptions.outHeight / targetOptions.inSampleSize;
        int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
        return byteCount <= candidate.getAllocationByteCount();
    }

    /**
     * Return the byte usage per pixel of a bitmap based on its configuration.
     * @param config The bitmap configuration.
     * @return The byte usage per pixel.
     */
    private static int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

    /**
     * Get a usable cache directory (external if available, internal otherwise).
     *
     * @param context The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
//        final String cachePath =
//                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
//                        !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
//                                context.getCacheDir().getPath();
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable())
        {
            File externalCacheDir = getExternalCacheDir(context);
            if (externalCacheDir != null)
                cachePath = externalCacheDir.getPath();
            else
            {
                File cacheDir = context.getCacheDir();
                if (cacheDir == null)
                    return null;
                else
                    cachePath = cacheDir.getPath();
            }
        }
        else
        {
            File cacheDir = context.getCacheDir();
            if (cacheDir == null)
                return null;
            else
                cachePath = cacheDir.getPath();
        }


        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
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
        // http://stackoverflow.com/questions/332079
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

    /**
     * Get the size in bytes of a bitmap in a BitmapDrawable. Note that from Android 4.4 (KitKat)
     * onward this returns the allocated memory size of the bitmap which can be larger than the
     * actual bitmap data byte count (in the case it was re-used).
     *
     * @param value
     * @return size in bytes
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static int getBitmapSize(Bitmap bitmap) {
        // From KitKat onward use getAllocationByteCount() as allocated bytes can potentially be
        // larger than bitmap byte count.
        if (Utils.hasKitKat()) {
            return bitmap.getAllocationByteCount();
        }

        return bitmap.getByteCount();
    }

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     *         otherwise.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isExternalStorageRemovable() {
        return Environment.isExternalStorageRemovable();
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getExternalCacheDir(Context context) {
        return context.getExternalCacheDir();

        // Before Froyo we need to construct the external cache dir ourselves
//        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
//        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static long getUsableSpace(File path) {
        // if (Utils.hasGingerbread()) {
        return path.getUsableSpace();
        // }
//        final StatFs stats = new StatFs(path.getPath());
//        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }

    /**
     * Locate an existing instance of this Fragment or if not found, create and
     * add it using FragmentManager.
     *
     * @param fm The FragmentManager manager to use.
     * @return The existing instance of the Fragment or the new instance if just
     *         created.
     */
    private static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
        // Check to see if we have retained the worker fragment.
        RetainFragment mRetainFragment = (RetainFragment) fm.findFragmentByTag(TAG);

        // If not retained (or first time running), we need to create and add it.
        if (mRetainFragment == null) {
            mRetainFragment = new RetainFragment();
            fm.beginTransaction().add(mRetainFragment, TAG).commitAllowingStateLoss();
        }

        return mRetainFragment;
    }

    /**
     * A simple non-UI Fragment that stores a single Object and is retained over configuration
     * changes. It will be used to retain the ImageCache object.
     */
    public static class RetainFragment extends Fragment {
        private Object mObject;

        /**
         * Empty constructor as per the Fragment documentation
         */
        public RetainFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure this Fragment is retained over a configuration change
            setRetainInstance(true);
        }

        /**
         * Store a single object in this Fragment.
         *
         * @param object The object to store
         */
        public void setObject(Object object) {
            mObject = object;
        }

        /**
         * Get the stored object.
         *
         * @return The stored object
         */
        public Object getObject() {
            return mObject;
        }
    }

}
