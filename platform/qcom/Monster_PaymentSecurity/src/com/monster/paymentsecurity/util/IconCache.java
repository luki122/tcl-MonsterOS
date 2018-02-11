package com.monster.paymentsecurity.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * 轻量的图片内存缓存， 缓存apk图标
 * Created by logic on 16-12-23.
 */
public class IconCache {

    private static final int DEFAULT_THREAD_POOL_SIZE = 2;
    private static final int SET_IMAGE_VIEW = 1;
    LruCache<String, Bitmap> mMemoryCache;
    private ExecutorService mImageThreadPool = null;
    Context mContext;
    private Handler mUIHandler;
    private boolean clear = false;
    /**
     * 后台轮询线程
     */
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;
    /**
     * 任务队列
     */
    private LinkedList<Runnable> mTaskQueue;
    /**
     * 同步信号
     */
    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    private Semaphore mSemaphoreThreadPool;

    public IconCache(Context context){
        this.mContext = context.getApplicationContext();
        //获取系统分配给每个应用程序的最大内存，每个应用系统分配32M
        int maxMemory = (int) Runtime.getRuntime().maxMemory() / 1024;
        int mCacheSize = maxMemory / 128;
        mCacheSize = mCacheSize < 2048 ? 2048 : mCacheSize;
        Log.v("logic", "cacheSize =" + mCacheSize);
        mMemoryCache = new LruCache<String, Bitmap>(mCacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
        mImageThreadPool = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE, (r) -> {
                Thread thread = new Thread(r, "IconLoaderThread");
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
        });
        initBackThread();
        mTaskQueue = new LinkedList<>();
        mSemaphoreThreadPool = new Semaphore(DEFAULT_THREAD_POOL_SIZE);
    }

    public void clear(){
        clear = true;
        if (mUIHandler != null) {
            mUIHandler.removeMessages(SET_IMAGE_VIEW);
        }
        mImageThreadPool.shutdown();
        mPoolThread.interrupt();
        mMemoryCache.evictAll();
        mMemoryCache = null;
        mContext = null;
        Runtime.getRuntime().gc();
    }

    private void initBackThread() {
        // 后台轮询线程
        mPoolThread = new Thread() {
            @Override
            public void run()
            {
                Looper.prepare();
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        // 线程池去取出一个任务进行执行
                        mImageThreadPool.execute(getATask());
                        try {
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {

                        }
                    }
                };
                // 初始化成功
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            }
        };
        mPoolThread.start();
    }


    /**
     * 添加Bitmap到缓存
     * @param key key
     * @param bitmap bmp
     */
    private synchronized void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * 从缓存中获取一个Bitmap
     */
    private synchronized Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    private Drawable getIcon(String key, boolean isPath){

        Drawable drawable;
        if (isPath){
            drawable = PackageUtils.getApkIcon(mContext, key);
        }else {
            drawable = PackageUtils.getAppIcon(mContext, key);
        }

        if (drawable instanceof BitmapDrawable){
            BitmapDrawable bd = (BitmapDrawable) drawable;
            Bitmap bitmap  = bd.getBitmap();
            addBitmapToMemoryCache(key, bitmap);
        }
        return drawable;
    }

    public void loadIcon(ImageView imageView, final String key){
        if (TextUtils.isEmpty(key) || null == imageView){
            return;
        }

        Bitmap bitmap = getBitmapFromMemCache(key);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }

        imageView.setTag(key);

        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                public void handleMessage(Message msg) {
                    if (clear) return;
                    if (msg.what == SET_IMAGE_VIEW){
                        ImageMsgHolder holder = (ImageMsgHolder) msg.obj;
                        ImageView imageview = holder.imageView;
                        if(holder.key.equals((imageview.getTag().toString()))) {
                            imageview.setImageDrawable(holder.drawable);
                        }
                    }
                }
            };
        }

        //插入一个任务
        addLoadTask(buildLoadTask(key, imageView));
    }

    private Runnable buildLoadTask(final String key, final ImageView imageView) {
        return () -> {
            boolean isPath = key.contains(File.separator);
            Drawable drawable = getIcon(key, isPath);
            refreshBitmap(key, imageView, drawable);
            mSemaphoreThreadPool.release();
        };
    }

    private synchronized void addLoadTask(Runnable task) {
        mTaskQueue.add(task);
        try {
            if (mPoolThreadHandler == null)
                mSemaphorePoolThreadHandler.acquire();
        } catch (InterruptedException e) {
        }
        mPoolThreadHandler.sendEmptyMessage(0x110);
    }

    private Runnable getATask() {
        return mTaskQueue.removeLast();
    }

    private void refreshBitmap( String key, final ImageView imageView,
                               Drawable drawable) {
        Message message = Message.obtain(mUIHandler, SET_IMAGE_VIEW);
        ImageMsgHolder holder = new ImageMsgHolder();
        holder.drawable = drawable;
        holder.key = key;
        holder.imageView = imageView;
        message.obj = holder;
        mUIHandler.sendMessage(message);
    }

    private static class ImageMsgHolder {
        Drawable drawable;
        ImageView imageView;
        String key;
    }
}
