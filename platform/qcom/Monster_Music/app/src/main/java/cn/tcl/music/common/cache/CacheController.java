package cn.tcl.music.common.cache;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.util.LogUtil;

public class CacheController implements MusicApplication.AppBackgroundListener {

    private final static String TAG = "CacheController";

    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;

    public final static String DEFAULT_IMAGE_DIR = "Artworks";
    public final static String DEFAULT_HTTP_DIR = "Http";

    public final static int DELAY_BEFORE_CLOSE = 2000;

    // Lru cache for Http
    private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final int HTTP_IO_BUFFER_SIZE = 8 * 1024;

    private DiskLruCache mHttpDiskCache;
    private File mHttpCacheDir;
    private boolean mHttpDiskCacheStarting = true;
    private final Object mHttpDiskCacheLock = new Object();
    private static final int DISK_CACHE_INDEX = 0;

    private static CacheController sInstance;

    private ImageCache mImageCache;
    private ImageCache.ImageCacheParams mImageCacheParams;

    private Context mApplicationContext;

    //private final Handler mHandler;

    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer) params[0]) {
                case MESSAGE_CLEAR:
                    clearCacheInternal();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
            }
            return null;
        }
    }

    private CacheController(Context context) {
        mApplicationContext = context.getApplicationContext();
        init();
        //mHandler = new Handler(Looper.getMainLooper());
    }


    public static CacheController getInstance(Context context) {
        if (sInstance != null) {
            //sInstance.removeCloseMessage();
            return sInstance;
        }

        sInstance = new CacheController(context);
        return sInstance;
    }

    public static class FileHttpObject {
        public FileDescriptor fileDescriptor;
        public FileInputStream fileInputStream;
    }

    public FileHttpObject getOrDownloadHttpImage(String data) {
        final String key = ImageCache.hashKeyForDisk(data);
        FileHttpObject fileHttpObject = new FileHttpObject();
        DiskLruCache.Snapshot snapshot;
        synchronized (mHttpDiskCacheLock) {
            /**
             *when app appGoToBackground,it will call closeCache(),then mHttpDiskCache will be null,so need call initHttpDiskCache
             */
            if (null == mHttpDiskCache) {
                initHttpDiskCache();
            }
            if (mHttpDiskCache != null) {
                // Wait for disk cache to initialize
                while (mHttpDiskCacheStarting) {
                    try {
                        mHttpDiskCacheLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
                try {
                    snapshot = mHttpDiskCache.get(key);
                    if (snapshot == null) {
                        DiskLruCache.Editor editor = mHttpDiskCache.edit(key);
                        if (editor != null) {
                            if (downloadUrlToStream(data,
                                    editor.newOutputStream(DISK_CACHE_INDEX))) {
                                editor.commit();
                            } else {
                                editor.abort();
                            }
                        }
                        snapshot = mHttpDiskCache.get(key);
                    }
                    if (snapshot != null) {
                        fileHttpObject.fileInputStream =
                                (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
                        fileHttpObject.fileDescriptor = fileHttpObject.fileInputStream.getFD();
                    }
                } catch (IOException e) {
                    LogUtil.e(TAG, "processBitmap - " + e);
                } catch (IllegalStateException e) {
                    LogUtil.e(TAG, "processBitmap - " + e);
                } catch (Exception e) {
                    LogUtil.e(TAG, "processBitmap - " + e);
                } finally {
                    if (fileHttpObject.fileDescriptor == null && fileHttpObject.fileInputStream != null) {
                        try {
                            fileHttpObject.fileInputStream.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }
        return fileHttpObject;
    }

    public Bitmap getBitmapFromMemCache(String data) {
        if (mImageCache != null)
            return mImageCache.getBitmapFromMemCache(data);
        else
            return null;
    }

    public Bitmap getBitmapFromDiskCache(String data) {
        if (mImageCache != null)
            return mImageCache.getBitmapFromDiskCache(data);
        else
            return null;
    }

    public void addBitmapToCache(String data, BitmapDrawable drawable) {
        if (mImageCache != null) {
            mImageCache.addBitmapToCache(data, drawable.getBitmap());
        }
    }

    public ImageCache getImageCache() {
        return mImageCache;
    }

    /**
     * Download a bitmap from a URL and write the content to an output stream.
     *
     * @param urlString The URL to fetch
     * @return true if successful, false otherwise
     */
    private boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        out = new BufferedOutputStream(outputStream, HTTP_IO_BUFFER_SIZE);

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), HTTP_IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (final IOException e) {
            LogUtil.e(TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
            }
        }
        return false;
    }

    private void init() {
        mHttpCacheDir = ImageCache.getDiskCacheDir(mApplicationContext, DEFAULT_HTTP_DIR);

        mImageCacheParams = new ImageCache.ImageCacheParams(mApplicationContext, DEFAULT_IMAGE_DIR);
        //mImageCacheParams.memoryCacheEnabled = false;

        mImageCacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        mImageCache = new ImageCache(mImageCacheParams);

    //    ((MusicApplication) mApplicationContext).registerAppBackgroundListener(this);

        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    protected void initDiskCacheInternal() {
        if (mImageCache != null) {
            mImageCache.initDiskCache();
        }
        initHttpDiskCache();
    }

    protected void clearCacheInternal() {
        if (mImageCache != null) {
            mImageCache.clearCache();
        }
        synchronized (mHttpDiskCacheLock) {
            if (mHttpDiskCache != null && !mHttpDiskCache.isClosed()) {
                try {
                    mHttpDiskCache.delete();
                } catch (IOException e) {
                    LogUtil.e(TAG, "clearCacheInternal - " + e);
                }
                mHttpDiskCache = null;
                mHttpDiskCacheStarting = true;
                initHttpDiskCache();
            }
        }
    }

    protected void flushCacheInternal() {
        if (mImageCache != null) {
            mImageCache.flush();
        }
        synchronized (mHttpDiskCacheLock) {
            if (mHttpDiskCache != null) {
                try {
                    mHttpDiskCache.flush();
                } catch (IOException e) {
                    LogUtil.e(TAG, "flush - " + e);
                }
            }
        }
    }

    protected void closeCacheInternal() {
        if (mImageCache != null) {
            mImageCache.close();
            mImageCache = null;
        }
        synchronized (mHttpDiskCacheLock) {
            if (mHttpDiskCache != null) {
                try {
                    if (!mHttpDiskCache.isClosed()) {
                        mHttpDiskCache.close();
                        mHttpDiskCache = null;
                    }
                } catch (IOException e) {
                    LogUtil.e(TAG, "closeCacheInternal - " + e);
                }
            }

            DiskLruCache.closeCache();
        }
    //    ((MusicApplication) mApplicationContext).unRegisterAppBackgroundListener(this);
        sInstance = null;
    }

    public void clearCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }

    public void flushCache() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }

    private void initHttpDiskCache() {
        if (!mHttpCacheDir.exists()) {
            mHttpCacheDir.mkdirs();
        }
        synchronized (mHttpDiskCacheLock) {
            /**
             *if ImageCache.getUsableSpace(mHttpCacheDir) > HTTP_CACHE_SIZE the mHttpDiskCache will be null
             *then the url image will be null
             *so need log to debug
             */
            LogUtil.d("CacheController", "initHttpDiskCache ImageCache.getUsableSpace(mHttpCacheDir)=" +
                    ImageCache.getUsableSpace(mHttpCacheDir) + ", HTTP_CACHE_SIZE=" + HTTP_CACHE_SIZE);
            if (null == mHttpDiskCache) {
                if (ImageCache.getUsableSpace(mHttpCacheDir) > HTTP_CACHE_SIZE) {
                    try {
                        mHttpDiskCache = DiskLruCache.open(mHttpCacheDir, 1, 1, HTTP_CACHE_SIZE);
                    } catch (IOException e) {
                        try {
                            mHttpDiskCache.delete();
                        } catch (IOException e1) {
                            mHttpDiskCache = null;
                        }
                        mHttpDiskCache = null;
                    } catch (Exception e) {
                        try {
                            mHttpDiskCache.delete();
                        } catch (IOException e1) {
                            mHttpDiskCache = null;
                        }
                        mHttpDiskCache = null;
                    }
                }
                mHttpDiskCacheStarting = false;
                mHttpDiskCacheLock.notifyAll();
            }
        }
    }

    @Override
    public void appGoToBackground(Activity activity) {
//        closeCache();
    }

    @Override
    public void appComeToForeground(Activity activity) {
    }
}

