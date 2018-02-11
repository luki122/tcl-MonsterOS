package cn.tcl.music.view.image;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import cn.tcl.music.common.cache.CacheController;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.common.cache.ImageCache;

/**
 * This class wraps up completing some arbitrary long running work when loading a bitmap to an
 * ImageView. It handles things like using a memory and disk cache, running the work in a background
 * thread and setting a placeholder image.
 */
public abstract class ImageWorker {
    private static final String TAG = "ImageWorker";
    private static final int FADE_IN_TIME = 200;

    private Bitmap mLoadingBitmap;
    private boolean mFadeInBitmap = true;
    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();
    protected Resources mResources;

    protected CacheController mCacheController;

    protected int[] mEmptyImageResources;
    private LinkedHashMap<String, WeakReference<cn.tcl.music.view.image.AsyncTask>> taskCache = new LinkedHashMap<>();

    protected ImageWorker(Context context, int... emptyImageResources) {
        mResources = context.getResources();
        setEmptyImageRes(emptyImageResources);
        mCacheController = CacheController.getInstance(context);
    }

    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link ImageWorker#processBitmap(Object)} to define the processing logic). A memory and
     * disk cache will be used if an {@link ImageCache} has been added using
     * {@link ImageWorker#(android.app.FragmentManager, ImageCache.ImageCacheParams)}. If the
     * image is found in the memory cache, it is set immediately, otherwise an {@link cn.tcl.music.view.image.AsyncTask}
     * will be created to asynchronously load the bitmap.
     *
     * @param data      The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void loadImage(Object data, ImageView imageView) {
        if (data == null) {
            loadEmptyImageResources(imageView, true);
            return;
        }

        Bitmap bitmap = mCacheController.getBitmapFromMemCache(String.valueOf(data));

        if (bitmap != null) {
            // Bitmap found in memory cache
            imageView.setImageDrawable(new BitmapDrawable(mResources, bitmap));
        } else if (cancelPotentialWork(data, imageView)) {
            //BEGIN_INCLUDE(execute_background_task)
            final BitmapWorkerTask task = new BitmapWorkerTask(data, imageView);
            /**************************add by xiangxiang.liu 2015/12/05 减小内存开销 start************************************/
            taskCache.put(task.toString(), new WeakReference<cn.tcl.music.view.image.AsyncTask>(task));
            /**************************add by xiangxiang.liu 2015/12/05 减小内存开销 end************************************/

            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mResources, mLoadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);

            // NOTE: This uses a custom version of AsyncTask that has been pulled from the
            // framework and slightly modified. Refer to the docs at the top of the class
            // for more info on what was changed.
            task.executeOnExecutor(cn.tcl.music.view.image.AsyncTask.DUAL_THREAD_EXECUTOR);
            //END_INCLUDE(execute_background_task)
        }
    }

    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link ImageWorker#processBitmap(Object)} to define the processing logic). A memory and
     * disk cache will be used if an {@link ImageCache} has been added using
     * {@link ImageWorker#(android.app.FragmentManager, ImageCache.ImageCacheParams)}. If the
     * image is found in the memory cache, it is set immediately, otherwise an {@link cn.tcl.music.view.image.AsyncTask}
     * will be created to asynchronously load the bitmap.
     *
     * @param data      The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void loadImageAsMosaic(Object[] data, ImageView imageView, int tilesNumber) {
        if (data == null) {
            loadEmptyImageResources(imageView, true);
            return;
        }

        Bitmap bitmap = mCacheController.getBitmapFromMemCache(String.valueOf(data));

        if (bitmap != null) {
            // Bitmap found in memory cache
            imageView.setImageDrawable(new BitmapDrawable(mResources, bitmap));
        } else if (cancelPotentialWork(data, imageView)) {
            //BEGIN_INCLUDE(execute_background_task)
            final BitmapWorkerTask task = new BitmapWorkerTask(data, imageView, tilesNumber);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mResources, mLoadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);

            // NOTE: This uses a custom version of AsyncTask that has been pulled from the
            // framework and slightly modified. Refer to the docs at the top of the class
            // for more info on what was changed.
            task.executeOnExecutor(cn.tcl.music.view.image.AsyncTask.DUAL_THREAD_EXECUTOR);
            //END_INCLUDE(execute_background_task)
        }
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param bitmap
     */
    public void setLoadingImage(Bitmap bitmap) {
        mLoadingBitmap = bitmap;
    }

    /**
     * Set image to display when image provided is empty
     */

    public void setEmptyImageRes(int... imageResIds) {
        final int numImages = imageResIds.length;
        if (numImages <= 0)
            return;
        mEmptyImageResources = imageResIds;
    }

    public void loadEmptyImageResources(ImageView imageView, boolean shouldDoTheProcess) {
        if (mEmptyImageResources.length > 0) {
            int index = 0;
            if (imageView.getTag() != null) {
                long id = (Long) imageView.getTag();
                index = (int) (id % mEmptyImageResources.length);

            }

            if (mEmptyImageResources[index] < 0) {
                imageView.setImageDrawable(null);
                return;
            }
            if (shouldDoTheProcess)
                loadImage(mEmptyImageResources[index], imageView);
            else
                imageView.setImageResource(mEmptyImageResources[index]);
        } else {
            imageView.setImageDrawable(null);
        }
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param resId
     */
    public void setLoadingImage(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }

    /**
     * If set to true, the image will fade-in once it has been loaded by the background thread.
     */
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }

    /**
     * Subclasses should override this to define any processing or work that must happen to produce
     * the final bitmap. This will be executed in a background thread and be long running. For
     * example, you could resize a large bitmap here, or pull down an image from the network.
     *
     * @param data The data to identify which image to process, as provided by
     *             {@link ImageWorker#loadImage(Object, ImageView)}
     * @return The processed bitmap
     */
    protected abstract Bitmap processBitmap(Object data);

    protected abstract Bitmap processBitmap(Object data, int imageWidth, int imageHeight);

    protected abstract Bitmap processMosaicBitmap(Object[] dataArray, int numberOfTiles);

    /**
     * Cancels any pending work attached to the provided ImageView.
     *
     * @param imageView
     */
    public static void cancelWork(ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
        }
    }

    /**
     * Returns true if the current work has been canceled or if there was no work in
     * progress on this image view.
     * Returns false if the work in progress deals with the same data. The work is not
     * stopped in that case.
     */
    public boolean cancelPotentialWork(Object data, ImageView imageView) {
        //BEGIN_INCLUDE(cancel_potential_work)
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        /**************************add by xiangxiang.liu 2015/12/05 减小内存开销 start************************************/
        if (bitmapWorkerTask != null) {
            taskCache.put(bitmapWorkerTask.toString(), new WeakReference<cn.tcl.music.view.image.AsyncTask>(bitmapWorkerTask));
        }
        /**************************add by xiangxiang.liu 2015/12/05 减小内存开销 end************************************/
        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.mData;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
        //END_INCLUDE(cancel_potential_work)
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active work task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class BitmapWorkerTask extends cn.tcl.music.view.image.AsyncTask<Void, Void, BitmapDrawable> {
        private Object mData;
        private final WeakReference<ImageView> imageViewReference;
        private int mNumberOfTiles;

        public BitmapWorkerTask(Object data, ImageView imageView) {
            mData = data;
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        public BitmapWorkerTask(Object data, ImageView imageView, int numberOfTiles) {
            mData = data;
            imageViewReference = new WeakReference<ImageView>(imageView);
            mNumberOfTiles = numberOfTiles;
        }

        /**
         * Background processing.
         */
        @Override
        protected BitmapDrawable doInBackground(Void... params) {
            //BEGIN_INCLUDE(load_bitmap_in_background)

            final String dataString = String.valueOf(mData);
            Bitmap bitmap = null;
            BitmapDrawable drawable = null;

            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            ImageView currentImageView = getAttachedImageView();

            // If the image cache is available and this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
            if (!isCancelled() && currentImageView != null
                    && !mExitTasksEarly) {
                bitmap = mCacheController.getBitmapFromDiskCache(dataString);
            }

            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            if (bitmap == null && !isCancelled() && currentImageView != null
                    && !mExitTasksEarly) {
                if (mData instanceof Object[])
                    bitmap = processMosaicBitmap((Object[]) mData, mNumberOfTiles);
                else
                    bitmap = processBitmap(mData);
            }

            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null) {
                drawable = new BitmapDrawable(mResources, bitmap);
                mCacheController.addBitmapToCache(dataString, drawable);
            }

            return drawable;
            //END_INCLUDE(load_bitmap_in_background)
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(BitmapDrawable value) {
            //BEGIN_INCLUDE(complete_background_work)
            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || mExitTasksEarly) {
                value = null;
            }

            final ImageView imageView = getAttachedImageView();
            if (imageView != null) {
                //[BUGFIX] -Modify by TCTNJ-liang.guo,PR1059238, 2015-08-04 Begin
                try {
                    setImageDrawable(imageView, value);
                } catch (Exception e) {
                    Log.e(TAG, "Exception : " + e.getMessage());
                }
                //[BUGFIX] -Modify by TCTNJ-liang.guo,PR1059238, 2015-08-04 End
            }

            //END_INCLUDE(complete_background_work)
        }

        @Override
        protected void onCancelled(BitmapDrawable value) {
            super.onCancelled(value);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        /**
         * Returns the ImageView associated with this task as long as the ImageView's task still
         * points to this task as well. Returns null otherwise.
         */
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }

    /**
     * A custom Drawable that will be attached to the imageView while the work is in progress.
     * Contains a reference to the actual worker task, so that it can be stopped if a new binding is
     * required, and makes sure that only the last started worker process can bind its result,
     * independently of the finish order.
     */
    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    /**
     * Called when the processing is complete and the final drawable should be
     * set on the ImageView.
     *
     * @param imageView
     * @param drawable
     */
    private void setImageDrawable(ImageView imageView, Drawable drawable) {
        if (drawable == null) {
            loadEmptyImageResources(imageView, false);
            return;
        }
        if (mFadeInBitmap && imageView.getWindowToken() != null) {
            // Transition drawable with a transparent drawable and the final drawable
            final TransitionDrawable td =
                    new TransitionDrawable(new Drawable[]{
                            new ColorDrawable(Color.TRANSPARENT),
                            drawable
                    });
            // Set background to loading bitmap
            imageView.setBackground(
                    new BitmapDrawable(mResources, mLoadingBitmap));

            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageDrawable(drawable);
        }
    }

    /**
     * Pause any ongoing background work. This can be used as a temporary
     * measure to improve performance. For example background work could
     * be paused when a ListView or GridView is being scrolled using a
     * {@link android.widget.AbsListView.OnScrollListener} to keep
     * scrolling smooth.
     * <p/>
     * If work is paused, be sure setPauseWork(false) is called again
     * before your fragment or activity is destroyed (for example during
     * {@link android.app.Activity#onPause()}), or there is a risk the
     * background thread will never finish.
     */
    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    //[BUGFIX]-Modify by TCTNJ,liang.guo, 2015-10-22,PR1101733 Begin
    public void recyle() {
        if (mLoadingBitmap != null && !mLoadingBitmap.isRecycled()) {
            mLoadingBitmap.recycle();
            mLoadingBitmap = null;
            System.gc();
        }
    }
    //[BUGFIX]-Modify by TCTNJ,liang.guo, 2015-10-22,PR1101733 End

    public void removeAllTask(boolean mayInterruptIfRunning) {
        if (taskCache.size() > 0) {
            for (Map.Entry entry : taskCache.entrySet()) {
                cn.tcl.music.view.image.AsyncTask task = getTaskById((String) entry.getKey());
                LogUtil.d(TAG, "task key = " + entry.getKey() + ", value = " + task);
                if (task != null) {
                    task.cancel(mayInterruptIfRunning);
                    LogUtil.d(TAG, "cancel task key = " + entry.getKey());
                }
            }
            taskCache.clear();
        }
    }

    private cn.tcl.music.view.image.AsyncTask getTaskById(String taskId) {
        WeakReference<cn.tcl.music.view.image.AsyncTask> existTaskRef = taskCache.get(taskId);
        if (existTaskRef != null)
            return existTaskRef.get();
        return null;
    }
}