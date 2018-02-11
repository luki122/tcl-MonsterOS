/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import cn.tcl.meetingassistant.R;
import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * load image quickly
 */
public class ImageLoader {
    //IMG Compression size
    public final static int IMG_32 = 32;
    public final static int IMG_16 = 16;
    public final static int IMG_8 = 8;
    public final static int IMG_2 = 2;
    public final static int IMG_0 = 1;

    private static ImageLoader mInstance;
    private final String TAG = ImageLoader.class.getSimpleName();

    //back thread and handler
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;
    private ExecutorService mThreadPool;
    private final static int THREAD_NUM = 2;

    private LruCache<String, Bitmap> mLruCache;
    private LinkedList<Runnable> mTaskQueue;

    private Handler mUIHandler;
    private Semaphore mThreadPoolSemaphore = new Semaphore(THREAD_NUM);
    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);

    private int mInSampleSize;

    private Context mContext;

    // init imageLoader
    private ImageLoader(int imgSize, Context context) {
        mContext = context;
        mInSampleSize = imgSize;
        initBackThread();
        int cacheMemory = ((int) Runtime.getRuntime().maxMemory()) / 8;
        MeetingLog.d(TAG, "cacheMemory is " + cacheMemory);
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
        mThreadPool = Executors.newFixedThreadPool(THREAD_NUM);
        mTaskQueue = new LinkedList<>();
    }

    public static ImageLoader getInstance(int imgSize, Context context) {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(imgSize, context);
                }
            }
        } else {
            if (mInstance.mInSampleSize != imgSize) {
                synchronized (ImageLoader.class) {
                    mInstance = new ImageLoader(imgSize, context);
                }
            }
        }
        return mInstance;
    }

    private void initBackThread() {
        mPoolThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        mThreadPool.execute(getTask());
                        try {
                            mThreadPoolSemaphore.acquire();
                        } catch (InterruptedException e) {
                            MeetingLog.e(TAG, "Semaphore Interrupted Exception", e);
                        }
                    }
                };
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            }
        });
        mPoolThread.start();
    }

    private Runnable getTask() {
        return mTaskQueue.removeLast();
    }


    /**
     * load bitmap,if cache have the image,will return,if no,will load from sd card
     *
     * @param imageView
     * @param filePath
     */
    public void loadBitmap(final ImageView imageView, final String filePath,boolean isAdaptSize) {
        MeetingLog.d(TAG, "start load image:" + filePath);
        imageView.setTag(filePath);
        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ImageHolder imageHolder = (ImageHolder) msg.obj;
                    ImageView view = imageHolder.mImageView;
                    String name = imageHolder.mFileName;
                    Bitmap bitmap = imageHolder.mBitmap;
                    if (view.getTag().toString().equals(name)) {
                        view.setImageBitmap(bitmap);
                        MeetingLog.d(TAG, "END load iamge:" + name);
                    }
                }
            };
        }
        Bitmap bm = getBitmapFromLruCache(filePath);
        if (bm == null || mInSampleSize==IMG_0) {
            MeetingLog.d(TAG, "load image:" + filePath + " from local");
            //imageView.setImageResource(R.mipmap.ic_launcher);
            addTask(buildTask(imageView, filePath,isAdaptSize));
        } else {
            MeetingLog.d(TAG, "load image:" + filePath + " from cache");
            refreshBitmap(imageView, filePath, bm);
        }
    }

    private void addTask(Runnable task) {
        mTaskQueue.add(task);
        if (mPoolThreadHandler == null) {
            try {
                mSemaphorePoolThreadHandler.acquire();
            } catch (InterruptedException e) {
                MeetingLog.e(TAG, "SemaphorePoolThreadHandler error", e);
            }
        }
        mPoolThreadHandler.sendEmptyMessage(1);
    }

    private Runnable buildTask(final ImageView imageView, final String fileName, final boolean isAdaptSize) {
        return new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmapFromFile(fileName,isAdaptSize);
                addBitmapToLruCache(bitmap, fileName);
                refreshBitmap(imageView, fileName, bitmap);
                mThreadPoolSemaphore.release();
            }
        };
    }

    private void addBitmapToLruCache(Bitmap bitmap, String fileName) {
        try {
            mLruCache.put(fileName, bitmap);
        }catch (Exception e){
            MeetingLog.e(TAG,"bitmap   is " + bitmap );
            MeetingLog.e(TAG,"filename is " + fileName );
        }
    }

    private Bitmap loadBitmapFromFile(String filePath,boolean isAdapteSize) {
        if (new File(filePath).exists()) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = false;
            if(isAdapteSize){
                bmOptions.inSampleSize = getSampleSize(filePath);
            }else {
                bmOptions.inSampleSize = mInSampleSize;
            }
            MeetingLog.d(TAG, "bitmap mInSampleSize=" + mInSampleSize);
            Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmOptions);
            return bitmap;
        } else {
            MeetingLog.d(TAG, "file don't exists,use default bitmap");
            return null;
        }
    }

    private void refreshBitmap(ImageView imageView, String fileName, Bitmap bm) {
        ImageHolder imageHolder = new ImageHolder();
        imageHolder.mBitmap = bm;
        imageHolder.mFileName = fileName;
        imageHolder.mImageView = imageView;
        Message msg = Message.obtain();
        msg.obj = imageHolder;
        mUIHandler.sendMessage(msg);
    }

    private Bitmap getBitmapFromLruCache(String fileName) {
        return mLruCache.get(fileName);
    }

    public void removeBitmapFromLruCache(String fileName) {
        mLruCache.remove(fileName);
    }

    private class ImageHolder {
        ImageView mImageView;
        String mFileName;
        Bitmap mBitmap;
    }

    private float mImgWidth;

    private int getSampleSize(String file) {
        if (mInSampleSize == IMG_0) {
            return 0;
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        if (mImgWidth == 0) {
            if (mInSampleSize == IMG_16) {
                mImgWidth = mContext.getResources().getDimension(R.dimen.layout_common_285dp);
            } else if (mInSampleSize == IMG_2) {
                mImgWidth = mContext.getResources().getDimension(R.dimen.layout_common_285dp);
            }
        }
        int scaleFactor = (int) (photoW / mImgWidth) * 2;
        MeetingLog.d(TAG, "photoW=" + photoW + "  photoH=" + photoH + "  targetW=" + mImgWidth + "  scaleFactor=" + scaleFactor);
        return scaleFactor;
    }



}
