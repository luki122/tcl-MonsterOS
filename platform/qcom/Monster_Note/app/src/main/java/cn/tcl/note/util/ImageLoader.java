/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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

import cn.tcl.note.R;

/**
 * load image quickly
 */
public class ImageLoader {
    //IMG Compression size
    public final static int IMG_16 = 4;
    public final static int IMG_2 = 2;
    public final static int IMG_0 = 1;
    private int mDefaultHeight = 0;
    private int mMaxHeight = 0;
    private float mImgWidth = 0;
    private static ImageLoader mInstance;
    private final String TAG = ImageLoader.class.getSimpleName();

    //back thread and handler
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;
    private ExecutorService mThreadPool;
    private final static int THREAD_NUM = 1;

    private LruCache<String, Bitmap> mLruCache;
    private LinkedList<Runnable> mTaskQueue;

    private Handler mUIHandler;
    private Semaphore mThreadPoolSemaphore = new Semaphore(THREAD_NUM);
    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);

    private int mInSampleSize;

    private Bitmap mLoseBitmap;
    private Drawable mImgBorder;

    private int mImgCannotLoad = 0;

    // init imageLoader
    private ImageLoader(int imgSize, Context context) {
        initResource(context);
        mInSampleSize = imgSize;
        initBackThread();
        int cacheMemory = ((int) Runtime.getRuntime().maxMemory()) / 8;
        NoteLog.d(TAG, "cacheMemory is " + cacheMemory);
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
        mThreadPool = Executors.newFixedThreadPool(THREAD_NUM);
        mTaskQueue = new LinkedList<>();
    }

    private void initResource(Context context) {
        mLoseBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.img_lose);
        mDefaultHeight = (int) context.getResources().getDimension(R.dimen.editor_img_defalt_height);
        mMaxHeight = (int) context.getResources().getDimension(R.dimen.editor_img_max_height);
        mImgBorder = context.getDrawable(R.drawable.edit_img_bordor);

        if (mInSampleSize == IMG_2) {
            mImgWidth = context.getResources().getDimension(R.dimen.show_img_width);
        } else {
            mImgWidth = context.getResources().getDimension(R.dimen.editor_img_width);
        }
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
                    mInstance.clearCache();
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
                            NoteLog.e(TAG, "Semaphore Interrupted Exception", e);
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
     * @param fileName
     */
    public void loadBitmap(final ImageView imageView, final String fileName) {
        NoteLog.d(TAG, "start load image:" + fileName);
        mImgCannotLoad++;
        imageView.setTag(fileName);
        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ImageHolder imageHolder = (ImageHolder) msg.obj;
                    ImageView view = imageHolder.mImageView;
                    String name = imageHolder.mFileName;
                    Bitmap bitmap = imageHolder.mBitmap;
                    if (view.getTag().toString().equals(name)) {
                        if (bitmap == null) {
                            bitmap = mLoseBitmap;
                            setMinShow(view);
                        } else {
                            setShowSize(view);
                        }
                        view.setImageBitmap(bitmap);
                        NoteLog.d(TAG, "END load iamge:" + name);
                    }
                    mImgCannotLoad--;
                }
            };
        }
        Bitmap bm = getBitmapFromLruCache(fileName);
        if (bm == null || mInSampleSize == IMG_0) {
            NoteLog.d(TAG, "load image:" + fileName + " from local");
            setMinShow(imageView);
            imageView.setImageResource(R.drawable.pic_defauft);
            addTask(buildTask(imageView, fileName));
        } else {
            NoteLog.d(TAG, "load image:" + fileName + " from cache");
            refreshBitmap(imageView, fileName, bm);
        }
    }

    private void setMinShow(ImageView imageView) {
        imageView.setBackground(mImgBorder);
        imageView.setMaxHeight(mDefaultHeight);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
    }

    private void setShowSize(ImageView imageView) {
        imageView.setBackground(null);
        imageView.setMaxHeight(mMaxHeight);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    private void addTask(Runnable task) {
        mTaskQueue.add(task);
        if (mPoolThreadHandler == null) {
            try {
                mSemaphorePoolThreadHandler.acquire();
            } catch (InterruptedException e) {
                NoteLog.e(TAG, "SemaphorePoolThreadHandler error", e);
            }
        }
        mPoolThreadHandler.sendEmptyMessage(1);
    }

    private Runnable buildTask(final ImageView imageView, final String fileName) {
        return new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmapFromFile(fileName);
                if (bitmap != null) {
                    addBitmapToLruCache(bitmap, fileName);
                }
                refreshBitmap(imageView, fileName, bitmap);
                mThreadPoolSemaphore.release();
            }
        };
    }

    private void addBitmapToLruCache(Bitmap bitmap, String fileName) {
        mLruCache.put(fileName, bitmap);
    }

    private Bitmap loadBitmapFromFile(String fileName) {
        String file = FileUtils.getPicWholePath(fileName);
        if (new File(file).exists()) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = getSampleSize(file);
            NoteLog.d(TAG, "bitmap mInSampleSize=" + bmOptions.inSampleSize);

            Bitmap bitmap = BitmapFactory.decodeFile(file, bmOptions);
            return bitmap;
        } else {
            NoteLog.d(TAG, "file don't exists,use default bitmap");
            return null;
        }
    }

    private int getSampleSize(String file) {
        if (mInSampleSize == IMG_0) {
            return 0;
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = (int) (photoW / mImgWidth);
        NoteLog.d(TAG, "mImgWidth=" + mImgWidth + "photoW=" + photoW + "  photoH=" + photoH + "  targetW=" + mImgWidth + "  scaleFactor=" + scaleFactor);
        return scaleFactor;
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

    public void clearCache() {
        if (mPoolThreadHandler != null) {
            mPoolThreadHandler.removeCallbacksAndMessages(null);
        }
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
        }

        mImgBorder = null;
        mLruCache.evictAll();
        mInstance = null;
    }

    private class ImageHolder {
        ImageView mImageView;
        String mFileName;
        Bitmap mBitmap;
    }

    public boolean iSloadFinish() {
        NoteLog.d(TAG, "mImgCannotLoad=" + mImgCannotLoad);
        return mImgCannotLoad == 0;
    }
}
